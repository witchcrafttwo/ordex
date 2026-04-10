package org;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
public class FileMove {
    public void Move(Path from, Path to, boolean overwrite) throws IOException {
        try {
            Thread.sleep(500);
            Files.move(from, to);
        } catch (UnsupportedOperationException e) {
            System.out.println("サポートされないコピー・オプションが配列に含まれています");
        } catch(DirectoryNotEmptyException e){
            System.out.println("空ではないディレクトリが含まれています");
        } catch(FileAlreadyExistsException e){
            System.out.println("すでにファイルが存在するよ。確認してね");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}