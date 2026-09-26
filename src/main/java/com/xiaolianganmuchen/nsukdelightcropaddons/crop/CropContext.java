package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import com.xiaolianganmuchen.nsukdelightcropaddons.NSUKDelightCropAddons;
import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.FarmlandBoxDataAccess;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public final class CropContext {
   private static final ThreadLocal<Deque<ResolvedCrop>> STACK = ThreadLocal.withInitial(ArrayDeque::new);

   private CropContext() {
   }

   public static void push(FarmlandBoxData data) {
      STACK.get().push(resolveOrMissing(data));
   }

   public static void pop() {
      Deque<ResolvedCrop> stack = STACK.get();
      if (!stack.isEmpty()) {
         stack.pop();
      }

      if (stack.isEmpty()) {
         STACK.remove();
      }
   }

   public static void clear() {
      STACK.remove();
   }

   public static Optional<ResolvedCrop> current() {
      Deque<ResolvedCrop> stack = STACK.get();
      if (stack.isEmpty()) {
         return Optional.empty();
      } else {
         ResolvedCrop crop = stack.peek();
         return crop != null && !crop.isMissing() ? Optional.of(crop) : Optional.empty();
      }
   }

   private static ResolvedCrop resolveOrMissing(FarmlandBoxData data) {
      try {
         String addonId = FarmlandBoxDataAccess.addonCropId(data);
         return addonId != null && !addonId.isBlank() ? CropCatalog.resolve(addonId).orElse(ResolvedCrop.missing()) : ResolvedCrop.missing();
      } catch (Exception var2) {
         NSUKDelightCropAddons.LOGGER.error("Failed to bind delight crop context for farmland box", var2);
         return ResolvedCrop.missing();
      }
   }
}
