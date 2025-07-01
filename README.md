# CSC325 Week 6 

## resources/files should contain:
- FirebaseAPI.json with variables "apiKey" and "projectId"
- serviceAccountKey.json from the firebase service accounts adminsdk


## update firebase rules to match the following: 
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}


