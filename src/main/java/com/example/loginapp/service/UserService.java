package com.example.loginapp.service;

import org.springframework.stereotype.Service;
import com.example.loginapp.model.User;

@Service
public class UserService {
    
    // In a real app, you would use a database or other authentication mechanism
    public boolean authenticate(User user) {
        // For demo purposes, just check if the username and password are not empty
        // and the password is at least 4 characters long
        return user.getUsername() != null && !user.getUsername().isEmpty() &&
               user.getPassword() != null && user.getPassword().length() >= 4;
    }
}
