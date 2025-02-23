package com.example.finalprojectapp.parents;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;
import com.google.android.material.snackbar.Snackbar;

import Objects.Review;

/**
 * A fragment that allows parents to submit reviews for a kindergarten.
 */
public class ReviewFragment extends Fragment {

    private RatingBar ratingBar; // RatingBar for selecting a rating
    private EditText editTextReview; // EditText for entering the review text
    private FireBaseManager fireBaseManager; // Firebase manager for database operations

    private String gartenName; // Name of the kindergarten being reviewed
    private String parentEmail; // Email of the parent submitting the review

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        // Initialize the FireBaseManager object
        fireBaseManager = new FireBaseManager(getContext());

        // Initialize UI elements
        ratingBar = view.findViewById(R.id.ratingBar);
        editTextReview = view.findViewById(R.id.editTextReview);
        Button submitButton = view.findViewById(R.id.buttonSubmit);

        // Retrieve parameters passed from the Bundle
        if (getArguments() != null) {
            gartenName = getArguments().getString("gartenName");
            parentEmail = getArguments().getString("parentEmail");
        }

        // Set the click listener for the submit button
        submitButton.setOnClickListener(v -> submitReview());

        return view;
    }

    /**
     * Submits the review to the Firebase database.
     */
    private void submitReview() {
        float rating = ratingBar.getRating();
        int adjustedRating = (int) (rating * 2); // Adjust the rating to a scale of 1-10
        String reviewText = editTextReview.getText().toString().trim();

        // Validate that a rating has been selected
        if (rating == 0) {
            Snackbar.make(getView(), "Please rate the kindergarten", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Validate that a review text has been entered
        if (reviewText.isEmpty()) {
            Snackbar.make(getView(), "Please write a review", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Create a Review object with the entered data
        Review review = new Review(parentEmail, adjustedRating, reviewText);
        fireBaseManager.addReviewToGartenAndParent(gartenName, parentEmail, review); // Submit the review to Firebase

        Snackbar.make(getView(), "Review submitted successfully", Snackbar.LENGTH_SHORT).show();

        // Reset the rating bar and review text field
        ratingBar.setRating(0);
        editTextReview.setText("");
    }
}
