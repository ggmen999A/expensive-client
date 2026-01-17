package by.algorithm.alpha.system.discordrpc;

import by.algorithm.alpha.system.discordrpc.system.DiscordEventHandlers;
import by.algorithm.alpha.system.discordrpc.system.DiscordRichPresence;
import by.algorithm.alpha.system.discordrpc.system.helpers.RPCButton;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;

public class DiscordRPC {

    private static Thread rpcThread;
    private static final long lastTimeMillis = System.currentTimeMillis();
    public static String avatarUrl;
    public static String userid;
    public static BufferedImage avatar;
    public static String state;

    public static void startDiscord() {
        DiscordEventHandlers eventHandlers = new DiscordEventHandlers.Builder().ready((user) -> {
            if (user.avatar != null) {
                userid = user.username.toString();
                avatarUrl = "https://cdn.discordapp.com/avatars/" + user.userId + "/" + user.avatar;
                try {
                    URLConnection url = new URL(DiscordRPC.avatarUrl).openConnection();
                    url.setRequestProperty("User-Agent", "Mozilla/5.0");
                    avatar = ImageIO.read(url.getInputStream());
                } catch (Exception ignored) {}
            }
        }).build();
        by.algorithm.alpha.system.discordrpc.system.DiscordRPC.INSTANCE.Discord_Initialize("1373204312859217970", eventHandlers, true, "");

        rpcThread = new Thread(() -> {
            while(true) {
                by.algorithm.alpha.system.discordrpc.system.DiscordRPC.INSTANCE.Discord_RunCallbacks();
                updatePresence();

                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ignored) {}
            }

        });
        rpcThread.start();
    }

    public static void shutdownDiscord() {
        if (rpcThread != null) {
            rpcThread.interrupt();
            by.algorithm.alpha.system.discordrpc.system.DiscordRPC.INSTANCE.Discord_Shutdown();
        }
    }
    private static void updatePresence() {
        DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder();
        builder.setStartTimestamp(lastTimeMillis / 1000);
        builder.setDetails("User: algorithm");
        builder.setState("Build - Development (1.0)");
        builder.setLargeImage("chikibum");
        builder.setSmallImage("chikibum", "Solth - New Era");
        builder.setButtons(RPCButton.create("Телеграм", "https://t.me/algorithmdlc"),
                RPCButton.create("Сайт", "https://algorithmdlc.fun"));
        by.algorithm.alpha.system.discordrpc.system.DiscordRPC.INSTANCE.Discord_UpdatePresence(builder.build());
    }

}