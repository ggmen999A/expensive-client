package by.algorithm.alpha.api.modules.impl.movement;

import by.algorithm.alpha.api.modules.settings.Setting;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.events.EventMotion;
import com.google.common.eventbus.Subscribe;

import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.player.MoveUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector2f;

@ModuleAnnot(name = "ElytraFly", type = ModuleCategory.Movement, description = "Полет на элитрах")
public class ElytraFly extends Module {
    private static final ModeSetting mode = new ModeSetting("Мод", "Поднятие-Верх", "Поднятие-Верх", "Matrix-old", "Обычная-элитра", "Vulcan-old", "HolyWorld");
    private final SliderSetting speed = (new SliderSetting("Скорость X", 0.5F, 0.0F, 12.0F, 0.01F)).setVisible(() -> mode.is("Matrix-old"));
    private final SliderSetting speedY = (new SliderSetting("Скорость Y", 0.5F, 0.0F, 12.0F, 0.01F)).setVisible(() -> mode.is("Matrix-old"));
    private static final BooleanSetting noShake = (new BooleanSetting("Убрать дёргание", false)).setVisible(() -> mode.is("Поднятие-Верх"));
    private final BooleanSetting aggressiveFly = (new BooleanSetting("Дерзкий флай", false)).setVisible(() -> mode.is("Matrix-old"));
    private final SliderSetting upValue = (new SliderSetting("Значение Y", 0.1488F, 0.05F, 1.0F, 0.001F)).setVisible(() -> mode.is("Matrix-old") && !(Boolean)this.aggressiveFly.get());
    private final SliderSetting horizontal = (new SliderSetting("Скорость XZ", 0.5F, 0.0F, 5.0F, 0.1F)).setVisible(() -> mode.is("Vulcan-old"));
    private final SliderSetting vertical = (new SliderSetting("Скорость Y", 0.5F, 0.0F, 5.0F, 0.1F)).setVisible(() -> mode.is("Vulcan-old"));
    public static boolean shackingcontroll;
    private final StopWatch timer = new StopWatch();
    private ItemStack chestStack;
    public Vector2f rotate;
    private final Minecraft mc;

    public ElytraFly() {
        this.chestStack = ItemStack.EMPTY;
        this.rotate = Vector2f.ZERO;
        this.mc = Minecraft.getInstance();
        this.addSettings(mode, noShake, this.speed, this.speedY, this.upValue, this.aggressiveFly, this.horizontal, this.vertical);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        shackingcontroll = noShake.get();
        switch (mode.get()) {
            case "Поднятие-Верх":
                this.handleFlyUp();
                break;
            case "HolyWorld":
                this.handleHolyWorld();
                break;
            case "Matrix-old":
                this.handleMatrix();
                break;
            case "Vulcan-old":
                this.handleVulcan();
                break;
            case "Обычная-элитра":
                this.handleVanillaBoost();
        }

    }

    private void handleFlyUp() {
        PlayerEntity p = Minecraft.player;
        if (p != null) {
            this.chestStack = p.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if (this.chestStack.getItem() == Items.ELYTRA) {
                if (p.isOnGround()) {
                    p.jump();
                    p.rotationPitchHead = -90.0F;
                } else if (ElytraItem.isUsable(this.chestStack) && !p.isElytraFlying()) {
                    p.startFallFlying();
                    ((ClientPlayerEntity)p).connection.sendPacket(new CEntityActionPacket(p, CEntityActionPacket.Action.START_FALL_FLYING));
                    p.rotationPitchHead = -90.0F;
                }

                p.rotationPitch = 0.0F;
                p.setMotion(p.getMotion().x, p.getMotion().y * 1.0789999961853027, p.getMotion().z);
            }
        }
    }

    private void handleHolyWorld() {
        PlayerEntity p = Minecraft.player;
        if (p != null) {
            if (p.isOnGround()) {
                p.jump();
                p.setMotion(p.getMotion().x, 0.42, p.getMotion().z);
                p.rotationPitchHead = -90.0F;
            } else if (ElytraItem.isUsable(this.chestStack) && !p.isElytraFlying()) {
                p.startFallFlying();
                ((ClientPlayerEntity)p).connection.sendPacket(new CEntityActionPacket(p, CEntityActionPacket.Action.START_FALL_FLYING));
                p.setMotion(p.getMotion().x, 0.5, p.getMotion().z);
                p.rotationPitchHead = -90.0F;
            }

            p.rotationPitch = 0.0F;
            if (!p.isOnGround()) {
                p.setMotion(p.getMotion().x, 0.36, p.getMotion().z);
            }

        }
    }

    private void handleMatrix() {
        PlayerEntity p = Minecraft.player;
        if (p != null) {
            this.chestStack = p.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if (this.chestStack.getItem() != Items.ELYTRA) {
                this.print("§cНадень элитру!");
                this.toggle();
            } else {
                float pitch = p.rotationPitch;
                if ((pitch > 50.0F || pitch < -50.0F) && !(Boolean)this.aggressiveFly.get()) {
                    p.rotationPitch = 0.0F;
                    this.print("§cНе поворачивай сильно голову!");
                }

                if (this.aggressiveFly.get()) {
                    this.updateAggressiveMotion();
                } else {
                    p.setMotion(p.getMotion().x, (double) this.upValue.get(), p.getMotion().z);
                }

                if (p.isOnGround()) {
                    p.jump();
                } else if (ElytraItem.isUsable(this.chestStack) && !p.isElytraFlying()) {
                    p.startFallFlying();
                    ((ClientPlayerEntity)p).connection.sendPacket(new CEntityActionPacket(p, CEntityActionPacket.Action.START_FALL_FLYING));
                }

                if (p.isElytraFlying()) {
                    p.stopFallFlying();
                }

            }
        }
    }

    private void handleVulcan() {
        PlayerEntity p = Minecraft.player;
        if (p != null) {
            this.chestStack = p.getItemStackFromSlot(EquipmentSlotType.CHEST);
            if (this.chestStack.getItem() == Items.ELYTRA) {
                boolean var10000;
                var10000 = p.getActivePotionEffect(Effects.SPEED) != null && p.getActivePotionEffect(Effects.SPEED).getAmplifier() >= 2;

                if (p.isElytraFlying()) {
                    p.stopFallFlying();
                }

                if (!p.isInWater() && !p.isSwimming() && !p.collidedHorizontally) {
                    this.updateAggressiveMotion();
                }
            }

        }
    }

    private void handleVanillaBoost() {
        PlayerEntity p = Minecraft.player;
        if (p != null && p.isAlive()) {
            p.setMotion(p.getMotion().add(0.0, 0.065, 0.0));
        }

    }

    private void updateAggressiveMotion() {
        PlayerEntity p = Minecraft.player;
        double yMotion = this.getVerticalMotion();
        if (MoveUtils.isMoving()) {
            MoveUtils.setMotion((double) this.horizontal.get());
            p.setMotion(p.getMotion().x, yMotion, p.getMotion().z);
        } else {
            MoveUtils.setMotion(0.0);
            p.setMotion(0.0, yMotion, 0.0);
        }

    }

    private double getVerticalMotion() {
        if (this.mc.gameSettings.keyBindSneak.isKeyDown()) {
            return -(Float)this.vertical.get();
        } else {
            return this.mc.gameSettings.keyBindJump.isKeyDown() ? (double) this.vertical.get() : 0.0;
        }
    }

    @Subscribe
    private void onMotion(EventMotion e) {
    }


}
