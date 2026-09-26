package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import com.mojang.authlib.GameProfile;
import common.cn.kafei.simukraft.material.WorkContainerService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class CropHarvest {
   private static final int MAX_SCAN_HEIGHT = 3;
   private static final GameProfile FARMER_PROFILE = new GameProfile(
      UUID.nameUUIDFromBytes("nsukdelightcropaddons:farmer".getBytes(StandardCharsets.UTF_8)), "NSUKDelightFarmer"
   );

   private CropHarvest() {
   }

   public static boolean isReady(ServerLevel level, ResolvedCrop crop, BlockPos cropPos) {
      if (level != null && crop != null && !crop.isMissing() && cropPos != null && !crop.isStem()) {
         if (crop.harvestHeight() <= 1) {
            return crop.isSegmentMature(level.getBlockState(cropPos));
         } else {
            List<BlockPos> column = column(level, crop, cropPos);
            boolean allMature = true;

            for (BlockPos pos : column) {
               if (!crop.isSegmentMature(level.getBlockState(pos))) {
                  allMature = false;
                  break;
               }
            }

            return CropRules.columnIsComplete(crop.harvestHeight(), column.size(), allMature);
         }
      } else {
         return false;
      }
   }

   public static void harvest(ServerLevel level, ResolvedCrop crop, List<BlockPos> chestPositions, BlockPos cropPos) {
      if (level != null && crop != null && !crop.isMissing() && cropPos != null) {
         List<BlockPos> column = column(level, crop, cropPos);
         if (!column.isEmpty()) {
            AABB area = harvestArea(column);
            Set<UUID> before = itemIds(level, area);
            List<ItemStack> drops = new ArrayList<>();

            for (int index = column.size() - 1; index >= 0; index--) {
               harvestSegment(level, crop, column.get(index), drops);
            }

            WorkContainerService.depositDropsOrDrop(level, chestPositions, drops, cropPos);
            collectNewItems(level, chestPositions, area, before);
         }
      }
   }

   public static void replantIfNeeded(ServerLevel level, ResolvedCrop crop, List<BlockPos> chestPositions, BlockPos cropPos) {
      if (level != null && crop != null && !crop.isMissing() && !crop.isStem() && cropPos != null) {
         if (crop.shouldPlantAt(cropPos.getX(), cropPos.getZ())) {
            BlockState cropState = level.getBlockState(cropPos);
            BlockState soilState = level.getBlockState(cropPos.below());
            if ((cropState.isAir() || cropState.canBeReplaced()) && soilState.is(Blocks.FARMLAND)) {
               if (WorkContainerService.consumeItem(level, chestPositions, crop.seed())) {
                  level.setBlock(cropPos, crop.plantState(), 3);
               }
            }
         }
      }
   }

   static List<BlockPos> column(ServerLevel level, ResolvedCrop crop, BlockPos cropPos) {
      List<BlockPos> positions = new ArrayList<>();
      int limit = crop.harvestHeight() > 1 ? Math.max(crop.harvestHeight(), 3) : (crop.hasExtraHarvestBlocks() ? 3 : 1);

      for (int dy = 0; dy < limit; dy++) {
         BlockPos pos = cropPos.above(dy);
         if (!level.isLoaded(pos)) {
            break;
         }

         BlockState state = level.getBlockState(pos);
         if (!crop.isOwnPlant(state)) {
            break;
         }

         positions.add(pos.immutable());
      }

      return positions;
   }

   private static void harvestSegment(ServerLevel level, ResolvedCrop crop, BlockPos pos, List<ItemStack> drops) {
      BlockState state = level.getBlockState(pos);
      if (crop.isOwnPlant(state) && crop.isSegmentMature(state)) {
         if (tryRightClickHarvest(level, pos, state)) {
            BlockState after = level.getBlockState(pos);
            if (!crop.isOwnPlant(after) || !crop.isSegmentMature(after)) {
               return;
            }
         }

         BlockState current = level.getBlockState(pos);
         if (crop.isOwnPlant(current)) {
            FakePlayer farmer = fakeFarmer(level);
            ItemStack tool = new ItemStack(Items.IRON_HOE);
            drops.addAll(Block.getDrops(current, level, pos, level.getBlockEntity(pos), farmer, tool));
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
         }
      }
   }

   private static boolean tryRightClickHarvest(ServerLevel level, BlockPos pos, BlockState state) {
      try {
         FakePlayer farmer = fakeFarmer(level);
         farmer.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
         BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
         InteractionResult result = state.useWithoutItem(level, farmer, hit);
         return result.consumesAction();
      } catch (RuntimeException var6) {
         return false;
      }
   }

   private static FakePlayer fakeFarmer(ServerLevel level) {
      return FakePlayerFactory.get(level, FARMER_PROFILE);
   }

   private static AABB harvestArea(List<BlockPos> column) {
      BlockPos first = column.getFirst();
      BlockPos last = column.getLast();
      return new AABB(first).minmax(new AABB(last)).inflate(1.0);
   }

   private static Set<UUID> itemIds(ServerLevel level, AABB area) {
      Set<UUID> ids = new HashSet<>();

      for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, area)) {
         ids.add(entity.getUUID());
      }

      return ids;
   }

   private static void collectNewItems(ServerLevel level, List<BlockPos> chestPositions, AABB area, Set<UUID> before) {
      for (ItemEntity entity : List.copyOf(level.getEntitiesOfClass(ItemEntity.class, area))) {
         if (entity.isAlive() && !before.contains(entity.getUUID())) {
            ItemStack leftover = WorkContainerService.insertIntoAny(level, chestPositions, entity.getItem());
            if (leftover.isEmpty()) {
               entity.discard();
            } else {
               entity.setItem(leftover);
            }
         }
      }
   }
}
