package controller;

import dto.request.CVScreenRequest;
import dto.response.CVExtractResponse;
import entity.Candidate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.CandidateService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidates")
@CrossOrigin(origins = "*")
public class CandidateController {

    private final CandidateService candidateService;

    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping("/screen")
    public ResponseEntity<?> screenCV(@RequestBody CVScreenRequest request) {
        try {
            CVExtractResponse response = candidateService.screenAndSaveCandidate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Lỗi chấm điểm CV: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getCandidates(@RequestParam(required = false) String status) {
        try {
            if (status != null && !status.isBlank()) {
                return ResponseEntity.ok(candidateService.getCandidatesByStatus(status));
            }
            return ResponseEntity.ok(candidateService.getAllCandidates());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Trạng thái không hợp lệ: " + status));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateCandidateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            CVExtractResponse updated = candidateService.updateCandidateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Lỗi cập nhật trạng thái: " + e.getMessage()));
        }
    }
}