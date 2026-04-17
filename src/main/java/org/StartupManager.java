package org;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import java.util.Locale;
import java.util.Optional;

public class StartupManager {
    private static final String RUN_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run";
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
                Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, RUN_KEY, RUN_VALUE_NAME, launchTarget);
                return true;
            } else {
                if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, RUN_KEY, RUN_VALUE_NAME)) {
                    Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, RUN_KEY, RUN_VALUE_NAME);
                }
                return true;
            }
        } catch (RuntimeException e) {
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
            if (!Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, RUN_KEY, RUN_VALUE_NAME)) {
                return false;
            }
            String currentValue = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, RUN_KEY, RUN_VALUE_NAME);
            return currentValue.toLowerCase(Locale.ROOT).contains(launchTarget.toLowerCase(Locale.ROOT));
        } catch (RuntimeException e) {
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
}
