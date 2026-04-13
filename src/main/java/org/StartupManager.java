package org;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

public class StartupManager {
    private static final String RUN_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String RUN_VALUE_NAME = "ordex";

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
        Optional<String> processCommandOpt = ProcessHandle.current().info().command();
        String processCommand = processCommandOpt.orElse("");
        String lower = processCommand.toLowerCase(Locale.ROOT);

        // exe化されている場合はその実行ファイルを直接起動する
        if (lower.endsWith(".exe") && !lower.contains("java")) {
            return processCommand;
        }

        String[] args = ProcessHandle.current().info().arguments().orElse(new String[0]);
        for (int i = 0; i < args.length - 1; i++) {
            if ("-jar".equals(args[i])) {
                return "\"" + processCommand + "\" -jar \"" + args[i + 1] + "\"";
            }
        }

        // フォールバック: これまで通りjavaw + classpath（開発起動向け）
        String javaw = System.getProperty("java.home") + "\\bin\\javaw.exe";
        String classPath = System.getProperty("java.class.path", "");
        return "\"" + javaw + "\" -cp \"" + classPath + "\" org.Main";
    }

    private String readProcessOutput(InputStream inputStream) {
        StringBuilder output = new StringBuilder();
        try (Scanner scanner = new Scanner(inputStream)) {
            while (scanner.hasNextLine()) {
                output.append(scanner.nextLine()).append('\n');
            }
        }
        return output.toString();
    }
}
