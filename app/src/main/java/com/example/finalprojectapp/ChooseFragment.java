package com.example.finalprojectapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.finalprojectapp.parents.RegisterFragment;
import com.example.finalprojectapp.staff.StaffRegisterFragment;

/**
 * ChooseFragment is a fragment that provides the user with the choice to register as either a staff member or a parent.
 * Depending on the button clicked, the appropriate registration fragment is loaded.
 */
public class ChooseFragment extends Fragment {

    public ChooseFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_choose, container, false);

        // Initialize buttons for staff and parent registration
        Button staffButton = view.findViewById(R.id.staffButton);
        Button parentButton = view.findViewById(R.id.parentButton);

        // Set onClickListener for the staff button to load the staff registration fragment
        staffButton.setOnClickListener(v -> {
            loadRegisterStaffFragment();
        });

        // Set onClickListener for the parent button to load the parent registration fragment
        parentButton.setOnClickListener(v -> {
            loadRegisterFragment();
        });

        return view;
    }

    /**
     * Loads the RegisterFragment for parent registration and replaces the current fragment with it.
     * The transaction is added to the back stack so that the user can navigate back.
     */
    private void loadRegisterFragment() {
        RegisterFragment registerFragment = new RegisterFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, registerFragment);
        fragmentTransaction.addToBackStack(null); // Add to back stack so user can navigate back
        fragmentTransaction.commit();
    }

    /**
     * Loads the StaffRegisterFragment for staff registration and replaces the current fragment with it.
     * The transaction is added to the back stack so that the user can navigate back.
     */
    private void loadRegisterStaffFragment() {
        StaffRegisterFragment staffRegisterFragment = new StaffRegisterFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, staffRegisterFragment);
        fragmentTransaction.addToBackStack(null); // Add to back stack so user can navigate back
        fragmentTransaction.commit();
    }
}
