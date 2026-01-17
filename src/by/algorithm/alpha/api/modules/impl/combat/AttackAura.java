package by.algorithm.alpha.api.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.api.command.friends.FriendStorage;
import by.algorithm.alpha.system.events.EventInput;
import by.algorithm.alpha.system.events.EventMotion;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeListSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.utils.math.SensUtils;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.player.MouseUtil;
import by.algorithm.alpha.system.utils.player.MoveUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.hypot;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@ModuleAnnot(name = "AttackAura", type = ModuleCategory.Combat, description = "Автоматические удары сущностей")
public class AttackAura extends Module {
    @Getter
    private final ModeSetting type = new ModeSetting("Тип", "FunTime","FunTime", "Grim", "ReallyWorld", "HvH","SpookyTime", "SmoothTest","HolyWorld");
    private final ModeSetting varHvH = new ModeSetting("Тип", "Snap", "Smooth", "Snap").setVisible(() -> type.is("HvH"));
    private final SliderSetting attackRange = new SliderSetting("Дистанция атаки", 3f, 3f, 9f, 0.01f);
    private final ModeSetting sortMode = new ModeSetting("Сортировать", "По всему", "По всему", "По здоровью", "По броне", "По дистанции", "По обзору");
    private final ModeSetting hwRotationType = new ModeSetting("HW Тип", "Smooth", "Smooth").setVisible(() -> this.type.is("HolyWorld"));

    final ModeSetting speedType = new ModeSetting("Скорость ротации", "Средняя", "Быстрая", "Средняя", "Медленная").setVisible(() -> type.is("HvH"));
    public ModeSetting vari = new ModeSetting("Тип Grim", "Lite", "Lite", "Test", "Old", "RW", "Snap-G").setVisible(() -> type.is("GrimTest"));
    final ModeListSetting targets = new ModeListSetting("Цели",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Голые", true),
            new BooleanSetting("Мобы", false),
            new BooleanSetting("Животные", false),
            new BooleanSetting("Друзья", false),
            new BooleanSetting("Голые невидимки", true),
            new BooleanSetting("Невидимки", true),
            new BooleanSetting("Боты", false));

    @Getter
    public final ModeListSetting options = new ModeListSetting("Опции",
            new BooleanSetting("Только криты", true),
            new BooleanSetting("Отжимать щит", true),
            new BooleanSetting("Ломать щит", true),
            new BooleanSetting("Ускорять ротацию при атаке", false),
            new BooleanSetting("Синхронизировать атаку с ТПС", false),
            new BooleanSetting("Фокусировать одну цель", true),
            new BooleanSetting("Коррекция движения", true),
            new BooleanSetting("Не бить при использовании", true),

            new BooleanSetting("Ограничение FOV", false),
            new BooleanSetting("Не бить через стены", true));
    final ModeSetting critType = new ModeSetting("Крит хелпер", "None", "None", "Matrix", "NCP", "NCP+", "Grim");

    final ModeSetting correctionType = new ModeSetting("Тип коррекции", "Незаметный", "Незаметный", "Сфокусированный");
    final ModeSetting sprintResetType = new ModeSetting("Сброс спринта", "Выключен", "Выключен", "Обычный", "Незаметный", "Legit");
    private final SliderSetting fov = new SliderSetting(
            "FOV",
            90f,
            10f,
            180f,
            1f
    ).setVisible(() ->
            options.getValueByName("Ограничение FOV").get()
                    && !type.is("HvH")
    );


    @Getter
    private final StopWatch stopWatch = new StopWatch();
    public Vector2f rotateVector = new Vector2f(0.0F, 0.0F);
    @Getter
    private static LivingEntity target;
    private Entity selected;
    private final Random random = new Random();

    private boolean isHeadReacting = false;
    private float headReactionProgress = 0f;
    private float targetHeadYaw = 0f;
    private boolean headReactionDirection = true;
    private boolean isReturningToTarget = false;
    private final StopWatch headReactionTimer = new StopWatch();
    int ticks = 0;
    boolean isRotated;
    private float lastBodyYaw = 0F;
    private final float bodyRotationSpeed = 3.6F;
    private int hitCounter = 0;
    private String currentHitPart = "head";
    private int previousSlot = -1;

    public AttackAura() {
        addSettings(type,hwRotationType,varHvH,vari, attackRange, sortMode, targets, options, correctionType, sprintResetType, speedType, critType,  fov);
    }

