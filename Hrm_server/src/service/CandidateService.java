package service;

import dto.request.CVScreenRequest;
import dto.response.CVExtractResponse;
import entity.Candidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.CandidateRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final GeminiService geminiService;

    @Autowired
    public CandidateService(CandidateRepository candidateRepository, GeminiService geminiService) {
        this.candidateRepository = candidateRepository;
        this.geminiService = geminiService;
    }

    /**
     * 1. Lọc CV tự động bằng AI và lưu ứng viên vào Database
     */
    public CVExtractResponse screenAndSaveCandidate(CVScreenRequest request) {
        // Tạo Prompt chuẩn gửi Gemini AI
        String prompt = String.format(
                "Bạn là chuyên gia tuyển dụng. Hãy phân tích và chấm điểm CV sau cho vị trí: %s.\n" +
                        "Nội dung CV:\n%s\n\n" +
                        "Yêu cầu trả về đúng định dạng:\n" +
                        "ĐIỂM: [Ghi duy nhất 1 số từ 0 đến 100]\n" +
                        "ĐÁNH GIÁ: [Nhận xét ngắn gọn 2-3 câu về độ phù hợp]",
                request.getPositionApplied(),
                request.getCvText()
        );

        // Gọi Gemini API
        String aiResponse = geminiService.callGeminiAPI(prompt);

        // Trích xuất điểm số từ phản hồi AI
        double aiScore = extractScoreFromAiResponse(aiResponse);

        // Khởi tạo và lưu Entity Candidate
        Candidate candidate = new Candidate();
        candidate.setFullName(request.getFullName());
        candidate.setEmail(request.getEmail());
        candidate.setPhone(request.getPhone());
        candidate.setPositionApplied(request.getPositionApplied());
        candidate.setYearsOfExperience(request.getYearsOfExperience());
        candidate.setCvText(request.getCvText());
        candidate.setAiScore(aiScore);
        candidate.setAiFeedback(aiResponse);
        candidate.setStatus(aiScore >= 70.0 ? "APPROVED" : "REJECTED");

        Candidate savedCandidate = candidateRepository.save(candidate);

        return mapToResponse(savedCandidate, "Lọc CV và lưu ứng viên thành công!");
    }

    /**
     * 2. Lấy danh sách tất cả ứng viên
     */
    public List<CVExtractResponse> getAllCandidates() {
        return candidateRepository.findAll()
                .stream()
                .map(c -> mapToResponse(c, "Thành công"))
                .collect(Collectors.toList());
    }

    /**
     * 3. Lấy chi tiết ứng viên theo ID
     */
    public CVExtractResponse getCandidateById(Long id) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(id);
        if (candidateOpt.isEmpty()) {
            CVExtractResponse response = new CVExtractResponse();
            response.setMessage("Không tìm thấy ứng viên có ID: " + id);
            return response;
        }
        return mapToResponse(candidateOpt.get(), "Thành công");
    }

    /**
     * 4. Cập nhật trạng thái ứng viên (APPROVED / REJECTED)
     */
    public CVExtractResponse updateCandidateStatus(Long id, String status) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(id);
        if (candidateOpt.isEmpty()) {
            CVExtractResponse response = new CVExtractResponse();
            response.setMessage("Không tìm thấy ứng viên!");
            return response;
        }

        Candidate candidate = candidateOpt.get();
        candidate.setStatus(status);
        Candidate updated = candidateRepository.save(candidate);

        return mapToResponse(updated, "Cập nhật trạng thái thành công!");
    }

    /**
     * 5. Xóa ứng viên
     */
    public boolean deleteCandidate(Long id) {
        if (candidateRepository.existsById(id)) {
            candidateRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // --- Helper Methods ---

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
            System.err.println("Lỗi bóc tách điểm AI: " + e.getMessage());
        }
        return 50.0; // Trả về điểm mặc định nếu AI không đúng định dạng
    }

    private CVExtractResponse mapToResponse(Candidate candidate, String message) {
        CVExtractResponse response = new CVExtractResponse();
        response.setCandidateId(candidate.getId());
        response.setFullName(candidate.getFullName());
        response.setEmail(candidate.getEmail());
        response.setPhone(candidate.getPhone());
        response.setPositionApplied(candidate.getPositionApplied());
        response.setYearsOfExperience(candidate.getYearsOfExperience());
        response.setAiScore(candidate.getAiScore());
        response.setAiFeedback(candidate.getAiFeedback());
        response.setStatus(candidate.getStatus());
        response.setInterviewQuestions(candidate.getInterviewQuestions());
        response.setMessage(message);
        return response;
    }
}