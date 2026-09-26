package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;

public final class ResolvedCrop {
   private static final ResolvedCrop MISSING = new ResolvedCrop(null, Items.AIR, Blocks.AIR, List.of(), List.of(), null);
   private final CropSpec spec;
   private final Item seed;
   private final Block plantBlock;
   private final List<Block> extraHarvestBlocks;
   private final List<Block> growthBlocks;
   private final Block produceBlock;

   private ResolvedCrop(CropSpec spec, Item seed, Block plantBlock, List<Block> extraHarvestBlocks, List<Block> growthBlocks, Block produceBlock) {
      this.spec = spec;
      this.seed = seed;
      this.plantBlock = plantBlock;
      this.extraHarvestBlocks = extraHarvestBlocks;
      this.growthBlocks = growthBlocks;
      this.produceBlock = produceBlock;
   }

   public static ResolvedCrop missing() {
      return MISSING;
   }

   public boolean isMissing() {
      return this == MISSING || this.spec == null;
   }

   public static Optional<ResolvedCrop> resolve(CropSpec spec) {
      if (spec == null) {
         return Optional.empty();
      } else {
         Item seed = (Item)BuiltInRegistries.ITEM.get(spec.seedId());
         if (seed != null && seed != Items.AIR) {
            Block plantBlock = lookupBlock(spec.plantBlockId());
            if (plantBlock == Blocks.AIR && seed instanceof BlockItem blockItem) {
               plantBlock = blockItem.getBlock();
            }

            if (plantBlock == Blocks.AIR) {
               return Optional.empty();
            } else if (!(plantBlock instanceof CropBlock) && !spec.allowNonCropBlock()) {
               return Optional.empty();
            } else {
               List<Block> extraHarvestBlocks = lookupDistinctBlocks(spec.extraHarvestBlockIds(), plantBlock);
               List<Block> growthBlocks = lookupDistinctBlocks(spec.growthBlockIds(), plantBlock);
               growthBlocks.removeAll(extraHarvestBlocks);
               Block produceBlock = lookupBlock(spec.produceBlockId());
               if (produceBlock == Blocks.AIR) {
                  produceBlock = null;
               }

               return Optional.of(new ResolvedCrop(spec, seed, plantBlock, List.copyOf(extraHarvestBlocks), List.copyOf(growthBlocks), produceBlock));
            }
         } else {
            return Optional.empty();
         }
      }
   }

   public CropSpec spec() {
      return this.spec;
   }

   public String id() {
      return this.spec == null ? "" : this.spec.id();
   }

   public String sourceModId() {
      return this.spec == null ? "" : this.spec.sourceModId();
   }

   public Item seed() {
      return this.seed;
   }

   public Block plantBlock() {
      return this.plantBlock;
   }

   public Block produceBlock() {
      return this.produceBlock;
   }

   public BlockState plantState() {
      if (this.isMissing()) {
         return Blocks.AIR.defaultBlockState();
      } else {
         BlockState state = this.plantBlock.defaultBlockState();
         if (this.spec.waterlogOnPlant() && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = (BlockState)state.setValue(BlockStateProperties.WATERLOGGED, true);
         }

         return state;
      }
   }

   public boolean isStem() {
      return this.spec != null && this.spec.layout() == CropLayout.STEM;
   }

   public boolean shouldPlantAt(int x, int z) {
      return !this.isMissing() && this.spec != null ? this.spec.layout() == CropLayout.FULL || (Math.floorMod(x, 2) + Math.floorMod(z, 2)) % 2 == 0 : false;
   }

   public boolean isOwnPlant(BlockState state) {
      if (state == null) {
         return false;
      } else if (state.is(this.plantBlock)) {
         return true;
      } else {
         for (Block extra : this.extraHarvestBlocks) {
            if (state.is(extra)) {
               return true;
            }
         }

         for (Block growth : this.growthBlocks) {
            if (state.is(growth)) {
               return true;
            }
         }

         return false;
      }
   }

