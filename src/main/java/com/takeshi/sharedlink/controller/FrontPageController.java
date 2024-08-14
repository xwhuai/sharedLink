package com.takeshi.sharedlink.controller;

import com.takeshi.sharedlink.SharedLinkApplication;
import com.takeshi.sharedlink.util.SecurityUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.prefs.Preferences;

public class FrontPageController {

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Pane mask;

    @FXML
    private TextField cookieField;

    @FXML
    private TextField cidField;

    private Preferences preferences;

    private final String COOKIE = "cookie";

    private final String CID = "cid";

    @FXML
    public void initialize() {
        preferences = Preferences.userNodeForPackage(FrontPageController.class);
        // 加载保存的cookie和cid到TextField
        String decryptCookie = "", decryptCid = "";
        try {
            decryptCookie = SecurityUtils.decrypt(preferences.get(COOKIE, ""));
            decryptCid = SecurityUtils.decrypt(preferences.get(CID, ""));
        } catch (Exception ignored) {

        }
        cookieField.setText(decryptCookie);
        cidField.setText(decryptCid);
    }

    @FXML
    private synchronized void onNextButtonClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        Platform.runLater(() -> {
            clickedButton.setDisable(true);
            loadingIndicator.setVisible(true);
            mask.setVisible(true);
        });
        new Thread(() -> {
            try {
                String cookie = this.cookieField.getText();
                String cid = this.cidField.getText();
                if (Objects.isNull(cookie) || cookie.isBlank()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("输入错误");
                        alert.setContentText("请输入115网盘账户Cookie");
                        alert.showAndWait();
                    });
                    return;
                }
                // 保存cookie和cid到Preferences
                preferences.put(COOKIE, SecurityUtils.encrypt(cookie));
                preferences.put(CID, SecurityUtils.encrypt(cid));
                FXMLLoader loader = new FXMLLoader(SharedLinkApplication.class.getResource("sharedLink-view.fxml"));
                Scene sharedLinkScene = new Scene(loader.load());
                try {
                    SharedLinkController sharedLinkController = loader.getController();
                    sharedLinkController.setUserData(cookie, cid);
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("输入错误");
                        alert.setContentText("请输入有效的115网盘用户Cookie\n" + e.getMessage());
                        alert.showAndWait();
                    });
                    return;
                }
                Platform.runLater(() -> {
                    Stage stage = (Stage) cookieField.getScene().getWindow();
                    stage.setScene(sharedLinkScene);
                    stage.show();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("错误");
                    alert.setContentText("系统错误: " + e.getMessage());
                    alert.showAndWait();
                });
            } finally {
                Platform.runLater(() -> {
                    clickedButton.setDisable(false);
                    loadingIndicator.setVisible(false);
                    mask.setVisible(false);
                });
            }
        }).start();
    }

}