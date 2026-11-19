package util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ApiClient {

    // Chỉ cần sửa đúng 1 dòng này nếu đổi cổng hoặc chạy Server trên máy khác
    public static final String BASE_URL = "http://localhost:8080/api";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // Cấu hình Gson hỗ trợ sẵn LocalDate & LocalTime từ Spring Boot
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, type, ctx) ->
                    new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, ctx) ->
                    LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
            .registerTypeAdapter(LocalTime.class, (JsonSerializer<LocalTime>) (src, type, ctx) ->
                    new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_TIME)))
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, type, ctx) ->
                    LocalTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_TIME))
            .create();

    // 1. Gọi GET trả về 1 đối tượng
    public static <T> T get(String endpoint, Class<T> responseType) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json")
                .GET()
                .build();
        return sendRequest(request, responseType);
    }

    // 2. Gọi GET trả về danh sách (List<T>)
    public static <T> List<T> getList(String endpoint, Type listType) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json")
                .GET()
                .build();
        String json = sendRawRequest(request);
        return gson.fromJson(json, listType);
    }

    // 3. Gọi POST gửi dữ liệu JSON
    public static <T> T post(String endpoint, Object body, Class<T> responseType) throws Exception {
        String jsonBody = (body != null) ? gson.toJson(body) : "";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return sendRequest(request, responseType);
    }

    // 4. Gọi PUT cập nhật dữ liệu (hỗ trợ cả trường hợp không có body như nút Hoàn thành Task)
    public static <T> T put(String endpoint, Object body, Class<T> responseType) throws Exception {
        HttpRequest.BodyPublisher publisher = (body != null)
                ? HttpRequest.BodyPublishers.ofString(gson.toJson(body))
                : HttpRequest.BodyPublishers.noBody();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(publisher)
                .build();
        return sendRequest(request, responseType);
    }

    // 5. Gọi DELETE xóa dữ liệu
    public static void delete(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .DELETE()
                .build();
        sendRawRequest(request);
    }

    // --- Hàm lõi gửi request & bắt lỗi từ Server ---
    private static <T> T sendRequest(HttpRequest request, Class<T> responseType) throws Exception {
        String responseBody = sendRawRequest(request);
        if (responseType == Void.class || responseBody == null || responseBody.isBlank()) {
            return null;
        }
        return gson.fromJson(responseBody, responseType);
    }

    private static String sendRawRequest(HttpRequest request) throws Exception {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        String body = response.body();

        if (status >= 200 && status < 300) {
            return body;
        }

        // Bóc tách câu thông báo lỗi tiếng Việt từ JSON của Server (nếu có)
        String errorMessage = "Lỗi kết nối Server (Mã: " + status + ")";
        try {
            JsonObject jsonObj = JsonParser.parseString(body).getAsJsonObject();
            if (jsonObj.has("message")) {
                errorMessage = jsonObj.get("message").getAsString();
            }
        } catch (Exception ignored) {
        }
        throw new RuntimeException(errorMessage);
    }
}
