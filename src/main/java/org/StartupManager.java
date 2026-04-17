package org;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

public class StartupManager {
    private static final String RUN_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String RUN_VALUE_NAME = "ordex";
    private static final String LEGACY_STARTUP_SCRIPT_NAME = "ordex-autostart.cmd";

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
                boolean updated = process.waitFor() == 0;
                deleteLegacyStartupScript();
                return updated;
            } else {
                Process process = new ProcessBuilder(
                        "reg", "delete", RUN_KEY,
                        "/v", RUN_VALUE_NAME,
                        "/f").start();
                int exit = process.waitFor();
                boolean deleted = exit == 0 || exit == 1;
                deleteLegacyStartupScript();
                return deleted;
            }
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public boolean isAutoStartRegistered() {
        if (!isSupportedPlatform()) {
            return false;
        }
        try {
            Process process = new ProcessBuilder(
                    "reg", "query", RUN_KEY, "/v", RUN_VALUE_NAME).start();
            String output = readProcessOutput(process.getInputStream());
            int exit = process.waitFor();
            if (exit != 0) {
                return false;
            }

            String launchTarget = resolveLaunchTarget();
            if (launchTarget == null || launchTarget.isBlank()) {
                return true;
            }

            String registeredCommand = extractRegisteredCommand(output);
            if (registeredCommand == null || registeredCommand.isBlank()) {
                return false;
            }

            String expectedExecutable = extractExecutablePath(launchTarget);
            String registeredExecutable = extractExecutablePath(registeredCommand);
            boolean runKeyMatched;
            if (expectedExecutable == null || registeredExecutable == null) {
                runKeyMatched = normalizeCommand(registeredCommand).contains(normalizeCommand(launchTarget));
            } else {
                runKeyMatched = normalizePath(expectedExecutable).equals(normalizePath(registeredExecutable));
            }
            return runKeyMatched;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private String resolveLaunchTarget() {
        // jpackage実行時はランチャーの実パスが提供されるので最優先で使う
        String jpackageLauncher = System.getProperty("jpackage.app-path", "");
        if (!jpackageLauncher.isBlank()) {
            return quoteIfNeeded(jpackageLauncher) + " --tray";
        }

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

        String inferredLauncher = inferLauncherPath(processCommand);
        if (inferredLauncher != null) {
            return quoteIfNeeded(inferredLauncher) + " --tray";
        }

        // フォールバック: これまで通りjavaw + classpath（開発起動向け）
        String javaw = System.getProperty("java.home") + "\\bin\\javaw.exe";
        String classPath = System.getProperty("java.class.path", "");
        return quoteIfNeeded(javaw) + " -cp " + quoteIfNeeded(classPath) + " org.Main --tray";
    }

    private String inferLauncherPath(String processCommand) {
        if (processCommand == null || processCommand.isBlank()) {
            return null;
        }
        String lower = processCommand.toLowerCase(Locale.ROOT).replace('/', '\\');
        String marker = "\\runtime\\bin\\java";
        int markerIndex = lower.indexOf(marker);
        if (markerIndex < 0) {
            return null;
        }
        try {
            String appRoot = processCommand.substring(0, markerIndex);
            Path rootPath = Path.of(appRoot);
            String appName = rootPath.getFileName() != null ? rootPath.getFileName().toString() : "";
            if (appName.isBlank()) {
                return null;
            }
            Path launcher = rootPath.resolve(appName + ".exe");
            if (!launcher.toFile().isFile()) {
                return null;
            }
            return launcher.toString();
        } catch (Exception e) {
            return null;
        }
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

    private String extractRegisteredCommand(String regQueryOutput) {
        if (regQueryOutput == null || regQueryOutput.isBlank()) {
            return null;
        }

        String[] lines = regQueryOutput.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.toLowerCase(Locale.ROOT).startsWith(RUN_VALUE_NAME.toLowerCase(Locale.ROOT))) {
                continue;
            }
            int typeIndex = trimmed.indexOf("REG_");
            if (typeIndex < 0) {
                continue;
            }

            int valueStart = trimmed.indexOf(' ', typeIndex);
            if (valueStart < 0 || valueStart + 1 >= trimmed.length()) {
                continue;
            }
            return trimmed.substring(valueStart + 1).trim();
        }
        return null;
    }

    private String extractExecutablePath(String command) {
        if (command == null) {
            return null;
        }
        String trimmed = command.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("\"")) {
            int end = trimmed.indexOf('"', 1);
            if (end <= 1) {
                return null;
            }
            return trimmed.substring(1, end);
        }
        int spaceIndex = trimmed.indexOf(' ');
        if (spaceIndex < 0) {
            return trimmed;
        }
        return trimmed.substring(0, spaceIndex);
    }

    private String normalizePath(String path) {
        return path.replace('/', '\\').toLowerCase(Locale.ROOT).trim();
    }

    private String normalizeCommand(String command) {
        return command.replace("\"", "").replace('/', '\\').toLowerCase(Locale.ROOT).trim();
    }

    private void deleteLegacyStartupScript() {
        try {
            String appData = System.getenv("APPDATA");
            if (appData == null || appData.isBlank()) {
                return;
            }
            Path scriptPath = Path.of(
                    appData,
                    "Microsoft",
                    "Windows",
                    "Start Menu",
                    "Programs",
                    "Startup",
                    LEGACY_STARTUP_SCRIPT_NAME);
            Files.deleteIfExists(scriptPath);
        } catch (Exception ignored) {
            // レガシー補助ファイルの削除失敗は自動起動設定の成否に影響させない
        }
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
