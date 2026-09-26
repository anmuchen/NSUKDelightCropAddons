package com.xiaolianganmuchen.nsukdelightcropaddons.mixin;

import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropContext;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {FarmCrop.class},
   remap = false
)
public abstract class FarmCropMixin {
   public FarmCropMixin() {
   }

   @Inject(
      method = {"seed"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void nsukdelight$seed(CallbackInfoReturnable<Item> cir) {
      CropContext.current().ifPresent(crop -> cir.setReturnValue(crop.seed()));
   }

   @Inject(
      method = {"plantState"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void nsukdelight$plantState(CallbackInfoReturnable<BlockState> cir) {
      CropContext.current().ifPresent(crop -> cir.setReturnValue(crop.plantState()));
   }

   @Inject(
      method = {"plantBlock"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void nsukdelight$plantBlock(CallbackInfoReturnable<Block> cir) {
      CropContext.current().ifPresent(crop -> cir.setReturnValue(crop.plantBlock()));
   }

   @Inject(
      method = {"produceBlock"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void nsukdelight$produceBlock(CallbackInfoReturnable<Block> cir) {
      CropContext.current().ifPresent(crop -> cir.setReturnValue(crop.produceBlock()));
   }

   @Inject(
      method = {"isStem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void nsukdelight$isStem(CallbackInfoReturnable<Boolean> cir) {
      CropContext.current().ifPresent(crop -> cir.setReturnValue(crop.isStem()));
   }

   @Inject(
      method = {"shouldPlantAt"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void nsukdelight$shouldPlantAt(int x, int z, CallbackInfoReturnable<Boolean> cir) {
      CropContext.current().ifPresent(crop -> cir.setReturnValue(crop.shouldPlantAt(x, z)));
   }

   @Inject(
      method = {"isOwnPlant"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void nsukdelight$isOwnPlant(BlockState state, CallbackInfoReturnable<Boolean> cir) {
      CropContext.current().ifPresent(crop -> cir.setReturnValue(crop.isOwnPlant(state)));
   }

   @Inject(
      method = {"isMatureFull"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void nsukdelight$isMatureFull(BlockState state, CallbackInfoReturnable<Boolean> cir) {
      CropContext.current().ifPresent(crop -> cir.setReturnValue(crop.isMatureFull(state)));
   }

   @Inject(
      method = {"isProduce"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void nsukdelight$isProduce(BlockState state, CallbackInfoReturnable<Boolean> cir) {
      CropContext.current().ifPresent(crop -> cir.setReturnValue(crop.isProduce(state)));
   }

   @Inject(
      method = {"translationKey"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void nsukdelight$translationKey(CallbackInfoReturnable<String> cir) {
      CropContext.current().ifPresent(crop -> cir.setReturnValue(crop.translationKey()));
   }
}
