package org;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

public class StartupManager {
    private static final String RUN_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String RUN_VALUE_NAME = "ordex";
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

        String launchTarget = resolveLaunchTarget();
        if (launchTarget == null || launchTarget.isBlank()) {
        File startupScript = getStartupScriptFile();
        if (startupScript == null) {
            return false;
        }

        try {
            if (enabled) {
                Process process = new ProcessBuilder(
                        "reg", "add", RUN_KEY,
                        "/v", RUN_VALUE_NAME,
                        "/t", "REG_SZ",
                        "/d", launchTarget,
                        "/f").start();
                return process.waitFor() == 0;
            } else {
                Process process = new ProcessBuilder(
                        "reg", "delete", RUN_KEY,
                        "/v", RUN_VALUE_NAME,
                        "/f").start();
                int exit = process.waitFor();
                return exit == 0 || exit == 1;
            }
        } catch (IOException | InterruptedException e) {
                String command = "@echo off\r\n" + buildLaunchCommand();
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
        if (!isSupportedPlatform()) {
            return false;
        }
        String launchTarget = resolveLaunchTarget();
        if (launchTarget == null || launchTarget.isBlank()) {
            return false;
        }
        try {
            Process process = new ProcessBuilder(
                    "reg", "query", RUN_KEY, "/v", RUN_VALUE_NAME).start();
            String output = readProcessOutput(process.getInputStream());
            int exit = process.waitFor();
            return exit == 0 && output.toLowerCase(Locale.ROOT).contains(launchTarget.toLowerCase(Locale.ROOT));
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private String resolveLaunchTarget() {
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
            return "\"" + processCommand + "\" --hidden";
            return "start \"\" \"" + processCommand + "\"\r\n";
        }

        String[] args = ProcessHandle.current().info().arguments().orElse(new String[0]);
        for (int i = 0; i < args.length - 1; i++) {
            if ("-jar".equals(args[i])) {
                return "\"" + processCommand + "\" -jar \"" + args[i + 1] + "\" --hidden";
            }
        }

        // フォールバック: これまで通りjavaw + classpath（開発起動向け）
        String javaw = System.getProperty("java.home") + "\\bin\\javaw.exe";
        String classPath = System.getProperty("java.class.path", "");
        return "\"" + javaw + "\" -cp \"" + classPath + "\" org.Main --hidden";
    }

    private String readProcessOutput(InputStream inputStream) {
        StringBuilder output = new StringBuilder();
        try (Scanner scanner = new Scanner(inputStream)) {
            while (scanner.hasNextLine()) {
                output.append(scanner.nextLine()).append('\n');
            }
        }
        return output.toString();
                return "start \"\" \"" + processCommand + "\" -jar \"" + args[i + 1] + "\"\r\n";
            }
        }

        // フォールバック: これまで通りjavaw + classpath
        String javaw = System.getProperty("java.home") + "\\bin\\javaw.exe";
        String classPath = System.getProperty("java.class.path", "");
        return "start \"\" \"" + javaw + "\" -cp \"" + classPath + "\" org.Main\r\n";
    }
}
