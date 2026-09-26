package com.xiaolianganmuchen.nsukdelightcropaddons.config;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

public final class DelightCropConfig {
   private static final Builder BUILDER = new Builder();
   private static final BooleanValue ENABLE_FARMERS_DELIGHT = BUILDER.comment("Allow Farmer's Delight farmland crops in the NSUK farmland box.")
      .define("enableFarmersDelight", true);
   private static final BooleanValue ENABLE_DUMPLINGS_DELIGHT = BUILDER.comment("Allow Dumplings Delight Rewrapped farmland crops in the NSUK farmland box.")
      .define("enableDumplingsDelight", true);
   private static final BooleanValue ENABLE_PINEAPPLE_DELIGHT = BUILDER.comment("Allow Pineapple Delight farmland crops in the NSUK farmland box.")
      .define("enablePineappleDelight", true);
   private static final BooleanValue ENABLE_UBES_DELIGHT = BUILDER.comment("Allow Ube's Delight farmland crops in the NSUK farmland box.")
      .define("enableUbesDelight", true);
   private static final BooleanValue ENABLE_VEGGIES_DELIGHT = BUILDER.comment("Allow Veggies Delight farmland crops in the NSUK farmland box.")
      .define("enableVeggiesDelight", true);
   private static final BooleanValue ENABLE_CORN_DELIGHT = BUILDER.comment("Allow Corn Delight farmland crops in the NSUK farmland box.")
      .define("enableCornDelight", true);
   private static final BooleanValue ENABLE_EXPANDED_DELIGHT = BUILDER.comment("Allow Expanded Delight farmland crops in the NSUK farmland box.")
      .define("enableExpandedDelight", true);
   private static final BooleanValue ENABLE_CULTURAL_DELIGHTS = BUILDER.comment("Allow Cultural Delights farmland crops in the NSUK farmland box.")
      .define("enableCulturalDelights", true);
   private static final BooleanValue ENABLE_CROPTOPIA = BUILDER.comment("Allow Croptopia farmland crops in the NSUK farmland box.")
      .define("enableCroptopia", true);
   private static final BooleanValue ENABLE_GAN_DELIGHT = BUILDER.comment("Allow Gan Delight Reborn farmland crops in the NSUK farmland box.")
      .define("enableGanDelight", true);
   private static final BooleanValue ENABLE_TRAIL_AND_TALES_DELIGHT = BUILDER.comment("Allow Trail & Tales Delight farmland crops in the NSUK farmland box.")
      .define("enableTrailAndTalesDelight", true);
   private static final BooleanValue ENABLE_IMMORTALERS_DELIGHT = BUILDER.comment("Allow Immortalers Delight farmland crops in the NSUK farmland box.")
      .define("enableImmortalersDelight", true);
   private static final BooleanValue ENABLE_YOUKAIS_FEASTS = BUILDER.comment("Allow Youkais' Feasts farmland crops in the NSUK farmland box.")
      .define("enableYoukaisFeasts", true);
   private static final BooleanValue ENABLE_RUSTIC_DELIGHT = BUILDER.comment("Allow Rustic Delight farmland crops in the NSUK farmland box.")
      .define("enableRusticDelight", true);
   private static final BooleanValue AUTO_DISCOVERY = BUILDER.comment("Scan loaded delight mods for CropBlock seeds that were not listed in JSON.")
      .define("autoDiscovery", true);
   private static final ConfigValue<List<? extends String>> DISABLED_CROPS = BUILDER.comment("Catalog ids to hide, for example fd_cabbage or dd_garlic.")
      .defineListAllowEmpty("disabledCrops", List.of(), () -> "", DelightCropConfig::isString);
   public static final ModConfigSpec SPEC = BUILDER.build();

   private DelightCropConfig() {
   }

   public static boolean configReady() {
      try {
         return SPEC.isLoaded();
      } catch (RuntimeException var1) {
         return false;
      }
   }

   public static boolean autoDiscovery() {
      return flag(AUTO_DISCOVERY, true);
   }

