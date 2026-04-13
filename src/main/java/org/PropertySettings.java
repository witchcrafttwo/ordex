package org;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Set;


public class PropertySettings {
    private static final String FileName = "ordex.properties";
    File Path;

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
        Properties write = loadAllProperties();

        clearRuleKeys(write);

        if (watchFolder != null) {
            write.setProperty("watchPath", watchFolder.getAbsolutePath());
        }
        if (destinationFolder != null) {
            write.setProperty("destinationPath", destinationFolder.getAbsolutePath());
        }

        int index = 0;
        for(String key : keyword){
            write.setProperty("keyword" + index, key);
            index++;
        }
        index = 0;
        for(String ext : extension){
            write.setProperty("extension" + index, ext);
            index++;
        }

        saveProperties(write);
    }

    public SavedConfig readConfig() {
        File path = getDefaultSavePath();
        if (!path.exists()) {
            return null;
        }

        Properties read = loadAllProperties();
        ArrayList<String> key = new ArrayList<>();
        ArrayList<String> ext = new ArrayList<>();
        for(int index = 0;;index++) {
            String keyword = read.getProperty("keyword" + index);
            String extension = read.getProperty("extension" + index);
            if(keyword == null && extension == null ){
                break;
            }
            if(keyword != null ) {
                key.add(keyword);
            }
            if(extension != null){
                ext.add(extension);
            }
        }

        return new SavedConfig(
                read.getProperty("watchPath"),
                read.getProperty("destinationPath"),
                key,
                ext,
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

    private void clearRuleKeys(Properties properties) {
        properties.remove("watchPath");
        properties.remove("destinationPath");
        Set<String> keys = properties.stringPropertyNames();
        ArrayList<String> removeKeys = new ArrayList<>();
        for (String key : keys) {
            if (key.startsWith("keyword") || key.startsWith("extension")) {
                removeKeys.add(key);
            }
        }
        for (String key : removeKeys) {
            properties.remove(key);
        }
    }
}
