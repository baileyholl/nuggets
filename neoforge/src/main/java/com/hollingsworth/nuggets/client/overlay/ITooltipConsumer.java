package src.main.java.com.hollingsworth.nuggets.client.overlay;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;


@FunctionalInterface
public interface ITooltipConsumer {

    void accept(EntityHitResult result, List<Component> tooltip);
}
