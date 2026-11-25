package ClientApplication;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.WebcamUtils;

import java.net.URL;

public class MainClient extends Application {


    private static final String[] LOGIN_FXML_CANDIDATES = {
            "/src/src/view/Login/Login.fxml",
            "/Login/Login.fxml",
            "/src/view/Login/Login.fxml"
    };

    private static final String APP_TITLE = "AI HRM System - Đăng nhập";

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlLocation = resolveLoginFxml();
        if (fxmlLocation == null) {
            throw new IllegalStateException(
                    "Không tìm thấy Login.fxml. Hãy kiểm tra thư mục src.view/Login/Login.fxml và cấu hình resources."
            );
        }

        Parent root = FXMLLoader.load(fxmlLocation);
        Scene scene = new Scene(root, 1280, 760);

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(680);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();

        primaryStage.setOnCloseRequest(event -> {
            WebcamUtils.stopCamera();
            Platform.exit();
        });
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        WebcamUtils.stopCamera();
        super.stop();
    }

    private URL resolveLoginFxml() {
        for (String path : LOGIN_FXML_CANDIDATES) {
            URL url = getClass().getResource(path);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}