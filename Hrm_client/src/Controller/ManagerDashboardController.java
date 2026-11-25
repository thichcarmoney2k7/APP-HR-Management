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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.SessionManager;
import util.WebcamUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class ManagerDashboardController {

    private static final String VIEW_OVERVIEW = "OVERVIEW";
    private static final String VIEW_ATTENDANCE = "/src/view/Manager/AdminAttendance.fxml";
    private static final String VIEW_CV_SCREENING = "/src/view/Manager/AdminCVScreening.fxml";
    private static final String VIEW_EMPLOYEE_MGMT = "/src/view/Manager/AdminEmployeeMgmt.fxml";
    private static final String VIEW_TASK_MGMT = "/src/view/Manager/AdminTask.fxml";
    private static final String VIEW_KPI_MGMT = "/src/view/Manager/AdminKPI.fxml";

    @FXML private BorderPane mainBorderPane;
    @FXML private VBox contentContainer;
    @FXML private VBox overviewView;

    @FXML private Label lblWelcome;
    @FXML private Label lblRoleBadge;
    @FXML private Label lblCurrentPage;

    @FXML private Button btnBack;
    @FXML private Button btnForward;

    @FXML private Button btnNavOverview;
    @FXML private Button btnNavAttendance;
    @FXML private Button btnNavCV;
    @FXML private Button btnNavEmployee;
    @FXML private Button btnNavTask;
    @FXML private Button btnNavKPI;

    private static final String DEFAULT_BTN_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #e2e8f0; -fx-alignment: CENTER_LEFT; -fx-padding: 11 14; -fx-font-size: 13.5px; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String ACTIVE_BTN_STYLE =
            "-fx-background-color: linear-gradient(to right, #2563eb, #4f46e5); -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 11 14; -fx-font-size: 13.5px; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(37, 99, 235, 0.35), 10, 0, 0, 2);";

    private final Deque<String> backStack = new ArrayDeque<>();
    private final Deque<String> forwardStack = new ArrayDeque<>();
    private String currentView;

    @FXML
    public void initialize() {
        String name = SessionManager.getFullName() != null && !SessionManager.getFullName().isBlank()
                ? SessionManager.getFullName()
                : (SessionManager.getUsername() != null ? SessionManager.getUsername() : "Quản lý");

        if (lblWelcome != null) {
            lblWelcome.setText(name);
        }
        if (lblRoleBadge != null && SessionManager.getRole() != null) {
            lblRoleBadge.setText("Vai trò: " + SessionManager.getRole().toUpperCase());
        }

        currentView = VIEW_OVERVIEW;
        if (lblCurrentPage != null) {
            lblCurrentPage.setText(resolveTitle(VIEW_OVERVIEW));
        }
        setActiveButton(VIEW_OVERVIEW);
        updateNavButtons();
    }

    @FXML
    public void showOverview() {
        navigateTo(VIEW_OVERVIEW, resolveTitle(VIEW_OVERVIEW), true);
    }

    @FXML
    public void showAttendance() {
        navigateTo(VIEW_ATTENDANCE, resolveTitle(VIEW_ATTENDANCE), true);
    }

    @FXML
    public void showCVScreening() {
        navigateTo(VIEW_CV_SCREENING, resolveTitle(VIEW_CV_SCREENING), true);
    }

    @FXML
    public void showEmployeeMgmt() {
        navigateTo(VIEW_EMPLOYEE_MGMT, resolveTitle(VIEW_EMPLOYEE_MGMT), true);
    }

    @FXML
    public void showTaskMgmt() {
        navigateTo(VIEW_TASK_MGMT, resolveTitle(VIEW_TASK_MGMT), true);
    }

    @FXML
    public void showKPIMgmt() {
        navigateTo(VIEW_KPI_MGMT, resolveTitle(VIEW_KPI_MGMT), true);
    }

    @FXML
    public void handleBack() {
        if (backStack.isEmpty()) return;
        if (currentView != null) {
            forwardStack.push(currentView);
        }
        String target = backStack.pop();
        navigateTo(target, resolveTitle(target), false);
    }

    @FXML
    public void handleForward() {
        if (forwardStack.isEmpty()) return;
        if (currentView != null) {
            backStack.push(currentView);
        }
        String target = forwardStack.pop();
        navigateTo(target, resolveTitle(target), false);
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        WebcamUtils.stopCamera();
        SessionManager.clear();
        switchScene(event, "/src/view/Login/Login.fxml", "AI HRM System - Đăng nhập");
    }

    private void navigateTo(String targetView, String title, boolean recordHistory) {
        if (targetView.equals(currentView)) return;

        try {
            WebcamUtils.stopCamera();
            Parent viewNode;

            if (VIEW_OVERVIEW.equals(targetView)) {
                if (overviewView == null) return;
                viewNode = overviewView;
            } else {
                URL resource = getClass().getResource(targetView);
                if (resource == null) {
                    System.err.println("Không tìm thấy FXML: " + targetView);
                    return;
                }
                viewNode = FXMLLoader.load(resource);
            }

            if (recordHistory && currentView != null) {
                backStack.push(currentView);
                forwardStack.clear();
            }

            currentView = targetView;
            if (contentContainer != null) {
                contentContainer.getChildren().setAll(viewNode);
            } else if (mainBorderPane != null) {
                mainBorderPane.setCenter(viewNode);
            }
            if (lblCurrentPage != null) {
                lblCurrentPage.setText(title);
            }
            setActiveButton(targetView);
            updateNavButtons();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(String viewKey) {
        Button active = switch (viewKey) {
            case VIEW_ATTENDANCE -> btnNavAttendance;
            case VIEW_CV_SCREENING -> btnNavCV;
            case VIEW_EMPLOYEE_MGMT -> btnNavEmployee;
            case VIEW_TASK_MGMT -> btnNavTask;
            case VIEW_KPI_MGMT -> btnNavKPI;
            default -> btnNavOverview;
        };
        for (Button btn : List.of(btnNavOverview, btnNavAttendance, btnNavCV, btnNavEmployee, btnNavTask, btnNavKPI)) {
            if (btn != null) {
                btn.setStyle(btn == active ? ACTIVE_BTN_STYLE : DEFAULT_BTN_STYLE);
            }
        }
    }

    private void updateNavButtons() {
        if (btnBack != null) btnBack.setDisable(backStack.isEmpty());
        if (btnForward != null) btnForward.setDisable(forwardStack.isEmpty());
    }

    private String resolveTitle(String viewKey) {
        return switch (viewKey) {
            case VIEW_ATTENDANCE -> "Chấm công Face ID & Đăng ký khuôn mặt";
            case VIEW_CV_SCREENING -> "Sàng lọc CV & Tuyển dụng AI";
            case VIEW_EMPLOYEE_MGMT -> "Quản lý Hồ sơ Nhân sự";
            case VIEW_TASK_MGMT -> "Quản lý & Giao việc";
            case VIEW_KPI_MGMT -> "Đánh giá KPI & Thưởng";
            default -> "Trung tâm Điều hành Quản lý";
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