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

import Objects.Garden;
import Objects.GardenStaff;

public class AddGardenDialogFragment extends DialogFragment {

    private Spinner gartenSpinner; // Spinner for selecting a garden
    private Button AddButton; // Button to confirm the addition of a garden to staff
    private GardenStaff staff; // The staff object being modified
    private FireBaseManager fireBaseManager; // Manager for Firebase operations
    private StaffListFragment parentFragment; // Reference to the parent fragment for refreshing data

    /**
     * Creates a new instance of AddGardenDialogFragment.
     *
     * @param staff The GardenStaff object to which a garden will be added.
     * @param parentFragment The parent fragment to refresh after the update.
     * @return A new instance of AddGardenDialogFragment.
     */
    public static AddGardenDialogFragment newInstance(GardenStaff staff, StaffListFragment parentFragment) {
        AddGardenDialogFragment fragment = new AddGardenDialogFragment();
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_garden_dialog, null); // Inflate the custom dialog layout
        gartenSpinner = view.findViewById(R.id.gartenSpinner); // Initialize the garden spinner
        AddButton = view.findViewById(R.id.AddButton); // Initialize the add button

        // Populate the spinner with garden names from Firebase
        fireBaseManager.populateGartenSpinner(gartenSpinner);

        AddButton.setOnClickListener(v -> {
            String selectedGarten = (String) gartenSpinner.getSelectedItem(); // Get the selected garden name
            fireBaseManager.getGartenIdByName(selectedGarten, new FireBaseManager.GartenIdCallback() {
                @Override
                public void onCallback(String gartenId) {
                    if (gartenId != null) {
                        // Fetch garden details by ID
                        fireBaseManager.getGartenById(gartenId, new FireBaseManager.GartenCallback() {
                            @Override
                            public void onCallback(Garden garden) {
                                if (garden != null) {
                                    staff.setGarten(garden); // Assign the garden to the staff
                                    // Update the staff's garden information in Firebase
                                    fireBaseManager.updateKinderGartenStaff(staff, new FireBaseManager.GartenIdCallback() {
                                        @Override
                                        public void onCallback(String gartenId) {
                                            if (gartenId != null) {
                                                dismiss(); // Close the dialog
                                                parentFragment.loadStaffWithoutGarten(); // Reload the staff list in the parent fragment
                                            } else {
                                                Snackbar.make(getView(), "Failed to update staff with garden", Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Snackbar.make(getView(), "Failed to get garden details", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Snackbar.make(getView(), "Failed to get garden ID", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        });

        dialog.setContentView(view); // Set the custom view to the dialog
        return dialog;
    }

    /**
     * Sets the parent fragment to allow refreshing of data after an update.
     *
     * @param parentFragment The parent fragment that needs to be refreshed.
     */
    public void setParentFragment(StaffListFragment parentFragment) {
        this.parentFragment = parentFragment;
    }
}
