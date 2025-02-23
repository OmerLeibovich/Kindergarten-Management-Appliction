package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalprojectapp.R;

import java.util.List;

import Objects.ChildPhoto;

/**
 * Adapter for displaying a list of ChildPhoto objects in a RecyclerView.
 */
public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder> {
    private List<ChildPhoto> photosList;
    private Context context;

    /**
     * Constructor for PhotosAdapter.
     *
     * @param context      The context of the application.
     * @param photosList   The list of ChildPhoto objects to be displayed.
     */
    public PhotosAdapter(Context context, List<ChildPhoto> photosList) {
        this.photosList = photosList;
        this.context = context;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        ChildPhoto photo = photosList.get(position);

        // Display the image using Glide
        Glide.with(context).load(photo.getImageURL()).into(holder.imageView);

        // Display other parameters
        holder.textViewChildId.setText("Child ID: " + photo.getChildId());
        holder.textViewClassName.setText("Class Name: " + photo.getClassName());
        holder.textViewTime.setText("Time: " + photo.getTime());
    }

    @Override
    public int getItemCount() {
        return photosList.size();
    }

    /**
     * Updates the list of photos and notifies the adapter of data changes.
     *
     * @param newPhotosList The new list of ChildPhoto objects.
     */
    public void updateList(List<ChildPhoto> newPhotosList) {
        this.photosList.clear();
        this.photosList.addAll(newPhotosList);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for holding and binding photo item views.
     */
    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewChildId;
        TextView textViewClassName;
        TextView textViewTime;

        /**
         * Constructor for PhotoViewHolder.
         *
         * @param itemView The view for a single photo item.
         */
        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewChildId = itemView.findViewById(R.id.textViewChildId);
            textViewClassName = itemView.findViewById(R.id.textViewClassName);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }
    }
}
