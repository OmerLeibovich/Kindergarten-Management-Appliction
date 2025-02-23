package Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;
import com.example.finalprojectapp.staff.UploadImageFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Objects.Child;
import Objects.Note;
import Objects.GardenStaff;

/**
 * Adapter for displaying a list of Child objects in a RecyclerView.
 */
public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildViewHolder> {

    private List<Child> children;
    private Context context;
    private GardenStaff staff;

    /**
     * Constructor for the ChildrenAdapter.
     *
     * @param children List of Child objects to display.
     * @param context The context in which the adapter is running.
     * @param staff The GardenStaff object associated with the current staff.
     */
    public ChildrenAdapter(List<Child> children, Context context, GardenStaff staff) {
        this.children = children;
        this.context = context;
        this.staff = staff;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Child child = children.get(position);

        holder.textViewName.setText(child.getFullName() != null ? child.getFullName() : "Unknown Name");
        holder.textViewAge.setText("Age: " + (child.getAge() != null ? child.getAge() : "Unknown Age"));
        holder.textViewGartenName.setText("Garten: " + (child.getGartenName() != null ? child.getGartenName() : "Unknown Garten"));
        holder.textViewClasses.setText("Classes: " + (child.getHobbies() != null && !child.getHobbies().isEmpty() ? String.join(", ", child.getHobbies()) : "No Classes"));

        holder.buttonAddNote.setOnClickListener(v -> {
            showAddNoteDialog(child);
        });

        holder.UploadButton.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            UploadImageFragment fragment = UploadImageFragment.newInstance(child);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    /**
     * Shows a dialog for adding a note to a Child.
     *
     * @param child The Child object to which the note will be added.
     */
    private void showAddNoteDialog(Child child) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_note, null);
        builder.setView(dialogView);

        Spinner spinnerClasses = dialogView.findViewById(R.id.spinnerClasses);
        EditText editTextNote = dialogView.findViewById(R.id.editTextNote);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBaR);

        // List of course types to be populated after retrieving data from Firebase
        List<String> courseTypes = new ArrayList<>();

        FireBaseManager fireBaseManager = new FireBaseManager(context);

        for (String courseNumber : child.getHobbies()) {
            fireBaseManager.getCourseTypeFromGarden(child.getGartenName(), courseNumber, courseType -> {
                if (courseType != null) {
                    courseTypes.add(courseType);
                } else {
                    courseTypes.add("Unknown Course");
                }

                if (courseTypes.size() == child.getHobbies().size()) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, courseTypes);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerClasses.setAdapter(adapter);
                }
            });
        }

        builder.setPositiveButton("Add Note", (dialog, which) -> {
            String selectedClass = spinnerClasses.getSelectedItem().toString();
            String noteText = editTextNote.getText().toString();
            int rating = Math.round(ratingBar.getRating() * 2);

            // Information about the staff member who is adding the note
            String staffName = staff.getName();
            String staffRole = staff.getRole();

            // Create a new Note object with the writer's details, class type, and rating
            Note note = new Note(noteText, new Date(), staffName, staffRole, selectedClass, rating);

            // Add the note to the child's list of notes
            if (child.getNotes() == null) {
                child.setNotes(new ArrayList<>());
            }

            // Check if the note already exists
            boolean noteExists = false;
            for (Note existingNote : child.getNotes()) {
                if (existingNote.getNote().equals(noteText) && existingNote.getCourseType().equals(selectedClass)) {
                    // If the note exists, update the rating and class type
                    existingNote.setRating(rating);
                    noteExists = true;
                    break;
                }
            }

            // If the note does not exist, add it to the list
            if (!noteExists) {
                child.getNotes().add(note);
            }

            // Save the updated child object to Firebase
            fireBaseManager.updateChild(child);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    /**
     * Updates the list of Child objects and notifies the adapter of data changes.
     *
     * @param children The new list of Child objects.
     */
    public void setChildren(List<Child> children) {
        this.children = children;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for displaying a single Child item.
     */
    public static class ChildViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewAge, textViewGartenName, textViewClasses;
        Button buttonAddNote, UploadButton;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewAge = itemView.findViewById(R.id.textViewAge);
            textViewGartenName = itemView.findViewById(R.id.textViewGartenName);
            textViewClasses = itemView.findViewById(R.id.textViewClasses);
            buttonAddNote = itemView.findViewById(R.id.buttonAddNote);
            UploadButton = itemView.findViewById(R.id.buttonUploadImage);
        }
    }
}
