package com.example.finalprojectapp.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Adapters.GardenClassAdapter;
import Objects.GardenClass;

public class ClasssForGardenFragment extends Fragment {

    // Argument keys for fragment initialization
    private static final String ARG_GARTEN_ID = "gartenId";
    private static final String ARG_CHILD_CLASSES = "childClasses";

    private String gartenId; // Holds the ID of the kindergarten
    private List<GardenClass> childClasses; // List of child classes
    private RecyclerView recyclerView; // RecyclerView to display the classes
    private GardenClassAdapter classAdapter; // Adapter for the RecyclerView
    private List<GardenClass> classList; // List of classes to be displayed
    private FireBaseManager fireBaseManager; // Firebase manager for data operations
    private boolean isAscending = true; // Tracks the current sorting order

    // Required empty public constructor
    public ClasssForGardenFragment() {}

    /**
     * Creates a new instance of ClasssForGardenFragment with a list of child classes.
     *
     * @param gartenId The ID of the kindergarten.
     * @param childClasses The list of child classes to be displayed.
     * @return A new instance of ClasssForGardenFragment.
     */
    public static ClasssForGardenFragment newInstance(String gartenId, List<GardenClass> childClasses) {
        ClasssForGardenFragment fragment = new ClasssForGardenFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GARTEN_ID, gartenId);
        args.putSerializable(ARG_CHILD_CLASSES, (Serializable) childClasses);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Creates a new instance of ClasssForGardenFragment without a list of child classes.
     *
     * @param gartenId The ID of the kindergarten.
     * @return A new instance of ClasssForGardenFragment.
     */
    public static ClasssForGardenFragment newInstance(String gartenId) {
        ClasssForGardenFragment fragment = new ClasssForGardenFragment();
        Bundle args = new Bundle();
        args.putString("gartenId", gartenId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            gartenId = getArguments().getString(ARG_GARTEN_ID);
            childClasses = (List<GardenClass>) getArguments().getSerializable(ARG_CHILD_CLASSES);
        }
        classList = new ArrayList<>();
        fireBaseManager = new FireBaseManager(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_classs_for_garden, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerViewClasses);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        classAdapter = new GardenClassAdapter(classList, getContext(), fireBaseManager, gartenId);
        recyclerView.setAdapter(classAdapter);

        Button sortButton = view.findViewById(R.id.sortButton);
        sortButton.setOnClickListener(v -> sortClassesByChildCount());

        if (childClasses != null) {
            // Load the provided list of child classes
            loadChildClasses(childClasses);
        } else {
            // Load all classes for the kindergarten
            loadClasses();
        }
    }

    /**
     * Sorts the classes by the number of children.
     * Toggles between ascending and descending order.
     */
    private void sortClassesByChildCount() {
        Collections.sort(classList, (class1, class2) -> {
            int childCount1 = class1.getChildren() != null ? class1.getChildren().size() : 0;
            int childCount2 = class2.getChildren() != null ? class2.getChildren().size() : 0;
            return isAscending ? Integer.compare(childCount1, childCount2) : Integer.compare(childCount2, childCount1);
        });
        isAscending = !isAscending; // Toggle the sorting order for the next sort
        classAdapter.notifyDataSetChanged();
    }

    /**
     * Loads all classes for the specified kindergarten from Firebase.
     */
    private void loadClasses() {
        fireBaseManager.loadClassesForGarten(gartenId, classList, classAdapter, getView());
    }

    /**
     * Loads the provided list of child classes into the RecyclerView.
     *
     * @param childClasses The list of child classes to load.
     */
    private void loadChildClasses(List<GardenClass> childClasses) {
        classList.clear();
        classList.addAll(childClasses);
        classAdapter.notifyDataSetChanged();
    }
}
