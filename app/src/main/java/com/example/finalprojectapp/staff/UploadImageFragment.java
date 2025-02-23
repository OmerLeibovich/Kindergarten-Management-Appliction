package com.example.finalprojectapp.staff;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.collection.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Objects.Child;
import Objects.ChildPhoto;

/**
 * UploadImageFragment is a fragment that allows users to upload an image for a specific child.
 * Users can select the image from either the camera or the gallery, and then save the image to Firebase Storage.
 */
public class UploadImageFragment extends Fragment {

    private static final String ARG_CHILD = "child";
    private Child selectedChild;
    private Uri imageUri;
    private Bitmap bitmap;

    /**
     * Creates a new instance of UploadImageFragment with the specified child.
     *
     * @param child The child object for whom the image is being uploaded.
     * @return A new instance of UploadImageFragment.
     */
    public static UploadImageFragment newInstance(Child child) {
        UploadImageFragment fragment = new UploadImageFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CHILD, child);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Displays a snackbar message.
     *
     * @param view The view to find a parent from.
     * @param message The message to be displayed.
     */
    private void showSnackbar(View view, String message) {
        if (view != null && view.getParent() != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedChild = getArguments().getParcelable(ARG_CHILD);
            if (selectedChild != null) {
                Log.d("UploadImageFragment", "Child received: " + selectedChild.getFullName());
            } else {
                Log.e("UploadImageFragment", "selectedChild is null after receiving arguments");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload_image, container, false);

        Log.d("UploadImageFragment", "Child Name: " + (selectedChild != null ? selectedChild.getFullName() : "No Child"));

        TextView textViewChildName = view.findViewById(R.id.textViewChildName);
        if (selectedChild != null) {
            textViewChildName.setText(selectedChild.getFullName());
        }

        Spinner spinnerClasses = view.findViewById(R.id.spinnerClasses);

        List<String> classNameList = new ArrayList<>();
        classNameList.add("None");

        FireBaseManager fireBaseManager = new FireBaseManager(getContext());

        if (selectedChild != null && selectedChild.getHobbies() != null) {
            for (String courseNumber : selectedChild.getHobbies()) {
                fireBaseManager.getCourseTypeFromGarden(selectedChild.getGartenName(), courseNumber, courseType -> {
                    if (courseType != null) {
                        classNameList.add(courseType);
                    } else {
                        classNameList.add("Unknown Course");
                    }

                    if (classNameList.size() == selectedChild.getHobbies().size() + 1) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, classNameList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerClasses.setAdapter(adapter);
                    }
                });
            }
        }

        Button uploadButton = view.findViewById(R.id.buttonUploadImage);
        uploadButton.setOnClickListener(v -> showImageSourceDialog());

        Button saveButton = view.findViewById(R.id.buttonSaveImage);
        saveButton.setOnClickListener(v -> saveImageToCollection());

        return view;
    }

    /**
     * Displays a dialog for the user to choose between taking a photo with the camera or selecting one from the gallery.
     */
    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openGallery();
                    }
                });
        builder.show();
    }

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK) {
                    if (imageUri != null) {
                        Log.d("CameraResult", "Image saved at: " + imageUri.toString());
                    } else {
                        Log.e("CameraError", "Image URI is null");
                    }
                } else {
                    Log.e("CameraError", "Image capture failed or canceled");
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
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

    @SuppressLint("RestrictedApi")
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("CameraError", "Failed to create image file", ex);
            }

            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraLauncher.launch(cameraIntent);
            } else {
                Log.e("CameraError", "Photo file is null");
            }
        } else {
            Log.e("CameraError", "No camera app found");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        galleryLauncher.launch(intent);
    }

    /**
     * Saves the selected image to Firebase Storage and associates it with the selected child.
     * The image is saved with the details of the child and class in the Firebase Firestore.
     */
    private void saveImageToCollection() {
        if (imageUri != null && selectedChild != null) {
            String selectedClass = ((Spinner) getView().findViewById(R.id.spinnerClasses)).getSelectedItem().toString();
            Date currentTime = new Date();

            // Create a ChildPhoto object with the image URL, class name, current time, and child ID
            ChildPhoto newPhoto = new ChildPhoto(null, selectedClass, currentTime, selectedChild.getID());

            FireBaseManager.saveChildPhoto(newPhoto, imageUri,
                    unused -> Toast.makeText(getActivity(), "ChildPhoto saved successfully", Toast.LENGTH_SHORT).show(),
                    e -> Toast.makeText(getActivity(), "Failed to save ChildPhoto: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } else {
            Log.e("SaveImage", "Image URI or selectedChild is null");
            Toast.makeText(getActivity(), "No image or child selected", Toast.LENGTH_SHORT).show();
        }
    }
}
