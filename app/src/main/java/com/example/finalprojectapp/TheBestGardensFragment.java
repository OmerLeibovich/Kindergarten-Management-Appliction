package com.example.finalprojectapp;

import android.annotation.SuppressLint;
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

import com.example.finalprojectapp.database.FireBaseManager;

import Adapters.GardenAdapter;

/**
 * TheBestGardensFragment is a Fragment that displays the top three highest-rated gardens.
 * This class uses Firebase to load and display the data in a list.
 */
public class TheBestGardensFragment extends Fragment {

    // UI components
    private RecyclerView recyclerView;
    private GardenAdapter gardenAdapter;
    private FireBaseManager fireBaseManager;

    /**
     * Called when the fragment's UI is created.
     * Loads the view and initializes the RecyclerView for displaying the top-rated gardens.
     *
     * @param inflater       The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container      The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The inflated view to be displayed.
     */
    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the view from the layout
        View view = inflater.inflate(R.layout.fragment_the_best_gardens, container, false);

        // Initialize the RecyclerView and set it up with a vertical LayoutManager
        recyclerView = view.findViewById(R.id.recyclerViewTopGardens);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize FireBaseManager for working with Firebase data
        fireBaseManager = new FireBaseManager(getContext());

        // Load the top-rated gardens
        loadTopRatedGardens();

        return view;
    }

    /**
     * Function to load the top three highest-rated gardens from Firebase.
     * Utilizes the FireBaseManager class to retrieve the data and sets up the adapter for displaying the list.
     */
    private void loadTopRatedGardens() {
        // Call to FirebaseManager to get the top three highest-rated gardens
        fireBaseManager.getTopRatedGardens(3, gardens -> {
            if (gardens != null) {
                // Create the adapter with the retrieved data and display it in the RecyclerView
                gardenAdapter = new GardenAdapter(gardens, getContext(), "viewer");
                recyclerView.setAdapter(gardenAdapter);
            } else {
                Log.d("TheBestGardensFragment", "Failed to load top-rated gardens."); // Log in case of failure to load gardens
            }
        });
    }
}
