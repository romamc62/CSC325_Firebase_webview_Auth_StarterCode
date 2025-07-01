package com.example.csc325_firebase_webview_auth.view;//package modelview;

import com.example.csc325_firebase_webview_auth.model.Person;
import com.example.csc325_firebase_webview_auth.viewmodel.AccessDataViewModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.auth.UserRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.firebase.cloud.StorageClient;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.util.Duration;

/**
 * Main Screen controller
 */
public class AccessFBViewController {

    /**
     * FXML declarations for fxml objects and names
     */
    @FXML
    private TextField firstNameField, lastNameField, emailField, departmentField, majorField, imageUrlField;
    @FXML
    private TableView<Person> personTableView;
    @FXML
    private TableColumn<Person, String> idColumn, firstNameColumn, lastNameColumn, emailColumn, majorColumn, departmentColumn;
    @FXML
    private MenuItem registerMenuItem, closeMenuItem, deleteMenuItem, helpMenuItem;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Button addButton, deleteButton, editButton, clearButton, backToLoginButton;

    //member variables
    private boolean key;
    private ObservableList<Person> listOfUsers = FXCollections.observableArrayList();
    private Person person;
    public ObservableList<Person> getListOfUsers() {
        return listOfUsers;
    }
    private String imageUrl;

    /**
     * Initializes controller and sets up table view
     * Sets bindings and configures table columns to display data
     */
    @FXML
    void initialize() {

        AccessDataViewModel accessDataViewModel = new AccessDataViewModel();
        personTableView.setItems(listOfUsers);

        firstNameField.textProperty().bindBidirectional(accessDataViewModel.userFirstNameProperty());
        lastNameField.textProperty().bindBidirectional(accessDataViewModel.userLastNameProperty());
        departmentField.textProperty().bindBidirectional(accessDataViewModel.userDepartment());
        emailField.textProperty().bindBidirectional(accessDataViewModel.userEmailProperty());
        majorField.textProperty().bindBidirectional(accessDataViewModel.userMajorProperty());
        imageUrlField.textProperty().bindBidirectional(accessDataViewModel.userImageUrlProperty());


        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        majorColumn.setCellValueFactory(new PropertyValueFactory<>("major"));


        personTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                firstNameField.setText(newValue.getFirstName());
                lastNameField.setText(newValue.getLastName());
                majorField.setText(newValue.getDepartment());
                departmentField.setText(newValue.getDepartment());
                emailField.setText(newValue.getEmail());
                imageUrlField.setText(newValue.getImageUrl());

                String url = newValue.getImageUrl();
                if(url != null) {
                    profileImageView.setImage(new Image(url));
                } else {
                    InputStream stream = getClass().getResourceAsStream("/files/profile_empty.png");
                    if(stream != null) {
                        profileImageView.setImage(new Image(stream));
                    } else {
                        System.out.println("Could not find image");
                        profileImageView.setImage(null);
                    }
                }
            }
        });
        readFirebase();
    }

    /**
     * Handles action event for adding a record
     * @param event action event that triggers function
     */
    @FXML
    private void addRecord(ActionEvent event) {
        addData();
    }

    /**
     * Handle action event for
     * @param event
     */
    @FXML
    private void readRecord(ActionEvent event) {
        readFirebase();
    }

    /**
     * Handles action event for clearing a record
     * @param event action event that triggers function
     */
    @FXML
    private void clearButtonClick(ActionEvent event) {
        clearForm();
    }

    /**
     * Function to add a person to the database
     */
    @FXML
    public void addData() {

        int nextId = getNextAvailableId();
        String id = String.valueOf(nextId);

        DocumentReference docRef = App.fstore.collection("References").document(id);

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("firstName", firstNameField.getText());
        data.put("lastName", lastNameField.getText());
        data.put("department", departmentField.getText());
        data.put("major", majorField.getText());
        data.put("email", emailField.getText());
        data.put("imageUrl", imageUrlField.getText());

        try {
            ApiFuture<WriteResult> result = docRef.set(data);
            result.get();
            readFirebase();
            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the next available id
     * @return next max id
     */
    private int getNextAvailableId() {
        int maxId = 0;
        try {
            ApiFuture<QuerySnapshot> future = App.fstore.collection("References").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            for (QueryDocumentSnapshot doc : documents) {
                String idStr = doc.getString("id");
                if (idStr != null && idStr.matches("\\d+")) {
                    int id = Integer.parseInt(idStr);
                    if (id > maxId) {
                        maxId = id;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maxId + 1;
    }

    public boolean readFirebase() {
        key = false;

        ApiFuture<QuerySnapshot> future =  App.fstore.collection("References").get();

        List<QueryDocumentSnapshot> documents;
        try {
            documents = future.get().getDocuments();
            listOfUsers.clear();
            if(documents.size() > 0) {
                System.out.println("Outing....");
                for (QueryDocumentSnapshot document : documents) {

                    String id = document.getString("id");
                    String firstName = document.getString("firstName");
                    String lastName = document.getString("lastName");
                    String department = document.getString("department");
                    String major = document.getString("major");
                    String email = document.getString("email");
                    String imageUrl = document.getString("imageUrl");

                    System.out.println(document.getId() + " => " + document.getData().get("id"));
                    Person person  = new Person(id, firstName, lastName, department, major, email, imageUrl);
                    listOfUsers.add(person);
                    Collections.sort(listOfUsers, Comparator.comparing(Person::getId));

                }
                personTableView.setItems(listOfUsers);
            } else {
               System.out.println("No data");
            }
            key=true;
        }
        catch (InterruptedException | ExecutionException ex) {
             ex.printStackTrace();
        }
        return key;
    }

        public void sendVerificationEmail() {
        try {
            UserRecord user = App.fauth.getUser("name");
            //String url = user.getPassword();

        }
        catch (Exception e) {
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            App.setRoot("/files/SignUp.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void handleHelp(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Firebase JavaFX Application");
        alert.setContentText("Author: Daniel Stevens\nVersion 1.0\nCSC325 Firestore Project");
        alert.showAndWait();
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Person selected = personTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        String idToDelete = selected.getId();

        ApiFuture<QuerySnapshot> future = App.fstore.collection("References")
                .whereEqualTo("id", idToDelete)
                .get();
        try {
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                App.fstore.collection("References").document(doc.getId()).delete();
            }
            readFirebase();
            clearForm();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * handles image upload event
     * @param event action event that triggers image upload
     */
    @FXML
    private void handleImageUpload(MouseEvent event) {
        FileChooser filechooser = new FileChooser();
        filechooser.setTitle("Select Image");
        filechooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", ".jpeg"));

        File selectedFile = filechooser.showOpenDialog(profileImageView.getScene().getWindow());

        if (selectedFile != null) {
            try {
                String uploadedUrl = uploadImage(selectedFile);
                if(uploadedUrl != null) {
                    imageUrlField.setText(uploadedUrl);
                    profileImageView.setImage(new Image(uploadedUrl));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * uploads an image to the database
     * @param file file to upload
     * @return the link to the firebase object
     */
    private String uploadImage(File file){
        try {
            String fileName = UUID.randomUUID().toString() + "=" + file.getName();
            Bucket bucket = StorageClient.getInstance().bucket("csc325-c8b73.firebasestorage.app");
            Blob blob = bucket.create("profile_images/" + fileName, new FileInputStream(file), Bucket.BlobWriteOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));
            return "https://storage.googleapis.com/" + bucket.getName() + "/profile_images/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * clears the text fields
     */
    private void clearForm(){
        firstNameField.clear();
        lastNameField.clear();
        departmentField.clear();
        majorField.clear();
        emailField.clear();
        imageUrlField.clear();
        profileImageView.setImage(new Image(getClass().getResourceAsStream("/files/profile_empty.png")));
    }

    /**
     * Handles the edit button function
     * @param event action event that triggers edit
     */
    @FXML
    private void handleEdit(ActionEvent event) {
        Person selectedPerson = personTableView.getSelectionModel().getSelectedItem();

        if (selectedPerson != null) {
            selectedPerson.setFirstName(firstNameField.getText());
            selectedPerson.setLastName(lastNameField.getText());
            selectedPerson.setDepartment(departmentField.getText());
            selectedPerson.setMajor(majorField.getText());
            selectedPerson.setEmail(emailField.getText());
            selectedPerson.setImageUrl(imageUrlField.getText());

            updatePersonInFirebase(selectedPerson);
            readFirebase();
            PauseTransition delay = new PauseTransition(Duration.millis(200));
            delay.setOnFinished(e -> clearForm());
            delay.play();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No selection");
            alert.setContentText("Please select a person to edit");
            alert.showAndWait();
        }
    }

    /**
     * Updates a record in the database
     * @param person person object to update
     */
    private void updatePersonInFirebase(Person person) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("firstName", person.getFirstName());
            updates.put("lastName", person.getLastName());
            updates.put("department", person.getDepartment());
            updates.put("major", person.getMajor());
            updates.put("email", person.getEmail());
            updates.put("imageUrl", person.getImageUrl());

            App.fstore.collection("References")
                    .document(person.getId())
                    .update(updates);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}