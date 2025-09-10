package com.example;

import com.example.client.ClientForgeEvents;
import com.example.screen.ForgingScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import com.example.util.PrefixEffectHandler;



public class TemplateMod implements ModInitializer {
	public static final String MOD_ID = "template-mod";

	public static final ScreenHandlerType<ForgingScreenHandler> FORGING_SCREEN_HANDLER =
			Registry.register(
					Registries.SCREEN_HANDLER,
					new Identifier(MOD_ID, "forging"),
					new ExtendedScreenHandlerType<>(
							(syncId, inv, buf) -> new ForgingScreenHandler(syncId, inv, ScreenHandlerContext.EMPTY)
					)
			);
	@Override
	public void onInitialize() {
		com.example.registry.ModBlocks.register();
		com.example.item.ModItems.register();
		com.example.item.ModTabs.register();   // 注册创造模式物品栏标签

		PrefixEffectHandler.initialize(); // 注册前缀效果处理器

		ClientForgeEvents.init(); // 注册锻造效果
	}
}