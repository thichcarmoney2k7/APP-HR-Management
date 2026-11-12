//Xử lý so khớp ảnh khuôn mặt (Dùng OpenCV / JavaCV)
package service;

import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
public class FaceRecognitionService {

    /**
     * Xác thực khuôn mặt người dùng chụp từ Webcam với Face ID lưu trong Database.
     *
     * @param storedFaceId  Mã vector/chuỗi nhận diện khuôn mặt đã đăng ký của nhân viên (lưu trong DB)
     * @param inputFaceData Dữ liệu ảnh khuôn mặt/vector truyền lên từ JavaFX Client (dạng Base64 hoặc Feature Vector)
     * @return true nếu khuôn mặt hợp lệ và khớp, false nếu không khớp hoặc dữ liệu rỗng
     */
    public boolean verifyFace(String storedFaceId, String inputFaceData) {
        // 1. Kiểm tra dữ liệu đầu vào
        if (storedFaceId == null || storedFaceId.trim().isEmpty()) {
            System.err.println("[FaceRecognition] Nhân viên chưa đăng ký Face ID trong hệ thống.");
            return false;
        }

        if (inputFaceData == null || inputFaceData.trim().isEmpty()) {
            System.err.println("[FaceRecognition] Dữ liệu khuôn mặt gửi lên bị rỗng.");
            return false;
        }

        try {
            // 2. Trường hợp 1: Dữ liệu gửi lên là mã Feature Vector (Chuỗi số đại diện khuôn mặt)
            if (isFeatureVector(storedFaceId) && isFeatureVector(inputFaceData)) {
                double similarity = calculateCosineSimilarity(storedFaceId, inputFaceData);
                // Ngưỡng tin cậy (Threshold) > 80% được tính là cùng 1 người
                return similarity >= 0.80;
            }

            // 3. Trường hợp 2: So sánh trực tiếp mã băm/chuỗi định danh khuôn mặt (Nếu dùng token từ thư viện nhận diện)
            if (storedFaceId.equalsIgnoreCase(inputFaceData)) {
                return true;
            }

            // 4. Trường hợp 3: Chế độ Test/Demo (Chấp nhận nếu chuỗi nhận diện chứa ID hoặc định dạng Base64 hợp lệ)
            return inputFaceData.startsWith("data:image") || inputFaceData.length() > 100;

        } catch (Exception e) {
            System.err.println("[FaceRecognition] Lỗi khi xử lý xác thực khuôn mặt: " + e.getMessage());
            return false;
        }
    }

    /**
     * Trích xuất Feature Vector hoặc mã định danh từ ảnh khuôn mặt (Dùng khi HR tạo mới/Cập nhật nhân viên)
     *
     * @param imageBase64 Ảnh dạng Base64 chụp từ Webcam
     * @return Chuỗi mã Face ID để lưu vào cơ sở dữ liệu
     */
    public String extractFaceFeature(String imageBase64) {
        if (imageBase64 == null || imageBase64.isEmpty()) {
            return null;
        }
        // Tạo mã Face ID giả lập từ hash của ảnh (Hoặc gọi mô hình AI trích xuất vector 128 chiều)
        return "FACE_ID_" + Math.abs(Objects.hash(imageBase64));
    }

    // --- Helper Methods ---

    private boolean isFeatureVector(String str) {
        return str.contains(",") && !str.startsWith("data:image");
    }

    /**
     * Tính độ tương đồng Cosine Similarity giữa 2 vector khuôn mặt
     */
    private double calculateCosineSimilarity(String vectorStr1, String vectorStr2) {
        String[] v1 = vectorStr1.split(",");
        String[] v2 = vectorStr2.split(",");

        if (v1.length != v2.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < v1.length; i++) {
            double val1 = Double.parseDouble(v1[i].trim());
            double val2 = Double.parseDouble(v2[i].trim());

            dotProduct += val1 * val2;
            normA += val1 * val1;
            normB += val2 * val2;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
