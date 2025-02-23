package com.example.finalprojectapp.staff;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.finalprojectapp.R;

/**
 * MainStaffFragment is a simple {@link Fragment} subclass that displays a greeting message.
 * This fragment can be instantiated with a parameter, typically used to pass a user's name or role.
 */
public class MainStaffFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    // The parameter to store the value passed during fragment creation
    private String mParam1;

    /**
     * Default constructor required for fragment subclasses.
     */
    public MainStaffFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of this fragment using the provided parameter.
     *
     * @param param1 A string parameter, usually a name or role, to customize the greeting.
     * @return A new instance of fragment MainStaffFragment.
     */
    public static MainStaffFragment newInstance(String param1) {
        MainStaffFragment fragment = new MainStaffFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to do initial creation of the fragment. Retrieves the parameter passed during creation.
     *
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view. Displays the greeting message with the parameter.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to. The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_staff, container, false);
        TextView helloTextView = view.findViewById(R.id.helloTextView);
        helloTextView.setText("Hello " + mParam1);
        return view;
    }
}
