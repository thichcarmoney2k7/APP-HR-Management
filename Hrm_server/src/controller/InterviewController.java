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
}