package com.infoworks.utils.saga;

/**
 * The Saga Pattern is a design pattern used in distributed systems and microservices
 * architecture to manage long-running transactions and ensure data consistency across
 * services without using distributed transactions (2PC).
 */
public class ChoreographedSagaTest {

    /**
     * A saga is a sequence of local transactions. Each transaction updates one service and
     * publishes an event or sends a message to trigger the next step.
     * If a step fails, the saga executes compensating transactions (rollbacks) for the previous steps.
     *
     * Choreography (Event-Based)
     * • Services listen to events and react.
     * • No central coordinator.
     * • Each service emits events to trigger the next service.
     * Pros:
     * • Decentralized
     * • Simple to implement with messaging/event buses
     * Cons:
     * • Harder to manage and monitor
     * • Difficult to debug
     * • Event ordering issues
     *
     * If a step fails, the service must emit a compensating event.
     *
     * Compensation:
     * When something fails, each completed step must undo its work. That’s done with
     * compensating transactions — logic to reverse what was done (e.g., refund payment, restock
     * items).
     * This means you must explicitly code for rollback in each service.
     */
}
