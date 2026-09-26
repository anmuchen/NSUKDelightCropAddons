package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import java.util.Locale;

public enum CropLayout {
   FULL,
   STEM;

   private CropLayout() {
   }

   public static CropLayout fromName(String name) {
      if (name != null && !name.isBlank()) {
         try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException var2) {
            return FULL;
         }
      } else {
         return FULL;
      }
   }
}
