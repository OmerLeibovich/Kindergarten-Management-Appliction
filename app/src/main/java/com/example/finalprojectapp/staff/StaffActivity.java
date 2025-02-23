package com.example.finalprojectapp.staff;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectapp.MainActivity;
import com.example.finalprojectapp.R;
import com.example.finalprojectapp.TheBestGardensFragment;
import com.example.finalprojectapp.UserSessionManager;
import com.example.finalprojectapp.database.FireBaseManager;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import Adapters.ChildrenAdapter;
import Objects.Child;
import Objects.Garden;
import Objects.GardenStaff;

/**
 * StaffActivity handles the main operations for staff members, directors, and system administrators within the application.
 * The activity manages different views and functionalities depending on the user's role.
 */
public class StaffActivity extends AppCompatActivity {

    private Button addClassButton, addKinderGartenButton, openRegistrationButton, addOrganizationalAffiliationButton, SortByPrecent;
    private String userRole;
    private UserSessionManager session;
    private RecyclerView recyclerView;
    private ChildrenAdapter adapter;
    private List<Child> childrenList;
    private GardenStaff gardenStaff;
    private TabLayout tabLayout;
    private FragmentManager fragmentManager;
    private FireBaseManager fireBaseManager;

    /**
     * Called when the activity is first created. Initializes the UI elements and checks the user's role to display the appropriate view.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        addClassButton = findViewById(R.id.addClassButton);
        addKinderGartenButton = findViewById(R.id.addKinderGartenButton);
        openRegistrationButton = findViewById(R.id.openRegistrationButton);
        addOrganizationalAffiliationButton = findViewById(R.id.addOrganizationalAffiliationButton);
        SortByPrecent = findViewById(R.id.SortByPrecent);

        session = new UserSessionManager(getApplicationContext());
        fireBaseManager = new FireBaseManager(this);

        // Check if the user is logged in
        Log.d("StaffActivity", "Checking if user is logged in");
        if (!session.isLoggedIn()) {
            // If the user is not logged in, navigate to the login screen
            Log.d("StaffActivity", "User is not logged in, navigating to login screen");
            Intent intent = new Intent(StaffActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Get the user's role from the session
        userRole = session.getUserRole();
        Log.d("StaffActivity", "User role from session: " + userRole);

        fragmentManager = getSupportFragmentManager();
        tabLayout = findViewById(R.id.tab_layout);

        if ("KINDER_GARTEN_DIRECTOR".equals(userRole)) {
            initializeDirectorView(savedInstanceState);
        } else if ("SYSTEM_ADMINISTRATOR".equals(userRole)) {
            initializeSystemAdminView();
        } else if ("KINDER_GARTEN_STAFF".equals(userRole)) {
            initializeStaffView();
        } else {
            unauthorizedAccess();
        }
    }

    /**
     * Initializes the view for kindergarten directors. Sets up the tab layout and fragment management for director-specific features.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    private void initializeDirectorView(Bundle savedInstanceState) {
        tabLayout.addTab(tabLayout.newTab().setText("Gardens"));
        tabLayout.addTab(tabLayout.newTab().setText("Staff"));
        tabLayout.addTab(tabLayout.newTab().setText("StaffInGarten"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment;
                switch (tab.getPosition()) {
                    case 0:
                        fragment = new DirectorGardensFragment();
                        break;
                    case 1:
                        fragment = new StaffListFragment();
                        break;
                    case 2:
                        fragment = new StaffListInGardenFragment();
                        break;
                    default:
                        fragment = new MainStaffFragment();
                        break;
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                updateButtonVisibility(fragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        fragmentManager.addOnBackStackChangedListener(() -> {
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            updateButtonVisibility(currentFragment);
        });

        if (savedInstanceState == null) {
            TabLayout.Tab tab = tabLayout.getTabAt(0);
            if (tab != null) {
                tab.select();
            }
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new DirectorGardensFragment())
                    .commit();
        }

        addClassButton.setOnClickListener(v -> replaceFragmentAndHideTabs(new AddGardenClassFragment()));
        addKinderGartenButton.setOnClickListener(v -> replaceFragmentAndHideTabs(new AddGardenFragment()));

        SortByPrecent.setOnClickListener(v -> {
            // Create the AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(StaffActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_percentage_range, null);
            builder.setView(dialogView);

            final EditText minPercentInput = dialogView.findViewById(R.id.minPercentInput);
            final EditText maxPercentInput = dialogView.findViewById(R.id.maxPercentInput);

            builder.setPositiveButton("Confirm", (dialog, which) -> {
                String minPercentStr = minPercentInput.getText().toString().trim();
                String maxPercentStr = maxPercentInput.getText().toString().trim();

                if (!minPercentStr.isEmpty() && !maxPercentStr.isEmpty()) {
                    int minPercent = Integer.parseInt(minPercentStr);
                    int maxPercent = Integer.parseInt(maxPercentStr);

                    // Fetch the gardens from Firebase
                    fireBaseManager.getGardensWithRatingsInRange(minPercent, maxPercent, new FireBaseManager.GardenListCallback() {
                        @Override
                        public void onGardensRetrieved(List<Garden> gardens) {
                            // Update the fragment with the filtered gardens
                            updateFragmentWithFilteredGardens(gardens);
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(StaffActivity.this, "Failed to retrieve gardens: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(StaffActivity.this, "Please fill in both percentage fields", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    /**
     * Initializes the view for system administrators, displaying relevant buttons and setting up fragment management.
     */
    private void initializeSystemAdminView() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new SystemAdministratorsMainFragment())
                .commit();
        tabLayout.setVisibility(View.GONE);
        addClassButton.setVisibility(View.GONE);
        addKinderGartenButton.setVisibility(View.GONE);
        SortByPrecent.setVisibility(View.GONE);
        openRegistrationButton.setVisibility(View.VISIBLE);
        addOrganizationalAffiliationButton.setVisibility(View.VISIBLE);

        openRegistrationButton.setOnClickListener(v -> openRegistrationForAllGardens());
        addOrganizationalAffiliationButton.setOnClickListener(v -> showAddOrganizationalAffiliationDialog());
    }

    /**
     * Initializes the view for kindergarten staff members, displaying the appropriate fragment.
     */
    private void initializeStaffView() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new GardenAndClassesStaffFragment())
                .commit();
        tabLayout.setVisibility(View.GONE);
        addClassButton.setVisibility(View.GONE);
        SortByPrecent.setVisibility(View.GONE);
        addKinderGartenButton.setVisibility(View.GONE);
        openRegistrationButton.setVisibility(View.GONE);
    }

    /**
     * Handles unauthorized access by hiding all elements and redirecting to the main activity.
     */
    private void unauthorizedAccess() {
        tabLayout.setVisibility(View.GONE);
        addClassButton.setVisibility(View.GONE);
        SortByPrecent.setVisibility(View.GONE);
        addKinderGartenButton.setVisibility(View.GONE);
        openRegistrationButton.setVisibility(View.GONE);
        Toast.makeText(this, "Unauthorized access", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(StaffActivity.this, MainActivity.class));
        finish();
    }

    /**
     * Updates the visibility of buttons based on the currently displayed fragment.
     *
     * @param fragment The currently displayed fragment.
     */
    private void updateButtonVisibility(Fragment fragment) {
        if ("KINDER_GARTEN_DIRECTOR".equals(userRole)) {
            if (fragment instanceof DirectorGardensFragment || fragment instanceof StaffListFragment || fragment instanceof StaffListInGardenFragment) {
                addClassButton.setVisibility(View.VISIBLE);
                addKinderGartenButton.setVisibility(View.VISIBLE);
                SortByPrecent.setVisibility(View.VISIBLE);
            } else {
                addClassButton.setVisibility(View.INVISIBLE);
                addKinderGartenButton.setVisibility(View.INVISIBLE);
                SortByPrecent.setVisibility(View.INVISIBLE);
            }
        } else {
            addClassButton.setVisibility(View.GONE);
            addKinderGartenButton.setVisibility(View.GONE);
            SortByPrecent.setVisibility(View.GONE);
        }
    }

    /**
     * Replaces the current fragment and hides the tab layout and buttons.
     *
     * @param fragment The fragment to display.
     */
    public void replaceFragmentAndHideTabs(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        tabLayout.setVisibility(View.GONE);
        addClassButton.setVisibility(View.INVISIBLE);
        addKinderGartenButton.setVisibility(View.INVISIBLE);
        SortByPrecent.setVisibility(View.INVISIBLE);
    }

    /**
     * Handles the back button press to navigate the fragment back stack.
     * If no fragments are in the back stack, the default back button behavior is triggered.
     */
    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            tabLayout.setVisibility(View.VISIBLE);
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            updateButtonVisibility(currentFragment);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Inflates the menu for the activity.
     *
     * @param menu The options menu in which the items are placed.
     * @return true if the menu is displayed, false otherwise.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.staff_menu, menu);
        return true;
    }

    /**
     * Displays the specified fragment.
     *
     * @param fragment The fragment to display.
     */
    private void showFragment(Fragment fragment) {
        Fragment existingFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (existingFragment == null || !existingFragment.getClass().equals(fragment.getClass())) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }

    /**
     * Handles menu item selection, including logout and navigation to other sections.
     *
     * @param item The selected menu item.
     * @return true if the item was handled, false otherwise.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logoutUser();
            return true;
        } else if (id == R.id.action_the_best_gardens) {
            showFragment(new TheBestGardensFragment());
            return true;
        } else if (id == R.id.action_main_page) {
            Intent intent = new Intent(this, StaffActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Logs out the user and navigates to the main activity.
     */
    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        session.logoutUser();
        Intent intent = new Intent(StaffActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Opens registration for all gardens.
     */
    private void openRegistrationForAllGardens() {
        fireBaseManager.updateAllGardensRegistrationStatus(true, success -> {
            if (success) {
                Toast.makeText(this, "Registration opened for all gardens", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to open registration for all gardens", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a dialog to add a new organizational affiliation.
     */
    private void showAddOrganizationalAffiliationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Organizational Affiliation");

        final EditText input = new EditText(this);
        input.setHint("Enter affiliation name");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String affiliationName = input.getText().toString().trim();
            if (!affiliationName.isEmpty()) {
                addOrganizationalAffiliation(affiliationName);
            } else {
                Toast.makeText(this, "Affiliation name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Updates the current fragment with the filtered list of gardens.
     *
     * @param gardens The filtered list of gardens.
     */
    private void updateFragmentWithFilteredGardens(List<Garden> gardens) {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof DirectorGardensFragment) {
            ((DirectorGardensFragment) currentFragment).updateGardenList(gardens);
        } else {
            // If the current fragment is not DirectorGardensFragment, you can decide what to do here
            Log.w("StaffActivity", "Current fragment is not DirectorGardensFragment");
        }
    }

    /**
     * Adds a new organizational affiliation to the system.
     *
     * @param affiliationName The name of the new affiliation.
     */
    private void addOrganizationalAffiliation(String affiliationName) {
        fireBaseManager.addOrganizationalAffiliation(affiliationName, success -> {
            if (success) {
                Toast.makeText(this, "Affiliation added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add affiliation", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
