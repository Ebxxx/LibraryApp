package com.example.libraryapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.libraryapp.R;
import com.example.libraryapp.data.SupabaseClient;
import com.example.libraryapp.models.User;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private MaterialButton loginButton;
    private SupabaseClient supabaseClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database client
        supabaseClient = SupabaseClient.getInstance(this);

        // Initialize views
        usernameLayout = findViewById(R.id.username_layout);
        passwordLayout = findViewById(R.id.password_layout);
        usernameEditText = findViewById(R.id.username_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (validateInput(username, password)) {
                    loginButton.setEnabled(false); // Disable button during login
                    attemptLogin(username, password);
                }
            }
        });
    }

    private void attemptLogin(String username, String password) {
        supabaseClient.loginUser(username, password)
            .thenAccept(user -> {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, 
                        "Welcome, " + user.getFirstName() + "!", 
                        Toast.LENGTH_SHORT).show();
                    
                    // Redirect ALL users to LibraryDashboardActivity
                    Intent intent = new Intent(LoginActivity.this, LibraryDashboardActivity.class);
                    intent.putExtra("USER_ID", user.getUserId());
                    intent.putExtra("USER_NAME", user.getFirstName() + " " + user.getLastName());
                    intent.putExtra("USER_ROLE", user.getRole());
                    startActivity(intent);
                    finish();
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    loginButton.setEnabled(true);
                    Toast.makeText(LoginActivity.this, 
                        "Login failed: " + throwable.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
                return null;
            });
    }

    private boolean validateInput(String username, String password) {
        boolean isValid = true;

        if (username.isEmpty()) {
            usernameLayout.setError("Username cannot be empty");
            isValid = false;
        } else {
            usernameLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Password cannot be empty");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        return isValid;
    }
} 