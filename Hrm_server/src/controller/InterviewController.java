package controller;

import dto.request.InterviewGenerateRequest;
import dto.response.InterviewResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.InterviewService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews")
@CrossOrigin(origins = "*")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    // 1. Sinh mới bộ câu hỏi phỏng vấn bằng Gemini AI
    @PostMapping("/generate")
    public ResponseEntity<?> generateQuestions(@RequestBody InterviewGenerateRequest request) {
        try {
            InterviewResponse response = interviewService.generateInterviewQuestions(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Lỗi sinh câu hỏi phỏng vấn: " + e.getMessage()));
        }
    }

    // 2. Lấy câu hỏi phỏng vấn của ứng viên (đã lưu trong DB, nếu chưa có mới gọi AI sinh)
    @GetMapping("/candidate/{id}")
    public ResponseEntity<?> getQuestionsByCandidate(@PathVariable Long id) {
        try {
            List<InterviewResponse> responses = interviewService.getInterviewsByCandidate(id);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 3. Nộp câu trả lời để Gemini AI chấm điểm phỏng vấn
    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluateInterview(@RequestBody Map<String, String> payload) {
        try {
            Long candidateId = Long.parseLong(payload.get("candidateId"));
            String answers = payload.get("answers");
            InterviewResponse response = interviewService.evaluateInterviewAnswers(candidateId, answers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Lỗi chấm điểm phỏng vấn: " + e.getMessage()));
        }
    }
}