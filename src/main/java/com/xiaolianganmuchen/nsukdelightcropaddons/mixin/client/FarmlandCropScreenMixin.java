package com.xiaolianganmuchen.nsukdelightcropaddons.mixin.client;

import client.cn.kafei.simukraft.client.farmland.FarmlandCropScreen;
import com.xiaolianganmuchen.nsukdelightcropaddons.client.DelightCropSelectScreen;
import common.cn.kafei.simukraft.network.farmland.FarmlandBoxOpenResponsePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {FarmlandCropScreen.class},
   remap = false
)
public abstract class FarmlandCropScreenMixin {
   public FarmlandCropScreenMixin() {
   }

   @Inject(
      method = {"open"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void nsukdelight$openGroupedCropScreen(FarmlandBoxOpenResponsePacket packet, CallbackInfo ci) {
      DelightCropSelectScreen.open(packet);
      ci.cancel();
   }
}
