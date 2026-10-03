package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private boolean active = false;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "SimpleTrigger", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "TWKS"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                active = !active;
            }
            
            if (active && client.player != null && client.world != null) {
                if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                    Entity target = ((EntityHitResult) client.crosshairTarget).getEntity();
                    ClientPlayerEntity player = client.player;
                    
                    if (target instanceof LivingEntity living && living.isAlive()) {
                        
                        if (living.isBlocking()) {
                            int axeSlot = findAxeSlot(player);
                            if (axeSlot != -1) {
                                int originalSlot = player.getInventory().selectedSlot;
                                player.getInventory().selectedSlot = axeSlot;
                                client.interactionManager.attackEntity(player, target);
                                player.swingHand(Hand.MAIN_HAND);
                                player.getInventory().selectedSlot = originalSlot;
                                return;
                            }
                        }

                        if (player.getAttackCooldownProgress(0.0f) >= 1.0f) {
                            boolean inAir = !player.isOnGround() && !player.isSubmergedInWater() && !player.isClimbing();
                            boolean isFalling = player.fallDistance > 0.0f || player.getVelocity().y < 0;

                            if (inAir && !isFalling) {
                                return;
                            }

                            int swordSlot = findSwordSlot(player);
                            if (swordSlot != -1 && player.getInventory().selectedSlot != swordSlot) {
                                player.getInventory().selectedSlot = swordSlot;
                            }

                            client.interactionManager.attackEntity(player, target);
                            player.swingHand(Hand.MAIN_HAND);
                        }
                    }
                }
            }
        });
    }

    private int findAxeSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }

    private int findSwordSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem() instanceof SwordItem) {
                return i;
            }
        }
        return -1;
    }
}
