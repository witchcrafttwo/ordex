package org;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Optional;

public class StartupManager {
    private static final String STARTUP_FILE_NAME = "ordex-autostart.bat";

    public boolean isSupportedPlatform() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }

    public boolean setAutoStartEnabled(boolean enabled) {
        if (!isSupportedPlatform()) {
            return false;
        }

        File startupScript = getStartupScriptFile();
        if (startupScript == null) {
            return false;
        }

        try {
            if (enabled) {
                String command = "@echo off\r\n" + buildLaunchCommand();
                String javaw = System.getProperty("java.home") + "\\bin\\javaw.exe";
                String classPath = System.getProperty("java.class.path", "");
                String command = "@echo off\r\n"
                        + "start \"\" \"" + javaw + "\" -cp \"" + classPath + "\" org.Main\r\n";
                Files.writeString(startupScript.toPath(), command, StandardCharsets.UTF_8);
            } else {
                Files.deleteIfExists(startupScript.toPath());
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isAutoStartRegistered() {
        File startupScript = getStartupScriptFile();
        return startupScript != null && startupScript.exists();
    }

    private File getStartupScriptFile() {
        String appData = System.getenv("APPDATA");
        if (appData == null || appData.isBlank()) {
            return null;
        }
        File startupDir = new File(appData, "Microsoft\\Windows\\Start Menu\\Programs\\Startup");
        if (!startupDir.exists() && !startupDir.mkdirs()) {
            return null;
        }
        return new File(startupDir, STARTUP_FILE_NAME);
    }

    private String buildLaunchCommand() {
        Optional<String> processCommandOpt = ProcessHandle.current().info().command();
        String processCommand = processCommandOpt.orElse("");
        String lower = processCommand.toLowerCase(Locale.ROOT);

        // exe化されている場合はその実行ファイルを直接起動する
        if (lower.endsWith(".exe") && !lower.contains("java")) {
            return "start \"\" \"" + processCommand + "\"\r\n";
        }

        String[] args = ProcessHandle.current().info().arguments().orElse(new String[0]);
        for (int i = 0; i < args.length - 1; i++) {
            if ("-jar".equals(args[i])) {
                return "start \"\" \"" + processCommand + "\" -jar \"" + args[i + 1] + "\"\r\n";
            }
        }

        // フォールバック: これまで通りjavaw + classpath
        String javaw = System.getProperty("java.home") + "\\bin\\javaw.exe";
        String classPath = System.getProperty("java.class.path", "");
        return "start \"\" \"" + javaw + "\" -cp \"" + classPath + "\" org.Main\r\n";
    }
}
