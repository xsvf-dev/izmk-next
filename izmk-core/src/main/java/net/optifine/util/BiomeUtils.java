package net.optifine.util;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.*;
import net.optifine.config.BiomeId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class BiomeUtils {
    private static final ArrayList<ResourceKey<Biome>> biomeKeys = new ArrayList<>();
    private static final Registry<Biome> defaultBiomeRegistry = makeDefaultBiomeRegistry();
    private static Level biomeWorld = Minecraft.getInstance().level;

    static {
        biomeKeys.add(Biomes.THE_VOID);
        biomeKeys.add(Biomes.PLAINS);
        biomeKeys.add(Biomes.SUNFLOWER_PLAINS);
        biomeKeys.add(Biomes.SNOWY_PLAINS);
        biomeKeys.add(Biomes.ICE_SPIKES);
        biomeKeys.add(Biomes.DESERT);
        biomeKeys.add(Biomes.SWAMP);
        biomeKeys.add(Biomes.MANGROVE_SWAMP);
        biomeKeys.add(Biomes.FOREST);
        biomeKeys.add(Biomes.FLOWER_FOREST);
        biomeKeys.add(Biomes.BIRCH_FOREST);
        biomeKeys.add(Biomes.DARK_FOREST);
        biomeKeys.add(Biomes.OLD_GROWTH_BIRCH_FOREST);
        biomeKeys.add(Biomes.OLD_GROWTH_PINE_TAIGA);
        biomeKeys.add(Biomes.OLD_GROWTH_SPRUCE_TAIGA);
        biomeKeys.add(Biomes.TAIGA);
        biomeKeys.add(Biomes.SNOWY_TAIGA);
        biomeKeys.add(Biomes.SAVANNA);
        biomeKeys.add(Biomes.SAVANNA_PLATEAU);
        biomeKeys.add(Biomes.WINDSWEPT_HILLS);
        biomeKeys.add(Biomes.WINDSWEPT_GRAVELLY_HILLS);
        biomeKeys.add(Biomes.WINDSWEPT_FOREST);
        biomeKeys.add(Biomes.WINDSWEPT_SAVANNA);
        biomeKeys.add(Biomes.JUNGLE);
        biomeKeys.add(Biomes.SPARSE_JUNGLE);
        biomeKeys.add(Biomes.BAMBOO_JUNGLE);
        biomeKeys.add(Biomes.BADLANDS);
        biomeKeys.add(Biomes.ERODED_BADLANDS);
        biomeKeys.add(Biomes.WOODED_BADLANDS);
        biomeKeys.add(Biomes.MEADOW);
        biomeKeys.add(Biomes.CHERRY_GROVE);
        biomeKeys.add(Biomes.GROVE);
        biomeKeys.add(Biomes.SNOWY_SLOPES);
        biomeKeys.add(Biomes.FROZEN_PEAKS);
        biomeKeys.add(Biomes.JAGGED_PEAKS);
        biomeKeys.add(Biomes.STONY_PEAKS);
        biomeKeys.add(Biomes.RIVER);
        biomeKeys.add(Biomes.FROZEN_RIVER);
        biomeKeys.add(Biomes.BEACH);
        biomeKeys.add(Biomes.SNOWY_BEACH);
        biomeKeys.add(Biomes.STONY_SHORE);
        biomeKeys.add(Biomes.WARM_OCEAN);
        biomeKeys.add(Biomes.LUKEWARM_OCEAN);
        biomeKeys.add(Biomes.DEEP_LUKEWARM_OCEAN);
        biomeKeys.add(Biomes.OCEAN);
        biomeKeys.add(Biomes.DEEP_OCEAN);
        biomeKeys.add(Biomes.COLD_OCEAN);
        biomeKeys.add(Biomes.DEEP_COLD_OCEAN);
        biomeKeys.add(Biomes.FROZEN_OCEAN);
        biomeKeys.add(Biomes.DEEP_FROZEN_OCEAN);
        biomeKeys.add(Biomes.MUSHROOM_FIELDS);
        biomeKeys.add(Biomes.DRIPSTONE_CAVES);
        biomeKeys.add(Biomes.LUSH_CAVES);
        biomeKeys.add(Biomes.DEEP_DARK);
        biomeKeys.add(Biomes.NETHER_WASTES);
        biomeKeys.add(Biomes.WARPED_FOREST);
        biomeKeys.add(Biomes.CRIMSON_FOREST);
        biomeKeys.add(Biomes.SOUL_SAND_VALLEY);
        biomeKeys.add(Biomes.BASALT_DELTAS);
        biomeKeys.add(Biomes.THE_END);
        biomeKeys.add(Biomes.END_HIGHLANDS);
        biomeKeys.add(Biomes.END_MIDLANDS);
        biomeKeys.add(Biomes.SMALL_END_ISLANDS);
        biomeKeys.add(Biomes.END_BARRENS);
    }

    public static void onWorldChanged(Level worldIn) {
        biomeRegistry = getBiomeRegistry(worldIn);
        biomeWorld = worldIn;
        PLAINS = biomeRegistry.get(Biomes.PLAINS);
        SUNFLOWER_PLAINS = biomeRegistry.get(Biomes.SUNFLOWER_PLAINS);
        SNOWY_PLAINS = biomeRegistry.get(Biomes.SNOWY_PLAINS);
        ICE_SPIKES = biomeRegistry.get(Biomes.ICE_SPIKES);
        DESERT = biomeRegistry.get(Biomes.DESERT);
        WINDSWEPT_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_HILLS);
        WINDSWEPT_GRAVELLY_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_GRAVELLY_HILLS);
        MUSHROOM_FIELDS = biomeRegistry.get(Biomes.MUSHROOM_FIELDS);
        SWAMP = biomeRegistry.get(Biomes.SWAMP);
        MANGROVE_SWAMP = biomeRegistry.get(Biomes.MANGROVE_SWAMP);
