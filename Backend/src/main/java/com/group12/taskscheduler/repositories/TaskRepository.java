package com.group12.taskscheduler.repositories;

import com.group12.taskscheduler.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Find tasks by weight
    List<Task> findByWeight(int weight);
    
    // Find tasks by due date
    List<Task> findByDueDate(LocalDate dueDate);
    
    // Find tasks with due date before a specific date
    List<Task> findByDueDateBefore(LocalDate date);
    
    // Find tasks with due date after a specific date
    List<Task> findByDueDateAfter(LocalDate date);
    
    // Find tasks by name (case-insensitive)
    List<Task> findByNameContainingIgnoreCase(String name);
} 