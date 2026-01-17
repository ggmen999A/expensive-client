package by.algorithm.alpha.api.modules.impl.movement;


import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.Setting;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;


@ModuleAnnot(name = "Speed", type = ModuleCategory.Player, description = "Хождение со спидами флеша")
public class Speed extends Module {
    private final Minecraft mc = Minecraft.getInstance();
    public ModeSetting mode = new ModeSetting("Режим", "Collision (Grim)", "Collision (Grim)");
    public BooleanSetting legit = new BooleanSetting("Легит", true);
    private boolean lastLegitState;
    public StopWatch timer;
    private boolean boosting;
    private final ItemStack currentStack;

    public Speed() {
        this.lastLegitState = this.legit.get();
        this.timer = new StopWatch();
        this.currentStack = ItemStack.EMPTY;
        this.addSettings(this.mode, this.legit);
    }

    public void onEnable() {
        super.onEnable();
        this.boosting = false;
        this.timer.reset();
        this.lastLegitState = this.legit.get();
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (Minecraft.player != null && Minecraft.world != null) {
            boolean current = this.legit.get();
            if (current != this.lastLegitState) {
                if (!current && Minecraft.player != null) {
                    Minecraft.player.sendMessage(new StringTextComponent("§c§lС выключенным легит режимом может сильно флагать и быстро вызывать на проверки, играть на свой страх и риск!!!"), Minecraft.player.getUniqueID());
                }

                this.lastLegitState = current;
            }

            switch (this.mode.get()) {
                case "Collision (Grim)":
                    this.doFunTimeNear();
                    break;
                case "FunTimeTest":
                    this.doFunTimeTest();
                    break;
                case "Grim":
                    Vector3d motion = Minecraft.player.getMotion();
                    Minecraft.player.setMotion(motion.x * 1.18, motion.y, motion.z * 1.18);
            }

        }
    }

    private void doFunTimeTest() {
        BlockState bs = Minecraft.world.getBlockState(Minecraft.player.getPosition().down());
        boolean ok = bs.getBlock() instanceof StairsBlock || bs.getBlock() instanceof SlabBlock || bs.getBlock() instanceof BarrelBlock || bs.getBlock() instanceof ScaffoldingBlock || bs.getBlock() instanceof CarpetBlock || bs.getBlock() instanceof FlowerPotBlock;
        if (ok && Minecraft.player.isOnGround() && MoveUtils.isMoving()) {
            this.mc.gameSettings.keyBindJump.setPressed(true);
            this.multiplySpeed(2.0);
        }

    }

    private void doFunTimeNear() {
        AxisAlignedBB box = Minecraft.player.getBoundingBox().grow(0.44);
        int armorstands = Minecraft.world.getEntitiesWithinAABB(ArmorStandEntity.class, box).size();
        int living = Minecraft.world.getEntitiesWithinAABB(LivingEntity.class, box).size();
        boolean boost = armorstands > 1 || living > 1;
        if (boost && !Minecraft.player.isOnGround()) {
            float base = this.legit.get() ? 0.03F : 0.1F;
            float factor = armorstands > 1 ? 0.0F / (float)armorstands : base;
            Minecraft.player.jumpMovementFactor = factor;
        }

    }

    private void multiplySpeed(double m) {
        Vector3d v = Minecraft.player.getMotion();
        Minecraft.player.setMotion(v.x * m, v.y, v.z * m);
    }
}
