package com.example.finalprojectapp.staff;

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
 * DirectorGardensFragment is a {@link Fragment} that displays a list of gardens managed by a kindergarten director.
 * It fetches the user type and loads the relevant gardens into a RecyclerView.
 */
public class DirectorGardensFragment extends Fragment {

    // RecyclerView to display the list of gardens
    private RecyclerView recyclerView;

    // Adapter for managing the display of gardens in the RecyclerView
    private GardenAdapter gardenAdapter;

    // List to store the gardens fetched from the database
    private List<Garden> gardenList;

    // FireBaseManager instance for handling Firebase operations
    private FireBaseManager fireBaseManager;

    /**
     * Default constructor for the fragment.
     * Initializes the garden list and FireBaseManager.
     */
    public DirectorGardensFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of this fragment.
     *
     * @return A new instance of fragment DirectorGardensFragment.
     */
    public static DirectorGardensFragment newInstance() {
        return new DirectorGardensFragment();
    }

    /**
     * Called when the fragment is created.
     * Initializes the garden list and FireBaseManager.
     *
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gardenList = new ArrayList<>();
        fireBaseManager = new FireBaseManager(getContext());
    }

    /**
     * Called to initialize the fragment's user interface.
     * Inflates the fragment's layout.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_director_gardens, container, false);
    }

    /**
     * Called after the fragment's view has been created.
     * Sets up the RecyclerView and starts the process to fetch the user type and load the gardens.
     *
     * @param view The View returned by {@link #onCreateView}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerViewGardens);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fetchUserTypeAndLoadGardens(view);
    }

    /**
     * Called when the fragment becomes visible to the user.
     * Reloads the gardens to ensure the displayed data is up-to-date.
     */
    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) {
            loadGardens(view);
        }
    }

    /**
     * Fetches the user type from Firebase and initializes the garden adapter.
     * Then, it loads the gardens into the RecyclerView.
     *
     * @param view The view from which the gardens should be loaded.
     */
    private void fetchUserTypeAndLoadGardens(View view) {
        fireBaseManager.getUserType(new FireBaseManager.UserTypeCallback() {
            @Override
            public void onCallback(String userType) {
                gardenAdapter = new GardenAdapter(gardenList, getActivity(), userType);
                recyclerView.setAdapter(gardenAdapter);
                loadGardens(view);
            }
        });
    }

    /**
     * Updates the list of gardens displayed in the RecyclerView.
     *
     * @param gardens The updated list of gardens to display.
     */
    public void updateGardenList(List<Garden> gardens) {
        // Assumes you have a RecyclerView adapter that displays the gardens
        gardenAdapter.updateGardens(gardens);
    }

    /**
     * Loads the gardens into the RecyclerView.
     *
     * @param view The view from which the gardens should be loaded.
     */
    private void loadGardens(View view) {
        if (gardenAdapter != null) {
            fireBaseManager.loadGardens(gardenList, gardenAdapter, view);
        }
    }
}
