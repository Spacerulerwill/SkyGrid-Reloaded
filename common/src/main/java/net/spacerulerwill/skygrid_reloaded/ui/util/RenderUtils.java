package net.spacerulerwill.skygrid_reloaded.ui.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RenderUtils {
    private static final ResourceLocation SLOT_TEXTURE = ResourceLocation.withDefaultNamespace("container/slot");

    private static void renderIconBackgroundTexture(GuiGraphics context, int x, int y) {
        context.blitSprite(SLOT_TEXTURE, x, y, 0, 18, 18);
    }

    public static void renderItemIcon(Item item, GuiGraphics context, int x, int y) {
        ItemStack itemStack = item.getDefaultInstance();
        renderIconBackgroundTexture(context, x + 1, y + 1);
        if (!itemStack.isEmpty()) {
            context.renderFakeItem(itemStack, x + 2, y + 2);
        }
    }
}
