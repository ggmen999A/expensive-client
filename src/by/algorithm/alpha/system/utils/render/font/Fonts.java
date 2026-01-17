package by.algorithm.alpha.system.utils.render.font;

import by.algorithm.alpha.system.utils.render.font.common.Lang;
import by.algorithm.alpha.system.utils.render.font.styled.StyledFont;
import lombok.SneakyThrows;
import net.minecraft.util.ResourceLocation;

public class Fonts {
    public static final String FONT_DIR = "/assets/minecraft/expensive/font/";

    private static final ResourceLocation SMALL_FONT = new ResourceLocation("expensive/font/small.ttf");
    private static final ResourceLocation MC_FONT = new ResourceLocation("expensive/font/mc.ttf");
    private static final ResourceLocation PENUS_FONT = new ResourceLocation("expensive/font/penus.ttf");
    private static final ResourceLocation WOVELINE_FONT = new ResourceLocation("expensive/font/woveline.otf");
    private static final ResourceLocation ICONS_FONT = new ResourceLocation("expensive/font/icons.ttf");
    private static final ResourceLocation VERDANA_FONT = new ResourceLocation("expensive/font/verdana.ttf");
    private static final ResourceLocation SORA_FONT = new ResourceLocation("expensive/font/sora.ttf");
    private static final ResourceLocation GLYPHTER_FONT = new ResourceLocation("expensive/font/Glyphter.ttf");
    private static final ResourceLocation NUNITO_BOLD_FONT = new ResourceLocation("expensive/font/nunito-bold.ttf");
    private static final ResourceLocation NUNITO_LIGHT_FONT = new ResourceLocation("expensive/font/nunito-light.ttf");
    private static final ResourceLocation GILROY_BOLD_FONT = new ResourceLocation("expensive/font/gilroy-bold.ttf");
    private static final ResourceLocation GILROY_FONT = new ResourceLocation("expensive/font/gilroy.ttf");
    private static final ResourceLocation MONTSERRAT_BOLD_FONT = new ResourceLocation("expensive/font/Montserrat-Bold.ttf");
    private static final ResourceLocation MONTSERRAT_LIGHT_FONT = new ResourceLocation("expensive/font/Montserrat-Light.ttf");
    private static final ResourceLocation MONTSERRAT_MEDIUM_FONT = new ResourceLocation("expensive/font/Montserrat-Medium.ttf");
    private static final ResourceLocation MONTSERRAT_REGULAR_FONT = new ResourceLocation("expensive/font/Montserrat-Regular.ttf");
    private static final ResourceLocation MONTSERRAT_SEMIBOLD_FONT = new ResourceLocation("expensive/font/Montserrat-SemiBold.ttf");
    private static final ResourceLocation INTER_FONT = new ResourceLocation("expensive/font/inter.ttf");

    public static volatile StyledFont[] minecraft = new StyledFont[24];
    public static volatile StyledFont[] verdana = new StyledFont[24];
    public static volatile StyledFont[] gilroyBold = new StyledFont[24];
    public static volatile StyledFont[] msBold = new StyledFont[24];
    public static volatile StyledFont[] msMedium = new StyledFont[24];
    public static volatile StyledFont[] msLight = new StyledFont[24];
    public static volatile StyledFont[] msRegular = new StyledFont[24];
    public static volatile StyledFont[] msSemiBold = new StyledFont[24];

    public static volatile StyledFont[] gilroy = new StyledFont[24];
    public static volatile StyledFont[] sora = new StyledFont[24];
    public static volatile StyledFont[] woveline = new StyledFont[24];
    public static volatile StyledFont[] nunitoBold = new StyledFont[131];
    public static volatile StyledFont[] nunitoLight = new StyledFont[48];
    public static volatile StyledFont[] icons = new StyledFont[24];
    public static volatile StyledFont[] configIcon = new StyledFont[24];
    public static volatile StyledFont[] inter = new StyledFont[24];

    public static volatile StyledFont[] icons1 = new StyledFont[131];
    public static volatile StyledFont[] small = new StyledFont[23];

    @SneakyThrows
    public static void init() {
        long time = System.currentTimeMillis();

        small[22] = new StyledFont(SMALL_FONT, 22, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        minecraft[8] = new StyledFont(MC_FONT, 8, 0.0f, 0.0f, 0.0f, false, Lang.ENG_RU);
        icons[16] = new StyledFont(PENUS_FONT, 16, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        icons[12] = new StyledFont(PENUS_FONT, 12, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        woveline[19] = new StyledFont(WOVELINE_FONT, 19, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        icons1[130] = new StyledFont(ICONS_FONT, 130, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);

        for (int i = 8; i < 24; i++) {
            icons1[i] = new StyledFont(ICONS_FONT, i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 8; i < 16; i++) {
            verdana[i] = new StyledFont(VERDANA_FONT, i, 0.0f, 0.0f, 0.0f, false, Lang.ENG_RU);
        }

        for (int i = 10; i < 23; i++) {
            sora[i] = new StyledFont(SORA_FONT, i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 10; i < 23; i++) {
            configIcon[i] = new StyledFont(GLYPHTER_FONT, i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 8; i < 48; i++) {
            nunitoLight[i] = new StyledFont(NUNITO_LIGHT_FONT, i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 8; i < 131; i++) {
            nunitoBold[i] = new StyledFont(NUNITO_BOLD_FONT, i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 10; i < 23; i++) {
            gilroyBold[i] = new StyledFont(GILROY_BOLD_FONT, i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 8; i < 24; i++) {
            gilroy[i] = new StyledFont(GILROY_FONT, i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 8; i < 24; i++) {
            msBold[i] = new StyledFont(MONTSERRAT_BOLD_FONT, i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 8; i < 24; i++) {
            msLight[i] = new StyledFont(MONTSERRAT_LIGHT_FONT, i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 8; i < 24; i++) {
            msMedium[i] = new StyledFont(MONTSERRAT_MEDIUM_FONT, i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 8; i < 24; i++) {
            msRegular[i] = new StyledFont(MONTSERRAT_REGULAR_FONT, i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 8; i < 24; i++) {
            msSemiBold[i] = new StyledFont(MONTSERRAT_SEMIBOLD_FONT, i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        for (int i = 8; i < 24; i++) {
            inter[i] = new StyledFont(INTER_FONT, i, 0.0f, 0.0f, 0.0f, true, Lang.ENG_RU);
        }

        System.out.println("Шрифты инициализированы за " + (System.currentTimeMillis() - time) + " миллисекунд!");
    }
}