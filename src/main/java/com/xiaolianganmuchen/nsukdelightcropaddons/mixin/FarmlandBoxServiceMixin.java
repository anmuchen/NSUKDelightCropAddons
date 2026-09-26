package com.xiaolianganmuchen.nsukdelightcropaddons.mixin;

import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropCatalog;
import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.FarmlandBoxCleanup;
import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.FarmlandBoxDataAccess;
import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.FarmlandBoxReconcile;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import common.cn.kafei.simukraft.farmland.FarmlandBoxManager;
import common.cn.kafei.simukraft.farmland.FarmlandBoxService;
import common.cn.kafei.simukraft.farmland.FarmlandBoxView;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {FarmlandBoxService.class},
   remap = false
)
public abstract class FarmlandBoxServiceMixin {
   public FarmlandBoxServiceMixin() {
   }

   @Inject(
      method = {"onRemoved"},
      at = {@At("HEAD")}
   )
   private static void nsukdelight$purgeOnRemoved(ServerLevel level, BlockPos boxPos, CallbackInfo ci) {
      FarmlandBoxCleanup.purge(level, boxPos);
   }

   @Inject(
      method = {"setCrop"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void nsukdelight$setAddonCrop(ServerLevel level, BlockPos boxPos, String cropId, CallbackInfoReturnable<Boolean> cir) {
      if (cropId != null && !cropId.isBlank() && FarmCrop.fromId(cropId) == null) {
         if (CropCatalog.isSelectable(cropId)) {
            FarmlandBoxManager manager = FarmlandBoxManager.get(level);
            FarmlandBoxData data = manager.getOrCreate(boxPos);
            if (data.running()) {
               cir.setReturnValue(false);
            } else {
               data.setCrop(FarmCrop.WHEAT);
               FarmlandBoxDataAccess.setAddonCropId(data, cropId);
               manager.persist(data);
               FarmlandBoxReconcile.markSettled(level, boxPos);
               cir.setReturnValue(true);
            }
         }
      }
   }

   @Inject(
      method = {"setCrop"},
      at = {@At("RETURN")}
   )
   private static void nsukdelight$clearAddonIfVanilla(ServerLevel level, BlockPos boxPos, String cropId, CallbackInfoReturnable<Boolean> cir) {
      if (Boolean.TRUE.equals(cir.getReturnValue()) && FarmCrop.fromId(cropId) != null) {
         FarmlandBoxData data = FarmlandBoxManager.get(level).get(boxPos);
         if (data != null && FarmlandBoxDataAccess.hasAddonCrop(data)) {
            FarmlandBoxDataAccess.setAddonCropId(data, "");
            FarmlandBoxManager.get(level).persist(data);
            FarmlandBoxReconcile.markSettled(level, boxPos);
         }
      }
   }

   @Inject(
      method = {"buildView"},
      at = {@At("HEAD")}
   )
   private static void nsukdelight$reconcileOnOpen(ServerLevel level, BlockPos boxPos, CallbackInfoReturnable<FarmlandBoxView> cir) {
      FarmlandBoxManager manager = FarmlandBoxManager.get(level);
      FarmlandBoxData data = manager.get(boxPos);
      if (data != null) {
         FarmlandBoxReconcile.reconcileIfNeeded(level, manager, data);
      }
   }

   @Inject(
      method = {"buildView"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private static void nsukdelight$viewAddonCrop(ServerLevel level, BlockPos boxPos, CallbackInfoReturnable<FarmlandBoxView> cir) {
      FarmlandBoxView view = (FarmlandBoxView)cir.getReturnValue();
      if (view != null) {
         FarmlandBoxData data = FarmlandBoxManager.get(level).get(boxPos);
         if (data != null && FarmlandBoxDataAccess.hasAddonCrop(data)) {
            String addonId = FarmlandBoxDataAccess.addonCropId(data);
            if (!addonId.equals(view.cropId())) {
               cir.setReturnValue(
                  new FarmlandBoxView(
                     view.boxPos(),
                     view.hasCity(),
                     addonId,
                     view.hasPlot(),
                     view.plotMin(),
                     view.plotMax(),
                     view.hasChest(),
                     view.chestPos(),
                     view.running(),
                     view.hasFarmer(),
                     view.farmerName()
                  )
               );
            }
         }
      }
   }
}
