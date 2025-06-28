
package com.example.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Support Ticket System.
 * 
 * <p>This application provides a RESTful API for managing support tickets,
 * including functionality to create tickets, update their status, and add comments.
 * The system uses an in-memory storage solution with ConcurrentHashMap for
 * thread-safe operations.</p>
 * 
 * <p>Key features include:</p>
 * <ul>
 *   <li>Ticket creation and management</li>
 *   <li>Status transitions with validation</li>
 *   <li>Comment system with visibility controls</li>
 *   <li>Filtering and querying capabilities</li>
 * </ul>
 * 
 * @author Support Team
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
public class TicketApplication {
    
    /**
     * Main method to start the Spring Boot application.
     * 
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(TicketApplication.class, args);
    }
}
