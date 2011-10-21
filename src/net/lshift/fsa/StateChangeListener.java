package net.lshift.fsa;

import java.util.EventListener;

public interface StateChangeListener<S extends Enum<S>, E extends Enum<E>>
extends EventListener {
    public void stateChange(StateChangeEvent<S,E> event);
}
