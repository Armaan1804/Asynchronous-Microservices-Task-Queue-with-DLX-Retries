package com.example.taskqueue.consumer;

import com.example.taskqueue.model.Task;
import com.example.taskqueue.model.TaskPayload;
import com.example.taskqueue.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskWorker {

    private static final Logger logger = LoggerFactory.getLogger(TaskWorker.class);
    private final TaskRepository taskRepository;

    @Autowired
    public TaskWorker(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @RabbitListener(queues = "task_queue")
    public void processTask(TaskPayload task) {
        // Retrieve task from DB or create if missing
        Task taskEntity = taskRepository.findById(task.getTaskId())
                .orElseGet(() -> new Task(task.getTaskId(), task.getTaskType(), task.getExecutionTimeMs(), "PROCESSING"));

        int currentAttempt = taskEntity.getRetryCount() + 1;
        taskEntity.setRetryCount(currentAttempt);
        taskEntity.setStatus("PROCESSING");
        taskRepository.save(taskEntity);

        logger.info("Received Task ID: {} [Type: {}] - Processing... Attempt: {}",
                task.getTaskId(), task.getTaskType(), currentAttempt);

        try {
            // Simulate task failure if taskType is "fail"
            if ("fail".equalsIgnoreCase(task.getTaskType())) {
                throw new RuntimeException("Simulated task processing failure");
            }

            // Simulate heavy workload execution time
            Thread.sleep(task.getExecutionTimeMs());

            // Success: Update database status
            taskEntity.setStatus("COMPLETED");
            taskEntity.setFailureReason(null); // clear failure reason on success
            taskRepository.save(taskEntity);
            logger.info("Successfully completed Task ID: {}", task.getTaskId());

        } catch (Exception e) {
            logger.error("Error processing Task ID: {} on attempt: {}", task.getTaskId(), currentAttempt);

            // Log failure reason to DB
            taskEntity.setFailureReason(e.getMessage() != null ? e.getMessage() : e.toString());

            if (currentAttempt >= 3) {
                taskEntity.setStatus("FAILED");
                logger.error("Task ID: {} exceeded max retries. Marking status as FAILED.", task.getTaskId());
            }
            taskRepository.save(taskEntity);

            // Re-throw exception so Spring AMQP knows the task failed and handles retries / DLX routing
            throw new RuntimeException(e);
        }
    }
}
