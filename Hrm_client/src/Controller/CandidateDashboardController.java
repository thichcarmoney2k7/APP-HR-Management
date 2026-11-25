package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import util.SessionManager;
import util.WebcamUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

public class CandidateDashboardController {

    @FXML private BorderPane mainBorderPane;
    @FXML private Label lblWelcome;
    @FXML private Label lblCurrentPage;
    @FXML private Button btnBack;
    @FXML private Button btnForward;
    @FXML private Button btnNavInterview;

    private static final String ACTIVE_BTN_STYLE =
            "-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16; -fx-font-size: 14px; -fx-background-radius: 6;";

    private final Deque<String> backStack = new ArrayDeque<>();
    private final Deque<String> forwardStack = new ArrayDeque<>();
    private String currentViewPath;

    @FXML
    public void initialize() {
        String name = SessionManager.getFullName() != null
                ? SessionManager.getFullName()
                : SessionManager.getUsername();
        if (lblWelcome != null && name != null) {
            lblWelcome.setText("Ứng viên: " + name);
        }
        if (btnNavInterview != null) {
            btnNavInterview.setStyle(ACTIVE_BTN_STYLE);
        }

        navigateTo("/view/UserInterview.fxml", "Nộp CV & Phỏng vấn AI", true);
    }

    @FXML
    public void showInterview() {
        navigateTo("/view/UserInterview.fxml", "Nộp CV & Phỏng vấn AI", true);
    }

    @FXML
    public void handleBack() {
        if (backStack.isEmpty()) return;
        if (currentViewPath != null) {
            forwardStack.push(currentViewPath);
        }
        String target = backStack.pop();
        navigateTo(target, resolveTitle(target), false);
    }

    @FXML
    public void handleForward() {
        if (forwardStack.isEmpty()) return;
        if (currentViewPath != null) {
            backStack.push(currentViewPath);
        }
        String target = forwardStack.pop();
        navigateTo(target, resolveTitle(target), false);
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        WebcamUtils.stopCamera();
        SessionManager.clear();
        switchScene(event, "/view/Login.fxml", "AI HRM System - Đăng nhập");
    }

    private void navigateTo(String fxmlPath, String title, boolean recordHistory) {
        if (fxmlPath.equals(currentViewPath)) return;

        try {
            WebcamUtils.stopCamera();
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Không tìm thấy FXML: " + fxmlPath);
                return;
            }
            Parent view = FXMLLoader.load(resource);

            if (recordHistory && currentViewPath != null) {
                backStack.push(currentViewPath);
                forwardStack.clear();
            }

            currentViewPath = fxmlPath;
            if (mainBorderPane != null) {
                mainBorderPane.setCenter(view);
            }
            if (lblCurrentPage != null) {
                lblCurrentPage.setText(title);
            }
            updateNavButtons();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateNavButtons() {
        if (btnBack != null) btnBack.setDisable(backStack.isEmpty());
        if (btnForward != null) btnForward.setDisable(forwardStack.isEmpty());
    }

    private String resolveTitle(String path) {
        return switch (path) {
            case "/view/UserInterview.fxml" -> "Nộp CV & Phỏng vấn AI";
            default -> "Cổng Ứng viên";
        };
    }

    private void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}