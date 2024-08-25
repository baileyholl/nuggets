package src.main.java.com.hollingsworth.nuggets.common.debug;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.io.PrintWriter;

public class EntityDebugger implements IDebugger{
    public FixedStack<EntityEvent> events = new FixedStack<>(100);

    public final Entity entity;

    public EntityDebugger(Entity entity) {
        this(entity, 100);
    }

    public EntityDebugger(Entity entity, int size) {
        this.entity = entity;
        events = new FixedStack<>(size);
    }

    @Override
    public void addEntityEvent(DebugEvent event, boolean storeDuplicate) {
        // Do not store duplicate events back to back with the same ID
        if(storeDuplicate || events.isEmpty() || !events.peek().id.equals(event.id)){
            events.push(new EntityEvent(entity, event.id, event.message));
        }
    }

    @Override
    public void writeFile(PrintWriter writer) {
        writer.print("Entity: " + " (" + entity.getClass().getSimpleName() + ")");
        // print current entity goal
        if(entity instanceof Mob mob){
            for(WrappedGoal goal : mob.goalSelector.getAvailableGoals().stream().filter(WrappedGoal::isRunning).toList()){
                writer.println("Running Goal: " + goal.getGoal().getClass().getSimpleName());
            }
        }
        for(EntityEvent event : events){
            writer.println(event.toString());
        }
    }
}
