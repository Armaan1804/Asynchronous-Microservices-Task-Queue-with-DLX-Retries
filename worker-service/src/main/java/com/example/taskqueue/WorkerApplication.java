package com.example.taskqueue;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Declare the queue with the exact same DLX arguments to prevent mismatches
    @Bean
    public Queue taskQueue() {
        return QueueBuilder.durable("task_queue")
                .withArgument("x-dead-letter-exchange", "task_dlx")
                .withArgument("x-dead-letter-routing-key", "task_routing_key_dlq")
                .build();
    }
}
