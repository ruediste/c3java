package net.lshift.fsa;

import java.beans.PropertyChangeEvent;

public class StateChangeEvent<S extends Enum<S>, E extends Enum<E>>
    extends PropertyChangeEvent
{
    private static final long serialVersionUID = 1L;
    private final E cause;
    private final int version;

    public StateChangeEvent(
        StateMachine<S,E> source,
        S oldValue, S newValue,
        E cause,
        int version)
    {
        super(source, "state", oldValue, newValue);
        this.cause = cause;
        this.version = version;
    }

    @SuppressWarnings("unchecked")
    public S getOldState() {
        return (S)getOldValue();
    }

    @SuppressWarnings("unchecked")
    public S getNewState() {
        return (S)getNewValue();
    }

    public int getVersion() {
        return version;
    }

    public E getCause() {
        return cause;
    }


}
