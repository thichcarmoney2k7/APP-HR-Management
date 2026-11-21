package Controller;

import dto.request.RegisterRequest;
import dto.response.AuthResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.AuthClientService;

import java.awt.*;

public class AuthController {

    // --- Các thành phần giao diện form Đăng nhập ---
    @FXML private TextField txtLoginUsername; // Nhập Email (với Candidate) hoặc Username
    @FXML private PasswordField txtLoginPassword;
    @FXML private Button btnLogin;

    // --- Các thành phần giao diện form Đăng ký Ứng viên ---
    @FXML private TextField txtRegFullName;
    @FXML private TextField txtRegEmail;
    @FXML private PasswordField txtRegPassword;
    @FXML private TextField txtRegPhone;
    @FXML private TextField txtRegPosition;
    @FXML private TextField txtRegExperience;
    @FXML private TextArea txtRegCvText;
    @FXML private Button btnRegister;

    // --- Nhãn hiển thị thông báo ---
    @FXML private Label lblStatus;

    private final AuthClientService authService = new AuthClientService();

    // Xử lý sự kiện nút Đăng nhập
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = txtLoginUsername.getText().trim();
        String password = txtLoginPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!", true);
            return;
        }

        btnLogin.setDisable(true);
        showStatus("Đang kiểm tra đăng nhập...", false);

        new Thread(() -> {
            try {
                AuthResponse response = authService.login(username, password);
                Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    if (response != null && response.isSuccess()) {
                        showStatus(response.getMessage(), false);
                        routeByRole(event, response.getRole());
                    } else {
                        String msg = (response != null) ? response.getMessage() : "Đăng nhập thất bại!";
                        showStatus(msg, true);
                    }
                });
            } catch (Exception e) {
                // ApiClient tự động bóc trường "message" khi Server trả lỗi 401/400
                Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    showStatus(e.getMessage(), true);
                });
            }
        }).start();
    }

    // Xử lý sự kiện nút Đăng ký ứng viên
    @FXML
    public void handleRegister(ActionEvent event) {
        String fullName = txtRegFullName.getText().trim();
        String email = txtRegEmail.getText().trim();
        String password = txtRegPassword.getText().trim();
        String phone = (txtRegPhone != null) ? txtRegPhone.getText().trim() : "";
        String position = (txtRegPosition != null) ? txtRegPosition.getText().trim() : "";
        String cvText = (txtRegCvText != null) ? txtRegCvText.getText().trim() : "";

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showStatus("Vui lòng nhập đầy đủ Họ tên, Email và Mật khẩu!", true);
            return;
        }

        int yearsOfExperience = 0;
        if (txtRegExperience != null && !txtRegExperience.getText().trim().isEmpty()) {
            try {
                yearsOfExperience = Integer.parseInt(txtRegExperience.getText().trim());
            } catch (NumberFormatException e) {
                showStatus("Số năm kinh nghiệm phải là số nguyên!", true);
                return;
            }
        }

        // Server lấy email làm username để đồng bộ khi nâng quyền lên Employee
        RegisterRequest request = new RegisterRequest(
                email,
                password,
                fullName,
                email,
                phone,
                position,
                yearsOfExperience,
                cvText
        );

        btnRegister.setDisable(true);
        showStatus("Đang đăng ký tài khoản ứng viên...", false);

        new Thread(() -> {
            try {
                AuthResponse response = authService.registerCandidate(request);
                Platform.runLater(() -> {
                    btnRegister.setDisable(false);
                    if (response != null && response.isSuccess()) {
                        showStatus(response.getMessage() + " Hãy đăng nhập bằng Email vừa tạo.", false);
                        txtLoginUsername.setText(email);
                        txtLoginPassword.clear();
                    } else {
                        String msg = (response != null) ? response.getMessage() : "Đăng ký thất bại!";
                        showStatus(msg, true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btnRegister.setDisable(false);
                    showStatus(e.getMessage(), true);
                });
            }
        }).start();
    }

    // Điều hướng màn hình theo vai trò (Role)
    private void routeByRole(ActionEvent event, String role) {
        if (role == null) return;

        switch (role.toUpperCase()) {
            case "CANDIDATE":
                // Chuyển sang màn hình Nộp CV cho AI chấm & Phỏng vấn AI (UserInterviewController)
                switchScene(event, "/view/user_interview.fxml", "Cổng Ứng Viên - Chấm CV & Phỏng Vấn AI");
                break;

            case "EMPLOYEE":
                // Chuyển sang màn hình Check-in khuôn mặt / Nhận Task & Xem KPI
                switchScene(event, "/view/user_attendance.fxml", "Nhân Viên - Chấm Công & Công Việc");
                break;

            case "MANAGER":
            case "ADMIN":
                // Chuyển sang cụm màn hình Quản trị
                switchScene(event, "/view/admin_employee_mgmt.fxml", "Quản Lý Nhân Sự & Đánh Giá KPI");
                break;

            default:
                showStatus("Không xác định được quyền truy cập: " + role, true);
        }
    }

    private void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            showStatus("Không tìm thấy giao diện " + fxmlPath + " (kiểm tra lại đường dẫn file .fxml)", true);
            e.printStackTrace();
        }
    }

    private void showStatus(String message, boolean isError) {
        if (lblStatus != null) {
            lblStatus.setText(message);
            lblStatus.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        }
    }
}
