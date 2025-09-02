package com.example.client;

import com.example.TemplateMod;
import com.example.screen.ForgingScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class TemplateModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 把 Screen 绑定到 ScreenHandler
        ScreenRegistry.register(TemplateMod.FORGING_SCREEN_HANDLER, ForgingScreen::new);
    }
}