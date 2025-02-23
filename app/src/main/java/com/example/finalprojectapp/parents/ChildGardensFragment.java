package com.example.finalprojectapp.parents;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;

import java.util.ArrayList;
import java.util.List;

import Adapters.GardenAdapter;
import Objects.Garden;

/**
 * A fragment that displays a list of gardens associated with the parent.
 */
public class ChildGardensFragment extends Fragment {

    private RecyclerView recyclerViewGardens;
    private GardenAdapter gardenAdapter;
    private List<Garden> gardensList;
    private FireBaseManager fireBaseManager;

    /**
     * Default constructor required for fragment instantiation.
     */
    public ChildGardensFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the layout for this fragment and initializes UI components.
     *
     * @param inflater           LayoutInflater object to inflate the layout.
     * @param container          Parent view that the fragment's UI should be attached to.
     * @param savedInstanceState Bundle object containing saved state.
     * @return The inflated view for this fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_child_gardens, container, false);

        // Initialize RecyclerView and its adapter
        recyclerViewGardens = view.findViewById(R.id.recyclerViewGardens);
        recyclerViewGardens.setLayoutManager(new LinearLayoutManager(getContext()));

        gardensList = new ArrayList<>();
        gardenAdapter = new GardenAdapter(gardensList, getContext(), "parent");
        recyclerViewGardens.setAdapter(gardenAdapter);

        // Initialize FireBaseManager
        fireBaseManager = new FireBaseManager(getContext());

        // Load gardens for the parent
        loadGardensForParent();

        return view;
    }

    /**
     * Loads gardens associated with the parent from Firebase and updates the RecyclerView.
     */
    private void loadGardensForParent() {
        // Assuming FireBaseManager has a method to get gardens with children of the parent
        fireBaseManager.getGardensForParent(gardens -> {
            if (gardens != null) {
                gardensList.clear();
                gardensList.addAll(gardens);
                gardenAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Handles the selection of courses and logs the selected courses.
     *
     * @param selectedCourses List of selected course names.
     */
    public void onCoursesSelected(List<String> selectedCourses) {
        Log.d("ChildGardensFragment", "Selected courses: " + selectedCourses.toString());
        // Update the UI or save the courses as needed
    }
}
