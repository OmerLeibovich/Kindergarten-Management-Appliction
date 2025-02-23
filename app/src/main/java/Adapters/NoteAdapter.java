package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectapp.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import Objects.Note;

/**
 * Adapter for displaying notes in a RecyclerView.
 */
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notesList;
    private Map<String, Integer> behaviorRatings; // Stores behavior ratings

    /**
     * Constructor for NoteAdapter.
     *
     * @param notesList List of notes to be displayed.
     * @param behaviorRatings Map of behavior ratings for different courses.
     */
    public NoteAdapter(List<Note> notesList, Map<String, Integer> behaviorRatings) {
        this.notesList = notesList;
        this.behaviorRatings = behaviorRatings; // Initialize behavior ratings
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each note item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        // Bind data to the ViewHolder
        Note note = notesList.get(position);

        holder.textViewAuthor.setText("Author: " + note.getAuthorName());
        holder.textViewRole.setText("Role: " + note.getAuthorRole());
        holder.textViewCourseType.setText("Course Type: " + note.getCourseType());
        holder.textViewNote.setText("Note: " + note.getNote());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        holder.textViewDate.setText("Date: " + sdf.format(note.getDate()));

        // Display the appropriate rating for the course
        if (note.getRating() != null) {
            holder.textViewRating.setText("Rating: " + note.getRating());
        } else {
            holder.textViewRating.setText("Rating: N/A");
        }
    }

    @Override
    public int getItemCount() {
        // Return the number of notes
        return notesList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView textViewAuthor, textViewRole, textViewCourseType, textViewNote, textViewDate, textViewRating;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the TextViews
            textViewAuthor = itemView.findViewById(R.id.textViewAuthor);
            textViewRole = itemView.findViewById(R.id.textViewRole);
            textViewCourseType = itemView.findViewById(R.id.textViewCourseType);
            textViewNote = itemView.findViewById(R.id.textViewNote);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewRating = itemView.findViewById(R.id.textViewRating);
        }
    }
}
