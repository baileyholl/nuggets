package com.hollingsworth.nuggets.common.datagen;

import com.hollingsworth.nuggets.common.datagen.patchouli.IPatchouliPage;
import com.hollingsworth.nuggets.common.datagen.patchouli.PatchouliBuilder;
import com.hollingsworth.nuggets.common.datagen.patchouli.PatchouliRecord;
import com.hollingsworth.nuggets.common.datagen.patchouli.TextPage;
import com.hollingsworth.nuggets.common.registry.RegistryHelper;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;



public abstract class PatchouliProvider extends SimpleDataProvider{
    public List<PatchouliRecord> pages = new ArrayList<>();
    public String modId;
    public String bookName;

    public PatchouliProvider(String modId, String bookName, DataGenerator generatorIn) {
        super(generatorIn);
        this.modId = modId;
    }

    public abstract void addEntries(List<PatchouliRecord> pages);

    public String getLangPath(String name, int count) {
        return modId + ".page" + count + "." + name;
    }

    public String getLangPath(String name) {
        return modId + ".page." + name;
    }

    public PatchouliRecord addPage(PatchouliBuilder builder, Path path) {
        return addPage(new PatchouliRecord(builder, path));
    }

    public PatchouliRecord addPage(PatchouliRecord patchouliPage){
        this.pages.add(patchouliPage);
        return patchouliPage;
    }

    public PatchouliBuilder buildBasicItem(ItemLike item, ResourceLocation category, IPatchouliPage recipePage) {
        PatchouliBuilder builder = new PatchouliBuilder(category, item.asItem().getDescriptionId())
                .withIcon(item.asItem())
                .withPage(new TextPage("ars_nouveau.page." + RegistryHelper.getRegistryName(item.asItem()).getPath()));
        if (recipePage != null) {
            builder.withPage(recipePage);
        }
        return builder;
    }

    public PatchouliRecord addBasicItem(ItemLike item, ResourceLocation category, IPatchouliPage recipePage) {
        PatchouliBuilder builder = buildBasicItem(item, category, recipePage);
        return addPage(new PatchouliRecord(builder, getPath(category, RegistryHelper.getRegistryName(item.asItem()))));
    }

    public Path getPath(ResourceLocation category, ResourceLocation fileName) {
        return this.output.resolve("assets/" + modId + "/patchouli_books/" + bookName + "/en_us/entries/" + category.getPath() + "/" + fileName.getPath() + ".json");
    }

    public Path getPath(ResourceLocation category, String fileName) {
        return this.output.resolve("assets/" + modId + "/patchouli_books/" + bookName + "/en_us/entries/" + category.getPath() + "/" + fileName + ".json");
    }

    @Override
    public void collectJsons(CachedOutput pOutput) {
        addEntries(this.pages);
        for (PatchouliRecord patchouliPage : pages) {
            saveStable(pOutput, patchouliPage.build(), patchouliPage.path());
        }
    }

    @Override
    public String getName() {
        return "Patchouli";
    }
}
