package com.example.finalprojectapp.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.UserSessionManager;
import com.example.finalprojectapp.database.FireBaseManager;

import java.util.ArrayList;
import java.util.List;

import Adapters.GardenAdapter;
import Objects.Garden;

/**
 * SystemAdministratorsMainFragment is a fragment that displays a list of all gardens.
 * This fragment is specifically designed for system administrators who have the role "SYSTEM_ADMINISTRATOR".
 * It uses a RecyclerView to show the gardens and loads the data from Firebase using FireBaseManager.
 */
public class SystemAdministratorsMainFragment extends Fragment {

    private RecyclerView recyclerView;
    private GardenAdapter gardenAdapter;
    private FireBaseManager fireBaseManager;
    private List<Garden> gardenList;

    /**
     * Called to create the view hierarchy associated with the fragment.
     * Inflates the layout for this fragment and initializes the RecyclerView and other components.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null if the user role is not "SYSTEM_ADMINISTRATOR".
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_system_administrators_main, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGardens);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        gardenList = new ArrayList<>();

        fireBaseManager = new FireBaseManager(getContext());
        UserSessionManager sessionManager = new UserSessionManager(getContext());

        String userRole = sessionManager.getUserRole();

        if (userRole != null && userRole.equals("SYSTEM_ADMINISTRATOR")) {
            gardenAdapter = new GardenAdapter(gardenList, getContext(), userRole);
            recyclerView.setAdapter(gardenAdapter);
            loadGardens();
        } else {
            return null;
        }
        return view;
    }

    /**
     * Loads the list of all gardens from Firebase and updates the RecyclerView with the data.
     * This method is only called if the user is a system administrator.
     */
    private void loadGardens() {
        fireBaseManager.getAllGardens(gardens -> {
            if (gardens != null) {
                gardenList.clear();
                gardenList.addAll(gardens);
                gardenAdapter.notifyDataSetChanged();
            }
        });
    }
}
