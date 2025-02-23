package Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalprojectapp.R;
import com.example.finalprojectapp.UserSessionManager;
import com.example.finalprojectapp.database.FireBaseManager;
import com.example.finalprojectapp.parents.ChildGardensFragment;
import com.example.finalprojectapp.parents.GardenDetailsFragment;
import com.example.finalprojectapp.parents.PhotosFragment;
import com.example.finalprojectapp.parents.ReviewFragment;
import com.example.finalprojectapp.staff.DirectorGardensFragment;
import com.example.finalprojectapp.staff.ViewReviewsFragment;
import com.example.finalprojectapp.staff.AddGardenFragment;
import com.example.finalprojectapp.staff.ClasssForGardenFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import Objects.Child;
import Objects.Garden;
import Objects.GardenClass;

public class GardenAdapter extends RecyclerView.Adapter<GardenAdapter.ViewHolder> {

    private String childId;
    private List<Garden> gardens;
    private Context context;
    private FireBaseManager fireBaseManager;
    private String userType;

    public GardenAdapter(List<Garden> gardens, Context context, String userType, String childId) {
        this.gardens = gardens;
        this.context = context;
        this.userType = userType;
        this.childId = childId;
        fireBaseManager = new FireBaseManager(context);
    }
    public GardenAdapter(List<Garden> gardens, Context context, String userType) {
        this.gardens = gardens;
        this.context = context;
        this.userType = userType;
        fireBaseManager = new FireBaseManager(context);
    }

