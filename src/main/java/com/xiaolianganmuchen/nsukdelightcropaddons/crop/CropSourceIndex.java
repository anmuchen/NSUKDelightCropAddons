package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CropSourceIndex {
   private static final Map<String, String> PREFIX_BY_MOD = new ConcurrentHashMap<>();
   private static final Set<String> DISCOVER_NAMESPACES = ConcurrentHashMap.newKeySet();

   private CropSourceIndex() {
   }

   public static void register(String modId, String prefix, boolean discover) {
      if (modId != null && !modId.isBlank()) {
         PREFIX_BY_MOD.put(modId, prefix != null && !prefix.isBlank() ? prefix : fallbackPrefix(modId));
         CropCatalog.registerSourceLabel(modId, "source.nsukdelightcropaddons." + modId);
         if (discover) {
            DISCOVER_NAMESPACES.add(modId);
         }
      }
   }

   public static String prefixFor(String modId) {
      return modId != null && !modId.isBlank() ? PREFIX_BY_MOD.getOrDefault(modId, fallbackPrefix(modId)) : "x";
   }

   public static Set<String> discoverNamespaces() {
      return Set.copyOf(DISCOVER_NAMESPACES);
   }

   private static String fallbackPrefix(String modId) {
      String compact = modId.replace("_", "");
      return compact.length() <= 3 ? compact : compact.substring(0, 3);
   }

   static {
      register("farmersdelight", "fd", true);
      register("dumplings_delight", "dd", true);
      register("pineapple_delight", "pd", true);
      register("ubesdelight", "ud", true);
      register("veggiesdelight", "vd", true);
      register("corn_delight", "cnd", true);
      register("expandeddelight", "xd", true);
      register("culturaldelights", "cld", true);
      register("croptopia", "ctp", true);
      register("gan_delight_reborn", "gd", true);
      register("trailandtales_delight", "ttd", true);
      register("immortalers_delight", "imd", true);
      register("youkaisfeasts", "yf", true);
      register("rusticdelight", "rd", true);
      register("custom", "cu", false);
   }
}
