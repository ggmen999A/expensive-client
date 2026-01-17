package by.algorithm.alpha.api.rotation;

import by.algorithm.alpha.api.modules.impl.combat.AttackAura;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector2f;

public interface RotationStrategy {
    /**
     * Выполнить вычисление новой вектор-ротации.
     *
     * @param ctx                    ссылка на модуль (можно использовать геттеры/настройки)
     * @param target                 текущая цель
     * @param aimVec                 вектор от глаз игрока до точки прицеливания
     * @param attack                 флаг — мы атакуем или нет
     * @param rotationYawSpeed       параметр скорости из вызова (можно игнорировать)
     * @param rotationPitchSpeed     параметр скорости из вызова (можно игнорировать)
     * @return новая rotateVector (yaw, pitch)
     */
    Vector2f rotate(AttackAura ctx, LivingEntity target, Vector3d aimVec, boolean attack,
                    float rotationYawSpeed, float rotationPitchSpeed);

    String getName();
}
