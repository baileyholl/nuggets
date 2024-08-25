package src.main.java.com.hollingsworth.nuggets.common.state_machine;

import src.main.java.com.hollingsworth.nuggets.Nuggets;

import javax.annotation.Nonnull;

public class SimpleStateMachine<State extends IState, Event extends IStateEvent> {

    protected State currentState;

    public boolean debug;

    public SimpleStateMachine(@Nonnull State initialState) {
        currentState = initialState;
        currentState.onStart();
    }

    protected void changeState(@Nonnull State nextState) {
        if(debug){
            Nuggets.logger.debug("Changing state from {} to {}", currentState, nextState);
        }
        currentState.onEnd();
        currentState = nextState;
        currentState.onStart();
    }

    public void tick() {
        if(currentState == null)
            return;
        IState nextState = currentState.tick();
        if (nextState != null) {
            changeState((State)nextState);
        }
    }

    public void onEvent(Event event) {
        IState nextState = currentState.onEvent(event);
        if (nextState != null) {
            changeState((State) nextState);
        }
    }

    public State getCurrentState(){
        return currentState;
    }

    public SimpleStateMachine<State, Event> setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
}
