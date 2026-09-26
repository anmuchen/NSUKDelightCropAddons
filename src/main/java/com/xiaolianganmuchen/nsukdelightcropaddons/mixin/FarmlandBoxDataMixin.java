package com.xiaolianganmuchen.nsukdelightcropaddons.mixin;

import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.AddonCropPersistence;
import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.FarmlandBoxDataExt;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {FarmlandBoxData.class},
   remap = false
)
public abstract class FarmlandBoxDataMixin implements FarmlandBoxDataExt {
   @Unique
   private String nsukdelight$addonCropId = "";

   public FarmlandBoxDataMixin() {
   }

   @Override
   public String nsukdelight$getAddonCropId() {
      return this.nsukdelight$addonCropId == null ? "" : this.nsukdelight$addonCropId;
   }

   @Override
   public void nsukdelight$setAddonCropId(String id) {
      this.nsukdelight$addonCropId = id == null ? "" : id;
   }

   @Inject(
      method = {"toTag"},
      at = {@At("RETURN")}
   )
   private void nsukdelight$writeAddonCrop(CallbackInfoReturnable<CompoundTag> cir) {
      String addonId = this.nsukdelight$getAddonCropId();
      if (!addonId.isBlank()) {
         CompoundTag tag = (CompoundTag)cir.getReturnValue();
         tag.putString("Crop", addonId);
         tag.putString("AddonCrop", addonId);
      }
   }

   @Inject(
      method = {"fromTag"},
      at = {@At("RETURN")}
   )
   private static void nsukdelight$readAddonCrop(CompoundTag tag, CallbackInfoReturnable<FarmlandBoxData> cir) {
      FarmlandBoxData data = (FarmlandBoxData)cir.getReturnValue();
      if (data != null && tag != null) {
         String cropId = AddonCropPersistence.restoreId(
            tag.contains("Crop") ? tag.getString("Crop") : "", tag.contains("AddonCrop") ? tag.getString("AddonCrop") : "", id -> FarmCrop.fromId(id) != null
         );
         if (!cropId.isBlank()) {
            data.setCrop(FarmCrop.WHEAT);
            ((FarmlandBoxDataExt)(Object)data).nsukdelight$setAddonCropId(cropId);
         }
      }
   }
}
