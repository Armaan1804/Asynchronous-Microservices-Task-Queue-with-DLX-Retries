package com.example.taskqueue.controller;

import com.example.taskqueue.config.RabbitMQConfig;
import com.example.taskqueue.model.Task;
import com.example.taskqueue.model.TaskPayload;
import com.example.taskqueue.repository.TaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final RabbitTemplate rabbitTemplate;
    private final TaskRepository taskRepository;

    @Autowired
    public TaskController(RabbitTemplate rabbitTemplate, TaskRepository taskRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.taskRepository = taskRepository;
    }

    @PostMapping
    public ResponseEntity<String> submitTask(@RequestParam String type, @RequestParam int duration) {
        String taskId = UUID.randomUUID().toString();

        // 1. Log to PostgreSQL
        Task taskEntity = new Task(taskId, type, duration, "QUEUED");
        taskRepository.save(taskEntity);

        // 2. Publish to RabbitMQ
        TaskPayload payload = new TaskPayload(taskId, type, duration);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                payload
        );

        System.out.println("Submitted task " + taskId + " to DB & Queue");

        return ResponseEntity.ok("Task submitted successfully! ID: " + taskId);
    }
}
