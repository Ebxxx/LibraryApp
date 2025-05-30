package com.example.libraryapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.libraryapp.R;
import com.example.libraryapp.adapters.LibraryResourceAdapter;
import com.example.libraryapp.data.SupabaseClient;
import com.example.libraryapp.models.BookDetails;
import com.example.libraryapp.models.Borrowing;
import com.example.libraryapp.models.LibraryResource;
import com.example.libraryapp.models.MediaDetails;
import com.example.libraryapp.models.PeriodicalDetails;
import com.example.libraryapp.models.ResourceCategory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LibraryDashboardActivity extends AppCompatActivity implements LibraryResourceAdapter.OnResourceClickListener {
    private RecyclerView resourcesRecyclerView;
    private LibraryResourceAdapter adapter;
    private TextInputEditText searchInput;
    private MaterialButton categoryFilterButton;
    private TextView categoryStatusText;
    private FloatingActionButton filterFab;
    private SupabaseClient supabaseClient;
    private String currentCategory = "All";
    private String userName;
    private String userRole;
    private int userId;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private boolean useLazyLoading = true; // Flag to toggle between approaches
    private List<LibraryResource> allResources = new ArrayList<>(); // Cache for filtering

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_dashboard);

        // Get user data from intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        userName = getIntent().getStringExtra("USER_NAME");
        userRole = getIntent().getStringExtra("USER_ROLE");

        // Initialize Supabase client
        supabaseClient = SupabaseClient.getInstance(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Welcome, " + userName);
            getSupportActionBar().setSubtitle(userRole);
        }

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Setup navigation drawer header
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
        TextView navHeaderRole = headerView.findViewById(R.id.nav_header_role);
        navHeaderName.setText(userName);
        navHeaderRole.setText(userRole);

        // Setup navigation item selection
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.nav_catalog) {
                // Already on catalog, just close drawer
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_borrowing_history) {
                Intent intent = new Intent(this, BorrowingHistoryActivity.class);
                intent.putExtra("USER_ID", userId);
                intent.putExtra("USER_NAME", userName);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Profile feature coming soon", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_logout) {
                // Logout and return to login
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            
            drawerLayout.closeDrawer(GravityCompat.START);
            return false;
        });

        // Initialize views
        resourcesRecyclerView = findViewById(R.id.resourcesRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        categoryFilterButton = findViewById(R.id.categoryFilterButton);
        categoryStatusText = findViewById(R.id.categoryStatusText);
        filterFab = findViewById(R.id.filterFab);

        // Setup RecyclerView
        adapter = new LibraryResourceAdapter(this);
        resourcesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resourcesRecyclerView.setAdapter(adapter);

        // Setup search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterResources(s.toString());
            }
        });

        // Setup category filter button
        categoryFilterButton.setOnClickListener(v -> showCategoryFilterDialog());

        // Setup filter FAB for advanced filters
        filterFab.setOnClickListener(v -> showAdvancedFilterDialog());

        // Load initial data
        loadAllResources();
        
        // Load pending requests count for status display
        loadPendingRequestsCount();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void loadAllResources() {
        if (useLazyLoading) {
            loadAllResourcesLazy();
        } else {
            loadAllResourcesWithDetails();
        }
    }

    // New lazy loading approach - load basic resources first, then enrich on-demand
    private void loadAllResourcesLazy() {
        android.util.Log.d("LibraryDashboard", "Using lazy loading approach");
        
        CompletableFuture<List<LibraryResource>> future = supabaseClient.getAllLibraryResourcesBasic();
        future.thenAccept(resources -> {
            runOnUiThread(() -> {
                if (resources != null && !resources.isEmpty()) {
                    android.util.Log.d("LibraryDashboard", "Loaded " + resources.size() + " basic resources");
                    
                    // Debug: Log first few resource IDs to verify they're being set correctly
                    for (int i = 0; i < Math.min(3, resources.size()); i++) {
                        LibraryResource resource = resources.get(i);
                        android.util.Log.d("LibraryDashboard", "Resource " + i + ": ID=" + resource.getResourceId() + 
                            ", Title=" + resource.getTitle() + ", Category=" + resource.getCategory());
                    }
                    
                    // Cache all resources for filtering
                    allResources = new ArrayList<>(resources);
                    
                    // Set basic resources first for immediate display
                    adapter.setResources(resources);
                    updateCategoryStatus();
                    
                    // Then enrich with details asynchronously
                    enrichResourcesInBackground(resources);
                } else {
                    android.util.Log.w("LibraryDashboard", "No resources found or null response");
                    Toast.makeText(this, "No resources found", Toast.LENGTH_SHORT).show();
                }
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> {
                android.util.Log.e("LibraryDashboard", "Error loading basic resources: " + e.getMessage(), e);
                Toast.makeText(this, "Error loading resources: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
            return null;
        });
    }

    // Original approach - load everything with details in one go
    private void loadAllResourcesWithDetails() {
        android.util.Log.d("LibraryDashboard", "Using original approach with details");
        
        CompletableFuture<List<LibraryResource>> future = supabaseClient.getAllLibraryResources();
        future.thenAccept(resources -> {
            runOnUiThread(() -> {
                if (resources != null) {
                    android.util.Log.d("LibraryDashboard", "Loaded " + resources.size() + " resources with details");
                    
                    // Cache all resources for filtering
                    allResources = new ArrayList<>(resources);
                    
                    adapter.setResources(resources);
                    updateCategoryStatus();
                } else {
                    Toast.makeText(this, "Failed to load resources", Toast.LENGTH_SHORT).show();
                }
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> {
                android.util.Log.e("LibraryDashboard", "Error: " + e.getMessage(), e);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
            return null;
        });
    }

    // Enrich resources with details in the background
    private void enrichResourcesInBackground(List<LibraryResource> resources) {
        CompletableFuture<List<LibraryResource>> enrichFuture = 
            supabaseClient.enrichResourcesWithDetailsOnDemand(resources);
        
        enrichFuture.thenAccept(enrichedResources -> {
            runOnUiThread(() -> {
                android.util.Log.d("LibraryDashboard", "Successfully enriched resources with details");
                
                // Update cached resources
                allResources = new ArrayList<>(enrichedResources);
                
                // Apply current filters to the enriched data
                filterResources(searchInput.getText().toString());
            });
        }).exceptionally(e -> {
            android.util.Log.w("LibraryDashboard", "Failed to enrich resources with details: " + e.getMessage(), e);
            // Don't show error to user since basic resources are already displayed
            return null;
        });
    }

    private void showCategoryFilterDialog() {
        String[] categories = {"All Categories", "Books", "Periodicals", "Media"};
        String[] categoryValues = {"All", "book", "periodical", "media"};
        
        int currentSelection = 0;
        for (int i = 0; i < categoryValues.length; i++) {
            if (categoryValues[i].equals(currentCategory)) {
                currentSelection = i;
                break;
            }
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by Category")
                .setSingleChoiceItems(categories, currentSelection, (dialog, which) -> {
                    currentCategory = categoryValues[which];
                    categoryFilterButton.setText(categories[which]);
                    filterResources(searchInput.getText().toString());
                    updateCategoryStatus();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAdvancedFilterDialog() {
        // TODO: Implement advanced filter dialog for status, date range, etc.
        Toast.makeText(this, "Advanced filter functionality coming soon", Toast.LENGTH_SHORT).show();
    }

    private void filterResources(String searchQuery) {
        List<LibraryResource> filteredResources = new ArrayList<>(allResources);
        
        // Apply category filter
        if (!currentCategory.equals("All")) {
            filteredResources = filteredResources.stream()
                .filter(resource -> resource.getCategory() != null && 
                        resource.getCategory().equalsIgnoreCase(currentCategory))
                .collect(Collectors.toList());
        }
        
        // Apply search filter
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String query = searchQuery.toLowerCase().trim();
            filteredResources = filteredResources.stream()
                .filter(resource -> resource.getTitle() != null && 
                        resource.getTitle().toLowerCase().contains(query))
                .collect(Collectors.toList());
        }
        
        adapter.setResources(filteredResources);
        updateCategoryStatus();
    }

    private void updateCategoryStatus() {
        int totalCount = adapter.getItemCount();
        String categoryText;
        
        if (currentCategory.equals("All")) {
            categoryText = "Showing " + totalCount + " library resources";
                            } else {
            String categoryName = getCategoryDisplayName(currentCategory);
            categoryText = "Showing " + totalCount + " " + categoryName.toLowerCase() + " resources";
        }
        
        categoryStatusText.setText(categoryText);
    }

    private String getCategoryDisplayName(String category) {
        switch (category.toLowerCase()) {
            case "book": return "Book";
            case "periodical": return "Periodical";
            case "media": return "Media";
            default: return "All";
        }
    }

    @Override
    public void onResourceClick(LibraryResource resource) {
        // Option 1: Use new BorrowingActivity (recommended)
        openBorrowingActivity(resource);
        
        // Option 2: Use dialog approach (fallback)
        // showResourceDetailsDialog(resource);
    }
    
    private void openBorrowingActivity(LibraryResource resource) {
        android.util.Log.d("LibraryDashboard", "Opening borrowing activity for resource: " + 
            resource.toString());
        
        if (resource.getResourceId() <= 0) {
            android.util.Log.e("LibraryDashboard", "Invalid resource ID: " + resource.getResourceId());
            Toast.makeText(this, "Invalid resource selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, BorrowingActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USER_NAME", userName);
        intent.putExtra("RESOURCE_ID", resource.getResourceId());
        
        android.util.Log.d("LibraryDashboard", "Starting BorrowingActivity with RESOURCE_ID: " + resource.getResourceId());
        startActivity(intent);
    }

    private void showResourceDetailsDialog(LibraryResource resource) {
        if (resource == null) {
            Toast.makeText(this, "Resource details not available", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Create custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_resource_details, null);
        
        // Find views in the dialog
        TextView titleText = dialogView.findViewById(R.id.dialogResourceTitle);
        TextView categoryText = dialogView.findViewById(R.id.dialogResourceCategory);
        TextView statusText = dialogView.findViewById(R.id.dialogResourceStatus);
        TextView accessionText = dialogView.findViewById(R.id.dialogResourceAccession);
        TextView detailsText = dialogView.findViewById(R.id.dialogResourceDetails);
        MaterialButton requestButton = dialogView.findViewById(R.id.dialogRequestButton);
        
        // Set basic resource information
        titleText.setText(resource.getTitle());
        categoryText.setText(getCategoryDisplayName(resource.getCategory()));
        statusText.setText(resource.getStatus() != null ? resource.getStatus() : "Available");
        accessionText.setText("Accession: " + (resource.getAccessionNumber() != null ? resource.getAccessionNumber() : "N/A"));
        
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
            if (book.getPublicationDate() != null) {
                details.append("• Publication Date: ").append(
                    new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                        .format(book.getPublicationDate())).append("\n");
            }
            
        } else if ("periodical".equals(category) && resource.getPeriodicalDetails() != null) {
            PeriodicalDetails periodical = resource.getPeriodicalDetails();
            details.append("📰 Periodical Details:\n");
            if (periodical.getVolume() != null) details.append("• Volume: ").append(periodical.getVolume()).append("\n");
            if (periodical.getIssue() != null) details.append("• Issue: ").append(periodical.getIssue()).append("\n");
            if (periodical.getIssn() != null) details.append("• ISSN: ").append(periodical.getIssn()).append("\n");
            if (periodical.getPublicationDate() != null) {
                details.append("• Publication Date: ").append(
                    new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                        .format(periodical.getPublicationDate())).append("\n");
            }
            
        } else if ("media".equals(category) && resource.getMediaDetails() != null) {
            MediaDetails media = resource.getMediaDetails();
            details.append("🎬 Media Details:\n");
            if (media.getFormat() != null) details.append("• Format: ").append(media.getFormat()).append("\n");
            if (media.getMediaType() != null) details.append("• Type: ").append(media.getMediaType()).append("\n");
            if (media.getRuntime() != null && media.getRuntime() > 0) {
                details.append("• Runtime: ").append(media.getRuntime()).append(" minutes\n");
            }
        } else {
            details.append("Details are being loaded...\n");
        }
        
        // Add creation date if available
        if (resource.getCreatedAt() != null) {
            details.append("\n📅 Added to library: ").append(
                new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                    .format(resource.getCreatedAt()));
        }
        
        detailsText.setText(details.toString());
        
        // Set up request button
        boolean isAvailable = resource.getStatus() == null || 
                             "available".equalsIgnoreCase(resource.getStatus()) ||
                             "borrowed".equalsIgnoreCase(resource.getStatus());
        
        if (isAvailable) {
            // Check if user already has a pending request for this resource
            checkExistingRequestAndSetupButton(resource, requestButton, builder);
                    } else {
            requestButton.setText("Not Available");
            requestButton.setEnabled(false);
        }
        
        builder.setView(dialogView)
               .setTitle("Resource Details")
               .setNegativeButton("Close", null)
               .show();
    }

    private void checkExistingRequestAndSetupButton(LibraryResource resource, MaterialButton requestButton, AlertDialog.Builder builder) {
        // Show loading state on button
        requestButton.setText("Checking...");
        requestButton.setEnabled(false);
        
        // Check for existing borrowing request
        CompletableFuture<Boolean> existingRequestFuture = supabaseClient.checkExistingBorrowingRequest(userId, resource.getResourceId());
        
        existingRequestFuture.thenAccept(hasExistingRequest -> {
            runOnUiThread(() -> {
                if (hasExistingRequest) {
                    // User already has a pending request
                    requestButton.setText("Request Pending");
                    requestButton.setEnabled(false);
                    requestButton.setOnClickListener(v -> {
                        Toast.makeText(this, "You already have a pending request for this resource", Toast.LENGTH_LONG).show();
                    });
                } else {
                    // No existing request, allow new request
                    requestButton.setText("Request to Borrow");
                    requestButton.setEnabled(true);
                    requestButton.setOnClickListener(v -> {
                        handleResourceRequest(resource);
                        // Close dialog after request
                        AlertDialog dialog = builder.create();
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    });
                }
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> {
                // On error, allow request but show warning
                requestButton.setText("Request to Borrow");
                requestButton.setEnabled(true);
                requestButton.setOnClickListener(v -> {
                    handleResourceRequest(resource);
                    // Close dialog after request
                    AlertDialog dialog = builder.create();
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                });
                
                android.util.Log.w("LibraryDashboard", "Failed to check existing request: " + e.getMessage());
            });
            return null;
        });
    }

    private void handleResourceRequest(LibraryResource resource) {
        // Create confirmation dialog for borrowing request
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);
        confirmBuilder.setTitle("Confirm Request")
                     .setMessage("Do you want to request to borrow \"" + resource.getTitle() + "\"?")
                     .setPositiveButton("Yes, Request", (dialog, which) -> {
                         createBorrowingRequest(resource);
                     })
                     .setNegativeButton("Cancel", null)
                     .show();
    }

    private void createBorrowingRequest(LibraryResource resource) {
        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
            .setTitle("Processing Request")
            .setMessage("Creating borrowing request...")
            .setCancelable(false)
            .create();
        loadingDialog.show();
        
        // Create the borrowing request in the database
        CompletableFuture<Borrowing> borrowingFuture = supabaseClient.createBorrowingRequest(userId, resource.getResourceId());
        
        borrowingFuture.thenAccept(borrowing -> {
            runOnUiThread(() -> {
                loadingDialog.dismiss();
                
                if (borrowing != null) {
                    // Show success dialog with details
                    showRequestSuccessDialog(resource, borrowing);
                    } else {
                    Toast.makeText(this, "Failed to create borrowing request", Toast.LENGTH_LONG).show();
                }
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> {
                loadingDialog.dismiss();
                
                String errorMessage = e.getMessage();
                if (errorMessage != null && errorMessage.contains("already have a pending request")) {
                    // Show specific message for duplicate requests
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Request Already Exists")
                           .setMessage("You already have a pending borrowing request for \"" + resource.getTitle() + "\".")
                           .setPositiveButton("OK", null)
                           .setNeutralButton("View My Requests", (dialog, which) -> {
                               // TODO: Navigate to borrowing history
                               Toast.makeText(this, "Borrowing history feature coming soon", Toast.LENGTH_SHORT).show();
                           })
                           .show();
                } else {
                    // Show generic error message
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Request Failed")
                           .setMessage("Failed to create borrowing request: " + errorMessage)
                           .setPositiveButton("OK", null)
                           .setNeutralButton("Retry", (dialog, which) -> {
                               createBorrowingRequest(resource);
                           })
                           .show();
                }
                
                android.util.Log.e("LibraryDashboard", "Error creating borrowing request: " + errorMessage, e);
            });
            return null;
        });
    }

    private void showRequestSuccessDialog(LibraryResource resource, Borrowing borrowing) {
        // Format the due date
        String dueDateStr = "Not set";
        if (borrowing.getDueDate() != null) {
            dueDateStr = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                .format(borrowing.getDueDate());
        }
        
        String message = "Your borrowing request has been submitted successfully!\n\n" +
                        "Resource: " + resource.getTitle() + "\n" +
                        "Request ID: " + borrowing.getBorrowingId() + "\n" +
                        "Status: " + borrowing.getStatus().toUpperCase() + "\n" +
                        "Due Date: " + dueDateStr + "\n\n" +
                        "You will be notified when your request is approved by the librarian.";
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ Request Submitted")
               .setMessage(message)
               .setPositiveButton("OK", null)
               .setNeutralButton("View My Requests", (dialog, which) -> {
                   // TODO: Navigate to borrowing history
                   Toast.makeText(this, "Borrowing history feature coming soon", Toast.LENGTH_SHORT).show();
               })
               .show();
        
        // Log the successful request
        android.util.Log.d("LibraryDashboard", 
            "Borrowing request created successfully - ID: " + borrowing.getBorrowingId() + 
            ", User: " + userId + ", Resource: " + resource.getResourceId() + 
            " (" + resource.getTitle() + ")");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_library_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
                loadAllResources();
            loadPendingRequestsCount(); // Also refresh pending count
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh pending requests count when returning to dashboard
        loadPendingRequestsCount();
    }

    private void loadPendingRequestsCount() {
        CompletableFuture<Integer> pendingCountFuture = supabaseClient.getUserPendingRequestsCount(userId);
        
        pendingCountFuture.thenAccept(count -> {
            runOnUiThread(() -> {
                if (count > 0) {
                    // Update toolbar subtitle to show pending requests
                    String subtitle = userRole + " • " + count + " pending request" + (count == 1 ? "" : "s");
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setSubtitle(subtitle);
                    }
                    
                    android.util.Log.d("LibraryDashboard", "User has " + count + " pending borrowing requests");
                }
            });
        }).exceptionally(e -> {
            android.util.Log.w("LibraryDashboard", "Could not load pending requests count: " + e.getMessage());
            return null;
        });
    }
} 