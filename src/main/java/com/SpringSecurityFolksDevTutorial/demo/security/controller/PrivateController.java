package com.SpringSecurityFolksDevTutorial.demo.security.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/private")
public class PrivateController {

    @GetMapping
    public String helloWorld() {
        return "Hello! PRIVATE";
    }

    @PreAuthorize(value = "hasRole(`ADMIN`)")
    @GetMapping("/admin")
    public String helloWorldUser() {
        return "Hello! ADMIN PRIVATE";
    }

}
