package com.ljh.sideproj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/notifications")
    public String notifications() {
        return "notifications";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/bids")
    public String bids() {
        return "bids";
    }

    @GetMapping("/bookmarks")
    public String bookmarks() {
        return "bookmarks";
    }

    @GetMapping("/bid-detail")
    public String bidDetail() {
        return "bid-detail";
    }

    @GetMapping("/my-bids")
    public String myBids() {
        return "my-bids";
    }
}
