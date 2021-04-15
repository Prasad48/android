package com.useriq.sdk;


import java.util.HashMap;
import java.util.Map;

/**
 * A simple event based state machine.
 *
 * @param <State> The state of the entity
 * @param <EventType> The event type to be handled
 *
 * @author sudhakar
 * @created 11-Oct-2018
 */
public final class StateMachine<State extends Enum<State>, EventType extends Enum<EventType>, Context> {
    private Node<State, EventType, Context> current;
    private Listener<Context, State> listener;

    private StateMachine(Node<State, EventType, Context> node, Listener<Context, State> listener) {
        this.current = node;
        this.listener = listener;
    }

    /**
     * Apply an event to the state machine with no context object.
     */
    public boolean apply(EventType eventType) {
        return apply(eventType, null);
    }

    /**
     * Apply an event to the state machine.
     *
     * @param eventType The event type to be handled
     * @param context A context to pass into the transition function
     * @return true if transition is valid & applied
     */
    public boolean apply(EventType eventType, Context context) {
        Node<State, EventType, Context> nextNode = current.getNeighbor(eventType);

        if (nextNode == null) return false;

        listener.beforeChange(nextNode.state, context);

        current = nextNode;
        return true;
    }

    /**
     * @return The current state of the state machine
     */
    public State getState() {
        return current.state;
    }

    /**
     * A callback to execute during state transition.
     *
     * @param <Context> A context object which may be provided when applying an event.
     */
    @FunctionalInterface
    public interface Listener<Context, State> {
        void beforeChange(State nextState, Context c);
    }

    /**
     * A builder for simple event based state machines
     *
     * @param <State> The state of the entity
     * @param <EventType> The event type to be handled
     */
    static final public class Builder<State extends Enum<State>, EventType extends Enum<EventType>, Context> {
        private final Map<State, Node<State, EventType, Context>> nodes;
        private final Node<State, EventType, Context> root;

        /**
         * @param initialState the initial state of the state machine
         */
        Builder(State initialState) {
            nodes = new HashMap<>();
            root = new Node<>(initialState);
            nodes.put(initialState, root);
        }

        /**
         * Use this method to construct the state machine after
         * completing the declaration of state machine
         * topology and listeners.
         *
         * @return the final state machine
         */
        public StateMachine<State, EventType, Context> build(Listener<Context, State> listener) {
            return new StateMachine<>(root, listener);
        }

        /**
         * Add a transition to the state machine from "startState"
         * to "endState" in response to events of type "eventType"
         *
         * @param startState the starting state of the transition
         * @param eventType the event type that triggered the transition
         * @param endState the end state of the transition
         */
        Builder<State, EventType, Context> addTransition(State startState, EventType eventType, State endState) {
            Node<State, EventType, Context> startNode = nodes.get(startState);

            if (startNode == null) {
                startNode = new Node<>(startState);
                nodes.put(startState, startNode);
            }

            Node<State, EventType, Context> endNode = nodes.get(endState);

            if (endNode == null) {
                endNode = new Node<>(endState);
                nodes.put(endState, endNode);
            }

            startNode.addNeighbor(eventType, endNode);

            return this;
        }
    }

    static final class Node<State extends Enum<State>, EventType extends Enum<EventType>, Context> {
        private final Map<EventType, Node<State, EventType, Context>> neighbors = new HashMap<>();

        final State state;

        Node(State state) {
            this.state = state;
        }

        Node<State, EventType, Context> getNeighbor(EventType eventType) {
            return neighbors.get(eventType);
        }

        void addNeighbor(EventType eventType, Node<State, EventType, Context> destination) {
            neighbors.put(eventType, destination);
        }
    }
}