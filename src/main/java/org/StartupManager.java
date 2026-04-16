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
    private static final String MACHINE_RUN_KEY = "HKLM\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";
    private static final String RUN_VALUE_NAME = "ordex";
    private static final String STARTUP_SHORTCUT_NAME = "ordex.lnk";

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
        String executablePath = resolveExecutablePath();

        try {
            if (enabled) {
                boolean shortcutUpdated = createStartupShortcut(executablePath);
                boolean registryUpdated = runProcess(new ProcessBuilder(
                        "reg", "add", RUN_KEY,
                        "/v", RUN_VALUE_NAME,
                        "/t", "REG_SZ",
                        "/d", launchTarget,
                        "/f"));
                boolean machineRegistryUpdated = runProcess(new ProcessBuilder(
                        "reg", "add", MACHINE_RUN_KEY,
                        "/v", RUN_VALUE_NAME,
                        "/t", "REG_SZ",
                        "/d", launchTarget,
                        "/f"));
                return shortcutUpdated || registryUpdated || machineRegistryUpdated;
            } else {
                Process process = new ProcessBuilder(
                        "reg", "delete", RUN_KEY,
                        "/v", RUN_VALUE_NAME,
                        "/f").start();
                int exit = process.waitFor();
                boolean registryDeleted = exit == 0 || exit == 1;
                Process machineProcess = new ProcessBuilder(
                        "reg", "delete", MACHINE_RUN_KEY,
                        "/v", RUN_VALUE_NAME,
                        "/f").start();
                int machineExit = machineProcess.waitFor();
                boolean machineRegistryDeleted = machineExit == 0 || machineExit == 1;
                boolean shortcutDeleted = deleteStartupShortcut();
                return registryDeleted || machineRegistryDeleted || shortcutDeleted;
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
            boolean registryMatched = queryRunKeyContains(RUN_KEY, launchTarget);
            boolean machineRegistryMatched = queryRunKeyContains(MACHINE_RUN_KEY, launchTarget);
            return registryMatched || machineRegistryMatched || isStartupShortcutRegistered();
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private String resolveExecutablePath() {
        Optional<String> processCommandOpt = ProcessHandle.current().info().command();
        String processCommand = processCommandOpt.orElse("");
        String lower = processCommand.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".exe") && !lower.contains("java")) {
            return processCommand;
        }
        return null;
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

    private boolean queryRunKeyContains(String keyPath, String launchTarget) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(
                "reg", "query", keyPath, "/v", RUN_VALUE_NAME).start();
        String output = readProcessOutput(process.getInputStream());
        int exit = process.waitFor();
        return exit == 0 && output.toLowerCase(Locale.ROOT).contains(launchTarget.toLowerCase(Locale.ROOT));
    }

    private boolean runProcess(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        return process.waitFor() == 0;
    }

    private boolean createStartupShortcut(String executablePath) throws IOException, InterruptedException {
        if (executablePath == null || executablePath.isBlank()) {
            return false;
        }
        Path shortcutPath = getStartupShortcutPath();
        if (shortcutPath == null) {
            return false;
        }
        Files.createDirectories(shortcutPath.getParent());
        String workingDirectory = Path.of(executablePath).getParent().toString();
        String script = "$s=(New-Object -ComObject WScript.Shell).CreateShortcut('"
                + escapeForPowerShell(shortcutPath.toString())
                + "');"
                + "$s.TargetPath='" + escapeForPowerShell(executablePath) + "';"
                + "$s.Arguments='--tray';"
                + "$s.WorkingDirectory='" + escapeForPowerShell(workingDirectory) + "';"
                + "$s.Save()";
        return runProcess(new ProcessBuilder("powershell", "-NoProfile", "-Command", script));
    }

    private boolean deleteStartupShortcut() {
        Path shortcutPath = getStartupShortcutPath();
        if (shortcutPath == null) {
            return false;
        }
        try {
            return !Files.exists(shortcutPath) || Files.deleteIfExists(shortcutPath);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isStartupShortcutRegistered() {
        Path shortcutPath = getStartupShortcutPath();
        if (shortcutPath == null || !Files.exists(shortcutPath)) {
            return false;
        }
        return true;
    }

    private Path getStartupShortcutPath() {
        String appData = System.getenv("APPDATA");
        if (appData == null || appData.isBlank()) {
            return null;
        }
        return Path.of(appData, "Microsoft", "Windows", "Start Menu", "Programs", "Startup", STARTUP_SHORTCUT_NAME);
    }

    private String escapeForPowerShell(String value) {
        return value.replace("'", "''");
    }

}
