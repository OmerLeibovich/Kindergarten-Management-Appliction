package com.example.finalprojectapp.staff;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.finalprojectapp.R;
import com.example.finalprojectapp.UserSessionManager;
import com.example.finalprojectapp.database.FireBaseManager;
import Adapters.ChildrenAdapter;
import Objects.GardenStaff;
import java.util.List;

/**
 * GardenAndClassesStaffFragment is a {@link Fragment} that displays a list of approved children
 * for a specific garden associated with the logged-in staff member.
 * It retrieves the staff member's garden and loads the children assigned to it.
 */
public class GardenAndClassesStaffFragment extends Fragment {

    // RecyclerView to display the list of children
    private RecyclerView recyclerViewChildren;

    // Adapter for managing the display of children in the RecyclerView
    private ChildrenAdapter childrenAdapter;

    // FireBaseManager instance for handling Firebase operations
    private FireBaseManager fireBaseManager;

    /**
     * Called to initialize the fragment's user interface.
     * Sets up the RecyclerView and loads the approved children for the staff's garden.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_garden_and_classes_staff, container, false);

        // Initialize the RecyclerView and set its layout manager
        recyclerViewChildren = view.findViewById(R.id.recyclerViewChildren);
        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize FireBaseManager for Firebase operations
        fireBaseManager = new FireBaseManager(getContext());

        // Retrieve the email of the logged-in staff member from the session manager
        UserSessionManager sessionManager = new UserSessionManager(getContext());
        String staffEmail = sessionManager.getUserEmail();

        // Fetch the staff member's details and load the approved children for their garden
        fireBaseManager.getStaffByEmail(staffEmail, (documentId, staff) -> {
            if (staff != null && staff.getGarten() != null) {
                String gardenName = staff.getGarten().getName();
                loadApprovedChildren(gardenName, staff); // Pass the logged-in staff object
            }
        });

        return view;
    }

    /**
     * Loads the approved children for the given garden and sets up the RecyclerView with the data.
     *
     * @param gardenName The name of the garden associated with the staff member.
     * @param staff The GardenStaff object representing the logged-in staff member.
     */
    private void loadApprovedChildren(String gardenName, GardenStaff staff) {
        fireBaseManager.getApprovedChildrenByGarden(gardenName, children -> {
            if (children != null && !children.isEmpty()) {
                // Initialize the adapter with the list of children and set it on the RecyclerView
                childrenAdapter = new ChildrenAdapter(children, getContext(), staff);
                recyclerViewChildren.setAdapter(childrenAdapter);
            }
        });
    }
}
