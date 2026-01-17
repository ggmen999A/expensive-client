package by.algorithm.alpha.system.visuals.hud.impl;

import by.algorithm.alpha.api.command.staffs.StaffStorage;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.system.utils.animations.Direction;
import by.algorithm.alpha.system.utils.animations.impl.EaseInOutQuad;
import by.algorithm.alpha.system.visuals.hud.ElementUpdater;
import com.mojang.blaze3d.matrix.MatrixStack;
import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.utils.dragable.Dragging;
import by.algorithm.alpha.system.utils.math.Vector4i;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.Stencil;
import by.algorithm.alpha.system.visuals.hud.ElementRenderer;
import by.algorithm.alpha.system.visuals.styles.Style;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class StaffActive implements ElementRenderer, ElementUpdater {

    final Dragging dragging;

    private final List<Staff> staffPlayers = new ArrayList<>();
    private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    private final Pattern prefixMatches = Pattern.compile(".*(mod|der|adm|help|wne|хелп|адм|поддержка|кура|own|taf|curat|dev|supp|yt|сотруд).*");
    private final EaseInOutQuad animation = new EaseInOutQuad(300, 1, Direction.BACKWARDS);
    long lastStateChangeTime = 0;
    boolean lastShouldRender = false;

    static final Minecraft mc = Minecraft.getInstance();

    @Override
    public void update(EventUpdate e) {
        List<Staff> newStaffPlayers = new ArrayList<>();
        long currentTime = System.currentTimeMillis();


        for (ScorePlayerTeam team : mc.world.getScoreboard().getTeams().stream().sorted(Comparator.comparing(Team::getName)).toList()) {
            String name = team.getMembershipCollection().toString().replaceAll("[\\[\\]]", "");
            boolean vanish = true;
            NetworkPlayerInfo playerInfo = null;
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    vanish = false;
                    playerInfo = info;
                }
            }
            if (namePattern.matcher(name).matches() && !name.equals(mc.player.getName().getString())) {
                if (!vanish && playerInfo != null) {
                    boolean isStaff = prefixMatches.matcher(team.getPrefix().getString().toLowerCase(Locale.ROOT)).matches() || StaffStorage.isStaff(name);
                    if (isStaff) {
                        Staff existingStaff = staffPlayers.stream()
                                .filter(s -> s.getName().equals(name))
                                .findFirst()
                                .orElse(null);
                        long joinTime = existingStaff != null ? existingStaff.getJoinTime() : currentTime;
                        Staff staff = new Staff(team.getPrefix(), name, false, Status.NONE, joinTime, playerInfo);
                        newStaffPlayers.add(staff);
                    }
                }
                if (vanish && !team.getPrefix().getString().isEmpty()) {
                    Staff existingStaff = staffPlayers.stream()
                            .filter(s -> s.getName().equals(name))
                            .findFirst()
                            .orElse(null);
                    long joinTime = existingStaff != null ? existingStaff.getJoinTime() : currentTime;
                    Staff staff = new Staff(team.getPrefix(), name, true, Status.VANISHED, joinTime, null);
                    newStaffPlayers.add(staff);
                }
            }
        }

        staffPlayers.clear();
        staffPlayers.addAll(newStaffPlayers);
    }

    float width;
    float height;
    int yOff;

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack matrix = eventDisplay.getMatrixStack();
        float x = dragging.getX();
        float y = dragging.getY();
        Style style = Initclass.getInstance().getStyleManager().getCurrentStyle();
        Vector4i vector4i = new Vector4i(style.getFirstColor().getRGB(), style.getFirstColor().getRGB(), style.getSecondColor().getRGB(), style.getSecondColor().getRGB());

        String name = "Staff";

        boolean isChatOpen = mc.currentScreen instanceof ChatScreen;
        boolean shouldRender = mc.player != null && !isChatOpen && !staffPlayers.isEmpty();
        if (shouldRender != lastShouldRender) {
            lastStateChangeTime = System.currentTimeMillis();
            lastShouldRender = shouldRender;
        }

        if (isChatOpen) {
            animation.setDirection(Direction.FORWARDS);
        } else if (!shouldRender) {
            animation.setDirection(Direction.BACKWARDS);
            if (animation.getOutput() == 0) return;
        } else {
            animation.setDirection(Direction.FORWARDS);
            if (animation.getOutput() == 0) {
                animation.reset();
                animation.setDirection(Direction.FORWARDS);
            }
        }

        float animationValue = (float) animation.getOutput();
        float timeSinceChange = (System.currentTimeMillis() - lastStateChangeTime) / 1000.0f;
        float scaleFactor = 1.0f;

        if (!isChatOpen && shouldRender && timeSinceChange < 0.3f && !lastShouldRender) {
            scaleFactor = timeSinceChange < 0.15f
                    ? 1.0f + 0.05f * (timeSinceChange / 0.15f)
                    : 1.05f - 0.05f * ((timeSinceChange - 0.15f) / 0.15f);
        } else if (!isChatOpen && !shouldRender && animationValue > 0) {
            scaleFactor = 1.0f - 0.05f * (1.0f - animationValue);
        }

        float maxWidth = 80f;
        height = 18;
        yOff = 0;

        for (Staff f : staffPlayers) {
            String staffName = f.getName();
            float nameWidth = Fonts.tenacity.getWidth(staffName, 8);
            String timeText = formatTimeOnServer(System.currentTimeMillis() - f.getJoinTime());
            float timeWidth = Fonts.tenacity.getWidth(timeText, 7);
            float localWidth = 10 + 5 + nameWidth + timeWidth + 5;

            if (localWidth > maxWidth - 10) {
                maxWidth = localWidth + 10;
            }

            height += 12.5f;
        }

        float scaledWidth = maxWidth * animationValue * scaleFactor;
        float scaledHeight = height * animationValue * scaleFactor;
        float offsetX = x + (maxWidth - scaledWidth) / 2;
        float offsetY = y + (height - scaledHeight) / 2;

        DisplayUtils.drawRoundedRect(offsetX, offsetY, scaledWidth, scaledHeight, 4 * animationValue * scaleFactor,
                ColorUtils.rgba(0, 0, 0, (int) (200 * animationValue)));

        float scaledImageSize = 10 * animationValue * scaleFactor;

        Fonts.tenacity.drawText(matrix, name, offsetX + 19.5f * animationValue * scaleFactor, offsetY + 4f * animationValue * scaleFactor,
                ColorUtils.setAlpha(-1, (int) (255 * animationValue)), 9 * animationValue * scaleFactor);
        Fonts.nur.drawText(matrix, "E", offsetX + 6.5f * animationValue * scaleFactor, offsetY + 5.5f * animationValue * scaleFactor,
                ColorUtils.setAlpha(ColorUtils.rgb(166, 26, 17), (int) (255 * animationValue)), 8.5f * animationValue * scaleFactor);

        yOff = 0;

        for (Staff f : staffPlayers) {
            String staffName = f.getName();
            float nameWidth = Fonts.tenacity.getWidth(staffName, 8);
            String timeText = formatTimeOnServer(System.currentTimeMillis() - f.getJoinTime());
            float timeWidth = Fonts.tenacity.getWidth(timeText, 7);

            if (f.getPlayerInfo() != null && !f.isSpec()) {
                ResourceLocation skin = f.getPlayerInfo().getLocationSkin();

                float headX = offsetX + 3 * animationValue * scaleFactor;
                float headY = offsetY + 19.5f * animationValue * scaleFactor + yOff;
                float headSize = scaledImageSize;
                Stencil.initStencilToWrite();
                DisplayUtils.drawRoundedRect(headX, headY, headSize, headSize, headSize * 0.2f, ColorUtils.rgba(255, 255, 255, 255));
                Stencil.readStencilBuffer(1);
                mc.getTextureManager().bindTexture(skin);
                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                com.mojang.blaze3d.systems.RenderSystem.color4f(1.0f, 1.0f, 1.0f, animationValue);
                AbstractGui.drawScaledCustomSizeModalRect(
                        (int) headX, (int) headY,
                        8.0F, 8.0F,
                        8.0F, 8.0F,
                        (int) headSize, (int) headSize,
                        64.0F, 64.0F
                );

                com.mojang.blaze3d.systems.RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
                com.mojang.blaze3d.systems.RenderSystem.disableBlend();

                Stencil.uninitStencilBuffer();
            }

            float textX = offsetX + 15 * animationValue * scaleFactor;
            Fonts.tenacity.drawText(matrix, staffName, textX, offsetY + 20.5f * animationValue * scaleFactor + yOff,
                    ColorUtils.setAlpha(-1, (int) (255 * animationValue)), 8 * animationValue * scaleFactor);

            Fonts.tenacity.drawText(matrix, timeText, offsetX + scaledWidth - timeWidth - 3 * animationValue * scaleFactor,
                    offsetY + 21f * animationValue * scaleFactor + yOff,
                    ColorUtils.setAlpha(ColorUtils.rgb(166, 26, 17), (int) (255 * animationValue)), 7 * animationValue * scaleFactor);

            yOff += 12.5f;
        }

        width = Math.max(maxWidth, 80);
        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    private String formatTimeOnServer(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @AllArgsConstructor
    @Data
    public static class Staff {
        ITextComponent prefix;
        String name;
        boolean isSpec;
        Status status;
        long joinTime;
        NetworkPlayerInfo playerInfo;

        public void updateStatus() {
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    if (info.getGameType() == GameType.SPECTATOR) {
                        return;
                    }
                    status = Status.NONE;
                    return;
                }
            }
            status = Status.VANISHED;
        }
    }

    public enum Status {
        NONE("", ColorUtils.rgb(254, 68, 68)),
        VANISHED("V", ColorUtils.rgb(0, 255, 38));
        public final String string;
        public final int color;

        Status(String string, int color) {
            this.string = string;
            this.color = color;
        }
    }
}