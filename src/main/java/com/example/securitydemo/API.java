package com.example.securitydemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@ResponseBody
public class API {
    @GetMapping("/api/me")
    public String me(Principal user) {
        return user.getName();
    }
}
