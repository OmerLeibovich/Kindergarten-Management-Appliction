package Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Objects.GardenStaff;

/**
 * Adapter for displaying a list of GardenStaff objects in a RecyclerView.
 */
public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {
    private List<GardenStaff> staffList;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private OnItemClickListener listener;

    /**
     * Interface for handling item click events.
     */
    public interface OnItemClickListener {
        void onUpdateClick(GardenStaff staff);
        void onRemoveClick(GardenStaff staff);
        void onAddClick(GardenStaff staff);
        void onAddCourseClick(GardenStaff staff);
    }

    /**
     * Constructor for StaffAdapter.
     *
     * @param staffList The list of GardenStaff objects to be displayed.
     * @param listener  The listener for item click events.
     */
    public StaffAdapter(List<GardenStaff> staffList, OnItemClickListener listener) {
        this.staffList = staffList;
        this.listener = listener;
    }

    /**
     * Updates the list of staff and notifies the adapter of data changes.
     *
     * @param newList The new list of GardenStaff objects.
     */
    public void updateList(List<GardenStaff> newList) {
        staffList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        GardenStaff staff = staffList.get(position);
        Log.d("StaffAdapter", "Binding staff: " + staff.getName());
        holder.bind(staff, listener);
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    /**
     * ViewHolder class for holding and binding staff item views.
     */
    static class StaffViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView emailTextView;
        private TextView roleTextView;
        private TextView startToWorkTextView;
        private TextView gartenTextView;
        private Button updateButton;
        private Button removeButton;
        private Button addButton;
        private Button addCourseButton;

        /**
         * Constructor for StaffViewHolder.
         *
         * @param itemView The view for a single staff item.
         */
        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            roleTextView = itemView.findViewById(R.id.roleTextView);
            startToWorkTextView = itemView.findViewById(R.id.startToWorkTextView);
            gartenTextView = itemView.findViewById(R.id.gartenTextView);
            updateButton = itemView.findViewById(R.id.updateButton);
            removeButton = itemView.findViewById(R.id.removeButton);
            addButton = itemView.findViewById(R.id.addButton);
            addCourseButton = itemView.findViewById(R.id.addCourseButton);
        }

        /**
         * Binds the GardenStaff data to the views and sets up click listeners.
         *
         * @param staff   The GardenStaff object to be displayed.
         * @param listener The listener for item click events.
         */
        public void bind(GardenStaff staff, OnItemClickListener listener) {
            nameTextView.setText(staff.getName());
            emailTextView.setText(staff.getEmail());
            roleTextView.setText(staff.getRole());
            if (staff.getStartToWork() != null) {
                startToWorkTextView.setText(dateFormat.format(staff.getStartToWork()));
            } else {
                startToWorkTextView.setText("N/A");
            }
            if (staff.getGarten() != null) {
                gartenTextView.setText(staff.getGarten().getName());
                updateButton.setVisibility(View.VISIBLE);
                removeButton.setVisibility(View.VISIBLE);
                addButton.setVisibility(View.GONE);
                addCourseButton.setVisibility(View.VISIBLE);
            } else {
                gartenTextView.setText("None");
                updateButton.setVisibility(View.GONE);
                removeButton.setVisibility(View.GONE);
                addButton.setVisibility(View.VISIBLE);
                addCourseButton.setVisibility(View.GONE);
            }

            updateButton.setOnClickListener(v -> listener.onUpdateClick(staff));
            removeButton.setOnClickListener(v -> listener.onRemoveClick(staff));
            addButton.setOnClickListener(v -> listener.onAddClick(staff));
            addCourseButton.setOnClickListener(v -> listener.onAddCourseClick(staff));
        }
    }
}
