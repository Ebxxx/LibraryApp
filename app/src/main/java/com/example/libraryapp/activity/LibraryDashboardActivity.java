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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.libraryapp.R;
import com.example.libraryapp.adapters.LibraryResourceAdapter;
import com.example.libraryapp.data.SupabaseClient;
import com.example.libraryapp.models.LibraryResource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LibraryDashboardActivity extends AppCompatActivity implements LibraryResourceAdapter.OnResourceClickListener {
    private RecyclerView resourcesRecyclerView;
    private LibraryResourceAdapter adapter;
    private TextInputEditText searchInput;
    private TabLayout tabLayout;
    private FloatingActionButton filterFab;
    private SupabaseClient supabaseClient;
    private String currentCategory = "All";
    private String userName;
    private String userRole;
    private int userId;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

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
        tabLayout = findViewById(R.id.tabLayout);
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
                searchResources(s.toString());
            }
        });

        // Setup tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategory = tab.getText().toString();
                if (currentCategory.equals("All")) {
                    loadAllResources();
                } else {
                    loadResourcesByCategory(currentCategory);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Setup filter FAB
        filterFab.setOnClickListener(v -> showFilterDialog());

        // Load initial data
        loadAllResources();
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
        CompletableFuture<List<LibraryResource>> future = supabaseClient.getAllLibraryResources();
        future.thenAccept(resources -> {
            runOnUiThread(() -> {
                if (resources != null) {
                    adapter.setResources(resources);
                } else {
                    Toast.makeText(this, "Failed to load resources", Toast.LENGTH_SHORT).show();
                }
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> 
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
            return null;
        });
    }

    private void loadResourcesByCategory(String category) {
        CompletableFuture<List<LibraryResource>> future = supabaseClient.getAllLibraryResources();
        future.thenAccept(resources -> {
            runOnUiThread(() -> {
                if (resources != null) {
                    // Filter resources by category
                    List<LibraryResource> filteredResources = resources.stream()
                        .filter(resource -> resource.getCategory().equalsIgnoreCase(category))
                        .collect(Collectors.toList());
                    adapter.setResources(filteredResources);
                } else {
                    Toast.makeText(this, "Failed to load resources", Toast.LENGTH_SHORT).show();
                }
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> 
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
            return null;
        });
    }

    private void searchResources(String query) {
        if (query.isEmpty()) {
            if (currentCategory.equals("All")) {
                loadAllResources();
            } else {
                loadResourcesByCategory(currentCategory);
            }
            return;
        }

        CompletableFuture<List<LibraryResource>> future = supabaseClient.searchLibraryResources(query);
        future.thenAccept(searchResults -> {
            runOnUiThread(() -> {
                if (searchResults != null) {
                    // Apply category filter if not "All"
                    final List<LibraryResource> filteredResults;
                    if (!currentCategory.equals("All")) {
                        filteredResults = searchResults.stream()
                            .filter(resource -> resource.getCategory().equalsIgnoreCase(currentCategory))
                            .collect(Collectors.toList());
                    } else {
                        filteredResults = searchResults;
                    }
                    adapter.setResources(filteredResults);
                }
            });
        }).exceptionally(e -> {
            runOnUiThread(() -> 
                Toast.makeText(this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
            return null;
        });
    }

    private void showFilterDialog() {
        // TODO: Implement filter dialog
        Toast.makeText(this, "Filter functionality coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResourceClick(LibraryResource resource) {
        // TODO: Navigate to resource details
        Toast.makeText(this, "Selected: " + resource.getTitle(), Toast.LENGTH_SHORT).show();
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
            if (currentCategory.equals("All")) {
                loadAllResources();
            } else {
                loadResourcesByCategory(currentCategory);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 