/*
 * @author bytedream
 * @version 1.12.0
 *
 * Some <code>@since</code> versions may be not correct, because the <code>@since</code> tag got added in
 * version 1.12.0 and I don't have all versions (1.0.0 - 1.11.0), so I cannot see when some methods were added

 */

package org.bytedream.cryptogx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Main class<p/>
 *
 * @since 1.0.0
 */
public class Main extends Application {

    protected static final int NON_PORTABLE = 1;
    protected static final int PORTABLE = 0;

    protected static final int TYPE = NON_PORTABLE;

    protected final static String configDefaultTextKey = "";
    protected final static String configDefaultTextSalt = "";
    protected final static String configDefaultTextAlgorithm = "AES-128";
    protected final static String configDefaultFileEnDecryptKey = "";
    protected final static String configDefaultFileEnDecryptSalt = "";
    protected final static String configDefaultFileEnDecryptAlgorithm = "AES-128";
    protected final static int configDefaultFileDeleteIterations = 5;
    protected final static String configDefaultFileOutputPath = "";
    protected final static boolean configDefaultRemoveFileFromFileBox = false;
    protected final static boolean configDefaultLimitNumberOfThreads = true;

    private final static byte[] buffer = new byte[64];

    private static Stage mainStage;
    private double rootWindowX, rootWindowY;
    protected static File config;
    protected static boolean isConfig;

