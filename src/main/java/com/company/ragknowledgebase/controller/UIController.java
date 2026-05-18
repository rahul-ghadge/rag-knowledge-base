package com.company.ragknowledgebase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the interactive web UI (Thymeleaf + Vanilla JS).
 */
@Controller
public class UIController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "RAG Knowledge Base");
        return "index";
    }
}
