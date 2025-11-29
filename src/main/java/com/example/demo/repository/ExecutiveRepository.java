package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Executive;

@Repository
public interface ExecutiveRepository extends JpaRepository<Executive, Long> {
    List<Executive> findByRole(String role);
    List<Executive> findByNameContainingIgnoreCase(String name);
}