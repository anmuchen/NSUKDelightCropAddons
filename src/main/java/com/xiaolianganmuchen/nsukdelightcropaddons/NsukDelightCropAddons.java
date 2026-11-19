package com.xiaolianganmuchen.nsukdelightcropaddons;

import com.nsukcropapi.CropAddons;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * NSUK Delight Crop Addons —— 薄壳附属。
 *
 * <p>作物定义全部来自 jar 内 {@code data/nsukdelightcropaddons/nsuk_crops/*.json}，
 * 作业逻辑由 nsukcropapi 的共用作业库提供，本模组不再写任何 Mixin。</p>
 */
@Mod("nsukdelightcropaddons")
public class NsukDelightCropAddons {

    public static final String MODID = "nsukdelightcropaddons";

    public NsukDelightCropAddons(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CropAddons.bootstrap(NsukDelightCropAddons.class, MODID, "nsuk_crops"));
    }
}
