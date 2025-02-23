package com.example.finalprojectapp.parents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.finalprojectapp.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Objects.GardenClass;

/**
 * A dialog fragment that allows the user to select up to 3 courses from a list of available garden classes.
 */
public class SelectCoursesDialogFragment extends DialogFragment {

    private List<GardenClass> gardenClasses; // List of available garden classes
    private List<String> selectedCourses = new ArrayList<>(); // List of selected course IDs

    /**
     * Creates a new instance of SelectCoursesDialogFragment.
     *
     * @param gardenClasses The list of available garden classes.
     * @return A new instance of SelectCoursesDialogFragment.
     */
    public static SelectCoursesDialogFragment newInstance(List<GardenClass> gardenClasses) {
        SelectCoursesDialogFragment fragment = new SelectCoursesDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("gartenClasses", (Serializable) gardenClasses);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gardenClasses = (List<GardenClass>) getArguments().getSerializable("gartenClasses"); // Retrieve the garden classes from arguments
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_select_courses_dialog, null); // Inflate the custom dialog layout
        builder.setView(dialogView);

        LinearLayout coursesContainer = dialogView.findViewById(R.id.coursesContainer); // Container to hold dynamically added checkboxes

        // Adding checkboxes dynamically for each course
        for (GardenClass gardenClass : gardenClasses) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(gardenClass.getId()); // Set the checkbox text to the course ID
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (selectedCourses.size() < 3) {
                        selectedCourses.add(gardenClass.getId()); // Add the course ID to the selected list if not exceeding limit
                    } else {
                        checkBox.setChecked(false); // Prevent selection if more than 3 courses are selected
                        Toast.makeText(getContext(), "You can select up to 3 courses only", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    selectedCourses.remove(gardenClass.getId()); // Remove the course ID from the selected list if unchecked
                }
            });
            coursesContainer.addView(checkBox); // Add the checkbox to the container
        }

        builder.setPositiveButton("OK", (dialog, id) -> {
            // Pass the selected courses back to the target fragment
            ((ChildGardensFragment) getTargetFragment()).onCoursesSelected(selectedCourses);
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss()); // Dismiss the dialog on cancel

        return builder.create(); // Create and return the dialog
    }
}
