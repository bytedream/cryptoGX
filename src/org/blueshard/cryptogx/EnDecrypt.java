package org.blueshard.cryptogx;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

public class EnDecrypt {

    public static class AES extends Thread {

        private int iterations = 1000;
        private int keyLength = 256;

        private final String key;
        private final byte[] salt;

        public AES(String key, byte[] salt) {
            this.key = key;
            this.salt = salt;
        }

        public AES(String key, byte[] salt, int iterations) {
            this.key = key;
            this.salt = salt;
            this.iterations = iterations;
        }

        public AES(String key, byte[] salt, int iterations, int keyLength) {
            this.key = key;
            this.salt = salt;
            this.iterations = iterations;
            this.keyLength = keyLength;
        }

        /**
         * <p>Creates a secret key from given (plain text) key and salt</p>
         *
         * @param key from which a secret key should be created
         * @param salt from which a secret key should be created
         * @return the secret key
         * @throws NoSuchAlgorithmException
         * @throws InvalidKeySpecException
         */
        public byte[] createSecretKey(String key, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
            KeySpec keySpec = new PBEKeySpec(key.toCharArray(), salt, this.iterations, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

            return factory.generateSecret(keySpec).getEncoded();
        }

        /**
         * <p>Writes {@param inputStream} to {@param outputStream}</p>
         *
         * @param inputStream from which is written
         * @param outputStream to which is written
         * @param buffer
         * @throws IOException
         */
        public static void writeLineByLine(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws IOException {
            int numOfBytesRead;
            while ((numOfBytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, numOfBytesRead);
            }
            outputStream.close();
            inputStream.close();
        }

        /**
         * <p>Encrypts a file randomly line by line</p>
         *
         * @see EnDecrypt.AES#encryptRandomLineByLine(InputStream, OutputStream, byte[])
         */
        public static void encryptFileRandomLineByLine(File inputFile, File outputFile, byte[] buffer) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidAlgorithmParameterException {
            encryptRandomLineByLine(new FileInputStream(inputFile), new FileOutputStream(outputFile), buffer);
        }

        /**
         * <p>Encrypts a {@link InputStream} randomly line by line</p>
         *
         * @param inputStream that should be encrypted
         * @param outputStream to which the encrypted {@param inputStream} should be written to
         * @param buffer
         * @throws NoSuchAlgorithmException
         * @throws NoSuchPaddingException
         * @throws InvalidKeyException
         * @throws IOException
         * @throws InvalidAlgorithmParameterException
         */
        public static void encryptRandomLineByLine(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidAlgorithmParameterException {
            KeyGenerator randomKey = KeyGenerator.getInstance("AES");
            Key secretKey = new SecretKeySpec(randomKey.generateKey().getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
            writeLineByLine(cipherInputStream, outputStream, buffer);
        }

        /**
         * <p>En- / decrypts the {@param inputFile}</p>
         *
         * @param cipherMode says if the file should be en- or decrypted
         * @param inputFile that should be en- / decrypted
         * @param outputFile to which the en- / decrypted {@param inputFile} should be written to
         * @throws InvalidKeySpecException
         * @throws NoSuchAlgorithmException
         * @throws NoSuchPaddingException
         * @throws InvalidKeyException
         * @throws IOException
         * @throws BadPaddingException
         * @throws IllegalBlockSizeException
         */
        public void enDecryptFileAllInOne(int cipherMode, File inputFile, File outputFile) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException {
            Key secretKey = new SecretKeySpec(createSecretKey(key, salt), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKey);

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();
        }

        /**
         * <p>En- / decrypts the {@param inputBytes}</p>
         *
         * @param cipherMode says if the file should be en- or decrypted
         * @param inputBytes that should be en- / decrypted
         * @param outputStream to which the en- / decrypted {@param inputFile} should be written to
         * @throws InvalidKeySpecException
         * @throws NoSuchAlgorithmException
         * @throws NoSuchPaddingException
         * @throws InvalidKeyException
         * @throws IOException
         * @throws BadPaddingException
         * @throws IllegalBlockSizeException
         */
        public void enDecryptFileAllInOne(int cipherMode, byte[] inputBytes, OutputStream outputStream) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException {
            Key secretKey = new SecretKeySpec(createSecretKey(key, salt), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKey);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            outputStream.write(outputBytes);

            outputStream.close();
        }

        /**
         * <p>En- / decrypts the {@param inputFile}</p>
         *
         * @param cipherMode says if the file should be en- or decrypted
         * @param inputFile that should be en- / decrypted
         * @param outputFile to which the en- / decrypted {@param inputFile} should be written to
         * @param buffer
         * @throws InvalidKeySpecException
         * @throws NoSuchAlgorithmException
         * @throws NoSuchPaddingException
         * @throws InvalidKeyException
         * @throws IOException
         * @throws InvalidAlgorithmParameterException
         */
        public void enDecryptLineByLine(int cipherMode, File inputFile, File outputFile, byte[] buffer) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidAlgorithmParameterException {
            Key secretKey = new SecretKeySpec(createSecretKey(key, salt), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKey);

            FileInputStream fileInputStream = new FileInputStream(inputFile);
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

            if (cipherMode == Cipher.ENCRYPT_MODE) {
                CipherInputStream cipherInputStream = new CipherInputStream(fileInputStream, cipher);
                writeLineByLine(cipherInputStream, fileOutputStream, buffer);
            } else if (cipherMode == Cipher.DECRYPT_MODE) {
                CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, cipher);
                writeLineByLine(fileInputStream, cipherOutputStream, buffer);
            }
        }

        /**
         * <p>En- / decrypts the {@param inputStream}</p>
         *
         * @param cipherMode says if the file should be en- or decrypted
         * @param inputStream that should be en- / decrypted
         * @param outputStream to which the en- / decrypted {@param inputFile} should be written to
         * @param buffer
         * @throws InvalidKeySpecException
         * @throws NoSuchAlgorithmException
         * @throws NoSuchPaddingException
         * @throws InvalidKeyException
         * @throws IOException
         * @throws InvalidAlgorithmParameterException
         */
        public void enDecryptLineByLine(int cipherMode, InputStream inputStream, OutputStream outputStream, byte[] buffer) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidAlgorithmParameterException {
            Key secretKey = new SecretKeySpec(createSecretKey(key, salt), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKey);

            if (cipherMode == Cipher.ENCRYPT_MODE) {
                CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
                writeLineByLine(cipherInputStream, outputStream, buffer);
            } else if (cipherMode == Cipher.DECRYPT_MODE) {
                CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
                writeLineByLine(inputStream, cipherOutputStream, buffer);
            }
        }

        /**
         * <p>Encrypt {@param bytes} randomly</p>
         *
         * @see EnDecrypt.AES#encryptRandom(byte[])
         */
        public static String encryptRandom(String string) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
            return encryptRandom(string.getBytes(StandardCharsets.UTF_8));
        }

        /**
         * <p>Encrypt {@param bytes} randomly</p>
         *
         * @param bytes that should be encrypted
         * @return the encrypted {@param bytes} as {@link String}
         * @throws NoSuchPaddingException
         * @throws NoSuchAlgorithmException
         * @throws InvalidKeyException
         * @throws BadPaddingException
         * @throws IllegalBlockSizeException
         */
        public static String encryptRandom(byte[] bytes) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
            KeyGenerator randomKey = KeyGenerator.getInstance("AES");
            Key secretKey = new SecretKeySpec(randomKey.generateKey().getEncoded(), "AES");

            Cipher encryptRandomCipher = Cipher.getInstance("AES");
            encryptRandomCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(encryptRandomCipher.doFinal(bytes));
        }

        /**
         * <p>Encrypts a file randomly at once</p>
         *
         * @see EnDecrypt.AES#encryptFileRandomAllInOne(File, File)
         */
        public static void encryptFileRandomAllInOne(String inputFilename, String outputFilename) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
            encryptFileRandomAllInOne(new File(inputFilename), new File(outputFilename));
        }

