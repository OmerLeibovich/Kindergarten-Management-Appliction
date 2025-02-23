package com.example.finalprojectapp.parents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;

/**
 * A fragment that allows parents to search for gardens based on city, organizational affiliation, and age.
 */
public class SearchGardenFragment extends Fragment {

    private EditText cityEditText; // EditText for inputting the city
    private Spinner organizationSpinner; // Spinner for selecting organizational affiliation
    private EditText ageEditText; // EditText for inputting the child's age
    private Button searchButton; // Button to trigger the search

    public SearchGardenFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_garden, container, false);

        // Initialize the UI elements
        cityEditText = view.findViewById(R.id.cityEditText);
        organizationSpinner = view.findViewById(R.id.organizationSpinner);
        ageEditText = view.findViewById(R.id.ageEditText);
        searchButton = view.findViewById(R.id.searchButton);

        // Initialize FireBaseManager to fetch organizational affiliations
        FireBaseManager fireBaseManager = new FireBaseManager(getContext());
        fireBaseManager.getOrganizationalAffiliations(affiliations -> {
            if (affiliations != null && !affiliations.isEmpty()) {
                affiliations.add(0, ""); // Add an empty option at the start of the list
                // Alternatively, you could use affiliations.add(0, "Select an affiliation"); if you want a different text for the first option

                // Set up the spinner with the list of affiliations
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, affiliations);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                organizationSpinner.setAdapter(adapter);
            } else {
                Toast.makeText(getContext(), "Failed to load affiliations", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the search button click listener
        searchButton.setOnClickListener(v -> {
            String city = cityEditText.getText().toString().trim();
            String organization = organizationSpinner.getSelectedItem().toString();
            String age = ageEditText.getText().toString().trim();

            // Validate that the city field is not empty
            if (city.isEmpty()) {
                Toast.makeText(getContext(), "City is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Bundle the search criteria and pass it to the parent activity to show the search results
            Bundle args = new Bundle();
            args.putString("city", city);
            args.putString("organization", organization);
            args.putString("age", age);

            // Call the parent activity to display the search results fragment
            ((ParentActivity) getActivity()).showSearchResultsFragment(args);
        });

        return view;
    }
}
