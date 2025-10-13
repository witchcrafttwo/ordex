import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.WatchEvent.*;
public class FileWatcher {
    public static void watchservice(File dir,String keyword) throws IOException {
        String wtpath = dir.toString();
        String extension ;
        Path test = Paths.get("C:\\Users\\yunre\\documents\\.information");
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("監視対象がディレクトリではない: " + dir);
        }
        WatchService watcher;
        try{
            watcher = FileSystems.getDefault().newWatchService();
            Watchable path = Paths.get(wtpath);
            path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        } catch (IOException e){
            e.printStackTrace();
            return;
        }
            try {
                while (true) {
                    WatchKey watchkey;
                    try{
                        watchkey = watcher.take();
                    } catch (InterruptedException e){
                        System.err.println(e.getMessage());
                        return;
                    }

                    for (WatchEvent<?> event : watchkey.pollEvents()) {
                        Kind<?> kind = event.kind();
                        if (kind == OVERFLOW) continue;

                        Path rel = (Path)event.context();        // 相対名
                        Path full = dir.toPath().resolve(rel);          // フルパス
                        String name = rel.toString().toLowerCase();
                        System.out.println("kind=" + kind + ", context=" + rel);

                        // 一時拡張子は早めに弾く
                        if (name.endsWith(".crdownload") || name.endsWith(".tmp") || name.endsWith(".part")) {
                            continue;
                        }
                        if(name.endsWith(".txt") && kind == ENTRY_CREATE) {
                            FileMove(full,test.resolve(rel),true);
                            System.out.println("移動できましたよ");
                        }
                        System.out.println(kind.name() + " : " + full);

                        // キーワード判定（例：一致したら印を出す）
                        if (name.contains(keyword)) {
                            System.out.println("★キーワード一致: " + full);
                            // TODO: ここに実処理を書く（移動/通知 など）
                        }
                    }

                    if (!watchkey.reset()) {
                        System.out.println("キー失効。監視終了。");
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try { watcher.close(); } catch (IOException ignored) {}
            }

    } public static void FileMove(Path from, Path to,boolean overwrite) throws IOException {
        try{
            Files.move(from,to);
        } catch (IOException e) {
            System.out.println(e);
        }
}
}
