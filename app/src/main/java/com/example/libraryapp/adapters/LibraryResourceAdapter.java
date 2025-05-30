package com.example.libraryapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.libraryapp.R;
import com.example.libraryapp.models.LibraryResource;
import java.util.ArrayList;
import java.util.List;

public class LibraryResourceAdapter extends RecyclerView.Adapter<LibraryResourceAdapter.ResourceViewHolder> {
    private List<LibraryResource> resources = new ArrayList<>();
    private OnResourceClickListener listener;

    public interface OnResourceClickListener {
        void onResourceClick(LibraryResource resource);
    }

    public LibraryResourceAdapter(OnResourceClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library_resource, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        LibraryResource resource = resources.get(position);
        holder.bind(resource);
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    public void setResources(List<LibraryResource> resources) {
        this.resources = resources;
        notifyDataSetChanged();
    }

    class ResourceViewHolder extends RecyclerView.ViewHolder {
        private final ImageView coverImage;
        private final TextView titleText;
        private final TextView categoryText;
        private final TextView statusText;

        ResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.coverImage);
            titleText = itemView.findViewById(R.id.titleText);
            categoryText = itemView.findViewById(R.id.categoryText);
            statusText = itemView.findViewById(R.id.statusText);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onResourceClick(resources.get(position));
                }
            });
        }

        void bind(LibraryResource resource) {
            titleText.setText(resource.getTitle());
            categoryText.setText(resource.getCategory());
            statusText.setText(resource.getStatus());

            // Load cover image using Glide with error handling
            RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_no_photo)
                .error(R.drawable.ic_no_photo)
                .centerCrop();

            if (resource.getCoverImage() != null && !resource.getCoverImage().isEmpty()) {
                Glide.with(coverImage.getContext())
                    .load(resource.getCoverImage())
                    .apply(requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(coverImage);
            } else {
                coverImage.setImageResource(R.drawable.ic_no_photo);
            }
        }
    }
} 