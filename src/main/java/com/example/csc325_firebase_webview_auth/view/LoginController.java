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

import com.example.csc325_firebase_webview_auth.session.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private TextField passwordField;
    @FXML private Button loginButton;
    @FXML private Button signUpLink;

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    @FXML
    private void initialize() {
        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String pass  = passwordField.getText().trim();
            if (email.isEmpty() || pass.isEmpty()) {
                showAlert("Missing Credentials", "Please enter both email and password.");
                return;
            }
            try {
                String apiKey = loadConfig().getString("apiKey");
                URL url = new URL(
                  "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
                  + apiKey
                );
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type","application/json");
                conn.setDoOutput(true);

                JSONObject req = new JSONObject()
                    .put("email", email)
                    .put("password", pass)
                    .put("returnSecureToken", true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(req.toString().getBytes(StandardCharsets.UTF_8));
                }

                InputStream in = conn.getResponseCode()==200
                    ? conn.getInputStream()
                    : conn.getErrorStream();
                String resp = new BufferedReader(
                    new InputStreamReader(in,StandardCharsets.UTF_8))
                    .lines().reduce("", String::concat);
                JSONObject json = new JSONObject(resp);

                if (conn.getResponseCode() == 200) {
                    // save the idToken for later Firestore calls
                    String idToken = json.getString("idToken");
                    Session.setIdToken(idToken);

                    Parent main = FXMLLoader.load(
                      getClass().getResource("/files/WebContainer.fxml")
                    );
                    Scene scene = new Scene(main);
                    scene.getStylesheets().add(
                      getClass().getResource("/files/StyleSheet.css").toExternalForm()
                    );
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(scene);
                } else {
                    String err = json
                      .getJSONObject("error")
                      .getString("message");
                    showAlert("Login Failed", err);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Unexpected error during login.");
            }
        });

        signUpLink.setOnAction(e -> {
            try {
        Parent signUp = FXMLLoader.load(
            getClass().getResource("/files/SignUp.fxml")
        );
        Scene scene = new Scene(signUp);
        scene.getStylesheets().add(
            getClass().getResource("/files/StyleSheet.css").toExternalForm()
        );
        Stage stage = (Stage) signUpLink.getScene().getWindow();
        stage.setScene(scene);
    } catch (IOException ex) {
        ex.printStackTrace();
        showAlert("Navigation Error", "Could not load Sign Up screen.");
    }
});
    }

    private JSONObject loadConfig() throws Exception {
        try (InputStream in = getClass()
                 .getResourceAsStream("/files/FirebaseAPI.json")) {
            if (in == null) throw new IllegalStateException("Missing FirebaseAPI.json");
            String txt = new BufferedReader(
              new InputStreamReader(in,StandardCharsets.UTF_8))
              .lines().reduce("", String::concat);
            return new JSONObject(txt);
        }
    }
}
