package com.SpringSecurityFolksDevTutorial.demo.security.services;

public record CreateUserRequest(
        String name,
        String username,
        String password
) {}
