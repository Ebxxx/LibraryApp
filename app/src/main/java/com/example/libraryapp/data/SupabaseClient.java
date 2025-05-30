package com.example.libraryapp.data;

import com.example.libraryapp.core.config.SupabaseConfig;
import com.example.libraryapp.models.User;
import com.google.gson.Gson;
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

public class SupabaseClient {
    private static SupabaseClient instance;
    private final OkHttpClient client;
    private final Gson gson;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private SupabaseClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        client = new OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        
        gson = new Gson();
    }

    public static synchronized SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }

    public CompletableFuture<User> loginUser(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Query the users table to find user by username and password
                String url = SupabaseConfig.getUrl() + "/rest/v1/users?username=eq." + username + "&password=eq." + password + "&select=*";
                
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
} 