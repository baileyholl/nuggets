package src.main.java.com.hollingsworth.nuggets.common.debug;

/**
 * Attach this interface to an Entity or BlockEntity for tracking and exporting event logs
 */
public interface IDebuggerProvider {

    IDebugger getDebugger();

     default void addDebugEvent(DebugEvent event){
         this.addDebugEvent(event, false);
    }

    default void addDebugEvent(DebugEvent event, boolean storeDuplicate){
        getDebugger().addEntityEvent(event, storeDuplicate);
    }
}
