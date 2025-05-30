package com.example.libraryapp.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.libraryapp.R;
import com.example.libraryapp.adapters.BorrowingHistoryAdapter;
import com.example.libraryapp.data.SupabaseClient;
import com.example.libraryapp.models.Borrowing;
import com.example.libraryapp.models.LibraryResource;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BorrowingHistoryActivity extends AppCompatActivity {
    private RecyclerView borrowingRecyclerView;
    private BorrowingHistoryAdapter adapter;
    private SupabaseClient supabaseClient;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrowing_history);

        // Get user ID from intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        String userName = getIntent().getStringExtra("USER_NAME");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Borrowing Requests");
            getSupportActionBar().setSubtitle(userName);
        }

        // Initialize Supabase client
        supabaseClient = SupabaseClient.getInstance(this);

        // Initialize views
        initializeViews();

        // Load borrowing history
        loadBorrowingHistory();
    }

    private void initializeViews() {
        borrowingRecyclerView = findViewById(R.id.borrowingRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);

        // Setup RecyclerView
        adapter = new BorrowingHistoryAdapter();
        borrowingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        borrowingRecyclerView.setAdapter(adapter);

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadBorrowingHistory);
        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        );
    }

    private void loadBorrowingHistory() {
        showLoading(true);
        
        android.util.Log.d("BorrowingHistory", "Loading borrowing history for user ID: " + userId);
        
        CompletableFuture<List<Borrowing>> borrowingsFuture = supabaseClient.getUserBorrowingRequestsWithDetails(userId);
        
        borrowingsFuture.thenAccept(borrowings -> {
            runOnUiThread(() -> {
                showLoading(false);
                
                if (borrowings != null && !borrowings.isEmpty()) {
                    android.util.Log.d("BorrowingHistory", "Loaded " + borrowings.size() + " borrowing records");
                    
                    // Debug: Log first few records to verify resource details
                    for (int i = 0; i < Math.min(3, borrowings.size()); i++) {
                        Borrowing borrowing = borrowings.get(i);
                        android.util.Log.d("BorrowingHistory", "Borrowing " + i + ": " + borrowing.toString());
                        
                        if (borrowing.getResource() != null) {
                            LibraryResource resource = borrowing.getResource();
                            android.util.Log.d("BorrowingHistory", "  Resource details: ID=" + resource.getResourceId() + 
                                ", Title=" + resource.getTitle() + ", Category=" + resource.getCategory() + 
                                ", Accession=" + resource.getAccessionNumber());
                            
                            // Check if category-specific details are loaded
                            if ("book".equals(resource.getCategory()) && resource.getBookDetails() != null) {
                                android.util.Log.d("BorrowingHistory", "  Book details: Author=" + resource.getBookDetails().getAuthor());
                            } else if ("periodical".equals(resource.getCategory()) && resource.getPeriodicalDetails() != null) {
                                android.util.Log.d("BorrowingHistory", "  Periodical details: Volume=" + resource.getPeriodicalDetails().getVolume());
                            } else if ("media".equals(resource.getCategory()) && resource.getMediaDetails() != null) {
                                android.util.Log.d("BorrowingHistory", "  Media details: Format=" + resource.getMediaDetails().getFormat());
                            } else {
                                android.util.Log.w("BorrowingHistory", "  Category-specific details not loaded for: " + resource.getCategory());
                            }
                        } else {
                            android.util.Log.w("BorrowingHistory", "  No resource details found for borrowing ID: " + borrowing.getBorrowingId());
                        }
                    }
                    
                    adapter.setBorrowings(borrowings);
                    showEmptyState(false);
                    
                    // Show status summary
                    showStatusSummary(borrowings);
                } else {
                    showEmptyState(true);
                    android.util.Log.d("BorrowingHistory", "No borrowing records found for user ID: " + userId);
                }
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> {
                showLoading(false);
                String errorMessage = "Error loading borrowing history: " + e.getMessage();
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                android.util.Log.e("BorrowingHistory", errorMessage, e);
                showEmptyState(true);
            });
            return null;
        });
    }

    private void showStatusSummary(List<Borrowing> borrowings) {
        int pendingCount = 0;
        int activeCount = 0;
        int returnedCount = 0;
        int rejectedCount = 0;

        for (Borrowing borrowing : borrowings) {
            String status = borrowing.getStatus();
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "pending":
                        pendingCount++;
                        break;
                    case "active":
                        activeCount++;
                        break;
                    case "returned":
                    case "completed":
                        returnedCount++;
                        break;
                    case "rejected":
                        rejectedCount++;
                        break;
                }
            }
        }

        // Update toolbar subtitle with summary
        String summary = String.format("Total: %d | Pending: %d | Active: %d", 
            borrowings.size(), pendingCount, activeCount);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(summary);
        }

        android.util.Log.d("BorrowingHistory", "Status summary - " + summary + " | Returned: " + returnedCount + " | Rejected: " + rejectedCount);
    }

    private void showLoading(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            borrowingRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showEmptyState(boolean empty) {
        if (empty) {
            borrowingRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("📚 No borrowing requests yet\n\nBrowse library resources and make your first request!");
        } else {
            borrowingRecyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadBorrowingHistory();
    }
} 