   public static boolean isSourceEnabled(String modId) {
      if (modId != null && !modId.isBlank()) {
         return switch (modId) {
            case "farmersdelight" -> flag(ENABLE_FARMERS_DELIGHT, true);
            case "mynethersdelight", "ends_delight" -> false;
            case "dumplings_delight" -> flag(ENABLE_DUMPLINGS_DELIGHT, true);
            case "pineapple_delight" -> flag(ENABLE_PINEAPPLE_DELIGHT, true);
            case "ubesdelight" -> flag(ENABLE_UBES_DELIGHT, true);
            case "veggiesdelight" -> flag(ENABLE_VEGGIES_DELIGHT, true);
            case "corn_delight" -> flag(ENABLE_CORN_DELIGHT, true);
            case "expandeddelight" -> flag(ENABLE_EXPANDED_DELIGHT, true);
            case "culturaldelights" -> flag(ENABLE_CULTURAL_DELIGHTS, true);
            case "croptopia" -> flag(ENABLE_CROPTOPIA, true);
            case "gan_delight_reborn" -> flag(ENABLE_GAN_DELIGHT, true);
            case "trailandtales_delight" -> flag(ENABLE_TRAIL_AND_TALES_DELIGHT, true);
            case "immortalers_delight" -> flag(ENABLE_IMMORTALERS_DELIGHT, true);
            case "youkaisfeasts" -> flag(ENABLE_YOUKAIS_FEASTS, true);
            case "rusticdelight" -> flag(ENABLE_RUSTIC_DELIGHT, true);
            case "custom" -> true;
            default -> true;
         };
      } else {
         return false;
      }
   }

   public static List<DelightCropConfig.SourceToggle> sourceToggles() {
      return List.of(
         new DelightCropConfig.SourceToggle("farmersdelight", ENABLE_FARMERS_DELIGHT),
         new DelightCropConfig.SourceToggle("dumplings_delight", ENABLE_DUMPLINGS_DELIGHT),
         new DelightCropConfig.SourceToggle("pineapple_delight", ENABLE_PINEAPPLE_DELIGHT),
         new DelightCropConfig.SourceToggle("ubesdelight", ENABLE_UBES_DELIGHT),
         new DelightCropConfig.SourceToggle("veggiesdelight", ENABLE_VEGGIES_DELIGHT),
         new DelightCropConfig.SourceToggle("corn_delight", ENABLE_CORN_DELIGHT),
         new DelightCropConfig.SourceToggle("expandeddelight", ENABLE_EXPANDED_DELIGHT),
         new DelightCropConfig.SourceToggle("culturaldelights", ENABLE_CULTURAL_DELIGHTS),
         new DelightCropConfig.SourceToggle("croptopia", ENABLE_CROPTOPIA),
         new DelightCropConfig.SourceToggle("gan_delight_reborn", ENABLE_GAN_DELIGHT),
         new DelightCropConfig.SourceToggle("trailandtales_delight", ENABLE_TRAIL_AND_TALES_DELIGHT),
         new DelightCropConfig.SourceToggle("immortalers_delight", ENABLE_IMMORTALERS_DELIGHT),
         new DelightCropConfig.SourceToggle("youkaisfeasts", ENABLE_YOUKAIS_FEASTS),
         new DelightCropConfig.SourceToggle("rusticdelight", ENABLE_RUSTIC_DELIGHT)
      );
   }

   public static void setAutoDiscovery(boolean value) {
      AUTO_DISCOVERY.set(value);
   }

   public static boolean isCropEnabled(String cropId) {
      if (cropId != null && !cropId.isBlank()) {
         try {
            if (!configReady()) {
               return true;
            } else {
               Set<String> disabled = ((List<String>)DISABLED_CROPS.get()).stream().map(value -> value.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
               return !disabled.contains(cropId.toLowerCase(Locale.ROOT));
            }
         } catch (RuntimeException var2) {
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean flag(BooleanValue value, boolean fallback) {
      try {
         return !configReady() ? fallback : (Boolean)value.get();
      } catch (RuntimeException var3) {
         return fallback;
      }
   }

   private static boolean isString(Object value) {
      return value instanceof String;
   }

   public record SourceToggle(String modId, BooleanValue value) {
      public boolean enabled() {
         return DelightCropConfig.flag(this.value, true);
      }

      public void setEnabled(boolean enabled) {
         this.value.set(enabled);
      }

      public String labelKey() {
         return "source.nsukdelightcropaddons." + this.modId;
      }
   }
}
