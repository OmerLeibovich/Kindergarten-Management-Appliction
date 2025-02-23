package Adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;

import java.util.ArrayList;
import java.util.List;

import Objects.Review;

/**
 * Adapter for displaying a list of Review objects in a RecyclerView.
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private List<Review> reviews;
    private FireBaseManager fireBaseManager;
    private String gartenName;

    /**
     * Constructor for ReviewsAdapter.
     *
     * @param reviews       The list of Review objects to be displayed.
     * @param fireBaseManager The instance of FireBaseManager for Firebase operations.
     * @param gartenName    The name of the garden. If null, indicates a parent user.
     */
    public ReviewsAdapter(List<Review> reviews, FireBaseManager fireBaseManager, String gartenName) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        this.fireBaseManager = fireBaseManager;
        this.gartenName = gartenName;
    }

    /**
     * Updates the list of reviews and notifies the adapter of data changes.
     *
     * @param reviews The new list of Review objects.
     */
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        holder.textViewParentEmail.setText(review.getParentEmail());
        holder.textViewComment.setText(review.getComment());
        holder.textViewRating.setText(String.valueOf(review.getRating()));
        holder.textViewReviewDate.setText(review.getReviewDate().toString());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(holder.itemView.getContext(),
                R.array.admin_rating_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.StatusSpinner.setAdapter(adapter);

        if (gartenName == null) { // Parent user
            holder.textViewResponse.setText("Response: " + (review.getManagerResponse() != null ? review.getManagerResponse() : "No response yet"));
            holder.textViewResponse.setVisibility(View.VISIBLE);
            holder.buttonReply.setVisibility(View.GONE);
            holder.StatusButton.setVisibility(View.GONE);
            holder.StatusSpinner.setVisibility(View.GONE);
        } else {
            holder.textViewResponse.setVisibility(View.GONE);
            holder.buttonReply.setVisibility(View.VISIBLE);
            holder.StatusButton.setVisibility(View.VISIBLE);
            holder.StatusSpinner.setVisibility(View.VISIBLE);

            holder.buttonReply.setOnClickListener(v -> {
                // Create an EditText in the dialog
                EditText input = new EditText(v.getContext());
                input.setHint("Enter your response");

                // Create AlertDialog
                AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                        .setTitle("Manager Response")
                        .setView(input)
                        .setPositiveButton("Submit", (dialogInterface, which) -> {
                            String managerResponse = input.getText().toString().trim();

                            if (!managerResponse.isEmpty()) {
                                // Save the response
                                saveManagerResponse(review, managerResponse, holder.itemView);
                            } else {
                                Toast.makeText(v.getContext(), "Response cannot be empty", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.show();
            });

            // Listener for status button click
            holder.StatusButton.setOnClickListener(v -> {
                String selectedStatus = holder.StatusSpinner.getSelectedItem().toString();

                if (!selectedStatus.isEmpty()) {
                    // Update garden status in Firebase by garden name
                    fireBaseManager.updateGardenStatusByName(gartenName, selectedStatus, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(v.getContext(), "Status updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(v.getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(v.getContext(), "Please select a status", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Saves the manager's response to the review and updates Firebase.
     *
     * @param review          The Review object being updated.
     * @param managerResponse The response text from the manager.
     * @param view            The view associated with this operation.
     */
    private void saveManagerResponse(Review review, String managerResponse, View view) {
        review.setManagerResponse(managerResponse);

        // Update the review in Firebase for the garden by garden name
        fireBaseManager.updateReviewInGardenByName(gartenName, review);

        // Update the review in Firebase for the parent by parent email
        fireBaseManager.updateReviewInParentByEmail(review.getParentEmail(), review);
    }

    /**
     * Reloads the reviews from Firebase and updates the adapter.
     *
     * @param view The view associated with this operation.
     */
    private void reloadReviews(View view) {
        fireBaseManager.getReviewsForGarten(gartenName, reviews -> {
            if (reviews != null && !reviews.isEmpty()) {
                setReviews(reviews);
            } else {
                setReviews(new ArrayList<>());
            }
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    /**
     * ViewHolder class for holding and binding review item views.
     */
    public static class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView textViewParentEmail, textViewComment, textViewRating, textViewReviewDate, textViewResponse;
        Button buttonReply, StatusButton;
        Spinner StatusSpinner;

        /**
         * Constructor for ReviewViewHolder.
         *
         * @param itemView The view for a single review item.
         */
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewParentEmail = itemView.findViewById(R.id.textViewParentEmail);
            textViewComment = itemView.findViewById(R.id.textViewComment);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textViewReviewDate = itemView.findViewById(R.id.textViewReviewDate);
            buttonReply = itemView.findViewById(R.id.buttonReply);
            textViewResponse = itemView.findViewById(R.id.textViewResponse);
            StatusButton = itemView.findViewById(R.id.StatusButton);
            StatusSpinner = itemView.findViewById(R.id.spinnerAdminStatus);
        }
    }
}
