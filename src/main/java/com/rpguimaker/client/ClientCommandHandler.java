package com.rpguimaker.client;

import com.rpguimaker.client.screen.StudioScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.common.NeoForge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Client-side command handler using Brigadier and event interception.
 */
@EventBusSubscriber(value = Dist.CLIENT)
public class ClientCommandHandler {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("rpgui")
                .then(Commands.literal("studio").executes(context -> {
                    Minecraft.getInstance().execute(() -> {
                        Minecraft.getInstance().setScreen(new StudioScreen());
                    });
                    return 1;
                }))
                .then(Commands.literal("help").executes(context -> {
                    Minecraft.getInstance().execute(() -> {
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().player
                                    .displayClientMessage(Component.literal("§b--- RPG UI Maker Help ---"), false);
                            Minecraft.getInstance().player.displayClientMessage(
                                    Component.literal("§f/rpgui studio §7- Open UI Editor"), false);
                            Minecraft.getInstance().player.displayClientMessage(
                                    Component.literal(
                                            "§fPut PNGs in §b[gameDir]/rpguimaker/textures/ §7to import them!"),
                                    false);
                            Minecraft.getInstance().player.displayClientMessage(
                                    Component.literal("§7Save files are stored in §brpguimaker/exports/"), false);
                        }
                    });
                    return 1;
                }));

        dispatcher.register(builder);
    }

    // Keep for potential non-registered command handling or legacy support if
    // needed
    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        // We'll prioritize the registered command system now,
        // but can keep this as a secondary trigger if needed.
    }
}
