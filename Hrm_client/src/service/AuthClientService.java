package service;

import dto.request.LoginRequest;
import dto.request.LogoutRequest;
import dto.request.RegisterRequest;
import dto.response.AuthResponse;
import util.ApiClient;
import util.SessionManager;

public class AuthClientService {

    // 1. Đăng ký tài khoản ứng viên mới -> gọi POST /api/auth/register
    public AuthResponse registerCandidate(RegisterRequest request) throws Exception {
        return ApiClient.post("/auth/register", request, AuthResponse.class);
    }

    // 2. Đăng nhập hệ thống -> gọi POST /api/auth/login và lưu phiên vào SessionManager
    public AuthResponse login(String username, String password) throws Exception {
        LoginRequest request = new LoginRequest(username, password);
        AuthResponse response = ApiClient.post("/auth/login", request, AuthResponse.class);

        if (response != null && response.isSuccess()) {
            SessionManager.setSession(
                    response.getUserId(),
                    response.getEmployeeId(),
                    response.getUsername(),
                    response.getFullName(),
                    response.getRole()
            );
        }
        return response;
    }

    // 3. Đăng xuất -> gọi POST /api/auth/logout và xóa SessionManager
    public void logout() {
        try {
            if (SessionManager.isLoggedIn()) {
                LogoutRequest request = new LogoutRequest(SessionManager.getUserId());
                ApiClient.post("/auth/logout", request, AuthResponse.class);
            }
        } catch (Exception ignored) {
            // Vẫn xóa session phía Client ngay cả khi Server không phản hồi
        } finally {
            SessionManager.clearSession();
        }
    }
}