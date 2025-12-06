package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Event;
import com.example.demo.model.Executive;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.ExecutiveRepository;


@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final EventRepository eventRepo;
    private final ExecutiveRepository execRepo;

    public AdminController(EventRepository eventRepo, ExecutiveRepository execRepo){
        this.eventRepo=eventRepo;
        this.execRepo=execRepo;
    }

    @GetMapping("/executives")
    public List<Executive> getAllExecutives() {
        return execRepo.findAll();
    }
    
    @GetMapping("/executives/{id}")
    public ResponseEntity<Executive> getExecutiveById(@PathVariable Long id) {
        return execRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/executives")
    public ResponseEntity<Executive> createExecutive(@RequestBody Executive executive){
        Executive saved = execRepo.save(executive);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/executives")
    public ResponseEntity<Executive> updateExecutive(@PathVariable Long id, @RequestBody Executive executive) {
        return execRepo.findById(id)
            .map(existing -> {
                executive.setId(id);
                Executive updated = execRepo.save(executive);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/executives/{id}")
    public ResponseEntity<Void> deleteExecutive(@PathVariable Long id) {
        if (execRepo.existsById(id)) {
            execRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/events")
    public List<Event> getAllEvents() {
        return eventRepo.findAll();
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/events")
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event saved = eventRepo.save(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event event) {
        return eventRepo.findById(id)
            .map(existing -> {
                event.setId(id);
                Event updated = eventRepo.save(event);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        if (eventRepo.existsById(id)) {
            eventRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}
