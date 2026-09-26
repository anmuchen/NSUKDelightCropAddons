package com.xiaolianganmuchen.nsukdelightcropaddons.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropContext;
import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.FarmlandBoxCleanup;
import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.FarmlandBoxReconcile;
import common.cn.kafei.simukraft.farmland.FarmlandBoxData;
import common.cn.kafei.simukraft.farmland.FarmlandBoxManager;
import common.cn.kafei.simukraft.farmland.FarmlandFarmingService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(
   value = {FarmlandFarmingService.class},
   remap = false
)
public abstract class FarmlandFarmingServiceMixin {
   public FarmlandFarmingServiceMixin() {
   }

   @WrapMethod(
      method = {"tickBox"}
   )
   private static void nsukdelight$withCrop(
      ServerLevel level, FarmlandBoxManager manager, FarmlandBoxData data, @Coerce Object boxRuntime, long gameTime, Operation<Void> original
   ) {
      if (level != null && data != null && level.isLoaded(data.boxPos())) {
         Block boxBlock = (Block)BuiltInRegistries.BLOCK.get(ResourceLocation.parse("simukraft:nsuk_farmland_box"));
         if (boxBlock != Blocks.AIR && !level.getBlockState(data.boxPos()).is(boxBlock)) {
            FarmlandBoxCleanup.purge(level, data.boxPos());
            return;
         }
      }

      CropContext.push(data);

      try {
         original.call(new Object[]{level, manager, data, boxRuntime, gameTime});
      } finally {
         CropContext.pop();
      }
   }

   @WrapMethod(
      method = {"tick"}
   )
   private static void nsukdelight$clearContext(ServerLevel level, Operation<Void> original) {
      try {
         FarmlandBoxReconcile.reconcileAll(level);
         original.call(new Object[]{level});
      } finally {
         CropContext.clear();
      }
   }
}
