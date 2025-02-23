package com.example.finalprojectapp.parents;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;
import android.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import Objects.Child;
import Objects.GardenClass;

/**
 * A fragment that displays details about a specific garden and allows parents to register their children.
 */
public class GardenDetailsFragment extends Fragment {

    private TextView gartenNameTextView, gartenAddressTextView, gartenCityTextView;
    private TextView gartenPhoneNumberTextView, gartenOpenTimeTextView, gartenCloseTimeTextView, gartenOrgAffiliationTextView;
    private Button selectClassesButton, registerButton; // Button to select classes and register a child
    private EditText childIdEditText, childNameEditText, childAgeEditText, childHobbyEditText; // EditTexts for child details
    private ImageView gartenView; // ImageView to display garden image
    private List<String> selectedClasses = new ArrayList<>(); // List to store selected classes

    private FirebaseFirestore db; // Firebase Firestore instance
    private List<GardenClass> availableClasses;  // List of available classes in the garden

    /**
     * Creates a new instance of GardenDetailsFragment.
     *
     * @param gartenName The name of the garden.
     * @param userType The type of user viewing the details.
     * @return A new instance of GardenDetailsFragment.
     */
    public static GardenDetailsFragment newInstance(String gartenName, String userType) {
        GardenDetailsFragment fragment = new GardenDetailsFragment();
        Bundle args = new Bundle();
        args.putString("gartenName", gartenName);
        args.putString("userType", userType);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_garden_details, container, false);

        // Initialize UI components
        gartenNameTextView = view.findViewById(R.id.gartenNameTextView);
        gartenAddressTextView = view.findViewById(R.id.gartenAddressTextView);
        gartenCityTextView = view.findViewById(R.id.gartenCityTextView);
        gartenView = view.findViewById(R.id.gartenImageView);

        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        // Find additional TextViews
        gartenPhoneNumberTextView = view.findViewById(R.id.gartenPhoneNumberTextView);
        gartenOpenTimeTextView = view.findViewById(R.id.gartenOpenTimeTextView);
        gartenCloseTimeTextView = view.findViewById(R.id.gartenCloseTimeTextView);
        gartenOrgAffiliationTextView = view.findViewById(R.id.gartenOrgAffiliationTextView);

        // Initialize buttons and EditTexts for child details
        selectClassesButton = view.findViewById(R.id.selectClassesButton);
        registerButton = view.findViewById(R.id.registerButton);
        childIdEditText = view.findViewById(R.id.childId);
        childNameEditText = view.findViewById(R.id.childFullName);
        childAgeEditText = view.findViewById(R.id.childAge);
        childHobbyEditText = view.findViewById(R.id.childHobby);

        // Retrieve child information from the Bundle
        Bundle args = getArguments();
        if (args != null) {
            String childName = args.getString("childName");
            String childAge = args.getString("childAge");
            String childID = args.getString("childID");
            String childHobby = args.getString("childHobby");

            // Display child details in the corresponding fields
            childNameEditText.setText(childName);
            childAgeEditText.setText(childAge);
            childIdEditText.setText(childID);
            childHobbyEditText.setText(childHobby);
        }

        // Retrieve the name of the garden from the Bundle
        String gartenName = getArguments() != null ? getArguments().getString("gartenName") : null;

        if (gartenName != null) {
            // Load garden details based on the name
            loadGartenDetailsByName(gartenName);
        } else {
            Log.e("GardenDetailsFragment", "Garten name is null. Unable to load garden details.");
        }

        // Set click listener for selecting classes
        selectClassesButton.setOnClickListener(v -> {
            ClassesDialogFragment dialog = ClassesDialogFragment.newInstance(availableClasses, selectedClasses);
            dialog.setTargetFragment(GardenDetailsFragment.this, 1);
            dialog.show(getParentFragmentManager(), "ClassesDialog");
        });

        // Set click listener for registering a child
        registerButton.setOnClickListener(v -> {
            String childId = childIdEditText.getText().toString();
            String fullName = childNameEditText.getText().toString();
            int age = Integer.parseInt(childAgeEditText.getText().toString());
            String hobby = childHobbyEditText.getText().toString();

            // Check if the user is logged in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userId = user.getUid();

                // Create the Child object
                Child child = new Child(childId, fullName, age, hobby, selectedClasses, gartenNameTextView.getText().toString());

                // Use the new function in FireBaseManager to register the child
                FireBaseManager fireBaseManager = new FireBaseManager(getContext());
                fireBaseManager.registerChildWithParent(child, userId, getView(), gartenId -> {
                    if (gartenId != null) {
                        showRegistrationSuccessDialog(); // Show the AlertDialog after successful registration
                    } else {
                        Toast.makeText(getContext(), "Failed to register child", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    /**
     * Shows a dialog to the user indicating that the registration was successful.
     * Offers the option to register another child or return to the SearchGardenFragment.
     */
    private void showRegistrationSuccessDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Registration Successful")
                .setMessage("Would you like to register another child?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear the fields to allow registering another child
                    clearChildDetails();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Return to the SearchGardenFragment
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new SearchGardenFragment())
                            .addToBackStack(null)
                            .commit();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Clears the child details fields for a new registration.
     */
    private void clearChildDetails() {
        childIdEditText.setText("");
        childNameEditText.setText("");
        childAgeEditText.setText("");
        childHobbyEditText.setText("");
        selectedClasses.clear();
    }

    /**
     * Loads the details of the garden based on the provided garden name.
     *
     * @param gartenName The name of the garden.
     */
    private void loadGartenDetailsByName(String gartenName) {
        FireBaseManager fireBaseManager = new FireBaseManager(getContext());
        fireBaseManager.getGartenIdByName(gartenName, gartenId -> {
            if (gartenId != null) {
                loadGartenDetails(gartenId);  // Load the garden details by ID
            } else {
                Toast.makeText(getContext(), "Failed to find garden with name: " + gartenName, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads the details of the garden based on the garden ID.
     *
     * @param gartenId The ID of the garden.
     */
    private void loadGartenDetails(String gartenId) {
        FireBaseManager fireBaseManager = new FireBaseManager(getContext());
        fireBaseManager.getGartenById(gartenId, garten -> {
            if (garten != null) {
                gartenNameTextView.append(": "+garten.getName());
                gartenAddressTextView.append(": "+garten.getAddress());
                gartenCityTextView.append(": "+garten.getCity());
                gartenPhoneNumberTextView.append(": "+garten.getPhoneNumber());
                gartenOpenTimeTextView.append(": "+garten.getOpenTime());
                gartenCloseTimeTextView.append(": "+garten.getCloseTime());
                gartenOrgAffiliationTextView.append(": "+garten.getOrganizationalAffiliation());
                Glide.with(this).load(garten.getImageUrl()).into(gartenView);

                availableClasses = garten.getClasses();
                if (availableClasses != null && !availableClasses.isEmpty()) {
                    selectClassesButton.setEnabled(true);
                } else {
                    selectClassesButton.setEnabled(false);
                    Toast.makeText(getContext(), "No classes available", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Failed to load garden details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            selectedClasses = data.getStringArrayListExtra("selectedClasses");
            // Update UI or handle the selected classes as needed
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
