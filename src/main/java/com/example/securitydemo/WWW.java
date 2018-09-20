package com.example.securitydemo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
@ResponseBody
public class WWW {
    @GetMapping("/")
    public String index() {
        return "<h1>Welcome!</h1><a href='/me'>Profile</a>";
    }
    @GetMapping("/me")
    public String viewUser(Principal user) {
        return String.format("<h1>Messages by %s</h1><a href='/logout'>Logout</a>", user.getName());
    }
    @GetMapping("/{user}/{messageId}")
    public String viewMessage(@PathVariable String user, @PathVariable int messageId) {
        return String.format("<h1>Message %d from %s</h1><a href='/logout'>Logout</a>", 42, user);
    }
}
