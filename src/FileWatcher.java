import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class FileWatcher {
    public static void watchservice(File dir) throws IOException, InterruptedException {
            WatchService watchservice = FileSystems.getDefault().newWatchService();
            Path dirPath = dir.toPath(); //File型をPath型へ変換
            dirPath.register(watchservice, StandardWatchEventKinds.ENTRY_CREATE);
        new Thread(() -> {
            try {
                while (true) {
                    WatchKey key = watchservice.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        // イベント処理
                    }
                    key.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();



    }
}
