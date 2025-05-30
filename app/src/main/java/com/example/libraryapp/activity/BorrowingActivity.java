package com.example.libraryapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.libraryapp.R;
import com.example.libraryapp.data.SupabaseClient;
import com.example.libraryapp.models.Borrowing;
import com.example.libraryapp.models.LibraryResource;
import com.example.libraryapp.models.BookDetails;
import com.example.libraryapp.models.PeriodicalDetails;
import com.example.libraryapp.models.MediaDetails;
import com.google.android.material.card.MaterialCardView;
import java.util.concurrent.CompletableFuture;

public class BorrowingActivity extends AppCompatActivity {
    private SupabaseClient supabaseClient;
    private LibraryResource resource;
    private int userId;
    private String userName;
    
    // UI Components
    private ImageView resourceCoverImage;
    private TextView resourceTitle;
    private TextView resourceCategory;
    private TextView resourceStatus;
    private TextView resourceAccession;
    private TextView resourceDetails;
    private MaterialCardView resourceCard;
    private Button borrowButton;
    private Button cancelButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrowing);
        
        // Initialize Supabase client
        supabaseClient = SupabaseClient.getInstance(this);
        
        // Get data from intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        userName = getIntent().getStringExtra("USER_NAME");
        int resourceId = getIntent().getIntExtra("RESOURCE_ID", -1);
        
        android.util.Log.d("BorrowingActivity", "Received intent data - USER_ID: " + userId + 
            ", USER_NAME: " + userName + ", RESOURCE_ID: " + resourceId);
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Borrow Resource");
        }
        
        // Initialize views
        initializeViews();
        
        // Validate input
        if (userId == -1 || resourceId == -1) {
            android.util.Log.e("BorrowingActivity", "Invalid user or resource ID - USER_ID: " + userId + ", RESOURCE_ID: " + resourceId);
            Toast.makeText(this, "Invalid user or resource ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Load resource details
        loadResourceDetails(resourceId);
    }
    
    private void initializeViews() {
        resourceCoverImage = findViewById(R.id.resourceCoverImage);
        resourceTitle = findViewById(R.id.resourceTitle);
        resourceCategory = findViewById(R.id.resourceCategory);
        resourceStatus = findViewById(R.id.resourceStatus);
        resourceAccession = findViewById(R.id.resourceAccession);
        resourceDetails = findViewById(R.id.resourceDetails);
        resourceCard = findViewById(R.id.resourceCard);
        borrowButton = findViewById(R.id.borrowButton);
        cancelButton = findViewById(R.id.cancelButton);
        
        // Set button listeners
        borrowButton.setOnClickListener(v -> handleBorrowRequest());
        cancelButton.setOnClickListener(v -> finish());
    }
    
    private void loadResourceDetails(int resourceId) {
        // Show loading state
        showLoadingState(true);
        
        CompletableFuture<LibraryResource> resourceFuture = supabaseClient.getLibraryResourceById(resourceId);
        
        resourceFuture.thenAccept(loadedResource -> {
            runOnUiThread(() -> {
                if (loadedResource != null) {
                    this.resource = loadedResource;
                    displayResourceDetails();
                    validateBorrowingEligibility();
                } else {
                    Toast.makeText(this, "Resource not found", Toast.LENGTH_LONG).show();
                    finish();
                }
                showLoadingState(false);
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Error loading resource: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
                showLoadingState(false);
            });
            return null;
        });
    }
    
    private void displayResourceDetails() {
        if (resource == null) return;
        
        // Set basic resource information
        resourceTitle.setText(resource.getTitle());
        resourceCategory.setText(getCategoryDisplayName(resource.getCategory()));
        resourceStatus.setText(resource.getStatus() != null ? resource.getStatus() : "Unknown");
        resourceAccession.setText("Accession: " + (resource.getAccessionNumber() != null ? resource.getAccessionNumber() : "N/A"));
        
        // Load cover image
        if (resource.getCoverImage() != null && !resource.getCoverImage().isEmpty()) {
            Glide.with(this)
                .load(resource.getCoverImage())
                .placeholder(R.drawable.ic_no_photo)
                .error(R.drawable.ic_no_photo)
                .into(resourceCoverImage);
        } else {
            resourceCoverImage.setImageResource(R.drawable.ic_no_photo);
        }
        
        // Set category-specific details
        StringBuilder details = new StringBuilder();
        String category = resource.getCategory();
        
        if ("book".equals(category) && resource.getBookDetails() != null) {
            BookDetails book = resource.getBookDetails();
            details.append("📚 Book Details:\n");
            if (book.getAuthor() != null) details.append("• Author: ").append(book.getAuthor()).append("\n");
            if (book.getPublisher() != null) details.append("• Publisher: ").append(book.getPublisher()).append("\n");
            if (book.getEdition() != null) details.append("• Edition: ").append(book.getEdition()).append("\n");
            if (book.getIsbn() != null) details.append("• ISBN: ").append(book.getIsbn()).append("\n");
            
        } else if ("periodical".equals(category) && resource.getPeriodicalDetails() != null) {
            PeriodicalDetails periodical = resource.getPeriodicalDetails();
            details.append("📰 Periodical Details:\n");
            if (periodical.getVolume() != null) details.append("• Volume: ").append(periodical.getVolume()).append("\n");
            if (periodical.getIssue() != null) details.append("• Issue: ").append(periodical.getIssue()).append("\n");
            if (periodical.getIssn() != null) details.append("• ISSN: ").append(periodical.getIssn()).append("\n");
            
        } else if ("media".equals(category) && resource.getMediaDetails() != null) {
            MediaDetails media = resource.getMediaDetails();
            details.append("🎬 Media Details:\n");
            if (media.getFormat() != null) details.append("• Format: ").append(media.getFormat()).append("\n");
            if (media.getMediaType() != null) details.append("• Type: ").append(media.getMediaType()).append("\n");
            if (media.getRuntime() != null && media.getRuntime() > 0) {
                details.append("• Runtime: ").append(media.getRuntime()).append(" minutes\n");
            }
        } else {
            details.append("Loading details...\n");
        }
        
        resourceDetails.setText(details.toString());
    }
    
    private void validateBorrowingEligibility() {
        if (resource == null) return;
        
        // Check resource availability
        boolean isAvailable = resource.getStatus() != null && "available".equalsIgnoreCase(resource.getStatus());
        
        if (!isAvailable) {
            borrowButton.setText("Not Available");
            borrowButton.setEnabled(false);
            showStatusMessage("This resource is currently " + resource.getStatus() + " and cannot be borrowed.", false);
            return;
        }
        
        // Check if user already has a pending request
        borrowButton.setText("Checking eligibility...");
        borrowButton.setEnabled(false);
        
        CompletableFuture<Boolean> existingRequestFuture = supabaseClient.checkExistingBorrowingRequest(userId, resource.getResourceId());
        
        existingRequestFuture.thenAccept(hasExistingRequest -> {
            runOnUiThread(() -> {
                if (hasExistingRequest) {
                    borrowButton.setText("Request Pending");
                    borrowButton.setEnabled(false);
                    showStatusMessage("You already have a pending borrowing request for this resource.", false);
                } else {
                    // Check borrowing limit
                    checkBorrowingLimit();
                }
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> {
                borrowButton.setText("Request to Borrow");
                borrowButton.setEnabled(true);
                showStatusMessage("Could not verify existing requests. You may still proceed.", true);
            });
            return null;
        });
    }
    
    private void checkBorrowingLimit() {
        CompletableFuture<Boolean> borrowingLimitFuture = supabaseClient.checkUserBorrowingLimit(userId);
        
        borrowingLimitFuture.thenAccept(canBorrow -> {
            runOnUiThread(() -> {
                if (canBorrow) {
                    borrowButton.setText("Request to Borrow");
                    borrowButton.setEnabled(true);
                    showStatusMessage("This resource is available for borrowing.", true);
                } else {
                    borrowButton.setText("Borrowing Limit Reached");
                    borrowButton.setEnabled(false);
                    showStatusMessage("You have reached your maximum borrowing limit. Please return some items first.", false);
                }
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> {
                borrowButton.setText("Request to Borrow");
                borrowButton.setEnabled(true);
                showStatusMessage("Could not verify borrowing limit. You may still proceed.", true);
            });
            return null;
        });
    }
    
    private void handleBorrowRequest() {
        if (resource == null) return;
        
        // Show confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Borrowing Request")
               .setMessage("Do you want to request to borrow \"" + resource.getTitle() + "\"?\n\n" +
                          "This will create a pending request that needs librarian approval.")
               .setPositiveButton("Yes, Request", (dialog, which) -> createBorrowingRequest())
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    private void createBorrowingRequest() {
        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
            .setTitle("Processing Request")
            .setMessage("Creating borrowing request...")
            .setCancelable(false)
            .create();
        loadingDialog.show();
        
        // Create the borrowing request
        CompletableFuture<Borrowing> borrowingFuture = supabaseClient.createBorrowingRequest(userId, resource.getResourceId());
        
        borrowingFuture.thenAccept(borrowing -> {
            runOnUiThread(() -> {
                loadingDialog.dismiss();
                
                if (borrowing != null) {
                    showSuccessDialog(borrowing);
                } else {
                    Toast.makeText(this, "Failed to create borrowing request", Toast.LENGTH_LONG).show();
                }
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> {
                loadingDialog.dismiss();
                
                String errorMessage = e.getMessage();
                AlertDialog.Builder errorBuilder = new AlertDialog.Builder(this);
                errorBuilder.setTitle("Request Failed")
                           .setMessage("Failed to create borrowing request:\n\n" + errorMessage)
                           .setPositiveButton("OK", null)
                           .setNeutralButton("Retry", (dialog, which) -> createBorrowingRequest())
                           .show();
                
                android.util.Log.e("BorrowingActivity", "Error creating borrowing request: " + errorMessage, e);
            });
            return null;
        });
    }
    
    private void showSuccessDialog(Borrowing borrowing) {
        String dueDateStr = "Not set";
        if (borrowing.getDueDate() != null) {
            dueDateStr = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                .format(borrowing.getDueDate());
        }
        
        String message = "Your borrowing request has been submitted successfully!\n\n" +
                        "📖 Resource: " + resource.getTitle() + "\n" +
                        "🆔 Request ID: " + borrowing.getBorrowingId() + "\n" +
                        "📅 Due Date: " + dueDateStr + "\n" +
                        "👤 Status: " + borrowing.getStatus().toUpperCase() + "\n\n" +
                        "You will be notified when your request is approved by the librarian.";
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ Request Submitted Successfully")
               .setMessage(message)
               .setPositiveButton("OK", (dialog, which) -> {
                   // Return to dashboard
                   Intent intent = new Intent(this, LibraryDashboardActivity.class);
                   intent.putExtra("USER_ID", userId);
                   intent.putExtra("USER_NAME", userName);
                   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   startActivity(intent);
                   finish();
               })
               .setCancelable(false)
               .show();
    }
    
    private void showLoadingState(boolean loading) {
        if (loading) {
            resourceCard.setVisibility(View.GONE);
            borrowButton.setEnabled(false);
            cancelButton.setEnabled(false);
        } else {
            resourceCard.setVisibility(View.VISIBLE);
            cancelButton.setEnabled(true);
        }
    }
    
    private void showStatusMessage(String message, boolean positive) {
        // You could implement a status message view here
        // For now, we'll just log it
        android.util.Log.d("BorrowingActivity", "Status: " + message);
    }
    
    private String getCategoryDisplayName(String category) {
        if (category == null) return "Unknown";
        switch (category.toLowerCase()) {
            case "book": return "Book";
            case "periodical": return "Periodical";
            case "media": return "Media";
            default: return category;
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
} 