    @Subscribe
    public void onInput(EventInput eventInput) {
        if (options.getValueByName("Коррекция движения").get() && correctionType.is("Незаметная") && target != null && Minecraft.player != null) {
            MoveUtils.fixMovement(eventInput, rotateVector.x);
        }
    }
    // Метод накладывает случайное дрожание на текущий вектор вращения
    private void applyJitter(float intensity) {
        // Проверка: иногда (50% шанс) мы не применяем джиттер, чтобы движение не выглядело как паркинсон
        if (random.nextBoolean()) {
            float jitterYaw = (random.nextFloat() - 0.5f) * intensity;
            float jitterPitch = (random.nextFloat() - 0.5f) * intensity;

            // Обновляем вектор с учетом шума
            rotateVector = new Vector2f(
                    rotateVector.x + jitterYaw,
                    // Для питча ограничиваем углы, чтобы не смотреть в себя (-90/90)
                    MathHelper.clamp(rotateVector.y + jitterPitch, -89.0F, 89.0F)
            );
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (sprintResetType.is("Legit")) {
            // Legit режим работает через игровые настройки без пакетов
            Minecraft.player.setSprinting(false);
            mc.gameSettings.keyBindSprint.setPressed(false);
        }
        if (sprintResetType.is("Выключен")) {
            Minecraft.player.setSprinting(false);
            mc.gameSettings.keyBindSprint.setPressed(false);
        }
        if (options.getValueByName("Фокусировать одну цель").get() && (target == null || !isValid(target)) || !options.getValueByName("Фокусировать одну цель").get()) {
            updateTarget();
        }

        if (target != null && !isUsingItem()) {
            isRotated = false;
            if (shouldPlayerFalling() && (stopWatch.hasTimeElapsed())) {
                updateAttack();
                ticks = 2;
            }
            if (type.is("Grim") || varHvH.is("Snap")|| vari.is("Snap-G")) {
                if (!isRotated) {
                    updateRotation(false, 80, 35);
                }
            } else {
                if (!isRotated) {
                    updateRotation(false, 80, 35);
                }
            }
        } else {
            stopWatch.setLastMS(0);
            reset();
        }
        if (target != null && isRotated && !Minecraft.player.isElytraFlying() && Minecraft.player.getDistanceEyePos(target) <= attackDistance()) {
            critHelper();
        }
    }

    @Subscribe
    private void onWalking(EventMotion e) {
        if (target == null) return;
        float targetYaw = rotateVector.x;
        float targetPitch = rotateVector.y;

        if (isHeadReacting && (type.is("Grim") || type.is("SpookyTime"))) {
            if (!isReturningToTarget) {
                float reactionSpeed = 0.12f;
                headReactionProgress += reactionSpeed;

                if (headReactionProgress >= 1.0f) {
                    isReturningToTarget = true;
                    headReactionProgress = 0f;
                } else {
                    float smoothProgress = 1.0f - (float) Math.pow(1.0f - headReactionProgress, 3.0);
                    float currentReaction = targetHeadYaw * smoothProgress;
                    targetYaw += currentReaction;
                }
            } else {
                float returnSpeed = 0.10f;
                headReactionProgress += returnSpeed;

                if (headReactionProgress >= 1.0f) {
                    isHeadReacting = false;
                    isReturningToTarget = false;
                    headReactionProgress = 0f;
                    targetHeadYaw = 0f;
                } else {
                    float smoothProgress = (float) Math.pow(1.0f - headReactionProgress, 2.0);
                    float currentReaction = targetHeadYaw * smoothProgress;
                    targetYaw += currentReaction;
                }
            }
        }

        if (type.is("SpookyTime")) {
            float circleAmplitude = 17.0f;
            float circleSpeed = 1.0f;
            float time = Minecraft.player.ticksExisted * circleSpeed;
            float headCircleYaw = (float) Math.sin(time) * circleAmplitude;
            float headCirclePitch = (float) Math.cos(time) * circleAmplitude * 0.5f;
            targetYaw += headCircleYaw;
            targetPitch += headCirclePitch;
        }

        float headYaw = targetYaw;
        float headPitch = MathHelper.clamp(targetPitch, -89.0F, 89.0F);
        float gcd = SensUtils.getGCDValue();
        headYaw -= (headYaw - Minecraft.player.rotationYawHead) % gcd;
        headPitch -= (headPitch - Minecraft.player.rotationPitchHead) % gcd;
        Minecraft.player.rotationYawHead = headYaw;
        Minecraft.player.rotationPitchHead = headPitch;

        float bodyTargetYaw = rotateVector.x;
        float yawDifference = MathHelper.wrapDegrees(bodyTargetYaw - lastBodyYaw);
        float bodyYaw = lastBodyYaw;
        if (Math.abs(yawDifference) > 0.1F) {
            bodyYaw += yawDifference / bodyRotationSpeed;
        }
        Minecraft.player.renderYawOffset = bodyYaw;
        Minecraft.player.prevRenderYawOffset = lastBodyYaw;
        lastBodyYaw = bodyYaw;
        e.setYaw(rotateVector.x);
        e.setPitch(rotateVector.y);
    }

    private void updateTarget() {
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : Minecraft.world.getAllEntities()) {
            if (entity instanceof LivingEntity living && isValid(living)) {
                targets.add(living);
            }
        }

        if (targets.isEmpty()) {
            target = null;
            return;
        }

        if (targets.size() == 1) {
            target = targets.get(0);
            return;
        }

        switch (sortMode.get()) {
            case "По всему" ->
                    Collections.shuffle(targets, random);
            case "По здоровью" ->
                    targets.sort(Comparator.comparingDouble(this::getEntityHealth));
            case "По броне" ->
                    targets.sort(Comparator.comparingDouble(entity ->
                            entity instanceof PlayerEntity ? getEntityArmor((PlayerEntity) entity) : entity.getTotalArmorValue()));
            case "По дистанции" ->
                    targets.sort(Comparator.comparingDouble(entity ->
                            Minecraft.player.getDistance(entity)));
            case "По обзору" ->
                    targets.sort(Comparator.comparingDouble(this::getFovToEntity));
        }

        target = targets.get(0);
    }

