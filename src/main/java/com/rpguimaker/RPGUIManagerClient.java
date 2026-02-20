package com.rpguimaker;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import com.rpguimaker.client.ClientCommandHandler;

@Mod(value = RPGUIManager.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = RPGUIManager.MODID, value = Dist.CLIENT)
public class RPGUIManagerClient {
    public RPGUIManagerClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        RPGUIManager.LOGGER.info("HELLO FROM CLIENT SETUP");
        RPGUIManager.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
