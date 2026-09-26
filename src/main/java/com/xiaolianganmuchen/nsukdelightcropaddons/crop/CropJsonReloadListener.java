package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xiaolianganmuchen.nsukdelightcropaddons.NSUKDelightCropAddons;
import com.xiaolianganmuchen.nsukdelightcropaddons.config.CustomCropStorage;
import com.xiaolianganmuchen.nsukdelightcropaddons.config.DelightCropConfig;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.util.profiling.ProfilerFiller;

public final class CropJsonReloadListener implements PreparableReloadListener {
   public static final CropJsonReloadListener INSTANCE = new CropJsonReloadListener();
   private static final Gson GSON = new GsonBuilder().setLenient().create();
   private static final String DIRECTORY = "nsuk_crops";
   private static final String[] BUILTIN_FILES = new String[]{
      "farmersdelight.json",
      "dumplings_delight.json",
      "pineapple_delight.json",
      "ubesdelight.json",
      "veggiesdelight.json",
      "corn_delight.json",
      "expandeddelight.json",
      "culturaldelights.json",
      "croptopia.json",
      "gan_delight_reborn.json",
      "trailandtales_delight.json",
      "immortalers_delight.json",
      "youkaisfeasts.json",
      "rusticdelight.json"
   };

   private CropJsonReloadListener() {
   }

   public static void loadBuiltInFromJar() {
      List<CropSpec> loaded = new ArrayList<>();

      for (String fileName : BUILTIN_FILES) {
         String path = "/data/nsukdelightcropaddons/nsuk_crops/" + fileName;

         try (InputStream stream = CropJsonReloadListener.class.getResourceAsStream(path)) {
            if (stream != null) {
               JsonElement element = (JsonElement)GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonElement.class);
               if (element != null && element.isJsonObject()) {
                  loaded.addAll(parseFile(ResourceLocation.fromNamespaceAndPath("nsukdelightcropaddons", fileName), element.getAsJsonObject()));
               }
            }
         } catch (Exception var12) {
            NSUKDelightCropAddons.LOGGER.error("Failed to read built-in delight crop file {}", fileName, var12);
         }
      }

      CropCatalog.replaceOrigin(CropOrigin.JSON, loaded);

      try {
         CropDiscovery.discover(CropSourceIndex.discoverNamespaces());
      } catch (Exception var9) {
         NSUKDelightCropAddons.LOGGER.error("Delight crop auto-discovery failed", var9);
      }

