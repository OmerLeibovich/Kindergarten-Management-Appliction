package com.example.finalprojectapp.staff;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;
import java.util.List;

/**
 * UpdateGardenDialogFragment is a dialog fragment that allows the user to update the garden
 * associated with a specific staff member. It presents a list of gardens (excluding the current one)
 * to which the staff member can be reassigned.
 */
public class UpdateGardenDialogFragment extends DialogFragment {

    private Spinner gartenSpinner;
    private Button updateButton;
    private GardenStaff staff;
    private FireBaseManager fireBaseManager;
    private StaffListInGardenFragment parentFragment;

    /**
     * Creates a new instance of UpdateGardenDialogFragment with the specified staff member and parent fragment.
     *
     * @param staff The staff member whose garden assignment is to be updated.
     * @param parentFragment The parent fragment that invoked this dialog.
     * @return A new instance of UpdateGardenDialogFragment.
     */
    public static UpdateGardenDialogFragment newInstance(GardenStaff staff, StaffListInGardenFragment parentFragment) {
        UpdateGardenDialogFragment fragment = new UpdateGardenDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("staff", staff);
        fragment.setArguments(args);
        fragment.setParentFragment(parentFragment);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            staff = (GardenStaff) getArguments().getSerializable("staff");
        }
        fireBaseManager = new FireBaseManager(getContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_update_garden_dialog, null);
        gartenSpinner = view.findViewById(R.id.gartenSpinner);
        updateButton = view.findViewById(R.id.updateButton);

        // Load available gardens, excluding the current one associated with the staff member.
        loadGardensExcludingCurrent();

        updateButton.setOnClickListener(v -> {
            String selectedGarten = (String) gartenSpinner.getSelectedItem();
            fireBaseManager.getGartenIdByName(selectedGarten, gartenId -> {
                if (gartenId != null) {
                    fireBaseManager.getGartenById(gartenId, garden -> {
                        if (garden != null) {
                            staff.setGarten(garden);
                            fireBaseManager.updateKinderGartenStaff(staff, updatedGartenId -> {
                                if (updatedGartenId != null) {
                                    dismiss();
                                    parentFragment.loadStaffInGarten();  // Refresh the staff list in the parent fragment.
                                } else {
                                    Snackbar.make(getView(), "Failed to update staff with garden", Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Snackbar.make(getView(), "Failed to get garden details", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Snackbar.make(getView(), "Failed to get garden ID", Snackbar.LENGTH_SHORT).show();
                }
            });
        });

        dialog.setContentView(view);
        return dialog;
    }

    /**
     * Loads the list of gardens associated with the director, excluding the current garden of the staff member.
     * Populates the spinner with the list of available gardens.
     */
    private void loadGardensExcludingCurrent() {
        fireBaseManager.getDirectorGardens(gardenList -> {
            if (gardenList != null) {
                List<String> gardenNames = new ArrayList<>();
                for (Garden garden : gardenList) {
                    if (!garden.getName().equals(staff.getGarten().getName())) {
                        gardenNames.add(garden.getName());
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, gardenNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                gartenSpinner.setAdapter(adapter);
            }
        });
    }

    /**
     * Sets the parent fragment that invoked this dialog.
     *
     * @param parentFragment The parent fragment that contains the staff list.
     */
    public void setParentFragment(StaffListInGardenFragment parentFragment) {
        this.parentFragment = parentFragment;
    }
}
