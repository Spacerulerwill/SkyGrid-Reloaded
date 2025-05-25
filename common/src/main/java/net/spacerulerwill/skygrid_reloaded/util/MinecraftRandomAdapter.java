package net.spacerulerwill.skygrid_reloaded.util;

import net.minecraft.util.RandomSource;
import org.apache.commons.rng.UniformRandomProvider;

public class MinecraftRandomAdapter implements UniformRandomProvider {
    private final RandomSource minecraftRandom;

    public MinecraftRandomAdapter() {
        this.minecraftRandom = RandomSource.create();
    }

    public MinecraftRandomAdapter(RandomSource minecraftRandom) {
        this.minecraftRandom = minecraftRandom;
    }

    @Override
    public long nextLong() {
        return minecraftRandom.nextLong();
    }
}
