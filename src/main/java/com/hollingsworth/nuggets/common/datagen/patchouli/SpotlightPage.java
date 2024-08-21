package com.hollingsworth.nuggets.common.datagen.patchouli;

import com.hollingsworth.nuggets.common.registry.RegistryHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class SpotlightPage extends AbstractPage {

    public SpotlightPage(String itemString) {
        object.addProperty("item", itemString);
    }

    public SpotlightPage(ItemLike itemLike) {
        this(RegistryHelper.getRegistryName(itemLike.asItem()).toString());
    }

    public SpotlightPage withTitle(String title) {
        object.addProperty("title", title);
        return this;
    }

    public SpotlightPage linkRecipe(boolean link) {
        object.addProperty("link_recipe", link);
        return this;
    }

    public SpotlightPage withText(String text) {
        object.addProperty("text", text);
        return this;
    }

    @Override
    public ResourceLocation getType() {
        return ResourceLocation.tryParse("patchouli:spotlight");
    }
}