//        THE_VOID = biomeRegistry.get(Biomes.THE_VOID);
    }

    private static Biome getBiomeSafe(Registry<Biome> registry, ResourceKey<Biome> biomeKey, Supplier<Biome> biomeDefault) {
        Biome biome = registry.get(biomeKey);
        if (biome == null) {
            biome = biomeDefault.get();
        }

        return biome;
    }

    public static Registry<Biome> getBiomeRegistry(Level worldIn) {
        if (worldIn != null) {
            if (worldIn == biomeWorld) {
                return biomeRegistry;
            } else {
                Registry<Biome> registry = worldIn.registryAccess().registryOrThrow(Registries.BIOME);
                return fixBiomeIds(defaultBiomeRegistry, registry);
            }
        } else {
            return defaultBiomeRegistry;
        }
    }

    private static Registry<Biome> makeDefaultBiomeRegistry() {
        MappedRegistry<Biome> mappedregistry = new MappedRegistry<>(ResourceKey.createRegistryKey(ResourceLocation.tryParse("biomes")), Lifecycle.stable(), true);

        for (ResourceKey<Biome> resourcekey : biomeKeys) {
            Biome.BiomeBuilder biome$biomebuilder = new Biome.BiomeBuilder();
            biome$biomebuilder.hasPrecipitation(false);
            biome$biomebuilder.temperature(0.0F);
            biome$biomebuilder.downfall(0.0F);
            biome$biomebuilder.specialEffects((new BiomeSpecialEffects.Builder()).fogColor(0).waterColor(0).waterFogColor(0).skyColor(0).build());
            biome$biomebuilder.mobSpawnSettings((new MobSpawnSettings.Builder()).build());
            biome$biomebuilder.generationSettings((new BiomeGenerationSettings.Builder(null, null)).build());
            Biome biome = biome$biomebuilder.build();
            mappedregistry.createIntrusiveHolder(biome);
            mappedregistry.register(resourcekey, biome, Lifecycle.stable());
        }

        return mappedregistry;
    }

    private static Registry<Biome> fixBiomeIds(Registry<Biome> idRegistry, Registry<Biome> valueRegistry) {
        MappedRegistry<Biome> mappedregistry = new MappedRegistry<>(ResourceKey.createRegistryKey(ResourceLocation.tryParse("biomes")), Lifecycle.stable(), true);

        for (ResourceKey<Biome> resourcekey : idRegistry.registryKeySet()) {
            Biome biome = valueRegistry.get(resourcekey);
            if (biome == null) {
                biome = idRegistry.get(resourcekey);
            }

            int i = idRegistry.getId(idRegistry.get(resourcekey));
            mappedregistry.createIntrusiveHolder(biome);
            Holder.Reference<Biome> holder$reference = mappedregistry.registerMapping(i, resourcekey, biome, Lifecycle.stable());
        }

        for (ResourceKey<Biome> resourcekey1 : valueRegistry.registryKeySet()) {
            if (!mappedregistry.containsKey(resourcekey1)) {
                Biome biome1 = valueRegistry.get(resourcekey1);
                mappedregistry.createIntrusiveHolder(biome1);
                Holder.Reference<Biome> holder$reference1 = mappedregistry.register(resourcekey1, biome1, Lifecycle.stable());
            }
        }

        return mappedregistry;
    }    private static Registry<Biome> biomeRegistry = getBiomeRegistry(Minecraft.getInstance().level);

    public static Registry<Biome> getBiomeRegistry() {
        return biomeRegistry;
    }

    public static BiomeId getBiomeId(ResourceLocation loc) {
        return BiomeId.make(loc);
    }

    public static Biome getBiome(ResourceLocation loc) {
        return getBiomeRegistry().get(loc);
    }

    public static Set<ResourceLocation> getLocations() {
        return getBiomeRegistry().keySet();
    }

    public static List<BiomeId> getBiomeIds(Collection<ResourceLocation> locations) {
        List<BiomeId> list = new ArrayList<>();

        for (ResourceLocation resourcelocation : locations) {
            BiomeId biomeid = BiomeId.make(resourcelocation);
            if (biomeid != null) {
                list.add(biomeid);
            }
        }

        return list;
    }

    public static Biome getBiome(BlockAndTintGetter lightReader, BlockPos blockPos) {
        Biome biome = PLAINS;
        if (lightReader instanceof LevelReader) {
            biome = ((LevelReader) lightReader).getBiome(blockPos).value();
        }

        return biome;
    }




    public static Biome DESERT = biomeRegistry.get(Biomes.DESERT);


    public static Biome WINDSWEPT_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_HILLS);


    public static Biome WINDSWEPT_GRAVELLY_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_GRAVELLY_HILLS);
    public static Biome MUSHROOM_FIELDS = biomeRegistry.get(Biomes.MUSHROOM_FIELDS);
    public static Biome SWAMP = biomeRegistry.get(Biomes.SWAMP);
    public static Biome MANGROVE_SWAMP = biomeRegistry.get(Biomes.MANGROVE_SWAMP);
    public static Biome PLAINS = biomeRegistry.get(Biomes.PLAINS);
    public static Biome SUNFLOWER_PLAINS = biomeRegistry.get(Biomes.SUNFLOWER_PLAINS);
    public static Biome SNOWY_PLAINS = biomeRegistry.get(Biomes.SNOWY_PLAINS);
    public static Biome ICE_SPIKES = biomeRegistry.get(Biomes.ICE_SPIKES);
}