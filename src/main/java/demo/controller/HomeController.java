package demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

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
