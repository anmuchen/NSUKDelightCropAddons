package com.xiaolianganmuchen.nsukdelightcropaddons.mixin;

import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropContext;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropHarvest;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.ResolvedCrop;
import common.cn.kafei.simukraft.farmland.FarmCrop;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import common.cn.kafei.simukraft.farmland.FarmlandFarmingService;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {FarmlandFarmingService.class},
   remap = false
)
public abstract class FarmlandHarvestServiceMixin {
   public FarmlandHarvestServiceMixin() {
   }

   @Inject(
      method = {"needsHarvestWork"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void nsukdelight$needsHarvestWork(ServerLevel level, FarmlandBoxData data, BlockPos cropPos, CallbackInfoReturnable<Boolean> cir) {
      CropContext.current().ifPresent(crop -> {
         if (!crop.isStem()) {
            cir.setReturnValue(CropHarvest.isReady(level, crop, cropPos));
         }
      });
   }

   @Inject(
      method = {"harvestBlock"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void nsukdelight$harvestBlock(ServerLevel level, List<BlockPos> chestPositions, BlockPos pos, BlockState state, CallbackInfo ci) {
      CropContext.current().ifPresent(crop -> {
         if (!crop.isStem() && crop.isOwnPlant(state)) {
            CropHarvest.harvest(level, crop, chestPositions, pos);
            ci.cancel();
         }
      });
   }

   @Inject(
      method = {"needsBonemealWork"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void nsukdelight$needsBonemealWork(
      ServerLevel level, FarmlandBoxData data, List<BlockPos> chestPositions, BlockPos cropPos, CallbackInfoReturnable<Boolean> cir
   ) {
      CropContext.current().ifPresent(crop -> {
         if (!crop.isStem() && CropHarvest.isReady(level, crop, cropPos)) {
            cir.setReturnValue(false);
         }
      });
   }

   @Inject(
      method = {"farmerStatusLabel"},
      at = {@At("HEAD")},
      cancellable = true,
      require = 0
   )
   private static void nsukdelight$farmerStatusLabel(ServerLevel level, String translationKey, FarmCrop crop, CallbackInfoReturnable<String> cir) {
      CropContext.current()
         .ifPresent(
            resolved -> cir.setReturnValue(
               Serializer.toJson(Component.translatable(translationKey, new Object[]{statusName(resolved)}), level.registryAccess())
            )
         );
   }

   private static Component statusName(ResolvedCrop crop) {
      return Component.translatableWithFallback(crop.statusTranslationKey(), Component.translatable(crop.seedTranslationKey()).getString());
   }
}
