package net.spacerulerwill.skygrid_reloaded;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class PresetsReloadListener implements SimpleSynchronousResourceReloadListener {
    private final HolderLookup.Provider wrapperLookup;

    public PresetsReloadListener(HolderLookup.Provider wrapperLookup) {
        this.wrapperLookup = wrapperLookup;
    }

    @Override
    public ResourceLocation getFabricId() {
        return Constants.PRESETS_RESOURCE_LOCATION;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        Common.onResourceManagerReload(wrapperLookup, manager);
    }
}
