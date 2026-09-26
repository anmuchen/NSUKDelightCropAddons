package com.xiaolianganmuchen.nsukdelightcropaddons.crop;

import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public final class CropAge {
   private CropAge() {
   }

   public static boolean isMaxAge(BlockState state) {
      if (state == null) {
         return false;
      } else if (state.getBlock() instanceof CropBlock cropBlock) {
         return cropBlock.isMaxAge(state);
      } else if (state.hasProperty(BlockStateProperties.AGE_7)) {
         return (Integer)state.getValue(BlockStateProperties.AGE_7) >= 7;
      } else if (state.hasProperty(BlockStateProperties.AGE_5)) {
         return (Integer)state.getValue(BlockStateProperties.AGE_5) >= 5;
      } else if (state.hasProperty(BlockStateProperties.AGE_4)) {
         return (Integer)state.getValue(BlockStateProperties.AGE_4) >= 4;
      } else if (state.hasProperty(BlockStateProperties.AGE_3)) {
         return (Integer)state.getValue(BlockStateProperties.AGE_3) >= 3;
      } else if (state.hasProperty(BlockStateProperties.AGE_2)) {
         return (Integer)state.getValue(BlockStateProperties.AGE_2) >= 2;
      } else if (state.hasProperty(BlockStateProperties.AGE_1)) {
         return (Integer)state.getValue(BlockStateProperties.AGE_1) >= 1;
      } else {
         IntegerProperty age = findAgeProperty(state);
         return age != null && (Integer)state.getValue(age) >= max(age);
      }
   }

   public static boolean hasAge(BlockState state) {
      return state == null
         ? false
         : state.getBlock() instanceof CropBlock
            || state.hasProperty(BlockStateProperties.AGE_7)
            || state.hasProperty(BlockStateProperties.AGE_5)
            || state.hasProperty(BlockStateProperties.AGE_4)
            || state.hasProperty(BlockStateProperties.AGE_3)
            || state.hasProperty(BlockStateProperties.AGE_2)
            || state.hasProperty(BlockStateProperties.AGE_1)
            || findAgeProperty(state) != null;
   }

   private static IntegerProperty findAgeProperty(BlockState state) {
      for (Property<?> property : state.getProperties()) {
         if (property instanceof IntegerProperty integerProperty && CropRules.isAgePropertyName(property.getName())) {
            return integerProperty;
         }
      }

      return null;
   }

   private static int max(IntegerProperty property) {
      int highest = 0;

      for (int value : property.getPossibleValues()) {
         if (value > highest) {
            highest = value;
         }
      }

      return highest;
   }
}
