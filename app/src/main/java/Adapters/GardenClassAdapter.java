package Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;
import com.example.finalprojectapp.staff.AddGardenClassFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import Objects.GardenClass;

/**
 * Adapter for displaying garden classes in a RecyclerView.
 */
public class GardenClassAdapter extends RecyclerView.Adapter<GardenClassAdapter.ClassViewHolder> {

    private List<GardenClass> classes;
    private Context context;
    private FireBaseManager fireBaseManager;
    private String gartenId;

    /**
     * Constructor for GardenClassAdapter.
     *
     * @param classes List of GardenClass objects to be displayed.
     * @param context The context in which the adapter is used.
     * @param fireBaseManager The FireBaseManager instance for Firebase operations.
     * @param gartenId The ID of the garden.
     */
    public GardenClassAdapter(List<GardenClass> classes, Context context, FireBaseManager fireBaseManager, String gartenId) {
        this.classes = classes;
        this.context = context;
        this.fireBaseManager = fireBaseManager;
        this.gartenId = gartenId;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each garden class item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_garden_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        // Bind data to the ViewHolder
        GardenClass gardenClass = classes.get(position);
        holder.className.setText("Course Number: " + gardenClass.getCourseNumber());
        holder.classType.setText("Course Type: " + gardenClass.getCourseType());
        holder.maxChildren.setText("Max Children: " + gardenClass.getMaxChildren());
        holder.ageRange.setText("Age Range: " + gardenClass.getMinAge() + " - " + gardenClass.getMaxAge());

        // Set up click listeners for delete and edit buttons
        holder.buttonDeleteClass.setOnClickListener(v -> showDeleteDialog(holder.itemView, gardenClass, position));
        holder.buttonEditClass.setOnClickListener(v -> showEditFragment(gardenClass));
    }

    @Override
    public int getItemCount() {
        // Return the number of garden classes
        return classes.size();
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView className;
        TextView classType;
        TextView maxChildren;
        TextView ageRange;
        Button buttonDeleteClass;
        Button buttonEditClass;

        public ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the TextViews and Buttons
            className = itemView.findViewById(R.id.textViewClassName);
            classType = itemView.findViewById(R.id.textViewClassType);
            maxChildren = itemView.findViewById(R.id.textViewMaxChildren);
            ageRange = itemView.findViewById(R.id.textViewAgeRange);
            buttonDeleteClass = itemView.findViewById(R.id.buttonDeleteClass);
            buttonEditClass = itemView.findViewById(R.id.buttonUpdateClass);
        }
    }

    /**
     * Shows a confirmation dialog to delete a garden class.
     *
     * @param view The view to anchor the Snackbar.
     * @param gardenClass The garden class to be deleted.
     * @param position The position of the garden class in the list.
     */
    private void showDeleteDialog(View view, GardenClass gardenClass, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Class")
                .setMessage("Are you sure you want to delete this class?")
                .setPositiveButton("Yes", (dialog, which) -> deleteClass(view, gardenClass, position))
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Deletes a garden class from Firebase and updates the RecyclerView.
     *
     * @param view The view to anchor the Snackbar.
     * @param gardenClass The garden class to be deleted.
     * @param position The position of the garden class in the list.
     */
    private void deleteClass(View view, GardenClass gardenClass, int position) {
        fireBaseManager.deleteClassFromGarten(gartenId, gardenClass, new FireBaseManager.GartenIdCallback() {
            @Override
            public void onCallback(String gartenId) {
                if (gartenId != null) {
                    classes.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, classes.size());
                    Snackbar.make(view, "Class deleted successfully", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(view, "Failed to delete class", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Shows a fragment for editing a garden class.
     *
     * @param gardenClass The garden class to be edited.
     */
    private void showEditFragment(GardenClass gardenClass) {
        AddGardenClassFragment fragment = AddGardenClassFragment.newInstance(gartenId, gardenClass);
        ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
