package com.example.csc325_firebase_webview_auth.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.csc325_firebase_webview_auth.model.Person;
import com.example.csc325_firebase_webview_auth.session.Session;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class WebContainerController {

    private String projectId, apiKey;
    private static final String COLLECTION = "people";

    @FXML private TableView<Person> tableView;
    @FXML private TableColumn<Person,String> colId, colFirstName, colLastName, colDepartment, colMajor, colEmail;
    @FXML private ImageView profileImage;
    @FXML private TextField firstNameField, lastNameField, departmentField, majorField, emailField, imageUrlField;
    @FXML private Button clearButton, addButton, deleteButton, editButton;

    private ObservableList<Person> data;
    // **Declare** the default image reference
    private Image defaultProfileImage;

    @FXML
    private void initialize() {
        JSONObject cfg = loadConfig();
        apiKey    = cfg.getString("apiKey");
        projectId = cfg.getString("projectId");

        // **Capture** whatever image is in the FXML as the default
        defaultProfileImage = profileImage.getImage();

        data = FXCollections.observableArrayList();
        tableView.setItems(data);

        colId         .setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName  .setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName   .setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colDepartment .setCellValueFactory(new PropertyValueFactory<>("department"));
        colMajor      .setCellValueFactory(new PropertyValueFactory<>("major"));
        colEmail      .setCellValueFactory(new PropertyValueFactory<>("email"));

        clearButton .setOnAction(e -> clearFields());
        addButton   .setOnAction(e -> createRecord());
        deleteButton.setOnAction(e -> deleteRecord());
        editButton  .setOnAction(e -> updateRecord());

        tableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldP, newP) -> { if (newP != null) loadIntoFields(newP); }
        );

        loadRecords();
    }

    private String baseUrl() {
        return "https://firestore.googleapis.com/v1/projects/"
            + projectId + "/databases/(default)/documents/" + COLLECTION;
    }

    private void loadRecords() {
        data.clear();
        try {
            HttpURLConnection c = openConnection(baseUrl() + "?key=" + apiKey, "GET");
            int code = c.getResponseCode();
            String body = readStream(c, code);
            if (code == 200) {
                JSONObject root = new JSONObject(body);
                if (root.has("documents")) {
                    JSONArray docs = root.getJSONArray("documents");
                    for (Object o : docs) {
                        JSONObject doc = (JSONObject) o;
                        String fullName = doc.getString("name");
                        String id = fullName.substring(fullName.lastIndexOf('/') + 1);
                        JSONObject f = doc.getJSONObject("fields");
                        data.add(new Person(
                            id,
                            f.optJSONObject("firstName").optString("stringValue",""),
                            f.optJSONObject("lastName" ).optString("stringValue",""),
                            f.optJSONObject("department").optString("stringValue",""),
                            f.optJSONObject("major"     ).optString("stringValue",""),
                            f.optJSONObject("email"     ).optString("stringValue",""),
                            f.optJSONObject("imageUrl"  ).optString("stringValue","")
                        ));
                    }
                }
            } else {
                System.err.printf("LOAD %d → %s%n", code, body);
                showAlert("Load Error","Could not load data.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Load Error","Could not load data.");
        }
    }

    private void createRecord() {
        try {
            JSONObject flds = new JSONObject()
                .put("firstName", new JSONObject().put("stringValue", firstNameField.getText()))
                .put("lastName" , new JSONObject().put("stringValue", lastNameField.getText()))
                .put("department",new JSONObject().put("stringValue", departmentField.getText()))
                .put("major"    ,new JSONObject().put("stringValue", majorField.getText()))
                .put("email"    ,new JSONObject().put("stringValue", emailField.getText()))
                .put("imageUrl" ,new JSONObject().put("stringValue", imageUrlField.getText()));
            JSONObject bodyJson = new JSONObject().put("fields", flds);

            HttpURLConnection c = openConnection(baseUrl() + "?key=" + apiKey, "POST");
            writeBody(c, bodyJson.toString());

            int code = c.getResponseCode();
            String body = readStream(c, code);
            if (code == 200) {
                loadRecords();
                tableView.getSelectionModel().selectLast();
                clearFields();
            } else {
                System.err.printf("CREATE %d → %s%n", code, body);
                showAlert("Create Error","Could not create record.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Create Error","Could not create record.");
        }
    }

    private void updateRecord() {
        Person sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            JSONObject flds = new JSONObject()
                .put("firstName", new JSONObject().put("stringValue", firstNameField.getText()))
                .put("lastName" , new JSONObject().put("stringValue", lastNameField.getText()))
                .put("department",new JSONObject().put("stringValue", departmentField.getText()))
                .put("major"    ,new JSONObject().put("stringValue", majorField.getText()))
                .put("email"    ,new JSONObject().put("stringValue", emailField.getText()))
                .put("imageUrl" ,new JSONObject().put("stringValue", imageUrlField.getText()));
            JSONObject bodyJson = new JSONObject().put("fields", flds);

            String urlStr = baseUrl()
                + "/" + sel.getId()
                + "?key=" + apiKey
                + "&updateMask.fieldPaths=firstName"
                + "&updateMask.fieldPaths=lastName"
                + "&updateMask.fieldPaths=department"
                + "&updateMask.fieldPaths=major"
                + "&updateMask.fieldPaths=email"
                + "&updateMask.fieldPaths=imageUrl";

            HttpURLConnection c = openConnection(urlStr, "POST");
            c.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            writeBody(c, bodyJson.toString());

            int code = c.getResponseCode();
            String body = readStream(c, code);
            if (code == 200) {
                loadRecords();
                tableView.getSelectionModel().select(sel);
                clearFields();
            } else {
                System.err.printf("UPDATE %d → %s%n", code, body);
                showAlert("Update Error","Could not update record.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Update Error","Could not update record.");
        }
    }

    private void deleteRecord() {
        Person sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            String urlStr = baseUrl() + "/" + sel.getId() + "?key=" + apiKey;
            HttpURLConnection c = openConnection(urlStr, "DELETE");

            int code = c.getResponseCode();
            String body = readStream(c, code);
            if (code == 200) {
                loadRecords();
                clearFields();
            } else {
                System.err.printf("DELETE %d → %s%n", code, body);
                showAlert("Delete Error","Could not delete record.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Delete Error","Could not delete record.");
        }
    }

    // --- Helpers ---

    private HttpURLConnection openConnection(String urlStr, String method) throws IOException {
        HttpURLConnection c = (HttpURLConnection)new URL(urlStr).openConnection();
        c.setRequestMethod(method);
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestProperty("Authorization", "Bearer " + Session.getIdToken());
        if (!"GET".equals(method) && !"DELETE".equals(method)) c.setDoOutput(true);
        return c;
    }

    private void writeBody(HttpURLConnection c, String body) throws IOException {
        try (OutputStream os = c.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readStream(HttpURLConnection c, int code) throws IOException {
        InputStream in = code == 200 ? c.getInputStream() : c.getErrorStream();
        return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
            .lines().reduce("", String::concat);
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        departmentField.clear();
        majorField.clear();
        emailField.clear();
        imageUrlField.clear();
        // reset image to the default captured at initialize()
        profileImage.setImage(defaultProfileImage);
        tableView.getSelectionModel().clearSelection();
    }

    private void loadIntoFields(Person p) {
        firstNameField.setText(p.getFirstName());
        lastNameField .setText(p.getLastName());
        departmentField.setText(p.getDepartment());
        majorField.setText(p.getMajor());
        emailField.setText(p.getEmail());
        imageUrlField.setText(p.getImageUrl());
        if (!p.getImageUrl().isEmpty()) {
            profileImage.setImage(new Image(p.getImageUrl(), true));
        } else {
            profileImage.setImage(null);
        }
    }

    private JSONObject loadConfig() {
        try (InputStream in = getClass().getResourceAsStream("/files/FirebaseAPI.json")) {
            if (in == null) throw new IllegalStateException("Missing config");
            String txt = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                         .lines().reduce("", String::concat);
            return new JSONObject(txt);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