    public void setGardens(List<Garden> gardens) {
        this.gardens = gardens;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_garden, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Garden garden = gardens.get(position);
        holder.textViewName.append(": "+garden.getName());
        holder.textViewAddress.append(": "+garden.getAddress());
        holder.textViewCity.append(": "+garden.getCity());

        String imageUrl = garden.getImageUrl();
        Glide.with(context).load(imageUrl).into(holder.imageViewGarten);


        if ("SYSTEM_ADMINISTRATOR".equals(userType)) {
            holder.OpenRegisterGarden.setVisibility(View.VISIBLE);
            holder.buttonChangeAffiliation.setVisibility(View.VISIBLE);
            holder.buttonRegisterGarden.setVisibility(View.GONE);
            holder.buttonEditGarden.setVisibility(View.GONE);
            holder.buttonDeleteGarden.setVisibility(View.GONE);
            holder.buttonViewClasses.setVisibility(View.GONE);
            holder.buttonViewChildren.setVisibility(View.VISIBLE);
            holder.ReviewsButton.setVisibility(View.VISIBLE);
        }
        else if ("parent".equals(userType)) {
            holder.buttonViewChildren.setVisibility(View.GONE);
            if (((FragmentActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof ChildGardensFragment) {
                if (garden.isRegistered()) {
                    holder.buttonRegisterGarden.setVisibility(View.GONE);
                    holder.buttonEditGarden.setVisibility(View.VISIBLE);
                    holder.buttonDeleteGarden.setVisibility(View.VISIBLE);
                    holder.buttonViewClasses.setVisibility(View.GONE);

                } else {
                    holder.buttonRegisterGarden.setVisibility(View.GONE);
                    holder.buttonEditGarden.setVisibility(View.GONE);
                    holder.buttonDeleteGarden.setVisibility(View.GONE);
                    holder.buttonViewClasses.setVisibility(View.VISIBLE);
                    if (garden.getRegistrationStartDate() != null) {
                        holder.ButtonRateGarden.setVisibility(View.VISIBLE);
                        holder.buttonPhotos.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                holder.buttonRegisterGarden.setVisibility(View.VISIBLE);
                holder.buttonEditGarden.setVisibility(View.GONE);
                holder.buttonDeleteGarden.setVisibility(View.GONE);
                holder.OpenRegisterGarden.setVisibility(View.GONE);
                holder.buttonViewClasses.setVisibility(View.GONE);
            }
        }
        else  {
            holder.buttonRegisterGarden.setVisibility(View.GONE);
            holder.buttonEditGarden.setVisibility(View.VISIBLE);
            holder.buttonDeleteGarden.setVisibility(View.VISIBLE);
            holder.OpenRegisterGarden.setVisibility(View.GONE);
            holder.buttonViewClasses.setVisibility(View.VISIBLE);
            holder.buttonViewChildren.setVisibility(View.VISIBLE);
            if ("director".equals(userType) ) {
                if (((FragmentActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof DirectorGardensFragment) {
                    holder.StatusText.append(": " + garden.getStatus());
                    holder.StatusText.setVisibility(View.VISIBLE);
                }
            }
        }

        holder.buttonViewChildren.setOnClickListener(v -> {
            fireBaseManager.getAllChildrenInGarden(garden.getName(), childrenList -> {
                if (childrenList != null && !childrenList.isEmpty()) {
                    List<Child> unapprovedChildren = new ArrayList<>();
                    List<Child> approvedChildren = new ArrayList<>();


                    List<Child> childrenInGarden = new ArrayList<>();
                    for (Child child : childrenList) {
                        if (garden.getChildren() != null && garden.getChildren().containsKey(child.getID())) {
                            boolean isApproved = garden.getChildren().get(child.getID()).isApproved(); // בדיקה אם הילד מאושר
                            if (isApproved) {
                                approvedChildren.add(child);
                            } else {
                                unapprovedChildren.add(child);
                            }
                            childrenInGarden.add(child);
                        }
                    }

                    if (!childrenInGarden.isEmpty()) {
                        String[] unapprovedChildrenNames = new String[unapprovedChildren.size()];
                        boolean[] checkedItems = new boolean[unapprovedChildren.size()];
                        for (int i = 0; i < unapprovedChildren.size(); i++) {
                            unapprovedChildrenNames[i] = unapprovedChildren.get(i).getFullName();
                            checkedItems[i] = false;
                        }

                        displayChildrenDialog(context, holder, approvedChildren, unapprovedChildren, unapprovedChildrenNames, checkedItems, garden.getName());
                    } else {
                        Snackbar.make(holder.itemView, "No children found in this garden", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(holder.itemView, "No children found in this garden", Snackbar.LENGTH_SHORT).show();
                }
            });
        });


        holder.buttonDeleteGarden.setOnClickListener(v -> {
            if ("parent".equals(userType)) {

                UserSessionManager userSessionManager = new UserSessionManager(context);
                String parentEmail = userSessionManager.getUserEmail();

                if (parentEmail != null) {

                    fireBaseManager.getParentByEmail(parentEmail, (parentDocumentId, parent) -> {
                        if (parent == null || parentDocumentId == null) {
                            Snackbar.make(holder.itemView, "Failed to retrieve parent details", Snackbar.LENGTH_SHORT).show();
                            return;
                        }

                        List<Child> children = parent.getChildren();
                        if (children == null || children.isEmpty()) {
                            Snackbar.make(holder.itemView, "No children found for this parent", Snackbar.LENGTH_SHORT).show();
                            return;
                        }


                        List<Child> childrenInGarden = new ArrayList<>();
                        for (Child child : children) {
                            if (garden.getChildren() != null && garden.getChildren().containsKey(child.getID())) {
                                childrenInGarden.add(child);
                            }
                        }

                        if (childrenInGarden.isEmpty()) {
                            Snackbar.make(holder.itemView, "No children from this parent are in this garden", Snackbar.LENGTH_SHORT).show();
                            return;
                        }


                        String[] childNames = new String[childrenInGarden.size()];
                        boolean[] selectedChildren = new boolean[childrenInGarden.size()];
                        for (int i = 0; i < childrenInGarden.size(); i++) {
                            childNames[i] = childrenInGarden.get(i).getFullName();
                            selectedChildren[i] = false; // אף אחד מהילדים לא נבחר כברירת מחדל
                        }


                        new AlertDialog.Builder(context)
                                .setTitle("Select Children to Remove")
                                .setMultiChoiceItems(childNames, selectedChildren, (dialog, which, isChecked) -> {
                                    selectedChildren[which] = isChecked;
                                })
                                .setPositiveButton("Remove", (dialog, which) -> {
                                    boolean hasUpdated = false;
                                    List<Child> childrenToRemove = new ArrayList<>();

                                    for (int i = 0; i < selectedChildren.length; i++) {
                                        if (selectedChildren[i]) {
                                            String childId = childrenInGarden.get(i).getID();


                                            if (garden.getChildren() != null) {
                                                garden.getChildren().remove(childId);
                                            }


                                            for (GardenClass gardenClass : garden.getClasses()) {
                                                if (gardenClass.getChildren() != null) {
                                                    gardenClass.getChildren().remove(childId);
                                                }
                                            }


                                            childrenToRemove.add(childrenInGarden.get(i));
                                            hasUpdated = true;


                                            fireBaseManager.deleteChildById(childId, success -> {
                                                if (!success) {
                                                    Snackbar.make(holder.itemView, "Failed to delete child from Firestore", Snackbar.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }

                                    if (hasUpdated) {

                                        fireBaseManager.getGardenByName(garden.getName(), retrievedGarden -> {
                                            if (retrievedGarden != null && retrievedGarden.getId() != null) {
                                                fireBaseManager.updateKinderGarten(retrievedGarden.getId(), garden, successUpdate -> {
                                                    if (successUpdate) {

                                                        for (Child childToRemove : childrenToRemove) {
                                                            children.remove(childToRemove);


                                                            fireBaseManager.updateParent(parentDocumentId, parent, success -> {
                                                                if (success) {
                                                                    Snackbar.make(holder.itemView, "Selected children removed successfully from garden, classes, parent, and Firestore", Snackbar.LENGTH_SHORT).show();
                                                                } else {
                                                                    Snackbar.make(holder.itemView, "Failed to update parent after removing children", Snackbar.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                    } else {
                                                        Snackbar.make(holder.itemView, "Failed to remove children from garden and classes", Snackbar.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                Snackbar.make(holder.itemView, "Failed to retrieve garden by name or Document ID", Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Snackbar.make(holder.itemView, "No children selected for removal", Snackbar.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    });
                } else {
                    Snackbar.make(holder.itemView, "Failed to retrieve parent email from session", Snackbar.LENGTH_SHORT).show();
                }
            } else {
                showConfirmationDialog("Delete Garden", "Are you sure you want to delete this garden?", () -> {
                    fireBaseManager.deleteKinderGarten(garden.getId(), holder.itemView, gartenId -> {
                        if (gartenId != null) {
                            gardens.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, gardens.size());
                            Snackbar.make(holder.itemView, "Garden deleted successfully", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(holder.itemView, "Failed to delete garden", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });

        holder.buttonEditGarden.setOnClickListener(v -> {
            if ("parent".equals(userType)) {
                GardenDetailsFragment gardenDetailsFragment = GardenDetailsFragment.newInstance(
                        garden.getName(),
                        userType
                );

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, gardenDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                AddGardenFragment addGardenFragment = AddGardenFragment.newInstance(garden);
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addGardenFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        holder.buttonViewClasses.setOnClickListener(v -> {
            if ("parent".equals(userType)) {
                fireBaseManager.getChildClassesInGarden(garden.getId(), childId, new FireBaseManager.CourseListCallback() {
                    @Override
                    public void onCallback(List<GardenClass> childClasses) {
                        ClasssForGardenFragment classsForGardenFragment = ClasssForGardenFragment.newInstance(garden.getId(), childClasses);
                        ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, classsForGardenFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                });
            } else {
                ClasssForGardenFragment classsForGardenFragment = ClasssForGardenFragment.newInstance(garden.getId());
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, classsForGardenFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        holder.OpenRegisterGarden.setOnClickListener(v -> {
            fireBaseManager.updateGardenRegistrationStatusByName(garden.getName(), true, success -> {
                if (success) {
                    Snackbar.make(holder.itemView, "Registration opened for this garden", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(holder.itemView, "Failed to open registration for this garden", Snackbar.LENGTH_SHORT).show();
                }
            });
        });

        if (garden.isRegistered() && garden.getRegistrationStartDate() != null) {
            holder.buttonRegisterGarden.setEnabled(true);

            holder.buttonRegisterGarden.setOnClickListener(v -> {
                GardenDetailsFragment gardenDetailsFragment = GardenDetailsFragment.newInstance(garden.getName(), userType);
                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, gardenDetailsFragment)
                        .addToBackStack(null)
                        .commit();
            });
        } else {
            holder.buttonRegisterGarden.setEnabled(false);
        }

        holder.buttonChangeAffiliation.setOnClickListener(v -> {

            fireBaseManager.getOrganizationalAffiliations(affiliations -> {

                affiliations.remove(garden.getOrganizationalAffiliation());


                String[] affiliationsArray = affiliations.toArray(new String[0]);


                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Choose New Affiliation");
                builder.setItems(affiliationsArray, (dialog, which) -> {
                    String selectedAffiliation = affiliationsArray[which];


                    fireBaseManager.getGartenIdByName(garden.getName(), new FireBaseManager.GartenIdCallback() {
                        @Override
                        public void onCallback(String gartenId) {
                            if (gartenId != null) {

                                garden.setId(gartenId);

                                garden.setOrganizationalAffiliation(selectedAffiliation);


                                fireBaseManager.updateKinderGarten(garden, new FireBaseManager.GartenIdCallback() {
                                    @Override
                                    public void onCallback(String gartenId) {
                                        if (gartenId != null) {
                                            Snackbar.make(holder.itemView, "Affiliation updated successfully", Snackbar.LENGTH_SHORT).show();
                                        } else {
                                            Snackbar.make(holder.itemView, "Failed to update affiliation", Snackbar.LENGTH_SHORT).show();
                                        }
                                    }
                                }, holder.itemView);
                            } else {
                                Snackbar.make(holder.itemView, "Failed to find garden by name", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.show();
            });
        });
        holder.ButtonRateGarden.setOnClickListener(v -> {

            ReviewFragment reviewFragment = new ReviewFragment();

            UserSessionManager userSessionManager = new UserSessionManager(context);
            String parentEmail = userSessionManager.getUserEmail();


            Bundle args = new Bundle();
            args.putString("gartenName", garden.getName());
            args.putString("parentEmail",parentEmail );


            reviewFragment.setArguments(args);


            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, reviewFragment)
                    .addToBackStack(null)
                    .commit();
        });
        holder.ReviewsButton.setOnClickListener(v -> {
            ViewReviewsFragment reviewsFragment = new ViewReviewsFragment();

            UserSessionManager userSessionManager = new UserSessionManager(context);
            String parentEmail = userSessionManager.getUserEmail();

            Bundle args = new Bundle();
            args.putString("gartenName", garden.getName());
            args.putString("parentEmail",parentEmail );
            reviewsFragment.setArguments(args);

            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, reviewsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        holder.buttonPhotos.setOnClickListener(v -> {
            PhotosFragment photosFragment = new PhotosFragment();

            UserSessionManager userSessionManager = new UserSessionManager(context);
            String parentEmail = userSessionManager.getUserEmail();

            Bundle args = new Bundle();
            args.putString("gartenName", garden.getName());
            args.putString("parentEmail",parentEmail );
            photosFragment.setArguments(args);

            ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, photosFragment)
                    .addToBackStack(null)
                    .commit();
        });




    }


    @Override
    public int getItemCount() {
        return gardens.size();
    }

    private void displayChildrenDialog(Context context, ViewHolder holder, List<Child> approvedChildren, List<Child> unapprovedChildren, String[] unapprovedChildrenNames, boolean[] checkedItems, String gardenName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Children in Garden");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        if (!approvedChildren.isEmpty()) {
            TextView approvedTitle = new TextView(context);
            approvedTitle.setText("Approved Children:");
            layout.addView(approvedTitle);

            for (Child child : approvedChildren) {
                TextView childName = new TextView(context);
                childName.setText(child.getFullName());
                layout.addView(childName);
            }
        }

        if (!unapprovedChildren.isEmpty()) {
            TextView unapprovedTitle = new TextView(context);
            unapprovedTitle.setText("Pending Approval:");
            layout.addView(unapprovedTitle);

            for (int i = 0; i < unapprovedChildrenNames.length; i++) {
                CheckBox checkBox = new CheckBox(context);
                checkBox.setText(unapprovedChildrenNames[i]);
                int finalI = i;
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> checkedItems[finalI] = isChecked);
                layout.addView(checkBox);
            }
        }

        builder.setView(layout);

        if ("director".equals(userType)){
            builder.setPositiveButton("Approve", (dialog, which) -> {
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        Child child = unapprovedChildren.get(i);
                        fireBaseManager.updateChildApprovedStatusInGardenByName(gardenName, child.getID(), true, success -> {
                            if (Boolean.TRUE.equals(success)) {
                                Snackbar.make(holder.itemView, "Child " + child.getFullName() + " approved successfully", Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(holder.itemView, "Failed to approve " + child.getFullName(), Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> onConfirm.run())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
    public void updateGardens(List<Garden> gardens) {
        this.gardens = gardens;
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewAddress, textViewCity,StatusText;
        Button buttonDeleteGarden, buttonEditGarden, buttonViewClasses, buttonRegisterGarden, OpenRegisterGarden,
                buttonChangeAffiliation, buttonViewChildren,ButtonRateGarden,ReviewsButton,buttonPhotos;
        ImageView imageViewGarten;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewGartenName);
            textViewAddress = itemView.findViewById(R.id.textViewGartenAddress);
            textViewCity = itemView.findViewById(R.id.textViewGartenCity);
            buttonDeleteGarden = itemView.findViewById(R.id.buttonDeleteGarden);
            buttonEditGarden = itemView.findViewById(R.id.buttonEditGarden);
            buttonViewClasses = itemView.findViewById(R.id.buttonViewClasses);
            buttonRegisterGarden = itemView.findViewById(R.id.buttonRegisterGarden);
            OpenRegisterGarden = itemView.findViewById(R.id.OpenRegisterGarden);
            buttonChangeAffiliation = itemView.findViewById(R.id.buttonChangeAffiliation);
            buttonViewChildren = itemView.findViewById(R.id.buttonViewAllChildren);
            imageViewGarten = itemView.findViewById(R.id.imageViewGarten);
            ButtonRateGarden = itemView.findViewById(R.id.RateGarden);
            ReviewsButton = itemView.findViewById(R.id.Reviews);
            buttonPhotos = itemView.findViewById(R.id.buttonPhoto);
            StatusText = itemView.findViewById(R.id.StatusText);
        }
    }
}
