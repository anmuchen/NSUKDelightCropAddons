package com.xiaolianganmuchen.nsukdelightcropaddons;

import com.mojang.logging.LogUtils;
import com.xiaolianganmuchen.nsukdelightcropaddons.config.DelightCropConfig;
import com.xiaolianganmuchen.nsukdelightcropaddons.crop.CropJsonReloadListener;
import com.xiaolianganmuchen.nsukdelightcropaddons.nsuk.FarmlandBoxReconcile;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

@Mod("nsukdelightcropaddons")
public class NSUKDelightCropAddons {
   public static final String MODID = "nsukdelightcropaddons";
   public static final Logger LOGGER = LogUtils.getLogger();

   public NSUKDelightCropAddons(IEventBus modEventBus, ModContainer modContainer) {
      modEventBus.addListener(this::commonSetup);
      NeoForge.EVENT_BUS.register(this);
      modContainer.registerConfig(Type.COMMON, DelightCropConfig.SPEC);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      event.enqueueWork(() -> {
         CropJsonReloadListener.loadBuiltInFromJar();
         LOGGER.info("NSUK Delight Crop Addons ready. Farmland box can plant loaded delight crops.");
      });
   }

   @SubscribeEvent
   public void onAddReloadListeners(AddReloadListenerEvent event) {
      event.addListener(CropJsonReloadListener.INSTANCE);
   }

   @SubscribeEvent
   public void onServerStopping(ServerStoppingEvent event) {
      FarmlandBoxReconcile.clear();
   }
}
