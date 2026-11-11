package service;

import dto.request.InterviewGenerateRequest;
import dto.response.InterviewResponse;
import entity.Candidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.CandidateRepository;

import java.util.Optional;

@Service
public class InterviewService {

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

        // Tránh null khi đưa vào Prompt
        String position = candidate.getPositionApplied() != null ? candidate.getPositionApplied() : "Chưa xác định";
        String cvText = candidate.getCvText() != null ? candidate.getCvText() : "Chưa có nội dung CV";

        // 1. Tạo Prompt sinh câu hỏi phỏng vấn
        String prompt = String.format(
                "Bạn là chuyên gia tuyển dụng. Hãy tạo 5 câu hỏi phỏng vấn kỹ thuật và tình huống cho ứng viên ứng tuyển vị trí %s dựa trên CV sau:\n%s",
                position, cvText
        );

        // 2. Gọi Gemini AI
        String questions = geminiService.callGeminiAPI(prompt);

        // 3. Lưu câu hỏi vào Candidate
        candidate.setInterviewQuestions(questions);
        candidateRepository.save(candidate);

        // 4. Trả response
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
        response.setMessage(candidate.getInterviewQuestions() != null ? "Lấy bộ câu hỏi thành công!" : "Ứng viên chưa tạo câu hỏi phỏng vấn.");

        return response;
    }
}