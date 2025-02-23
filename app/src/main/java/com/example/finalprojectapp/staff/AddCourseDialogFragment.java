package com.example.finalprojectapp.staff;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import Adapters.CourseArrayAdapter;
import Objects.Garden;
import Objects.GardenClass;
import Objects.GardenStaff;

public class AddCourseDialogFragment extends DialogFragment {

    private Spinner courseSpinner; // Spinner for selecting a course
    private Button addButton; // Button to add the selected course to the staff
    private GardenStaff staff; // The staff member to whom the course will be added
    private FireBaseManager fireBaseManager; // Firebase manager for handling database operations
    private StaffListInGardenFragment parentFragment; // Reference to the parent fragment for refreshing data

    /**
     * Creates a new instance of AddCourseDialogFragment.
     *
     * @param staff The GardenStaff object to which a course will be added.
     * @param parentFragment The parent fragment to refresh after the update.
     * @return A new instance of AddCourseDialogFragment.
     */
    public static AddCourseDialogFragment newInstance(GardenStaff staff, StaffListInGardenFragment parentFragment) {
        AddCourseDialogFragment fragment = new AddCourseDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("staff", staff);
        fragment.setArguments(args);
        fragment.setParentFragment(parentFragment); // Set the parent fragment for later use
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            staff = (GardenStaff) getArguments().getSerializable("staff"); // Retrieve the staff object from arguments
        }
        fireBaseManager = new FireBaseManager(getContext()); // Initialize Firebase manager
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_course_dialog, null); // Inflate the custom dialog layout
        courseSpinner = view.findViewById(R.id.courseSpinner); // Initialize the course spinner
        addButton = view.findViewById(R.id.addButton); // Initialize the add button

        // Load the list of courses into the spinner
        loadCourses();

        addButton.setOnClickListener(v -> {
            GardenClass selectedCourse = (GardenClass) courseSpinner.getSelectedItem(); // Get the selected course
            // Add the selected course to the staff
            fireBaseManager.addCourseToStaff(staff, selectedCourse, new FireBaseManager.GartenIdCallback() {
                @Override
                public void onCallback(String gartenId) {
                    if (gartenId != null) {
                        dismiss(); // Close the dialog
                        parentFragment.loadStaffInGarten(); // Reload the staff list in the parent fragment
                    } else {
                        Snackbar.make(getView(), "Failed to add course", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        });

        dialog.setContentView(view); // Set the custom view to the dialog
        return dialog;
    }

    /**
     * Loads the courses available in the garden associated with the staff member.
     * Populates the spinner with the courses.
     */
    private void loadCourses() {
        fireBaseManager.getDirectorGardens(new FireBaseManager.GartenListCallback() {
            @Override
            public void onCallback(List<Garden> gardenList) {
                if (gardenList != null) {
                    for (Garden garden : gardenList) {
                        if (garden.getName().equals(staff.getGarten().getName())) {
                            List<GardenClass> courses = garden.getClasses();
                            CourseArrayAdapter adapter = new CourseArrayAdapter(getContext(), courses);
                            courseSpinner.setAdapter(adapter);
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * Sets the parent fragment to allow refreshing of data after an update.
     *
     * @param parentFragment The parent fragment that needs to be refreshed.
     */
    public void setParentFragment(StaffListInGardenFragment parentFragment) {
        this.parentFragment = parentFragment;
    }
}
