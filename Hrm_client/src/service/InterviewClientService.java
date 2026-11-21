package service;

import com.google.gson.reflect.TypeToken;
import dto.request.InterviewGenerateRequest;
import dto.response.InterviewResponse;
import util.ApiClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterviewClientService {

    // 1. Yêu cầu AI sinh bộ câu hỏi mới -> POST /api/interviews/generate
    public InterviewResponse generateQuestions(InterviewGenerateRequest request) throws Exception {
        return ApiClient.post("/interviews/generate", request, InterviewResponse.class);
    }

    // 2. Lấy bộ câu hỏi phỏng vấn của ứng viên (ưu tiên lấy câu đã lưu trong DB) -> GET /api/interviews/candidate/{id}
    public InterviewResponse getOrCreateInterviewQuestions(Long candidateId) throws Exception {
        List<InterviewResponse> list = ApiClient.getList(
                "/interviews/candidate/" + candidateId,
                new TypeToken<List<InterviewResponse>>() {}.getType()
        );
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    // 3. Gửi câu trả lời phỏng vấn cho Gemini AI chấm điểm -> POST /api/interviews/evaluate
    public InterviewResponse evaluateInterview(Long candidateId, String answers) throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("candidateId", String.valueOf(candidateId));
        payload.put("answers", answers);
        return ApiClient.post("/interviews/evaluate", payload, InterviewResponse.class);
    }
}
