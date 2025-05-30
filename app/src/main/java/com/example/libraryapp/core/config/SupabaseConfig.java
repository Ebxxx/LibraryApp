package com.example.libraryapp.core.config;

public class SupabaseConfig {
    private static final String SUPABASE_URL = "https://kiiawuqvluzcrggkjmyt.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtpaWF3dXF2bHV6Y3JnZ2tqbXl0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgwNTIyMzAsImV4cCI6MjA2MzYyODIzMH0.ATuYjkiwNX4Lzy7XkvLvclti0pkNBm3L7uobEjhB7iI";
    private static final String SUPABASE_SERVICE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtpaWF3dXF2bHV6Y3JnZ2tqbXl0Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0ODA1MjIzMCwiZXhwIjoyMDYzNjI4MjMwfQ.0QG0iBIYtyUBmUuU9-9Hoi20Lge4nax3NGTFJCUH7Gs";
    
    public static String getUrl() {
        return SUPABASE_URL;
    }
    
    public static String getAnonKey() {
        return SUPABASE_ANON_KEY;
    }
    
    public static String getServiceKey() {
        return SUPABASE_SERVICE_KEY;
    }
} 