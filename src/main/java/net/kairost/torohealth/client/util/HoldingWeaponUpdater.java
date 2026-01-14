package net.kairost.torohealth.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.TridentItem;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.config.ToroHealthConfig;

public class HoldingWeaponUpdater {
    public static void update() {
        if (ToroHealthConfig.InWorldBarVisibilityMode.NONE.equals(ToroHealthConfig.CONFIG.inWorldBarOptions.inWorldBarVisibilityMode.get()))
            return;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            ToroHealth.setHoldingWeapon(false);
            return;
        }
        ToroHealth.setHoldingWeapon(isWeapon(player.getMainHandItem()) || isWeapon(player.getOffhandItem()));
    }

    private static boolean isWeapon(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item instanceof AxeItem
            || item instanceof BowItem
            || item instanceof CrossbowItem
            || item instanceof SwordItem
            || item instanceof ThrowablePotionItem
            || item instanceof TridentItem
            || item instanceof EndCrystalItem;
    }
}
