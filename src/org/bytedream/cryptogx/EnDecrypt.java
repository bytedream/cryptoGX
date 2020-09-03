package org.bytedream.cryptogx;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Class for en- / decrypt text and files<p/>
 *
 * @since 1.0.0
 */
public class EnDecrypt {

    public static class AES extends Thread {

        public int iterations = 65536;

        private final String secretKeyFactoryAlgorithm = "PBKDF2WithHmacSHA1";
        private int keySize = 256;

        private final String key;
        private final byte[] salt;

        public AES(String key, byte[] salt) {
            this.key = key;
            this.salt = salt;
        }

        public AES(String key, byte[] salt, int keySize) {
            this.key = key;
            this.salt = salt;
            this.keySize = keySize;
        }

        public AES(String key, byte[] salt, int iterations, int keySize) {
            this.key = key;
            this.salt = salt;
            this.iterations = iterations;
            this.keySize = keySize;
        }

        /**
         * <p>Creates a secret key from given (plain text) key and salt</p>
         *
         * @return the secret key
         * @throws NoSuchAlgorithmException
         * @throws InvalidKeySpecException
         *
         * @since 1.0.0
         */
        private byte[] createSecretKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(secretKeyFactoryAlgorithm);
            PBEKeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt, iterations, keySize);

            return factory.generateSecret(keySpec).getEncoded();
        }

        /**
         * <p>Writes {@param inputStream} to {@param outputStream}</p>
         *
         * @param inputStream from which is written
         * @param outputStream to which is written
         * @param buffer
         * @throws IOException
         *
         * @since 1.12.0
         */
        private void write(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws IOException {
            int numOfBytesRead;
            while ((numOfBytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, numOfBytesRead);
            }
            outputStream.close();
            inputStream.close();
        }

        /**
         * <p>Encrypts the {@param inputStream} to {@param outputStream}</p>
         *
         * @param inputStream that should be encrypted
         * @param outputStream to which the encrypted {@param inputFile} should be written to
         * @param buffer
         * @throws InvalidKeySpecException
         * @throws NoSuchAlgorithmException
         * @throws NoSuchPaddingException
         * @throws InvalidKeyException
         * @throws IOException
         *
         * @since 1.12.0
         */
        public void encryptFile(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException {
            Key secretKey = new SecretKeySpec(createSecretKey(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
            write(cipherInputStream, outputStream, buffer);
        }

        /**
         * <p>Encrypts all files in the {@param inputDirectory} to the {@param outputDirectory}</p>
         *
         * @param inputDirectory that should be encrypted
         * @param outputDirectory to which the encrypted {@param inputDirectory} files should be written to
         * @param fileEnding get added to every file that gets encrypted (if the {@param fileEnding} starts and ends with
         * a '@', the {@param fileEnding} will get removed from the file if it exists)
         * @param buffer
         * @throws InvalidKeySpecException
         * @throws NoSuchAlgorithmException
         * @throws NoSuchPaddingException
         * @throws InvalidKeyException
         * @throws IOException
         *
         * @since 1.12.0
         */
        public void encryptDirectory(String inputDirectory, String outputDirectory, String fileEnding, byte[] buffer) throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException {
            AtomicBoolean remove = new AtomicBoolean(false);

            if (fileEnding == null) {
                fileEnding = "";
            } else if (fileEnding.startsWith("@") && fileEnding.endsWith("@")) {
                fileEnding = fileEnding.substring(1, fileEnding.length() - 1);
                remove.set(true);
            }

            HashMap<File, File> files = new HashMap<>();
            final String finalFileEnding = fileEnding;
            Files.walk(Paths.get(inputDirectory)).map(Path::toFile).forEach(oldFile -> {
                String oldFilePath = oldFile.getAbsolutePath();
                if (oldFile.isDirectory()) {
                    new File(oldFilePath.replace(inputDirectory, outputDirectory + "/")).mkdir();
                }else if (remove.get() && oldFilePath.endsWith(finalFileEnding)) {
                    files.put(oldFile, new File(oldFilePath.substring(0, oldFilePath.lastIndexOf(finalFileEnding))
                            .replace(inputDirectory, outputDirectory + "/") + finalFileEnding));
                } else {
                    files.put(oldFile, new File(oldFilePath.replace(inputDirectory, outputDirectory + "/") + finalFileEnding));
                }
            });

            File newFile;
            for (Map.Entry<File, File> entry: files.entrySet()) {
                newFile = entry.getValue();
                encryptFile(new FileInputStream(entry.getKey()), new FileOutputStream(newFile), buffer);
            }
        }

        /**
         * <p>Decrypts the {@param inputStream} to {@param outputStream}</p>
         *
         * @param inputStream that should be decrypted
         * @param outputStream to which the decrypted {@param inputFile} should be written to
         * @param buffer
         * @throws InvalidKeySpecException
         * @throws NoSuchAlgorithmException
         * @throws NoSuchPaddingException
         * @throws InvalidKeyException
         * @throws IOException
         * @throws InvalidAlgorithmParameterException
         *
         * @since 1.12.0
         */
        public void decryptFile(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException{
            Key secretKey = new SecretKeySpec(createSecretKey(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            write(inputStream, cipherOutputStream, buffer);
        }

        /**
         * <p>Decrypts all files in the {@param inputDirectory} to the {@param outputDirectory}</p>
         *
         * @param inputDirectory that should be decrypted
         * @param outputDirectory to which the decrypted {@param inputDirectory} files should be written to
         * @param fileEnding get added to every file that gets decrypted (if the {@param fileEnding} starts and ends with
         * a '@', the {@param fileEnding} will get removed from the file if it exists)
         * @param buffer
         * @throws InvalidKeySpecException
         * @throws NoSuchAlgorithmException
         * @throws NoSuchPaddingException
         * @throws InvalidKeyException
         * @throws IOException
         *
         * @since 1.12.0
         */
        public void decryptDirectory(String inputDirectory, String outputDirectory, String fileEnding, byte[] buffer) throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException {
            AtomicBoolean remove = new AtomicBoolean(false);

            if (fileEnding == null) {
                fileEnding = "";
            } else if (fileEnding.startsWith("@") && fileEnding.endsWith("@")) {
                fileEnding = fileEnding.substring(1, fileEnding.length() - 1);
                remove.set(true);
            }

            HashMap<File, File> files = new HashMap<>();
            final String finalFileEnding = fileEnding;
            Files.walk(Paths.get(inputDirectory)).map(Path::toFile).forEach(oldFile -> {
                String oldFilePath = oldFile.getAbsolutePath();
                if (oldFile.isDirectory()) {
                    new File(oldFilePath.replace(inputDirectory, outputDirectory + "/")).mkdir();
                }
                else if (remove.get() && oldFilePath.endsWith(finalFileEnding)) {
                    files.put(oldFile, new File(oldFilePath.substring(0, oldFilePath.lastIndexOf(finalFileEnding))
                            .replace(inputDirectory, outputDirectory + "/") + finalFileEnding));
                } else {
                    files.put(oldFile, new File(oldFilePath.replace(inputDirectory, outputDirectory + "/") + finalFileEnding));
                }
            });

            File newFile;
            for (Map.Entry<File, File> entry: files.entrySet()) {
                newFile = entry.getValue();
                decryptFile(new FileInputStream(entry.getKey()), new FileOutputStream(newFile), buffer);
            }
        }

        /**
         * <p>Encrypt {@param bytes}</p>
         *
         * @param bytes that should be encrypted
         * @return encrypted bytes
         * @throws BadPaddingException
         * @throws IllegalBlockSizeException
         * @throws NoSuchPaddingException
         * @throws NoSuchAlgorithmException
         * @throws InvalidKeySpecException
         * @throws InvalidKeyException
         *
         * @since 1.0.0
         */
        public byte[] encrypt(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
            Key secretKey = new SecretKeySpec(createSecretKey(), "AES");

            Cipher encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return encryptCipher.doFinal(bytes);
        }

        /**
         * <p>Encrypt {@param bytes}</p>
         *
         * @param string that should be encrypted
         *
         * @see EnDecrypt.AES#encrypt(byte[])
         *
         * @since 1.0.0
         */
        public String encrypt(String string) throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
            return Base64.getEncoder().encodeToString(encrypt(string.getBytes(StandardCharsets.UTF_8)));
        }

        /**
         * <p>Decrypt encrypted {@param bytes}</p>
         *
         * @param bytes that should be decrypted
         * @return decrypted bytes
         * @throws BadPaddingException
         * @throws IllegalBlockSizeException
         * @throws NoSuchPaddingException
         * @throws NoSuchAlgorithmException
         * @throws InvalidKeySpecException
         * @throws InvalidKeyException
         *
         * @since 1.12.0
         */
        public byte[] decrypt(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
            Key secretKey = new SecretKeySpec(createSecretKey(), "AES");

            Cipher decryptCipher = Cipher.getInstance("AES");
            decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
            return decryptCipher.doFinal(Base64.getDecoder().decode(bytes));
        }

        /**
         * <p>Decrypt encrypted {@param string}</p>
         *
         * @param string that should be decrypted
         *
         * @see EnDecrypt.AES#decrypt(byte[])
         *
         * @since 1.0.0
         */
        public String decrypt(String string) throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
            return new String(decrypt(string.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        }

    }
}
