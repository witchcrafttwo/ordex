package org;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Set;


public class PropertySettings {
    private static final String FileName = "ordex.properties";
    private static final String RULE_COUNT_KEY = "rule.count";
    File Path;

    public static class SavedRule {
        private final String watchPath;
        private final String destinationPath;
        private final ArrayList<String> keywords;
        private final ArrayList<String> extensions;

        public SavedRule(String watchPath, String destinationPath, List<String> keywords, List<String> extensions) {
            this.watchPath = watchPath;
            this.destinationPath = destinationPath;
            this.keywords = new ArrayList<>(keywords);
            this.extensions = new ArrayList<>(extensions);
        }

        public String getWatchPath() {
            return watchPath;
        }

        public String getDestinationPath() {
            return destinationPath;
        }

        public ArrayList<String> getKeywords() {
            return new ArrayList<>(keywords);
        }

        public ArrayList<String> getExtensions() {
            return new ArrayList<>(extensions);
        }
    }

    public static class SavedConfig {
        private final String watchPath;
        private final String destinationPath;
        private final ArrayList<String> keywords;
        private final ArrayList<String> extensions;
        private final boolean autoStartEnabled;

        public SavedConfig(String watchPath, String destinationPath, ArrayList<String> keywords, ArrayList<String> extensions, boolean autoStartEnabled) {
            this.watchPath = watchPath;
            this.destinationPath = destinationPath;
            this.keywords = keywords;
            this.extensions = extensions;
            this.autoStartEnabled = autoStartEnabled;
        }

        public String getWatchPath() {
            return watchPath;
        }

        public String getDestinationPath() {
            return destinationPath;
        }

        public ArrayList<String> getKeywords() {
            return keywords;
        }

        public ArrayList<String> getExtensions() {
            return extensions;
        }

        public boolean isAutoStartEnabled() {
            return autoStartEnabled;
        }
    }

    private File getDefaultSavePath() {
        String base = System.getProperty("user.home");
        return new File(base, "Documents/ordexsave/" + FileName);
    }

    public void writeConfig(File watchFolder, File destinationFolder, List<String> keyword, List<String> extension){
        String watchPath = watchFolder == null ? null : watchFolder.getAbsolutePath();
        String destinationPath = destinationFolder == null ? null : destinationFolder.getAbsolutePath();
        SavedRule singleRule = new SavedRule(watchPath, destinationPath, keyword, extension);
        writeRules(List.of(singleRule));
    }

    public void clearConfig() {
        Properties write = loadAllProperties();
        clearAllRuleKeys(write);
        saveProperties(write);
    }

    public void writeRules(List<SavedRule> rules) {
        Properties write = loadAllProperties();
        clearAllRuleKeys(write);
        write.setProperty(RULE_COUNT_KEY, String.valueOf(rules.size()));

        for (int ruleIndex = 0; ruleIndex < rules.size(); ruleIndex++) {
            SavedRule rule = rules.get(ruleIndex);
            String prefix = "rule." + ruleIndex + ".";
            if (rule.getWatchPath() != null && !rule.getWatchPath().isBlank()) {
                write.setProperty(prefix + "watchPath", rule.getWatchPath());
            }
            if (rule.getDestinationPath() != null && !rule.getDestinationPath().isBlank()) {
                write.setProperty(prefix + "destinationPath", rule.getDestinationPath());
            }
            for (int keyIndex = 0; keyIndex < rule.getKeywords().size(); keyIndex++) {
                write.setProperty(prefix + "keyword." + keyIndex, rule.getKeywords().get(keyIndex));
            }
            for (int extIndex = 0; extIndex < rule.getExtensions().size(); extIndex++) {
                write.setProperty(prefix + "extension." + extIndex, rule.getExtensions().get(extIndex));
            }
        }

        saveProperties(write);
    }

    public ArrayList<SavedRule> readRules() {
        Properties read = loadAllProperties();
        ArrayList<SavedRule> savedRules = new ArrayList<>();

        int ruleCount = parseRuleCount(read.getProperty(RULE_COUNT_KEY));
        if (ruleCount <= 0) {
            SavedConfig legacy = readLegacySingleConfig(read);
            if (legacy != null) {
                savedRules.add(new SavedRule(
                        legacy.getWatchPath(),
                        legacy.getDestinationPath(),
                        legacy.getKeywords(),
                        legacy.getExtensions()));
            }
            return savedRules;
        }

        for (int ruleIndex = 0; ruleIndex < ruleCount; ruleIndex++) {
            String prefix = "rule." + ruleIndex + ".";
            ArrayList<String> keywords = readSequentialValues(read, prefix + "keyword.");
            ArrayList<String> extensions = readSequentialValues(read, prefix + "extension.");
            String watchPath = read.getProperty(prefix + "watchPath");
            String destinationPath = read.getProperty(prefix + "destinationPath");

            if (watchPath == null && destinationPath == null && keywords.isEmpty() && extensions.isEmpty()) {
                continue;
            }
            savedRules.add(new SavedRule(watchPath, destinationPath, keywords, extensions));
        }
        return savedRules;
    }

