package by.algorithm.alpha.system.visuals.hud.impl;

import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.utils.animations.Animation;
import by.algorithm.alpha.system.utils.animations.Direction;
import by.algorithm.alpha.system.utils.animations.impl.EaseBackIn;
import by.algorithm.alpha.system.utils.animations.impl.EaseInOutQuad;
import by.algorithm.alpha.system.utils.dragable.Dragging;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.math.Vector4i;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.Scissor;
import by.algorithm.alpha.system.utils.render.Stencil;
import by.algorithm.alpha.api.modules.impl.render.HUD;
import by.algorithm.alpha.system.visuals.hud.ElementRenderer;
import by.algorithm.alpha.system.visuals.styles.Style;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.optifine.util.TextureUtils;
import org.lwjgl.opengl.GL11;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class MediaplayerRenderer implements ElementRenderer {

    final Minecraft mc = Minecraft.getInstance();
    final StopWatch stopWatch = new StopWatch();
    final Dragging drag;

    IMediaSession session = null;
    String title = "No track";
    String artist = "Unknown artist";
    int duration = 0;
    int position = 0;
    boolean isPlaying = false;
    BufferedImage artwork = null;

    // Добавляем кеширование текстуры
    private ResourceLocation artworkTexture = null;
    private BufferedImage lastArtwork = null;

    final Animation animation = new EaseBackIn(400, 1, 1);
    final Animation hitAnimation = new EaseInOutQuad(300, 1, Direction.BACKWARDS);
    final Animation progressAnimation = new EaseInOutQuad(250, 1, Direction.FORWARDS);

    float progressAnimationValue = 0.0f;
    float hitRedOverlay = 0.0f;
    float scrollOffset = 0.0f;
    long scrollStartTime = 0;

    float width = 100;
    float height = 35;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final List<String> ownerPriority = List.of("SPOTIFY", "CHROME", "FIREFOX", "EDGE");
    private boolean isMediaPlayerInfoEnabled = true;
    boolean allowRender = false;

    @Override
    public void render(EventDisplay eventDisplay) {
        if (Initclass.getInstance().getFunctionRegistry() != null) {
            isMediaPlayerInfoEnabled = Initclass.getInstance().getFunctionRegistry().getHud().isState()
                    && HUD.elements.getValueByName("Активная музыка").get();
        }

        updateMediaInfo();

        float x = drag.getX();
        float y = drag.getY();

        boolean out = !allowRender || stopWatch.isReached(3000);
        animation.setDuration(out ? 400 : 300);
        animation.setDirection(out ? Direction.BACKWARDS : Direction.FORWARDS);

        if (isPlaying) {
            hitAnimation.setDirection(Direction.FORWARDS);
            hitRedOverlay = 0.15f;
        } else {
            hitAnimation.setDirection(Direction.BACKWARDS);
        }
        hitRedOverlay = (float) hitAnimation.getOutput();

        if (animation.getOutput() == 0.0f) {
            return;
        }

        MatrixStack matrix = eventDisplay.getMatrixStack();

        GlStateManager.pushMatrix();
        Style style = Initclass.getInstance().getStyleManager().getCurrentStyle();

        sizeAnimation(x + (width / 2), y + (height / 2), animation.getOutput());

        DisplayUtils.drawRoundedRect(x, y, width, height, 4, ColorUtils.rgba(0, 0, 0, 200));

        renderArtwork(x, y, eventDisplay);

        renderTrackInfo(x, y, eventDisplay);

        renderProgressBar(x, y, eventDisplay, style);

        GlStateManager.popMatrix();

        drag.setWidth(width);
        drag.setHeight(height);
    }

    private void renderArtwork(float x, float y, EventDisplay eventDisplay) {
        float artworkSize = 25;
        float artworkX = x + 4;
        float artworkY = y + 5;

        Stencil.initStencilToWrite();
        DisplayUtils.drawRoundedRect(artworkX, artworkY, artworkSize, artworkSize,
                1, ColorUtils.rgba(255, 255, 255, 255));
        Stencil.readStencilBuffer(1);

        if (artwork != null) {
            // Создаем или обновляем текстуру только если изображение изменилось
            if (lastArtwork != artwork) {
                try {
                    // Удаляем старую текстуру если она есть
                    if (artworkTexture != null) {
                        mc.getTextureManager().deleteTexture(artworkTexture);
                    } else {
                        // Обнуляем кешированную текстуру если обложка трека отсутствует
                        if (artworkTexture != null) {
                            mc.getTextureManager().deleteTexture(artworkTexture);
                            artworkTexture = null;
                            lastArtwork = null;
                        }

                        // Рисуем дефолтный градиент когда нет обложки трека
                        Vector4i defaultGradient = new Vector4i(
                                ColorUtils.rgba(70, 70, 70, 255),
                                ColorUtils.rgba(90, 90, 90, 255),
                                ColorUtils.rgba(110, 110, 110, 255),
                                ColorUtils.rgba(70, 70, 70, 255)
                        );
                        DisplayUtils.drawRoundedRect(artworkX, artworkY, artworkSize, artworkSize,
                                new Vector4f(1, 1, 1, 1), defaultGradient);

                        float noteX = artworkX + artworkSize / 2 - 3;
                        float noteY = artworkY + artworkSize / 2 - 3;
                        Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), "♫",
                                noteX, noteY, ColorUtils.rgba(180, 180, 180, 255), 7);
                    }

                    // Создаем новую текстуру с улучшенным качеством
                    DynamicTexture dynamicTexture = new DynamicTexture(TextureUtils.toNativeImage(artwork));
                    dynamicTexture.setBlurMipmap(false, false); // Отключаем размытие для четкости
                    artworkTexture = mc.getTextureManager().getDynamicTextureLocation("artwork", dynamicTexture);

                    // Настраиваем фильтрацию для лучшего качества
                    GlStateManager.bindTexture(dynamicTexture.getGlTextureId());
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                    lastArtwork = artwork;
                } catch (Exception e) {
                    artworkTexture = null;
                    e.printStackTrace();
                }
            }

            // Рендерим обложку трека если текстура создана успешно
            if (artworkTexture != null) {
                DisplayUtils.drawImage(artworkTexture, artworkX, artworkY, artworkSize, artworkSize, ColorUtils.rgba(255, 255, 255, 255));
            } else {
                // Если не удалось создать текстуру, рисуем градиент с нотой
                Vector4i artworkGradient = new Vector4i(
                        ColorUtils.rgba(102, 126, 234, 255),
                        ColorUtils.rgba(118, 75, 162, 255),
                        ColorUtils.rgba(64, 224, 208, 255),
                        ColorUtils.rgba(102, 126, 234, 255)
                );
                DisplayUtils.drawRoundedRect(artworkX, artworkY, artworkSize, artworkSize,
                        new Vector4f(1, 1, 1, 1), artworkGradient);

                float noteX = artworkX + artworkSize / 2 - 4;
                float noteY = artworkY + artworkSize / 2 - 4;
                Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), "♪",
                        noteX, noteY, ColorUtils.rgba(255, 255, 255, 200), 8);
            }
        } else {
            // Обнуляем кешированную текстуру если artwork стал null
            if (artworkTexture != null) {
                mc.getTextureManager().deleteTexture(artworkTexture);
                artworkTexture = null;
                lastArtwork = null;
            }

            // Рисуем дефолтный градиент
            Vector4i defaultGradient = new Vector4i(
                    ColorUtils.rgba(70, 70, 70, 255),
                    ColorUtils.rgba(90, 90, 90, 255),
                    ColorUtils.rgba(110, 110, 110, 255),
                    ColorUtils.rgba(70, 70, 70, 255)
            );
            DisplayUtils.drawRoundedRect(artworkX, artworkY, artworkSize, artworkSize,
                    new Vector4f(1, 1, 1, 1), defaultGradient);

            float noteX = artworkX + artworkSize / 2 - 3;
            float noteY = artworkY + artworkSize / 2 - 3;
            Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), "♫",
                    noteX, noteY, ColorUtils.rgba(180, 180, 180, 255), 7);
        }

        Stencil.uninitStencilBuffer();
    }

    private void renderTrackInfo(float x, float y, EventDisplay eventDisplay) {
        float textX = x + 35;
        float textY = y + 5.5f;
        float availableWidth = width - 40;

        String displayTitle = title != null ? title : "No track";
        String displayArtist = artist != null ? artist : "Unknown artist";
        String combinedText = displayTitle + " (" + displayArtist + ")";

        float textWidth = Fonts.tenacity.getWidth(combinedText, 8);

        Scissor.push();
        Scissor.setFromComponentCoordinates(textX, textY, availableWidth, height);

        if (textWidth > availableWidth) {
            long currentTime = System.currentTimeMillis();

            if (scrollStartTime == 0) {
                scrollStartTime = currentTime;
            }

            long elapsedTime = currentTime - scrollStartTime;

            if (elapsedTime > 2000) {
                float scrollTime = (elapsedTime - 2000) / 50.0f;
                scrollOffset = scrollTime;

                float maxOffset = textWidth + 20;
                if (scrollOffset > maxOffset) {
                    scrollStartTime = currentTime;
                    scrollOffset = 0;
                }
            } else {
                scrollOffset = 0;
            }

            float renderX = textX - scrollOffset;

            Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), displayTitle,
                    renderX, textY, ColorUtils.rgba(255, 255, 255, 255), 8);

            float artistX = renderX + Fonts.tenacity.getWidth(displayTitle + " ", 8);
            Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), "(" + displayArtist + ")",
                    artistX, textY, ColorUtils.rgba(128, 128, 128, 255), 8);

            if (elapsedTime > 2000) {
                float secondRenderX = renderX + textWidth + 20;
                if (secondRenderX < textX + availableWidth) {
                    Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), displayTitle,
                            secondRenderX, textY, ColorUtils.rgba(255, 255, 255, 255), 8);

                    float secondArtistX = secondRenderX + Fonts.tenacity.getWidth(displayTitle + " ", 8);
                    Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), "(" + displayArtist + ")",
                            secondArtistX, textY, ColorUtils.rgba(128, 128, 128, 255), 8);
                }
            }
        } else {
            scrollOffset = 0.0f;
            scrollStartTime = 0;

            Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), displayTitle,
                    textX, textY, ColorUtils.rgba(255, 255, 255, 255), 8);

            float artistX = textX + Fonts.tenacity.getWidth(displayTitle + " ", 8);
            Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), "(" + displayArtist + ")",
                    artistX, textY, ColorUtils.rgba(128, 128, 128, 255), 8);
        }

        Scissor.unset();
        Scissor.pop();
    }

    private void renderProgressBar(float x, float y, EventDisplay eventDisplay, Style style) {
        if (duration <= 0) return;

        float progressX = x + 35;
        float progressY = y + 22;
        float progressWidth = width - 40;
        float progressHeight = 6;

        String currentTimeStr = formatDuration(position);
        String totalTimeStr = formatDuration(duration);
        String timeDisplay = currentTimeStr + " из " + totalTimeStr;

        Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), timeDisplay,
                progressX, progressY - 8, ColorUtils.rgba(255, 255, 255, 255), 7);

        DisplayUtils.drawRoundedRect(progressX, progressY, progressWidth, progressHeight,
                new Vector4f(3, 3, 3, 3), ColorUtils.rgb(32, 32, 32));

        float progress = MathHelper.clamp((float) position / duration, 0, 1);
        progressAnimationValue = MathUtil.fast(progressAnimationValue, progress, 8);

        if (progressAnimationValue > 0) {
            Vector4i redGradient = new Vector4i(
                    ColorUtils.rgba(255, 50, 50, 255),
                    ColorUtils.rgba(255, 50, 50, 255),
                    ColorUtils.rgba(255, 80, 80, 255),
                    ColorUtils.rgba(255, 50, 50, 255)
            );
            DisplayUtils.drawRoundedRect(progressX, progressY,
                    progressWidth * progressAnimationValue, progressHeight,
                    new Vector4f(3, 3, 3, 3), redGradient);
        }
    }

    private void updateMediaInfo() {
        if (!isMediaPlayerInfoEnabled) {
            title = "Media Info Disabled";
            artist = "N/A";
            duration = 0;
            position = 0;
            allowRender = false;
            return;
        }

        executorService.submit(() -> {
            try {
                List<IMediaSession> sessions = MediaPlayerInfo.Instance != null ?
                        MediaPlayerInfo.Instance.getMediaSessions() : null;

                if (sessions == null || sessions.isEmpty()) {
                    allowRender = false;
                    return;
                }

                sessions = sessions.stream()
                        .sorted((s1, s2) -> Integer.compare(getOwnerPriorityIndex(s1), getOwnerPriorityIndex(s2)))
                        .collect(Collectors.toList());

                IMediaSession mediaSession = sessions.stream()
                        .filter(s -> s.getMedia() != null &&
                                (!s.getMedia().getArtist().isEmpty() || !s.getMedia().getTitle().isEmpty()))
                        .findFirst()
                        .orElse(null);

                if (mediaSession != null && mediaSession.getMedia() != null) {
                    MediaInfo media = mediaSession.getMedia();
                    title = media.getTitle();
                    artist = media.getArtist();
                    duration = (int) media.getDuration();
                    position = (int) media.getPosition();
                    isPlaying = media.getPlaying();
                    artwork = media.getArtwork();

                    allowRender = true;
                    stopWatch.reset();
                } else {
                    allowRender = false;
                }
            } catch (Throwable e) {
                title = "Loading...";
                artist = "Loading...";
                duration = 0;
                position = 0;
                isPlaying = false;
                allowRender = true;
                e.printStackTrace();
            }
        });
    }

    private int getOwnerPriorityIndex(IMediaSession session) {
        for (int i = 0; i < ownerPriority.size(); i++) {
            if (session.getOwner().toUpperCase().contains(ownerPriority.get(i))) {
                return i;
            }
        }
        return ownerPriority.size();
    }

    private String formatDuration(int duration) {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0);
    }
}