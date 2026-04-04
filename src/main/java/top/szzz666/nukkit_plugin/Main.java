package top.szzz666.nukkit_plugin;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginLogger;
import top.szzz666.nukkit_plugin.command.MyCommand;
import top.szzz666.nukkit_plugin.config.EasyConfig;
import top.szzz666.nukkit_plugin.listener.Listeners;
import top.szzz666.nukkit_plugin.panel.esay_chest_menu.CMListener;

import static top.szzz666.nukkit_plugin.config.MyConfig.database_jdbcUrl;
import static top.szzz666.nukkit_plugin.config.MyConfig.initConfig;
import static top.szzz666.nukkit_plugin.tools.PluginUtil.checkServer;



public class Main extends PluginBase {
    public static Plugin plugin;
    public static Server nkServer;
    public static PluginLogger logger;
    public static CommandSender consoleObjects;
    public static String ConfigPath;
    public static EasyConfig ec;

    //插件读取
    @Override
    public void onLoad() {
        nkServer = this.getServer();
        plugin = this;
        logger = this.getLogger();
        consoleObjects = getServer().getConsoleSender();
        ConfigPath = getDataFolder().getPath();
        initConfig();
        logger.info("&b" + plugin.getName() + "插件读取...");
    }

    //插件开启
    @Override
    public void onEnable() {
        checkServer();
        //注册监听器
        nkServer.getPluginManager().registerEvents(new Listeners(), this);
        nkServer.getPluginManager().registerEvents(new CMListener(), this);
        //注册命令
        nkServer.getCommandMap().register(this.getName(), new MyCommand());
//        pluginNameLineConsole();
        logger.info("&b" + plugin.getName() + "插件开启");
        logger.warning("&c" + plugin.getName() + "如果遇到任何bug，请加入Q群进行反馈：894279534");
    }

    //插件关闭
    @Override
    public void onDisable() {
        logger.info("&b" + plugin.getName() + "插件关闭");
    }

}
