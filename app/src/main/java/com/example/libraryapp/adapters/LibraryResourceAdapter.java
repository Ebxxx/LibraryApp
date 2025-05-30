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
import com.example.libraryapp.models.BookDetails;
import com.example.libraryapp.models.PeriodicalDetails;
import com.example.libraryapp.models.MediaDetails;
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
            // Validate resource
            if (resource == null) {
                android.util.Log.e("LibraryResourceAdapter", "Resource is null!");
                return;
            }
            
            if (resource.getCategory() == null || resource.getCategory().isEmpty()) {
                android.util.Log.e("LibraryResourceAdapter", "Resource category is null or empty for: " + resource.getTitle());
                return;
            }
            
            String category = resource.getCategory().trim();
            android.util.Log.d("LibraryResourceAdapter", "Binding resource: " + resource.getTitle() + " with category: '" + category + "'");
            
            titleText.setText(resource.getTitle());
            statusText.setText(resource.getStatus());

            // Build category-specific details
            StringBuilder detailsBuilder = new StringBuilder();
            detailsBuilder.append(category);
            
            // Display specific details based on category
            if ("book".equals(category)) {
                if (resource.getBookDetails() != null) {
                    BookDetails book = resource.getBookDetails();
                    android.util.Log.d("LibraryResourceAdapter", "Displaying book details for: " + resource.getTitle());
                    
                    if (book.getAuthor() != null && !book.getAuthor().isEmpty()) {
                        detailsBuilder.append(" • ").append(book.getAuthor());
                    }
                    if (book.getPublisher() != null && !book.getPublisher().isEmpty()) {
                        detailsBuilder.append(" • ").append(book.getPublisher());
                    }
                    if (book.getEdition() != null && !book.getEdition().isEmpty()) {
                        detailsBuilder.append(" • ").append(book.getEdition());
                    }
                } else {
                    android.util.Log.d("LibraryResourceAdapter", "Book details not yet loaded for: " + resource.getTitle());
                    detailsBuilder.append(" • Loading details...");
                }
                
            } else if ("periodical".equals(category)) {
                if (resource.getPeriodicalDetails() != null) {
                    PeriodicalDetails periodical = resource.getPeriodicalDetails();
                    android.util.Log.d("LibraryResourceAdapter", "Displaying periodical details for: " + resource.getTitle());
                    
                    if (periodical.getVolume() != null && !periodical.getVolume().isEmpty()) {
                        detailsBuilder.append(" • Vol. ").append(periodical.getVolume());
                    }
                    if (periodical.getIssue() != null && !periodical.getIssue().isEmpty()) {
                        detailsBuilder.append(" • Issue ").append(periodical.getIssue());
                    }
                    if (periodical.getIssn() != null && !periodical.getIssn().isEmpty()) {
                        detailsBuilder.append(" • ISSN: ").append(periodical.getIssn());
                    }
                } else {
                    android.util.Log.d("LibraryResourceAdapter", "Periodical details not yet loaded for: " + resource.getTitle());
                    detailsBuilder.append(" • Loading details...");
                }
                
            } else if ("media".equals(category)) {
                if (resource.getMediaDetails() != null) {
                    MediaDetails media = resource.getMediaDetails();
                    android.util.Log.d("LibraryResourceAdapter", "Displaying media details for: " + resource.getTitle());
                    
                    if (media.getFormat() != null && !media.getFormat().isEmpty()) {
                        detailsBuilder.append(" • ").append(media.getFormat());
                    }
                    if (media.getMediaType() != null && !media.getMediaType().isEmpty()) {
                        detailsBuilder.append(" • ").append(media.getMediaType());
                    }
                    if (media.getRuntime() != null && media.getRuntime() > 0) {
                        detailsBuilder.append(" • ").append(media.getRuntime()).append(" min");
                    }
                } else {
                    android.util.Log.d("LibraryResourceAdapter", "Media details not yet loaded for: " + resource.getTitle());
                    detailsBuilder.append(" • Loading details...");
                }
                
            } else {
                android.util.Log.e("LibraryResourceAdapter", "Unknown category: '" + category + "' for resource: " + resource.getTitle());
                detailsBuilder.append(" • Unknown category type");
            }
            
            categoryText.setText(detailsBuilder.toString());

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