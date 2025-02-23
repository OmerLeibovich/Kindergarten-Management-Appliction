package com.example.finalprojectapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * UserSessionManager is a class for managing and storing the logged-in user's session details
 * in the application using SharedPreferences. The class allows saving the login status,
 * user's email address, and user's role.
 */
public class UserSessionManager {

    // Private variables for internal use
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    // SharedPreferences file name and keys
    private static final String PREF_NAME = "UserSession"; // SharedPreferences file name
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn"; // Key representing the user's login status
    private static final String KEY_USER_EMAIL = "userEmail"; // Key representing the user's email address
    private static final String KEY_USER_ROLE = "userRole"; // Key representing the user's role

    /**
     * Constructor for the class. Initializes SharedPreferences and its editor.
     *
     * @param context the application context
     */
    public UserSessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Creates a new login session and saves the user's email address and role.
     *
     * @param email the user's email address
     * @param role the user's role
     */
    public void createLoginSession(String email, String role) {
        Log.d("UserSessionManager", "Creating session for email: " + email + ", role: " + role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true); // Save login status
        editor.putString(KEY_USER_EMAIL, email); // Save email address
        editor.putString(KEY_USER_ROLE, role); // Save user role
        editor.commit(); // Commit the changes immediately
        Log.d("UserSessionManager", "Session created successfully");
    }

    /**
     * Checks if the user is logged into the application.
     *
     * @return true if the user is logged in, otherwise false
     */
    public boolean isLoggedIn() {
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        Log.d("UserSessionManager", "isLoggedIn: " + isLoggedIn);
        return isLoggedIn;
    }

    /**
     * Returns the email address of the logged-in user.
     *
     * @return the user's email address, or null if not saved
     */
    public String getUserEmail() {
        String email = sharedPreferences.getString(KEY_USER_EMAIL, null);
        Log.d("UserSessionManager", "getUserEmail: " + email);
        return email;
    }

    /**
     * Returns the role of the logged-in user.
     *
     * @return the user's role, or null if not saved
     */
    public String getUserRole() {
        String role = sharedPreferences.getString(KEY_USER_ROLE, null);
        Log.d("UserSessionManager", "getUserRole: " + role);
        return role;
    }

    /**
     * Logs out the user from the application and clears all session details.
     */
    public void logoutUser() {
        Log.d("UserSessionManager", "Logging out user");
        editor.clear(); // Clear all saved data
        editor.commit(); // Commit the changes
        Log.d("UserSessionManager", "User logged out successfully");
    }
}
