package com.example.headphones;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client-only: noise cancelling, /hp commands, and music that only plays while worn. */
@Mod(value = HeadphonesMod.MODID, dist = Dist.CLIENT)
public class HeadphonesClient {
    private static int tick = 0;

    public HeadphonesClient() {
        NeoForge.EVENT_BUS.addListener(HeadphonesClient::onPlaySound);
        NeoForge.EVENT_BUS.addListener(HeadphonesClient::onCommands);
        NeoForge.EVENT_BUS.addListener(HeadphonesClient::onTick);
    }

    static boolean wearingHeadphones() {
        var player = Minecraft.getInstance().player;
        return player != null && player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HEADPHONES.get());
    }

    private static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null || !wearingHeadphones()) return;
        SoundSource source = sound.getSource();
        if (source == SoundSource.HOSTILE || source == SoundSource.NEUTRAL
                || source == SoundSource.AMBIENT || source == SoundSource.WEATHER) {
            event.setSound(null);
        }
    }

    private static void onTick(ClientTickEvent.Post event) {
        if (++tick % 10 != 0) return;
        if (MusicPlayer.isPlaying() && !wearingHeadphones()) {
            MusicPlayer.stop(); // took the headphones off -> music stops
        }
    }

    private static void reply(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendSystemMessage(Component.literal("[Headphones] " + msg));
    }

    private static void onCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("hp")
                .then(Commands.literal("play")
                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(c -> {
                                    if (!wearingHeadphones()) {
                                        reply(c, "ต้องสวมหูฟังก่อน");
                                        return 0;
                                    }
                                    MusicPlayer.play(StringArgumentType.getString(c, "url").trim());
                                    return 1;
                                })))
                .then(Commands.literal("stop").executes(c -> { MusicPlayer.stop(); reply(c, "หยุดเพลงแล้ว"); return 1; }))
                .then(Commands.literal("volume")
                        .then(Commands.argument("percent", IntegerArgumentType.integer(0, 100))
                                .executes(c -> {
                                    int v = IntegerArgumentType.getInteger(c, "percent");
                                    MusicPlayer.setVolume(v);
                                    reply(c, "ระดับเสียง " + v + "%");
                                    return 1;
                                }))));
    }
}
