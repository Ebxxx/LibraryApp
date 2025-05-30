package com.example.libraryapp.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.libraryapp.R;
import com.example.libraryapp.adapters.BorrowingHistoryAdapter;
import com.example.libraryapp.data.SupabaseClient;
import com.example.libraryapp.models.BorrowingHistory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BorrowingHistoryActivity extends AppCompatActivity {
    private RecyclerView borrowingRecyclerView;
    private BorrowingHistoryAdapter adapter;
    private SupabaseClient supabaseClient;
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
            getSupportActionBar().setTitle("Borrowing History");
            getSupportActionBar().setSubtitle(userName);
        }

        // Initialize Supabase client
        supabaseClient = SupabaseClient.getInstance(this);

        // Setup RecyclerView
        borrowingRecyclerView = findViewById(R.id.borrowingRecyclerView);
        adapter = new BorrowingHistoryAdapter();
        borrowingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        borrowingRecyclerView.setAdapter(adapter);

        // Load borrowing history
        loadBorrowingHistory();
    }

    private void loadBorrowingHistory() {
        // For now, we'll show sample data since we haven't implemented the API yet
        List<BorrowingHistory> sampleHistory = createSampleBorrowingHistory();
        adapter.setBorrowingHistory(sampleHistory);
    }

    private List<BorrowingHistory> createSampleBorrowingHistory() {
        List<BorrowingHistory> history = new ArrayList<>();
        
        // Sample data
        Date today = new Date();
        Date week_ago = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
        Date month_ago = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000);
        Date two_weeks_later = new Date(today.getTime() + 14 * 24 * 60 * 60 * 1000);
        
        history.add(new BorrowingHistory(1, userId, 101, "Introduction to Java Programming", 
                "Book", week_ago, two_weeks_later, null, "ACTIVE"));
        
        history.add(new BorrowingHistory(2, userId, 102, "Data Structures and Algorithms", 
                "Book", month_ago, new Date(month_ago.getTime() + 14 * 24 * 60 * 60 * 1000), 
                new Date(month_ago.getTime() + 10 * 24 * 60 * 60 * 1000), "RETURNED"));
        
        history.add(new BorrowingHistory(3, userId, 103, "Computer Networks Documentary", 
                "Media", new Date(month_ago.getTime() - 7 * 24 * 60 * 60 * 1000), 
                new Date(month_ago.getTime()), new Date(month_ago.getTime() - 2 * 24 * 60 * 60 * 1000), "RETURNED"));

        return history;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 