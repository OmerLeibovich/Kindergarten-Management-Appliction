package com.example.finalprojectapp.parents;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.finalprojectapp.LoginFragment;
import com.example.finalprojectapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import Objects.Parent;

/**
 * A fragment that handles user registration for parents.
 */
public class RegisterFragment extends Fragment {

    private EditText nameField; // Input field for the parent's name
    private EditText emailField; // Input field for the parent's email
    private EditText passwordField; // Input field for the parent's password
    private Button registerButton; // Button to submit the registration form
    private TextView alreadyHaveAccount; // TextView to navigate to the login screen

    private FirebaseFirestore db; // Firestore database instance
    private FirebaseAuth mAuth; // Firebase authentication instance

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        nameField = view.findViewById(R.id.nameField);
        emailField = view.findViewById(R.id.emailField);
        passwordField = view.findViewById(R.id.passwordField);
        registerButton = view.findViewById(R.id.registerButton);
        alreadyHaveAccount = view.findViewById(R.id.alreadyHaveAccount);

        // Set up the register button click listener
        registerButton.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            // Validate inputs before attempting to register
            if (validateInputs(name, email, password)) {
                registerUser(name, email, password);
            }
        });

        // Set up the click listener to load the login fragment
        alreadyHaveAccount.setOnClickListener(v -> {
            loadLoginFragment();
        });

        return view;
    }

    /**
     * Validates the user's input for name, email, and password.
     *
     * @param name     The parent's name.
     * @param email    The parent's email.
     * @param password The parent's password.
     * @return True if all inputs are valid, false otherwise.
     */
    private boolean validateInputs(String name, String email, String password) {
        if (TextUtils.isEmpty(name)) {
            nameField.setError("Name is required");
            nameField.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Valid email is required");
            emailField.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters");
            passwordField.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Registers the user with Firebase Authentication and saves the user data to Firestore.
     *
     * @param name     The parent's name.
     * @param email    The parent's email.
     * @param password The parent's password.
     */
    private void registerUser(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Parent parent = new Parent(email, password, name);
                        saveUserToFirestore(user, parent);
                        if (isAdded()) {
                            loadLoginFragment(); // Navigate to the login screen upon successful registration
                        }
                    } else {
                        Toast.makeText(getActivity(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Saves the registered user's information to Firestore.
     *
     * @param user   The FirebaseUser object representing the registered user.
     * @param parent The Parent object containing the user's information.
     */
    private void saveUserToFirestore(FirebaseUser user, Parent parent) {
        db.collection("Parents")
                .document(user.getUid())
                .set(parent)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(getActivity(), "Registration successful", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Failed to save user data", Toast.LENGTH_SHORT).show());
    }

    /**
     * Loads the login fragment after a successful registration or if the user already has an account.
     */
    private void loadLoginFragment() {
        LoginFragment loginFragment = new LoginFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, loginFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
