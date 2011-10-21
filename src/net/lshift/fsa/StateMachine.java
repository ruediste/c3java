package net.lshift.fsa;

import static java.lang.String.format;
import static net.lshift.java.util.Maps.entry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class StateMachine<S extends Enum<S>, E extends Enum<E>> {

    public interface Action {
        public void invoke();
    }

    public static class Transition<S extends Enum<S>, E extends Enum<E>> {
        public final E trigger;
        public final Action [] actions;
        public final S result;

        public Transition(E trigger, Action [] actions, S result)
        {
            super();
            this.trigger = trigger;
            this.actions = actions;
            this.result = result;
        }
    }

    public static <S extends Enum<S>, E extends Enum<E>> Transition<S,E>  transition(
        E trigger,
        S result,
        Action... actions) {
        return new Transition<S,E>(trigger, actions, result);
    }

    public static <S extends Enum<S>, E extends Enum<E>>
    Map.Entry<S,Map<E, Transition<S,E>>> transitions(
        S state, Transition<S,E> ... transitions) {
        Map<E, Transition<S,E>> map = new HashMap<E, Transition<S,E>>();
        for(Transition<S,E> transition: transitions)
            if(map.put(transition.trigger, transition) != null)
                throw new IllegalArgumentException("Duplicate transition");
        return entry(state, Collections.unmodifiableMap(map));
    }

    public final Map<S,Map<E,Transition<S,E>>> transitions;
    public final Class<S> states;
    public final Class<E> events;
    public final S initial;

    private S state;
    private int stateCounter = 0;
    private Set<StateChangeListener<S,E>> listeners =
        new HashSet<StateChangeListener<S,E>>();

    public StateMachine(
        Class<S> states,
        Class<E> events,
        S initial,
        Map<S,Map<E,Transition<S,E>>> transitions) {

        Set<S> stateSet = new HashSet<S>(Arrays.asList(states.getEnumConstants()));
        if(!stateSet.equals(transitions.keySet()))
            throw new IllegalArgumentException("Transitions not defined for all states");

        this.states = states;
        this.events = events;
        this.transitions = transitions;
        this.initial = initial;
        this.state = initial;
    }


    public final synchronized void trigger(E event) {
        if(!offer(event)) {
            throw new IllegalStateException(
                String.format("No transition for event %1$s in state %2$s", event.name(), state.name()));
        }
    }

    public final synchronized boolean test(E event) {
        return this.transitions.get(state).get(event) != null;
    }

    public final synchronized boolean offer(E event) {
        Transition<S,E> transition = this.transitions.get(state).get(event);
        if(transition == null)
            return false;

        StateChangeEvent<S, E> changeEvent = newStateChangeEvent(event, transition);
        for(Action action: transition.actions)
            action.invoke();
        this.state = transition.result;
        this.stateCounter++;
        notifyStateChangeEvent(changeEvent);

        return true;
    }

    private StateChangeEvent<S, E> newStateChangeEvent(E event, Transition<S, E> transition) {
        return new StateChangeEvent<S,E>(
            this, state, transition.result, event, stateCounter);
    }

    private void notifyStateChangeEvent(StateChangeEvent<S, E> changeEvent) {
        for(StateChangeListener<S, E> listener: listeners)
            listener.stateChange(changeEvent);
    }


    public Runnable triggerCommand(final E event) {
        return new Runnable() {
            public void run() {
                trigger(event);
            }
        };
    }

    public S triggerAndWaitFor(E event, S ... stateList) throws InterruptedException {
        final Collection<S> states = Arrays.asList(stateList);
        final BlockingQueue<S> queue = new ArrayBlockingQueue<S>(1);
        final StateChangeListener<S,E> listener = new StateChangeListener<S,E>() {
            public void stateChange(StateChangeEvent<S, E> event) {
                // Note: use offer. Once we have sent one event, offer will
                // return false. This is fine: the scheduler won't be blocked,
                // and its not an error when this happens. I originally thought
                // synchronized queue would do this, however it has a queue length
                // of zero. If no-one is listening yet, offer will fail, which
                // would be a race, and if you use put() the state machine will
                // block, which is undesirable
                if(states.contains(event.getNewState())) {
                    queue.offer(event.getNewState());
                }
            }
        };

        try {
            synchronized(this) {
                this.addStateChangeListener(listener);
                this.trigger(event);
            }

            return queue.take();
        }
        finally {
            // Note: it doesn't matter that the listener may not have been added
            this.removeStateChangeListener(listener);
        }
    }

    public synchronized boolean addStateChangeListener(StateChangeListener<S, E> e) {
        return listeners.add(e);
    }

    public synchronized boolean removeStateChangeListener(Object o) {
        return listeners.remove(o);
    }

    public synchronized void assertState(S ... states) {
        List<S> stateSet = Arrays.asList(states);
        if(!stateSet.contains(state))
            throw new IllegalStateException(format("%1$s not in %1$s", state, stateSet));
    }

    public synchronized S getState() {
        return state;
    }
}
