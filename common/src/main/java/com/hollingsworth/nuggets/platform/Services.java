package com.hollingsworth.nuggets.platform;

import java.util.ServiceLoader;

public class Services {

    public static final IPlatformHelper PLATFORM = ServiceLoader.load(IPlatformHelper.class).findFirst()
            .orElseThrow(() -> new NullPointerException("Failed to load service for " + IPlatformHelper.class.getName()));
}
