package org;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;


public class PropertySettings {
    private static final String FileName = "ordex.properties";
    File Path;

    public static class SavedConfig {
        private final String watchPath;
        private final String destinationPath;
        private final ArrayList<String> keywords;
        private final ArrayList<String> extensions;

        public SavedConfig(String watchPath, String destinationPath, ArrayList<String> keywords, ArrayList<String> extensions) {
            this.watchPath = watchPath;
            this.destinationPath = destinationPath;
            this.keywords = keywords;
            this.extensions = extensions;
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
    }

    private File getDefaultSavePath() {
        String base = System.getProperty("user.home");
        return new File(base, "Documents/ordexsave/" + FileName);
    }

    public void writeConfig(File watchFolder, File destinationFolder, List<String> keyword, List<String> extension){
        Properties write = new Properties();

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

    public SavedConfig readConfig() {
        File path = getDefaultSavePath();
        if (!path.exists()) {
            return null;
        }

        Properties read = new Properties();
        ArrayList<String> key = new ArrayList<>();
        ArrayList<String> ext = new ArrayList<>();
        try(Reader reader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)){
            read.load(reader);
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
        } catch(IOException e){
            System.out.println("読み込みエラー" + e.getMessage());
        }

        return new SavedConfig(
                read.getProperty("watchPath"),
                read.getProperty("destinationPath"),
                key,
                ext);
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
    }
