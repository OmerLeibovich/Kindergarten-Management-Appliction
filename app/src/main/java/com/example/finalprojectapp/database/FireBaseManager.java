package com.example.finalprojectapp.database;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.FieldValue;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Objects.Child;
import Objects.ChildPhoto;
import Objects.ChildStatus;
import Objects.Garden;
import Objects.GardenClass;
import Objects.GardenStaff;
import Objects.Note;
import Objects.Parent;
import Objects.Review;

/**
 * This class manages all interactions with Firebase Firestore and Firebase Storage for the application.
 */
public class FireBaseManager {

    private static FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseHelper databaseHelper;
    private Context context;
    private Map<String, String> gardenIdToNameMap;

    /**
     * Constructor for initializing the FireBaseManager.
     *
     * @param context Application context.
     */
    public FireBaseManager(Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("kindergarten_images");
        databaseHelper = new DatabaseHelper(context);
        gardenIdToNameMap = new HashMap<>();
        this.storage = FirebaseStorage.getInstance();
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
    }

    /**
     * Displays a snackbar with the given message.
     *
     * @param view    The view to attach the snackbar to.
     * @param message The message to display.
     */
    private void showSnackbar(View view, String message) {
        if (view != null && view.getParent() != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves a user's data to Firestore after registration.
     *
     * @param user  The FirebaseUser object.
     * @param name  The user's name.
     * @param email The user's email.
     * @param view  The view to attach the snackbar to.
     */
    private void saveUserToFirestore(FirebaseUser user, String name, String email, View view) {
        Map<String, String> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("name", name);
        userData.put("email", email);

        db.collection("users")
                .add(userData)
                .addOnSuccessListener(documentReference -> showSnackbar(view, "Registration successful"))
                .addOnFailureListener(e -> showSnackbar(view, "Failed to save user data"));
    }

    /**
     * Adds a new kindergarten class to a specific kindergarten.
     *
     * @param gartenId         The ID of the kindergarten.
     * @param kinderGardenClass The GardenClass object containing class details.
     * @param view             The view to attach the snackbar to.
     * @param callback         A callback to handle the result.
     */
    public void addKinderGartenClass(String gartenId, GardenClass kinderGardenClass, View view, GartenIdCallback callback) {
        DocumentReference docRef = db.collection("kindergartens").document(gartenId);

        // Generate a unique ID for the new class
        String classId = db.collection("kindergartens").document().getId();
        kinderGardenClass.setId(classId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Garden garden = document.toObject(Garden.class);
                    List<GardenClass> classes = garden.getClasses();
                    classes.add(kinderGardenClass);
                    docRef.update("classes", classes)
                            .addOnSuccessListener(aVoid -> {
                                // Save data to SQLite as well
                                databaseHelper.addKinderGartenClass(
                                        kinderGardenClass.getCourseNumber(),
                                        kinderGardenClass.getCourseType(),
                                        kinderGardenClass.getMaxChildren(),
                                        kinderGardenClass.getMinAge(),
                                        kinderGardenClass.getMaxAge()
                                );
                                showSnackbar(view, "Class added successfully");
                                callback.onCallback(gartenId);
                            })
                            .addOnFailureListener(e -> {
                                showSnackbar(view, "Failed to add class");
                                callback.onCallback(null);
                            });
                } else {
                    showSnackbar(view, "Garden not found");
                }
            } else {
                showSnackbar(view, "Failed to get garden");
            }
        });
    }

    /**
     * Populates a spinner with kindergarten names associated with the current director.
     *
     * @param gartenSpinner The spinner to populate.
     */
    public void populateGartenSpinner(Spinner gartenSpinner) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("directors").document(user.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> gartenIds = (List<String>) document.get("kindergartens");
                        if (gartenIds != null && !gartenIds.isEmpty()) {
                            loadGartenNames(gartenIds, gartenSpinner);
                        } else {
                            showSnackbar(gartenSpinner, "No gardens found for this director");
                        }
                    } else {
                        showSnackbar(gartenSpinner, "No gardens found for this director");
                    }
                } else {
                    showSnackbar(gartenSpinner, "Failed to retrieve gardens");
                }
            });
        }
    }

    /**
     * Loads kindergarten names into a spinner.
     *
     * @param gartenIds     The list of kindergarten IDs.
     * @param gartenSpinner The spinner to populate.
     */
    private void loadGartenNames(List<String> gartenIds, Spinner gartenSpinner) {
        List<String> gartenNames = new ArrayList<>();
        for (String gartenId : gartenIds) {
            db.collection("kindergartens").document(gartenId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String gartenName = document.getString("name");
                        gardenIdToNameMap.put(gartenId, gartenName);
                        gartenNames.add(gartenName);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, gartenNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        gartenSpinner.setAdapter(adapter);
                    } else {
                        showSnackbar(gartenSpinner, "Garden not found");
                    }
                } else {
                    showSnackbar(gartenSpinner, "Failed to load garden names");
                }
            });
        }
    }

    /**
     * Loads all kindergartens associated with the current director and updates the provided garden list and adapter.
     *
     * @param gardenList   The list of kindergartens to be updated.
     * @param gardenAdapter The adapter for the RecyclerView displaying the kindergartens.
     * @param view         The view to attach the snackbar to.
     */
    public void loadGardens(List<Garden> gardenList, RecyclerView.Adapter gardenAdapter, View view) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("directors").document(user.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> gardenIds = (List<String>) document.get("kindergartens");
                        if (gardenIds != null && !gardenIds.isEmpty()) {
                            gardenList.clear(); // Clear the list before adding new items
                            List<Garden> newGardens = new ArrayList<>();
                            for (String gardenId : gardenIds) {
                                db.collection("kindergartens").document(gardenId).get().addOnCompleteListener(gardenTask -> {
                                    if (gardenTask.isSuccessful()) {
                                        DocumentSnapshot gardenDocument = gardenTask.getResult();
                                        if (gardenDocument.exists()) {
                                            Garden garden = gardenDocument.toObject(Garden.class);
                                            garden.setId(gardenDocument.getId()); // Set the document ID
                                            newGardens.add(garden);
                                        }
                                        if (newGardens.size() == gardenIds.size()) {
                                            gardenList.clear();
                                            gardenList.addAll(newGardens);
                                            if (gardenAdapter != null) {
                                                gardenAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    } else {
                                        showSnackbar(view, "Failed to load garden");
                                    }
                                });
                            }
                        } else {
                            showSnackbar(view, "No gardens found for this director");
                        }
                    } else {
                        showSnackbar(view, "Director not found");
                    }
                } else {
                    showSnackbar(view, "Failed to retrieve director");
                }
            });
        }
    }

    /**
     * Loads all classes for a specific kindergarten and updates the provided class list and adapter.
     *
     * @param gartenId    The ID of the kindergarten.
     * @param classList   The list of classes to be updated.
     * @param classAdapter The adapter for the RecyclerView displaying the classes.
     * @param view        The view to attach the snackbar to.
     */
    public void loadClassesForGarten(String gartenId, List<GardenClass> classList, RecyclerView.Adapter classAdapter, View view) {
        db.collection("kindergartens").document(gartenId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Garden garden = document.toObject(Garden.class);
                    if (garden != null && garden.getClasses() != null) {
                        classList.clear();
                        classList.addAll(garden.getClasses());
                        classAdapter.notifyDataSetChanged();
                    }
                } else {
                    showSnackbar(view, "Garden not found");
                }
            } else {
                showSnackbar(view, "Failed to load classes");
            }
        });
    }

    /**
     * Adds a new kindergarten to the Firestore database and SQLite, then updates the director's list of kindergartens.
     *
     * @param garden   The Garden object to be added.
     * @param callback A callback to handle the result.
     * @param view     The view to attach the snackbar to.
     */
    public void addKinderGarten(Garden garden, GartenIdCallback callback, View view) {
        db.collection("kindergartens")
                .whereEqualTo("name", garden.getName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // If a kindergarten with the same name already exists, show a message and do not add the kindergarten
                        showSnackbar(view, "A kindergarten with this name already exists.");
                        callback.onCallback(null);
                    } else {
                        // If the name does not exist, add the kindergarten
                        db.collection("kindergartens")
                                .add(garden)
                                .addOnSuccessListener(documentReference -> {
                                    garden.setId(documentReference.getId());

                                    // Also update SQLite
                                    databaseHelper.addKinderGarten(garden);

                                    updateDirectorGardens(garden.getId(), callback, view);
                                })
                                .addOnFailureListener(e -> {
                                    showSnackbar(view, "Failed to add KinderGarten");
                                    callback.onCallback(null);
                                });
                    }
                });
    }

    /**
     * Updates the director's list of kindergartens in Firestore.
     *
     * @param gartenId The ID of the kindergarten to be added.
     * @param callback A callback to handle the result.
     * @param view     The view to attach the snackbar to.
     */
    private void updateDirectorGardens(String gartenId, GartenIdCallback callback, View view) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DocumentReference directorRef = db.collection("directors").document(user.getUid());
            directorRef.update("kindergartens", FieldValue.arrayUnion(gartenId))
                    .addOnSuccessListener(aVoid -> {
                        showSnackbar(view, "Garden added to director successfully");
                        callback.onCallback(gartenId);
                    })
                    .addOnFailureListener(e -> {
                        showSnackbar(view, "Failed to add garden to director");
                        callback.onCallback(null);
                    });
        }
    }

    /**
     * Retrieves the ID of a kindergarten by its name.
     *
     * @param gartenName The name of the kindergarten.
     * @param callback   A callback to handle the result.
     */
    public void getGartenIdByName(String gartenName, GartenIdCallback callback) {
        db.collection("kindergartens")
                .whereEqualTo("name", gartenName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String gartenId = document.getId();
                            callback.onCallback(gartenId);
                            return;
                        }
                        callback.onCallback(null); // No document found
                    } else {
                        callback.onCallback(null);
                    }
                });
    }

    /**
     * Deletes a kindergarten from Firestore and SQLite, and updates related records.
     *
     * @param gartenId The ID of the kindergarten to be deleted.
     * @param view     The view to attach the snackbar to.
     * @param callback A callback to handle the result.
     */
    public void deleteKinderGarten(String gartenId, View view, GartenIdCallback callback) {
        db.collection("kindergartens").document(gartenId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String gartenName = task.getResult().getString("name");

                        db.collection("kindergartens").document(gartenId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    databaseHelper.deleteKinderGarten(gartenId); // Delete kindergarten from SQLite

                                    removeGardenFromDirector(gartenId, view, success -> {
                                        if (success != null) {
                                            updateStaffGardenField(gartenName, view, success1 -> {
                                                if (success1) {
                                                    callback.onCallback(gartenId);
                                                } else {
                                                    showSnackbar(view, "Failed to update staff members");
                                                    callback.onCallback(null);
                                                }
                                            });
                                        } else {
                                            showSnackbar(view, "Failed to remove garden from director");
                                            callback.onCallback(null);
                                        }
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    showSnackbar(view, "Failed to delete KinderGarten");
                                    callback.onCallback(null);
                                });
                    } else {
                        showSnackbar(view, "Failed to retrieve garden name");
                        callback.onCallback(null);
                    }
                });
    }

    /**
     * Removes a kindergarten from the director's list in Firestore and updates the local SQLite database.
     *
     * @param gartenId The ID of the kindergarten to be removed.
     * @param view     The view to attach the snackbar to.
     * @param callback A callback to handle the result.
     */
    private void removeGardenFromDirector(String gartenId, View view, GartenIdCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DocumentReference directorRef = db.collection("directors").document(user.getUid());
            directorRef.update("kindergartens", FieldValue.arrayRemove(gartenId))
                    .addOnSuccessListener(aVoid -> {
                        showSnackbar(view, "Garden removed from director successfully");

                        // Update the local SQLite database
                        databaseHelper.removeGardenFromDirector(user.getUid(), gartenId);

                        callback.onCallback(gartenId);
                    })
                    .addOnFailureListener(e -> {
                        showSnackbar(view, "Failed to remove garden from director");
                        callback.onCallback(null);
                    });
        }
    }

    /**
     * Updates a kindergarten in Firestore and shows a snackbar upon success or failure.
     *
     * @param garden   The Garden object containing updated information.
     * @param callback A callback to handle the result.
     * @param view     The view to attach the snackbar to.
     */
    public void updateKinderGarten(@NonNull Garden garden, GartenIdCallback callback, View view) {
        db.collection("kindergartens").document(garden.getId())
                .set(garden)
                .addOnSuccessListener(aVoid -> {
                    showSnackbar(view, "Garden updated successfully");
                    callback.onCallback(garden.getId());
                })
                .addOnFailureListener(e -> {
                    showSnackbar(view, "Failed to update garden");
                    e.printStackTrace();
                    callback.onCallback(null);
                });
    }

    /**
     * Deletes a specific class from a kindergarten in Firestore and updates the local SQLite database.
     *
     * @param gartenId    The ID of the kindergarten.
     * @param gardenClass The GardenClass object representing the class to be deleted.
     * @param callback    A callback to handle the result.
     */
    public void deleteClassFromGarten(String gartenId, GardenClass gardenClass, GartenIdCallback callback) {
        DocumentReference docRef = db.collection("kindergartens").document(gartenId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Garden garden = document.toObject(Garden.class);
                    List<GardenClass> classes = garden.getClasses();
                    boolean classRemoved = classes.removeIf(c -> c.getCourseNumber().equals(gardenClass.getCourseNumber()));

                    if (classRemoved) {
                        docRef.update("classes", classes)
                                .addOnSuccessListener(aVoid -> {
                                    // Delete the class from SQLite
                                    databaseHelper.deleteClassFromGarten(gartenId, gardenClass.getCourseNumber());

                                    callback.onCallback(gartenId);
                                })
                                .addOnFailureListener(e -> callback.onCallback(null));
                    } else {
                        callback.onCallback(null); // If the class was not found
                    }
                } else {
                    callback.onCallback(null); // If the document was not found
                }
            } else {
                callback.onCallback(null); // If the operation failed
            }
        });
    }

    /**
     * Updates a specific class in a kindergarten in Firestore.
     *
     * @param gartenId    The ID of the kindergarten.
     * @param gardenClass The GardenClass object containing updated information.
     * @param view        The view to attach the snackbar to.
     * @param callback    A callback to handle the result.
     */
    public void updateClassInGarten(String gartenId, GardenClass gardenClass, View view, GartenIdCallback callback) {
        DocumentReference docRef = db.collection("kindergartens").document(gartenId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Garden garden = document.toObject(Garden.class);
                    List<GardenClass> classes = garden.getClasses();
                    boolean classFound = false;
                    for (int i = 0; i < classes.size(); i++) {
                        if (classes.get(i).getId().equals(gardenClass.getId())) {
                            classes.set(i, gardenClass);
                            classFound = true;
                            break;
                        }
                    }
                    if (classFound) {
                        docRef.update("classes", classes)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firebase", "Class updated successfully");
                                    callback.onCallback(gartenId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firebase", "Failed to update class in Firebase", e);
                                    callback.onCallback(null);
                                });
                    } else {
                        Log.e("Firebase", "Class not found in the list: " + gardenClass.getId());
                        Snackbar.make(view, "Class not found", Snackbar.LENGTH_SHORT).show();
                        callback.onCallback(null);
                    }
                } else {
                    Log.e("Firebase", "Garden document not found for ID: " + gartenId);
                    callback.onCallback(null);
                }
            } else {
                Log.e("Firebase", "Failed to get garden document", task.getException());
                callback.onCallback(null);
            }
        });
    }

    /**
     * Checks if a course number exists in a specific kindergarten's list of classes.
     *
     * @param gartenId    The ID of the kindergarten.
     * @param courseNumber The course number to check.
     * @param callback    A callback to handle the result.
     */
    public void isCourseNumberExists(String gartenId, String courseNumber, GartenIdCallback callback) {
        db.collection("kindergartens").document(gartenId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Garden garden = document.toObject(Garden.class);
                            List<GardenClass> classes = garden.getClasses();
                            for (GardenClass gardenClass : classes) {
                                if (gardenClass.getCourseNumber().equals(courseNumber)) {
                                    callback.onCallback(gartenId);
                                    return;
                                }
                            }
                            callback.onCallback(null);
                        } else {
                            callback.onCallback(null);
                        }
                    } else {
                        callback.onCallback(null);
                    }
                });
    }

    /**
     * Retrieves all staff members without a garden assigned and returns them via the callback.
     *
     * @param callback A callback to handle the list of staff members.
     */
    public void getStaffWithoutGarten(StaffListCallback callback) {
        db.collection("staff")
                .whereEqualTo("garten", null)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        List<GardenStaff> staffWithoutGarten = new ArrayList<>();
                        for (DocumentSnapshot document : documents) {
                            GardenStaff staff = document.toObject(GardenStaff.class);
                            staffWithoutGarten.add(staff);
                            Log.d("Firebase", "Staff: " + staff.getName()); // Log data
                        }
                        callback.onCallback(staffWithoutGarten);
                    } else {
                        Log.e("Firebase", "Failed to get staff without garden", task.getException());
                        callback.onCallback(null);
                    }
                });
    }



    /**
     * Updates a specific staff member's information in Firestore by their email.
     *
     * @param staff    The GardenStaff object containing updated information.
     * @param callback A callback to handle the result.
     */
    public void updateKinderGartenStaff(@NonNull GardenStaff staff, GartenIdCallback callback) {
        db.collection("staff")
                .whereEqualTo("email", staff.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String documentId = document.getId();
                        db.collection("staff").document(documentId)
                                .set(staff)
                                .addOnSuccessListener(aVoid -> {
                                    callback.onCallback(documentId);
                                })
                                .addOnFailureListener(e -> {
                                    callback.onCallback(null);
                                });
                    } else {
                        callback.onCallback(null);
                    }
                });
    }

    /**
     * Retrieves a specific kindergarten by its ID from Firestore.
     *
     * @param gartenId The ID of the kindergarten to retrieve.
     * @param callback A callback to handle the result.
     */
    public void getGartenById(String gartenId, GartenCallback callback) {
        db.collection("kindergartens").document(gartenId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Garden garden = task.getResult().toObject(Garden.class);
                callback.onCallback(garden);
            } else {
                callback.onCallback(null);
            }
        });
    }

    /**
     * Retrieves all staff members assigned to the kindergartens managed by the current director.
     *
     * @param callback A callback to handle the list of staff members.
     */
    public void getStaffInGarten(StaffListCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("directors").document(user.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> gartenIds = (List<String>) document.get("kindergartens");
                        if (gartenIds != null && !gartenIds.isEmpty()) {
                            List<GardenStaff> allStaff = new ArrayList<>();
                            CountDownLatch latch = new CountDownLatch(gartenIds.size());
                            for (String gartenId : gartenIds) {
                                db.collection("kindergartens").document(gartenId).get().addOnCompleteListener(gartenTask -> {
                                    if (gartenTask.isSuccessful()) {
                                        DocumentSnapshot gartenDoc = gartenTask.getResult();
                                        if (gartenDoc.exists()) {
                                            String gartenName = gartenDoc.getString("name");
                                            fetchStaffByGartenName(gartenName, allStaff, latch, callback);
                                        } else {
                                            latch.countDown();
                                        }
                                    } else {
                                        latch.countDown();
                                    }
                                });
                            }
                        } else {
                            callback.onCallback(null);
                        }
                    } else {
                        callback.onCallback(null);
                    }
                } else {
                    callback.onCallback(null);
                }
            });
        }
    }

    /**
     * Fetches staff members by the name of the kindergarten they are associated with.
     *
     * @param gartenName The name of the kindergarten.
     * @param allStaff   A list to accumulate the staff members.
     * @param latch      A CountDownLatch to synchronize the callback.
     * @param callback   A callback to handle the final list of staff members.
     */
    private void fetchStaffByGartenName(String gartenName, List<GardenStaff> allStaff, CountDownLatch latch, StaffListCallback callback) {
        db.collection("staff")
                .whereEqualTo("garten.name", gartenName)
                .get()
                .addOnCompleteListener(staffTask -> {
                    if (staffTask.isSuccessful()) {
                        List<DocumentSnapshot> documents = staffTask.getResult().getDocuments();
                        for (DocumentSnapshot doc : documents) {
                            GardenStaff staff = doc.toObject(GardenStaff.class);
                            allStaff.add(staff);
                        }
                    }
                    latch.countDown();
                    if (latch.getCount() == 0) {
                        callback.onCallback(allStaff);
                    }
                });
    }

    /**
     * Retrieves all kindergartens managed by the current director.
     *
     * @param callback A callback to handle the list of kindergartens.
     */
    public void getDirectorGardens(GartenListCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("directors").document(user.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> gartenIds = (List<String>) document.get("kindergartens");
                        if (gartenIds != null && !gartenIds.isEmpty()) {
                            List<Garden> gardens = new ArrayList<>();
                            for (String gartenId : gartenIds) {
                                db.collection("kindergartens").document(gartenId).get().addOnCompleteListener(gartenTask -> {
                                    if (gartenTask.isSuccessful()) {
                                        DocumentSnapshot gartenDocument = gartenTask.getResult();
                                        if (gartenDocument.exists()) {
                                            Garden garden = gartenDocument.toObject(Garden.class);
                                            garden.setId(gartenDocument.getId()); // Set the document ID
                                            gardens.add(garden);
                                        }
                                        if (gardens.size() == gartenIds.size()) {
                                            callback.onCallback(gardens);
                                        }
                                    }
                                });
                            }
                        } else {
                            callback.onCallback(null);
                        }
                    } else {
                        callback.onCallback(null);
                    }
                } else {
                    callback.onCallback(null);
                }
            });
        } else {
            callback.onCallback(null);
        }
    }

    /**
     * Adds a course to a staff member's list of courses in Firestore.
     *
     * @param staff    The GardenStaff object representing the staff member.
     * @param course   The GardenClass object representing the course to be added.
     * @param callback A callback to handle the result.
     */
    public void addCourseToStaff(GardenStaff staff, GardenClass course, GartenIdCallback callback) {
        db.collection("staff")
                .whereEqualTo("email", staff.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String documentId = document.getId();
                        List<GardenClass> courses = staff.getClasses();
                        if (courses == null) {
                            courses = new ArrayList<>();
                        }
                        courses.add(course);
                        staff.setClasses(courses);
                        db.collection("staff").document(documentId)
                                .set(staff)
                                .addOnSuccessListener(aVoid -> {
                                    callback.onCallback(documentId);
                                })
                                .addOnFailureListener(e -> {
                                    callback.onCallback(null);
                                });
                    } else {
                        callback.onCallback(null);
                    }
                });
    }

    /**
     * Retrieves the list of classes for a specific kindergarten by its ID.
     *
     * @param gartenId The ID of the kindergarten.
     * @param callback A callback to handle the list of classes.
     */
    public void getClassesForGarten(String gartenId, CourseListCallback callback) {
        db.collection("kindergartens").document(gartenId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Garden garden = document.toObject(Garden.class);
                    if (garden != null && garden.getClasses() != null) {
                        callback.onCallback(garden.getClasses());
                    } else {
                        callback.onCallback(new ArrayList<>()); // Return empty list if no classes found
                    }
                } else {
                    callback.onCallback(null); // Document not found
                }
            } else {
                callback.onCallback(null); // Task not successful
            }
        });
    }

    /**
     * Searches for kindergartens based on city, organization, and age.
     *
     * @param city         The city where the kindergarten is located.
     * @param organization The organization affiliation of the kindergarten.
     * @param age          The age to filter by, within the range of the kindergarten's age limits.
     * @param callback     A callback to handle the list of matching kindergartens.
     */
    public void searchGardens(String city, String organization, String age, FireBaseManager.GartenListCallback callback) {
        CollectionReference gardensRef = db.collection("kindergartens");

        Query query = gardensRef.whereEqualTo("city", city);

        // If an organization is specified, add it to the query
        if (organization != null && !organization.isEmpty()) {
            query = query.whereEqualTo("organizationalAffiliation", organization);
        }

        // If an age is specified, add it to the query
        if (age != null && !age.isEmpty()) {
            int ageInt = Integer.parseInt(age);
            query = query.whereLessThanOrEqualTo("minAge", ageInt)
                    .whereGreaterThanOrEqualTo("maxAge", ageInt);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Garden> gardenList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Garden garden = document.toObject(Garden.class);
                    gardenList.add(garden);
                }
                callback.onCallback(gardenList);
            } else {
                callback.onCallback(null); // Return null in case of failure
            }
        });
    }

    /**
     * Determines the type of the current user (parent, director, or staff) based on their Firestore records.
     *
     * @param callback A callback to handle the user type as a string ("parent", "director", "staff"), or null if not found.
     */
    public void getUserType(UserTypeCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            // Check if user is a parent
            db.collection("Parents").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    callback.onCallback("parent");
                } else {
                    // Check if user is a director
                    db.collection("directors").document(userId).get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful() && task1.getResult().exists()) {
                            callback.onCallback("director");
                        } else {
                            // Check if user is a staff member
                            db.collection("staff").document(userId).get().addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful() && task2.getResult().exists()) {
                                    callback.onCallback("staff");
                                } else {
                                    callback.onCallback(null);
                                }
                            });
                        }
                    });
                }
            });
        } else {
            callback.onCallback(null);
        }
    }

    /**
     * Updates the "garten" field for all staff members associated with a specific kindergarten.
     *
     * @param gartenName The name of the kindergarten.
     * @param view       The view for displaying messages.
     * @param callback   A callback to handle the update result.
     */
    private void updateStaffGardenField(String gartenName, View view, UpdateCallback callback) {
        db.collection("staff")
                .whereEqualTo("garten.name", gartenName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        List<String> staffIds = new ArrayList<>();
                        for (DocumentSnapshot document : documents) {
                            staffIds.add(document.getId());
                        }
                        updateStaffRecords(staffIds, callback);
                    } else {
                        showSnackbar(view, "Failed to retrieve staff members");
                        callback.onCallback(false);
                    }
                });
    }

    /**
     * Updates the "garten" field for a list of staff members in Firestore.
     *
     * @param staffIds The list of staff member document IDs.
     * @param callback A callback to handle the update result.
     */
    private void updateStaffRecords(List<String> staffIds, UpdateCallback callback) {
        for (String staffId : staffIds) {
            DocumentReference staffRef = db.collection("staff").document(staffId);
            staffRef.update("garten", null)
                    .addOnSuccessListener(aVoid -> {
                        // Check if all staff members have been updated
                        if (staffIds.indexOf(staffId) == staffIds.size() - 1) {
                            callback.onCallback(true);
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.onCallback(false);
                    });
        }
    }

    /**
     * Retrieves all kindergartens from Firestore.
     *
     * @param callback A callback to handle the list of kindergartens.
     */
    public void getAllGardens(GartenListCallback callback) {
        db.collection("kindergartens")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Garden> gardenList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Garden garden = document.toObject(Garden.class);
                            gardenList.add(garden);
                        }
                        callback.onCallback(gardenList);
                    } else {
                        callback.onCallback(null);
                    }
                });
    }

    /**
     * Updates the registration status of a specific kindergarten by its name.
     *
     * @param gardenName The name of the kindergarten to update.
     * @param status     The new registration status (true or false).
     * @param callback   A callback to handle the result of the update.
     */
    public void updateGardenRegistrationStatusByName(String gardenName, boolean status, UpdateCallback callback) {
        db.collection("kindergartens")
                .whereEqualTo("name", gardenName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("registered", status);

                            if (status) {
                                updates.put("registrationStartDate", new Date()); // Update the registration start date and time
                            }

                            document.getReference().update(updates)
                                    .addOnSuccessListener(aVoid -> callback.onCallback(true))
                                    .addOnFailureListener(e -> callback.onCallback(false));
                        }
                    } else {
                        callback.onCallback(false);
                    }
                });
    }

    /**
     * Updates the registration status for all kindergartens in the collection.
     *
     * @param status   The new registration status (true or false) for all kindergartens.
     * @param callback A callback to handle the result of the update.
     */
    public void updateAllGardensRegistrationStatus(boolean status, UpdateCallback callback) {
        db.collection("kindergartens")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("registered", status);

                            if (status) {
                                updates.put("registrationStartDate", new Date()); // Update the registration start date and time for all kindergartens
                            }

                            document.getReference().update(updates);
                        }
                        callback.onCallback(true);
                    } else {
                        callback.onCallback(false);
                    }
                });
    }

    /**
     * Registers a child under a parent in the Firestore and updates the relevant kindergarten and classes.
     *
     * @param child     The child to be registered.
     * @param parentId  The ID of the parent under whom the child will be registered.
     * @param view      The view for displaying messages.
     * @param callback  A callback to handle the result of the registration.
     */
    public void registerChildWithParent(Child child, String parentId, View view, GartenIdCallback callback) {
        db.collection("Parents").document(parentId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Parent parent = task.getResult().toObject(Parent.class);

                if (parent != null) {
                    // Add the child to the parent's list of children
                    parent.addChild(child);

                    // Update the parent's document in Firestore
                    db.collection("Parents").document(parentId).set(parent)
                            .addOnSuccessListener(aVoid -> {
                                // Save the child object in a separate Firestore document
                                db.collection("Children").add(child)
                                        .addOnSuccessListener(documentReference -> {
                                            // Update the children's map in the kindergarten
                                            updateGardenAndClassesWithChild(child, view, callback);
                                        })
                                        .addOnFailureListener(e -> {
                                            showSnackbar(view, "Failed to register child");
                                            callback.onCallback(null);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                showSnackbar(view, "Failed to update parent details");
                                callback.onCallback(null);
                            });
                } else {
                    showSnackbar(view, "Failed to load parent details");
                    callback.onCallback(null);
                }
            } else {
                showSnackbar(view, "Failed to load parent details");
                callback.onCallback(null);
            }
        });
    }

    /**
     * Updates the relevant kindergarten and classes with the registered child.
     *
     * @param child     The child to be added to the kindergarten and classes.
     * @param view      The view for displaying messages.
     * @param callback  A callback to handle the result of the update.
     */
    private void updateGardenAndClassesWithChild(Child child, View view, GartenIdCallback callback) {
        // Retrieve kindergarten details by the child's kindergarten name
        db.collection("kindergartens")
                .whereEqualTo("name", child.getGartenName())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot gartenDoc = task.getResult().getDocuments().get(0);
                        Garden garden = gartenDoc.toObject(Garden.class);

                        if (garden != null && gartenDoc.getId() != null) {
                            // Add the child with approval status "false"
                            garden.getChildren().put(child.getID(), new ChildStatus(child, false));

                            // Update the kindergarten document in Firestore
                            db.collection("kindergartens").document(gartenDoc.getId()).set(garden)
                                    .addOnSuccessListener(aVoid -> {
                                        // Update the children's map in the selected classes
                                        updateClassesWithChild(gartenDoc.getId(), garden, child, view, callback);
                                    })
                                    .addOnFailureListener(e -> {
                                        showSnackbar(view, "Failed to update garden details");
                                        callback.onCallback(null);
                                    });
                        } else {
                            showSnackbar(view, "Failed to load garden details or garden ID is null");
                            callback.onCallback(null);
                        }
                    } else {
                        showSnackbar(view, "Failed to find garden with name: " + child.getGartenName());
                        callback.onCallback(null);
                    }
                });
    }

    /**
     * Updates the selected classes within a kindergarten with the registered child.
     *
     * @param gartenDocumentId The ID of the kindergarten document in Firestore.
     * @param garden           The kindergarten object to be updated.
     * @param child            The child to be added to the classes.
     * @param view             The view for displaying messages.
     * @param callback         A callback to handle the result of the update.
     */
    private void updateClassesWithChild(String gartenDocumentId, Garden garden, Child child, View view, GartenIdCallback callback) {
        List<String> selectedClasses = child.getHobbies();

        if (garden.getClasses() == null || garden.getClasses().isEmpty()) {
            showSnackbar(view, "No classes available in the garden");
            callback.onCallback(null);
            return;
        }

        for (String selectedClass : selectedClasses) {
            for (GardenClass gardenClass : garden.getClasses()) {
                if (gardenClass.getCourseNumber().equals(selectedClass)) {
                    // Ensure the children map is not null
                    if (gardenClass.getChildren() == null) {
                        gardenClass.setChildren(new HashMap<>());
                    }

                    // Add the child to the class's children map with approval status "false"
                    gardenClass.getChildren().put(child.getID(), new ChildStatus(child, false));

                    if (gartenDocumentId != null) {
                        // Update the kindergarten document in Firestore including the updated classes
                        db.collection("kindergartens").document(gartenDocumentId).set(garden)
                                .addOnFailureListener(e -> {
                                    showSnackbar(view, "Failed to update class details");
                                });
                    } else {
                        showSnackbar(view, "Failed to update class because Garden ID is null.");
                    }
                }
            }
        }

        showSnackbar(view, "Child registered successfully and added to garden and classes");
        callback.onCallback(child.getID());
    }



    /**
     * Retrieves the list of kindergartens associated with a parent's children.
     *
     * @param callback The callback to handle the list of kindergartens.
     */
    public void getGardensForParent(GartenListCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String parentId = user.getUid();

            db.collection("Parents").document(parentId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    Parent parent = task.getResult().toObject(Parent.class);

                    if (parent != null && parent.getChildren() != null) {
                        List<String> childIds = new ArrayList<>();
                        for (Child child : parent.getChildren()) {
                            childIds.add(child.getID());
                        }

                        db.collection("kindergartens").get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                List<Garden> gardens = new ArrayList<>();
                                for (DocumentSnapshot document : task1.getResult()) {
                                    Garden garden = document.toObject(Garden.class);
                                    if (garden != null && garden.getChildren() != null) {
                                        for (String childId : childIds) {
                                            if (garden.getChildren().containsKey(childId)) {
                                                gardens.add(garden);
                                                break; // No need to check other children for this garden
                                            }
                                        }
                                    }
                                }
                                callback.onCallback(gardens);
                            } else {
                                callback.onCallback(null);
                            }
                        });
                    } else {
                        callback.onCallback(null);
                    }
                } else {
                    callback.onCallback(null);
                }
            });
        }
    }

    /**
     * Retrieves the list of classes a child is enrolled in within a specific kindergarten.
     *
     * @param gartenId The ID of the kindergarten.
     * @param childId  The ID of the child.
     * @param callback The callback to handle the list of classes.
     */
    public void getChildClassesInGarden(String gartenId, String childId, CourseListCallback callback) {
        db.collection("kindergartens").document(gartenId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Garden garden = task.getResult().toObject(Garden.class);
                if (garden != null && garden.getClasses() != null) {
                    List<GardenClass> childClasses = new ArrayList<>();
                    for (GardenClass gardenClass : garden.getClasses()) {
                        if (gardenClass.getChildren().containsKey(childId)) {
                            childClasses.add(gardenClass);  // Add to the list of child's classes
                        }
                    }
                    callback.onCallback(childClasses);  // Return the list of classes
                } else {
                    callback.onCallback(null);  // If no classes found, return null
                }
            } else {
                callback.onCallback(null);  // If kindergarten not found, return null
            }
        });
    }

    /**
     * Updates a kindergarten's details in Firestore.
     *
     * @param gartenId The ID of the kindergarten to update.
     * @param garden   The updated Garden object.
     * @param callback The callback to handle the success or failure of the update.
     */
    public void updateKinderGarten(String gartenId, Garden garden, UpdateCallback callback) {
        db.collection("kindergartens").document(gartenId).set(garden)
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> callback.onCallback(false));
    }

    /**
     * Retrieves a parent object from Firestore by their email.
     *
     * @param email    The email of the parent to retrieve.
     * @param callback The callback to handle the retrieved parent object and document ID.
     */
    public void getParentByEmail(String email, ParentCallback callback) {
        db.collection("Parents")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        Parent parent = document.toObject(Parent.class);
                        callback.onCallback(document.getId(), parent); // Return the document ID and parent object
                    } else {
                        callback.onCallback(null, null);
                    }
                });
    }

    /**
     * Retrieves a kindergarten object from Firestore by its name.
     *
     * @param gardenName The name of the kindergarten to retrieve.
     * @param callback   The callback to handle the retrieved kindergarten object.
     */
    public void getGardenByName(String gardenName, FireBaseManager.GartenCallback callback) {
        db.collection("kindergartens")
                .whereEqualTo("name", gardenName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        Garden garden = document.toObject(Garden.class);
                        if (garden != null) {
                            garden.setId(document.getId()); // Set the document ID in the Garden object
                            callback.onCallback(garden);
                        } else {
                            callback.onCallback(null);
                        }
                    } else {
                        callback.onCallback(null);
                    }
                });
    }

    /**
     * Updates a parent's details in Firestore.
     *
     * @param parentDocumentId The document ID of the parent to update.
     * @param parent           The updated Parent object.
     * @param callback         The callback to handle the success or failure of the update.
     */
    public void updateParent(String parentDocumentId, Parent parent, UpdateCallback callback) {
        db.collection("Parents").document(parentDocumentId)
                .set(parent)
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> callback.onCallback(false));
    }

    /**
     * Deletes a child document from Firestore by the child's ID.
     *
     * @param childId  The ID of the child to delete.
     * @param callback The callback to handle the success or failure of the deletion.
     */
    public void deleteChildById(String childId, UpdateCallback callback) {
        db.collection("Children")
                .whereEqualTo("id", childId) // Make sure this matches the correct field name for the child ID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Delete the found document
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> callback.onCallback(true))
                                    .addOnFailureListener(e -> callback.onCallback(false));
                        }
                    } else {
                        // If no documents were found or there was an error
                        callback.onCallback(false);
                    }
                });
    }


    /**
     * Adds a new organizational affiliation to Firestore.
     *
     * @param affiliationName The name of the organizational affiliation to add.
     * @param callback        The callback to handle the success or failure of the addition.
     */
    public void addOrganizationalAffiliation(String affiliationName, UpdateCallback callback) {
        DocumentReference docRef = db.collection("OrganizationalAffiliations").document(affiliationName);
        docRef.set(new HashMap<>()) // Create a document with the affiliation name
                .addOnSuccessListener(aVoid -> callback.onCallback(true))
                .addOnFailureListener(e -> callback.onCallback(false));
    }

    /**
     * Retrieves all organizational affiliations from Firestore.
     *
     * @param callback The callback to handle the list of affiliations.
     */
    public void getOrganizationalAffiliations(RoleListCallback callback) {
        db.collection("OrganizationalAffiliations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> affiliations = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            affiliations.add(document.getId());
                        }
                        callback.onCallback(affiliations);
                    } else {
                        callback.onCallback(null);
                    }
                });
    }

    /**
     * Retrieves all children in a specified kindergarten.
     *
     * @param gartenId The ID of the kindergarten.
     * @param callback The callback to handle the list of children.
     */
    public void getAllChildrenInGarden(String gartenId, ChildrenListCallback callback) {
        db.collection("Children")
                .whereEqualTo("gartenName", gartenId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Child> childrenList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Child child = document.toObject(Child.class);
                            childrenList.add(child);
                        }
                        callback.onCallback(childrenList);
                    } else {
                        callback.onCallback(null);
                    }
                });
    }

    /**
     * Updates the approval status of a child within a specified kindergarten by its name.
     *
     * @param gardenName The name of the kindergarten.
     * @param childId    The ID of the child.
     * @param isApproved The new approval status to set.
     * @param callback   The callback to handle the success or failure of the update.
     */
    public void updateChildApprovedStatusInGardenByName(String gardenName, String childId, boolean isApproved, UpdateCallback callback) {
        db.collection("kindergartens")  // Access the kindergarten collection
                .whereEqualTo("name", gardenName)  // Search for the kindergarten by name
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Update the 'approved' field for the specific child in the document
                            document.getReference()
                                    .update("children." + childId + ".approved", isApproved)
                                    .addOnSuccessListener(aVoid -> callback.onCallback(true))
                                    .addOnFailureListener(e -> callback.onCallback(false));
                        }
                    } else {
                        // No matching documents found
                        callback.onCallback(false);
                    }
                })
                .addOnFailureListener(e -> callback.onCallback(false));
    }

    /**
     * Checks if a child is approved in the Firestore database.
     *
     * @param childId  The ID of the child to check.
     * @param callback The callback to handle the approval status.
     */
    public void isChildApproved(String childId, UpdateCallback callback) {
        db.collection("children")
                .document(childId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean isApproved = documentSnapshot.getBoolean("approved");
                        callback.onCallback(isApproved);
                    } else {
                        callback.onCallback(false);
                    }
                })
                .addOnFailureListener(e -> callback.onCallback(false));
    }

    /**
     * Adds a review to both the specified kindergarten and the parent who submitted it.
     *
     * @param gartenName  The name of the kindergarten to add the review to.
     * @param parentEmail The email of the parent who submitted the review.
     * @param review      The review object to add.
     */
    public void addReviewToGartenAndParent(String gartenName, String parentEmail, Review review) {
        // Find the document for the kindergarten by name
        db.collection("kindergartens")
                .whereEqualTo("name", gartenName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the ID of the first matching document
                        String gartenId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Save the review in the kindergarten
                        db.collection("kindergartens").document(gartenId)
                                .update("reviews", FieldValue.arrayUnion(review))
                                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Review added to garden successfully."))
                                .addOnFailureListener(e -> Log.w("Firebase", "Error adding review to garden", e));
                    } else {
                        Log.w("Firebase", "Garden not found");
                    }
                })
                .addOnFailureListener(e -> Log.w("Firebase", "Error finding garden", e));

        // Find the document for the parent by email
        db.collection("Parents")
                .whereEqualTo("email", parentEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the ID of the first matching document
                        String parentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Save the review for the parent
                        db.collection("Parents").document(parentId)
                                .update("reviews", FieldValue.arrayUnion(review))
                                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Review added to parent successfully."))
                                .addOnFailureListener(e -> Log.w("Firebase", "Error adding review to parent", e));
                    } else {
                        Log.w("Firebase", "Parent not found");
                    }
                })
                .addOnFailureListener(e -> Log.w("Firebase", "Error finding parent", e));
    }

    /**
     * Retrieves the reviews for a specific kindergarten by its name.
     *
     * @param gartenName The name of the kindergarten to retrieve reviews for.
     * @param callback   The callback to handle the list of reviews.
     */
    public void getReviewsForGarten(String gartenName, ReviewListCallback callback) {
        db.collection("kindergartens").whereEqualTo("name", gartenName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        Log.d("Firebase", "Document ID: " + documentSnapshot.getId());

                        // Retrieve the list of reviews as a List of HashMaps
                        List<Map<String, Object>> reviewMaps = (List<Map<String, Object>>) documentSnapshot.get("reviews");

                        List<Review> reviews = new ArrayList<>();
                        if (reviewMaps != null) {
                            for (Map<String, Object> reviewMap : reviewMaps) {
                                try {
                                    Review review = new Review();
                                    review.setParentEmail((String) reviewMap.get("parentEmail"));
                                    review.setRating(((Long) reviewMap.get("rating")).intValue());
                                    review.setComment((String) reviewMap.get("comment"));
                                    review.setManagerResponse((String) reviewMap.get("managerResponse"));
                                    review.setReviewDate(((Timestamp) reviewMap.get("reviewDate")).toDate());

                                    // Filter the reviews to show only those where managerResponse is NULL
                                    if (review.getManagerResponse() == null) {
                                        reviews.add(review);
                                    }
                                } catch (ClassCastException e) {
                                    Log.e("Firebase", "Failed to cast reviewMap to Review", e);
                                }
                            }
                        }
                        callback.onCallback(reviews);
                    } else {
                        Log.d("Firebase", "No document found for garden: " + gartenName);
                        callback.onCallback(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> callback.onCallback(null));
    }

    /**
     * Retrieves the reviews for a specific parent by their email.
     *
     * @param parentEmail The email of the parent to retrieve reviews for.
     * @param callback    The callback to handle the list of reviews.
     */
    public void getReviewsForParent(String parentEmail, ReviewListCallback callback) {
        db.collection("Parents")
                .whereEqualTo("email", parentEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        Log.d("Firebase", "Document ID: " + documentSnapshot.getId());

                        // Retrieve the list of reviews as a List of HashMaps
                        List<Map<String, Object>> reviewMaps = (List<Map<String, Object>>) documentSnapshot.get("reviews");

                        List<Review> reviews = new ArrayList<>();
                        if (reviewMaps != null) {
                            for (Map<String, Object> reviewMap : reviewMaps) {
                                try {
                                    Review review = new Review();
                                    review.setParentEmail((String) reviewMap.get("parentEmail"));
                                    review.setRating(((Long) reviewMap.get("rating")).intValue());
                                    review.setComment((String) reviewMap.get("comment"));
                                    review.setManagerResponse((String) reviewMap.get("managerResponse"));
                                    review.setReviewDate(((Timestamp) reviewMap.get("reviewDate")).toDate());

                                    reviews.add(review);
                                } catch (ClassCastException e) {
                                    Log.e("Firebase", "Failed to cast reviewMap to Review", e);
                                }
                            }
                        }
                        callback.onCallback(reviews);
                    } else {
                        Log.d("Firebase", "No document found for parent: " + parentEmail);
                        callback.onCallback(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> callback.onCallback(null));
    }

    /**
     * Updates a specific review within a kindergarten by its name.
     *
     * @param gardenName    The name of the kindergarten to update the review in.
     * @param updatedReview The updated review object containing the new data.
     */
    public void updateReviewInGardenByName(String gardenName, Review updatedReview) {
        db.collection("kindergartens")
                .whereEqualTo("name", gardenName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String gartenId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        DocumentReference gartenDocRef = db.collection("kindergartens").document(gartenId);

                        gartenDocRef.get().addOnSuccessListener(documentSnapshot -> {
                            List<Map<String, Object>> reviews = (List<Map<String, Object>>) documentSnapshot.get("reviews");
                            if (reviews != null) {
                                for (Map<String, Object> reviewMap : reviews) {
                                    if (reviewMap.get("parentEmail").equals(updatedReview.getParentEmail())
                                            && ((Timestamp) reviewMap.get("reviewDate")).toDate().equals(updatedReview.getReviewDate())) {
                                        reviewMap.put("managerResponse", updatedReview.getManagerResponse());
                                        break;
                                    }
                                }
                                gartenDocRef.update("reviews", reviews)
                                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Review updated in garden successfully"))
                                        .addOnFailureListener(e -> Log.w("Firebase", "Error updating review in garden", e));
                            }
                        });
                    } else {
                        Log.w("Firebase", "Garden not found for name: " + gardenName);
                    }
                })
                .addOnFailureListener(e -> Log.w("Firebase", "Error finding garden by name", e));
    }

    /**
     * Updates a specific review within a parent's document by their email.
     *
     * @param parentEmail   The email of the parent whose review needs to be updated.
     * @param updatedReview The updated review object containing the new data.
     */
    public void updateReviewInParentByEmail(String parentEmail, Review updatedReview) {
        db.collection("Parents")
                .whereEqualTo("email", parentEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String parentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        DocumentReference parentDocRef = db.collection("Parents").document(parentId);

                        parentDocRef.get().addOnSuccessListener(documentSnapshot -> {
                            List<Map<String, Object>> reviews = (List<Map<String, Object>>) documentSnapshot.get("reviews");
                            if (reviews != null) {
                                for (Map<String, Object> reviewMap : reviews) {
                                    if (reviewMap.get("parentEmail").equals(updatedReview.getParentEmail())
                                            && ((Timestamp) reviewMap.get("reviewDate")).toDate().equals(updatedReview.getReviewDate())) {
                                        reviewMap.put("managerResponse", updatedReview.getManagerResponse());
                                        break;
                                    }
                                }
                                parentDocRef.update("reviews", reviews)
                                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Review updated in parent successfully"))
                                        .addOnFailureListener(e -> Log.w("Firebase", "Error updating review in parent", e));
                            }
                        });
                    } else {
                        Log.w("Firebase", "Parent not found for email: " + parentEmail);
                    }
                })
                .addOnFailureListener(e -> Log.w("Firebase", "Error finding parent by email", e));
    }

    /**
     * Retrieves the top-rated kindergartens based on the average review rating.
     *
     * @param topN     The number of top-rated kindergartens to retrieve.
     * @param callback The callback to handle the list of top-rated kindergartens.
     */
    public void getTopRatedGardens(int topN, FireBaseManager.GartenListCallback callback) {
        db.collection("kindergartens")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Garden> gardens = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Garden garden = documentSnapshot.toObject(Garden.class);
                        List<Map<String, Object>> reviewMaps = (List<Map<String, Object>>) documentSnapshot.get("reviews");

                        if (reviewMaps != null && !reviewMaps.isEmpty()) {
                            int totalRating = 0;
                            int reviewCount = 0;

                            for (Map<String, Object> reviewMap : reviewMaps) {
                                int rating = ((Long) reviewMap.get("rating")).intValue();
                                totalRating += rating;
                                reviewCount++;
                            }

                            if (reviewCount > 0) {
                                double averageRating = ((double) totalRating / reviewCount) * 10; // Calculate average and scale to 10
                                garden.setAverageRating(averageRating);
                            }
                        } else {
                            garden.setAverageRating(0); // No reviews available
                        }

                        gardens.add(garden);
                    }

                    // Sort the kindergartens by their average rating
                    gardens.sort((g1, g2) -> Double.compare(g2.getAverageRating(), g1.getAverageRating()));

                    // Return the top N kindergartens
                    callback.onCallback(gardens.subList(0, Math.min(topN, gardens.size())));
                })
                .addOnFailureListener(e -> callback.onCallback(null));
    }

    /**
     * Retrieves a staff member's details based on their email.
     *
     * @param email    The email of the staff member to retrieve.
     * @param callback The callback to handle the staff member's details.
     */
    public void getStaffByEmail(String email, UserCallback callback) {
        db.collection("staff")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        GardenStaff staff = document.toObject(GardenStaff.class);
                        callback.onCallback(document.getId(), staff);
                    } else {
                        callback.onCallback(null, null);
                    }
                });
    }


    /**
     * Retrieves the list of approved children for a specific garden by its name.
     *
     * @param gardenName The name of the garden to retrieve approved children from.
     * @param callback   The callback to handle the list of approved children.
     */
    public void getApprovedChildrenByGarden(String gardenName, ChildrenListCallback callback) {
        db.collection("kindergartens")
                .whereEqualTo("name", gardenName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot gardenDoc = queryDocumentSnapshots.getDocuments().get(0);
                        List<Child> childrenList = new ArrayList<>();

                        if (gardenDoc.exists() && gardenDoc.contains("children")) {
                            Map<String, Map<String, Object>> childrenMap = (Map<String, Map<String, Object>>) gardenDoc.get("children");

                            if (childrenMap != null) {
                                for (Map.Entry<String, Map<String, Object>> entry : childrenMap.entrySet()) {
                                    Map<String, Object> childData = entry.getValue();
                                    Boolean approved = (Boolean) childData.get("approved");

                                    if (approved != null && approved) {
                                        Map<String, Object> childInfo = (Map<String, Object>) childData.get("child");
                                        Child child = new Child();

                                        child.setID((String) childInfo.get("id"));
                                        child.setFullName((String) childInfo.get("fullName"));

                                        // Convert age from Long to Integer
                                        if (childInfo.get("age") != null) {
                                            child.setAge(((Long) childInfo.get("age")).intValue());
                                        }

                                        child.setGartenName((String) childInfo.get("gartenName"));
                                        child.setHobbies((List<String>) childInfo.get("hobbies"));

                                        childrenList.add(child);
                                    }
                                }
                            }
                        }
                        callback.onCallback(childrenList);
                    } else {
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> callback.onCallback(null));
    }

    /**
     * Updates a child document in Firebase Firestore. If the child has existing notes, it merges them with new ones.
     *
     * @param child The child object to update.
     */
    public void updateChild(Child child) {
        db.collection("Children")
                .whereEqualTo("id", child.getID())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);

                        // Retrieve existing notes
                        List<Note> existingNotes = (List<Note>) document.get("notes");

                        if (existingNotes == null) {
                            existingNotes = new ArrayList<>();
                        }

                        // Merge existing notes with new ones
                        if (child.getNotes() != null) {
                            for (Note newNote : child.getNotes()) {
                                boolean noteExists = false;
                                for (Note existingNote : existingNotes) {
                                    if (existingNote.getNote().equals(newNote.getNote())) {
                                        existingNote.setCourseType(newNote.getCourseType());
                                        existingNote.setRating(newNote.getRating());
                                        noteExists = true;
                                        break;
                                    }
                                }
                                if (!noteExists) {
                                    existingNotes.add(newNote);
                                }
                            }
                        }

                        // Prepare data for update
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("notes", existingNotes);

                        // Update child document in Firestore
                        document.getReference().update(updates)
                                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Child updated successfully with ID: " + child.getID()))
                                .addOnFailureListener(e -> Log.e("Firebase", "Failed to update child: " + e.getMessage()));

                        // Update parent documents with merged notes
                        List<Note> finalExistingNotes = existingNotes;
                        db.collection("Parents")
                                .get()
                                .addOnCompleteListener(parentTask -> {
                                    if (parentTask.isSuccessful()) {
                                        for (DocumentSnapshot parentDoc : parentTask.getResult()) {
                                            List<Map<String, Object>> children = (List<Map<String, Object>>) parentDoc.get("children");
                                            if (children != null) {
                                                for (Map<String, Object> childMap : children) {
                                                    if (childMap.get("id").equals(child.getID())) {
                                                        List<Note> parentNotes = (List<Note>) childMap.get("notes");

                                                        if (parentNotes == null) {
                                                            parentNotes = new ArrayList<>();
                                                        }

                                                        for (Note newNote : finalExistingNotes) {
                                                            boolean noteExists = false;
                                                            for (Note existingNote : parentNotes) {
                                                                if (existingNote.getNote().equals(newNote.getNote())) {
                                                                    existingNote.setCourseType(newNote.getCourseType());
                                                                    existingNote.setRating(newNote.getRating());
                                                                    noteExists = true;
                                                                    break;
                                                                }
                                                            }
                                                            if (!noteExists) {
                                                                parentNotes.add(newNote);
                                                            }
                                                        }

                                                        childMap.put("notes", parentNotes);
                                                    }
                                                }
                                                parentDoc.getReference().update("children", children)
                                                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Parent updated successfully with child ID: " + child.getID()))
                                                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to update parent with child ID: " + child.getID() + ", error: " + e.getMessage()));
                                            }
                                        }
                                    } else {
                                        Log.e("Firebase", "Error finding parent documents: " + parentTask.getException().getMessage());
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("Firebase", "Error finding parent documents: " + e.getMessage()));
                    } else {
                        Log.e("Firebase", "No child document found with ID: " + child.getID());
                    }
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Error finding child document: " + e.getMessage()));
    }

    /**
     * Retrieves the course type from a specific kindergarten by its name and course number.
     *
     * @param gardenName The name of the kindergarten.
     * @param courseNumber The course number to retrieve the type for.
     * @param listener   The listener to handle the retrieved course type.
     */
    public void getCourseTypeFromGarden(String gardenName, String courseNumber, OnCourseTypeFetchedListener listener) {
        db.collection("kindergartens")
                .whereEqualTo("name", gardenName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                List<Map<String, Object>> classes = (List<Map<String, Object>>) document.get("classes");
                                for (Map<String, Object> course : classes) {
                                    if (course.get("courseNumber").equals(courseNumber)) {
                                        listener.onCourseTypeFetched((String) course.get("courseType"));
                                        return;
                                    }
                                }
                            }
                        }
                        listener.onCourseTypeFetched(null); // No result found
                    } else {
                        listener.onCourseTypeFetched(null); // Failed to retrieve course type
                    }
                });
    }

    /**
     * Loads notes from Firebase Firestore for a specific parent based on their email.
     *
     * @param parentEmail The email of the parent to load notes for.
     * @param listener    The listener to handle the loaded notes.
     */
    public void loadNotesFromFirebaseByParentEmail(String parentEmail, OnNotesLoadedListener listener) {
        Log.d("FireBaseManager", "Loading notes for parent email: " + parentEmail);

        db.collection("Parents")
                .whereEqualTo("email", parentEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documentSnapshots = task.getResult();
                        Log.d("FireBaseManager", "Query successful, document snapshots: " + documentSnapshots.getDocuments());

                        if (!documentSnapshots.isEmpty()) {
                            List<Note> allNotes = new ArrayList<>();
                            for (DocumentSnapshot parentDoc : documentSnapshots.getDocuments()) {
                                Log.d("FireBaseManager", "Processing parent document: " + parentDoc.getId());
                                List<Map<String, Object>> children = (List<Map<String, Object>>) parentDoc.get("children");
                                if (children != null) {
                                    for (Map<String, Object> childMap : children) {
                                        List<Map<String, Object>> notesMap = (List<Map<String, Object>>) childMap.get("notes");

                                        if (notesMap != null) {
                                            for (Map<String, Object> noteMap : notesMap) {
                                                Log.d("FireBaseManager", "Converting note map: " + noteMap);
                                                Note note = convertMapToNote(noteMap);
                                                allNotes.add(note);
                                            }
                                        } else {
                                            Log.d("FireBaseManager", "No notes found for child: " + childMap);
                                        }
                                    }
                                } else {
                                    Log.d("FireBaseManager", "No children found for parent: " + parentDoc.getId());
                                }
                            }
                            Log.d("FireBaseManager", "Loaded notes: " + allNotes);
                            listener.onNotesLoaded(allNotes, null); // Passing all notes; ratings are not relevant in this case
                        } else {
                            Log.d("FireBaseManager", "No documents found for email: " + parentEmail);
                            listener.onNotesLoaded(null, null);
                        }
                    } else {
                        Log.e("FireBaseManager", "Error getting documents.", task.getException());
                        listener.onNotesLoaded(null, null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FireBaseManager", "Failed to load notes.", e);
                    listener.onError(e);
                });
    }

    /**
     * Converts a map to a Note object.
     *
     * @param noteMap The map containing note data.
     * @return A Note object constructed from the map.
     */
    private Note convertMapToNote(Map<String, Object> noteMap) {
        Log.d("FireBaseManager", "Converting map to note: " + noteMap);

        String authorName = (String) noteMap.get("authorName");
        String authorRole = (String) noteMap.get("authorRole");
        Date date = noteMap.get("date") != null ? ((Timestamp) noteMap.get("date")).toDate() : null;
        String noteText = (String) noteMap.get("note");
        String courseType = (String) noteMap.get("courseType");
        Integer rating = noteMap.get("rating") != null ? ((Long) noteMap.get("rating")).intValue() : null;

        return new Note(noteText, date, authorName, authorRole, courseType, rating);
    }




    /**
     * Saves a child photo to Firebase Storage and then stores its metadata in Firestore.
     *
     * @param newPhoto           The new ChildPhoto object containing metadata about the photo.
     * @param imageUri           The URI of the image to be uploaded.
     * @param onSuccessListener  The listener that will be triggered upon successful completion.
     * @param onFailureListener  The listener that will be triggered if an error occurs.
     */
    public static void saveChildPhoto(ChildPhoto newPhoto, Uri imageUri, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        if (imageUri != null && newPhoto.getChildId() != null) {
            String imageName = System.currentTimeMillis() + ".jpg";
            StorageReference storageRef = storage.getReference().child("child_photos/" + newPhoto.getChildId() + "/" + imageName);

            storageRef.putFile(imageUri)
                    .addOnFailureListener(onFailureListener)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        newPhoto.setImageURL(uri.toString());

                        db.collection("ChildPhotos").add(newPhoto)
                                .addOnSuccessListener(documentReference -> onSuccessListener.onSuccess(null))
                                .addOnFailureListener(onFailureListener);
                    }).addOnFailureListener(onFailureListener));
        } else {
            onFailureListener.onFailure(new IllegalArgumentException("Image URI or Child ID is null"));
        }
    }

    /**
     * Retrieves the list of children associated with a parent and a specific kindergarten.
     *
     * @param parentEmail       The email of the parent.
     * @param kindergartenName  The name of the kindergarten.
     * @param listener          The listener that will handle the list of child IDs.
     */
    public void getChildrenForParentAndKindergarten(String parentEmail, String kindergartenName, OnChildrenFetchedListener listener) {
        db.collection("Parents")
                .whereEqualTo("email", parentEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot parentDocument = task.getResult().getDocuments().get(0);
                        List<Map<String, Object>> children = (List<Map<String, Object>>) parentDocument.get("children");

                        List<String> childIds = new ArrayList<>();
                        if (children != null) {
                            for (Map<String, Object> child : children) {
                                String childGartenName = (String) child.get("gartenName");
                                String childId = (String) child.get("id");

                                Log.d("FirebaseManager", "Checking child: " + childId + ", gartenName: " + childGartenName);

                                if (childGartenName != null && childGartenName.equals(kindergartenName) && childId != null) {
                                    Log.d("FirebaseManager", "Match found: " + childId);
                                    childIds.add(childId);
                                }
                            }
                        }
                        listener.onChildrenFetched(childIds);
                    } else {
                        listener.onChildrenFetched(new ArrayList<>()); // במקרה של כישלון, מחזיר רשימה ריקה
                    }
                });
    }

    /**
     * Loads photos from Firestore for a list of child IDs.
     *
     * @param childIds  The list of child IDs to fetch photos for.
     * @param listener  The listener that will handle the list of ChildPhoto objects.
     */
    public void loadPhotosFromFirestore(List<String> childIds, OnPhotosLoadedListener listener) {
        CollectionReference photosRef = db.collection("ChildPhotos");

        if (childIds != null && !childIds.isEmpty()) {
            photosRef.whereIn("childId", childIds)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<ChildPhoto> photosList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ChildPhoto photo = document.toObject(ChildPhoto.class);
                                photosList.add(photo);
                            }
                            listener.onPhotosLoaded(photosList);
                        } else {
                            listener.onPhotosLoaded(new ArrayList<>()); // במקרה של כישלון, מחזיר רשימה ריקה
                        }
                    });
        } else {
            listener.onPhotosLoaded(new ArrayList<>()); // במקרה של רשימת ילדים ריקה, מחזיר רשימה ריקה
        }
    }

    /**
     * Updates the status of a kindergarten by its name.
     *
     * @param gardenName          The name of the kindergarten.
     * @param status              The new status to be set.
     * @param onCompleteListener  The listener that will be triggered upon completion.
     */
    public void updateGardenStatusByName(String gardenName, String status, OnCompleteListener<Void> onCompleteListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("kindergartens")
                .whereEqualTo("name", gardenName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        document.getReference().update("status", status)
                                .addOnCompleteListener(onCompleteListener);
                    } else {
                        onCompleteListener.onComplete(Tasks.forException(new Exception("Garden not found")));
                    }
                });
    }

    /**
     * Retrieves kindergartens with average ratings within a specified percentage range.
     *
     * @param minPercent  The minimum percentage rating.
     * @param maxPercent  The maximum percentage rating.
     * @param callback    The callback to handle the list of kindergartens.
     */
    public void getGardensWithRatingsInRange(int minPercent, int maxPercent, GardenListCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("kindergartens").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Garden> filteredGardens = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            List<Map<String, Object>> reviews = (List<Map<String, Object>>) document.get("reviews");
                            if (reviews != null && !reviews.isEmpty()) {
                                double sumRatings = 0;
                                for (Map<String, Object> review : reviews) {
                                    sumRatings += ((Number) review.get("rating")).doubleValue();
                                }
                                double averageRating = (sumRatings / reviews.size()) * 10; // Convert to percentage
                                if (averageRating >= minPercent && averageRating <= maxPercent) {
                                    Garden garden = document.toObject(Garden.class);
                                    filteredGardens.add(garden);
                                }
                            }
                        }
                        callback.onGardensRetrieved(filteredGardens);
                    } else {
                        callback.onError(task.getException().getMessage());
                    }
                });
    }



    /**
     * Listener for fetching a list of child IDs.
     */
    public interface OnChildrenFetchedListener {
        void onChildrenFetched(List<String> childIds);
    }

    /**
     * Listener for loading a list of child photos.
     */
    public interface OnPhotosLoadedListener {
        void onPhotosLoaded(List<ChildPhoto> photosList);
    }

    /**
     * Listener for loading notes and behavior ratings.
     */
    public interface OnNotesLoadedListener {
        void onNotesLoaded(List<Note> notes, Map<String, Integer> behaviorRatings);
        void onError(Exception e);
    }

    /**
     * Listener for fetching the type of a course.
     */
    public interface OnCourseTypeFetchedListener {
        void onCourseTypeFetched(String courseType);
    }

    /**
     * Callback for retrieving a list of gardens.
     */
    public interface GardenListCallback {
        void onGardensRetrieved(List<Garden> gardens);
        void onError(String errorMessage);
    }

    /**
     * Callback for handling a list of reviews.
     */
    public interface ReviewListCallback {
        void onCallback(List<Review> reviews);
    }

    /**
     * Callback for handling a list of children.
     */
    public interface ChildrenListCallback {
        void onCallback(List<Child> childrenList);
    }

    /**
     * Callback for retrieving parent data.
     */
    public interface ParentCallback {
        void onCallback(String parentId, Parent parent);
    }

    /**
     * Callback for retrieving staff data.
     */
    public interface UserCallback {
        void onCallback(String documentId, GardenStaff staff);
    }

    /**
     * Callback for indicating the success of an update operation.
     */
    public interface UpdateCallback {
        void onCallback(boolean success);
    }

    /**
     * Callback for determining the user type (parent, director, or staff).
     */
    public interface UserTypeCallback {
        void onCallback(String userType);
    }

    /**
     * Callback for retrieving a garden ID.
     */
    public interface GartenIdCallback {
        void onCallback(String gartenId);
    }

    /**
     * Callback for retrieving a list of roles.
     */
    public interface RoleListCallback {
        void onCallback(List<String> roles);
    }

    /**
     * Callback for retrieving a list of courses.
     */
    public interface CourseListCallback {
        void onCallback(List<GardenClass> courses);
    }

    /**
     * Callback for retrieving a list of staff members.
     */
    public interface StaffListCallback {
        void onCallback(List<GardenStaff> staffList);
    }

    /**
     * Callback for retrieving a list of gardens.
     */
    public interface GartenListCallback {
        void onCallback(List<Garden> gardenList);
    }

    /**
     * Callback for retrieving a specific garden object.
     */
    public interface GartenCallback {
        void onCallback(Garden garden);
    }
}
