package src.main.java.com.hollingsworth.nuggets.common.debug;

import java.io.PrintWriter;

/**
 * Interface for a debugger that can store and write debug events.
 * See {@link EntityDebugger} for an example implementation.
 */
public interface IDebugger {

    default void addEntityEvent(DebugEvent event){
        addEntityEvent(event, false);
    }

    void addEntityEvent(DebugEvent event, boolean storeDuplicate);

    void writeFile(PrintWriter writer);
}
