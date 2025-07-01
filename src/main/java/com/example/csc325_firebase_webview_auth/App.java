package com.example.csc325_firebase_webview_auth;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {
    private static final String CSS = "/files/StyleSheet.css";
    private static final double WIDTH = 1000;
    private static final double HEIGHT = 600;

    @Override
    public void start(Stage stage) throws Exception {
        Parent splash = FXMLLoader.load(getClass().getResource("/files/SplashScreen.fxml"));
        Scene splashScene = new Scene(splash, WIDTH, HEIGHT);
        splashScene.getStylesheets().add(getClass().getResource(CSS).toExternalForm());
        stage.setScene(splashScene);
        stage.setTitle("My Firebase App");
        stage.show();
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> loadLogin(stage));
        delay.play();
    }

    private void loadLogin(Stage stage) {
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/files/Login.fxml"));
            Scene loginScene = new Scene(login, WIDTH, HEIGHT);
            loginScene.getStylesheets().add(getClass().getResource(CSS).toExternalForm());
            stage.setScene(loginScene);
            stage.setWidth(WIDTH);
            stage.setHeight(HEIGHT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
