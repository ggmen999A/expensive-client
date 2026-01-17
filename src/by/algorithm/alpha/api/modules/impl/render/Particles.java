package by.algorithm.alpha.api.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.AttackEvent;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.player.ProjectionUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static net.minecraft.client.renderer.WorldRenderer.frustum;

@ModuleAnnot(name = "Particles", type = ModuleCategory.Render, description = "Партиклы при ударе")
public class Particles extends Module {

    private final ModeSetting setting = new ModeSetting("Вид", "Сердечки", "Сердечки", "Орбизы", "Молния", "Снежинки");
    private final SliderSetting value = new SliderSetting("Кол-во за удар", 20.0f, 1.0f, 50.0f, 1.0f);
    private final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();

    public Particles() {
        addSettings(setting, value);
    }

    private boolean isInView(Vector3d pos) {
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x,
                mc.getRenderManager().info.getProjectedView().y,
                mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(new AxisAlignedBB(pos.add(-0.2, -0.2, -0.2), pos.add(0.2, 0.2, 0.2)));
    }

    @Subscribe
    private void onUpdate(AttackEvent e) {
        if (e.entity == mc.player) return;
        if (e.entity instanceof LivingEntity livingEntity) {
            Vector3d hitPos = livingEntity.getPositon(mc.getRenderPartialTicks()).add(0, livingEntity.getHeight() / 2f, 0);

            for (int i = 0; i < value.get(); i++) {
                particles.add(new Particle(hitPos));
            }
        }
    }

    @Subscribe
    private void onDisplay(EventDisplay e) {
        if (mc.player == null || mc.world == null || e.getType() != EventDisplay.Type.PRE) {
            return;
        }

        for (Particle p : particles) {
            if (System.currentTimeMillis() - p.time > 8000) {
                particles.remove(p);
                continue;
            }
            if (mc.player.getPositionVec().distanceTo(p.pos) > 30) {
                particles.remove(p);
                continue;
            }
            if (isInView(p.pos)) {
                if (!mc.player.canEntityBeSeen(p.pos)) {
                    particles.remove(p);
                    continue;
                }
                p.update();
                Vector2f pos = ProjectionUtil.project(p.pos.x, p.pos.y, p.pos.z);

                float lifeProgress = (System.currentTimeMillis() - p.time) / 8000f;
                float size = Math.max(0.1f, 1.0f - lifeProgress * 0.7f);

                DisplayUtils.drawShadowCircle(pos.x, pos.y, 10 * size, ColorUtils.setAlpha(HUD.getColor(particles.indexOf(p), 1), (int) ((64 * p.alpha) * size)));

                switch (setting.get()) {
                    case "Сердечки" -> {
                        Fonts.damage.drawText(e.getMatrixStack(), "B", pos.x - 3 * size, pos.y - 3 * size, ColorUtils.setAlpha(HUD.getColor(particles.indexOf(p), 1), (int) ((200 * p.alpha) * size)), 15 * size, 0.05f);
                    }
                    case "Снежинки" -> {
                        Fonts.damage.drawText(e.getMatrixStack(), "A", pos.x - 3 * size, pos.y - 3 * size, ColorUtils.setAlpha(HUD.getColor(particles.indexOf(p), 1), (int) ((200 * p.alpha) * size)), 15 * size, 0.05f);
                    }
                    case "Молния" -> {
                        Fonts.damage.drawText(e.getMatrixStack(), "C", pos.x - 3 * size, pos.y - 3 * size, ColorUtils.setAlpha(HUD.getColor(particles.indexOf(p), 1), (int) ((200 * p.alpha) * size)), 15 * size, 0.05f);
                    }
                    case "Орбизы" -> {
                        DisplayUtils.drawCircle(pos.x, pos.y, 5 * size, ColorUtils.setAlpha(HUD.getColor(particles.indexOf(p), 1), (int) ((200 * p.alpha) * size)));
                    }
                }
            } else {
                particles.remove(p);
            }
        }
    }

    private class Particle {
        private Vector3d pos;
        private Vector3d velocity;
        private final long time;
        private float alpha;
        private boolean hasBouncedOnce = false;

        private static final double GRAVITY = -0.025;
        private static final double BOUNCE_DAMPING = 0.7;
        private static final double FRICTION = 0.985;

        public Particle(Vector3d startPos) {
            this.pos = startPos;

            // Уменьшенная начальная скорость для более плавного движения
            double horizontalSpeed = ThreadLocalRandom.current().nextDouble(0.02, 0.08);
            double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);

            this.velocity = new Vector3d(
                    Math.cos(angle) * horizontalSpeed,
                    ThreadLocalRandom.current().nextDouble(0.03, 0.08), // вверх
                    Math.sin(angle) * horizontalSpeed
            );

            this.time = System.currentTimeMillis();
            this.alpha = 0f;
        }

        public void update() {
            // Плавная анимация появления
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - time;

            if (elapsed < 800) {
                // Более медленное появление с постепенным ускорением
                float appearProgress = elapsed / 800f;
                alpha = MathUtil.fast(alpha, appearProgress, 8);
            } else if (elapsed > 6000) {
                // Fade out в последние 2 секунды
                float fadeProgress = (elapsed - 6000) / 2000f;
                alpha = MathUtil.fast(alpha, Math.max(0, 1.0f - fadeProgress), 12);
            } else {
                alpha = MathUtil.fast(alpha, 1.0f, 10);
            }

            // Применяем гравитацию с более плавным нарастанием
            velocity = velocity.add(0, GRAVITY, 0);

            // Применяем трение к горизонтальному движению
            velocity = new Vector3d(
                    velocity.x * FRICTION,
                    velocity.y,
                    velocity.z * FRICTION
            );

            // Обновляем позицию
            Vector3d newPos = pos.add(velocity);

            // Проверяем столкновение с землей
            BlockPos blockBelow = new BlockPos(newPos.x, newPos.y - 0.1, newPos.z);

            if (mc.world.getBlockState(blockBelow).isSolid() && velocity.y < 0) {
                // Отскок от земли с более плавным затуханием
                velocity = new Vector3d(
                        velocity.x * BOUNCE_DAMPING,
                        -velocity.y * BOUNCE_DAMPING,
                        velocity.z * BOUNCE_DAMPING
                );

                hasBouncedOnce = true;

                // Минимальная скорость отскока
                if (Math.abs(velocity.y) < 0.015) {
                    velocity = new Vector3d(velocity.x * 0.9, 0, velocity.z * 0.9);
                }

                // Корректируем позицию
                newPos = new Vector3d(newPos.x, blockBelow.getY() + 1.01, newPos.z);
            }

            // Проверяем столкновения со стенами
            BlockPos blockAtPos = new BlockPos(newPos.x, newPos.y, newPos.z);
            if (mc.world.getBlockState(blockAtPos).isSolid()) {
                BlockPos blockX = new BlockPos(pos.x + velocity.x, pos.y, pos.z);
                BlockPos blockZ = new BlockPos(pos.x, pos.y, pos.z + velocity.z);

                if (mc.world.getBlockState(blockX).isSolid()) {
                    velocity = new Vector3d(-velocity.x * BOUNCE_DAMPING, velocity.y, velocity.z);
                }
                if (mc.world.getBlockState(blockZ).isSolid()) {
                    velocity = new Vector3d(velocity.x, velocity.y, -velocity.z * BOUNCE_DAMPING);
                }

                newPos = pos;
            }

            pos = newPos;
        }
    }
}