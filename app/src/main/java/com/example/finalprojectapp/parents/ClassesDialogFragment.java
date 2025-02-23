package com.example.finalprojectapp.parents;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import Objects.GardenClass;

/**
 * A dialog fragment that allows the user to select classes from a list of available garden classes.
 * The user can select up to 3 classes.
 */
public class ClassesDialogFragment extends DialogFragment {

    private List<String> selectedClasses; // List of classes selected by the user
    private List<GardenClass> availableClasses;  // List of available classes

    /**
     * Creates a new instance of ClassesDialogFragment.
     *
     * @param availableClasses The list of available classes.
     * @param selectedClasses  The list of classes that are already selected.
     * @return A new instance of ClassesDialogFragment.
     */
    public static ClassesDialogFragment newInstance(List<GardenClass> availableClasses, List<String> selectedClasses) {
        ClassesDialogFragment fragment = new ClassesDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("availableClasses", new ArrayList<>(availableClasses != null ? availableClasses : new ArrayList<>()));
        args.putStringArrayList("selectedClasses", new ArrayList<>(selectedClasses != null ? selectedClasses : new ArrayList<>()));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        selectedClasses = getArguments().getStringArrayList("selectedClasses");
        availableClasses = (List<GardenClass>) getArguments().getSerializable("availableClasses");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Classes");

        // If no classes are available, show a toast and return an empty dialog
        if (availableClasses == null || availableClasses.isEmpty()) {
            Toast.makeText(getActivity(), "No classes available", Toast.LENGTH_SHORT).show();
            return builder.create();  // Return an empty dialog if no classes are available
        }

        // Create an array for the class names and a boolean array for the checked items
        String[] classesArray = new String[availableClasses.size()];
        boolean[] checkedItems = new boolean[availableClasses.size()];

        // Populate the arrays with the class names and selected status
        for (int i = 0; i < availableClasses.size(); i++) {
            classesArray[i] = availableClasses.get(i).getCourseNumber(); // Assuming `getCourseNumber` method exists
            if (selectedClasses.contains(classesArray[i])) {
                checkedItems[i] = true;
            }
        }

        // Set the multi-choice items for the dialog
        builder.setMultiChoiceItems(classesArray, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                if (selectedClasses.size() < 3) {
                    selectedClasses.add(classesArray[which]);
                } else {
                    ((AlertDialog) dialog).getListView().setItemChecked(which, false);
                    Toast.makeText(getActivity(), "You can select up to 3 classes only.", Toast.LENGTH_SHORT).show();
                }
            } else {
                selectedClasses.remove(classesArray[which]);
            }
        });

        // Set the positive button to return the selected classes
        builder.setPositiveButton("OK", (dialog, which) -> {
            Intent intent = new Intent();
            intent.putStringArrayListExtra("selectedClasses", new ArrayList<>(selectedClasses));
            if (getTargetFragment() != null) {
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
            }
        });

        // Set the negative button to dismiss the dialog
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}
