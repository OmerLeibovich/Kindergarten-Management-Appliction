package com.example.finalprojectapp.parents;

import android.os.Bundle;
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
 * A fragment that displays the search results based on the criteria provided by the user.
 */
public class SearchResultsFragment extends Fragment {

    private String city; // City filter for searching gardens
    private String organization; // Organization filter for searching gardens
    private String age; // Age filter for searching gardens
    private FireBaseManager fireBaseManager; // Firebase manager for database operations
    private RecyclerView recyclerView; // RecyclerView to display the list of gardens
    private GardenAdapter gardenAdapter; // Adapter for the RecyclerView
    private List<Garden> gardenList; // List of gardens to be displayed

    public SearchResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            city = getArguments().getString("city");
            organization = getArguments().getString("organization", ""); // Default to empty string if not provided
            age = getArguments().getString("age", ""); // Default to empty string if not provided
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);

        recyclerView = view.findViewById(R.id.recyclerView); // Initialize the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Set the layout manager for the RecyclerView

        gardenList = new ArrayList<>();
        gardenAdapter = new GardenAdapter(gardenList, getActivity(), ""); // Initialize the adapter with an empty userType initially
        recyclerView.setAdapter(gardenAdapter); // Set the adapter for the RecyclerView

        // Fetch user type and update the adapter accordingly
        fireBaseManager = new FireBaseManager(getContext());
        fireBaseManager.getUserType(userType -> {
            if (userType != null) {
                gardenAdapter = new GardenAdapter(gardenList, getActivity(), userType); // Update the adapter with the correct userType
                recyclerView.setAdapter(gardenAdapter); // Set the updated adapter to the RecyclerView
                searchGardens(); // Start searching for gardens
            }
        });

        return view;
    }

    /**
     * Searches for gardens based on the provided city, organization, and age filters.
     * Updates the RecyclerView with the search results.
     */
    private void searchGardens() {
        fireBaseManager.searchGardens(city, organization, age, new FireBaseManager.GartenListCallback() {
            @Override
            public void onCallback(List<Garden> gardenList) {
                if (gardenList != null) {
                    SearchResultsFragment.this.gardenList.clear(); // Clear the current list
                    SearchResultsFragment.this.gardenList.addAll(gardenList); // Add the new search results
                    gardenAdapter.notifyDataSetChanged(); // Notify the adapter that the data has changed
                } else {
                    // Handle the case where no gardens are found
                }
            }
        });
    }
}
