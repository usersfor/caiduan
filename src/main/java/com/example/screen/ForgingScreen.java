package com.example.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ForgingScreen extends HandledScreen<ForgingScreenHandler> {

    private static final Identifier TEXTURE =
            new Identifier("template-mod", "textures/gui/forging_table.png");

    public ForgingScreen(ForgingScreenHandler handler,
                         PlayerInventory inventory,
                         Text title) {
        super(handler, inventory, title);
    }

    /* 1.20.1 新版抽象方法：使用 DrawContext */
    @Override
    protected void drawBackground(DrawContext context,
                                  float delta,
                                  int mouseX,
                                  int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    /* 同样把 render 的签名更新为 DrawContext */
    @Override
    public void render(DrawContext context,
                       int mouseX,
                       int mouseY,
                       float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}