package service;

import com.google.gson.reflect.TypeToken;
import dto.request.CVScreenRequest;
import dto.response.CVExtractResponse;
import util.ApiClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CandidateClientService {

    // 1. Ứng viên nộp CV cho AI chấm điểm -> POST /api/candidates/screen
    public CVExtractResponse screenCV(CVScreenRequest request) throws Exception {
        return ApiClient.post("/candidates/screen", request, CVExtractResponse.class);
    }

    // 2. Lấy toàn bộ danh sách ứng viên -> GET /api/candidates
    public List<CVExtractResponse> getAllCandidates() throws Exception {
        return ApiClient.getList("/candidates", new TypeToken<List<CVExtractResponse>>() {}.getType());
    }

    // 3. Lọc danh sách ứng viên theo trạng thái (PENDING, APPROVED, REJECTED, HIRED) -> GET /api/candidates?status=...
    public List<CVExtractResponse> getCandidatesByStatus(String status) throws Exception {
        String encodedStatus = URLEncoder.encode(status.trim(), StandardCharsets.UTF_8);
        return ApiClient.getList(
                "/candidates?status=" + encodedStatus,
                new TypeToken<List<CVExtractResponse>>() {}.getType()
        );
    }

    // 4. Tìm hồ sơ của ứng viên đang đăng nhập theo Email (để lấy sẵn candidateId, điểm CV & câu hỏi phỏng vấn)
    public CVExtractResponse getCandidateByEmail(String email) throws Exception {
        if (email == null || email.isBlank()) return null;
        List<CVExtractResponse> all = getAllCandidates();
        return all.stream()
                .filter(c -> c.getEmail() != null && c.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst()
                .orElse(null);
    }

    // 5. Manager duyệt hoặc từ chối thủ công ứng viên -> PUT /api/candidates/{id}/status?status=...
    public CVExtractResponse updateCandidateStatus(Long candidateId, String status) throws Exception {
        String encodedStatus = URLEncoder.encode(status.trim(), StandardCharsets.UTF_8);
        return ApiClient.put(
                "/candidates/" + candidateId + "/status?status=" + encodedStatus,
                null,
                CVExtractResponse.class
        );
    }
}