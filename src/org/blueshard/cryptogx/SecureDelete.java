package org.blueshard.cryptogx;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.TreeSet;

/**
 * <p>Class for secure delete files<p/>
 *
 * @since 1.2.0
 */
public class SecureDelete {

    public static void deleteDirectory(String directory, int iterations, byte[] buffer) throws IOException {
        TreeSet<File> directories = new TreeSet<>();
        Files.walk(Paths.get(directory)).map(Path::toFile).forEach(directoryFile -> {
            if (directoryFile.isDirectory()) {
                directories.add(directoryFile);
            } else {
                try {
                    SecureDelete.deleteFile(directoryFile, iterations, buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (directoryFile.exists()) {
                    if (directoryFile.delete()) {
                        break;
                    }
                }
            }
        });

        File deleteDirectory = directories.last();
        while (deleteDirectory != null) {
            deleteDirectory.delete();

            while (deleteDirectory.delete()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            deleteDirectory = directories.lower(deleteDirectory);
        }
    }

    /**
     * <p>Overwrites the file {@param iterations} times line by line with random bytes (minimal size {@param minFileSize}; maximal size {@param maxFileSize}) and delete it</p>
     *
     * @param file that should be deleted
     * @param iterations how many times the file should be overwritten before it gets deleted
     * @return if the file could be deleted
     * @throws IOException
     *
     * @since 1.12.0
     */
    public static void deleteFile(File file, int iterations, byte[] buffer) throws IOException {
        SecureRandom secureRandom = new SecureRandom();
        RandomAccessFile raf = new RandomAccessFile(file, "rws");
        for (int i=0; i<iterations; i++) {
            long length = file.length();
            raf.seek(0);
            raf.getFilePointer();
            int pos = 0;
            while (pos < length) {
                secureRandom.nextBytes(buffer);
                raf.write(buffer);
                pos += buffer.length;
            }

        }
        raf.close();

        while (file.delete()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
