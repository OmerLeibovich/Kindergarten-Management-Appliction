package com.example.finalprojectapp.parents;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.UserSessionManager;
import com.example.finalprojectapp.database.FireBaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Adapters.NoteAdapter;
import Objects.Note;

/**
 * A fragment that displays notes sent by staff to parents.
 */
public class NoteFromStaff extends Fragment {

    private RecyclerView recyclerViewNotes; // RecyclerView to display the list of notes
    private NoteAdapter noteAdapter; // Adapter for the RecyclerView
    private List<Note> notesList = new ArrayList<>(); // List of notes to be displayed
    private FireBaseManager fireBaseManager; // Firebase manager for data operations
    private UserSessionManager sessionManager; // Session manager to handle user sessions

    public NoteFromStaff() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_from_staff, container, false);

        recyclerViewNotes = view.findViewById(R.id.recyclerViewNotes);
        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(getContext()));

        fireBaseManager = new FireBaseManager(getContext());
        sessionManager = new UserSessionManager(getContext());

        // Get the parent's email from the session
        String parentEmail = sessionManager.getUserEmail(); // This function should return the parent's email from the session

        loadNotes(parentEmail); // Load notes based on the parent's email

        return view;
    }

    /**
     * Loads notes from Firebase based on the parent's email.
     *
     * @param parentEmail The email of the parent whose notes are to be loaded.
     */
    private void loadNotes(String parentEmail) {
        fireBaseManager.loadNotesFromFirebaseByParentEmail(parentEmail, new FireBaseManager.OnNotesLoadedListener() {
            @Override
            public void onNotesLoaded(List<Note> notes, Map<String, Integer> behaviorRatings) {
                Log.d("NoteFromStaff", "Notes loaded: " + notes); // Log message added
                if (notes != null) {
                    notesList.clear();
                    notesList.addAll(notes); // Add loaded notes to the list
                    if (noteAdapter == null) {
                        noteAdapter = new NoteAdapter(notesList, behaviorRatings); // Initialize the adapter
                        recyclerViewNotes.setAdapter(noteAdapter); // Set the adapter to the RecyclerView
                    } else {
                        noteAdapter.notifyDataSetChanged(); // Notify adapter of data changes
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("NoteFromStaff", "Error loading notes", e); // Log error message
            }
        });
    }
}
