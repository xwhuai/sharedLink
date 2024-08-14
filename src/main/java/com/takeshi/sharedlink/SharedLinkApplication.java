package com.takeshi.sharedlink;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SharedLinkApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SharedLinkApplication.class.getResource("frontPage-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        // 设置全局的鼠标点击事件监听
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            // 如果点击的不是 TextField，取消焦点
            if (!(event.getTarget() instanceof TextInputControl)) {
                // 尝试将焦点设置到一个非 TextField 的节点上
                scene.getRoot().requestFocus();
            }
        });
        stage.setMinWidth(640);
        stage.setMinHeight(480);
        stage.setTitle("七濑武的115网盘分享链接批量转存");
        stage.setScene(scene);
        stage.show();
        stage.toFront();
    }

    public static void main(String[] args) {
        launch();
    }

}