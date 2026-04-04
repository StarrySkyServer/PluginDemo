package top.szzz666.nukkit_plugin.config;


import static top.szzz666.nukkit_plugin.Main.ConfigPath;
import static top.szzz666.nukkit_plugin.Main.ec;

public class MyConfig {

    @ConfigItem(key = "command", comment = "命令")
    public static String command = "test";

    @ConfigItem(key = "database.jdbcUrl", comment = "数据库连接地址")
    public static String database_jdbcUrl = "jdbc:mysql://localhost:3306/test1?useSSL=false&characterEncoding=utf8&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    @ConfigItem(key = "database.username", comment = "数据库用户名")
    public static String database_username = "test1";

    @ConfigItem(key = "database.password", comment = "数据库密码")
    public static String database_password = "78rDAFBjtjc2HdWK";

    public static void initConfig() {
        ec = new EasyConfig(ConfigPath + "/config.yml");
        ec.loadFromClass(MyConfig.class);
        ec.load();
    }

}
