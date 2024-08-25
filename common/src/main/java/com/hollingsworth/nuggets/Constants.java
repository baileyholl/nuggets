package com.hollingsworth.nuggets;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	public static final String MOD_ID = "nuggets";
	public static final String MOD_NAME = "Nuggets";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static ResourceLocation prefix(String path) {
		return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
	}
}