package by.algorithm.alpha.api.modules.api;

import by.algorithm.alpha.api.modules.impl.combat.*;
import by.algorithm.alpha.api.modules.impl.misc.*;
import by.algorithm.alpha.api.modules.impl.movement.*;
import by.algorithm.alpha.api.modules.impl.player.*;
import by.algorithm.alpha.api.modules.impl.render.*;
import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.system.events.EventKey;
import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class ModuleReg {
    private final List<Module> functions = new CopyOnWriteArrayList<>();


    private SwingAnimation swingAnimation;
    private HUD hud;
    private AutoGapple autoGapple;
    private AutoSprint autoSprint;
    private Velocity velocity;
    private ElytraBoost elytraBoost;
    private NoRender noRender;
    private Timer timer;
    private AutoTool autoTool;
    private xCarry xcarry;
    private ElytraHelper elytrahelper;
   // private AutoBuyUI autoBuyUI;
    private ItemSwapFix itemswapfix;
    private AutoPotion autopotion;
    private TriggerBot triggerbot;
    private BetterChat betterchat;
    private AutoDuel autoDuel;
    private NoDelay nojumpdelay;
    private NameTags nameTags;
    private ClickFriend clickfriend;
    private BowSpammer bowspammer;
    private InventoryMove inventoryMove;
  //  private AutoTransfer autoTransfer;
   // private GriefHelper griefHelper;
    private ItemCooldown itemCooldown;
    private ClickPearl clickPearl;
    private AutoSwap autoSwap;
    private AutoArmor autoArmor;
    private Hitbox hitbox;
   // private HitSound hitsound;
    private AntiPush antiPush;
    private FreeCam freeCam;
    private ChestStealer chestStealer;
    private AutoAccept autoAccept;
    private AutoRespawn autoRespawn;
    private Flight fly;
    private TargetStrafe targetStrafe;
    private ClientSounds clientSounds;
    private AutoTotem autoTotem;
    private NoSlow noSlow;
    private Arrows pointers;
    private AutoExplosion autoExplosion;
    private NoRotate noRotate;
    private AttackAura killAura;
    private AntiBot antiBot;
    private Trails trails;
    private Crosshair crosshair;
    private Strafe strafe;
    private World world;
    private ViewModel viewModel;
    private ElytraFly elytraFly;
    private ChinaHat chinaHat;
    private FireFly snow;
    private CollisionDisabler collisionDisabler;
    private SRPSpoofer srpSpoofer;
    private Particles particles;
    private TargetESP targetESP;
    private JumpCircle jumpCircle;
    private ItemPhysic itemPhysic;
    private Predictions predictions;
    private NoEntityTrace noEntityTrace;
    private ItemScroller itemScroller;
    private AutoFish autoFish;
    private StorageESP storageESP;
    private Spider spider;
    private NameProtect nameProtect;
    private ShulkerChecker shulkerChecker;
    private NoInteract noInteract;
    private GlassHand glassHand;
    private Tracers tracers;
    private SelfDestruct selfDestruct;
    private AutoJoin autoJoin;
    private LeaveTracker leaveTracker;
    private AntiAFK antiAFK;
    private Jesus jesus;
    private KTLeave ktLeave;
    private BetterMinecraft betterMinecraft;
    private Backtrack backtrack;
    private SeeInvisibles seeInvisibles;
    private FTHelper fTHelper;
    private CustomModels customModels;

    public void init() {
        registerAll(
                hud = new HUD(),
                customModels = new CustomModels(),
                fTHelper = new FTHelper(),
                autoGapple = new AutoGapple(),
                autoSprint = new AutoSprint(),
                velocity = new Velocity(),
                noRender = new NoRender(),
                autoTool = new AutoTool(),
                xcarry = new xCarry(),
                seeInvisibles = new SeeInvisibles(),
                elytrahelper = new ElytraHelper(),
                itemswapfix = new ItemSwapFix(),
                autopotion = new AutoPotion(),
                triggerbot = new TriggerBot(),
                nojumpdelay = new NoDelay(),
                clickfriend = new ClickFriend(),
                bowspammer = new BowSpammer(),
                collisionDisabler = new CollisionDisabler(),
                inventoryMove = new InventoryMove(),
             //   autoTransfer = new AutoTransfer(),
         //       griefHelper = new GriefHelper(),
                autoArmor = new AutoArmor(),
                elytraBoost = new ElytraBoost(),
                hitbox = new Hitbox(),
                autoDuel = new AutoDuel(),
                //hitsound = new HitSound(),
                antiPush = new AntiPush(),
            //    autoBuyUI = new AutoBuyUI(),
                freeCam = new FreeCam(),
                nameTags = new NameTags(),
                shulkerChecker = new ShulkerChecker(),
                autoJoin = new AutoJoin(),
                betterchat = new BetterChat(),
                srpSpoofer = new SRPSpoofer(),
                chestStealer = new ChestStealer(),
                ktLeave = new KTLeave(),
                autoAccept = new AutoAccept(),
                autoRespawn = new AutoRespawn(),
                fly = new Flight(),
                clientSounds = new ClientSounds(),
                noSlow = new NoSlow(),
                pointers = new Arrows(),
                autoExplosion = new AutoExplosion(),
                noRotate = new NoRotate(),
                antiBot = new AntiBot(),
                trails = new Trails(),
                crosshair = new Crosshair(),
                autoTotem = new AutoTotem(),
                itemCooldown = new ItemCooldown(),
                killAura = new AttackAura(),
                clickPearl = new ClickPearl(itemCooldown),
                jesus = new Jesus(),
                autoSwap = new AutoSwap(autoTotem),
                targetStrafe = new TargetStrafe(killAura),
                strafe = new Strafe(targetStrafe, killAura),
                swingAnimation = new SwingAnimation(killAura),
                targetESP = new TargetESP(killAura),
                world = new World(),
                viewModel = new ViewModel(),
                elytraFly = new ElytraFly(),
                chinaHat = new ChinaHat(),
                snow = new FireFly(),
                particles = new Particles(),
                jumpCircle = new JumpCircle(),
                itemPhysic = new ItemPhysic(),
                predictions = new Predictions(),
                noEntityTrace = new NoEntityTrace(),
                itemScroller = new ItemScroller(),
                autoFish = new AutoFish(),
                storageESP = new StorageESP(),
                spider = new Spider(),
                timer = new Timer(),
                nameProtect = new NameProtect(),
                noInteract = new NoInteract(),
                glassHand = new GlassHand(),
                tracers = new Tracers(),
                selfDestruct = new SelfDestruct(),
                leaveTracker = new LeaveTracker(),
                antiAFK = new AntiAFK(),
                betterMinecraft = new BetterMinecraft(),
                backtrack = new Backtrack(),
                new Parkour(),
                new RWHelper(),
                new HWHelper(), new Speed(), new FastShulkerBreak(), new TPLoot(), new Criticals(), new ElytraMotion(), new AirStuck(), new TridentFly(), new Spin(), new AutoMessage(),
                new HpAlert(), new Blink(), new SpiderFt(), new AutoCraft(), new AutoRegister(),  new GlowESP(), new AutoLoot(),new AirJump(), new AnarchyHelper(), new ElytraSpeed()
                ,new GodMode(), new ExpBottleFill()

        );

        Initclass.getInstance().getEventBus().register(this);
    }

    private void registerAll(Module... Functions) {
        Arrays.sort(Functions, Comparator.comparing(Module::getName));

        functions.addAll(List.of(Functions));
    }


    @Subscribe
    private void onKey(EventKey e) {
        if (selfDestruct.unhooked) return;
        for (Module Function : functions) {
            if (Function.getBind() == e.getKey()) {
                Function.toggle();
            }
        }
    }
}