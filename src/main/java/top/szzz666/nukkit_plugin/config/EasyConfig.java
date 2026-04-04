package top.szzz666.nukkit_plugin.config;

import org.yaml.snakeyaml.Yaml;
import top.szzz666.nukkit_plugin.Main;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EasyConfig {
    private final String configFilePath;
    private final Map<String, Object> defaults = new LinkedHashMap<>();
    private final Map<String, String> comments = new LinkedHashMap<>();
    private final Map<String, Field> fieldMap = new HashMap<>();
    private Map<String, Object> config = new LinkedHashMap<>();

    public EasyConfig(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    /**
     * 添加配置项及其默认值
     *
     * @param key          配置项名称
     * @param defaultValue 默认值
     */
    public void add(String key, Object defaultValue) {
        defaults.put(key, defaultValue);
    }

    /**
     * 添加配置项及其默认值和注释
     *
     * @param key          配置项名称
     * @param defaultValue 默认值
     * @param comment      注释内容
     */
    public void add(String key, Object defaultValue, String comment) {
        defaults.put(key, defaultValue);
        if (comment != null && !comment.isEmpty()) {
            comments.put(key, comment);
        }
    }

    /**
     * 从配置类加载配置项（支持 @ConfigItem 注解）
     *
     * @param configClass 配置类
     */
    public void loadFromClass(Class<?> configClass) {
        for (Field field : configClass.getDeclaredFields()) {
            ConfigItem annotation = field.getAnnotation(ConfigItem.class);
            if (annotation != null) {
                String key = annotation.key().isEmpty() ? field.getName() : annotation.key();
                String comment = annotation.comment();
                try {
                    field.setAccessible(true);
                    Object defaultValue = field.get(null);
                    add(key, defaultValue, comment);
                    fieldMap.put(key, field);
                } catch (IllegalAccessException e) {
                    Main.logger.error("加载配置项失败: " + key, e);
                }
            }
        }
    }

    /**
     * 将配置值回写到配置类的静态字段
     */
    public void syncToFields() {
        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            String key = entry.getKey();
            Field field = entry.getValue();
            try {
                Object value = get(key);
                field.set(null, value);
            } catch (IllegalAccessException e) {
                Main.logger.error("同步配置项到字段失败: " + key, e);
            }
        }
    }

    /**
     * 获取配置项的值，支持嵌套键（如 database.jdbcUrl）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Object value = getNestedValue(config, key);
        if (value != null) {
            return (T) value;
        }
        return (T) getNestedValue(defaults, key);
    }

    /**
     * 从嵌套 Map 中根据点号分隔的键获取值
     */
    private Object getNestedValue(Map<String, Object> map, String key) {
        String[] parts = key.split("\\.", 2);
        Object current = map.get(parts[0]);
        if (parts.length == 1) {
            return current;
        }
        if (current instanceof Map) {
            return getNestedValue((Map<String, Object>) current, parts[1]);
        }
        return null;
    }

    public String getString(String key) {
        Object value = get(key);
        return value != null ? value.toString() : null;
    }

    public int getInt(String key) {
        Object value = get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        return ((Number) value).intValue();
    }

    public boolean getBoolean(String key) {
        Object value = get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public double getDouble(String key) {
        Object value = get(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        return ((Number) value).doubleValue();
    }

    public <K, V> Map<K, V> getMap(String key) {
        return get(key);
    }

    public <T> List<T> getList(String key) {
        return get(key);
    }

    public void set(String key, Object value) {
        config.put(key, value);
    }

    public void load() {
        Yaml yaml = new Yaml();
        File configFile = new File(configFilePath);

        try {
            if (!configFile.exists()) {
                config = buildNestedMap(defaults);
                save();
                syncToFields();
                return;
            }

            try (InputStream inputStream = new FileInputStream(configFile)) {
                Map<String, Object> loadedConfig = yaml.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                if (loadedConfig != null) {
                    config = loadedConfig;
                } else {
                    config = new HashMap<>();
                }
            }

            boolean modified = false;
            for (String defaultKey : defaults.keySet()) {
                if (getNestedValue(config, defaultKey) == null) {
                    setNestedValue(config, defaultKey, defaults.get(defaultKey));
                    modified = true;
                }
            }

            if (modified) {
                save();
            }
            syncToFields();
        } catch (IOException e) {
            Main.logger.error("加载配置文件失败: " + configFilePath, e);
        }
    }

    /**
     * 将扁平的带点号的键转换为嵌套 Map
     */
    private Map<String, Object> buildNestedMap(Map<String, Object> flatMap) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : flatMap.entrySet()) {
            setNestedValue(result, entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 在嵌套 Map 中设置值
     */
    private void setNestedValue(Map<String, Object> map, String key, Object value) {
        String[] parts = key.split("\\.", 2);
        if (parts.length == 1) {
            map.put(parts[0], value);
        } else {
            map.computeIfAbsent(parts[0], k -> new LinkedHashMap<>());
            Object nested = map.get(parts[0]);
            if (nested instanceof Map) {
                setNestedValue((Map<String, Object>) nested, parts[1], value);
            }
        }
    }

    /**
     * 保存配置文件（带注释支持）
     */
    public void save() {
        try {
            File configFile = new File(configFilePath);
            File parentDir = configFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
                writeConfigWithComments(writer);
            }
        } catch (IOException e) {
            Main.logger.error("保存配置文件失败: " + configFilePath, e);
        }
    }

    /**
     * 写入带注释的配置内容（支持嵌套结构）
     */
    private void writeConfigWithComments(Writer writer) throws IOException {
        writeNestedMap(writer, config, "", new StringBuilder(), comments);
    }

    /**
     * 递归写入嵌套 Map，通过 parentPath 精确追踪嵌套层级以匹配注释
     */
    private void writeNestedMap(Writer writer, Map<String, Object> map, String indent,
                                StringBuilder parentPath, Map<String, String> allComments) throws IOException {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String fullKey = buildFullKey(parentPath, key);

            // 根据完整键路径精确匹配注释
            String comment = allComments.get(fullKey);
            if (comment != null && !comment.isEmpty()) {
                for (String line : comment.split("\n")) {
                    writer.write(indent + "# " + line + "\n");
                }
            }

            if (value instanceof Map) {
                writer.write(indent + key + ":\n");
                writeNestedMap(writer, (Map<String, Object>) value, indent + "  ",
                        new StringBuilder(fullKey).append('.'), allComments);
            } else if (value instanceof List) {
                writer.write(indent + key + ":");
                for (Object item : (List<?>) value) {
                    writer.write("\n" + indent + "  - " + toYamlValue(item));
                }
                writer.write("\n");
            } else {
                writer.write(indent + key + ": " + toYamlValue(value) + "\n");
            }
        }
    }

    /**
     * 构建完整的嵌套键路径（如 database.jdbcUrl）
     */
    private String buildFullKey(StringBuilder parentPath, String currentKey) {
        return parentPath.isEmpty() ? currentKey : parentPath + currentKey;
    }

    /**
     * 将值转换为 YAML 格式字符串
     */
    private String toYamlValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String str) {
            // 如果字符串包含特殊字符，加引号
            if (str.contains(":") || str.contains("#") || str.contains("\n") || str.isEmpty()) {
                return "\"" + str.replace("\"", "\\\"") + "\"";
            }
            return str;
        } else if (value instanceof List) {
            StringBuilder sb = new StringBuilder("\n");
            for (Object item : (List<?>) value) {
                sb.append("  - ").append(toYamlValue(item)).append("\n");
            }
            return sb.toString();
        } else if (value instanceof Map) {
            StringBuilder sb = new StringBuilder("\n");
            for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet()) {
                sb.append("  ").append(e.getKey()).append(": ").append(toYamlValue(e.getValue())).append("\n");
            }
            return sb.toString();
        }
        return value.toString();
    }
}