    /**
     * <p>Start the GUI</p>
     *
     * @param primaryStage of the GUI
     * @throws IOException if issues with loading 'mainGUI.fxml'
     *
     * @since 1.0.0
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler(Main::exceptionAlert);

        mainStage = primaryStage;

        Parent root = FXMLLoader.load(getClass().getResource("resources/mainGUI.fxml"));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setResizable(false);
        primaryStage.setTitle("cryptoGX");
        primaryStage.getIcons().add(new Image(getClass().getResource("resources/cryptoGX.png").toExternalForm()));
        Scene scene = new Scene(root);
        //Scene scene = new Scene(root, 900, 470);

        scene.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() + rootWindowX);
            primaryStage.setY(event.getScreenY() + rootWindowY);
        });
        scene.setOnMousePressed(event -> {
            rootWindowX = scene.getX() - event.getSceneX();
            rootWindowY = scene.getY() - event.getSceneY();
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * <p>Enter method for the application.
     * Can also be used to en- / decrypt text and files or secure delete files without starting GUI</p>
     *
     * @param args from the command line
     * @return
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException if wrong algorithm is given (command line)
     * @throws IllegalBlockSizeException if wrong size for key is given (command line)
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException if invalid key is given (command line)
     * @throws InvalidKeySpecException
     * @throws IOException if files cannot be en- / decrypted or deleted correctly (command line)
     * @throws InvalidAlgorithmParameterException if wrong algorithm parameters are given (command line)
     *
     * @since 1.0.0
     */
    public static void main(String[] args) throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, IOException, InvalidAlgorithmParameterException {
        if (Main.TYPE == Main.PORTABLE) {
            String system = System.getProperty("os.name").toLowerCase();
            if (system.startsWith("windows")) {
                config = new File("C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Roaming\\cryptoGX\\cryptoGX.config");
                File directory = new File("C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Roaming\\cryptoGX");
                if (!directory.isDirectory()) {
                    directory.mkdir();
                }
            } else if (system.startsWith("linux")) {
                config = new File(System.getProperty("user.home") + "/.cryptoGX/cryptoGX.config");
                File directory = new File(System.getProperty("user.home") + "/.cryptoGX/");
                if (!directory.isDirectory()) {
                    directory.mkdir();
                }
            } else {
                config = new File("cryptoGX.config");
            }
        } else {
            config = new File("cryptoGX.config");
        }
        isConfig = config.isFile();
        if (args.length == 0) {
            launch(args);
        } else {
            args[0] = args[0].replace("-", "");
            if (args[0].toLowerCase().equals("help") || args[0].toUpperCase().equals("H")) {
                System.out.println("Usage AES: \n\n" +
                        "    Text en- / decryption\n" +
                        "        encrypt: <cryptoGX jar file> AES <key> <salt> encrypt <string>\n" +
                        "        decrypt: <cryptoGX jar file> AES <key> <salt> decrypt <encrypted string>\n\n" +
                        "    File en- / decryption\n" +
                        "        encrypt: <cryptoGX jar file> AES <key> <salt> encrypt <path of file to encrypt> <encrypted file dest>\n" +
                        "        decrypt: <cryptoGX jar file> AES <key> <salt> decrypt <encrypted file path> <decrypted file dest>\n\n" +
                        "File secure delete: <cryptoGX jar file> delete <iterations> <path of file to delete>"); //for <iterations> the argument 'default' can be used, which is 5
            } else if (args[0].toLowerCase().equals("delete")) {
                if (args.length > 3) {
                    System.err.println("To many arguments were given, expected 3");
                } else if (args.length < 3) {
                    System.err.println("To few arguments were given, expected 3");
                }
                try {
                    if (args[1].equals("default")) {
                        args[1] = "5";
                    }
                    File deleteFile = new File(args[2]);
                    if (deleteFile.isFile()) {
                        SecureDelete.deleteFile(deleteFile, Integer.parseInt(args[1]), buffer);
                    } else if (deleteFile.isDirectory()) {
                        SecureDelete.deleteDirectory(args[2], Integer.parseInt(args[1]), buffer);
                    } else {
                        System.err.println("Couldn't find file " + args[4]);
                        System.exit(1);
                    }
                } catch (NumberFormatException e) {
                    System.err.println(args[1] + " must be a number\n Error: " + e.getMessage());
                }
            } else if (args[0].toLowerCase().equals("aes")) {
                if (args.length < 5) {
                    System.err.println("To few arguments were given");
                    System.exit(1);
                } else if (args.length > 6) {
                    System.err.println("To many arguments were given");
                    System.exit(1);
                }
                EnDecrypt.AES aes;
                if (args[2].isEmpty()) {
                    aes = new EnDecrypt.AES(args[1], new byte[16]);
                } else {
                    aes = new EnDecrypt.AES(args[1], args[2].getBytes(StandardCharsets.UTF_8));
                }
                String type = args[3].toLowerCase();
                if (args.length == 5) {
                    if (type.equals("encrypt")) {
                        System.out.println(Base64.getEncoder().encodeToString(aes.encrypt(args[4].getBytes(StandardCharsets.UTF_8))));
                    } else if (type.equals("decrypt")) {
                        System.out.println(aes.decrypt(args[4]));
                    } else {
                        System.err.println("Couldn't resolve argument " + args[3] + ", expected 'encrypt' or 'decrypt'");
                        System.exit(1);
                    }
                } else {
                    if (type.equals("encrypt")) {
                        File inputFile = new File(args[4]);
                        if (inputFile.isFile()) {
                            aes.encryptFile(new FileInputStream(inputFile), new FileOutputStream(args[5]), new byte[64]);
                        } else if (inputFile.isDirectory()) {
                            aes.encryptDirectory(args[4], args[5], ".cryptoGX", new byte[64]);
                        } else {
                            System.err.println("Couldn't find file " + args[4]);
                            System.exit(1);
                        }
                    } else if (type.equals("decrypt")) {
                        File inputFile = new File(args[4]);
                        if (inputFile.isFile()) {
                            aes.decryptFile(new FileInputStream(inputFile), new FileOutputStream(args[5]), new byte[64]);
                        } else if (inputFile.isDirectory()) {
                            aes.decryptDirectory(args[4], args[5], "@.cryptoGX@", new byte[64]);
                        } else {
                            System.err.println("Couldn't find file " + args[4]);
                            System.exit(1);
                        }
                    } else {
                        System.err.println("Couldn't resolve argument " + args[3] + ", expected 'encrypt' or 'decrypt'");
                        System.exit(1);
                    }
                }
                System.exit(0);
            }
        }
    }

