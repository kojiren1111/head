package com.example.headphones;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Equippable;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HeadphonesMod.MODID);

    /** Points at assets/headphones/equipment/headphones.json */
    public static final ResourceKey<EquipmentAsset> HEADPHONES_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(HeadphonesMod.MODID, "headphones"));

    public static final DeferredItem<Item> HEADPHONES = ITEMS.registerItem(
            "headphones",
            Item::new,
            new Item.Properties()
                    .stacksTo(1)
                    .component(DataComponents.EQUIPPABLE,
                            Equippable.builder(EquipmentSlot.HEAD)
                                    .setEquipSound(SoundEvents.ARMOR_EQUIP_GENERIC)
                                    .setAsset(HEADPHONES_ASSET)
                                    .build())
    );
}
