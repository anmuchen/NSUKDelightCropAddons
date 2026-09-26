package com.xiaolianganmuchen.nsukdelightcropaddons.mixin.client;

import client.cn.kafei.simukraft.client.farmland.FarmlandBoxScreenOpener;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropDisplayNames;
import common.cn.kafei.simukraft.network.farmland.FarmlandBoxOpenResponsePacket;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {FarmlandBoxScreenOpener.class},
   remap = false
)
public abstract class FarmlandBoxScreenOpenerMixin {
   public FarmlandBoxScreenOpenerMixin() {
   }

   @Inject(
      method = {"cropLine"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void nsukdelight$cropLine(FarmlandBoxOpenResponsePacket packet, CallbackInfoReturnable<Component> cir) {
      cir.setReturnValue(Component.translatable("gui.simukraft.farmland_box.crop_line", new Object[]{CropDisplayNames.of(packet.cropId())}));
   }
}
