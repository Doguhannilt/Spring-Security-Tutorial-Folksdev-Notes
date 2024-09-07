package com.SpringSecurityFolksDevTutorial.demo.security.services;

import com.SpringSecurityFolksDevTutorial.demo.security.model.User;
import com.SpringSecurityFolksDevTutorial.demo.security.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(CreateUserRequest request) {

        User user = new User(request.name(), request.username(), bCryptPasswordEncoder.encode(request.password()));
        userRepository.save(user);
        return user;
    }

}
