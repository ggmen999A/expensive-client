package by.algorithm.alpha.api.modules.notifications;

import by.algorithm.alpha.api.command.friends.FriendStorage;
import by.algorithm.alpha.system.utils.animations.Animation;
import by.algorithm.alpha.system.utils.animations.Direction;
import by.algorithm.alpha.system.utils.animations.impl.EaseBackIn;
import by.algorithm.alpha.system.utils.animations.impl.EaseInOutQuad;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import com.mojang.blaze3d.matrix.MatrixStack;

import java.util.concurrent.CopyOnWriteArrayList;

import static by.algorithm.alpha.system.utils.client.IMinecraft.mc;


public class NotificationManager {
    public static final FriendStorage NOTIFICATION_MANAGER = null;
    private final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();
    private MathUtil AnimationMath;
    boolean state;

    public void add(String text, String content, int time) {
        this.notifications.add(new Notification(text, content, time));
    }

    public void draw(MatrixStack stack) {
        int yOffset = 0;


        float startY = (float) mc.getMainWindow().scaledHeight() / 2f + 20f;

        for (Notification notification : this.notifications) {
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - notification.getTime();
            long totalDuration = notification.time2 * 1000L;

            if (elapsed <= totalDuration - 300L) {
                notification.yAnimation.setDirection(Direction.FORWARDS);
                notification.fadeAnimation.setDirection(Direction.FORWARDS);
            }

            if (elapsed > totalDuration - 300L) {
                notification.fadeAnimation.setDirection(Direction.BACKWARDS);
            }

            if (elapsed > totalDuration) {
                notification.yAnimation.setDirection(Direction.BACKWARDS);
            }

            if (notification.yAnimation.finished(Direction.BACKWARDS)) {
                this.notifications.remove(notification);
                continue;
            }

            float x = (float) mc.getMainWindow().scaledWidth() / 2f - notification.getWidth() / 2f;
            notification.yAnimation.setEndPoint(yOffset);
            notification.yAnimation.setDuration(500);
            notification.setX(x);
            notification.setY(MathUtil.fast(notification.getY(), startY + yOffset, 15.0f));
            yOffset += notification.getHeight() + 1.5f;

            notification.draw(stack);
        }
    }

    private class Notification {
        private float x = 0.0f;
        private float y = mc.getMainWindow().scaledHeight() + 22;
        private String text;
        private String content;
        private long time = System.currentTimeMillis();
        public Animation animation = new EaseInOutQuad(300, 1.0, Direction.FORWARDS);
        public Animation yAnimation = new EaseBackIn(300, 1.0, 1.0f);
        public Animation fadeAnimation = new EaseInOutQuad(300, 1.0, Direction.FORWARDS);
        float alpha;
        int time2 = 3;
        private boolean isState;
        private boolean state;

        private final float padding = 8.0f;
        private final float fontSize = 6.10f;
        private final float panelHeight = 17.5f;
        private final float cornerRadius = 3.3f;
        private final int bgAlpha = 160;

        public Notification(String text, String content, int time) {
            this.text = text;
            this.content = content;
            this.time2 = time;
        }

        public float draw(MatrixStack stack) {
            float panelWidth = getWidth();
            float panelX = getX();
            float panelY = getY();

            float fadeAlpha = (float) fadeAnimation.getOutput();
            int finalBgAlpha = (int) (bgAlpha * fadeAlpha);
            int textAlpha = (int) (230 * fadeAlpha);
            int contentAlpha = this.text.toLowerCase().contains("зелье") ?
                    (int) (200 * fadeAlpha) : (int) (180 * fadeAlpha);

            DisplayUtils.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, cornerRadius,
                    ColorUtils.rgba(0, 0, 0, finalBgAlpha));

            float iconX = panelX + 4f;
            float iconY = panelY + 4f;

            if (this.text.contains("включен")) {
                Fonts.nur.drawText(stack, "H", iconX, iconY,
                        ColorUtils.rgba(255, 255, 255, (int)(255 * fadeAlpha)), 10f);
            } else if (this.text.contains("выключен")) {
                Fonts.nur.drawText(stack, "I", iconX, iconY,
                        ColorUtils.rgba(255, 255, 255, (int)(255 * fadeAlpha)), 10f);
            }



            if (this.text.contains("Сломал")) {
                Fonts.nur.drawText(stack, "U", iconX, iconY,
                        ColorUtils.rgba(255, 255, 255, (int)(255 * fadeAlpha)), 10f);
            }

            if (this.text.contains("закончилось")) {
                Fonts.nur.drawText(stack, "J", iconX, iconY,
                        ColorUtils.rgba(255, 255, 255, (int)(255 * fadeAlpha)), 10f);
            }

            float textX = panelX + 19f;
            float textY = panelY + 6.5f;

            Fonts.sfMedium.drawText(stack, text, textX, textY,
                    ColorUtils.rgba(255, 255, 255, textAlpha), fontSize);

            if (content != null && !content.isEmpty()) {
                if (this.text.toLowerCase().contains("зелье")) {
                    Fonts.sfMedium.drawText(stack, content, textX, textY + 10.0f,
                            ColorUtils.rgba(180, 0, 255, contentAlpha), fontSize - 0.5f);
                } else {
                    Fonts.sfMedium.drawText(stack, content, textX, textY + 10.0f,
                            ColorUtils.rgba(180, 180, 180, contentAlpha), fontSize - 0.5f);
                }
            }

            return panelHeight;
        }



        public float getWidth() {
            float textWidth = Fonts.sfMedium.getWidth(text, fontSize);
            float contentWidth = content != null ? Fonts.sfMedium.getWidth(content, fontSize - 0.5f) : 0;
            float maxWidth = Math.max(textWidth, contentWidth);
            return maxWidth + 28f;
        }

        public float getHeight() {
            return content != null && !content.isEmpty() ? panelHeight + 5.0f : panelHeight;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }

        public String getText() {
            return this.text;
        }

        public String getContent() {
            return this.content;
        }

        public long getTime() {
            return this.time;
        }
    }
}