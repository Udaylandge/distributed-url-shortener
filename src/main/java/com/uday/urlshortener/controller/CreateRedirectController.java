package com.uday.urlshortener.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Backward-compatibility redirect from /create to /urls/create
 */
@Controller
public class CreateRedirectController {

    @GetMapping("/create")
    public String redirectToCreate() {
        return "redirect:/urls/create";
    }
}
