package com.example.block;

import com.example.screen.ForgingScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class ForgingTableBlock extends Block {

    private static final Text TITLE = Text.literal("");

    public ForgingTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.openHandledScreen(new ExtendedScreenHandlerFactory() {
                @Override
                public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                    buf.writeBlockPos(pos);                                // 坐标

                }

                @Override
                public Text getDisplayName() {
                    return TITLE;
                }

                @Override
                public ForgingScreenHandler createMenu(int syncId,
                                                       net.minecraft.entity.player.PlayerInventory inv,
                                                       net.minecraft.entity.player.PlayerEntity player) {
                    return new ForgingScreenHandler(syncId, inv,
                            ScreenHandlerContext.create(world, pos));
                }
            });
        }
        return ActionResult.SUCCESS;
    }
}