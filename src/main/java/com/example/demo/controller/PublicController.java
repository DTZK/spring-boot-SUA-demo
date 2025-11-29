package com.example.demo.controller;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final EventRepository eventRepo;
    private final ExecutiveRepository execRepo;

    public PublicController(EventRepository eventRepo, ExecutiveRepository execRepo) {
        this.eventRepo = eventRepo; this.execRepo = execRepo;
    }

    @GetMapping("/events")
    public List<Event> featured() {
        return eventRepo.findByFeaturedTrueOrderByDateDesc().stream().limit(3).toList();
    }

    @GetMapping("/executives")
    public List<Executive> executives() { return execRepo.findAll(); }
}

