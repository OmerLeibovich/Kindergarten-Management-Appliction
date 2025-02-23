package com.example.finalprojectapp.staff;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import Objects.Garden;

public class AddGardenFragment extends Fragment {

    private EditText nameField, addressField, cityField, phoneNumberField;
    private Button openTimeButton, closeTimeButton, uploadImageButton, addButton;
    private TextView openTimeTextView, closeTimeTextView;
    private Spinner organizationalAffiliationSpinner;
    private FireBaseManager fireBaseManager;
    private String openTime, closeTime;
    private Uri imageUri;
    private Bitmap bitmap;
    private Garden gardenToEdit;

    public AddGardenFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of AddGardenFragment for editing an existing garden.
     *
     * @param garden The garden object to edit.
     * @return A new instance of AddGardenFragment.
     */
    public static AddGardenFragment newInstance(Garden garden) {
        AddGardenFragment fragment = new AddGardenFragment();
        Bundle args = new Bundle();
        args.putSerializable("garden", garden);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_garden, container, false);
    }

    /**
     * Displays a Snackbar or Toast with a specified message.
     *
     * @param view The view to display the Snackbar.
     * @param message The message to display.
     */
    private void showSnackbar(View view, String message) {
        if (view != null && view.getParent() != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        nameField = view.findViewById(R.id.nameField);
        addressField = view.findViewById(R.id.addressField);
        cityField = view.findViewById(R.id.cityField);
        phoneNumberField = view.findViewById(R.id.phoneNumberField);
        openTimeButton = view.findViewById(R.id.openTimeButton);
        closeTimeButton = view.findViewById(R.id.closeTimeButton);
        openTimeTextView = view.findViewById(R.id.openTimeTextView);
        closeTimeTextView = view.findViewById(R.id.closeTimeTextView);
        organizationalAffiliationSpinner = view.findViewById(R.id.organizationalAffiliationSpinner);
        uploadImageButton = view.findViewById(R.id.uploadImageButton);
        addButton = view.findViewById(R.id.addButton);

        fireBaseManager = new FireBaseManager(getContext());

        // Load organizational affiliations from Firebase and display them in the spinner
        loadOrganizationalAffiliations();

        if (getArguments() != null) {
            gardenToEdit = (Garden) getArguments().getSerializable("garden");
            if (gardenToEdit != null) {
                populateFields(gardenToEdit);
            }
        }

        openTimeButton.setOnClickListener(v -> showTimePickerDialog(time -> {
            openTime = time;
            openTimeTextView.setText("Open Time: " + openTime);
        }, "Open Time"));

        closeTimeButton.setOnClickListener(v -> showTimePickerDialog(time -> {
            closeTime = time;
            closeTimeTextView.setText("Close Time: " + closeTime);
        }, "Close Time"));

        uploadImageButton.setOnClickListener(v -> openFileChooser());

        addButton.setText(gardenToEdit == null ? "Add" : "Update");
        addButton.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String address = addressField.getText().toString().trim();
            String city = cityField.getText().toString().trim();
            String phoneNumber = phoneNumberField.getText().toString().trim();
            String organizationalAffiliation = organizationalAffiliationSpinner.getSelectedItem().toString();

            // Validate input fields
            if (name.isEmpty() || address.isEmpty() || city.isEmpty() || phoneNumber.isEmpty() || openTime == null || closeTime == null || (bitmap == null && gardenToEdit == null)) {
                Toast.makeText(getActivity(), "Please fill all fields and upload an image", Toast.LENGTH_SHORT).show();
            } else {
                if (gardenToEdit != null) {
                    // Update existing garden
                    updateGarden(name, address, city, phoneNumber, openTime, closeTime, organizationalAffiliation);
                } else {
                    // Upload image and save new garden data
                    uploadImageAndSaveData(name, address, city, phoneNumber, openTime, closeTime, organizationalAffiliation);
                }
            }
        });
    }

    /**
     * Loads organizational affiliations from Firebase and sets them in the spinner.
     */
    private void loadOrganizationalAffiliations() {
        fireBaseManager.getOrganizationalAffiliations(affiliations -> {
            if (affiliations != null && !affiliations.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, affiliations);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                organizationalAffiliationSpinner.setAdapter(adapter);

                // If editing a garden, set the current value of the spinner
                if (gardenToEdit != null) {
                    int position = affiliations.indexOf(gardenToEdit.getOrganizationalAffiliation());
                    if (position >= 0) {
                        organizationalAffiliationSpinner.setSelection(position);
                    }
                }
            } else {
                Toast.makeText(getContext(), "Failed to load affiliations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Populates the fields with the details of the garden to edit.
     *
     * @param garden The garden object containing the details to populate.
     */
    private void populateFields(Garden garden) {
        nameField.setText(garden.getName());
        addressField.setText(garden.getAddress());
        cityField.setText(garden.getCity());
        phoneNumberField.setText(garden.getPhoneNumber());
        openTime = garden.getOpenTime();
        closeTime = garden.getCloseTime();
        openTimeTextView.setText("Open Time: " + openTime);
        closeTimeTextView.setText("Close Time: " + closeTime);
        // If organizational affiliations are already loaded in the spinner, set the current value
        if (organizationalAffiliationSpinner.getAdapter() != null) {
            int position = ((ArrayAdapter<String>) organizationalAffiliationSpinner.getAdapter()).getPosition(garden.getOrganizationalAffiliation());
            organizationalAffiliationSpinner.setSelection(position);
        }
    }

    /**
     * Displays a time picker dialog to select a time.
     *
     * @param callback The callback to handle the selected time.
     * @param title The title of the time picker dialog.
     */
    private void showTimePickerDialog(TimePickerCallback callback, String title) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minuteOfHour) -> {
            String time = String.format("%02d:%02d", hourOfDay, minuteOfHour);
            callback.onTimeSet(time);
        }, hour, minute, true);

        timePickerDialog.setTitle(title);
        timePickerDialog.show();
    }

    /**
     * Opens a file chooser to select an image.
     */
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLauncher.launch(intent);
    }

    /**
     * Handles the result of the file chooser activity.
     */
    private ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        try {
                            InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
                            bitmap = BitmapFactory.decodeStream(imageStream);
                            showSnackbar(getActivity().findViewById(android.R.id.content), "Image selected successfully");
                        } catch (Exception e) {
                            showSnackbar(getActivity().findViewById(android.R.id.content), "Image selection failed");
                        }
                    } else {
                        showSnackbar(getActivity().findViewById(android.R.id.content), "Image selection failed");
                    }
                }
            });

    /**
     * Uploads the selected image to Firebase and saves the kindergarten data.
     *
     * @param name The name of the kindergarten.
     * @param address The address of the kindergarten.
     * @param city The city where the kindergarten is located.
     * @param phoneNumber The phone number of the kindergarten.
     * @param openTime The opening time of the kindergarten.
     * @param closeTime The closing time of the kindergarten.
     * @param organizationalAffiliation The organizational affiliation of the kindergarten.
     */
    private void uploadImageAndSaveData(String name, String address, String city, String phoneNumber, String openTime, String closeTime, String organizationalAffiliation) {
        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            final String imageName = System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageRef.child(imageName);

            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnFailureListener(e -> {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }).addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                saveKinderGartenData(name, address, city, phoneNumber, openTime, closeTime, organizationalAffiliation, imageUrl);
                Toast.makeText(getActivity(), "Image uploaded successfully", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(e -> {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }));
        } else {
            Toast.makeText(getActivity(), "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the kindergarten data to Firebase.
     *
     * @param name The name of the kindergarten.
     * @param address The address of the kindergarten.
     * @param city The city where the kindergarten is located.
     * @param phoneNumber The phone number of the kindergarten.
     * @param openTime The opening time of the kindergarten.
     * @param closeTime The closing time of the kindergarten.
     * @param organizationalAffiliation The organizational affiliation of the kindergarten.
     * @param imageUrl The URL of the uploaded image.
     */
    private void saveKinderGartenData(String name, String address, String city, String phoneNumber, String openTime, String closeTime, String organizationalAffiliation, String imageUrl) {
        Garden garden = new Garden(name, address, city, phoneNumber, openTime, closeTime, organizationalAffiliation, imageUrl);

        fireBaseManager.addKinderGarten(garden, new FireBaseManager.GartenIdCallback() {
            @Override
            public void onCallback(String gartenId) {
                if (gartenId != null) {
                    View view = getView();
                    if (view != null) {
                        Snackbar.make(view, "KinderGarten added successfully", Snackbar.LENGTH_SHORT).show();
                    }
                    if (isAdded()) {
                        Intent intent = new Intent(getActivity(), StaffActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                } else {
                    View view = getView();
                    if (view != null) {
                        Snackbar.make(view, "Failed to add KinderGarten", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        }, getView());
    }

    /**
     * Updates the existing garden with the provided data.
     *
     * @param name The name of the garden.
     * @param address The address of the garden.
     * @param city The city where the garden is located.
     * @param phoneNumber The phone number of the garden.
     * @param openTime The opening time of the garden.
     * @param closeTime The closing time of the garden.
     * @param organizationalAffiliation The organizational affiliation of the garden.
     */
    private void updateGarden(String name, String address, String city, String phoneNumber, String openTime, String closeTime, String organizationalAffiliation) {
        gardenToEdit.setName(name);
        gardenToEdit.setAddress(address);
        gardenToEdit.setCity(city);
        gardenToEdit.setPhoneNumber(phoneNumber);
        gardenToEdit.setOpenTime(openTime);
        gardenToEdit.setCloseTime(closeTime);
        gardenToEdit.setOrganizationalAffiliation(organizationalAffiliation);

        fireBaseManager.updateKinderGarten(gardenToEdit, new FireBaseManager.GartenIdCallback() {
            @Override
            public void onCallback(String gartenId) {
                View view = getView();
                if (gartenId != null) {
                    showSnackbar(view, "Garden updated successfully");
                    Intent intent = new Intent(getActivity(), StaffActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    showSnackbar(view, "Failed to update garden");
                }
            }
        }, getView());
    }

    /**
     * Callback interface for handling the time set in the TimePickerDialog.
     */
    interface TimePickerCallback {
        void onTimeSet(String time);
    }
}