   public int harvestHeight() {
      if (this.spec != null && this.spec.harvestHeight() > 1) {
         return this.spec.harvestHeight();
      } else if (this.plantBlock != null && this.plantBlock != Blocks.AIR) {
         return looksTall(this.plantBlock.defaultBlockState()) ? 2 : 1;
      } else {
         return 1;
      }
   }

   public boolean isUpperSegment(BlockState state) {
      if (state == null) {
         return false;
      } else if (booleanProperty(state, "upper")) {
         return true;
      } else if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
         return true;
      } else {
         for (Block extra : this.extraHarvestBlocks) {
            if (state.is(extra)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean hasExtraHarvestBlocks() {
      return !this.extraHarvestBlocks.isEmpty();
   }

   public boolean isExtraHarvestBlock(BlockState state) {
      if (state == null) {
         return false;
      } else {
         for (Block extra : this.extraHarvestBlocks) {
            if (state.is(extra)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isMatureFull(BlockState state) {
      if (state != null && this.isOwnPlant(state) && !this.isStem()) {
         return this.harvestHeight() > 1 && !this.isUpperSegment(state) ? false : this.isSegmentMature(state);
      } else {
         return false;
      }
   }

   public boolean isSegmentMature(BlockState state) {
      return state != null && this.isOwnPlant(state)
         ? CropRules.isHarvestableStage(
            this.harvestHeight(),
            !this.extraHarvestBlocks.isEmpty(),
            this.isExtraHarvestBlock(state),
            this.isUpperSegment(state),
            CropAge.hasAge(state),
            CropAge.isMaxAge(state),
            this.spec == null || this.spec.harvestMaxAge()
         )
         : false;
   }

   public String statusTranslationKey() {
      return "crop.nsukdelightcropaddons." + this.id();
   }

   public String seedTranslationKey() {
      return this.seed != null && this.seed != Items.AIR ? this.seed.getDescriptionId() : this.statusTranslationKey();
   }

   public boolean isProduce(BlockState state) {
      return this.produceBlock != null && state != null && state.is(this.produceBlock);
   }

   public String translationKey() {
      if (this.isMissing()) {
         return "gui.simukraft.farmland_box.none";
      } else if (this.seed != null && this.seed != Items.AIR) {
         return this.seed.getDescriptionId();
      } else {
         return this.plantBlock != null && this.plantBlock != Blocks.AIR ? this.plantBlock.getDescriptionId() : "gui.simukraft.farmland_box.none";
      }
   }

   private static boolean looksTall(BlockState state) {
      if (state == null) {
         return false;
      } else {
         return state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) ? true : hasBooleanProperty(state, "upper");
      }
   }

   private static boolean booleanProperty(BlockState state, String name) {
      return findProperty(state, name) instanceof BooleanProperty booleanProperty && state.hasProperty(booleanProperty)
         ? (Boolean)state.getValue(booleanProperty)
         : false;
   }

   private static boolean hasBooleanProperty(BlockState state, String name) {
      return findProperty(state, name) instanceof BooleanProperty;
   }

   private static Property<?> findProperty(BlockState state, String name) {
      if (state != null && name != null && !name.isBlank()) {
         for (Property<?> property : state.getProperties()) {
            if (name.equals(property.getName())) {
               return property;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static List<Block> lookupDistinctBlocks(List<ResourceLocation> ids, Block plantBlock) {
      List<Block> blocks = new ArrayList<>();
      if (ids == null) {
         return blocks;
      } else {
         for (ResourceLocation id : ids) {
            Block block = lookupBlock(id);
            if (block != Blocks.AIR && block != plantBlock && !blocks.contains(block)) {
               blocks.add(block);
            }
         }

         return blocks;
      }
   }

   private static Block lookupBlock(ResourceLocation id) {
      if (id == null) {
         return Blocks.AIR;
      } else {
         Block block = (Block)BuiltInRegistries.BLOCK.get(id);
         return block == null ? Blocks.AIR : block;
      }
   }
}
