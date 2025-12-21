package net.kairost.torohealth.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.item.TridentItem;
import net.kairost.torohealth.ToroHealth;
import net.kairost.torohealth.config.ModConfig;

public class HoldingWeaponUpdater {
    public static void update() {
        if (ModConfig.InWorldBarVisibilityMode.NONE.equals(ToroHealth.getConfig().inWorldBarOptions.inWorldBarVisibilityMode))
            return;
        MinecraftClient minecraft = MinecraftClient.getInstance();
        PlayerEntity player = minecraft.player;
        if (player == null) {
            ToroHealth.setHoldingWeapon(false);
            return;
        }
        ToroHealth.setHoldingWeapon(isWeapon(player.getMainHandStack()) || isWeapon(player.getOffHandStack()));
    }

    private static boolean isWeapon(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item instanceof AxeItem
            || item instanceof BowItem
            || item instanceof CrossbowItem
            || item instanceof SwordItem
            || item instanceof ThrowablePotionItem
            || item instanceof TridentItem;
    }
}