    /**
     * <p>"Catch" all uncatched exceptions and opens an alert window</p>
     *
     * @param thread which called this method
     * @param throwable of the thread which called the method
     *
     * @since 1.3.0
     */
    private static void exceptionAlert(Thread thread, Throwable throwable) {
        throwable.printStackTrace();

        AtomicReference<Double> exceptionAlertX = new AtomicReference<>(Screen.getPrimary().getBounds().getMaxX() / 2);
        AtomicReference<Double> exceptionAlertY = new AtomicReference<>(Screen.getPrimary().getBounds().getMaxY() / 2);

        Alert enDecryptError = new Alert(Alert.AlertType.ERROR, "Error: " + throwable, ButtonType.OK);
        enDecryptError.initStyle(StageStyle.UNDECORATED);
        enDecryptError.setTitle("Error");
        enDecryptError.setResizable(true);
        ((Stage) enDecryptError.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Main.class.getResource("resources/cryptoGX.png").toExternalForm()));
        enDecryptError.getDialogPane().setContent(new Label("Error: " + throwable));

        Scene window = enDecryptError.getDialogPane().getScene();

        window.setOnMouseDragged(dragEvent -> {
            enDecryptError.setX(dragEvent.getScreenX() + exceptionAlertX.get());
            enDecryptError.setY(dragEvent.getScreenY() + exceptionAlertY.get());
        });
        window.setOnMousePressed(pressEvent -> {
            exceptionAlertX.set(window.getX() - pressEvent.getSceneX());
            exceptionAlertY.set(window.getY() - pressEvent.getSceneY());
        });

        enDecryptError.show();
    }

    /**
     * <p>Shows an error alert window</p>
     *
     * @param message which will the alert show
     * @param error which will show after the message
     */
    protected static void errorAlert(String message, String error) {
        AtomicReference<Double> alertX = new AtomicReference<>(Screen.getPrimary().getBounds().getMaxX() / 2);
        AtomicReference<Double> alertY = new AtomicReference<>(Screen.getPrimary().getBounds().getMaxY() / 2);

        Alert enDecryptError = new Alert(Alert.AlertType.ERROR, message +
                "\nError: " + error, ButtonType.OK);
        enDecryptError.initStyle(StageStyle.UNDECORATED);
        enDecryptError.setTitle("Error");
        enDecryptError.setResizable(true);
        ((Stage) enDecryptError.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Main.class.getResource("resources/cryptoGX.png").toExternalForm()));
        enDecryptError.getDialogPane().setContent(new Label(message));

        Scene window = enDecryptError.getDialogPane().getScene();

        window.setOnMouseDragged(dragEvent -> {
            enDecryptError.setX(dragEvent.getScreenX() + alertX.get());
            enDecryptError.setY(dragEvent.getScreenY() + alertY.get());
        });
        window.setOnMousePressed(pressEvent -> {
            alertX.set(window.getX() - pressEvent.getSceneX());
            alertY.set(window.getY() - pressEvent.getSceneY());
        });

        enDecryptError.show();
    }

    /**
     * <p>Shows an warning alert window</p>
     *
     * @param message that the alert window will show
     *
     * @since 1.4.0
     */
    protected static void warningAlert(String message) {
        AtomicReference<Double> alertX = new AtomicReference<>(Screen.getPrimary().getBounds().getMaxX() / 2);
        AtomicReference<Double> alertY = new AtomicReference<>(Screen.getPrimary().getBounds().getMaxY() / 2);

        Alert enDecryptError = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        enDecryptError.initStyle(StageStyle.UNDECORATED);
        enDecryptError.setTitle("Error");
        ((Stage) enDecryptError.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Main.class.getResource("resources/cryptoGX.png").toExternalForm()));
        enDecryptError.getDialogPane().setContent(new Label(message));

        Scene window = enDecryptError.getDialogPane().getScene();

        window.setOnMouseDragged(dragEvent -> {
            enDecryptError.setX(dragEvent.getScreenX() + alertX.get());
            enDecryptError.setY(dragEvent.getScreenY() + alertY.get());
        });
        window.setOnMousePressed(pressEvent -> {
            alertX.set(window.getX() - pressEvent.getSceneX());
            alertY.set(window.getY() - pressEvent.getSceneY());
        });

        enDecryptError.show();
    }
}
