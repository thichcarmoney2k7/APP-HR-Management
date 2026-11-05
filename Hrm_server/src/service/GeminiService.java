//Gọi AI: Trích xuất CV, Tạo câu hỏi phỏng vấn, Phân tích KPI
package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class GeminiService {

    // Lấy API Key tự động từ file application.properties
    @Value("${gemini.api.key}")
    private String apiKey;

    // Endpoint chính thức của Google Gemini API (sử dụng model gemini-2.0-flash nhanh và miễn phí)
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Hàm dùng chung để gửi Prompt tới Gemini AI và lấy câu trả lời dạng văn bản
     */
    public String callGeminiAPI(String promptText) {
        try {
            String requestUrl = GEMINI_API_URL + apiKey;

            // 1. Tạo Header HTTP (JSON)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 2. Tạo Body theo chuẩn cấu trúc JSON của Gemini API
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", promptText);

            Map<String, Object> contentObj = new HashMap<>();
            contentObj.put("parts", Collections.singletonList(textPart));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(contentObj));

            HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);

            // 3. Gửi Request POST tới Gemini API
            ResponseEntity<String> response = restTemplate.postForEntity(requestUrl, entity, String.class);

            // 4. Bóc tách JSON kết quả trả về từ Gemini
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            return textNode.asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi kết nối với AI Gemini: " + e.getMessage();
        }
    }
}