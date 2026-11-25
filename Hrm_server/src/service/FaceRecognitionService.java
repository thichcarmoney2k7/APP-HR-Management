package service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class FaceRecognitionService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    // Dùng đúng model và base URL giống cấu hình GeminiService hiện tại của bạn
    @Value("${gemini.model:gemini-2.5-flash}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Đối chiếu ảnh khuôn mặt mẫu (lúc đăng ký) và ảnh chụp lúc Check-in qua Gemini AI.
     * Trả về true nếu Gemini xác nhận là cùng một người.
     */
    public boolean verifyFace(String registeredFaceBase64, String capturedFaceBase64) {
        if (registeredFaceBase64 == null || registeredFaceBase64.isBlank()
                || capturedFaceBase64 == null || capturedFaceBase64.isBlank()) {
            return false;
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình gemini.api.key cho FaceRecognitionService!");
        }

        try {
            String cleanRegistered = stripBase64Prefix(registeredFaceBase64);
            String cleanCaptured = stripBase64Prefix(capturedFaceBase64);

            String prompt = """
                    Bạn là hệ thống xác thực khuôn mặt chấm công.
                    Ảnh thứ nhất là ảnh khuôn mặt mẫu đã đăng ký của nhân viên.
                    Ảnh thứ hai là ảnh chụp trực tiếp từ webcam lúc nhân viên check-in.
                    Hãy so sánh đặc điểm khuôn mặt của hai ảnh.
                    Chỉ trả lời đúng một dòng theo định dạng sau, không giải thích thêm:
                    MATCH: YES
                    hoặc
                    MATCH: NO
                    """;

            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", prompt);

            JsonObject image1Part = buildInlineImagePart(cleanRegistered, "image/jpeg");
            JsonObject image2Part = buildInlineImagePart(cleanCaptured, "image/jpeg");

            JsonArray parts = new JsonArray();
            parts.add(textPart);
            parts.add(image1Part);
            parts.add(image2Part);

            JsonObject content = new JsonObject();
            content.add("parts", parts);

            JsonArray contents = new JsonArray();
            contents.add(content);

            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.0);

            JsonObject body = new JsonObject();
            body.add("contents", contents);
            body.add("generationConfig", generationConfig);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            String aiReply = extractTextFromGeminiResponse(response.getBody());
            return aiReply != null && aiReply.toUpperCase().contains("MATCH: YES");
        } catch (Exception e) {
            System.err.println("Lỗi xác thực khuôn mặt qua Gemini AI: " + e.getMessage());
            return false;
        }
    }

    private JsonObject buildInlineImagePart(String base64Data, String mimeType) {
        JsonObject inlineData = new JsonObject();
        inlineData.addProperty("mime_type", mimeType);
        inlineData.addProperty("data", base64Data);

        JsonObject part = new JsonObject();
        part.add("inline_data", inlineData);
        return part;
    }

    private String stripBase64Prefix(String base64) {
        String trimmed = base64.trim();
        int commaIndex = trimmed.indexOf(',');
        if (trimmed.startsWith("data:") && commaIndex != -1) {
            return trimmed.substring(commaIndex + 1);
        }
        return trimmed;
    }

    private String extractTextFromGeminiResponse(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return "";
        JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();
        JsonArray candidates = root.getAsJsonArray("candidates");
        if (candidates == null || candidates.isEmpty()) return "";

        JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
        if (content == null) return "";

        JsonArray parts = content.getAsJsonArray("parts");
        if (parts == null || parts.isEmpty()) return "";

        return parts.get(0).getAsJsonObject().get("text").getAsString();
    }
}