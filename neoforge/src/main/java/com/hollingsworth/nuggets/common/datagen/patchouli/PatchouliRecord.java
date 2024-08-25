package src.main.java.com.hollingsworth.nuggets.common.datagen.patchouli;

import com.google.gson.JsonObject;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;

public record PatchouliRecord(PatchouliBuilder builder, Path path) {
    @Override
    public Path path() {
        return path;
    }

    public JsonObject build() {
        return builder.build();
    }

    public String relationPath(){
        String fileName = path.getFileName().toString();
        fileName = FilenameUtils.removeExtension(fileName);
        return builder.category.toString() + "/" + fileName;
    }
}
