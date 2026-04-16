package org;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

public class StartupManager {
    private static final String RUN_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String RUN_VALUE_NAME = "ordex";
    private static final String STARTUP_SCRIPT_NAME = "ordex-startup.cmd";

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
                boolean registryUpdated = runProcess(new ProcessBuilder(
                        "reg", "add", RUN_KEY,
                        "/v", RUN_VALUE_NAME,
                        "/t", "REG_SZ",
                        "/d", launchTarget,
                        "/f"));
                boolean scriptUpdated = createStartupScript(launchTarget);
                return registryUpdated || scriptUpdated;
            } else {
                boolean scriptDeleted = deleteStartupScript();
                Process process = new ProcessBuilder(
                        "reg", "delete", RUN_KEY,
                        "/v", RUN_VALUE_NAME,
                        "/f").start();
                int exit = process.waitFor();
                return scriptDeleted && (exit == 0 || exit == 1);
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
            boolean registryMatches = exit == 0
                    && output.toLowerCase(Locale.ROOT).contains(launchTarget.toLowerCase(Locale.ROOT));
            return registryMatches || startupScriptContains(launchTarget);
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
            return quoteIfNeeded(processCommand) + " --tray";
        }

        String[] args = ProcessHandle.current().info().arguments().orElse(new String[0]);
        for (int i = 0; i < args.length - 1; i++) {
            if ("-jar".equals(args[i])) {
                return quoteIfNeeded(processCommand) + " -jar " + quoteIfNeeded(args[i + 1]) + " --tray";
            }
        }

        // フォールバック: これまで通りjavaw + classpath（開発起動向け）
        String javaw = System.getProperty("java.home") + "\\bin\\javaw.exe";
        String classPath = System.getProperty("java.class.path", "");
        return quoteIfNeeded(javaw) + " -cp " + quoteIfNeeded(classPath) + " org.Main --tray";
    }

    private String quoteIfNeeded(String value) {
        if (value == null) {
            return "\"\"";
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed;
        }
        return "\"" + trimmed + "\"";
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

    private boolean runProcess(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        return process.waitFor() == 0;
    }

    private boolean createStartupScript(String launchTarget) {
        Path scriptPath = getStartupScriptPath();
        if (scriptPath == null) {
            return false;
        }

        try {
            Files.createDirectories(scriptPath.getParent());
            String scriptBody = "@echo off\r\n" + launchTarget + "\r\n";
            Files.writeString(scriptPath, scriptBody, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean deleteStartupScript() {
        Path scriptPath = getStartupScriptPath();
        if (scriptPath == null) {
            return false;
        }
        try {
            return !Files.exists(scriptPath) || Files.deleteIfExists(scriptPath);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean startupScriptContains(String launchTarget) {
        Path scriptPath = getStartupScriptPath();
        if (scriptPath == null || !Files.exists(scriptPath)) {
            return false;
        }
        try {
            String content = Files.readString(scriptPath, StandardCharsets.UTF_8);
            return content.toLowerCase(Locale.ROOT).contains(launchTarget.toLowerCase(Locale.ROOT));
        } catch (IOException e) {
            return false;
        }
    }

    private Path getStartupScriptPath() {
        String appData = System.getenv("APPDATA");
        if (appData == null || appData.isBlank()) {
            return null;
        }
        return Path.of(
                appData,
                "Microsoft",
                "Windows",
                "Start Menu",
                "Programs",
                "Startup",
                STARTUP_SCRIPT_NAME);
    }
}