    public SavedConfig readConfig() {
        File path = getDefaultSavePath();
        if (!path.exists()) {
            return null;
        }

        Properties read = loadAllProperties();
        ArrayList<SavedRule> rules = readRules();
        if (rules.isEmpty()) {
            return null;
        }
        SavedRule firstRule = rules.get(0);
        return new SavedConfig(
                firstRule.getWatchPath(),
                firstRule.getDestinationPath(),
                firstRule.getKeywords(),
                firstRule.getExtensions(),
                Boolean.parseBoolean(read.getProperty("autoStartEnabled", "false")));
    }

    public boolean isAutoStartEnabled() {
        Properties read = loadAllProperties();
        return Boolean.parseBoolean(read.getProperty("autoStartEnabled", "false"));
    }

    public void setAutoStartEnabled(boolean enabled) {
        Properties write = loadAllProperties();
        write.setProperty("autoStartEnabled", String.valueOf(enabled));
        saveProperties(write);
    }

    public void Write(ArrayList<String> keyword ,ArrayList<String> extension){
        writeConfig(null, null, keyword, extension);
    }


    public void Read(File path){
        SavedConfig savedConfig = readConfig();
        if (savedConfig == null) {
            return;
        }
        Ruledate date = new Ruledate();
        date.regi(savedConfig.getKeywords(), savedConfig.getExtensions());//ここでruledateクラスに読み込んだarraylistを保存する

    }

    private Properties loadAllProperties() {
        Properties properties = new Properties();
        File path = getDefaultSavePath();
        if (!path.exists()) {
            return properties;
        }
        try(Reader reader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)){
            properties.load(reader);
        } catch(IOException e){
            System.out.println("読み込みエラー" + e.getMessage());
        }
        return properties;
    }

    private void saveProperties(Properties write) {
        File savePath = getDefaultSavePath();
        savePath.getParentFile().mkdirs();
        Path = savePath;
        try (OutputStreamWriter out = new OutputStreamWriter(
                new FileOutputStream(savePath, false),
                StandardCharsets.UTF_8)) {
            write.store(out, "ordex settings");
        } catch(IOException e){
            System.out.println("書き込みエラー" + e.getMessage());
        }
    }

    private SavedConfig readLegacySingleConfig(Properties read) {
        ArrayList<String> key = readSequentialValues(read, "keyword");
        ArrayList<String> ext = readSequentialValues(read, "extension");
        String watchPath = read.getProperty("watchPath");
        String destinationPath = read.getProperty("destinationPath");

        if (watchPath == null && destinationPath == null && key.isEmpty() && ext.isEmpty()) {
            return null;
        }

        return new SavedConfig(
                watchPath,
                destinationPath,
                key,
                ext,
                Boolean.parseBoolean(read.getProperty("autoStartEnabled", "false")));
    }

    private int parseRuleCount(String countText) {
        if (countText == null) {
            return 0;
        }
        try {
            return Integer.parseInt(countText);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private ArrayList<String> readSequentialValues(Properties read, String keyPrefix) {
        ArrayList<String> values = new ArrayList<>();
        for (int index = 0;; index++) {
            String value = read.getProperty(keyPrefix + index);
            if (value == null) {
                break;
            }
            values.add(value);
        }
        return values;
    }

    private void clearAllRuleKeys(Properties properties) {
        Set<String> keys = properties.stringPropertyNames();
        ArrayList<String> removeKeys = new ArrayList<>();
        for (String key : keys) {
            if (key.equals("watchPath")
                    || key.equals("destinationPath")
                    || key.equals(RULE_COUNT_KEY)
                    || key.startsWith("keyword")
                    || key.startsWith("extension")
                    || key.startsWith("rule.")) {
                removeKeys.add(key);
            }
        }
        for (String key : removeKeys) {
            properties.remove(key);
        }
    }
}
