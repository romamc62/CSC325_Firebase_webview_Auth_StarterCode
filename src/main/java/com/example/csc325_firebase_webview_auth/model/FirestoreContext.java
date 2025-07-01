package com.example.csc325_firebase_webview_auth.model;

import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

/**
 *
 * @author MoaathAlrajab
 */
public class FirestoreContext {

    public Firestore firebase() {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(getClass().getResourceAsStream("/files/serviceAccountKey.json")))
                    .build();
            FirebaseApp.initializeApp(options);
            System.out.println("Firebase is initialized");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return FirestoreClient.getFirestore();
    }


}
