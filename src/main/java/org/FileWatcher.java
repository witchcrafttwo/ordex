package org;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.Locale;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.WatchEvent.*;
public class FileWatcher {
    public static void watchservice(File Sdir,File Todir, ArrayList<String> keyword, ArrayList<String> extension) {
        FileMove FM = new FileMove();
        Path Spath = Sdir.toPath();
        Path Tpath = Todir.toPath();

//        extension = ".txt";
        if (!Sdir.exists() || !Sdir.isDirectory()) {
            throw new IllegalArgumentException("監視対象がディレクトリではない: " + Sdir);
        }
        WatchService watcher;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            Watchable path = Spath;
            path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            while (true) {
                WatchKey watchkey;
                try {
                    watchkey = watcher.take();
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                    return;
                }

                for (WatchEvent<?> event : watchkey.pollEvents()) {
                    Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) continue;

                    Path rel = (Path) event.context();        // 相対名
                    Path SfullPath = Spath.resolve(rel);
                    Path TfullPath = Tpath.resolve(rel);
                    String name = rel.toString().toLowerCase(Locale.ROOT);
                    System.out.println("kind=" + kind + ", context=" + rel);

                    // 一時拡張子は早めに弾く
                    if (name.endsWith(".crdownload") || name.endsWith(".tmp") || name.endsWith(".part")) {
                        continue;
                    }
                    boolean extMatch =
                            extension.isEmpty()
                                     || extension.stream()
                                    .filter(ext -> ext != null && !ext.isBlank())
                                    .map(ext -> ext.toLowerCase(Locale.ROOT))
                                    .anyMatch(name::endsWith);

                    boolean keyMatch =
                            keyword.isEmpty()
                                    || keyword.stream()
                                    .filter(key -> key != null && !key.isBlank())
                                    .map(key -> key.toLowerCase(Locale.ROOT))
                                    .anyMatch(name::contains);

                       if (extMatch && (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) && keyMatch) {
                              if (!Files.exists(SfullPath)) {
                                continue;
                            }
                            try {
                                FM.Move(SfullPath, TfullPath, true);
                                System.out.println("移動できましたよ");
                            } catch (IOException moveError) {
                                System.out.println("移動できませんでした: " + moveError.getMessage());
                            }
                        }

                    System.out.println(kind.name() + " : " + SfullPath);

                    // キーワード判定（例：一致したら印を出す）
//                    if (name.contains(keyword)) {
//                        System.out.println("★キーワード一致: " + TfullPath);
//
//                    }
                }

                if (!watchkey.reset()) {
                    System.out.println("キー失効。監視終了。");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                watcher.close();
            } catch (IOException ignored) {
            }
        }
    }
}
