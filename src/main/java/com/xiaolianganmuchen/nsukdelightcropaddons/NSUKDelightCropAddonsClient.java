package com.xiaolianganmuchen.nsukdelightcropaddons;

import com.xiaolianganmuchen.nsukdelightcropaddons.client.DelightModConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(
   value = "nsukdelightcropaddons",
   dist = {Dist.CLIENT}
)
public class NSUKDelightCropAddonsClient {
   public NSUKDelightCropAddonsClient(ModContainer container) {
      container.registerExtensionPoint(IConfigScreenFactory.class, (IConfigScreenFactory)(modContainer, parent) -> new DelightModConfigScreen(parent));
   }
}
