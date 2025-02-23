package com.example.finalprojectapp.parents;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.finalprojectapp.MainActivity;
import com.example.finalprojectapp.R;
import com.example.finalprojectapp.TheBestGardensFragment;
import com.example.finalprojectapp.UserSessionManager;
import com.example.finalprojectapp.staff.ViewReviewsFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

/**
 * ParentActivity is the main activity for parents, providing a tabbed interface
 * to search for gardens, view child gardens, and receive notes from staff.
 */
public class ParentActivity extends AppCompatActivity {
    private UserSessionManager session; // Session manager to handle user sessions
    private TabLayout tabLayout; // TabLayout for navigating between different fragments

    private boolean notificationShown; // Flag to track if the approval notification has been shown

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        session = new UserSessionManager(getApplicationContext());

        // Check if the notification has already been shown
        notificationShown = getSharedPreferences("ParentPrefs", MODE_PRIVATE).getBoolean("notificationShown", false);

        String userName = getIntent().getStringExtra("userName");

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Search Garden"));
        tabLayout.addTab(tabLayout.newTab().setText("Child Gardens"));
        tabLayout.addTab(tabLayout.newTab().setText("Note From Staff"));

        // Set the default fragment to SearchGardenFragment
        showFragment(new SearchGardenFragment());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment;
                if (tab.getPosition() == 0) {
                    selectedFragment = new SearchGardenFragment();
                } else if (tab.getPosition() == 1) {
                    selectedFragment = new ChildGardensFragment();
                } else {
                    selectedFragment = new NoteFromStaff();
                }
                showFragment(selectedFragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Displays the specified fragment in the fragment container.
     *
     * @param fragment The fragment to display.
     */
    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
        listenForChildApprovalUpdates(session.getUserEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logoutUser();
            return true;
        } else if (id == R.id.action_view_comments) {
            showFragment(new ViewReviewsFragment());
            return true;
        } else if (id == R.id.action_the_best_gardens) {
            showFragment(new TheBestGardensFragment());
            return true;
        } else if (id == R.id.action_main_page) {
            Intent intent = new Intent(this, ParentActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Logs out the current user and redirects to the MainActivity.
     */
    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        session.logoutUser();
        Intent intent = new Intent(ParentActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Displays the SearchResultsFragment with the specified arguments.
     *
     * @param args The arguments to pass to the SearchResultsFragment.
     */
    public void showSearchResultsFragment(Bundle args) {
        SearchResultsFragment searchResultsFragment = new SearchResultsFragment();
        searchResultsFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, searchResultsFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Listens for updates on child approval status in the specified parent's kindergarten.
     *
     * @param parentEmail The email of the parent to listen for updates.
     */
    private void listenForChildApprovalUpdates(String parentEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Find the parent by email
        db.collection("Parents")
                .whereEqualTo("email", parentEmail)  // Use the email to find the parent
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot parentDoc = task.getResult().getDocuments().get(0);
                        List<Map<String, Object>> childrenList = (List<Map<String, Object>>) parentDoc.get("children");

                        // For each child of the parent, check if they have been approved in a kindergarten
                        if (childrenList != null) {
                            for (Map<String, Object> child : childrenList) {
                                String childId = (String) child.get("id");

                                // Search all kindergartens to see if the child has been approved
                                db.collection("kindergartens")
                                        .whereEqualTo("children." + childId + ".approved", true)
                                        .addSnapshotListener((snapshots, e) -> {
                                            if (e != null) {
                                                Log.w("Firebase", "Listen failed.", e);
                                                return;
                                            }

                                            if (snapshots != null) {
                                                for (DocumentSnapshot kindergartenDoc : snapshots.getDocuments()) {
                                                    Map<String, Object> childrenMap = (Map<String, Object>) kindergartenDoc.get("children");
                                                    if (childrenMap != null) {
                                                        Map<String, Object> childMap = (Map<String, Object>) childrenMap.get(childId);
                                                        if (childMap != null && Boolean.TRUE.equals(childMap.get("approved"))) {
                                                            String childName = (String) child.get("fullName"); // Retrieve the child's name from the data
                                                            sendApprovalNotificationToParent(childName);
                                                        }
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    } else {
                        Log.w("Firebase", "Parent not found for email: " + parentEmail);
                    }
                });
    }

    /**
     * Sends an approval notification to the parent if it hasn't already been shown.
     *
     * @param childName The name of the child who has been approved.
     */
    private void sendApprovalNotificationToParent(String childName) {
        if (!notificationShown) {
            notificationShown = true;  // Update the flag that the notification has been shown

            // Save the information in SharedPreferences that the notification has been shown
            SharedPreferences.Editor editor = getSharedPreferences("ParentPrefs", MODE_PRIVATE).edit();
            editor.putBoolean("notificationShown", true);
            editor.apply();

            runOnUiThread(() -> {
                // Display a notification to the parent with the child's name
                new AlertDialog.Builder(ParentActivity.this)
                        .setTitle("Child Approved")
                        .setMessage("Your child " + childName + " has been approved for the kindergarten.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }
    }
}
