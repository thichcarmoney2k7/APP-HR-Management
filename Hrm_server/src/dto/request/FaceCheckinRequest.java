//Gửi ảnh chụp khuôn mặt từ Camera
package dto.request;

public class FaceCheckinRequest {

    private Long employeeId;
    private String imageBase64; // Dữ liệu ảnh dạng chuỗi Base64 chụp từ Webcam JavaFX

    public FaceCheckinRequest() {
    }

    public FaceCheckinRequest(Long employeeId, String imageBase64) {
        this.employeeId = employeeId;
        this.imageBase64 = imageBase64;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}