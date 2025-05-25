package net.spacerulerwill.skygrid_reloaded;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod(Constants.MOD_ID)
public class SkyGridReloaded {
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(BuiltInRegistries.CHUNK_GENERATOR, Constants.MOD_ID);

    public SkyGridReloaded(IEventBus eventBus) {
        CHUNK_GENERATORS.register("skygrid", resourceLocation -> SkyGridChunkGenerator.MAP_CODEC);
        CHUNK_GENERATORS.register(eventBus);
        NeoForge.EVENT_BUS.register(EventHandler.class);
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void reload(AddReloadListenerEvent event) {
            HolderLookup.Provider wrapperLookup = event.getServerResources().getRegistryLookup();
            event.addListener(new PreparableReloadListener() {
                @Override
                public @NotNull CompletableFuture<Void> reload(
                        @NotNull PreparationBarrier preparationBarrier,
                        @NotNull ResourceManager resourceManager,
                        @NotNull ProfilerFiller profileFiller,
                        @NotNull ProfilerFiller profilerFiller1,
                        @NotNull Executor backgroundExecutor,
                        @NotNull Executor gameExecutor) {

                    return CompletableFuture.runAsync(() -> {
                        Common.onResourceManagerReload(wrapperLookup, resourceManager);
                    }, backgroundExecutor).thenCompose(preparationBarrier::wait);
                }
            });
        }
    }
}
