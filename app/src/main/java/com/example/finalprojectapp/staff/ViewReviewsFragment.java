package com.example.finalprojectapp.staff;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.UserSessionManager;
import com.example.finalprojectapp.database.FireBaseManager;

import java.util.ArrayList;
import java.util.List;

import Adapters.ReviewsAdapter;
import Objects.Review;

/**
 * ViewReviewsFragment is a fragment responsible for displaying reviews associated with a kindergarten or a parent.
 * It uses Firebase to fetch reviews based on the user's role and the selected kindergarten.
 */
public class ViewReviewsFragment extends Fragment {

    // UI components
    private RecyclerView recyclerView;
    private ReviewsAdapter reviewsAdapter;
    private FireBaseManager fireBaseManager;
    private String gartenName;

    /**
     * Called to create the view hierarchy associated with the fragment.
     * It initializes the RecyclerView and determines whether to load reviews for a parent or a kindergarten based on the user's role.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_reviews, container, false);

        // Initialize RecyclerView and set its layout manager
        recyclerView = view.findViewById(R.id.recyclerViewReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize FireBaseManager
        fireBaseManager = new FireBaseManager(getContext());

        // Get the user's role from the session
        UserSessionManager sessionManager = new UserSessionManager(getContext());
        String userRole = sessionManager.getUserRole();

        // Load reviews based on the user's role
        if (userRole.equals("Parent")) {
            String parentEmail = sessionManager.getUserEmail(); // Assuming there's a method to get the user's email
            loadParentReviews(parentEmail);
        } else {
            if (getArguments() != null) {
                gartenName = getArguments().getString("gartenName");
            }

            reviewsAdapter = new ReviewsAdapter(new ArrayList<>(), fireBaseManager, gartenName);
            recyclerView.setAdapter(reviewsAdapter);

            loadReviews();
        }

        return view;
    }

    /**
     * Loads reviews for the specified kindergarten and updates the RecyclerView adapter with the data.
     */
    private void loadReviews() {
        fireBaseManager.getReviewsForGarten(gartenName, reviews -> {
            if (reviews != null && !reviews.isEmpty()) {
                reviewsAdapter.setReviews(reviews);
            } else {
                Log.d("ViewReviewsFragment", "No reviews found or failed to load reviews.");
                reviewsAdapter.setReviews(new ArrayList<>());
            }
            reviewsAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Loads reviews associated with the specified parent's email and updates the RecyclerView adapter with the data.
     *
     * @param parentEmail The email of the parent whose reviews are to be loaded.
     */
    private void loadParentReviews(String parentEmail) {
        reviewsAdapter = new ReviewsAdapter(new ArrayList<>(), fireBaseManager, null);
        recyclerView.setAdapter(reviewsAdapter);
        fireBaseManager.getReviewsForParent(parentEmail, reviews -> {
            if (reviews != null && !reviews.isEmpty()) {
                reviewsAdapter.setReviews(reviews);
            } else {
                Log.d("ViewReviewsFragment", "No reviews found for parent or failed to load reviews.");
                reviewsAdapter.setReviews(new ArrayList<>());
            }
            reviewsAdapter.notifyDataSetChanged();
        });
    }
}
