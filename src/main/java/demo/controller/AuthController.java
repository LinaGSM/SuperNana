package demo.controller;

import demo.model.AppUser;
import demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.*;
import org.springframework.web.bind.annotation.*;


/**
 * Controller for handling authentication-related endpoints.
 * <p>
 * This controller provides the necessary functionality for user registration.
 * </p>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder encoder;


    /**
     * Registers a new user if the username does not already exist.
     * <p>
     * The user's password is encoded before saving the user in the repository.
     * </p>
     *
     * @param user The user to be registered, containing username and password.
     * @return A ResponseEntity containing a success or error message.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AppUser user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        user.setPassword(encoder.encode(user.getPassword()));
        userRepo.save(user);
        return ResponseEntity.ok("User registered successfully.");
    }
}
