package util;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebcamUtils {

    private static Webcam webcam;
    private static final AtomicBoolean isRunning = new AtomicBoolean(false);

    // 1. Mở Webcam mặc định và hiển thị trực tiếp lên ImageView
    public static synchronized void startCamera(ImageView imageView) {
        if (isRunning.get()) {
            return;
        }

        webcam = Webcam.getDefault();
        if (webcam == null) {
            throw new RuntimeException("Không tìm thấy thiết bị Webcam trên máy tính!");
        }

        Dimension size = WebcamResolution.VGA.getSize(); // 640x480
        if (!webcam.isOpen()) {
            webcam.setViewSize(size);
            webcam.open();
        }

        isRunning.set(true);

        Thread cameraThread = new Thread(() -> {
            while (isRunning.get() && webcam != null && webcam.isOpen()) {
                try {
                    BufferedImage bImage = webcam.getImage();
                    if (bImage != null && imageView != null) {
                        Image fxImage = SwingFXUtils.toFXImage(bImage, null);
                        Platform.runLater(() -> imageView.setImage(fxImage));
                    }
                    Thread.sleep(33); // ~30 FPS
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Lỗi luồng đọc hình ảnh Webcam: " + e.getMessage());
                }
            }
        }, "Webcam-Stream-Thread");

        cameraThread.setDaemon(true);
        cameraThread.start();
    }

    // 2. Chụp 1 khung hình hiện tại từ Webcam và mã hóa sang Base64 (JPEG)
    public static String captureBase64() throws Exception {
        if (webcam == null || !webcam.isOpen()) {
            throw new IllegalStateException("Webcam chưa sẵn sàng! Vui lòng bật camera trước khi chụp.");
        }

        BufferedImage image = webcam.getImage();
        if (image == null) {
            throw new RuntimeException("Không thu được khung hình từ Webcam, vui lòng thử lại!");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    // 3. Tắt Webcam và giải phóng phần cứng
    public static synchronized void stopCamera() {
        isRunning.set(false);
        if (webcam != null) {
            try {
                if (webcam.isOpen()) {
                    webcam.close();
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi đóng Webcam: " + e.getMessage());
            } finally {
                webcam = null;
            }
        }
    }

    // 4. Kiểm tra trạng thái camera
    public static boolean isCameraRunning() {
        return isRunning.get() && webcam != null && webcam.isOpen();
    }
}