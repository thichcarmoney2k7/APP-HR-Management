package service;

import dto.request.CVScreenRequest;
import dto.response.CVExtractResponse;
import entity.Candidate;
import enums.CandidateStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import repository.CandidateRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CandidateService {
    @Value("${recruitment.default-passing-score:70.0}")
    private double defaultPassingScore;

    private final CandidateRepository candidateRepository;
    private final GeminiService geminiService;

    @Autowired
    public CandidateService(CandidateRepository candidateRepository, GeminiService geminiService) {
        this.candidateRepository = candidateRepository;
        this.geminiService = geminiService;
    }

    /**
     * 1. Lọc CV tự động bằng AI và lưu/cập nhật ứng viên vào Database
     */
    public CVExtractResponse screenAndSaveCandidate(CVScreenRequest request) {
        String prompt = String.format(
                "Bạn là chuyên gia tuyển dụng. Hãy phân tích và chấm điểm CV sau cho vị trí: %s.\n" +
                        "Nội dung CV:\n%s\n\n" +
                        "Yêu cầu trả về đúng định dạng:\n" +
                        "ĐIỂM: [Ghi duy nhất 1 số từ 0 đến 100]\n" +
                        "ĐÁNH GIÁ: [Nhận xét ngắn gọn 2-3 câu về độ phù hợp]",
                request.getPositionApplied(),
                request.getCvText()
        );

        String aiResponse = geminiService.callGeminiAPI(prompt);
        double aiScore = extractScoreFromAiResponse(aiResponse);

        // Nếu ứng viên đã đăng ký tài khoản trước đó qua email thì cập nhật hồ sơ, chưa có thì tạo mới
        Candidate candidate = (request.getEmail() != null && !request.getEmail().isBlank())
                ? candidateRepository.findByEmail(request.getEmail()).orElse(new Candidate())
                : new Candidate();


        double threshold = (request.getPassingScore() != null)
                ? request.getPassingScore()
                : defaultPassingScore;

        candidate.setFullName(request.getFullName());
        candidate.setEmail(request.getEmail());
        candidate.setPhone(request.getPhone());
        candidate.setPositionApplied(request.getPositionApplied());
        candidate.setYearsOfExperience(request.getYearsOfExperience());
        candidate.setCvText(request.getCvText());
        candidate.setAiScore(aiScore);
        candidate.setAiFeedback(aiResponse);

        // So sánh với mức điểm động thay vì 70.0 cố định
        candidate.setStatus(aiScore >= threshold ? CandidateStatus.APPROVED : CandidateStatus.REJECTED);

        Candidate savedCandidate = candidateRepository.save(candidate);

        return mapToResponse(savedCandidate, "Lọc CV thành công (Điểm chuẩn: " + threshold + ")!");
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
     * 4. Tìm ứng viên theo Email (Repo method 1)
     */
    public CVExtractResponse getCandidateByEmail(String email) {
        Optional<Candidate> candidateOpt = candidateRepository.findByEmail(email);
        if (candidateOpt.isEmpty()) {
            CVExtractResponse response = new CVExtractResponse();
            response.setMessage("Không tìm thấy ứng viên có email: " + email);
            return response;
        }
        return mapToResponse(candidateOpt.get(), "Thành công");
    }

    /**
     * 5. Lọc danh sách ứng viên theo vị trí ứng tuyển (Repo method 2)
     */
    public List<CVExtractResponse> getCandidatesByPosition(String positionApplied) {
        return candidateRepository.findByPositionApplied(positionApplied)
                .stream()
                .map(c -> mapToResponse(c, "Thành công"))
                .collect(Collectors.toList());
    }

    /**
     * 6. Lọc danh sách ứng viên theo trạng thái: PENDING, APPROVED, REJECTED, HIRED (Repo method 3)
     */
    public List<CVExtractResponse> getCandidatesByStatus(String status) {
        return candidateRepository.findByStatus(status)
                .stream()
                .map(c -> mapToResponse(c, "Thành công"))
                .collect(Collectors.toList());
    }

    /**
     * 7. Lọc danh sách ứng viên đạt điểm AI từ mức tối thiểu trở lên, sắp xếp giảm dần (Repo method 4)
     */
    public List<CVExtractResponse> getTopCandidatesByScore(Double minScore) {
        return candidateRepository.findByAiScoreGreaterThanEqualOrderByAiScoreDesc(minScore)
                .stream()
                .map(c -> mapToResponse(c, "Thành công"))
                .collect(Collectors.toList());
    }

    /**
     * 8. Cập nhật trạng thái ứng viên (APPROVED / REJECTED)
     */
    public CVExtractResponse updateCandidateStatus(Long id, String status) {
        Optional<Candidate> candidateOpt = candidateRepository.findById(id);
        if (candidateOpt.isEmpty()) {
            CVExtractResponse response = new CVExtractResponse();
            response.setMessage("Không tìm thấy ứng viên!");
            return response;
        }

        CandidateStatus statusEnum;
        try {
            statusEnum = CandidateStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception e) {
            CVExtractResponse response = new CVExtractResponse();
            response.setMessage("Trạng thái không hợp lệ: " + status);
            return response;
        }

        Candidate candidate = candidateOpt.get();
        candidate.setStatus(statusEnum);
        Candidate updated = candidateRepository.save(candidate);

        return mapToResponse(updated, "Cập nhật trạng thái thành công!");
    }

    /**
     * 9. Xóa ứng viên
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
        return 50.0;
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
        response.setStatus(candidate.getStatus() != null ? candidate.getStatus().name() : null);
        response.setInterviewQuestions(candidate.getInterviewQuestions());
        response.setMessage(message);
        return response;
    }
}

