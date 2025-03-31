package demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    /**
     * Returns a welcome message with instructions for using the API.
     * <p>
     * This method is mapped to the root URL ("/") and provides users with helpful information on how to register, log in, and access secure endpoints.
     * </p>
     *
     * @return A string containing the welcome message and instructions.
     */
    @GetMapping("/")
    public String home() {
        return """
            🎉 Welcome to the SuperNana API!

            👉 Register a user at: POST /auth/register
            👉 Login at: /login (Spring Security form)
            👉 Try secure endpoints after login like: /queues, /topics

            You got this! 💪🔐
            """;
    }
}
