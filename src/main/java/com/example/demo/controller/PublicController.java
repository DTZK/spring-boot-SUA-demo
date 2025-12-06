package com.example.demo.controller;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Event;
import com.example.demo.model.Executive;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.ExecutiveRepository;
@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final EventRepository eventRepo;
    private final ExecutiveRepository execRepo;

    public PublicController(EventRepository eventRepo, ExecutiveRepository execRepo) {
        this.eventRepo = eventRepo; 
        this.execRepo = execRepo;
    }

    @GetMapping("/events")
    public List<Event> featured() {
        return eventRepo.findByFeaturedTrueOrderByDateDesc().stream().limit(3).toList();
    }

    @GetMapping("/executives")
    public List<Executive> executives() { 
        return execRepo.findAll(); 
    }
}

