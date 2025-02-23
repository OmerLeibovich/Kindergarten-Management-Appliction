package com.example.finalprojectapp.staff;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectapp.R;
import com.example.finalprojectapp.database.FireBaseManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import Adapters.StaffAdapter;
import Objects.Garden;
import Objects.GardenClass;
import Objects.GardenStaff;

/**
 * StaffListInGardenFragment displays a list of staff members in a specific garden.
 * The fragment allows filtering of staff members by role, garden, and course.
 * It also provides options to update or remove a garden from a staff member or add a course to a staff member.
 */
public class StaffListInGardenFragment extends Fragment implements StaffAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private StaffAdapter staffAdapter;
    private List<GardenStaff> staffList;
    private List<GardenStaff> originalStaffList; // To store the original list
    private FireBaseManager fireBaseManager;
    private Spinner roleSpinner;
    private Spinner gardenSpinner;
    private Spinner courseSpinner;
    private TextView roleLabel;
    private TextView gardenLabel;
    private TextView courseLabel;

    /**
     * Default constructor required for fragment subclasses.
     */
    public StaffListInGardenFragment() {
        // Required empty public constructor
    }

    /**
     * Creates a new instance of StaffListInGardenFragment.
     *
     * @return A new instance of fragment StaffListInGardenFragment.
     */
    public static StaffListInGardenFragment newInstance() {
        return new StaffListInGardenFragment();
    }

    /**
     * Called to inflate the layout for this fragment.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_staff_list_in_garden, container, false);
    }

    /**
     * Called immediately after onCreateView has returned, but before any saved state has been restored into the view.
     *
     * @param view               The View returned by onCreateView.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewStaffInGarten);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        staffList = new ArrayList<>();
        originalStaffList = new ArrayList<>(); // Initialize the original list
        staffAdapter = new StaffAdapter(staffList, this);
        recyclerView.setAdapter(staffAdapter);
        fireBaseManager = new FireBaseManager(getContext());

        roleSpinner = view.findViewById(R.id.roleSpinner);
        gardenSpinner = view.findViewById(R.id.gardenSpinner);
        courseSpinner = view.findViewById(R.id.courseSpinner);

        // Add labels for the spinners
        roleLabel = view.findViewById(R.id.roleLabel);
        gardenLabel = view.findViewById(R.id.gardenLabel);
        courseLabel = view.findViewById(R.id.courseLabel);

        roleLabel.setText("Role");
        gardenLabel.setText("Garden");
        courseLabel.setText("Course");

        loadStaffInGarten();
        loadFilters();

        // Set listeners to filter staff based on selection
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterStaff();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        gardenSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadCoursesForSelectedGarden();
                filterStaff();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterStaff();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Loads the list of staff members in the garden from Firebase.
     * The staff list is displayed in the RecyclerView.
     */
    void loadStaffInGarten() {
        fireBaseManager.getStaffInGarten(new FireBaseManager.StaffListCallback() {
            @Override
            public void onCallback(List<GardenStaff> staffList) {
                if (staffList != null) {
                    StaffListInGardenFragment.this.staffList.clear();
                    StaffListInGardenFragment.this.staffList.addAll(staffList);
                    originalStaffList.clear();
                    originalStaffList.addAll(staffList); // Save the original list
                    staffAdapter.notifyDataSetChanged();
                    // Display all staff by default
                    staffAdapter.updateList(new ArrayList<>(originalStaffList));
                } else {
                    Snackbar.make(getView(), "Failed to load staff", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Loads the available filters for role, garden, and course.
     * This method sets up the spinners for filtering staff.
     */
    private void loadFilters() {
        List<String> roles = new ArrayList<>();
        roles.add(""); // Add empty option
        roles.add("KINDERGARTEN_TEACHER");
        roles.add("ASSISTANT");
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        fireBaseManager.getDirectorGardens(new FireBaseManager.GartenListCallback() {
            @Override
            public void onCallback(List<Garden> gardenList) {
                List<String> gardenNames = new ArrayList<>();
                gardenNames.add(""); // Add empty option
                for (Garden garden : gardenList) {
                    gardenNames.add(garden.getName());
                }
                ArrayAdapter<String> gardenAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, gardenNames);
                gardenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                gardenSpinner.setAdapter(gardenAdapter);
            }
        });
    }

    /**
     * Loads the courses available for the selected garden.
     * This method updates the course spinner based on the selected garden.
     */
    private void loadCoursesForSelectedGarden() {
        String selectedGarden = (String) gardenSpinner.getSelectedItem();
        if (selectedGarden != null && !selectedGarden.isEmpty()) {
            fireBaseManager.getGartenIdByName(selectedGarden, new FireBaseManager.GartenIdCallback() {
                @Override
                public void onCallback(String gartenId) {
                    if (gartenId != null) {
                        fireBaseManager.getClassesForGarten(gartenId, new FireBaseManager.CourseListCallback() {
                            @Override
                            public void onCallback(List<GardenClass> courses) {
                                List<String> courseNumbers = new ArrayList<>();
                                courseNumbers.add(""); // Add empty option
                                for (GardenClass course : courses) {
                                    courseNumbers.add(course.getCourseNumber());
                                }
                                ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, courseNumbers);
                                courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                courseSpinner.setAdapter(courseAdapter);
                            }
                        });
                    }
                }
            });
        } else {
            // Clear the course spinner if no garden is selected
            ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
            courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            courseSpinner.setAdapter(courseAdapter);
        }
    }

    /**
     * Filters the staff list based on the selected role, garden, and course.
     */
    private void filterStaff() {
        String selectedRole = (String) roleSpinner.getSelectedItem();
        String selectedGarden = (String) gardenSpinner.getSelectedItem();
        String selectedCourse = (String) courseSpinner.getSelectedItem();

        List<GardenStaff> filteredList = new ArrayList<>();
        for (GardenStaff staff : originalStaffList) {
            boolean matchesRole = selectedRole == null || selectedRole.isEmpty() || staff.getRole().equals(selectedRole);
            boolean matchesGarden = selectedGarden == null || selectedGarden.isEmpty() || (staff.getGarten() != null && staff.getGarten().getName().equals(selectedGarden));
            boolean matchesCourse = selectedCourse == null || selectedCourse.isEmpty() || (staff.getClasses() != null && containsCourse(staff.getClasses(), selectedCourse));

            if (matchesRole && matchesGarden && matchesCourse) {
                filteredList.add(staff);
            }
        }

        staffAdapter.updateList(filteredList);
    }

    /**
     * Checks if the given list of classes contains a specific course number.
     *
     * @param classes      The list of GardenClass objects.
     * @param courseNumber The course number to check for.
     * @return True if the course is found, otherwise false.
     */
    private boolean containsCourse(List<GardenClass> classes, String courseNumber) {
        if (classes == null || courseNumber == null) {
            return false;
        }
        for (GardenClass gardenClass : classes) {
            if (gardenClass != null && courseNumber.equals(gardenClass.getCourseNumber())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called when the user clicks the "Update" button for a staff member.
     * This method displays a dialog to confirm the update.
     *
     * @param staff The GardenStaff object representing the staff member to update.
     */
    @Override
    public void onUpdateClick(GardenStaff staff) {
        new AlertDialog.Builder(getContext())
                .setTitle("Update Garden")
                .setMessage("Are you sure you want to update the garden for this staff member?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    UpdateGardenDialogFragment dialogFragment = UpdateGardenDialogFragment.newInstance(staff, this);
                    dialogFragment.show(getParentFragmentManager(), "UpdateGardenDialog");
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Called when the user clicks the "Remove" button for a staff member.
     * This method displays a dialog to confirm the removal.
     *
     * @param staff The GardenStaff object representing the staff member to remove from the garden.
     */
    @Override
    public void onRemoveClick(GardenStaff staff) {
        new AlertDialog.Builder(getContext())
                .setTitle("Remove Garden")
                .setMessage("Are you sure you want to remove the garden for this staff member?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    staff.setGarten(null);
                    staff.setClasses(new ArrayList<>()); // Reset courses
                    fireBaseManager.updateKinderGartenStaff(staff, new FireBaseManager.GartenIdCallback() {
                        @Override
                        public void onCallback(String gartenId) {
                            if (gartenId != null) {
                                staffAdapter.notifyDataSetChanged();
                                Snackbar.make(getView(), "Garden removed successfully", Snackbar.LENGTH_SHORT).show();
                                loadStaffInGarten(); // Refresh the list after removal
                            } else {
                                Snackbar.make(getView(), "Failed to remove garden", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Called when the user clicks the "Add" button for a staff member.
     * This method is not used in this fragment.
     *
     * @param staff The GardenStaff object representing the staff member.
     */
    @Override
    public void onAddClick(GardenStaff staff) {
        // Not used in this fragment
    }

    /**
     * Called when the user clicks the "Add Course" button for a staff member.
     * This method displays a dialog to confirm adding a course to the staff member.
     *
     * @param staff The GardenStaff object representing the staff member.
     */
    @Override
    public void onAddCourseClick(GardenStaff staff) {
        new AlertDialog.Builder(getContext())
                .setTitle("Add Course")
                .setMessage("Are you sure you want to add a course to this staff member?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    AddCourseDialogFragment dialogFragment = AddCourseDialogFragment.newInstance(staff, this);
                    dialogFragment.show(getParentFragmentManager(), "AddCourseDialog");
                })
                .setNegativeButton("No", null)
                .show();
    }
}
