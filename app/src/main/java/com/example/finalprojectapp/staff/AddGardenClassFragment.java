package com.example.finalprojectapp.staff;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import Objects.GardenClass;

public class AddGardenClassFragment extends Fragment {

    private EditText courseNumberField, maxChildrenField, minAgeField, maxAgeField; // Input fields for course number, max children, min and max age
    private Spinner courseTypeSpinner, gartenSpinner; // Spinners for selecting course type and garden
    private Button addButton; // Button to add or update a class
    private FireBaseManager fireBaseManager; // Firebase manager for data operations
    private GardenClass gardenClass; // GardenClass object being added or updated
    private String gartenId; // ID of the garden associated with the class
    private FirebaseFirestore db; // Firestore database instance

    public AddGardenClassFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of AddGardenClassFragment.
     *
     * @param gartenId The ID of the garden associated with the class.
     * @param gardenClass The GardenClass object to be edited (null if adding a new class).
     * @return A new instance of AddGardenClassFragment.
     */
    public static AddGardenClassFragment newInstance(String gartenId, GardenClass gardenClass) {
        AddGardenClassFragment fragment = new AddGardenClassFragment();
        Bundle args = new Bundle();
        args.putString("gartenId", gartenId);
        args.putSerializable("gartenClass", gardenClass);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_garden_class, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Connect the View to the fields and buttons
        courseNumberField = view.findViewById(R.id.courseNumberField);
        courseTypeSpinner = view.findViewById(R.id.courseTypeSpinner);
        maxChildrenField = view.findViewById(R.id.maxChildrenField);
        minAgeField = view.findViewById(R.id.minAgeField);
        maxAgeField = view.findViewById(R.id.maxAgeField);
        gartenSpinner = view.findViewById(R.id.gartenSpinner);
        addButton = view.findViewById(R.id.addButton);
        fireBaseManager = new FireBaseManager(getContext());
        db = FirebaseFirestore.getInstance();

        // Log to track when data arrives in the Fragment
        Log.d("AddKinderGartenClass", "onViewCreated called");

        if (getArguments() != null) {
            gartenId = getArguments().getString("gartenId");
            gardenClass = (GardenClass) getArguments().getSerializable("gartenClass");

            // Log to track the received data
            Log.d("AddKinderGartenClass", "gartenId: " + gartenId);
            Log.d("AddKinderGartenClass", "gartenClass: " + gardenClass);

            if (gardenClass != null) {
                // Populate the fields with the data of the existing class
                courseNumberField.setText(gardenClass.getCourseNumber());
                // Add logic here to set the spinner to the appropriate value
                maxChildrenField.setText(String.valueOf(gardenClass.getMaxChildren()));
                minAgeField.setText(String.valueOf(gardenClass.getMinAge()));
                maxAgeField.setText(String.valueOf(gardenClass.getMaxAge()));
                addButton.setText("Update Class"); // Change button text to "Update Class"
            }
        }

        // Populate the garden spinner with data from Firebase
        fireBaseManager.populateGartenSpinner(gartenSpinner);

        addButton.setOnClickListener(v -> {
            String courseNumber = courseNumberField.getText().toString().trim();
            String courseType = courseTypeSpinner.getSelectedItem().toString();
            String gartenName = gartenSpinner.getSelectedItem().toString();
            String maxChildrenText = maxChildrenField.getText().toString().trim();
            String minAgeText = minAgeField.getText().toString().trim();
            String maxAgeText = maxAgeField.getText().toString().trim();

            Log.d("AddKinderGartenClass", "courseNumber: " + courseNumber);
            Log.d("AddKinderGartenClass", "courseType: " + courseType);
            Log.d("AddKinderGartenClass", "gartenName: " + gartenName);
            Log.d("AddKinderGartenClass", "maxChildrenText: " + maxChildrenText);
            Log.d("AddKinderGartenClass", "minAgeText: " + minAgeText);
            Log.d("AddKinderGartenClass", "maxAgeText: " + maxAgeText);

            // Validate fields
            if (courseNumber.isEmpty() || courseType.isEmpty() || maxChildrenText.isEmpty() || minAgeText.isEmpty() || maxAgeText.isEmpty()) {
                Snackbar.make(view, "Please fill all fields", Snackbar.LENGTH_SHORT).show();
                return;
            }

            int maxChildren;
            int minAge;
            int maxAge;

            try {
                maxChildren = Integer.parseInt(maxChildrenText);
                minAge = Integer.parseInt(minAgeText);
                maxAge = Integer.parseInt(maxAgeText);
            } catch (NumberFormatException e) {
                Snackbar.make(view, "Please enter valid numbers for max children and age range", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Validate age range
            if (minAge >= maxAge) {
                Snackbar.make(view, "Min age should be less than max age", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Validate minimum children requirement
            if (maxChildren < 15) {
                Snackbar.make(view, "There must be 15 or more children in the class", Snackbar.LENGTH_SHORT).show();
                return;
            }

            String classId = gardenClass == null ? db.collection("kindergartens").document().getId() : gardenClass.getId();
            GardenClass kinderGardenClass = new GardenClass(classId, courseNumber, courseType, maxChildren, minAge, maxAge);

            fireBaseManager.getGartenIdByName(gartenName, gartenId -> {
                if (gartenId == null) {
                    Snackbar.make(view, "Failed to get garden ID", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                fireBaseManager.isCourseNumberExists(gartenId, courseNumber, existingGartenId -> {
                    if (existingGartenId != null) {
                        Snackbar.make(view, "A class with this course number already exists", Snackbar.LENGTH_SHORT).show();
                    } else {
                        if (gardenClass == null) {
                            // Add new class
                            fireBaseManager.addKinderGartenClass(gartenId, kinderGardenClass, getView(), new FireBaseManager.GartenIdCallback() {
                                @Override
                                public void onCallback(String gartenId) {
                                    if (gartenId != null) {
                                        Snackbar.make(getView(), "Class added successfully", Snackbar.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getActivity(), StaffActivity.class);
                                        startActivity(intent);
                                        getActivity().finish();
                                    } else {
                                        Snackbar.make(getView(), "Failed to add class", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            // Update existing class
                            fireBaseManager.updateClassInGarten(gartenId, kinderGardenClass, getView(), new FireBaseManager.GartenIdCallback() {
                                @Override
                                public void onCallback(String gartenId) {
                                    if (gartenId != null) {
                                        Log.d("Firebase", "Class updated successfully in Fragment");
                                        Snackbar.make(getView(), "Class updated successfully", Snackbar.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getActivity(), StaffActivity.class);
                                        startActivity(intent);
                                        getActivity().finish();
                                    } else {
                                        Log.e("Firebase", "Failed to update class in Fragment");
                                        Snackbar.make(getView(), "Failed to update class", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            });
        });
    }
}
