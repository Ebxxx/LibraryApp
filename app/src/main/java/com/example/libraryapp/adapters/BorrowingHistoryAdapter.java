package com.example.libraryapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.libraryapp.R;
import com.example.libraryapp.models.Borrowing;
import com.example.libraryapp.models.BookDetails;
import com.example.libraryapp.models.MediaDetails;
import com.example.libraryapp.models.PeriodicalDetails;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BorrowingHistoryAdapter extends RecyclerView.Adapter<BorrowingHistoryAdapter.BorrowingViewHolder> {
    private List<Borrowing> borrowings = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @NonNull
    @Override
    public BorrowingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_borrowing_history, parent, false);
        return new BorrowingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BorrowingViewHolder holder, int position) {
        Borrowing borrowing = borrowings.get(position);
        holder.bind(borrowing);
    }

    @Override
    public int getItemCount() {
        return borrowings.size();
    }

    public void setBorrowings(List<Borrowing> borrowings) {
        this.borrowings = borrowings;
        notifyDataSetChanged();
    }

    class BorrowingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView resourceCoverImage;
        private final TextView titleText;
        private final TextView categoryText;
        private final TextView resourceDetailsText;
        private final TextView accessionText;
        private final TextView borrowDateText;
        private final TextView dueDateText;
        private final TextView returnDateText;
        private final TextView requestIdText;
        private final TextView statusText;

        BorrowingViewHolder(@NonNull View itemView) {
            super(itemView);
            resourceCoverImage = itemView.findViewById(R.id.resourceCoverImage);
            titleText = itemView.findViewById(R.id.titleText);
            categoryText = itemView.findViewById(R.id.categoryText);
            resourceDetailsText = itemView.findViewById(R.id.resourceDetailsText);
            accessionText = itemView.findViewById(R.id.accessionText);
            borrowDateText = itemView.findViewById(R.id.borrowDateText);
            dueDateText = itemView.findViewById(R.id.dueDateText);
            returnDateText = itemView.findViewById(R.id.returnDateText);
            requestIdText = itemView.findViewById(R.id.requestIdText);
            statusText = itemView.findViewById(R.id.statusText);
        }

        void bind(Borrowing borrowing) {
            // Get resource information
            String resourceTitle = "Library Resource";
            String category = "Unknown";
            String accessionNumber = "N/A";
            String coverImageUrl = null;
            
            if (borrowing.getResource() != null) {
                if (borrowing.getResource().getTitle() != null) {
                    resourceTitle = borrowing.getResource().getTitle();
                }
                if (borrowing.getResource().getCategory() != null) {
                    category = capitalizeFirst(borrowing.getResource().getCategory());
                }
                if (borrowing.getResource().getAccessionNumber() != null) {
                    accessionNumber = borrowing.getResource().getAccessionNumber();
                }
                coverImageUrl = borrowing.getResource().getCoverImage();
            }
            
            // Set basic resource information
            titleText.setText(resourceTitle);
            categoryText.setText("📚 " + category);
            accessionText.setText("Accession: " + accessionNumber);
            
            // Load cover image
            loadCoverImage(coverImageUrl);
            
            // Set category-specific resource details
            String resourceDetails = buildResourceDetails(borrowing);
            resourceDetailsText.setText(resourceDetails);
            
            // Format dates
            formatDates(borrowing);
            
            // Set status
            formatStatus(borrowing);
            
            // Set request ID
            if (borrowing.getBorrowingId() != null) {
                requestIdText.setText("Request #" + borrowing.getBorrowingId());
                requestIdText.setVisibility(View.VISIBLE);
            } else {
                requestIdText.setVisibility(View.GONE);
            }
        }
        
        private void loadCoverImage(String coverImageUrl) {
            RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_no_photo)
                .error(R.drawable.ic_no_photo)
                .centerCrop();

            if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
                Glide.with(resourceCoverImage.getContext())
                    .load(coverImageUrl)
                    .apply(requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(resourceCoverImage);
            } else {
                resourceCoverImage.setImageResource(R.drawable.ic_no_photo);
            }
        }
        
        private String buildResourceDetails(Borrowing borrowing) {
            if (borrowing.getResource() == null) {
                return "Loading details...";
            }
            
            StringBuilder details = new StringBuilder();
            String category = borrowing.getResource().getCategory();
            
            if ("book".equals(category) && borrowing.getResource().getBookDetails() != null) {
                BookDetails book = borrowing.getResource().getBookDetails();
                
                if (book.getAuthor() != null && !book.getAuthor().isEmpty()) {
                    details.append("👤 ").append(book.getAuthor());
                }
                if (book.getPublisher() != null && !book.getPublisher().isEmpty()) {
                    if (details.length() > 0) details.append(" • ");
                    details.append("🏢 ").append(book.getPublisher());
                }
                if (book.getEdition() != null && !book.getEdition().isEmpty()) {
                    if (details.length() > 0) details.append(" • ");
                    details.append("📖 ").append(book.getEdition());
                }
                if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                    if (details.length() > 0) details.append("\n");
                    details.append("ISBN: ").append(book.getIsbn());
                }
                
            } else if ("periodical".equals(category) && borrowing.getResource().getPeriodicalDetails() != null) {
                PeriodicalDetails periodical = borrowing.getResource().getPeriodicalDetails();
                
                if (periodical.getVolume() != null && !periodical.getVolume().isEmpty()) {
                    details.append("📊 Vol. ").append(periodical.getVolume());
                }
                if (periodical.getIssue() != null && !periodical.getIssue().isEmpty()) {
                    if (details.length() > 0) details.append(" • ");
                    details.append("📄 Issue ").append(periodical.getIssue());
                }
                if (periodical.getIssn() != null && !periodical.getIssn().isEmpty()) {
                    if (details.length() > 0) details.append("\n");
                    details.append("ISSN: ").append(periodical.getIssn());
                }
                
            } else if ("media".equals(category) && borrowing.getResource().getMediaDetails() != null) {
                MediaDetails media = borrowing.getResource().getMediaDetails();
                
                if (media.getFormat() != null && !media.getFormat().isEmpty()) {
                    details.append("💿 ").append(media.getFormat());
                }
                if (media.getMediaType() != null && !media.getMediaType().isEmpty()) {
                    if (details.length() > 0) details.append(" • ");
                    details.append("🎬 ").append(media.getMediaType());
                }
                if (media.getRuntime() != null && media.getRuntime() > 0) {
                    if (details.length() > 0) details.append(" • ");
                    details.append("⏱️ ").append(media.getRuntime()).append(" min");
                }
            } else {
                details.append("Loading details...");
            }
            
            return details.length() > 0 ? details.toString() : "No additional details available";
        }
        
        private void formatDates(Borrowing borrowing) {
            // Borrow/Request date
            if (borrowing.getBorrowDate() != null) {
                borrowDateText.setText("Requested: " + dateFormat.format(borrowing.getBorrowDate()));
                borrowDateText.setVisibility(View.VISIBLE);
            } else {
                borrowDateText.setText("Request Date: N/A");
                borrowDateText.setVisibility(View.VISIBLE);
            }
            
            // Due date
            if (borrowing.getDueDate() != null) {
                dueDateText.setText("Due: " + dateFormat.format(borrowing.getDueDate()));
                dueDateText.setVisibility(View.VISIBLE);
                
                // Check if overdue
                if (borrowing.isOverdue()) {
                    dueDateText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
                } else {
                    dueDateText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
                }
            } else {
                dueDateText.setVisibility(View.GONE);
            }
            
            // Return date
            if (borrowing.getReturnDate() != null) {
                returnDateText.setText("Returned: " + dateFormat.format(borrowing.getReturnDate()));
                returnDateText.setVisibility(View.VISIBLE);
                returnDateText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
            } else {
                returnDateText.setVisibility(View.GONE);
            }
            }

        private void formatStatus(Borrowing borrowing) {
            String status = borrowing.getStatus() != null ? borrowing.getStatus().toUpperCase() : "UNKNOWN";
            statusText.setText(status);
            
            // Set status color and background based on the actual status from database
            int statusColor;
            int backgroundColor;
            
            switch (status.toLowerCase()) {
                case "pending":
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.white);
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark);
                    break;
                case "active":
                case "approved":
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.white);
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_dark);
                    break;
                case "returned":
                case "completed":
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.white);
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark);
                    break;
                case "rejected":
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.white);
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark);
                    break;
                case "overdue":
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.white);
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_light);
                    break;
                default:
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.black);
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray);
                    break;
            }
            
            statusText.setTextColor(statusColor);
            statusText.getBackground().setTint(backgroundColor);
        }
        
        private String capitalizeFirst(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        }
    }
} 