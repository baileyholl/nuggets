package com.hollingsworth.nuggets.common.datagen;

import com.google.common.collect.Maps;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Copied from Advancements.Builder with extensions to reduce copy pasta
 */
public class DataAdvancementBuilder implements net.neoforged.neoforge.common.extensions.IAdvancementBuilderExtension {
    @Nullable
    private ResourceLocation parentId;
    @Nullable
    private AdvancementHolder parent;
    @Nullable
    private DisplayInfo display;
    private AdvancementRewards rewards = AdvancementRewards.EMPTY;
    private Map<String, Criterion<?>> criteria = Maps.newLinkedHashMap();
    @Nullable
    private AdvancementRequirements requirements;
    private AdvancementRequirements.Strategy requirementsStrategy = AdvancementRequirements.Strategy.AND;
    private String modid;
    private String fileKey;

    private DataAdvancementBuilder(@Nullable ResourceLocation pParentId, @Nullable DisplayInfo pDisplay, AdvancementRewards pRewards, Map<String, Criterion<?>> pCriteria, AdvancementRequirements pRequirements) {
        this.parentId = pParentId;
        this.display = pDisplay;
        this.rewards = pRewards;
        this.criteria = pCriteria;
        this.requirements = pRequirements;
    }

    private DataAdvancementBuilder(String modid, String fileKey) {
        this.modid = modid;
        this.fileKey = fileKey;
    }

    public static DataAdvancementBuilder builder(String modid, String fileKey) {
        return new DataAdvancementBuilder(modid, fileKey);
    }

    public DataAdvancementBuilder parent(AdvancementHolder parent) {
        this.parent = parent;
        return this.parent(parent.id());
    }

    public DataAdvancementBuilder parent(ResourceLocation pParentId) {
        this.parentId = pParentId;
        return this;
    }

    public DataAdvancementBuilder display(ItemStack pStack, Component pTitle, Component pDescription, @Nullable ResourceLocation pBackground, AdvancementType pFrame, boolean pShowToast, boolean pAnnounceToChat, boolean pHidden) {
        return this.display(new DisplayInfo(pStack, pTitle, pDescription, Optional.ofNullable(pBackground), pFrame, pShowToast, pAnnounceToChat, pHidden));
    }

    public DataAdvancementBuilder display(ItemLike pItem, Component pTitle, Component pDescription, @Nullable ResourceLocation pBackground, AdvancementType pFrame, boolean pShowToast, boolean pAnnounceToChat, boolean pHidden) {
        return this.display(new DisplayInfo(new ItemStack(pItem.asItem()), pTitle, pDescription, Optional.ofNullable(pBackground), pFrame, pShowToast, pAnnounceToChat, pHidden));
    }

    public DataAdvancementBuilder display(DisplayInfo pDisplay) {
        this.display = pDisplay;
        return this;
    }

    // The following displays cannot be used for roots.
    public DataAdvancementBuilder display(ItemStack pItem, AdvancementType pFrame) {
        return this.display(new DisplayInfo(pItem, this.getComponent("title"), this.getComponent("desc"), Optional.empty(), pFrame, true, true, false));
    }

    public DataAdvancementBuilder display(ItemLike pItem, AdvancementType pFrame) {
        return this.display(new ItemStack(pItem), pFrame);
    }

    // The following displays cannot be used for roots.
    public DataAdvancementBuilder display(ItemStack pItem, AdvancementType pFrame, boolean hidden) {
        return this.display(new DisplayInfo(pItem, this.getComponent("title"), this.getComponent("desc"), Optional.empty(), pFrame, true, true, hidden));
    }

    public DataAdvancementBuilder display(ItemLike pItem, AdvancementType pFrame, boolean hidden) {
        return this.display(new ItemStack(pItem), pFrame, hidden);
    }


    public DataAdvancementBuilder rewards(AdvancementRewards.Builder pRewardsBuilder) {
        return this.rewards(pRewardsBuilder.build());
    }

    public DataAdvancementBuilder rewards(AdvancementRewards pRewards) {
        this.rewards = pRewards;
        return this;
    }

    public DataAdvancementBuilder addCriterion(Criterion<?> pCriterion) {
        return this.addCriterion(fileKey, pCriterion);
    }

    public DataAdvancementBuilder addCriterion(String pKey, Criterion<?> pCriterion) {
        if (this.criteria.containsKey(pKey)) {
            throw new IllegalArgumentException("Duplicate criterion " + pKey);
        } else {
            this.criteria.put(pKey, pCriterion);
            return this;
        }
    }

    public DataAdvancementBuilder requirements(AdvancementRequirements.Strategy pStrategy) {
        this.requirementsStrategy = pStrategy;
        return this;
    }

    public DataAdvancementBuilder requirements(AdvancementRequirements pRequirements) {
        this.requirements = pRequirements;
        return this;
    }

    public DataAdvancementBuilder normalItemRequirement(ItemLike item) {
        return this.display(item, AdvancementType.TASK).requireItem(item);
    }

    public DataAdvancementBuilder requireItem(ItemLike item) {
        return this.addCriterion("has_" + BuiltInRegistries.ITEM.getKey(item.asItem()).getPath(), InventoryChangeTrigger.TriggerInstance.hasItems(item));
    }

    public MutableComponent getComponent(String type) {
        return Component.translatable(modid + ".adv." + type + "." + fileKey);
    }

    public Advancement build() {

        if (this.requirements == null) {
            this.requirements = this.requirementsStrategy.create(this.criteria.keySet());
        }
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("Advancement " + fileKey + " has no criteria " + this);
        }
        return new Advancement(Optional.ofNullable(this.parentId), Optional.ofNullable(this.display), this.rewards, this.criteria, this.requirements, false);
    }

    public AdvancementHolder save(Consumer<AdvancementHolder> pConsumer, ResourceLocation pId) {
        var adv =  this.build();
        AdvancementHolder advancement = new AdvancementHolder(pId, adv);
        pConsumer.accept(advancement);
        return advancement;
    }

    public AdvancementHolder save(Consumer<AdvancementHolder> pConsumer, String namespace) {
        return this.save(pConsumer, ResourceLocation.fromNamespaceAndPath(namespace, fileKey));
    }

    public String toString() {
        return "Task Advancement{parentId=" + this.parentId + ", display=" + this.display + ", rewards=" + this.rewards + ", criteria=" + this.criteria + ", requirements=" + this.requirements + "}";
    }

    public Map<String, Criterion<?>> getCriteria() {
        return this.criteria;
    }
}

