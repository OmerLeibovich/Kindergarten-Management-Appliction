package com.example.finalprojectapp.staff;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

import Adapters.StaffAdapter;
import Objects.GardenStaff;

/**
 * StaffListFragment displays a list of staff members who are not currently assigned to any garden.
 * The fragment allows adding staff members to a garden by launching a dialog.
 */
public class StaffListFragment extends Fragment implements StaffAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private StaffAdapter staffAdapter;
    private List<GardenStaff> staffList;
    private FireBaseManager fireBaseManager;

    /**
     * Default constructor required for fragment subclasses.
     */
    public StaffListFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of StaffListFragment.
     *
     * @return A new instance of fragment StaffListFragment.
     */
    public static StaffListFragment newInstance() {
        return new StaffListFragment();
    }

    /**
     * Called to inflate the layout for this fragment.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_staff_list, container, false);
    }

    /**
     * Called immediately after onCreateView has returned, but before any saved state has been restored into the view.
     * Initializes the RecyclerView and loads the staff members who are not assigned to any garden.
     *
     * @param view               The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        staffList = new ArrayList<>();
        staffAdapter = new StaffAdapter(staffList, this);
        recyclerView.setAdapter(staffAdapter);
        fireBaseManager = new FireBaseManager(getContext());

        loadStaffWithoutGarten();
    }

    /**
     * Loads staff members who are not currently assigned to any garden from Firebase.
     * Updates the RecyclerView with the loaded data.
     */
    public void loadStaffWithoutGarten() {
        fireBaseManager.getStaffWithoutGarten(new FireBaseManager.StaffListCallback() {
            @Override
            public void onCallback(List<GardenStaff> staffList) {
                if (staffList != null) {
                    Log.d("StaffListFragment", "Loaded staff: " + staffList.size()); // Log the size of the loaded list
                    StaffListFragment.this.staffList.clear();
                    StaffListFragment.this.staffList.addAll(staffList);
                    staffAdapter.notifyDataSetChanged();
                } else {
                    Log.e("StaffListFragment", "Failed to load staff");
                    Snackbar.make(getView(), "Failed to load staff", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Handles the "Update" button click for a staff member.
     * This method is not implemented in this fragment.
     *
     * @param staff The GardenStaff object representing the staff member to update.
     */
    @Override
    public void onUpdateClick(GardenStaff staff) {
        // Handle update click
    }

    /**
     * Handles the "Remove" button click for a staff member.
     * This method is not implemented in this fragment.
     *
     * @param staff The GardenStaff object representing the staff member to remove.
     */
    @Override
    public void onRemoveClick(GardenStaff staff) {
        // Handle remove click
    }

    /**
     * Handles the "Add" button click for a staff member, allowing them to be added to a garden.
     *
     * @param staff The GardenStaff object representing the staff member to add.
     */
    @Override
    public void onAddClick(GardenStaff staff) {
        // Handle add click
        AddGardenDialogFragment dialogFragment = AddGardenDialogFragment.newInstance(staff, this);
        dialogFragment.show(getParentFragmentManager(), "UpdateGardenDialog");
    }

    /**
     * Handles the "Add Course" button click for a staff member.
     * This method is not implemented in this fragment.
     *
     * @param staff The GardenStaff object representing the staff member to add a course to.
     */
    @Override
    public void onAddCourseClick(GardenStaff staff) {
        // Handle add course click
    }
}
