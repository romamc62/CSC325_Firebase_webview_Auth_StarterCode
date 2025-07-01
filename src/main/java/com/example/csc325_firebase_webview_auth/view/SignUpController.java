package com.example.csc325_firebase_webview_auth.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SignUpController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;

    @FXML private Button backToLoginButton;
    @FXML private Button backToLoginLink;


    @FXML
    private void initialize() {
        String API_KEY = loadApiKey();
        registerButton.setOnAction(e -> {
            String email   = emailField.getText();
            String pass    = passwordField.getText();
            String confirm = confirmPasswordField.getText();

            if (email.isBlank() || pass.isBlank() || confirm.isBlank()) {
                showAlert("Missing Fields", "Please fill out all fields.");
                return;
            }
            if (!pass.equals(confirm)) {
                showAlert("Password Mismatch", "Passwords do not match.");
                return;
            }

            try {
                URL url = new URL(
                  "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" 
                  + API_KEY
                );
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonInput = String.format(
                  "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                  email, pass
                );
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                InputStream in = 
                  conn.getResponseCode() == 200 
                    ? conn.getInputStream() 
                    : conn.getErrorStream();
                String response = new BufferedReader(new InputStreamReader(in))
                    .lines().reduce("", String::concat);
                JSONObject json = new JSONObject(response);

                if (conn.getResponseCode() == 200) {
                    showAlert("Success", "Please login.");
                    goToLogin();
                } else {
                    String errorMsg = json
                      .getJSONObject("error")
                      .getString("message");
                    showAlert("Sign-Up Failed", errorMsg);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "An unexpected error occurred.");
            }
        });

        // wire up the "back to login" button, whichever fx:id was used
        Button backBtn = (backToLoginButton != null) 
                         ? backToLoginButton 
                         : backToLoginLink;
        if (backBtn != null) {
            backBtn.setOnAction(e -> goToLogin());
        }
    }

    private void goToLogin() {
        try {
            Parent login = FXMLLoader.load(
              getClass().getResource("/files/Login.fxml")
            );
            Scene scene = new Scene(login);
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(scene);
            scene.getStylesheets()
     .add(getClass().getResource("/files/StyleSheet.css").toExternalForm());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String loadApiKey() {
        try (InputStream in = getClass().getResourceAsStream("/files/FirebaseAPI.json")) {
            if (in == null) throw new RuntimeException("Cannot find FirebaseAPI.json");
            String json = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                            .lines().reduce("", (a,b)->a+b);
            return new JSONObject(json).getString("apiKey");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load API key", e);
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
