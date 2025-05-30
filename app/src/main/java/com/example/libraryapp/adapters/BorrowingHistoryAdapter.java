package com.example.libraryapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.libraryapp.R;
import com.example.libraryapp.models.Borrowing;
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
        private final TextView titleText;
        private final TextView categoryText;
        private final TextView borrowDateText;
        private final TextView dueDateText;
        private final TextView returnDateText;
        private final TextView statusText;

        BorrowingViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            categoryText = itemView.findViewById(R.id.categoryText);
            borrowDateText = itemView.findViewById(R.id.borrowDateText);
            dueDateText = itemView.findViewById(R.id.dueDateText);
            returnDateText = itemView.findViewById(R.id.returnDateText);
            statusText = itemView.findViewById(R.id.statusText);
        }

        void bind(Borrowing borrowing) {
            // Get resource title from the joined data or use placeholder
            String resourceTitle = "Library Resource";
            if (borrowing.getResource() != null && borrowing.getResource().getTitle() != null) {
                resourceTitle = borrowing.getResource().getTitle();
            }
            titleText.setText(resourceTitle);
            
            // Get resource category
            String category = "Unknown";
            if (borrowing.getResource() != null && borrowing.getResource().getCategory() != null) {
                category = capitalizeFirst(borrowing.getResource().getCategory());
            }
            categoryText.setText(category);
            
            // Format dates
            if (borrowing.getBorrowDate() != null) {
                borrowDateText.setText("Requested: " + dateFormat.format(borrowing.getBorrowDate()));
            } else {
                borrowDateText.setText("Request Date: N/A");
            }
            
            if (borrowing.getDueDate() != null) {
                dueDateText.setText("Due: " + dateFormat.format(borrowing.getDueDate()));
                dueDateText.setVisibility(View.VISIBLE);
            } else {
                dueDateText.setVisibility(View.GONE);
            }
            
            if (borrowing.getReturnDate() != null) {
                returnDateText.setText("Returned: " + dateFormat.format(borrowing.getReturnDate()));
                returnDateText.setVisibility(View.VISIBLE);
            } else {
                returnDateText.setVisibility(View.GONE);
            }

            // Set status with proper formatting
            String status = borrowing.getStatus() != null ? borrowing.getStatus().toUpperCase() : "UNKNOWN";
            statusText.setText(status);
            
            // Set status color based on the actual status from database
            int statusColor;
            switch (status.toLowerCase()) {
                case "pending":
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark);
                    break;
                case "active":
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_dark);
                    break;
                case "returned":
                case "completed":
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark);
                    break;
                case "rejected":
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark);
                    break;
                case "overdue":
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_light);
                    break;
                default:
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray);
                    break;
            }
            statusText.setTextColor(statusColor);
        }
        
        private String capitalizeFirst(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        }
    }
}