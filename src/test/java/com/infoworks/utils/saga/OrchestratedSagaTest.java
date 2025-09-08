package com.infoworks.utils.saga;

/**
 * The Saga Pattern is a design pattern used in distributed systems and microservices
 * architecture to manage long-running transactions and ensure data consistency across
 * services without using distributed transactions (2PC).
 */

public class OrchestratedSagaTest {

    /**
     * A saga is a sequence of local transactions. Each transaction updates one service and
     * publishes an event or sends a message to trigger the next step.
     * If a step fails, the saga executes compensating transactions (rollbacks) for the previous steps.
     *
     * Orchestration (Command-Based)
     * • A central orchestrator service manages the saga flow.
     * • It explicitly tells each service what to do next.
     * Pros:
     * • Easier to understand, monitor, and test
     * • Centralized logic
     * Cons:
     * • Introduces coupling to the orchestrator
     * • Orchestrator can become a bottleneck
     *
     * If a step fails, the orchestrator tells previous services to compensate.
     *
     * Compensation:
     * When something fails, each completed step must undo its work. That’s done with
     * compensating transactions — logic to reverse what was done (e.g., refund payment, restock
     * items).
     * This means you must explicitly code for rollback in each service.
     */
}
