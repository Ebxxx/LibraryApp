package com.example.libraryapp.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    
    /**
     * Hash a password using BCrypt
     * @param plainTextPassword The password to hash
     * @return The hashed password
     */
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }

    /**
     * Verify if a plaintext password matches a hashed password
     * @param plainTextPassword The password to check
     * @param hashedPassword The hashed password to check against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        try {
            // Convert PHP's BCrypt format to jBCrypt format if needed
            if (hashedPassword.startsWith("$2y$")) {
                hashedPassword = "$2a$" + hashedPassword.substring(4);
            }
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 