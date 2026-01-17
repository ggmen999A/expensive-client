package by.algorithm.alpha.system.utils.render.font.font;

public class Fonts {

    public static Font montserrat, consolas, icons, damage, sfui, sfbold, sfMedium, nur, icos, tenacity;

    public static void register() {
        montserrat = new Font("Montserrat-Regular.ttf.png", "Montserrat-Regular.ttf.json");
        icons = new Font("icons.ttf.png", "icons.ttf.json");
        consolas = new Font("consolas.ttf.png", "consolas.ttf.json");
        damage = new Font("damage.ttf.png", "damage.ttf.json");
        sfui = new Font("sf_semibold.ttf.png", "sf_semibold.ttf.json");
        sfbold = new Font("sf_bold.ttf.png", "sf_bold.ttf.json");
        sfMedium = new Font("sf_medium.ttf.png", "sf_medium.ttf.json");
        nur = new Font("nur.png", "nur.json");
        icos = new Font("icos.png", "icos.json");
        tenacity = new Font("tenacity.png", "tenacity.json");
    }

}
