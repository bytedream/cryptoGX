/**
 *
 * @author blueShard
 * @version 1.11.0
 */

package org.blueshard.cryptogx;

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

public class Main extends Application {

    protected static final int NON_PORTABLE = 414729643;
    protected static final int PORTABLE = 245714766;

    protected static final int TYPE = PORTABLE;

    protected final static String configDefaultName = "";
    protected final static String configDefaultEncryptHash = "";
    protected final static String configDefaultTextKey = "";
    protected final static String configDefaultTextSalt = "";
    protected final static String configDefaultTextAlgorithm = "AES";
    protected final static String configDefaultFileEnDecryptKey = "";
    protected final static String configDefaultFileEnDecryptSalt = "";
    protected final static String configDefaultFileEnDecryptAlgorithm = "AES";
    protected final static int configDefaultFileDeleteIterations = 5;
    protected final static String configDefaultFileOutputPath = "";
    protected final static boolean configDefaultRemoveFileFromFileBox = false;
    protected final static boolean configDefaultLimitNumberOfThreads = true;

    protected static ArrayList<String> textAlgorithms = new ArrayList<>();
    protected static ArrayList<String> fileEnDecryptAlgorithms = new ArrayList<>();

    private static Stage mainStage;
    private double rootWindowX, rootWindowY;
    protected static File config;
    protected static boolean isConfig;

    /**
     * <p>Start the GUI</p>
     *
     * @param primaryStage of the GUI
     * @throws IOException if issues with loading 'mainGUI.fxml'
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
        Scene scene = new Scene(root, 900, 470);

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
     * @return status
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException if wrong algorithm is given (command line)
     * @throws IllegalBlockSizeException if wrong size for key is given (command line)
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException if invalid key is given (command line)
     * @throws InvalidKeySpecException
     * @throws IOException if files cannot be en- / decrypted or deleted correctly (command line)
     * @throws InvalidAlgorithmParameterException if wrong algorithm parameters are given (command line)
     */
    public static void main(String[] args) throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, IOException, InvalidAlgorithmParameterException {
        if (Main.TYPE == Main.NON_PORTABLE) {
            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                config = new File("C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Roaming\\cryptoGX\\cryptoGX.config");
            } else {
                config = new File("cryptoGX.config");
            }
        } else {
            config = new File("cryptoGX.config");
        }
        isConfig = config.isFile();
        if (args.length == 0) {
            String version = Runtime.class.getPackage().getImplementationVersion();
            if (version.startsWith("1.")) {
                if (Integer.parseInt(version.substring(2, 3)) < 8) {
                    System.out.println("1");
                    JOptionPane.showMessageDialog(null, "Please use java 1.8.0_240 to java 10.*", "ERROR", JOptionPane.ERROR_MESSAGE);
                } else if (Integer.parseInt(version.substring(6, 9)) < 240) {
                    JOptionPane.showMessageDialog(null, "Please use java 1.8.0_240 to java 10.*", "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            } else if (Integer.parseInt(version.substring(0, 2)) > 10) {
                    JOptionPane.showMessageDialog(null, "Please use java 1.8.0_240 to java 10.*", "ERROR", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Please use java 1.8.0_240 to java 10.*", "ERROR", JOptionPane.ERROR_MESSAGE);
                }
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
                        "File secure delete: <iterations> <path of file to delete>");
            } else if (args[0].toLowerCase().equals("delete")) {
                if (args.length > 3) {
                    System.err.println("To many arguments were given, expected 3");
                } else if (args.length < 3) {
                    System.err.println("To few arguments were given, expected 3");
                }
                try {
                    SecureDelete.deleteFileLineByLine(args[2], Integer.parseInt(args[1]));
                } catch (NumberFormatException e) {
                    System.err.println(args[1] + " must be a number\n Error: " + e.getMessage());
                }
            } else if (args[0].toLowerCase().equals("aes")) {
                if (args.length < 4) {
                    System.err.println("To few arguments were given");
                    System.exit(1);
                }
                EnDecrypt.AES aes;
                if (args[2].isEmpty()) {
                    aes = new EnDecrypt.AES(args[1], new byte[16]);
                } else {
                    aes = new EnDecrypt.AES(args[1], args[2].getBytes(StandardCharsets.UTF_8));
                }
                String type = args[3].toLowerCase();
                if (type.equals("encrypt")) {
                    System.out.println(aes.encrypt(args[4]));
                } else if (type.equals("decrypt")) {
                    System.out.println(aes.decrypt(args[4]));
                } else if (type.equals("fileencrypt") || type.equals("encryptfile")) {
                    aes.encryptFileLineByLine(args[4], args[5]);
                } else if (type.equals("filedecrypt") ||type.equals("decryptfile")) {
                    aes.decryptFileLineByLine(args[4], args[5]);
                }
            }
        }
        System.exit(0);
    }

    /**
     * <p>"Catch" all uncatched exceptions and opens an alert window</p>
     *
     * @param thread which called this method
     * @param throwable of the thread which called the method
     */
    private static void exceptionAlert(Thread thread, Throwable throwable) {
        throwable.printStackTrace();

        AtomicReference<Double> exceptionAlertX = new AtomicReference<>(Screen.getPrimary().getBounds().getMaxX() / 2);
        AtomicReference<Double> exceptionAlertY = new AtomicReference<>(Screen.getPrimary().getBounds().getMaxY() / 2);

        Alert enDecryptError = new Alert(Alert.AlertType.ERROR, "Error: " + throwable, ButtonType.OK);
        enDecryptError.initStyle(StageStyle.UNDECORATED);
        enDecryptError.setTitle("Error");
        ((Stage) enDecryptError.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Main.class.getResource("resources/cryptoGX.png").toExternalForm()));

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
        ((Stage) enDecryptError.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Main.class.getResource("resources/cryptoGX.png").toExternalForm()));

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
     */
    protected static void warningAlert(String message) {
        AtomicReference<Double> alertX = new AtomicReference<>(Screen.getPrimary().getBounds().getMaxX() / 2);
        AtomicReference<Double> alertY = new AtomicReference<>(Screen.getPrimary().getBounds().getMaxY() / 2);

        Alert enDecryptError = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        enDecryptError.initStyle(StageStyle.UNDECORATED);
        enDecryptError.setTitle("Error");
        ((Stage) enDecryptError.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Main.class.getResource("resources/cryptoGX.png").toExternalForm()));

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
