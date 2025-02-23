package com.example.finalprojectapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalprojectapp.parents.ParentActivity;
import com.example.finalprojectapp.staff.StaffActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;

import Objects.Garden;
import com.example.finalprojectapp.database.FireBaseManager;

/**
 * MainActivity is the primary activity of the application, serving as the entry point and checking the logged-in user's type.
 * The class determines the user type and navigates them to the appropriate activity or displays the choice screen if the user is not logged in.
 */
public class MainActivity extends AppCompatActivity {

    // User session management
    private UserSessionManager session;
    private FireBaseManager fireBaseManager;

    /**
     * This function is called when the activity is created.
     * It checks if the user is logged in, and if so, determines the user type and navigates them to the appropriate activity.
     *
     * @param savedInstanceState Saves the instance state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new UserSessionManager(getApplicationContext());
        fireBaseManager = new FireBaseManager(getApplicationContext());

        // Check and update garden registration status if necessary
        checkAndCloseRegistrations();

        if (session.isLoggedIn()) {
            String email = session.getUserEmail();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            checkUserCollection(db, email);
        } else {
            loadChooseFragment(); // If the user is not logged in, load the choice screen
        }
    }

    /**
     * Checks if the user is a parent and navigates accordingly.
     * If not found as a parent, continues to check other collections in Firebase.
     *
     * @param db    FirebaseFirestore object for database access.
     * @param email The user's email address.
     */
    private void checkUserCollection(FirebaseFirestore db, String email) {
        db.collection("Parents").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                session.createLoginSession(email, "Parent");
                navigateToParentActivity();
            } else {
                checkDirectorsCollection(db, email);
            }
        });
    }

    /**
     * Checks if the user is a kindergarten director and navigates accordingly.
     * If not found as a director, continues to check other collections in Firebase.
     *
     * @param db    FirebaseFirestore object for database access.
     * @param email The user's email address.
     */
    private void checkDirectorsCollection(FirebaseFirestore db, String email) {
        db.collection("directors").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                session.createLoginSession(email, "KINDER_GARTEN_DIRECTOR");
                navigateToStaffActivity();
            } else {
                checkStaffCollection(db, email);
            }
        });
    }

    /**
     * Checks if the user is a kindergarten staff member and navigates accordingly.
     * If not found as staff, continues to check other collections in Firebase.
     *
     * @param db    FirebaseFirestore object for database access.
     * @param email The user's email address.
     */
    private void checkStaffCollection(FirebaseFirestore db, String email) {
        db.collection("staff").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                session.createLoginSession(email, "KINDER_GARTEN_STAFF");
                navigateToStaffActivity();
            } else {
                checkSystemAdminsCollection(db, email);
            }
        });
    }

    /**
     * Checks if the user is a system administrator and navigates accordingly.
     * If not found as a system administrator, loads the choice screen.
     *
     * @param db    FirebaseFirestore object for database access.
     * @param email The user's email address.
     */
    private void checkSystemAdminsCollection(FirebaseFirestore db, String email) {
        db.collection("systemAdministrators").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                session.createLoginSession(email, "SYSTEM_ADMINISTRATOR");
                navigateToStaffActivity();
            } else {
                loadChooseFragment();
            }
        });
    }

    /**
     * Navigates the user to the ParentActivity screen for parents.
     */
    private void navigateToParentActivity() {
        Intent intent = new Intent(MainActivity.this, ParentActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Navigates the user to the StaffActivity screen for staff or kindergarten directors.
     */
    private void navigateToStaffActivity() {
        Intent intent = new Intent(MainActivity.this, StaffActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Loads the ChooseFragment if the user is not logged in or not identified.
     */
    private void loadChooseFragment() {
        ChooseFragment chooseFragment = new ChooseFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, chooseFragment)
                .commit();
    }

    /**
     * Checks the registration status of all kindergartens and updates if necessary.
     * Checks if three days have passed since the registration start date, and if so, closes the registration.
     */
    public void checkAndCloseRegistrations() {
        fireBaseManager.getAllGardens(gardens -> {
            if (gardens != null) {
                for (Garden garden : gardens) {
                    if (garden.isRegistered()) {
                        Date registrationDate = garden.getRegistrationStartDate();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(registrationDate);
                        cal.add(Calendar.DAY_OF_YEAR, 3);

                        if (new Date().after(cal.getTime())) {
                            fireBaseManager.updateGardenRegistrationStatusByName(garden.getName(), false, success -> {
                                if (success) {
                                    Log.d("Firebase", "Registration closed for garden: " + garden.getName());
                                } else {
                                    Log.d("Firebase", "Failed to close registration for garden: " + garden.getName());
                                }
                            });
                        }
                    }
                }
            }
        });
    }
}
