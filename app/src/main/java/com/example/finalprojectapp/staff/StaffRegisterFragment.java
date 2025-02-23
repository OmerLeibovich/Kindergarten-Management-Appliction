package com.example.finalprojectapp.staff;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.example.finalprojectapp.database.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import Enums.StaffRole;
import Objects.GardenDirector;
import Objects.GardenStaff;
import Objects.SystemAdministrator;  // Add this import

/**
 * StaffRegisterFragment is responsible for handling the registration of staff members in the application.
 * The fragment allows users to register as a teacher, assistant, manager, or system administrator.
 * It uses Firebase Authentication to create a new user and Firebase Firestore to store user data.
 */
public class StaffRegisterFragment extends Fragment {

    // UI components
    private EditText emailField, passwordField, nameField;
    private CheckBox roleTeacher, roleAssistant, roleManager, roleSystemAdmin;
    private TextView dateTextView, loginTextView;
    private Button dateButton, registerButton;

    // Firebase authentication and database
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * Default constructor required for fragment subclasses.
     */
    public StaffRegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_staff_register, container, false);

        // Initialize UI components
        emailField = view.findViewById(R.id.emailField);
        passwordField = view.findViewById(R.id.passwordField);
        nameField = view.findViewById(R.id.nameField);
        roleTeacher = view.findViewById(R.id.roleTeacher);
        roleAssistant = view.findViewById(R.id.roleAssistant);
        roleManager = view.findViewById(R.id.roleManager);
        roleSystemAdmin = view.findViewById(R.id.roleSystemAdmin);
        dateTextView = view.findViewById(R.id.dateTextView);
        dateButton = view.findViewById(R.id.dateButton);
        registerButton = view.findViewById(R.id.registerButton);
        loginTextView = view.findViewById(R.id.loginTextView);

        // Initialize Firebase authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up date picker dialog
        dateButton.setOnClickListener(v -> showDatePickerDialog());

        // Set up role selection with mutual exclusivity
        roleTeacher.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                roleAssistant.setChecked(false);
                roleManager.setChecked(false);
                roleSystemAdmin.setChecked(false);
            }
        });

        roleAssistant.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                roleTeacher.setChecked(false);
                roleManager.setChecked(false);
                roleSystemAdmin.setChecked(false);
            }
        });

        roleManager.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                roleTeacher.setChecked(false);
                roleAssistant.setChecked(false);
                roleSystemAdmin.setChecked(false);
            }
        });

        roleSystemAdmin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                roleTeacher.setChecked(false);
                roleAssistant.setChecked(false);
                roleManager.setChecked(false);
            }
        });

        // Handle registration button click
        registerButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String name = nameField.getText().toString().trim();
            String date = dateTextView.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name) || TextUtils.isEmpty(date)) {
                Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!roleTeacher.isChecked() && !roleAssistant.isChecked() && !roleManager.isChecked() && !roleSystemAdmin.isChecked()) {
                Toast.makeText(getActivity(), "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(email, password, name, date);
        });

        // Navigate to login screen when the login text is clicked
        loginTextView.setOnClickListener(v -> loadLoginFragment());

        return view;
    }

    /**
     * Registers a new user using Firebase Authentication.
     *
     * @param email    The user's email.
     * @param password The user's password.
     * @param name     The user's name.
     * @param date     The user's start date.
     */
    private void registerUser(String email, String password, String name, String date) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            try {
                                saveUserToFirestore(user, name, email, password, date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Saves the user data to Firebase Firestore based on their selected role.
     *
     * @param user     The FirebaseUser object.
     * @param name     The user's name.
     * @param email    The user's email.
     * @param password The user's password.
     * @param date     The user's start date.
     * @throws ParseException if the date format is incorrect.
     */
    private void saveUserToFirestore(FirebaseUser user, String name, String email, String password, String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date startDate = sdf.parse(date);

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());

        if (roleTeacher.isChecked() || roleAssistant.isChecked()) {
            StaffRole role = roleTeacher.isChecked() ? StaffRole.KINDERGARTEN_TEACHER : StaffRole.ASSISTANT;
            GardenStaff staff = new GardenStaff(email, password, name, role.toString(), startDate);
            db.collection("staff")
                    .document(user.getUid())
                    .set(staff)
                    .addOnSuccessListener(aVoid -> {
                        databaseHelper.addUser(user.getUid(), name, email, password);
                        Toast.makeText(getActivity(), "Registration successful", Toast.LENGTH_SHORT).show();
                        if (isAdded()) {
                            loadLoginFragment();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to save user data", Toast.LENGTH_SHORT).show());
        } else if (roleManager.isChecked()) {
            GardenDirector director = new GardenDirector(email, password, name, StaffRole.KINDER_GARTEN_DIRECTOR.toString(), startDate);
            db.collection("directors")
                    .document(user.getUid())
                    .set(director)
                    .addOnSuccessListener(aVoid -> {
                        databaseHelper.addUser(user.getUid(), name, email, password);
                        Toast.makeText(getActivity(), "Registration successful", Toast.LENGTH_SHORT).show();
                        if (isAdded()) {
                            loadLoginFragment();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to save user data", Toast.LENGTH_SHORT).show());
        } else if (roleSystemAdmin.isChecked()) {
            SystemAdministrator admin = new SystemAdministrator(email, password, name, StaffRole.SYSTEM_ADMINISTRATOR.toString());
            db.collection("systemAdministrators")
                    .document(user.getUid())
                    .set(admin)
                    .addOnSuccessListener(aVoid -> {
                        databaseHelper.addUser(user.getUid(), name, email, password);
                        Toast.makeText(getActivity(), "Registration successful", Toast.LENGTH_SHORT).show();
                        if (isAdded()) {
                            loadLoginFragment();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to save user data", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Displays a DatePickerDialog to allow the user to select a date.
     */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view, year1, month1, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            dateTextView.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    /**
     * Navigates the user to the login fragment.
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
