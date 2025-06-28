
package com.example.ticket.repository;

import com.example.ticket.model.Ticket;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository class for managing ticket persistence using in-memory storage.
 * 
 * <p>This repository implementation uses a ConcurrentHashMap to provide
 * thread-safe operations for storing and retrieving tickets
 * This approach is suitable for development and testing
 * but should be replaced with a proper database solution for production use.</p>
 * 
 * <p>The repository provides basic CRUD operations required by the system:</p>
 * <ul>
 *   <li>Save tickets (create or update)</li>
 *   <li>Find tickets by ID</li>
 *   <li>Retrieve all tickets (for filtering operations)</li>
 * </ul>
 * 
 * <p>Implementation Details:</p>
 * <ul>
 *   <li>Uses in-memory storage (ConcurrentHashMap) for persistence</li>
 *   <li>Thread Safety: All operations are thread-safe for concurrent access</li>
 *   <li>UUID-based keys for distributed-system-friendly identifiers</li>
 *   <li>Supports the filtering requirements for listing tickets</li>
 * </ul>
 * 
 * <p>Thread Safety: All operations are thread-safe due to the use of
 * ConcurrentHashMap, making this suitable for concurrent access in a
 * multi-threaded environment as required by the system specifications.</p>
 * 
 * @author Support Team
 * @version 1.0
 * @since 1.0
 */
@Repository
public class TicketRepository {
    
    /**
     * In-memory storage for tickets using a thread-safe ConcurrentHashMap.
     * The key is the ticket UUID and the value is the Ticket object.
     * 
     * <p>ConcurrentHashMap is used to ensure thread-safety when multiple
     * requests access the repository simultaneously, as required for the
     * microservice to handle concurrent ticket operations.</p>
     * 
     * <p>This implementation satisfies the requirement for in-memory storage
     * using ConcurrentHashMap or similar data structures.</p>
     */
    private final Map<UUID, Ticket> tickets = new ConcurrentHashMap<>();

    /**
     * Saves a ticket to the repository.
     * 
     * <p>If a ticket with the same ID already exists, it will be replaced.
     * This method can be used for both creating new tickets and updating
     * existing ones, supporting all the CRUD operations required by the system.</p>
     * 
     * <p>This method supports:</p>
     * <ul>
     *   <li>Creating new tickets (submit new support ticket requirement)</li>
     *   <li>Updating existing tickets (status updates, comment additions)</li>
     *   <li>Thread-safe operations for concurrent access</li>
     * </ul>
     * 
     * @param ticket the ticket to save, must not be null
     * @return the saved ticket (same instance that was passed in)
     * @throws NullPointerException if ticket or ticket.getTicketId() is null
     */
    public Ticket save(Ticket ticket) {
        tickets.put(ticket.getTicketId(), ticket);
        return ticket;
    }

    /**
     * Finds a ticket by its unique identifier.
     * 
     * <p>This method supports the requirement for updating ticket status
     * and adding comments by allowing retrieval of specific tickets.</p>
     * 
     * @param id the UUID of the ticket to find, must not be null
     * @return an Optional containing the ticket if found, empty Optional otherwise
     * @throws NullPointerException if id is null
     */
    public Optional<Ticket> findById(UUID id) {
        return Optional.ofNullable(tickets.get(id));
    }

    /**
     * Retrieves all tickets stored in the repository.
     * 
     * <p>Returns a collection view of all tickets to support the requirement
     * for listing tickets with filtering options. The returned collection
     * is backed by the internal map, so changes to the tickets will be
     * reflected in the repository.</p>
     * 
     * <p>This method enables:</p>
     * <ul>
     *   <li>Listing all tickets</li>
     *   <li>Filtering by status, userId, and assigneeId</li>
     *   <li>Supporting the GET /tickets endpoint requirements</li>
     * </ul>
     * 
     * @return a Collection of all tickets in the repository
     */
    public Collection<Ticket> findAll() {
        return tickets.values();
    }
}
