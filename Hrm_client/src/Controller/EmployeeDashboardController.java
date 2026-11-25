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

public class EmployeeDashboardController {

    private static final String VIEW_OVERVIEW = "OVERVIEW";
    private static final String VIEW_ATTENDANCE = "/src/view/Employee/UserAttendance.fxml";
    private static final String VIEW_TASK = "/src/view/Employee/UserTask.fxml";
    private static final String VIEW_KPI = "/src/view/Employee/UserKPI.fxml";

    @FXML private BorderPane mainBorderPane;
    @FXML private VBox contentContainer;
    @FXML private VBox overviewView;

    @FXML private Label lblWelcome;
    @FXML private Label lblCurrentPage;

    @FXML private Button btnBack;
    @FXML private Button btnForward;

    @FXML private Button btnNavOverview;
    @FXML private Button btnNavAttendance;
    @FXML private Button btnNavTasks;
    @FXML private Button btnNavKPI;

    private static final String DEFAULT_BTN_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #e0f2fe; -fx-alignment: CENTER_LEFT; -fx-padding: 11 14; -fx-font-size: 13.5px; -fx-background-radius: 9; -fx-cursor: hand;";
    private static final String ACTIVE_BTN_STYLE =
            "-fx-background-color: linear-gradient(to right, #0d9488, #0284c7, #4f46e5); -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 11 14; -fx-font-size: 13.5px; -fx-background-radius: 9; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(13, 148, 136, 0.35), 12, 0, 0, 3);";

    private final Deque<String> backStack = new ArrayDeque<>();
    private final Deque<String> forwardStack = new ArrayDeque<>();
    private String currentView;

    @FXML
    public void initialize() {
        String name = SessionManager.getFullName() != null && !SessionManager.getFullName().isBlank()
                ? SessionManager.getFullName()
                : (SessionManager.getUsername() != null ? SessionManager.getUsername() : "Nhân viên");

        if (lblWelcome != null) {
            lblWelcome.setText(name);
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
    public void showTasks() {
        navigateTo(VIEW_TASK, resolveTitle(VIEW_TASK), true);
    }

    @FXML
    public void showKPI() {
        navigateTo(VIEW_KPI, resolveTitle(VIEW_KPI), true);
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
        switchScene(event, "/src/view/Login.fxml", "AI HRM System - Đăng nhập");
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
            case VIEW_TASK -> btnNavTasks;
            case VIEW_KPI -> btnNavKPI;
            default -> btnNavOverview;
        };
        for (Button btn : List.of(btnNavOverview, btnNavAttendance, btnNavTasks, btnNavKPI)) {
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
            case VIEW_ATTENDANCE -> "Lịch sử Chấm công";
            case VIEW_TASK -> "Công việc của tôi";
            case VIEW_KPI -> "KPI & Thưởng cá nhân";
            default -> "Không gian Nhân viên AI HRM";
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