      CustomCropStorage.loadAndApply();
      logUnresolved(loaded);
      NSUKDelightCropAddons.LOGGER.info("Loaded {} built-in delight crop definition(s)", loaded.size());
   }

   public CompletableFuture<Void> reload(
      PreparationBarrier barrier,
      ResourceManager resourceManager,
      ProfilerFiller prepareProfiler,
      ProfilerFiller applyProfiler,
      Executor backgroundExecutor,
      Executor gameExecutor
   ) {
      return CompletableFuture.<Map<ResourceLocation, JsonElement>>supplyAsync(() -> this.scanQuietly(resourceManager), backgroundExecutor)
         .exceptionally(throwable -> {
            NSUKDelightCropAddons.LOGGER.error("Delight crop scan failed", throwable);
            return Map.of();
         })
         .<Map<ResourceLocation, JsonElement>>thenCompose(barrier::wait)
         .thenAcceptAsync(this::applySafe, gameExecutor)
         .exceptionally(throwable -> {
            NSUKDelightCropAddons.LOGGER.error("Delight crop apply failed", throwable);
            return null;
         });
   }

   private Map<ResourceLocation, JsonElement> scanQuietly(ResourceManager resourceManager) {
      try {
         return this.scan(resourceManager);
      } catch (Throwable var3) {
         NSUKDelightCropAddons.LOGGER.error("Delight crop scan crashed", var3);
         return Map.of();
      }
   }

   private Map<ResourceLocation, JsonElement> scan(ResourceManager resourceManager) {
      Map<ResourceLocation, JsonElement> entries = new LinkedHashMap<>();

      try {
         Map<ResourceLocation, Resource> resources = resourceManager.listResources("nsuk_crops", location -> location.getPath().endsWith(".json"));

         for (Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            if (!id.getPath().contains("/_")) {
               try (InputStreamReader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                  JsonElement element = (JsonElement)GSON.fromJson(reader, JsonElement.class);
                  if (element != null) {
                     entries.put(id, element);
                  }
               } catch (Exception var12) {
                  NSUKDelightCropAddons.LOGGER.error("Skipped invalid delight crop json {}", id, var12);
               }
            }
         }
      } catch (Exception var13) {
         NSUKDelightCropAddons.LOGGER.error("Failed to scan nsuk_crops json", var13);
      }

      return entries;
   }

   private void applySafe(Map<ResourceLocation, JsonElement> entries) {
      try {
         this.apply(entries == null ? Map.of() : entries);
      } catch (Throwable var3) {
         NSUKDelightCropAddons.LOGGER.error("Delight crop reload failed; vanilla datapacks continue", var3);
      }
   }

   private void apply(Map<ResourceLocation, JsonElement> entries) {
      List<CropSpec> loaded = new ArrayList<>();

      for (Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
         try {
            if (!entry.getKey().getPath().contains("/_") && entry.getValue().isJsonObject()) {
               loaded.addAll(parseFile(entry.getKey(), entry.getValue().getAsJsonObject()));
            }
         } catch (Exception var6) {
            NSUKDelightCropAddons.LOGGER.error("Failed to parse delight crop file {}", entry.getKey(), var6);
         }
      }

      if (loaded.isEmpty()) {
         NSUKDelightCropAddons.LOGGER.warn("Delight crop reload produced 0 JSON crops; keeping existing JSON definitions");
      } else {
         CropCatalog.replaceOrigin(CropOrigin.JSON, loaded);
      }

      if (DelightCropConfig.configReady() && DelightCropConfig.autoDiscovery()) {
         CropDiscovery.discover(CropSourceIndex.discoverNamespaces());
      }

      CustomCropStorage.loadAndApply();
      logUnresolved(loaded);
      NSUKDelightCropAddons.LOGGER.info("Loaded {} JSON delight crop definition(s)", loaded.size());
   }

   private static void logUnresolved(List<CropSpec> loaded) {
      for (CropSpec spec : loaded) {
         if (ResolvedCrop.resolve(spec).isEmpty()) {
            NSUKDelightCropAddons.LOGGER
               .warn("Delight crop {} is listed but not selectable (seed={}, plant={})", new Object[]{spec.id(), spec.seedId(), spec.plantBlockId()});
         }
      }
   }

   private static List<CropSpec> parseFile(ResourceLocation fileId, JsonObject root) {
      String modId = string(root, "mod", fileId.getPath());
      if (!"mynethersdelight".equals(modId) && !"ends_delight".equals(modId) && CropCatalog.isModLoaded(modId)) {
         String prefix = string(root, "prefix", CropSourceIndex.prefixFor(modId));
         boolean discover = !root.has("discover") || root.get("discover").getAsBoolean();
         CropSourceIndex.register(modId, prefix, discover);
         if (root.has("label")) {
            CropCatalog.registerSourceLabel(modId, root.get("label").getAsString());
         }

         JsonArray crops = root.has("crops") && root.get("crops").isJsonArray() ? root.getAsJsonArray("crops") : new JsonArray();
         List<CropSpec> specs = new ArrayList<>();

         for (JsonElement element : crops) {
            if (element.isJsonObject()) {
               parseCrop(modId, prefix, element.getAsJsonObject()).ifPresent(specs::add);
            }
         }

         return specs;
      } else {
         return List.of();
      }
   }

   private static Optional<CropSpec> parseCrop(String modId, String prefix, JsonObject json) {
      String key = string(json, "id", "");
      ResourceLocation seedId = CropIds.parse(string(json, "seed", ""), modId);
      if (!key.isBlank() && seedId != null) {
         String id = CropIds.compose(prefix, key);
         ResourceLocation plantId = CropIds.parse(string(json, "plant", ""), modId);
         List<ResourceLocation> extra = idList(json, "harvest_blocks", modId);
         List<ResourceLocation> growth = idList(json, "growth_blocks", modId);
         ResourceLocation produceId = CropIds.parse(string(json, "produce", ""), modId);
         CropLayout layout = CropLayout.fromName(string(json, "layout", "full"));
         boolean waterlog = json.has("waterlog") && json.get("waterlog").getAsBoolean();
         boolean allowNonCrop = json.has("allow_non_crop_block") && json.get("allow_non_crop_block").getAsBoolean();
         int harvestHeight = json.has("harvest_height") ? json.get("harvest_height").getAsInt() : 0;
         boolean harvestMaxAge = !json.has("harvest_max_age") || json.get("harvest_max_age").getAsBoolean();

         try {
            return Optional.of(
               CropSpec.builder(id)
                  .sourceModId(modId)
                  .sourcePrefix(prefix)
                  .seedId(seedId)
                  .plantBlockId(plantId)
                  .extraHarvestBlockIds(extra)
                  .growthBlockIds(growth)
                  .layout(layout)
                  .produceBlockId(produceId)
                  .waterlogOnPlant(waterlog)
                  .allowNonCropBlock(allowNonCrop)
                  .harvestHeight(harvestHeight)
                  .harvestMaxAge(harvestMaxAge)
                  .origin(CropOrigin.JSON)
                  .build()
            );
         } catch (IllegalArgumentException var16) {
            NSUKDelightCropAddons.LOGGER.warn("Skipped crop {}: {}", id, var16.getMessage());
            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   private static List<ResourceLocation> idList(JsonObject json, String key, String modId) {
      List<ResourceLocation> ids = new ArrayList<>();
      if (json.has(key) && json.get(key).isJsonArray()) {
         for (JsonElement element : json.getAsJsonArray(key)) {
            ResourceLocation id = CropIds.parse(element.getAsString(), modId);
            if (id != null) {
               ids.add(id);
            }
         }

         return ids;
      } else {
         return ids;
      }
   }

   private static String string(JsonObject json, String key, String fallback) {
      if (json.has(key) && !json.get(key).isJsonNull()) {
         String value = json.get(key).getAsString();
         return value != null && !value.isBlank() ? value : fallback;
      } else {
         return fallback;
      }
   }
}
