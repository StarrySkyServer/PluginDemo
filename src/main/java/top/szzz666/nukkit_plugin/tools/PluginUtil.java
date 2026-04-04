package top.szzz666.nukkit_plugin.tools;

import cn.nukkit.command.CommandSender;
import io.leego.banana.BananaUtils;
import io.leego.banana.Font;
import lombok.SneakyThrows;
import top.szzz666.nukkit_plugin.Main;
import top.szzz666.nukkit_plugin.panel.esay_chest_menu.lib.AbstractFakeInventory;

import static top.szzz666.nukkit_plugin.Main.nkServer;
import static top.szzz666.nukkit_plugin.Main.plugin;

public class PluginUtil {
    public static void multCmd(CommandSender sender, String command) {
        nkServer.getCommandMap().dispatch(sender, command);
    }
    public static void checkServer(){
        boolean ver = false;
        //双核心兼容
        try {
            Class<?> c = Class.forName("cn.nukkit.Nukkit");
            c.getField("NUKKIT_PM1E");
            ver = true;

        } catch (ClassNotFoundException | NoSuchFieldException ignore) { }
        try {
            Class<?> c = Class.forName("cn.nukkit.Nukkit");
            "Nukkit PetteriM1 Edition".equalsIgnoreCase(c.getField("NUKKIT").get(c).toString());
            ver = true;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignore) {
        }

        AbstractFakeInventory.IS_PM1E = ver;
        if(ver){
            Main.logger.info("当前插件运行在: Nukkit MOT 核心上");
        }else{
            Main.logger.info("当前插件运行在: Nukkit 核心上");
        }
    }


    //Banana
    @SneakyThrows
    public static void pluginNameLineConsole() {
        lineConsole(BananaUtils.bananaify(plugin.getName(), Font.SMALL));
    }

    //将输入的字符串按行打印到控制台。
    public static void lineConsole(String s) {
        String[] lines = s.split("\n");
        for (String line : lines) {
            Main.logger.info(line);
        }
    }


}