    public float lastYaw;
    public float lastPitch;
    private Vector3d lastAimPoint = null;
    private int multiPointTicks = 0;
    private final float[] deltaPitchHistory = new float[5];
    private int historyIndex = 0;
    private final boolean isSnapping = false;
    private final int snapTickCounter = 0;
    private final Vector2f snapTarget = new Vector2f(0, 0);
    private final boolean isRecoiling = false;
    private final int recoilTicks = 0;
    private final Vector2f recoilTarget = new Vector2f(0, 0);
    private void updateRotation(boolean attack, float rotationYawSpeed, float rotationPitchSpeed) {
        double heightOffset;
        switch (currentHitPart) {
            case "head" -> heightOffset = target.getHeight() * 0.9;
            case "torso" -> heightOffset = target.getHeight() * 0.5;
            case "arms" -> heightOffset = target.getHeight() * 0.7;
            case "legs" -> heightOffset = target.getHeight() * 0.3;
            default -> heightOffset = target.getHeight() * 0.5;
        }

        Vector3d vec = target.getPositionVec().add(0, clamp(Minecraft.player.getPosYEye() - target.getPosY(),
                        0, heightOffset), 0)
                .subtract(Minecraft.player.getEyePosition(1.0F));

        isRotated = true;

        float yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90);
        float pitchToTarget = (float) (-Math.toDegrees(Math.atan2(vec.y, hypot(vec.x, vec.z))));

        float yawDelta = (wrapDegrees(yawToTarget - rotateVector.x));
        float pitchDelta = (wrapDegrees(pitchToTarget - rotateVector.y));