        /**
         * <p>Encrypts a file randomly at once</p>
         *
         * @param inputFile that should be encrypted
         * @param outputFile to which the encrypted file should be written to
         * @throws NoSuchAlgorithmException
         * @throws NoSuchPaddingException
         * @throws InvalidKeyException
         * @throws IOException
         * @throws BadPaddingException
         * @throws IllegalBlockSizeException
         */
        public static void encryptFileRandomAllInOne(File inputFile, File outputFile) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException {
            KeyGenerator randomKey = KeyGenerator.getInstance("AES");
            Key secretKey = new SecretKeySpec(randomKey.generateKey().getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();
        }

        /**
         * <p>Encrypts {@param inputFilename} randomly line by line and write it to {@param outputFilename}</p>
         *
         * @see EnDecrypt.AES#encryptRandomLineByLine(InputStream, OutputStream, byte[])
         */
        public static void encryptFileRandomLineByLine(String inputFilename, String outputFilename) throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IOException {
            encryptRandomLineByLine(new FileInputStream(inputFilename), new FileOutputStream(outputFilename), new byte[64]);
        }

        /**
         * <p>Encrypts {@param inputFilename} randomly line by line and write it to {@param outputFilename}</p>
         *
         * @see EnDecrypt.AES#encryptRandomLineByLine(InputStream, OutputStream, byte[])
         */
        public static void encryptFileRandomLineByLine(String inputFilename, String outputFilename, byte[] buffer) throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IOException {
            encryptRandomLineByLine(new FileInputStream(inputFilename), new FileOutputStream(outputFilename), buffer);
        }

        /**
         * <p>Decrypt encrypted {@param encryptedString}</p>
         *
         * @see EnDecrypt.AES#decrypt(byte[])
         */
        public String decrypt(String encryptedString) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
            Key secretKey = new SecretKeySpec(createSecretKey(key, salt), "AES");

            Cipher decryptCipher = Cipher.getInstance("AES");
            decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(decryptCipher.doFinal(Base64.getDecoder().decode(encryptedString)), StandardCharsets.UTF_8);
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
         */
        public byte[] decrypt(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
            return decrypt(Arrays.toString(bytes)).getBytes(StandardCharsets.UTF_8);
        }

        /**
         * <p>Encrypt {@param bytes}</p>
         *
         * @see EnDecrypt.AES#encrypt(byte[])
         */
        public String encrypt(String string) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
            return Base64.getEncoder().encodeToString(encrypt(string.getBytes(StandardCharsets.UTF_8)));
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
         */
        public byte[] encrypt(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
            Key secretKey = new SecretKeySpec(createSecretKey(key, salt), "AES");

            Cipher encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return encryptCipher.doFinal(bytes);
        }

        /**
         * <p>Decrypt encrypted {@param inputFilename} to {@param outputFilename} at once</p>
         *
         * @param inputFilename that should be decrypted
         * @param outputFilename to which the decrypted content should be written to
         * @throws NoSuchPaddingException
         * @throws NoSuchAlgorithmException
         * @throws IOException
         * @throws BadPaddingException
         * @throws IllegalBlockSizeException
         * @throws InvalidKeyException
         * @throws InvalidKeySpecException
         */
        public void decryptFileAllInOne(String inputFilename, String outputFilename) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException {
            enDecryptFileAllInOne(Cipher.DECRYPT_MODE, new File(inputFilename), new File(outputFilename));
        }

        /**
         * <p>Decrypt encrypted {@param inputBytes} to {@param outputStream} at once</p>
         *
         * @param inputBytes that should be decrypted
         * @param outputStream to which the decrypted content should be written to
         * @throws NoSuchPaddingException
         * @throws NoSuchAlgorithmException
         * @throws IOException
         * @throws BadPaddingException
         * @throws IllegalBlockSizeException
         * @throws InvalidKeyException
         * @throws InvalidKeySpecException
         */
        public void decryptFileAllInOne(byte[] inputBytes, OutputStream outputStream) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException {
            enDecryptFileAllInOne(Cipher.DECRYPT_MODE, inputBytes, outputStream);
        }

        /**
         * <p>Decrypt encrypted {@param inputFilename} to {@param outputFilename} line by line</p>
         *
         * @see EnDecrypt.AES#decryptFileLineByLine(InputStream, OutputStream, byte[])
         */
        public void decryptFileLineByLine(String inputFilename, String outputFilename) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeyException, InvalidKeySpecException {
            enDecryptLineByLine(Cipher.DECRYPT_MODE, new File(inputFilename), new File(outputFilename), new byte[64]);
        }

        /**
         * <p>Decrypt encrypted {@param inputStream} to {@param outputStream} line by line</p>
         *
         * @see EnDecrypt.AES#decryptFileLineByLine(InputStream, OutputStream, byte[])
         */
        public void decryptFileLineByLine(InputStream inputStream, OutputStream outputStream) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeyException, InvalidKeySpecException {
            enDecryptLineByLine(Cipher.DECRYPT_MODE, inputStream, outputStream, new byte[64]);
        }

        /**
         * <p>Decrypt encrypted {@param inputFilename} to {@param outputFilename} line by line</p>
         *
         * @see EnDecrypt.AES#decryptFileLineByLine(InputStream, OutputStream, byte[])
         */
        public void decryptFileLineByLine(String inputFilename, String outputFilename, byte[] buffer) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeyException, InvalidKeySpecException {
            enDecryptLineByLine(Cipher.DECRYPT_MODE, new File(inputFilename), new File(outputFilename), buffer);
        }

        /**
         * <p>Decrypt encrypted {@param inputStream} to {@param outputStream} line by line</p>
         *
         * @param inputStream that should be decrypted
         * @param outputStream to which the decrypted content should be written to
         * @param buffer
         * @throws NoSuchPaddingException
         * @throws InvalidAlgorithmParameterException
         * @throws NoSuchAlgorithmException
         * @throws IOException
         * @throws InvalidKeyException
         * @throws InvalidKeySpecException
         */
        public void decryptFileLineByLine(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeyException, InvalidKeySpecException {
            enDecryptLineByLine(Cipher.DECRYPT_MODE, inputStream, outputStream, buffer);
        }

        /**
         * <p>DEncrypt {@param inputFilename} to {@param outputFilename} at once</p>
         *
         * @param inputFilename that should be encrypt
         * @param outputFilename to which the encrypted content should be written to
         * @throws NoSuchPaddingException
         * @throws NoSuchAlgorithmException
         * @throws IOException
         * @throws BadPaddingException
         * @throws IllegalBlockSizeException
         * @throws InvalidKeyException
         * @throws InvalidKeySpecException
         */
        public void encryptFileAllInOne(String inputFilename, String outputFilename) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException {
            enDecryptFileAllInOne(Cipher.ENCRYPT_MODE, new File(inputFilename), new File(outputFilename));
        }

        /**
         * <p>Encrypt {@param inputBytes} to {@param outputStream} at once</p>
         *
         * @param inputBytes that should be encrypted
         * @param outputStream to which the encrypted content should be written to
         * @throws NoSuchPaddingException
         * @throws NoSuchAlgorithmException
         * @throws IOException
         * @throws BadPaddingException
         * @throws IllegalBlockSizeException
         * @throws InvalidKeyException
         * @throws InvalidKeySpecException
         */
        public void encryptFileAllInOne(byte[] inputBytes, OutputStream outputStream) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException {
            enDecryptFileAllInOne(Cipher.ENCRYPT_MODE, inputBytes, outputStream);
        }

        /**
         * <p>Encrypt {@param inputFilename} to {@param outputFilename} line by line</p>
         *
         * @see EnDecrypt.AES#encryptFileLineByLine(InputStream, OutputStream, byte[])
         */
        public void encryptFileLineByLine(String inputFilename, String outputFilename) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeyException, InvalidKeySpecException {
            enDecryptLineByLine(Cipher.ENCRYPT_MODE, new File(inputFilename), new File(outputFilename), new byte[64]);
        }

        /**
         * <p>Encrypt {@param inputStream} to {@param outputStream} line by line</p>
         *
         * @see EnDecrypt.AES#encryptFileLineByLine(InputStream, OutputStream, byte[])
         */
        public void encryptFileLineByLine(InputStream inputStream, OutputStream outputStream) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeyException, InvalidKeySpecException {
            enDecryptLineByLine(Cipher.ENCRYPT_MODE, inputStream, outputStream, new byte[64]);
        }

        /**
         * <p>Encrypt {@param inputFilename} to {@param outputFilename} line by line</p>
         *
         * @see EnDecrypt.AES#encryptFileLineByLine(InputStream, OutputStream, byte[])
         */
        public void encryptFileLineByLine(String inputFilename, String outputFilename, byte[] buffer) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeyException, InvalidKeySpecException {
            enDecryptLineByLine(Cipher.ENCRYPT_MODE, new File(inputFilename), new File(outputFilename), buffer);
        }

        /**
         * <p>Encrypt {@param inputStream} to {@param outputStream} line by line</p>
         *
         * @param inputStream that should be encrypted
         * @param outputStream to which the encrypted {@param inputStream} should be written to
         * @param buffer
         * @throws NoSuchPaddingException
         * @throws InvalidAlgorithmParameterException
         * @throws NoSuchAlgorithmException
         * @throws IOException
         * @throws InvalidKeyException
         * @throws InvalidKeySpecException
         */
        public void encryptFileLineByLine(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, InvalidKeyException, InvalidKeySpecException {
            enDecryptLineByLine(Cipher.ENCRYPT_MODE, inputStream, outputStream, buffer);
        }

    }
}
