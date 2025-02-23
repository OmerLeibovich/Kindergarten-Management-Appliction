package com.example.finalprojectapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalprojectapp.parents.ParentActivity;
import com.example.finalprojectapp.staff.StaffActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * LoginFragment is a fragment that handles user authentication.
 * It provides an interface for users to enter their email and password, and logs them in.
 * After successful authentication, it navigates the user to the appropriate activity based on their role.
 */
public class LoginFragment extends Fragment {

    // UI elements
    private EditText emailField, passwordField;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private UserSessionManager session;

    private static final String TAG = "LoginFragment";

    public LoginFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailField = view.findViewById(R.id.emailField);
        passwordField = view.findViewById(R.id.passwordField);
        loginButton = view.findViewById(R.id.loginButton);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        session = new UserSessionManager(getContext());

        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(getActivity(), "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        return view;
    }

    /**
     * Logs the user in using Firebase authentication.
     * If the login is successful, it checks which collection the user belongs to and navigates them accordingly.
     *
     * @param email    The user's email address.
     * @param password The user's password.
     */
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkUserCollection(user, email);
                    } else {
                        // If sign in fails, display a message to the user
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null, null);
                    }
                });
    }

    /**
     * Displays a snackbar or toast message to the user.
     *
     * @param view    The view to display the snackbar in.
     * @param message The message to display.
     */
    private void showSnackbar(View view, String message) {
        if (view != null && view.getParent() != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks which collection the user belongs to (Parents, Directors, Staff, etc.) and navigates accordingly.
     *
     * @param user  The authenticated FirebaseUser.
     * @param email The user's email address.
     */
    private void checkUserCollection(FirebaseUser user, String email) {
        if (user != null) {
            checkCollection(user, email, "Parents");
        }
    }

    /**
     * Checks the specified collection to see if the user belongs to it.
     * If found, it sets the session and navigates to the appropriate activity.
     *
     * @param user       The authenticated FirebaseUser.
     * @param email      The user's email address.
     * @param collection The collection to check (Parents, Directors, Staff, etc.).
     */
    private void checkCollection(FirebaseUser user, String email, String collection) {
        db.collection(collection)
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String name = document.getString("name");
                        String role = document.getString("role");
                        Log.d(TAG, "User found in collection: " + collection + ", role: " + role);
                        if ("Parents".equals(collection)) {
                            session.createLoginSession(email, "Parent");
                            updateUI(user, name);
                        } else if ("directors".equals(collection)) {
                            session.createLoginSession(email, "KINDER_GARTEN_DIRECTOR");
                            navigateToStaffActivity(user, name, "KINDER_GARTEN_DIRECTOR");
                        } else if ("staff".equals(collection)) {
                            if ("KINDERGARTEN_TEACHER".equals(role) || "ASSISTANT".equals(role)) {
                                session.createLoginSession(email, "KINDER_GARTEN_STAFF");
                                navigateToStaffActivity(user, name, "KINDER_GARTEN_STAFF");
                            }
                        } else if ("systemAdministrators".equals(collection)) {
                            session.createLoginSession(email, "SYSTEM_ADMINISTRATOR");
                            navigateToStaffActivity(user, name, "SYSTEM_ADMINISTRATOR");
                        }
                    } else {
                        Log.d(TAG, "User not found in collection: " + collection);
                        if ("Parents".equals(collection)) {
                            checkCollection(user, email, "directors");
                        } else if ("directors".equals(collection)) {
                            checkCollection(user, email, "staff");
                        } else if ("staff".equals(collection)) {
                            checkCollection(user, email, "systemAdministrators");
                        } else if ("systemAdministrators".equals(collection)) {
                            checkCollection(user, email, "Parents");
                            Toast.makeText(getActivity(), "User details not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "get failed with ", e);
                    Toast.makeText(getActivity(), "Failed to fetch user details", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the UI based on the user's authentication status.
     * If the user is authenticated, navigates to the ParentActivity.
     *
     * @param user The authenticated FirebaseUser.
     * @param name The user's name.
     */
    private void updateUI(FirebaseUser user, String name) {
        if (user != null) {
            // User is signed in, navigate to ParentActivity
            Intent intent = new Intent(getActivity(), ParentActivity.class);
            intent.putExtra("userEmail", user.getEmail());
            intent.putExtra("userName", name);
            startActivity(intent);
            getActivity().finish();
        } else {
            // User is not signed in
            Toast.makeText(getActivity(), "Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigates to the StaffActivity based on the user's role.
     *
     * @param user  The authenticated FirebaseUser.
     * @param name  The user's name.
     * @param role  The user's role.
     */
    private void navigateToStaffActivity(FirebaseUser user, String name, String role) {
        if (user != null) {
            // User is signed in, navigate to StaffActivity
            Intent intent = new Intent(getActivity(), StaffActivity.class);
            intent.putExtra("userEmail", user.getEmail());
            intent.putExtra("userName", name);
            intent.putExtra("userRole", role);
            startActivity(intent);
            getActivity().finish();
        } else {
            // User is not signed in
            Toast.makeText(getActivity(), "Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

}
