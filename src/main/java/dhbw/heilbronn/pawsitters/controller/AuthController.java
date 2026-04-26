package dhbw.heilbronn.pawsitters.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    // Startseite "Home"
    @GetMapping("/")
    public String home() {
        return "home";
    }

    // Login Form
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Wird nach erfolgreichen Login aufgerufen
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
