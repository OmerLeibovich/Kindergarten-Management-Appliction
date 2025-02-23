package com.example.finalprojectapp.parents;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.UserSessionManager;
import com.example.finalprojectapp.database.FireBaseManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import Adapters.PhotosAdapter;
import Objects.ChildPhoto;

/**
 * A fragment that displays photos of children in a specific kindergarten for a parent.
 * It allows filtering of photos by class and date.
 */
public class PhotosFragment extends Fragment {
    private RecyclerView recyclerView; // RecyclerView to display the list of photos
    private PhotosAdapter adapter; // Adapter for the RecyclerView
    private List<ChildPhoto> photosList; // List of photos to be displayed
    private List<ChildPhoto> originalPhotosList; // List to store the original set of photos
    private String GardenName; // Name of the kindergarten
    private String parentEmail; // Email of the parent
    private FireBaseManager firebaseManager; // Firebase manager for data operations

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photos, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        photosList = new ArrayList<>();
        originalPhotosList = new ArrayList<>(); // Initialize the list to store the original photos
        adapter = new PhotosAdapter(getActivity(), photosList);
        recyclerView.setAdapter(adapter);

        firebaseManager = new FireBaseManager(getContext());

        Bundle bundle = getArguments();
        if (bundle != null) {
            GardenName = bundle.getString("gartenName");
            parentEmail = bundle.getString("parentEmail");

            Log.d("PhotosFragment", "kindergartenName: " + GardenName);
        }

        // Fetch the list of children for the parent and kindergarten, then load photos
        firebaseManager.getChildrenForParentAndKindergarten(parentEmail, GardenName, childIds -> {
            Log.d("PhotosFragment", "childIds: " + childIds);
            firebaseManager.loadPhotosFromFirestore(childIds, photos -> {
                Log.d("PhotosFragment", "Photos loaded: " + photos.size());
                photosList.clear();
                photosList.addAll(photos);
                originalPhotosList.clear(); // Save the original list of photos
                originalPhotosList.addAll(photos);
                adapter.notifyDataSetChanged();
            });
        });

        // Set up the spinner for class filtering
        Spinner classSpinner = view.findViewById(R.id.classSpinner);
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(getContext(),
                R.array.course_types_filter, android.R.layout.simple_spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(adapterSpinner);

        // Set up the button for selecting a date
        Button buttonSelectDate = view.findViewById(R.id.buttonSelectDate);
        final String[] selectedDate = {""};
        buttonSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Display a date picker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view1, year1, month1, dayOfMonth) -> {
                        month1 += 1;
                        selectedDate[0] = year1 + "-" + String.format("%02d", month1) + "-" + String.format("%02d", dayOfMonth);

                        // Filter the photos based on the selected date
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        List<ChildPhoto> filteredList = new ArrayList<>();

                        for (ChildPhoto photo : originalPhotosList) {
                            String photoDate = dateFormat.format(photo.getTime());
                            if (photoDate.equals(selectedDate[0])) {
                                filteredList.add(photo);
                            }
                        }

                        // Update the list displayed in the RecyclerView
                        adapter.updateList(filteredList);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Set up the button for filtering photos by class
        Button buttonFilterByClass = view.findViewById(R.id.buttonFilterByClass);
        buttonFilterByClass.setOnClickListener(v -> {
            String specificClass = classSpinner.getSelectedItem().toString();

            List<ChildPhoto> filteredList = new ArrayList<>();
            for (ChildPhoto photo : originalPhotosList) {
                if (specificClass.isEmpty()) {
                    filteredList.add(photo);
                } else if (photo.getClassName().equals(specificClass)) {
                    filteredList.add(photo);
                }
            }

            adapter.updateList(filteredList);
        });

        return view;
    }
}
