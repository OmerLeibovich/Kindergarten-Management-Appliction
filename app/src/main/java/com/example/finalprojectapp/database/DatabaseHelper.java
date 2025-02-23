package com.example.finalprojectapp.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Objects.Garden;
import Objects.GardenClass;

/**
 * DatabaseHelper class handles all the database operations related to users, kindergartens, and classes.
 * It extends SQLiteOpenHelper to manage the creation and version management of the database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Information
    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_KINDERGARTENS = "kindergartens";
    private static final String TABLE_CLASSES = "classes";

    // Common Column Names
    private static final String COLUMN_ID = "id";

    // Users Table - Column Names
    private static final String COLUMN_UID = "uid";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // Kindergartens Table - Column Names
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_CITY = "city";
    private static final String COLUMN_PHONE_NUMBER = "phone_number";
    private static final String COLUMN_OPEN_TIME = "open_time";
    private static final String COLUMN_CLOSE_TIME = "close_time";
    private static final String COLUMN_ORG_AFFILIATION = "organizational_affiliation";
    private static final String COLUMN_IMAGE_URL = "image_url";

    // Classes Table - Column Names
    private static final String COLUMN_COURSE_NUMBER = "courseNumber";
    private static final String COLUMN_COURSE_TYPE = "courseType";
    private static final String COLUMN_MAX_CHILDREN = "maxChildren";
    private static final String COLUMN_MIN_AGE = "minAge";
    private static final String COLUMN_MAX_AGE = "maxAge";



    /**
     * SQL statement to create the users table.
     * The table contains the following columns:
     * - id: Auto-incremented primary key.
     * - uid: Unique identifier for the user.
     * - name: The user's name.
     * - email: The user's email address.
     * - password: The user's password.
     */
    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_UID + " TEXT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_EMAIL + " TEXT, " +
                    COLUMN_PASSWORD + " TEXT);";

    /**
     * SQL statement to create the kindergartens table.
     * The table contains the following columns:
     * - id: Auto-incremented primary key.
     * - name: The name of the kindergarten.
     * - address: The address of the kindergarten.
     * - city: The city where the kindergarten is located.
     * - phone_number: The contact phone number for the kindergarten.
     * - open_time: The opening time of the kindergarten.
     * - close_time: The closing time of the kindergarten.
     * - organizational_affiliation: The organizational affiliation of the kindergarten.
     * - image_url: The URL of the image representing the kindergarten.
     */
    private static final String TABLE_CREATE_KINDERGARTENS =
            "CREATE TABLE " + TABLE_KINDERGARTENS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_ADDRESS + " TEXT, " +
                    COLUMN_CITY + " TEXT, " +
                    COLUMN_PHONE_NUMBER + " TEXT, " +
                    COLUMN_OPEN_TIME + " TEXT, " +
                    COLUMN_CLOSE_TIME + " TEXT, " +
                    COLUMN_ORG_AFFILIATION + " TEXT, " +
                    COLUMN_IMAGE_URL + " TEXT);";

    /**
     * SQL statement to create the classes table.
     * The table contains the following columns:
     * - id: Auto-incremented primary key.
     * - courseNumber: The unique number of the course.
     * - courseType: The type or category of the course.
     * - maxChildren: The maximum number of children allowed in the class.
     * - minAge: The minimum age required to join the class.
     * - maxAge: The maximum age allowed in the class.
     */
    private static final String TABLE_CREATE_CLASSES =
            "CREATE TABLE " + TABLE_CLASSES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_COURSE_NUMBER + " TEXT, " +
                    COLUMN_COURSE_TYPE + " TEXT, " +
                    COLUMN_MAX_CHILDREN + " INTEGER, " +
                    COLUMN_MIN_AGE + " INTEGER, " +
                    COLUMN_MAX_AGE + " INTEGER);";


    /**
     * Constructor for DatabaseHelper.
     *
     * @param context The context in which the database is created.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * This is where the creation of tables and the initial population of the tables should happen.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_USERS);
        db.execSQL(TABLE_CREATE_KINDERGARTENS);
        db.execSQL(TABLE_CREATE_CLASSES);
    }

    /**
     * Called when the database needs to be upgraded.
     * This method will drop the existing tables and create them again.
     *
     * @param db         The SQLiteDatabase object.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_KINDERGARTENS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASSES);
        onCreate(db);
    }

    /**
     * Adds a new user to the database.
     *
     * @param uid      The unique user ID.
     * @param name     The name of the user.
     * @param email    The email of the user.
     * @param password The password of the user.
     */
    public void addUser(String uid, String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UID, uid);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    /**
     * Retrieves a user from the database based on the email.
     *
     * @param email The email of the user to be retrieved.
     * @return Cursor pointing to the retrieved user data.
     */
    public Cursor getUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_UID, COLUMN_NAME, COLUMN_EMAIL, COLUMN_PASSWORD};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        return db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
    }

    /**
     * Deletes a kindergarten from the database.
     *
     * @param gartenId The ID of the kindergarten to be deleted.
     */
    public void deleteKinderGarten(String gartenId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_KINDERGARTENS, "id=?", new String[]{gartenId});
        db.close();
    }

    /**
     * Removes a kindergarten from the director's list of kindergartens.
     *
     * @param directorUid The unique ID of the director.
     * @param gartenId    The ID of the kindergarten to be removed.
     */
    public void removeGardenFromDirector(String directorUid, String gartenId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Retrieve the current list of kindergartens
        Cursor cursor = db.query("directors", new String[]{"kindergartens"}, "uid=?", new String[]{directorUid}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String existingGardens = cursor.getString(cursor.getColumnIndex("kindergartens"));
            cursor.close();

            // Update the list by removing the deleted kindergarten
            if (existingGardens != null && !existingGardens.isEmpty()) {
                List<String> gardenList = new ArrayList<>(Arrays.asList(existingGardens.split(",")));
                gardenList.remove(gartenId);
                String updatedGardens = TextUtils.join(",", gardenList);

                ContentValues values = new ContentValues();
                values.put("kindergartens", updatedGardens);
                db.update("directors", values, "uid=?", new String[]{directorUid});
            }
        }

        db.close();
    }

    /**
     * Deletes a class from a specific kindergarten.
     *
     * @param gartenId     The ID of the kindergarten.
     * @param courseNumber The number of the course to be deleted.
     */
    public void deleteClassFromGarten(String gartenId, String courseNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CLASSES, COLUMN_COURSE_NUMBER + "=? AND gartenId=?", new String[]{courseNumber, gartenId});
        db.close();
    }

    /**
     * Adds a new kindergarten to the database.
     *
     * @param garden The Garden object containing the details of the kindergarten.
     */
    public void addKinderGarten(Garden garden) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, garden.getName());
        values.put(COLUMN_ADDRESS, garden.getAddress());
        values.put(COLUMN_CITY, garden.getCity());
        values.put(COLUMN_PHONE_NUMBER, garden.getPhoneNumber());
        values.put(COLUMN_OPEN_TIME, garden.getOpenTime());
        values.put(COLUMN_CLOSE_TIME, garden.getCloseTime());
        values.put(COLUMN_ORG_AFFILIATION, garden.getOrganizationalAffiliation());
        values.put(COLUMN_IMAGE_URL, garden.getImageUrl());
        db.insert(TABLE_KINDERGARTENS, null, values);
        db.close();
    }

    /**
     * Adds a new class to the database.
     *
     * @param courseNumber The course number of the class.
     * @param courseType   The type of the course.
     * @param maxChildren  The maximum number of children allowed in the class.
     * @param minAge       The minimum age required for the class.
     * @param maxAge       The maximum age allowed for the class.
     */
    public void addKinderGartenClass(String courseNumber, String courseType, int maxChildren, int minAge, int maxAge) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COURSE_NUMBER, courseNumber);
        values.put(COLUMN_COURSE_TYPE, courseType);
        values.put(COLUMN_MAX_CHILDREN, maxChildren);
        values.put(COLUMN_MIN_AGE, minAge);
        values.put(COLUMN_MAX_AGE, maxAge);
        db.insert(TABLE_CLASSES, null, values);
        db.close();
    }
}
