package com.example.headphones;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(HeadphonesMod.MODID)
public class HeadphonesMod {
    public static final String MODID = "headphones";

    public HeadphonesMod(IEventBus modBus) {
        ModItems.ITEMS.register(modBus);
        modBus.addListener(HeadphonesMod::addCreative);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.HEADPHONES.get());
        }
    }
}
