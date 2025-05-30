package com.example.libraryapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.libraryapp.R;
import com.example.libraryapp.models.BorrowingHistory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BorrowingHistoryAdapter extends RecyclerView.Adapter<BorrowingHistoryAdapter.BorrowingViewHolder> {
    private List<BorrowingHistory> borrowingHistory = new ArrayList<>();
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
        BorrowingHistory history = borrowingHistory.get(position);
        holder.bind(history);
    }

    @Override
    public int getItemCount() {
        return borrowingHistory.size();
    }

    public void setBorrowingHistory(List<BorrowingHistory> borrowingHistory) {
        this.borrowingHistory = borrowingHistory;
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

        void bind(BorrowingHistory history) {
            titleText.setText(history.getResourceTitle());
            categoryText.setText(history.getResourceCategory());
            borrowDateText.setText("Borrowed: " + dateFormat.format(history.getBorrowDate()));
            dueDateText.setText("Due: " + dateFormat.format(history.getDueDate()));
            
            if (history.getReturnDate() != null) {
                returnDateText.setText("Returned: " + dateFormat.format(history.getReturnDate()));
                returnDateText.setVisibility(View.VISIBLE);
            } else {
                returnDateText.setVisibility(View.GONE);
            }

            statusText.setText(history.getStatus());
            
            // Set status color
            int statusColor;
            switch (history.getStatus()) {
                case "ACTIVE":
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_dark);
                    break;
                case "RETURNED":
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark);
                    break;
                case "OVERDUE":
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark);
                    break;
                default:
                    statusColor = ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray);
                    break;
            }
            statusText.setTextColor(statusColor);
        }
    }
} 