        switch (type.get()) {
            case "HvH" -> {
                switch (varHvH.get()) {
                    case "Snap" -> {
                        float yaw, pitch;
                        boolean accel = options.getValueByName("Ускорять ротацию при атаке").get() && attack;

                        if (accel) {
                            // Полный snap при атаке
                            yaw = rotateVector.x + yawDelta;
                            pitch = clamp(rotateVector.y + pitchDelta, -90.0F, 90.0F);
                        } else {
                            // Частичный snap по скорости
                            float factor = switch (speedType.get()) {
                                case "Быстрая" -> 0.95F;
                                case "Средняя" -> 0.65F;
                                case "Медленная" -> 0.35F;
                                default -> 0.65F;
                            };
                            yaw = rotateVector.x + yawDelta * factor;
                            pitch = clamp(rotateVector.y + pitchDelta * factor, -90.0F, 90.0F);

                            // Джиттер (больше на медленной)
                            float jitter = switch (speedType.get()) {
                                case "Быстрая" -> 0.15F;
                                case "Средняя" -> 0.3F;
                                case "Медленная" -> 0.5F;
                                default -> 0.3F;
                            };
                            yaw += (random.nextFloat() - 0.5F) * jitter;
                            pitch += (random.nextFloat() - 0.5F) * jitter;
                        }

                        float gcd = SensUtils.getGCDValue();
                        float gcdRand = 0.995F + random.nextFloat() * 0.01F;
                        yaw -= (yaw - rotateVector.x) % (gcd * gcdRand);
                        pitch -= (pitch - rotateVector.y) % (gcd * gcdRand);

                        rotateVector = new Vector2f(yaw, pitch);
                        lastYaw = yaw;
                        lastPitch = pitch;

                        if (options.getValueByName("Коррекция движения").get()) {
                            Minecraft.player.rotationYawOffset = yaw;
                        }
                    }

                    case "Smooth" -> {
                        float yaw, pitch;
                        boolean accel = options.getValueByName("Ускорять ротацию при атаке").get() && attack;

                        float yawBase = switch (speedType.get()) {
                            case "Быстрая" -> accel ? 120.0F : 65.0F;
                            case "Средняя" -> accel ? 90.0F : 45.0F;
                            case "Медленная" -> accel ? 60.0F : 25.0F;
                            default -> accel ? 90.0F : 45.0F;
                        };

                        float pitchBase = yawBase * 0.45F;

                        float maxYawSpeed = yawBase + random.nextFloat() * 20.0F;
                        float maxPitchSpeed = pitchBase + random.nextFloat() * 10.0F;

                        float yawChange = Math.signum(yawDelta) * Math.min(Math.abs(yawDelta), maxYawSpeed);
                        float pitchChange = Math.signum(pitchDelta) * Math.min(Math.abs(pitchDelta), maxPitchSpeed);

                        // Джиттер (меньше при ускорении)
                        float jitter = accel ? 0.15F : 0.3F;
                        yawChange += (random.nextFloat() - 0.5F) * jitter;
                        pitchChange += (random.nextFloat() - 0.5F) * jitter;

                        yaw = rotateVector.x + yawChange;
                        pitch = clamp(rotateVector.y + pitchChange, -89.0F, 89.0F);

                        float gcd = SensUtils.getGCDValue();
                        float gcdRand = 0.995F + random.nextFloat() * 0.01F;
                        yaw -= (yaw - rotateVector.x) % (gcd * gcdRand);
                        pitch -= (pitch - rotateVector.y) % (gcd * gcdRand);

                        rotateVector = new Vector2f(yaw, pitch);
                        lastYaw = yaw;
                        lastPitch = pitch;

                        if (options.getValueByName("Коррекция движения").get()) {
                            Minecraft.player.rotationYawOffset = yaw;
                        }
                    }
                }
            }
            case "HolyWorld"-> {
                switch (hwRotationType.get()){
                    case "Smooth" -> {
                        // МУЛЬТИПОИНТЫ
                        if (multiPointTicks <= 0 || lastAimPoint == null) {
                            double width = target.getWidth();
                            double height = target.getHeight();

                            Vector3d newPoint;
                            do {
                                double offsetX = (random.nextDouble() - 0.5) * width * 0.8;
                                double offsetY = random.nextDouble() * height * 0.85 + height * 0.1;
                                double offsetZ = (random.nextDouble() - 0.5) * width * 0.8;
                                newPoint = target.getPositionVec().add(offsetX, offsetY, offsetZ);
                            } while (lastAimPoint != null && newPoint.distanceTo(lastAimPoint) < 0.3);

                            lastAimPoint = newPoint;
                            multiPointTicks = 8 + random.nextInt(7);
                        }
                        multiPointTicks--;

                        vec = lastAimPoint.subtract(Minecraft.player.getEyePosition(1.0F));

                        // Считаем yaw и pitch напрямую, без wrapDegrees
                        float targetYaw = (float) (Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90);
                        float targetPitch = (float) (-Math.toDegrees(Math.atan2(vec.y, Math.hypot(vec.x, vec.z))));

                        // Дельты без wrap
                        yawDelta = targetYaw - rotateVector.x;
                        pitchDelta = targetPitch - rotateVector.y;

                        // Ограничиваем скорость
                        float maxYawDelta = 60f + random.nextFloat() * 1.03f;
                        float maxPitchDelta = 23.133f + random.nextFloat() * 3.344f;

                        yawDelta = Math.signum(yawDelta) * Math.min(Math.abs(yawDelta), maxYawDelta);
                        pitchDelta = Math.signum(pitchDelta) * Math.min(Math.abs(pitchDelta), maxPitchDelta);

                        // Мини-фикс, чтобы yaw и pitch двигались вместе
                        if (Math.abs(yawDelta) < 0.01f && Math.abs(pitchDelta) > 0.5f)
                            yawDelta += 0.1f + random.nextFloat() * 0.5f;
                        if (Math.abs(pitchDelta) < 0.01f && Math.abs(yawDelta) > 0.5f)
                            pitchDelta += 0.1f + random.nextFloat() * 0.5f;

                        // Плавная скорость
                        float yawSpeed = Math.min(Math.max(Math.abs(yawDelta), 2f), 65f);
                        float pitchSpeed = Math.min(Math.max(Math.abs(pitchDelta), 1.5f), 23f);

                        float yawSpeedVar = yawSpeed * (0.85f + random.nextFloat() * 0.3f);
                        float pitchSpeedVar = pitchSpeed * (0.85f + random.nextFloat() * 0.3f);

                        float yaw = rotateVector.x + Math.signum(yawDelta) * yawSpeedVar;
                        float pitch = rotateVector.y + Math.signum(pitchDelta) * pitchSpeedVar;

                        // Джиттер
                        if (random.nextBoolean()) {
                            yaw += (random.nextFloat() - 0.5f) * 0.15f;
                            pitch += (random.nextFloat() - 0.5f) * 0.15f;
                        }

                        // Автокоррекция через прошлые значения
                        float autoregressiveFactor = 0.15f;
                        for (int i = 0; i < deltaPitchHistory.length; i++) {
                            float weight = (float) Math.pow(0.7, i + 1);
                            pitch += deltaPitchHistory[i] * autoregressiveFactor * weight;
                        }

                        deltaPitchHistory[historyIndex] = pitchDelta;
                        historyIndex = (historyIndex + 1) % deltaPitchHistory.length;

                        // GCD
                        float gcd = SensUtils.getGCDValue();
                        float gcdRandomizer = 0.9975f + random.nextFloat() * 0.005f;
                        yaw -= (yaw - rotateVector.x) % (gcd * gcdRandomizer);
                        pitch -= (pitch - rotateVector.y) % (gcd * gcdRandomizer);

                        pitch = clamp(pitch, -89f, 89f);

                        rotateVector = new Vector2f(yaw, pitch);
                        lastYaw = yaw;
                        lastPitch = pitch;

                        if (options.getValueByName("Коррекция движения").get()) {
                            Minecraft.player.rotationYawOffset = yaw;
                        }
                    }

                }
            }
            case "FunTime" -> {
                float yaw;
                float pitch;
                if (attack && selected != target && options.getValueByName("Ускорять ротацию при атаке").get()) {
                    yaw = rotateVector.x + yawDelta;
                    pitch = clamp(rotateVector.y + pitchDelta, -89.0F, 89.0F);
                } else {
                    float yawSpeed = Math.min(Math.max(Math.abs(yawDelta), 1.0f), rotationYawSpeed * 2.5f);
                    float pitchSpeed = Math.min(Math.max(Math.abs(pitchDelta), 1.0f), rotationPitchSpeed * 2.5f);
                    yaw = rotateVector.x + (yawDelta > 0 ? yawSpeed : -yawSpeed);
                    pitch = clamp(rotateVector.y + (pitchDelta > 0 ? pitchSpeed : -pitchSpeed), -89.0F, 89.0F);
                }
                float shakeIntensity = 0.2f;
                float shakeFrequency = 0.02f;
                if (Minecraft.player.ticksExisted % Math.max(1, (int)(shakeFrequency * 10)) == 0) {
                    yaw += (float) (Math.random() - 0.1) * shakeIntensity;
                    pitch += (float) (Math.random() - 0.5) * shakeIntensity;
                }
                float circleAmplitude = 13.4f;
                float circleSpeed = 1.2f;
                float time = Minecraft.player.ticksExisted * circleSpeed;
                yaw += (float) Math.sin(time) * circleAmplitude;
                pitch += (float) Math.cos(time) * circleAmplitude;
                yaw += (float) (Math.random() - 0.5) * 0.05f;
                pitch += (float) (Math.random() - 0.5) * 0.05f;
                float gcd = SensUtils.getGCDValue();
                float gcdRandomizer = (float) (Math.random() * 0.01f + 0.995f);
                yaw -= (yaw - rotateVector.x) % (gcd * gcdRandomizer);
                pitch -= (pitch - rotateVector.y) % (gcd * gcdRandomizer);
                float maxYawChange = 46.0f;
                float maxPitchChange = 42.0f;
                yaw = rotateVector.x + clamp(yaw - rotateVector.x, -maxYawChange, maxPitchChange);
                pitch = clamp(rotateVector.y + clamp(pitch - rotateVector.y, -maxPitchChange, maxPitchChange), -89.0F, 89.0F);
                rotateVector = new Vector2f(yaw, pitch);
                lastYaw = yaw;
                lastPitch = pitch;
                if (options.getValueByName("Коррекция движения").get()) {
                    Minecraft.player.rotationYawOffset = yaw;
                }
            }
            case "Grim" -> {
                float yaw;
                float pitch;
                if (attack && selected != target && options.getValueByName("Ускорять ротацию при атаке").get()) {
                    yaw = rotateVector.x + yawDelta;
                    pitch = clamp(rotateVector.y + pitchDelta, -89.0F, 89.0F);
                } else {
                    float yawSpeed = Math.min(Math.max(Math.abs(yawDelta), 1.0f), rotationYawSpeed * 2.5f);
                    float pitchSpeed = Math.min(Math.max(Math.abs(pitchDelta), 1.0f), rotationPitchSpeed * 2.5f);
                    yaw = rotateVector.x + (yawDelta > 0 ? yawSpeed : -yawSpeed);
                    pitch = clamp(rotateVector.y + (pitchDelta > 0 ? pitchSpeed : -pitchSpeed), -89.0F, 89.0F);
                }
                float shakeIntensity = 0.08f;
                float shakeFrequency = 0.05f;
                if (Minecraft.player.ticksExisted % Math.max(1, (int)(shakeFrequency * 20)) == 0) {
                    yaw += (float) (Math.random() - 0.5) * shakeIntensity;
                    pitch += (float) (Math.random() - 0.5) * shakeIntensity;
                }
                float circleAmplitude = 4.2f;
                float circleSpeed = 0.6f;
                float time = Minecraft.player.ticksExisted * circleSpeed;
                yaw += (float) Math.sin(time) * circleAmplitude;
                pitch += (float) Math.cos(time) * circleAmplitude;
                yaw += (float) (Math.random() - 0.5) * 0.02f;
                pitch += (float) (Math.random() - 0.5) * 0.02f;
                float gcd = SensUtils.getGCDValue();
                float gcdRandomizer = (float) (Math.random() * 0.005f + 0.9975f);
                yaw -= (yaw - rotateVector.x) % (gcd * gcdRandomizer);
                pitch -= (pitch - rotateVector.y) % (gcd * gcdRandomizer);
                float maxYawChange = 35.0f;
                float maxPitchChange = 30.0f;
                yaw = rotateVector.x + clamp(yaw - rotateVector.x, -maxYawChange, maxYawChange);
                pitch = clamp(rotateVector.y + clamp(pitch - rotateVector.y, -maxPitchChange, maxPitchChange), -89.0F, 89.0F);
                rotateVector = new Vector2f(yaw, pitch);
                lastYaw = yaw;
                lastPitch = pitch;
                if (options.getValueByName("Коррекция движения").get()) {
                    Minecraft.player.rotationYawOffset = yaw;
                }
            }
            case "ReallyWorld" -> {
                float currentYaw = Minecraft.player.rotationYaw;
                float currentPitch = Minecraft.player.rotationPitch;
                float targetYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90);
                float targetPitch = (float) (-Math.toDegrees(Math.atan2(vec.y, hypot(vec.x, vec.z))));
                targetPitch = clamp(targetPitch, -89.0F, 89.0F);
                if (attack) {
                    float yawDelta1 = wrapDegrees(targetYaw - rotateVector.x);
                    float pitchDelta1 = wrapDegrees(targetPitch - rotateVector.y);
                    float baseSpeed = 95f;
                    float accelFactor = 3.5f;
                    float snapSpeed = baseSpeed * (1 + Math.min(Math.abs(yawDelta1) / 90f, 1) * (accelFactor - 1));
                    float snapFactor = Math.min(1.0f, snapSpeed / Math.max(1.0f, Math.abs(yawDelta1)));
                    float snapYaw = rotateVector.x + yawDelta1 * snapFactor;
                    snapSpeed = baseSpeed * (1 + Math.min(Math.abs(pitchDelta) / 90f, 1) * (accelFactor - 1));
                    snapFactor = Math.min(1.0f, snapSpeed / Math.max(1.0f, Math.abs(pitchDelta1)));
                    float snapPitch = rotateVector.y + pitchDelta1 * snapFactor;
                    if (Math.abs(wrapDegrees(targetYaw - snapYaw)) < 0.1f) {
                        snapYaw = targetYaw;
                    }
                    if (Math.abs(wrapDegrees(targetPitch - snapPitch)) < 0.1f) {
                        snapPitch = targetPitch;
                    }
                    rotateVector = new Vector2f(snapYaw, snapPitch);
                } else {
                    float returnSpeed = 80f;
                    float yawDiff = wrapDegrees(currentYaw - rotateVector.x);
                    float pitchDiff = wrapDegrees(currentPitch - rotateVector.y);
                    float returnFactor = Math.min(1.0f, returnSpeed / Math.max(1.0f, Math.abs(yawDiff)));
                    float returnYaw = rotateVector.x + yawDiff * returnFactor;
                    returnFactor = Math.min(1.0f, returnSpeed / Math.max(1.0f, Math.abs(pitchDiff)));
                    float returnPitch = rotateVector.y + pitchDiff * returnFactor;
                    rotateVector = new Vector2f(returnYaw, returnPitch);
                }
                lastYaw = rotateVector.x;
                lastPitch = rotateVector.y;
                if (options.getValueByName("Коррекция движения").get()) {
                    Minecraft.player.rotationYawOffset = rotateVector.x;
                }
            }
            case "SpookyTime" -> {
                float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 1f), 205);
                float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 1f), 205);

                yawDelta = rotateVector.x + (yawDelta > 0 ? clampedYaw : -clampedYaw) + ThreadLocalRandom.current().nextFloat(-3f, 3f);
                pitchDelta = clamp(rotateVector.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -85.0F, 85.0F) + ThreadLocalRandom.current().nextFloat(-3f, 3f);

                if (!shouldPlayerFalling()) {
                    yawDelta = rotateVector.x + (Minecraft.player.rotationYaw - rotateVector.x) / 1.5f + ThreadLocalRandom.current().nextFloat(-3, 3f);
                    pitchDelta = clamp(rotateVector.y + (Minecraft.player.rotationPitch - rotateVector.y) / 1.5f, -85, 85) + ThreadLocalRandom.current().nextFloat(-3f, 3f);
                }

                float gcd = SensUtils.getGCDValue();
                yawDelta -= (yawDelta - rotateVector.x) % gcd;
                pitchDelta -= (pitchDelta - rotateVector.y) % gcd;
                rotateVector = new Vector2f(yawDelta, pitchDelta);

                if (options.getValueByName("Коррекция движения").get()) {
                    Minecraft.player.rotationYawOffset = yawDelta;
                }
            }




        }
    }

    private void updateAttack() {
        float attackDistance = attackRange.get();
        selected = MouseUtil.getMouseOver(target, rotateVector.x, rotateVector.y, attackDistance);

        if (options.getValueByName("Ускорять ротацию при атаке").get()) {
            updateRotation(true, 60, 35);
        }

        if ((selected == null || selected != target) && !Minecraft.player.isElytraFlying()) {
            return;
        }

        if (Minecraft.player.getDistanceEyePos(target) > attackDistance) {
            return;
        }

        if (Minecraft.player.isBlocking() && options.getValueByName("Отжимать щит").get()) {
            mc.playerController.onStoppedUsingItem(Minecraft.player);
        }

        boolean wasSprinting = Minecraft.player.isSprinting();
        if (!sprintResetType.is("Выключен") && wasSprinting) {
            if (sprintResetType.is("Обычный")) {
                Minecraft.player.setSprinting(false);
            } else if (sprintResetType.is("Незаметный")) {
                Minecraft.player.connection.sendPacket(new CEntityActionPacket(Minecraft.player, CEntityActionPacket.Action.STOP_SPRINTING));
            } else if (sprintResetType.is("Legit")) {
                // Legit: использует только клиентские методы без пакетов
                Minecraft.player.setSprinting(false);
                mc.gameSettings.keyBindSprint.setPressed(false);
            }
        }

        boolean needShieldBreak = options.getValueByName("Ломать щит").get() && isTargetBlocking();

        if (needShieldBreak) {
            int axeSlot = findAxeSlot();
            if (axeSlot != -1) {
                previousSlot = Minecraft.player.inventory.currentItem;
                switchToSlot(axeSlot);
            }
        }

        stopWatch.setLastMS(500);
        mc.playerController.attackEntity(Minecraft.player, target);
        Minecraft.player.swingArm(Hand.MAIN_HAND);
        startHeadReaction();

        if (needShieldBreak && previousSlot != -1) {
            switchToSlot(previousSlot);
            previousSlot = -1;
        }

        if (!sprintResetType.is("Выключен") && wasSprinting) {
            if (sprintResetType.is("Обычный")) {
                Minecraft.player.setSprinting(true);
            } else if (sprintResetType.is("Незаметный")) {
                Minecraft.player.connection.sendPacket(new CEntityActionPacket(Minecraft.player, CEntityActionPacket.Action.START_SPRINTING));
            } else if (sprintResetType.is("Legit")) {
                // Legit: восстанавливаем спринт через клиентские методы
                Minecraft.player.setSprinting(true);
                mc.gameSettings.keyBindSprint.setPressed(true);
            }
        }

        hitCounter++;
        if (hitCounter >= 2) {
            String[] hitParts = {"head", "torso", "arms", "legs"};
            currentHitPart = hitParts[random.nextInt(hitParts.length)];
            hitCounter = 0;
        }
    }
    private void startHeadReaction() {
        isHeadReacting = true;
        isReturningToTarget = false;
        headReactionProgress = 0f;

        headReactionDirection = !headReactionDirection;

        float reactionAngle = 30f + random.nextFloat() * 15f;
        targetHeadYaw = headReactionDirection ? reactionAngle : -reactionAngle;

        headReactionTimer.reset();
    }

    private boolean isTargetBlocking() {
        if (target == null) return false;

        if (target.isHandActive()) {
            ItemStack activeItem = target.getActiveItemStack();
            return activeItem.getItem() == Items.SHIELD;
        }

        return false;
    }

    private int findAxeSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = Minecraft.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof AxeItem || stack.getItem() instanceof SwordItem) {
                return i;
            }
        }
        return -1;
    }

    private void switchToSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            Minecraft.player.inventory.currentItem = slot;
            Minecraft.player.connection.sendPacket(new CHeldItemChangePacket(slot));
        }
    }

    private boolean shouldPlayerFalling() {
        boolean cancelReason = Minecraft.player.isInWater() && Minecraft.player.areEyesInFluid(FluidTags.WATER) || Minecraft.player.isInLava() || Minecraft.player.isOnLadder() || Minecraft.player.isPassenger() || Minecraft.player.abilities.isFlying;

        float attackStrength = Minecraft.player.getCooledAttackStrength(options.getValueByName("Синхронизировать атаку с ТПС").get()
                ? Initclass.getInstance().getTpsCalc().getAdjustTicks() : 1.5f);

        if (attackStrength < 0.92f) {
            return false;
        }

        if (!cancelReason && options.getValueByName("Только криты").get()) {
            return !Minecraft.player.isOnGround() && Minecraft.player.fallDistance > 0;
        }

        return true;
    }

    private boolean isValid(LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity) return false;

        if (entity.ticksExisted < 3) return false;

        if (Minecraft.player.getDistanceEyePos(entity) > attackRange.get()) return false;

        if (options.getValueByName("Не бить через стены").get() && !hasClearLineOfSight(entity)) {
            return false;
        }

        if (entity instanceof PlayerEntity p) {
            if (AntiBot.isBot(entity) && !targets.getValueByName("Боты").get()) {
                return false;
            }
            if (!targets.getValueByName("Друзья").get() && FriendStorage.isFriend(p.getName().getString())) {
                return false;
            }
            if (p.getName().getString().equalsIgnoreCase(Minecraft.player.getName().getString())) return false;
        }
        if (!type.is("HvH")
                && options.getValueByName("Ограничение FOV").get()) {

            double fovToEntity = getFovToEntity(entity);
            if (fovToEntity > fov.get()) {
                return false;
            }
        }


        if (entity instanceof PlayerEntity && !targets.getValueByName("Игроки").get()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.getTotalArmorValue() == 0 && !targets.getValueByName("Голые").get()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.isInvisible() && entity.getTotalArmorValue() == 0 && !targets.getValueByName("Голые невидимки").get()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.isInvisible() && !targets.getValueByName("Невидимки").get()) {
            return false;
        }

        if (entity instanceof MonsterEntity && !targets.getValueByName("Мобы").get()) {
            return false;
        }
        if (entity instanceof AnimalEntity && !targets.getValueByName("Животные").get()) {
            return false;
        }

        return !entity.isInvulnerable() && entity.isAlive() && !(entity instanceof ArmorStandEntity);
    }

    private void reset() {
        if (options.getValueByName("Коррекция движения").get()) {
            Minecraft.player.rotationYawOffset = Integer.MIN_VALUE;
        }
        rotateVector = new Vector2f(Minecraft.player.rotationYaw, Minecraft.player.rotationPitch);
        hitCounter = 0;
        currentHitPart = "head";
        previousSlot = -1;
        isHeadReacting = false;
        isReturningToTarget = false;
        headReactionProgress = 0f;
        targetHeadYaw = 0f;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
        target = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
        stopWatch.setLastMS(0);
        target = null;
    }

    private double getEntityArmor(PlayerEntity entityPlayer2) {
        double d2 = 0.0;
        for (int i2 = 0; i2 < 4; ++i2) {
            ItemStack is = entityPlayer2.inventory.armorInventory.get(i2);
            if (!(is.getItem() instanceof ArmorItem)) continue;
            d2 += getProtectionLvl(is);
        }
        return d2;
    }

    private double getProtectionLvl(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem i) {
            double damageReduceAmount = i.getDamageReduceAmount();
            if (stack.isEnchanted()) {
                damageReduceAmount += (double) EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack) * 0.25;
            }
            return damageReduceAmount;
        }
        return 0;
    }

    private double getEntityHealth(LivingEntity ent) {
        if (ent instanceof PlayerEntity player) {
            return (double) (player.getHealth() + player.getAbsorptionAmount()) * (getEntityArmor(player) / 20.0);
        }
        return ent.getHealth() + ent.getAbsorptionAmount();
    }

    private double getFovToEntity(LivingEntity entity) {
        Vector3d playerLook = Minecraft.player.getLookVec();
        Vector3d toEntity = entity.getPositionVec().subtract(Minecraft.player.getPositionVec()).normalize();
        double dot = playerLook.dotProduct(toEntity);
        return Math.acos(dot) * (180.0 / Math.PI);
    }

    private boolean isUsingItem() {
        if (!options.getValueByName("Не бить при использовании").get()) return false;
        ItemStack item = Minecraft.player.getHeldItemMainhand();
        return Minecraft.player.isHandActive() || item.getItem().isFood() || item.getItem() instanceof BowItem ||
                item.getItem() == Items.TRIDENT || item.getItem() == Items.POTION;
    }

    private boolean hasClearLineOfSight(LivingEntity entity) {
        Vector3d start = Minecraft.player.getEyePosition(1.0F);
        Vector3d end = entity.getPositionVec().add(0, entity.getHeight() * 0.5, 0);
        RayTraceContext context = new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, Minecraft.player);
        RayTraceResult result = Minecraft.world.rayTraceBlocks(context);
        return result.getType() == RayTraceResult.Type.MISS;
    }

    public void critHelper() {
        switch (critType.get()) {
            case "None"-> {
            }

            case "Matrix" -> {
                if (mc.player.isJumping && Minecraft.player.motion.getY() < -0.1 && Minecraft.player.fallDistance > 0.5 && MoveUtils.getMotion() < 0.12) {
                    Minecraft.player.motion.y = -1.0;
                }
            }

            case "NCP" -> {
                if (!Minecraft.player.isJumping || Minecraft.player.fallDistance == 0.0f) break;
                Minecraft.player.motion.y -= 0.078;
            }

            case "NCP+" -> {
                if ((Minecraft.player.fallDistance > 0.7 && Minecraft.player.fallDistance < 0.8) && target != null) {
                    mc.timer.timerSpeed = 2.0f;
                } else {
                    mc.timer.timerSpeed = 1;
                }
            }

            case "Grim" -> {
                if (Minecraft.player.isJumping && Minecraft.player.fallDistance > 0.0F && (double) Minecraft.player.fallDistance <= 1.2 && !MoveUtils.moveKeysPressed()) {
                    Minecraft.player.jumpTicks = 0;
                    if (mc.timer.timerSpeed == 1.0) {
                        mc.timer.timerSpeed = 1.0049999952316284f;
                    }
                }
            }
        }
    }
    public float attackDistance() {

        if (!Minecraft.player.isSwimming())
            return  3.3f;
        else
            return  3.0f;


    }
}