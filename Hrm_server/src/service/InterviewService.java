package service;

import dto.request.InterviewGenerateRequest;
import dto.response.InterviewResponse;
import entity.Candidate;
import enums.CandidateStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import repository.CandidateRepository;

import java.util.List;
import java.util.Optional;

@Service
public class InterviewService {

    @Value("${recruitment.default-passing-score:70.0}")
    private double defaultPassingScore;

    private final CandidateRepository candidateRepository;
    private final GeminiService geminiService;

    @Autowired
    public InterviewService(CandidateRepository candidateRepository, GeminiService geminiService) {
        this.candidateRepository = candidateRepository;
        this.geminiService = geminiService;
    }

    /**
     * 1. Sinh bộ câu hỏi phỏng vấn mới bằng Gemini AI và lưu vào DB
     */
    public InterviewResponse generateInterviewQuestions(InterviewGenerateRequest request) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(request.getCandidateId());
        if (candidateOpt.isEmpty()) {
            InterviewResponse response = new InterviewResponse();
            response.setMessage("Không tìm thấy ứng viên!");
            return response;
        }

        Candidate candidate = candidateOpt.get();

        String position = (request.getPosition() != null && !request.getPosition().isBlank())
                ? request.getPosition()
                : (candidate.getPositionApplied() != null ? candidate.getPositionApplied() : "Chưa xác định");
        String cvText = candidate.getCvText() != null ? candidate.getCvText() : "Chưa có nội dung CV";
        String jobDesc = (request.getJobDescription() != null && !request.getJobDescription().isBlank())
                ? "\nMô tả công việc (JD):\n" + request.getJobDescription()
                : "";

        String prompt = String.format(
                "Bạn là chuyên gia tuyển dụng. Hãy tạo 5 câu hỏi phỏng vấn kỹ thuật và tình huống " +
                        "(đánh số từ 1 đến 5, không kèm đáp án) cho ứng viên ứng tuyển vị trí %s dựa trên thông tin sau:%s\nNội dung CV:\n%s",
                position, jobDesc, cvText
        );

        String questions = geminiService.callGeminiAPI(prompt);

        candidate.setInterviewQuestions(questions);
        candidateRepository.save(candidate);

        InterviewResponse response = new InterviewResponse();
        response.setCandidateId(candidate.getId());
        response.setPosition(position);
        response.setGeneratedQuestions(questions);
        response.setMessage("Tạo bộ câu hỏi phỏng vấn thành công!");

        return response;
    }

    /**
     * 2. Lấy bộ câu hỏi phỏng vấn đã lưu trong DB (Không tốn lượt gọi AI)
     */
    public InterviewResponse getSavedInterviewQuestions(Long candidateId) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(candidateId);
        InterviewResponse response = new InterviewResponse();

        if (candidateOpt.isEmpty()) {
            response.setMessage("Không tìm thấy ứng viên!");
            return response;
        }

        Candidate candidate = candidateOpt.get();
        response.setCandidateId(candidate.getId());
        response.setPosition(candidate.getPositionApplied());
        response.setGeneratedQuestions(candidate.getInterviewQuestions());
        response.setMessage(candidate.getInterviewQuestions() != null
                ? "Lấy bộ câu hỏi thành công!"
                : "Ứng viên chưa tạo câu hỏi phỏng vấn.");

        return response;
    }

    /**
     * 3. Lấy danh sách phỏng vấn của ứng viên (Chỉ sinh mới khi DB chưa có câu hỏi)
     */
    public List<InterviewResponse> getInterviewsByCandidate(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ứng viên có ID: " + candidateId));

        // Nếu đã có câu hỏi lưu sẵn trong DB thì trả về luôn, tránh gọi lại AI gây đổi đề
        if (candidate.getInterviewQuestions() != null && !candidate.getInterviewQuestions().isBlank()) {
            return List.of(getSavedInterviewQuestions(candidateId));
        }

        InterviewGenerateRequest request = new InterviewGenerateRequest();
        request.setCandidateId(candidate.getId());
        request.setPosition(candidate.getPositionApplied());
        request.setJobDescription("");

        return List.of(generateInterviewQuestions(request));
    }

    /**
     * 4. Chấm điểm câu trả lời phỏng vấn của ứng viên bằng Gemini AI
     */
    public InterviewResponse evaluateInterviewAnswers(Long candidateId, String candidateAnswers) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(candidateId);
        InterviewResponse response = new InterviewResponse();

        if (candidateOpt.isEmpty()) {
            response.setMessage("Không tìm thấy ứng viên!");
            return response;
        }

        Candidate candidate = candidateOpt.get();
        if (candidate.getInterviewQuestions() == null || candidate.getInterviewQuestions().isBlank()) {
            response.setMessage("Ứng viên chưa có bộ câu hỏi phỏng vấn để chấm điểm!");
            return response;
        }

        if (candidateAnswers == null || candidateAnswers.isBlank()) {
            response.setMessage("Câu trả lời phỏng vấn không được để trống!");
            return response;
        }

        String prompt = String.format(
                "Bạn là giám khảo phỏng vấn chuyên môn cho vị trí: %s.\n" +
                        "Bộ câu hỏi phỏng vấn:\n%s\n\n" +
                        "Câu trả lời của ứng viên:\n%s\n\n" +
                        "Hãy chấm điểm và nhận xét mức độ chính xác, tư duy giải quyết vấn đề. Trả về đúng định dạng:\n" +
                        "ĐIỂM: [Ghi duy nhất 1 số từ 0 đến 100]\n" +
                        "ĐÁNH GIÁ: [Nhận xét ngắn gọn 2-3 câu]",
                candidate.getPositionApplied(),
                candidate.getInterviewQuestions(),
                candidateAnswers
        );

        String aiEvaluation = geminiService.callGeminiAPI(prompt);
        double interviewScore = extractScoreFromAiResponse(aiEvaluation);
        boolean isPassed = interviewScore >= defaultPassingScore;

        // Nối kết quả phỏng vấn vào aiFeedback để HR/Manager xem được cả điểm CV lẫn điểm phỏng vấn
        String previousFeedback = (candidate.getAiFeedback() != null && !candidate.getAiFeedback().isBlank())
                ? candidate.getAiFeedback() + "\n\n"
                : "";
        candidate.setAiFeedback(previousFeedback + "--- KẾT QUẢ PHỎNG VẤN AI ---\n" + aiEvaluation);
        candidate.setAiScore(interviewScore);

        if (!isPassed) {
            candidate.setStatus(CandidateStatus.REJECTED);
        }
        candidateRepository.save(candidate);

        response.setCandidateId(candidate.getId());
        response.setPosition(candidate.getPositionApplied());
        response.setGeneratedQuestions(candidate.getInterviewQuestions());
        response.setMessage((isPassed ? "ĐẠT PHỎNG VẤN" : "CHƯA ĐẠT PHỎNG VẤN")
                + " (" + interviewScore + "/100 điểm).\n" + aiEvaluation);

        return response;
    }

    // --- Helper bóc tách điểm từ phản hồi Gemini ---
    private double extractScoreFromAiResponse(String aiResponse) {
        try {
            if (aiResponse != null && aiResponse.contains("ĐIỂM:")) {
                String scoreLine = aiResponse.split("ĐIỂM:")[1].split("\n")[0].trim();
                String cleanedScore = scoreLine.replaceAll("[^0-9.]", "");
                if (!cleanedScore.isEmpty()) {
                    return Double.parseDouble(cleanedScore);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi bóc tách điểm phỏng vấn AI: " + e.getMessage());
        }
        return 50.0;
    }
}

