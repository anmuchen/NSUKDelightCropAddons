package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import com.xiaolianganmuchen.nsukdelightcropaddons.NSUKDelightCropAddons;
import com.xiaolianganmuchen.nsukdelightcropaddons.config.DelightCropConfig;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;

public final class CropDiscovery {
   private CropDiscovery() {
   }

   public static void discover(Set<String> namespaces) {
      if (DelightCropConfig.autoDiscovery() && namespaces != null && !namespaces.isEmpty()) {
         int added = 0;

         for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof CropBlock cropBlock) {
               ResourceLocation seedId = BuiltInRegistries.ITEM.getKey(item);
               if (seedId != null && namespaces.contains(seedId.getNamespace())) {
                  ResourceLocation plantId = BuiltInRegistries.BLOCK.getKey(cropBlock);
                  if (!shouldSkip(seedId, plantId, cropBlock) && !CropCatalog.isSeedRegistered(seedId)) {
                     String prefix = CropSourceIndex.prefixFor(seedId.getNamespace());
                     String key = CropIds.keyFromSeedPath(seedId.getPath());
                     String id = CropIds.compose(prefix, key);
                     if (!CropCatalog.contains(id)) {
                        CropSpec spec = CropSpec.builder(id)
                           .sourceModId(seedId.getNamespace())
                           .sourcePrefix(prefix)
                           .seedId(seedId)
                           .plantBlockId(plantId)
                           .origin(CropOrigin.DISCOVERY)
                           .build();
                        if (CropCatalog.register(spec)) {
                           added++;
                        }
                     }
                  }
               }
            }
         }

         if (added > 0) {
            NSUKDelightCropAddons.LOGGER.info("Auto-discovered {} farmland CropBlock(s) from {}", added, namespaces);
         }
      }
   }

   static boolean shouldSkip(ResourceLocation seedId, ResourceLocation plantId, Block plant) {
      if (!(plant instanceof StemBlock) && !(plant instanceof AttachedStemBlock) && !(plant instanceof LeavesBlock) && !(plant instanceof SaplingBlock)) {
         String typeName = plant.getClass().getName().toLowerCase(Locale.ROOT);
         return !typeName.contains("stemblock") && !typeName.contains("leafcrop") && !typeName.contains("sapling")
            ? isExcludedPath(seedId) || isExcludedPath(plantId) || isFarmerRice(seedId) || isFarmerRice(plantId)
            : true;
      } else {
         return true;
      }
   }

   private static boolean isFarmerRice(ResourceLocation id) {
      return id != null && "farmersdelight".equals(id.getNamespace()) && "rice".equals(id.getPath());
   }

   private static boolean isExcludedPath(ResourceLocation id) {
      if (id == null) {
         return false;
      } else {
         String path = id.getPath();
         return path.contains("saguaro")
            || path.contains("cactus")
            || path.contains("sapling")
            || path.contains("leaves")
            || path.contains("nether_wart")
            || path.contains("chorus")
            || path.contains("grape")
            || isNonFarmlandYoukai(id)
            || isNonFarmlandImmortalers(id);
      }
   }

   private static boolean isNonFarmlandYoukai(ResourceLocation id) {
      if (id != null && "youkaisfeasts".equals(id.getNamespace())) {
         String path = id.getPath();
         return "redbean".equals(path) || "tea".equals(path) || path.startsWith("tea_");
      } else {
         return false;
      }
   }

   private static boolean isNonFarmlandImmortalers(ResourceLocation id) {
      if (id != null && "immortalers_delight".equals(id.getNamespace())) {
         String path = id.getPath();
         return path.contains("gelpitaya")
            || path.contains("leisamboo")
            || path.contains("himekaido")
            || path.contains("pearlipearl")
            || path.contains("obsidian_walnut");
      } else {
         return false;
      }
   }
}
