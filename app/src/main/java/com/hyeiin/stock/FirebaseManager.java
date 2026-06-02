package com.hyeiin.stock;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;

/** Firebase Auth + Firestore ?깃????묎렐??*/
public class FirebaseManager {
    private FirebaseManager() {}

    public static FirebaseAuth      auth() { return FirebaseAuth.getInstance(); }
    public static FirebaseFirestore db()   { return FirebaseFirestore.getInstance(); }
    public static FirebaseFunctions functions() { return FirebaseFunctions.getInstance("us-central1"); }
    public static FirebaseUser      currentUser() { return auth().getCurrentUser(); }
    public static boolean           isLoggedIn()  { return currentUser() != null; }
    public static String            uid() {
        FirebaseUser u = currentUser();
        return u != null ? u.getUid() : "";
    }
}
