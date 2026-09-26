package com.xiaolianganmuchen.nsukdelightcropaddons.api;

import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropSpec;

public final class DelightCropApi {
   private DelightCropApi() {
   }

   public static boolean register(CropSpec spec) {
      return CropCatalog.register(spec);
   }

   public static void registerSourceLabel(String modId, String translationKey) {
      CropCatalog.registerSourceLabel(modId, translationKey);
   }
}
