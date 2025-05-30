package com.example.libraryapp.data;

import android.content.Context;
import com.example.libraryapp.core.config.SupabaseConfig;
import com.example.libraryapp.models.User;
import com.example.libraryapp.models.LibraryResource;
import com.example.libraryapp.utils.PasswordUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.mindrot.jbcrypt.BCrypt;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import com.example.libraryapp.models.BookDetails;
import com.example.libraryapp.models.PeriodicalDetails;
import com.example.libraryapp.models.MediaDetails;
import com.example.libraryapp.models.Borrowing;
import java.util.HashMap;
import java.util.Map;

public class SupabaseClient {
    private static SupabaseClient instance;
    private final OkHttpClient client;
    private final Gson gson;
    private final Gson borrowingGson; // Special Gson for borrowing operations
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final String supabaseUrl;
    private final String supabaseKey;

    private SupabaseClient(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        client = new OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        
        // Standard Gson instance with date handling
        gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();
        
        // Special Gson instance for borrowing operations with proper date formatting
        borrowingGson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create();
        
        this.supabaseUrl = "YOUR_SUPABASE_URL";
        this.supabaseKey = "YOUR_SUPABASE_KEY";
    }

    public static synchronized SupabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseClient(context.getApplicationContext());
        }
        return instance;
    }

    public CompletableFuture<User> loginUser(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // First get the user by username only
                String url = SupabaseConfig.getUrl() + "/rest/v1/users?username=eq." + username + "&select=*";
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Login failed: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<User>>(){}.getType();
                    List<User> users = gson.fromJson(responseBody, listType);

                    if (users == null || users.isEmpty()) {
                        throw new RuntimeException("Login failed: Invalid username or password");
                    }

                    User user = users.get(0);
                    
                    // Verify the password using our improved PasswordUtils
                    if (!PasswordUtils.verifyPassword(password, user.getPassword())) {
                        throw new RuntimeException("Login failed: Invalid username or password");
                    }

                    // Validate user role
                    if (!user.isValidRole()) {
                        throw new IllegalStateException("Access denied: Only students and faculty members can login");
                    }

                    return user;
                }
            } catch (IOException e) {
                throw new RuntimeException("Login failed: Network error - " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Login failed: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<User> getUserByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/users?username=eq." + username + "&select=*";
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to get user: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<User>>(){}.getType();
                    List<User> users = gson.fromJson(responseBody, listType);

                    if (users == null || users.isEmpty()) {
                        throw new RuntimeException("User not found");
                    }

                    return users.get(0);
                }
            } catch (IOException e) {
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<List<User>> getAllUsers() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/users?select=*";
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getServiceKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getServiceKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to get users: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<User>>(){}.getType();
                    return gson.fromJson(responseBody, listType);
                }
            } catch (IOException e) {
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<User> createUser(User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/users";
                String json = gson.toJson(user);
                
                RequestBody body = RequestBody.create(json, JSON);
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getServiceKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getServiceKey())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .post(body)
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to create user: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<User>>(){}.getType();
                    List<User> users = gson.fromJson(responseBody, listType);

                    if (users == null || users.isEmpty()) {
                        throw new RuntimeException("Failed to create user");
                    }

                    return users.get(0);
                }
            } catch (IOException e) {
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Void> updateUser(User user) {
        return CompletableFuture.runAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/users?user_id=eq." + user.getUserId();
                String json = gson.toJson(user);
                
                RequestBody body = RequestBody.create(json, JSON);
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getServiceKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getServiceKey())
                    .addHeader("Content-Type", "application/json")
                    .patch(body)
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to update user: HTTP " + response.code() + " - " + response.message());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Void> deleteUser(int userId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/users?user_id=eq." + userId;
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getServiceKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getServiceKey())
                    .addHeader("Content-Type", "application/json")
                    .delete()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to delete user: HTTP " + response.code() + " - " + response.message());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<List<LibraryResource>> getAllLibraryResources() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // First, get basic library resources
                String url = SupabaseConfig.getUrl() + "/rest/v1/library_resources?select=*";
                
                android.util.Log.d("SupabaseClient", "Fetching basic resources from URL: " + url);
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    android.util.Log.d("SupabaseClient", "Response code: " + response.code());
                    
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "No error body";
                        android.util.Log.e("SupabaseClient", "Failed to get resources: HTTP " + response.code() + 
                            " - " + response.message() + " - Body: " + errorBody);
                        throw new RuntimeException("Failed to get resources: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    android.util.Log.d("SupabaseClient", "Response body: " + responseBody);
                    
                    // Parse basic resources
                    Type listType = new TypeToken<List<LibraryResource>>(){}.getType();
                    List<LibraryResource> resources = gson.fromJson(responseBody, listType);
                    
                    if (resources == null) {
                        android.util.Log.w("SupabaseClient", "Basic resources list is null");
                        return new ArrayList<>();
                    }
                    
                    android.util.Log.d("SupabaseClient", "Successfully fetched " + resources.size() + " basic resources");
                    
                    // Now fetch details for each category separately
                    enrichResourcesWithDetails(resources);
                    
                    return resources;
                }
            } catch (IOException e) {
                android.util.Log.e("SupabaseClient", "Network error: " + e.getMessage(), e);
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error: " + e.getMessage(), e);
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<LibraryResource> getLibraryResourceById(int resourceId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/library_resources?resource_id=eq." + resourceId + "&select=*";
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to get resource: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<LibraryResource>>(){}.getType();
                    List<LibraryResource> resources = gson.fromJson(responseBody, listType);

                    if (resources == null || resources.isEmpty()) {
                        throw new RuntimeException("Resource not found");
                    }

                    return resources.get(0);
                }
            } catch (IOException e) {
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<List<LibraryResource>> searchLibraryResources(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/library_resources?title=ilike.*" + query + "*&select=*";
                
                android.util.Log.d("SupabaseClient", "Searching resources with query: " + query);
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Search failed: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<LibraryResource>>(){}.getType();
                    List<LibraryResource> resources = gson.fromJson(responseBody, listType);
                    
                    if (resources == null) {
                        return new ArrayList<>();
                    }
                    
                    android.util.Log.d("SupabaseClient", "Found " + resources.size() + " resources matching query: " + query);
                    
                    // Enrich search results with details
                    enrichResourcesWithDetails(resources);
                    
                    return resources;
                }
            } catch (IOException e) {
                android.util.Log.e("SupabaseClient", "Network error during search: " + e.getMessage(), e);
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error during search: " + e.getMessage(), e);
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    // Category-specific methods using simpler approach
    public CompletableFuture<List<LibraryResource>> getBookResources() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Fetch basic book resources
                String url = SupabaseConfig.getUrl() + "/rest/v1/library_resources?category=eq.book&select=*";
                
                android.util.Log.d("SupabaseClient", "Fetching basic books from URL: " + url);
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to get books: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<LibraryResource>>(){}.getType();
                    List<LibraryResource> resources = gson.fromJson(responseBody, listType);
                    
                    if (resources == null) {
                        return new ArrayList<>();
                    }
                    
                    android.util.Log.d("SupabaseClient", "Successfully retrieved " + resources.size() + " basic book resources");
                    
                    // Enrich with book details
                    enrichBooksWithDetails(resources);
                    
                    return resources;
                }
            } catch (IOException e) {
                android.util.Log.e("SupabaseClient", "Network error getting books: " + e.getMessage(), e);
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error getting books: " + e.getMessage(), e);
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<List<LibraryResource>> getPeriodicalResources() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Fetch basic periodical resources
                String url = SupabaseConfig.getUrl() + "/rest/v1/library_resources?category=eq.periodical&select=*";
                
                android.util.Log.d("SupabaseClient", "Fetching basic periodicals from URL: " + url);
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to get periodicals: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<LibraryResource>>(){}.getType();
                    List<LibraryResource> resources = gson.fromJson(responseBody, listType);
                    
                    if (resources == null) {
                        return new ArrayList<>();
                    }
                    
                    android.util.Log.d("SupabaseClient", "Successfully retrieved " + resources.size() + " basic periodical resources");
                    
                    // Enrich with periodical details
                    enrichPeriodicalsWithDetails(resources);
                    
                    return resources;
                }
            } catch (IOException e) {
                android.util.Log.e("SupabaseClient", "Network error getting periodicals: " + e.getMessage(), e);
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error getting periodicals: " + e.getMessage(), e);
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<List<LibraryResource>> getMediaResources() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Fetch basic media resources
                String url = SupabaseConfig.getUrl() + "/rest/v1/library_resources?category=eq.media&select=*";
                
                android.util.Log.d("SupabaseClient", "Fetching basic media from URL: " + url);
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to get media: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<LibraryResource>>(){}.getType();
                    List<LibraryResource> resources = gson.fromJson(responseBody, listType);
                    
                    if (resources == null) {
                        return new ArrayList<>();
                    }
                    
                    android.util.Log.d("SupabaseClient", "Successfully retrieved " + resources.size() + " basic media resources");
                    
                    // Enrich with media details
                    enrichMediaWithDetails(resources);
                    
                    return resources;
                }
            } catch (IOException e) {
                android.util.Log.e("SupabaseClient", "Network error getting media: " + e.getMessage(), e);
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error getting media: " + e.getMessage(), e);
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    // Helper method to enrich resources with details for all categories
    private void enrichResourcesWithDetails(List<LibraryResource> resources) {
        // Group resources by category for batch processing
        List<LibraryResource> books = new ArrayList<>();
        List<LibraryResource> periodicals = new ArrayList<>();
        List<LibraryResource> media = new ArrayList<>();
        
        for (LibraryResource resource : resources) {
            String category = resource.getCategory();
            if ("book".equals(category)) {
                books.add(resource);
            } else if ("periodical".equals(category)) {
                periodicals.add(resource);
            } else if ("media".equals(category)) {
                media.add(resource);
            }
        }
        
        // Enrich each category separately
        if (!books.isEmpty()) {
            enrichBooksWithDetails(books);
        }
        if (!periodicals.isEmpty()) {
            enrichPeriodicalsWithDetails(periodicals);
        }
        if (!media.isEmpty()) {
            enrichMediaWithDetails(media);
        }
    }

    // Asynchronous version for better performance
    private CompletableFuture<Void> enrichResourcesWithDetailsAsync(List<LibraryResource> resources) {
        return CompletableFuture.runAsync(() -> {
            // Group resources by category for batch processing
            List<LibraryResource> books = new ArrayList<>();
            List<LibraryResource> periodicals = new ArrayList<>();
            List<LibraryResource> media = new ArrayList<>();
            
            for (LibraryResource resource : resources) {
                String category = resource.getCategory();
                if ("book".equals(category)) {
                    books.add(resource);
                } else if ("periodical".equals(category)) {
                    periodicals.add(resource);
                } else if ("media".equals(category)) {
                    media.add(resource);
                }
            }
            
            // Create futures for parallel processing
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            if (!books.isEmpty()) {
                futures.add(CompletableFuture.runAsync(() -> enrichBooksWithDetails(books)));
            }
            if (!periodicals.isEmpty()) {
                futures.add(CompletableFuture.runAsync(() -> enrichPeriodicalsWithDetails(periodicals)));
            }
            if (!media.isEmpty()) {
                futures.add(CompletableFuture.runAsync(() -> enrichMediaWithDetails(media)));
            }
            
            // Wait for all enrichment operations to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            android.util.Log.d("SupabaseClient", "Completed asynchronous enrichment for all categories");
        });
    }

    // Enrich books with their specific details
    private void enrichBooksWithDetails(List<LibraryResource> books) {
        try {
            // Build resource IDs for batch query
            List<Integer> resourceIds = books.stream()
                .map(LibraryResource::getResourceId)
                .collect(java.util.stream.Collectors.toList());
            
            if (resourceIds.isEmpty()) return;
            
            // Create IN query for batch fetching
            String idsParam = resourceIds.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
            
            String url = SupabaseConfig.getUrl() + "/rest/v1/books?resource_id=in.(" + idsParam + ")&select=*";
            
            android.util.Log.d("SupabaseClient", "Fetching book details from URL: " + url);
            
            Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.getAnonKey())
                .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<BookDetails>>(){}.getType();
                    List<BookDetails> bookDetailsList = gson.fromJson(responseBody, listType);
                    
                    if (bookDetailsList != null) {
                        // Match book details to resources by resource_id
                        attachBookDetailsToResources(books, bookDetailsList);
                        android.util.Log.d("SupabaseClient", "Successfully enriched " + books.size() + " books with details");
                    }
                } else {
                    android.util.Log.w("SupabaseClient", "Failed to fetch book details: HTTP " + response.code());
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SupabaseClient", "Error enriching books with details: " + e.getMessage(), e);
        }
    }

    // Enrich periodicals with their specific details
    private void enrichPeriodicalsWithDetails(List<LibraryResource> periodicals) {
        try {
            List<Integer> resourceIds = periodicals.stream()
                .map(LibraryResource::getResourceId)
                .collect(java.util.stream.Collectors.toList());
            
            if (resourceIds.isEmpty()) return;
            
            String idsParam = resourceIds.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
            
            String url = SupabaseConfig.getUrl() + "/rest/v1/periodicals?resource_id=in.(" + idsParam + ")&select=*";
            
            android.util.Log.d("SupabaseClient", "Fetching periodical details from URL: " + url);
            
            Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.getAnonKey())
                .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<PeriodicalDetails>>(){}.getType();
                    List<PeriodicalDetails> periodicalDetailsList = gson.fromJson(responseBody, listType);
                    
                    if (periodicalDetailsList != null) {
                        attachPeriodicalDetailsToResources(periodicals, periodicalDetailsList);
                        android.util.Log.d("SupabaseClient", "Successfully enriched " + periodicals.size() + " periodicals with details");
                    }
                } else {
                    android.util.Log.w("SupabaseClient", "Failed to fetch periodical details: HTTP " + response.code());
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SupabaseClient", "Error enriching periodicals with details: " + e.getMessage(), e);
        }
    }

    // Enrich media with their specific details
    private void enrichMediaWithDetails(List<LibraryResource> media) {
        try {
            List<Integer> resourceIds = media.stream()
                .map(LibraryResource::getResourceId)
                .collect(java.util.stream.Collectors.toList());
            
            if (resourceIds.isEmpty()) return;
            
            String idsParam = resourceIds.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
            
            String url = SupabaseConfig.getUrl() + "/rest/v1/media_resources?resource_id=in.(" + idsParam + ")&select=*";
            
            android.util.Log.d("SupabaseClient", "Fetching media details from URL: " + url);
            
            Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.getAnonKey())
                .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<MediaDetails>>(){}.getType();
                    List<MediaDetails> mediaDetailsList = gson.fromJson(responseBody, listType);
                    
                    if (mediaDetailsList != null) {
                        attachMediaDetailsToResources(media, mediaDetailsList);
                        android.util.Log.d("SupabaseClient", "Successfully enriched " + media.size() + " media with details");
                    }
                } else {
                    android.util.Log.w("SupabaseClient", "Failed to fetch media details: HTTP " + response.code());
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SupabaseClient", "Error enriching media with details: " + e.getMessage(), e);
        }
    }

    // Helper methods to attach details to resources
    private void attachBookDetailsToResources(List<LibraryResource> books, List<BookDetails> bookDetailsList) {
        // Create a map for efficient lookup
        java.util.Map<Integer, BookDetails> detailsMap = bookDetailsList.stream()
            .collect(java.util.stream.Collectors.toMap(
                BookDetails::getResourceId, 
                java.util.function.Function.identity()));
        
        // Match details to resources by resource_id
        for (LibraryResource book : books) {
            BookDetails details = detailsMap.get(book.getResourceId());
            if (details != null) {
                book.setBookDetails(details);
                android.util.Log.d("SupabaseClient", "Matched book details for resource ID: " + book.getResourceId());
            } else {
                android.util.Log.w("SupabaseClient", "No book details found for resource ID: " + book.getResourceId());
            }
        }
    }

    private void attachPeriodicalDetailsToResources(List<LibraryResource> periodicals, List<PeriodicalDetails> periodicalDetailsList) {
        // Create a map for efficient lookup
        java.util.Map<Integer, PeriodicalDetails> detailsMap = periodicalDetailsList.stream()
            .collect(java.util.stream.Collectors.toMap(
                PeriodicalDetails::getResourceId, 
                java.util.function.Function.identity()));
        
        // Match details to resources by resource_id
        for (LibraryResource periodical : periodicals) {
            PeriodicalDetails details = detailsMap.get(periodical.getResourceId());
            if (details != null) {
                periodical.setPeriodicalDetails(details);
                android.util.Log.d("SupabaseClient", "Matched periodical details for resource ID: " + periodical.getResourceId());
            } else {
                android.util.Log.w("SupabaseClient", "No periodical details found for resource ID: " + periodical.getResourceId());
            }
        }
    }

    private void attachMediaDetailsToResources(List<LibraryResource> media, List<MediaDetails> mediaDetailsList) {
        // Create a map for efficient lookup
        java.util.Map<Integer, MediaDetails> detailsMap = mediaDetailsList.stream()
            .collect(java.util.stream.Collectors.toMap(
                MediaDetails::getResourceId, 
                java.util.function.Function.identity()));
        
        // Match details to resources by resource_id
        for (LibraryResource mediaResource : media) {
            MediaDetails details = detailsMap.get(mediaResource.getResourceId());
            if (details != null) {
                mediaResource.setMediaDetails(details);
                android.util.Log.d("SupabaseClient", "Matched media details for resource ID: " + mediaResource.getResourceId());
            } else {
                android.util.Log.w("SupabaseClient", "No media details found for resource ID: " + mediaResource.getResourceId());
            }
        }
    }

    // Lazy loading methods - load basic resources first, details on demand
    public CompletableFuture<List<LibraryResource>> getAllLibraryResourcesBasic() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/library_resources?select=*";
                
                android.util.Log.d("SupabaseClient", "Fetching basic resources only (lazy loading)");
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to get basic resources: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<LibraryResource>>(){}.getType();
                    List<LibraryResource> resources = gson.fromJson(responseBody, listType);
                    
                    android.util.Log.d("SupabaseClient", "Successfully fetched " + (resources != null ? resources.size() : 0) + " basic resources");
                    return resources != null ? resources : new ArrayList<>();
                }
            } catch (IOException e) {
                android.util.Log.e("SupabaseClient", "Network error: " + e.getMessage(), e);
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error: " + e.getMessage(), e);
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    // Load details for a specific resource on-demand
    public CompletableFuture<LibraryResource> enrichResourceWithDetails(LibraryResource resource) {
        return CompletableFuture.supplyAsync(() -> {
            if (resource == null || resource.getCategory() == null) {
                return resource;
            }
            
            String category = resource.getCategory();
            List<LibraryResource> singleResourceList = java.util.Arrays.asList(resource);
            
            switch (category) {
                case "book":
                    enrichBooksWithDetails(singleResourceList);
                    break;
                case "periodical":
                    enrichPeriodicalsWithDetails(singleResourceList);
                    break;
                case "media":
                    enrichMediaWithDetails(singleResourceList);
                    break;
                default:
                    android.util.Log.w("SupabaseClient", "Unknown category for resource enrichment: " + category);
                    break;
            }
            
            return resource;
        });
    }

    // Batch enrich multiple resources on-demand
    public CompletableFuture<List<LibraryResource>> enrichResourcesWithDetailsOnDemand(List<LibraryResource> resources) {
        return CompletableFuture.supplyAsync(() -> {
            enrichResourcesWithDetails(resources);
            return resources;
        });
    }

    // Borrowing request methods with proper validation
    public CompletableFuture<Borrowing> createBorrowingRequest(int userId, int resourceId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                android.util.Log.d("SupabaseClient", "Starting borrowing request validation for user: " + userId + ", resource: " + resourceId);
                
                // Step 1: Check if the resource exists and is available
                CompletableFuture<LibraryResource> resourceFuture = getLibraryResourceById(resourceId);
                LibraryResource resource = resourceFuture.join();
                
                if (resource == null) {
                    throw new RuntimeException("Resource not found");
                }
                
                android.util.Log.d("SupabaseClient", "Resource found: " + resource.getTitle() + " with status: " + resource.getStatus());
                
                if (!"available".equalsIgnoreCase(resource.getStatus())) {
                    throw new RuntimeException("This resource is not available for borrowing. Current status: " + resource.getStatus());
                }
                
                // Step 2: Check if user already has a pending request for this resource
                CompletableFuture<Boolean> existingRequestFuture = checkExistingBorrowingRequest(userId, resourceId);
                Boolean hasExistingRequest = existingRequestFuture.join();
                
                if (hasExistingRequest) {
                    throw new RuntimeException("You already have a pending request for this resource");
                }
                
                // Step 3: Check user's borrowing limit
                CompletableFuture<Boolean> borrowingLimitFuture = checkUserBorrowingLimit(userId);
                Boolean canBorrow = borrowingLimitFuture.join();
                
                if (!canBorrow) {
                    throw new RuntimeException("You have reached your maximum borrowing limit");
                }

                // Step 4: Create the borrowing request
                // Calculate due date (assuming 7 days borrowing period)
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 7);
                java.util.Date dueDate = calendar.getTime();

                // Format dates as ISO 8601 strings for Supabase
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                
                // Create borrowing data map (excluding auto-generated fields)
                Map<String, Object> borrowingData = new HashMap<>();
                borrowingData.put("user_id", userId);
                borrowingData.put("resource_id", resourceId);
                borrowingData.put("borrow_date", dateFormat.format(new java.util.Date()));
                borrowingData.put("due_date", dateFormat.format(dueDate));
                borrowingData.put("status", "pending");
                borrowingData.put("fine_amount", "0.00");

                String url = SupabaseConfig.getUrl() + "/rest/v1/borrowings";
                String json = gson.toJson(borrowingData);
                
                android.util.Log.d("SupabaseClient", "Creating borrowing request with data: " + json);
                
                RequestBody body = RequestBody.create(json, JSON);
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .post(body)
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    String responseBodyStr = response.body() != null ? response.body().string() : "No response body";
                    
                    android.util.Log.d("SupabaseClient", "Borrowing request response code: " + response.code());
                    android.util.Log.d("SupabaseClient", "Borrowing request response body: " + responseBodyStr);
                    
                    if (!response.isSuccessful()) {
                        android.util.Log.e("SupabaseClient", "Failed to create borrowing request: HTTP " + response.code() + 
                            " - " + response.message() + " - Body: " + responseBodyStr);
                        
                        // Parse error details if available
                        String errorMessage = "Failed to create borrowing request";
                        try {
                            JsonElement errorJson = JsonParser.parseString(responseBodyStr);
                            if (errorJson.isJsonObject()) {
                                JsonObject errorObj = errorJson.getAsJsonObject();
                                if (errorObj.has("message")) {
                                    errorMessage = errorObj.get("message").getAsString();
                                } else if (errorObj.has("hint")) {
                                    errorMessage = errorObj.get("hint").getAsString();
                                } else if (errorObj.has("details")) {
                                    errorMessage = errorObj.get("details").getAsString();
                                }
                            }
                        } catch (Exception parseError) {
                            android.util.Log.w("SupabaseClient", "Could not parse error response: " + parseError.getMessage());
                        }
                        
                        throw new RuntimeException(errorMessage + " (HTTP " + response.code() + ")");
                    }

                    android.util.Log.d("SupabaseClient", "Borrowing request created successfully");
                    
                    // Parse response as list of Borrowing objects
                    Type listType = new TypeToken<List<Borrowing>>(){}.getType();
                    List<Borrowing> borrowings = borrowingGson.fromJson(responseBodyStr, listType);

                    if (borrowings == null || borrowings.isEmpty()) {
                        android.util.Log.e("SupabaseClient", "Response parsed successfully but no borrowing objects returned");
                        throw new RuntimeException("Failed to create borrowing request - no data returned");
                    }

                    Borrowing createdBorrowing = borrowings.get(0);
                    android.util.Log.d("SupabaseClient", "Successfully created borrowing with ID: " + createdBorrowing.getBorrowingId());
                    
                    // Step 5: Update resource status to borrowed (like in PHP code)
                    try {
                        updateResourceStatus(resourceId, "borrowed").join();
                        android.util.Log.d("SupabaseClient", "Successfully updated resource status to borrowed");
        } catch (Exception e) {
                        android.util.Log.w("SupabaseClient", "Warning: Could not update resource status: " + e.getMessage());
                        // Don't fail the entire operation for this
                    }
                    
                    return createdBorrowing;
                }
            } catch (IOException e) {
                android.util.Log.e("SupabaseClient", "Network error creating borrowing request: " + e.getMessage(), e);
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error creating borrowing request: " + e.getMessage(), e);
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    // Check user's borrowing limit
    public CompletableFuture<Boolean> checkUserBorrowingLimit(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/users?user_id=eq." + userId + "&select=max_books";
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        android.util.Log.e("SupabaseClient", "Failed to get user borrowing limit: HTTP " + response.code());
                        return true; // Allow borrowing if we can't check the limit
                    }

                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<JsonObject>>(){}.getType();
                    List<JsonObject> users = gson.fromJson(responseBody, listType);
                    
                    if (users == null || users.isEmpty()) {
                        return true; // Allow if user not found
                    }
                    
                    int maxBooks = 5; // Default limit
                    JsonObject user = users.get(0);
                    if (user.has("max_books") && !user.get("max_books").isJsonNull()) {
                        maxBooks = user.get("max_books").getAsInt();
                    }
                    
                    // Check current active borrowings
                    String borrowingsUrl = SupabaseConfig.getUrl() + "/rest/v1/borrowings?user_id=eq." + userId + 
                                         "&status=in.(pending,active,overdue)&select=borrowing_id";
                    
                    Request borrowingsRequest = new Request.Builder()
                        .url(borrowingsUrl)
                        .addHeader("apikey", SupabaseConfig.getAnonKey())
                        .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                        .addHeader("Content-Type", "application/json")
                        .get()
                        .build();

                    try (Response borrowingsResponse = client.newCall(borrowingsRequest).execute()) {
                        if (borrowingsResponse.isSuccessful()) {
                            String borrowingsResponseBody = borrowingsResponse.body().string();
                            Type borrowingsListType = new TypeToken<List<JsonObject>>(){}.getType();
                            List<JsonObject> activeBorrowings = gson.fromJson(borrowingsResponseBody, borrowingsListType);
                            
                            int currentBorrowings = activeBorrowings != null ? activeBorrowings.size() : 0;
                            boolean canBorrow = currentBorrowings < maxBooks;
                            
                            android.util.Log.d("SupabaseClient", "User borrowing check - Current: " + currentBorrowings + ", Max: " + maxBooks + ", Can borrow: " + canBorrow);
                            
                            return canBorrow;
                        }
                    }
                    
                    return true; // Allow if we can't check
                }
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error checking user borrowing limit: " + e.getMessage(), e);
                return true; // Allow borrowing if check fails
            }
        });
    }

    // Update resource status
    public CompletableFuture<Void> updateResourceStatus(int resourceId, String status) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("status", status);
                
                String url = SupabaseConfig.getUrl() + "/rest/v1/library_resources?resource_id=eq." + resourceId;
                String json = gson.toJson(updateData);
                
                RequestBody body = RequestBody.create(json, JSON);
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .patch(body)
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to update resource status: HTTP " + response.code());
                    }
                    android.util.Log.d("SupabaseClient", "Successfully updated resource " + resourceId + " status to: " + status);
                }
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error updating resource status: " + e.getMessage(), e);
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Boolean> checkExistingBorrowingRequest(int userId, int resourceId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/borrowings?user_id=eq." + userId + 
                           "&resource_id=eq." + resourceId + "&status=eq.pending&select=borrowing_id";
                
                android.util.Log.d("SupabaseClient", "Checking existing borrowing request from URL: " + url);
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        android.util.Log.e("SupabaseClient", "Failed to check existing request: HTTP " + response.code());
                        return false; // Assume no existing request if check fails
                    }

                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<Borrowing>>(){}.getType();
                    List<Borrowing> existingRequests = gson.fromJson(responseBody, listType);
                    
                    boolean hasExisting = existingRequests != null && !existingRequests.isEmpty();
                    android.util.Log.d("SupabaseClient", "Existing request check result: " + hasExisting);
                    
                    return hasExisting;
                }
            } catch (IOException e) {
                android.util.Log.e("SupabaseClient", "Network error checking existing request: " + e.getMessage(), e);
                return false; // Assume no existing request if check fails
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error checking existing request: " + e.getMessage(), e);
                return false; // Assume no existing request if check fails
            }
        });
    }

    public CompletableFuture<List<Borrowing>> getUserBorrowingHistory(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/borrowings?user_id=eq." + userId + 
                           "&select=*,library_resources(title,category,accession_number)&order=borrow_date.desc";
                
                android.util.Log.d("SupabaseClient", "Fetching user borrowing history from URL: " + url);
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to get borrowing history: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    android.util.Log.d("SupabaseClient", "Borrowing history response: " + responseBody);
                    
                    Type listType = new TypeToken<List<Borrowing>>(){}.getType();
                    List<Borrowing> borrowings = gson.fromJson(responseBody, listType);
                    
                    return borrowings != null ? borrowings : new ArrayList<>();
                }
            } catch (IOException e) {
                android.util.Log.e("SupabaseClient", "Network error getting borrowing history: " + e.getMessage(), e);
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error getting borrowing history: " + e.getMessage(), e);
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<List<Borrowing>> getPendingBorrowingRequests() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/borrowings?status=eq.pending" + 
                           "&select=*,users(first_name,last_name,username),library_resources(title,category,accession_number)" +
                           "&order=borrow_date.desc";
                
                android.util.Log.d("SupabaseClient", "Fetching pending borrowing requests from URL: " + url);
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to get pending requests: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    android.util.Log.d("SupabaseClient", "Pending requests response: " + responseBody);
                    
                    Type listType = new TypeToken<List<Borrowing>>(){}.getType();
                    List<Borrowing> borrowings = gson.fromJson(responseBody, listType);
                    
                    return borrowings != null ? borrowings : new ArrayList<>();
                }
            } catch (IOException e) {
                android.util.Log.e("SupabaseClient", "Network error getting pending requests: " + e.getMessage(), e);
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error getting pending requests: " + e.getMessage(), e);
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    // Borrowing approval methods for librarians
    public CompletableFuture<Borrowing> approveBorrowingRequest(int borrowingId, int librarianId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                android.util.Log.d("SupabaseClient", "Starting borrowing approval for borrowing ID: " + borrowingId + " by librarian: " + librarianId);
                
                // Step 1: Get borrowing details with user information
                String getBorrowingUrl = SupabaseConfig.getUrl() + "/rest/v1/borrowings?borrowing_id=eq." + borrowingId + 
                                       "&select=*,users(borrowing_days_limit)";
                
                Request getBorrowingRequest = new Request.Builder()
                    .url(getBorrowingUrl)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response getBorrowingResponse = client.newCall(getBorrowingRequest).execute()) {
                    if (!getBorrowingResponse.isSuccessful()) {
                        throw new RuntimeException("Failed to get borrowing details: HTTP " + getBorrowingResponse.code());
                    }

                    String responseBody = getBorrowingResponse.body().string();
                    Type listType = new TypeToken<List<Borrowing>>(){}.getType();
                    List<Borrowing> borrowings = borrowingGson.fromJson(responseBody, listType);

                    if (borrowings == null || borrowings.isEmpty()) {
                        throw new RuntimeException("Borrowing record not found");
                    }

                    Borrowing borrowing = borrowings.get(0);
                    
                    if (!"pending".equals(borrowing.getStatus())) {
                        throw new RuntimeException("This borrowing request has already been processed");
                    }

                    // Step 2: Calculate due date based on user's borrowing days limit (default 7 days)
                    int borrowingDaysLimit = 7; // Default
                    // You can get this from the user object if needed
                    
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, borrowingDaysLimit);
                    java.util.Date dueDate = calendar.getTime();

                    // Format dates for Supabase
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                    dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                    // Step 3: Update borrowing record to approved
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("status", "active");
                    updateData.put("due_date", dateFormat.format(dueDate));
                    updateData.put("approved_by", librarianId);
                    updateData.put("approved_at", dateFormat.format(new java.util.Date()));

                    String updateUrl = SupabaseConfig.getUrl() + "/rest/v1/borrowings?borrowing_id=eq." + borrowingId;
                    String updateJson = gson.toJson(updateData);

                    RequestBody updateBody = RequestBody.create(updateJson, JSON);
                    Request updateRequest = new Request.Builder()
                        .url(updateUrl)
                        .addHeader("apikey", SupabaseConfig.getAnonKey())
                        .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "return=representation")
                        .patch(updateBody)
                        .build();

                    try (Response updateResponse = client.newCall(updateRequest).execute()) {
                        if (!updateResponse.isSuccessful()) {
                            String errorBody = updateResponse.body() != null ? updateResponse.body().string() : "No error body";
                            throw new RuntimeException("Failed to update borrowing record: HTTP " + updateResponse.code() + " - " + errorBody);
                        }

                        String updateResponseBody = updateResponse.body().string();
                        Type updateListType = new TypeToken<List<Borrowing>>(){}.getType();
                        List<Borrowing> updatedBorrowings = borrowingGson.fromJson(updateResponseBody, updateListType);

                        if (updatedBorrowings == null || updatedBorrowings.isEmpty()) {
                            throw new RuntimeException("Failed to get updated borrowing record");
                        }

                        Borrowing updatedBorrowing = updatedBorrowings.get(0);

                        // Step 4: Update resource status to borrowed
                        try {
                            updateResourceStatus(borrowing.getResourceId(), "borrowed").join();
                            android.util.Log.d("SupabaseClient", "Successfully updated resource status to borrowed");
                        } catch (Exception e) {
                            android.util.Log.w("SupabaseClient", "Warning: Could not update resource status: " + e.getMessage());
                            // Don't fail the entire operation for this
                        }

                        android.util.Log.d("SupabaseClient", "Successfully approved borrowing ID: " + borrowingId + " with due date: " + dateFormat.format(dueDate));
                        return updatedBorrowing;
                    }
                }
            } catch (IOException e) {
                android.util.Log.e("SupabaseClient", "Network error approving borrowing: " + e.getMessage(), e);
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error approving borrowing: " + e.getMessage(), e);
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }
    
    // Reject borrowing request
    public CompletableFuture<Void> rejectBorrowingRequest(int borrowingId, int librarianId, String reason) {
        return CompletableFuture.runAsync(() -> {
            try {
                android.util.Log.d("SupabaseClient", "Rejecting borrowing request ID: " + borrowingId);
                
                // Get borrowing details first
                String getBorrowingUrl = SupabaseConfig.getUrl() + "/rest/v1/borrowings?borrowing_id=eq." + borrowingId + "&select=*";
                
                Request getBorrowingRequest = new Request.Builder()
                    .url(getBorrowingUrl)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response getBorrowingResponse = client.newCall(getBorrowingRequest).execute()) {
                    if (!getBorrowingResponse.isSuccessful()) {
                        throw new RuntimeException("Failed to get borrowing details: HTTP " + getBorrowingResponse.code());
                    }

                    String responseBody = getBorrowingResponse.body().string();
                    Type listType = new TypeToken<List<Borrowing>>(){}.getType();
                    List<Borrowing> borrowings = borrowingGson.fromJson(responseBody, listType);

                    if (borrowings == null || borrowings.isEmpty()) {
                        throw new RuntimeException("Borrowing record not found");
                    }

                    Borrowing borrowing = borrowings.get(0);

                    // Update borrowing record to rejected
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                    dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("status", "rejected");
                    updateData.put("approved_by", librarianId);
                    updateData.put("approved_at", dateFormat.format(new java.util.Date()));
                    // You could add a rejection_reason field if your database supports it

                    String updateUrl = SupabaseConfig.getUrl() + "/rest/v1/borrowings?borrowing_id=eq." + borrowingId;
                    String updateJson = gson.toJson(updateData);

                    RequestBody updateBody = RequestBody.create(updateJson, JSON);
                    Request updateRequest = new Request.Builder()
                        .url(updateUrl)
                        .addHeader("apikey", SupabaseConfig.getAnonKey())
                        .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                        .addHeader("Content-Type", "application/json")
                        .patch(updateBody)
                        .build();

                    try (Response updateResponse = client.newCall(updateRequest).execute()) {
                        if (!updateResponse.isSuccessful()) {
                            String errorBody = updateResponse.body() != null ? updateResponse.body().string() : "No error body";
                            throw new RuntimeException("Failed to reject borrowing request: HTTP " + updateResponse.code() + " - " + errorBody);
                        }

                        // Update resource status back to available
                        try {
                            updateResourceStatus(borrowing.getResourceId(), "available").join();
                            android.util.Log.d("SupabaseClient", "Successfully updated resource status back to available");
                        } catch (Exception e) {
                            android.util.Log.w("SupabaseClient", "Warning: Could not update resource status: " + e.getMessage());
                        }

                        android.util.Log.d("SupabaseClient", "Successfully rejected borrowing request ID: " + borrowingId);
                    }
                }
            } catch (IOException e) {
                android.util.Log.e("SupabaseClient", "Network error rejecting borrowing: " + e.getMessage(), e);
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error rejecting borrowing: " + e.getMessage(), e);
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    // Get user's borrowing requests with detailed status information
    public CompletableFuture<List<Borrowing>> getUserBorrowingRequestsWithDetails(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/borrowings?user_id=eq." + userId + 
                           "&select=*,library_resources(title,category,accession_number,cover_image)" +
                           "&order=borrow_date.desc";
                
                android.util.Log.d("SupabaseClient", "Fetching detailed borrowing requests for user: " + userId);
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new RuntimeException("Failed to get borrowing requests: HTTP " + response.code() + " - " + response.message());
                    }

                    String responseBody = response.body().string();
                    android.util.Log.d("SupabaseClient", "Borrowing requests response: " + responseBody);
                    
                    Type listType = new TypeToken<List<Borrowing>>(){}.getType();
                    List<Borrowing> borrowings = borrowingGson.fromJson(responseBody, listType);
                    
                    if (borrowings != null) {
                        android.util.Log.d("SupabaseClient", "Successfully retrieved " + borrowings.size() + " borrowing requests");
                    }
                    
                    return borrowings != null ? borrowings : new ArrayList<>();
                }
            } catch (IOException e) {
                android.util.Log.e("SupabaseClient", "Network error getting borrowing requests: " + e.getMessage(), e);
                throw new RuntimeException("Network error: " + e.getMessage(), e);
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error getting borrowing requests: " + e.getMessage(), e);
                throw new RuntimeException("Error: " + e.getMessage(), e);
            }
        });
    }

    // Get count of pending requests for dashboard summary
    public CompletableFuture<Integer> getUserPendingRequestsCount(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SupabaseConfig.getUrl() + "/rest/v1/borrowings?user_id=eq." + userId + 
                           "&status=eq.pending&select=borrowing_id";
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SupabaseConfig.getAnonKey())
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.getAnonKey())
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        android.util.Log.e("SupabaseClient", "Failed to get pending requests count: HTTP " + response.code());
                        return 0;
                    }

                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<Object>>(){}.getType();
                    List<Object> requests = gson.fromJson(responseBody, listType);
                    
                    int count = requests != null ? requests.size() : 0;
                    android.util.Log.d("SupabaseClient", "User has " + count + " pending requests");
                    
                    return count;
                }
            } catch (Exception e) {
                android.util.Log.e("SupabaseClient", "Error getting pending requests count: " + e.getMessage(), e);
                return 0;
            }
        });
    }
} 