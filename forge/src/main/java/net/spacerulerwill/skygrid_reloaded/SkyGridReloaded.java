package net.spacerulerwill.skygrid_reloaded;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.spacerulerwill.skygrid_reloaded.worldgen.SkyGridChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod(Constants.MOD_ID)
public class SkyGridReloaded {
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, Constants.MOD_ID);

    public SkyGridReloaded(FMLJavaModLoadingContext context) {
        CHUNK_GENERATORS.register("skygrid", () -> SkyGridChunkGenerator.MAP_CODEC);
        CHUNK_GENERATORS.register(context.getModEventBus());
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        Constants.LOGGER.info("SkyGrid Reloaded is loaded!");
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void reload(AddReloadListenerEvent event) {
            RegistryAccess wrapperLookup = event.getRegistryAccess();
            event.addListener(new PreparableReloadListener() {
                @Override
                public @NotNull CompletableFuture<Void> reload(@NotNull PreparationBarrier preparationBarrier, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller, @NotNull ProfilerFiller profilerFiller1, @NotNull Executor executor, @NotNull Executor executor1) {
                    return CompletableFuture.runAsync(() -> {
                        Common.onResourceManagerReload(wrapperLookup, resourceManager);
                    }, executor).thenCompose(preparationBarrier::wait);
                }
            });
        }
    }
}