package by.algorithm.alpha;

import by.algorithm.alpha.api.command.*;
import by.algorithm.alpha.api.command.impl.*;
import by.algorithm.alpha.api.command.impl.feature.*;
import by.algorithm.alpha.api.modules.notifications.Notifications;
import by.algorithm.alpha.system.discordrpc.DiscordRPC;
import by.algorithm.alpha.system.utils.render.font.Fonts;
import by.algorithm.alpha.system.visuals.mainmenu.altmanager.AltScreen;
import com.google.common.eventbus.EventBus;
import by.algorithm.alpha.api.command.friends.FriendStorage;
import by.algorithm.alpha.api.command.staffs.StaffStorage;
import by.algorithm.alpha.api.config.ConfigStorage;
import by.algorithm.alpha.api.command.macro.MacroManager;
import by.algorithm.alpha.system.events.EventKey;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleReg;
import by.algorithm.alpha.api.modules.notifications.NotificationManager;
import by.algorithm.alpha.system.scripts.client.ScriptManager;
import by.algorithm.alpha.system.visuals.ab.factory.ItemFactory;
import by.algorithm.alpha.system.visuals.ab.factory.ItemFactoryImpl;
import by.algorithm.alpha.system.visuals.ab.logic.ActivationLogic;
import by.algorithm.alpha.system.visuals.ab.model.IItem;
import by.algorithm.alpha.system.visuals.ab.model.ItemStorage;
import by.algorithm.alpha.system.visuals.ab.render.Window;
import by.algorithm.alpha.system.visuals.autobuy.AutoBuyConfig;
import by.algorithm.alpha.system.visuals.autobuy.AutoBuyHandler;
import by.algorithm.alpha.system.visuals.gui.GUI;
import by.algorithm.alpha.system.visuals.mainmenu.altmanager.AltConfig;
import by.algorithm.alpha.system.visuals.styles.Style;
import by.algorithm.alpha.system.visuals.styles.StyleFactory;
import by.algorithm.alpha.system.visuals.styles.StyleFactoryImpl;
import by.algorithm.alpha.system.visuals.styles.StyleManager;
import by.algorithm.alpha.system.utils.other.TPSCalc;
import by.algorithm.alpha.system.utils.client.ServerTPS;
import by.algorithm.alpha.system.utils.dragable.DragManager;
import by.algorithm.alpha.system.utils.dragable.Dragging;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import via.ViaMCP;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Я просто делаю свой клиент, я свободный человек, пасщю что хочу
 * Газ (А-а)
 * Бро, я напастил авто тотем уже в три лет
 * Не ври, что ты не пастер, это не писаный клиент
 * Деус — модель, уши размером с живот макавто
 * Ты нервничаешь, бесишь сам себя, когда сурсов нет
 * У, у, у, у, у, я больших мазиков съедатель (А-а)
 * Больших уши уничтожитель, мазикавыпиватель (А-а)
 * Пастирских читов игратель, тебя выключатель (У-у)
 * Белый, но во мне краситель, к пастингу привыкатель (Е-е)
 * Бро, это shit skidding, я им рассказыватель (отвечаю)
 * Большие бидоны мазика — силой слова я их поедатель (У)
 * У меня большой живот — я его показыватель
 * Бро реал скидет, зовёт ся falok(Fals3r)
 * Эй, чит восьми из десяти mc.player — говно, я таким сру утром
 * Четыре бегина делят дерьмо — это на двух клиентах
 * Мой любимый кодер стал пастером — уже не авто тотем макслоло
 * Мазик рушит стены моего желудка
 * Она долго запускаться, как будто лоудер говно
 * Кодеры купаются в мазике, бля, они срут в коде
 * У меня есть шкильники качки, да, они жрут мазик
 * Они сразу поломают кисть, они не жмут руку
 * Ты пастишь реже ,чем dedinside — тя там не видно (Ха)
 * Я сейчас official, иногда ворую с клиентов (У-у)
 * Бро откинулся в Актобе — щас он летит к нам (У-у)
 * Кто этот ебаный Пастер, чё он нам пиздит там? (У-у)
 * Я могу потыкать ему этим Кодом прям в бошку (Гр-ра)
 * Подлетай к моему аирДропу — я залутаю его (Ха, макавто)
 * Я могу пастить даже спиной к монику, ты не сможешь так (Не сможешь)
 * Пока мне делали glow, у тебя пастили под носом, е, а-а
 * [Аутро]
 * У, у, у, у, у, я больших мазиков съедатель
 * Больших уши уничтожитель, мазикавыпиватель
 * Пастирских читов игратель, тебя выключатель
 * Давай спастим wintware, короче
 */

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Initclass {

    public static UserData userData;
    public boolean playerOnServer = false;
    public static final String CLIENT_NAME = "expensive solutions";

    // Экземпляр Expensive
    @Getter
    private static Initclass instance;

    private final Map<String, Dragging> draggableElements = new HashMap<>();

    // Менеджеры
    private ModuleReg functionRegistry;
    private ConfigStorage configStorage;
    private CommandDispatcher commandDispatcher;
    private ServerTPS serverTPS;
    private MacroManager macroManager;
    private Fonts fonts;
    private StyleManager styleManager;

    // Менеджер событий и скриптов
    private final EventBus eventBus = new EventBus();
    private final ScriptManager scriptManager = new ScriptManager();

    // Директории
    private final File clientDir = new File(Minecraft.getInstance().gameDir + "\\expensive");
    private final File filesDir = new File(Minecraft.getInstance().gameDir + "\\expensive\\files");

    // Элементы интерфейса
    private AltScreen altScreen;
    private AltConfig altConfig;
    private GUI dropDown;
    private Window autoBuyUI;


    // Конфигурация и обработчики
    private AutoBuyConfig autoBuyConfig = new AutoBuyConfig();
    private AutoBuyHandler autoBuyHandler;
    private ViaMCP viaMCP;
    private TPSCalc tpsCalc;
    private ActivationLogic activationLogic;
    private ItemStorage itemStorage;
    public NotificationManager notificationManager;

    public Initclass() {
        instance = this;

        if (!clientDir.exists()) {
            clientDir.mkdirs();
        }
        if (!filesDir.exists()) {
            filesDir.mkdirs();
        }



        clientLoad();
        FriendStorage.load();
        StaffStorage.load();
    }



    public Dragging createDrag(Module module, String name, float x, float y) {
        DragManager.draggables.put(name, new Dragging(module, name, x, y));
        return DragManager.draggables.get(name);
    }

    private void clientLoad() {
        viaMCP = new ViaMCP();
        serverTPS = new ServerTPS();
        functionRegistry = new ModuleReg();
        macroManager = new MacroManager();
        configStorage = new ConfigStorage();
        functionRegistry.init();
        fonts.init();
        initCommands();
        initStyles();
        altScreen = new AltScreen();
        altConfig = new AltConfig();
        tpsCalc = new TPSCalc();
        notificationManager = new NotificationManager();
        DiscordRPC.startDiscord();


        try {
            autoBuyConfig.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            altConfig.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            configStorage.init();
        } catch (IOException e) {
            System.out.println("Ошибка при подгрузке конфига.");
        }
        try {
            macroManager.init();
        } catch (IOException e) {
            System.out.println("Ошибка при подгрузке конфига макросов.");
        }
        DragManager.load();
        dropDown = new GUI(new StringTextComponent(""));
        initAutoBuy();
        autoBuyUI = new Window(new StringTextComponent(""), itemStorage);
        //autoBuyUI = new AutoBuyUI(new StringTextComponent("A"));
        autoBuyHandler = new AutoBuyHandler();
        autoBuyConfig = new AutoBuyConfig();

        eventBus.register(this);
        Notifications.NOTIFICATION_MANAGER = new NotificationManager();
    }

    private final EventKey eventKey = new EventKey(-1);

    public void onKeyPressed(int key) {
        eventKey.setKey(key);
        eventBus.post(eventKey);

        macroManager.onKeyPressed(key);

        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            Minecraft.getInstance().displayGuiScreen(dropDown);
        }


    }

    public Dragging getDraggingById(String id) {
        return draggableElements.get(id);
    }
    public Map<String, Dragging> getDraggableElements() {
        return draggableElements;
    }



    private void initAutoBuy() {
        ItemFactory itemFactory = new ItemFactoryImpl();
        CopyOnWriteArrayList<IItem> items = new CopyOnWriteArrayList<>();
        itemStorage = new ItemStorage(items, itemFactory);

        activationLogic = new ActivationLogic(itemStorage, eventBus);
    }

    private void initCommands() {
        Minecraft mc = Minecraft.getInstance();
        Logger logger = new MultiLogger(List.of(new ConsoleLogger(), new MinecraftLogger()));
        List<Command> commands = new ArrayList<>();
        Prefix prefix = new PrefixImpl();
        commands.add(new ListCommand(commands, logger));
        commands.add(new FriendCommand(prefix, logger, mc));
        commands.add(new BindCommand(prefix, logger));
        commands.add(new GPSCommand(prefix, logger));
        commands.add(new ConfigCommand(configStorage, prefix, logger));
        commands.add(new MacroCommand(macroManager, prefix, logger));
        commands.add(new VClipCommand(prefix, logger, mc));
        commands.add(new HClipCommand(prefix, logger, mc));
        commands.add(new StaffCommand(prefix, logger));
        commands.add(new MemoryCommand(logger));
        commands.add(new RCTCommand(logger, mc));

        AdviceCommandFactory adviceCommandFactory = new AdviceCommandFactoryImpl(logger);
        ParametersFactory parametersFactory = new ParametersFactoryImpl();

        commandDispatcher = new StandaloneCommandDispatcher(commands, adviceCommandFactory, prefix, parametersFactory, logger);
    }

    public void initStyles() {
        StyleFactory factory = new StyleFactoryImpl();
        List<Style> styles = new ArrayList<>();

        styles.add(factory.createStyle("Красный Градиент",
                new Color(255, 50, 50),
                new Color(180, 0, 0)));

        styles.add(factory.createStyle("Алый Закат",
                new Color(255, 69, 0),
                new Color(139, 0, 0)));

        styles.add(factory.createStyle("Кровавый Рубин",
                new Color(220, 20, 60),
                new Color(128, 0, 0)));

        styles.add(factory.createStyle("Огненная Страсть",
                new Color(255, 20, 20),
                new Color(160, 0, 0)));

        styleManager = new StyleManager(styles, styles.get(0));
    }


    public String randomNickname() {
        String[] names = new String[]{
                "VortexShadow", "NightHawk", "ZeroChaser", "HexaX", "XenoPower", "AeroSurge", "FirePulse",
                "NovaBlade", "ElectricFury", "DarkPhoenix", "WiredForce", "CyberViper", "TechPanda", "AquaRush",
                "NeonScythe", "QuantumWanderer", "SilentPulse", "BlazeCore", "ThunderStrike", "MysticTide",
                "SilverHaze", "GlitchTrap", "PhantomVibe", "RogueKnight", "ShadowTornado", "FlameKing", "QuantumRogue",
                "MysticSlayer", "FuturePrime", "CyberKnight", "DarkMatterX", "IronWolf", "NightCrawler", "PixelHunter",
                "GhostReaper", "PhantomDusk", "NeonBlaze", "HyperNova", "RedViper", "ThunderClash", "SkyRider",
                "PixelKing", "NebulaCore", "VortexCore", "ZeroReaper", "DarkVanguard", "SkyMaster", "VoltKing",
                "CursedSoul", "TechnoDragon", "GhostFury", "DarkByte", "SonicShifter", "NeonSniper", "ExoPhoenix",
                "BlazeRider", "StormRider", "QuantumRider", "CyberSpecter", "RedFalcon", "MegaInferno", "LaserBeast",
                "NightWhisper", "VortexHunter", "StellarShade", "HexaCore", "DriftPulse", "ShadowPhantom",
                "ZeroHunter", "BlazeWarden", "DarkRogue", "StormPunk", "EchoFury", "PhantomVanguard", "MysticVolt",
                "ViperHunter", "RogueHunter", "CyberSpectre", "BladeKnight", "FalconRider", "PixelSpecter",
                "StellarShadow", "ShadowPursuer", "VortexWarden", "ThunderGhost", "EclipseKnight", "HexaShifter",
                "SonicWraith", "CyberRanger", "ShatteredSoul", "DarkEcho", "VoltKnight", "VortexWraith", "RogueVibe",
                "NovaWarden", "SteelSpecter", "HexaViper", "FlameReaper", "NightTide", "DriftRider", "SonicPhantom",
                "BlazeWraith", "ShadowStrike", "IronSpecter", "EchoRider", "VortexTide", "HyperSpecter", "StealthHawk",
                "RedReaper", "CrimsonVibe", "WraithPursuer", "EclipseRider", "SilverHunter", "VoltReaper",
                "ShadowFlare", "FlameSpecter", "IronVanguard", "CrimsonKnight", "SilverRogue", "TechnoBlaze"
        };
        String[] titles = new String[]{"DADA", "YA", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "SUS", "SSS", "TAM", "TyT", "TaM", "Ok", "Pon", "LoL", "CHO", "4oo", "MaM", "Top", "PvP", "PVH", "DIK", "KAK", "SUN", "SIN", "COS", "FIT", "FAT", "HA", "AHH", "OHH", "UwU", "DA", "NaN", "RAP", "WoW", "SHO", "KA4", "Ka4", "AgA", "Fov", "LoVe", "TAN", "Mia", "Alt", "4el", "bot", "GlO", "Sir", "IO", "EX", "Mac", "Win", "Lin", "AC", "Bro", "6po", "6PO", "BRO", "mXn", "XiL", "TGN", "24", "228", "1337", "1488", "007", "001", "999", "333", "666", "111", "FBI", "FBR", "FuN", "FUN", "UFO", "OLD", "Old", "New", "OFF", "ON", "YES", "LIS", "NEO", "BAN", "OwO", "0_o", "0_0", "o_0", "IQ", "99K", "AK47", "SOS", "S0S", "SoS", "z0z", "zOz", "Zzz", "zzz", "ZZZ", "6y", "BU", "RAK", "PAK", "Pak", "MeM", "MoM", "M0M", "KAK", "TAK", "n0H", "BOSS", "RU", "ENG", "BAF", "BAD", "ZED", "oy", "Oy", "0y", "Big", "Air", "Dirt", "Dog", "CaT", "CAT", "KOT", "EYE", "CAN", "ToY", "ONE", "OIL", "HOT", "HoT", "VPN", "BnH", "Ty3", "GUN", "HZ", "XZ", "XYZ", "HZ", "XyZ", "HIS", "HER", "DOC", "COM", "DIS", "TOP", "1ST", "1st", "LORD", "DED", "ded", "HAK", "FUF", "IQQ", "KBH", "KVN", "HuH", "WWW", "RUN", "RuN", "run", "PRO", "100", "300", "3OO", "RAM", "DIR", "Yaw", "YAW", "TIP", "Tun", "Ton", "Tom", "Your", "AM", "FM", "YT", "yt", "Yt", "yT", "RUS", "KON", "FAK", "FUL", "RIL", "pul", "RW", "MST", "MEN", "MAN", "NO0", "SEX", "H2O", "H20", "LyT", "3000", "01", "KEK", "PUK", "nuk", "nyk", "nyK", "191", "192", "32O", "5OO", "320", "500", "777", "720", "480", "48O", "HUK", "BUS", "LUN", "LyH", "Fuu", "LaN", "LAN", "DIC", "HAA", "NON", "FAP", "4AK", "4on", "4EK", "4eK", "NVM", "BOG", "RIP", "SON", "XXL", "XXX", "GIT", "GAD", "8GB", "5G", "4G", "3G", "2G", "TX", "GTX", "RTX", "HOP", "TIR", "ufo", "MIR", "MAG", "ALI", "BOB", "GRO", "GOT", "ME", "SO", "Ay4", "MSK", "MCK", "RAY", "EVA", "EvA", "DEL", "ADD", "UP", "VK", "LOV", "AND", "AVG", "EGO", "YTY", "YoY", "I_I", "G_G", "D_D", "V_V", "F", "FF", "FFF", "LCM", "PCM", "CPS", "FPS", "GO", "G0", "70", "7UP", "JAZ", "GAZ", "7A3", "UFA", "HIT", "DAY", "DaY", "S00", "SCP", "FUK", "SIL", "COK", "SOK", "WAT", "WHO", "PUP", "PuP", "Py", "CPy", "SRU", "OII", "IO", "IS", "THE", "SHE", "nuc", "KXN", "VAL", "MIS", "HXI", "HI", "ByE", "WEB", "TNT", "BEE", "4CB", "III", "IVI", "POP", "C4", "BRUH", "Myp", "MyP", "NET", "CAR", "PET", "POV", "POG", "OKK", "ESP", "GOP", "G0P", "7on", "E6y", "BIT", "PIX", "AYE", "Aye", "PVP", "GAS", "REK", "rek", "PEK", "n0H", "RGB"};
        String name = names[(int)(((float)names.length - 1.0F) * (float)Math.random() * (((float)names.length - 1.0F) / (float)names.length))];
        String title = titles[(int)(((float)titles.length - 1.0F) * (float)Math.random() * (((float)titles.length - 1.0F) / (float)titles.length))];
        int size = (name + "_").length();
        return name + "_" + (16 - size == 0 ? "" : title);
    }


    @Getter
    @Setter
    @AllArgsConstructor
    public static class UserData {
        final String user;
        final int uid;
    }

}
