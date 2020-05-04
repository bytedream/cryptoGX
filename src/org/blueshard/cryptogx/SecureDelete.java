package org.blueshard.cryptogx;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class SecureDelete {

    /**
     * <p>Overwrites the file {@param iterations} times at once with random bytes an delete it</p>
     *
     * @see SecureDelete#deleteFileAllInOne(File, int)
     */
    public static boolean deleteFileAllInOne(String filename, int iterations) throws IOException, NoSuchAlgorithmException {
        return deleteFileAllInOne(new File(filename), iterations);
    }

    /**
     * <p>Overwrites the file {@param iterations} times at once with random bytes and delete it</p>
     *
     * @param file that should be deleted
     * @param iterations how many times the file should be overwritten before it gets deleted
     * @return if the file could be deleted
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static boolean deleteFileAllInOne(File file, int iterations) throws IOException, NoSuchAlgorithmException {
        long fileLength = file.length() + 1 ;
        for (int i=0; i<iterations; i++) {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            if (fileLength > 1000000000) {
                int numOfByteArrays = (int) Math.ceil((double) fileLength / 1000000000);
                for (int len=0; len<numOfByteArrays; len++) {
                    int newMaxFileSize = (int) fileLength / numOfByteArrays;
                    int newMinFileSize = 0;
                    byte[] randomBytes = new byte[new Random().nextInt(newMaxFileSize - newMinFileSize) + newMinFileSize];
                    SecureRandom.getInstanceStrong().nextBytes(randomBytes);
                    bufferedOutputStream.write(randomBytes);
                }
            } else {
                byte[] randomBytes = new byte[new Random().nextInt((int) fileLength)];
                SecureRandom.getInstanceStrong().nextBytes(randomBytes);
                bufferedOutputStream.write(randomBytes);
            }
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        }

        return file.delete();
    }

    /**
     * <p>Overwrites the file {@param iterations} times at once with random bytes (minimal size {@param minFileSize}; maximal size {@param maxFileSize}) and delete it</p>
     *
     * @see SecureDelete#deleteFileAllInOne(String, int, long, long)
     */
    public static boolean deleteFileAllInOne(String filename, int iterations, long minFileSize, long maxFileSize) throws IOException, NoSuchAlgorithmException {
        return deleteFileAllInOne(new File(filename), iterations, minFileSize, maxFileSize);
    }

    /**
     * <p>Overwrites the file {@param iterations} times at once with random bytes (minimal size {@param minFileSize}; maximal size {@param maxFileSize}) and delete it</p>
     *
     * @param file that should be deleted
     * @param iterations how many times the file should be overwritten before it gets deleted
     * @param minFileSize is the minimal file size for every {@param iterations}
     * @param maxFileSize is the maximal file size for every {@param iterations}
     * @return if the file could be deleted
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static boolean deleteFileAllInOne(File file, int iterations, long minFileSize, long maxFileSize) throws IOException, NoSuchAlgorithmException {
        for (int i = 0; i < iterations; i++) {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            if (maxFileSize > 1000000000) {
                int numOfByteArrays = (int) Math.ceil((double) maxFileSize / 1000000000);
                for (int len = 0; len < numOfByteArrays; len++) {
                    int newMaxFileSize = (int) maxFileSize / numOfByteArrays;
                    int newMinFileSize = 0;
                    if (minFileSize != 0) {
                        newMinFileSize = (int) minFileSize / numOfByteArrays;
                    }
                    byte[] randomBytes = new byte[new Random().nextInt(newMaxFileSize - newMinFileSize) + newMinFileSize];
                    SecureRandom.getInstanceStrong().nextBytes(randomBytes);
                    bufferedOutputStream.write(randomBytes);
                }
            } else {
                byte[] randomBytes = new byte[new Random().nextInt((int) maxFileSize - (int) minFileSize) + (int) minFileSize];
                SecureRandom.getInstanceStrong().nextBytes(randomBytes);
                bufferedOutputStream.write(randomBytes);
            }
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        }

        return file.delete();
    }

    /**
     * <p>Overwrites the file {@param iterations} times line by line with random bytes and delete it</p>
     *
     * @see SecureDelete#deleteFileLineByLine(File, int)
     */
    public static boolean deleteFileLineByLine(String filename, int iterations) throws NoSuchAlgorithmException, IOException {
        return deleteFileLineByLine(new File(filename), iterations);
    }

    /**
     * <p>Overwrites the file {@param iterations} times line by line with random bytes and delete it</p>
     *
     * @param file that should be deleted
     * @param iterations how many times the file should be overwritten before it gets deleted
     * @return if the file could be deleted
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static boolean deleteFileLineByLine(File file, int iterations) throws NoSuchAlgorithmException, IOException {
        long fileLength = file.length() + 1 ;
        for (int i=0; i<iterations; i++) {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            if (fileLength > 1000000000) {
                int numOfByteArrays = (int) Math.ceil((double) fileLength / 1000000000);
                for (int len=0; len<numOfByteArrays; len++) {
                    int newMaxFileSize = (int) fileLength / numOfByteArrays;
                    int newMinFileSize = 0;
                    byte[] randomBytes = new byte[new Random().nextInt(newMaxFileSize - newMinFileSize) + newMinFileSize];
                    SecureRandom.getInstanceStrong().nextBytes(randomBytes);
                    for (byte b: randomBytes) {
                        bufferedOutputStream.write(b);
                    }
                }
            } else {
                byte[] randomBytes = new byte[new Random().nextInt((int) fileLength)];
                SecureRandom.getInstanceStrong().nextBytes(randomBytes);
                for (byte b : randomBytes) {
                    bufferedOutputStream.write(b);
                }
            }
            bufferedOutputStream.flush();
            bufferedOutputStream.close();
        }

        return file.delete();
    }

    /**
     * <p>Overwrites the file {@param iterations} times line by line with random bytes (minimal size {@param minFileSize}; maximal size {@param maxFileSize}) and delete it</p>
     */
    public static boolean deleteFileLineByLine(String filename, int iterations, long minFileSize, long maxFileSize) throws NoSuchAlgorithmException, IOException {
        return deleteFileLineByLine(new File(filename), iterations, minFileSize, maxFileSize);
    }

    /**
     * <p>Overwrites the file {@param iterations} times line by line with random bytes (minimal size {@param minFileSize}; maximal size {@param maxFileSize}) and delete it</p>
     *
     * @param file that should be deleted
     * @param iterations how many times the file should be overwritten before it gets deleted
     * @param minFileSize is the minimal file size for every {@param iterations}
     * @param maxFileSize is the maximal file size for every {@param iterations}
     * @return if the file could be deleted
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static boolean deleteFileLineByLine(File file, int iterations, long minFileSize, long maxFileSize) throws NoSuchAlgorithmException, IOException {
            for (int i=0; i<iterations; i++) {
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                if (maxFileSize > 1000000000) {
                    int numOfByteArrays = (int) Math.ceil((double) maxFileSize / 1000000000);
                    for (int len=0; len<numOfByteArrays; len++) {
                        int newMaxFileSize = (int) maxFileSize / numOfByteArrays;
                        int newMinFileSize = 0;
                        if (minFileSize != 0) {
                            newMinFileSize = (int) minFileSize / numOfByteArrays;
                        }
                        byte[] randomBytes = new byte[new Random().nextInt(newMaxFileSize - newMinFileSize) + newMinFileSize];
                        SecureRandom.getInstanceStrong().nextBytes(randomBytes);
                        for (byte b: randomBytes) {
                            bufferedOutputStream.write(b);
                        }
                    }
                } else {
                    byte[] randomBytes = new byte[new Random().nextInt((int) maxFileSize - (int) minFileSize) + (int) minFileSize];
                    SecureRandom.getInstanceStrong().nextBytes(randomBytes);
                    for (byte b : randomBytes) {
                        bufferedOutputStream.write(b);
                    }
                }
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
            }

        return file.delete();
    }

}
