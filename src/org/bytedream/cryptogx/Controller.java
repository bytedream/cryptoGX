package org.bytedream.cryptogx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.bytedream.cryptogx.Settings.*;
import static org.bytedream.cryptogx.Main.*;

public class Controller implements Initializable {

    private Event fileEnDecryptLabelEvent;

    private double menubarX, menubarY;
    private boolean textLoading = false;
    private boolean fileEnDecryptLoading = false;
    private boolean fileDeleteLoading = false;
    private final AtomicInteger textThreads = new AtomicInteger(0);
    private final AtomicInteger totalThreads = new AtomicInteger(0);
    private final int tooltipShow = 15;
    private final int DATAFILEURL = 2;
    private final int FILEFILEURL = 1;
    private final int NONSPECIFICFILEURL = 0;
    private final byte[] buffer = new byte[64];
    private final KeyCombination paste = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
    private final Image loadingImage = new Image(getClass().getResource("resources/loading.gif").toExternalForm());

    private HashMap<String, String> currentConfigSettings = new HashMap<>();

    private final HashMap<Label, ArrayList<File>> enDecryptInputOutputFiles = new HashMap<>();
    private final HashMap<Label, ArrayList<Object>> enDecryptInputOutputInternetFiles = new HashMap<>();
    private final HashMap<Label, BufferedImage> enDecryptInputOutputClipboardImages = new HashMap<>();
    private final HashMap<Label, File> deleteInputFiles = new HashMap<>();
    private final List<Thread> fileEnDecryptThreads = Collections.synchronizedList(new ArrayList<>());
    private final List<Thread> fileDeleteThreads = Collections.synchronizedList(new ArrayList<>());

    private final ContextMenu fileEnDecryptInputContextMenu = new ContextMenu();
    private final ContextMenu fileDeleteInputContextMenu = new ContextMenu();
    private Label choosedLabel = null;
    private String choosedLabelType = null;
    private final MenuItem fileOutputFileChangeDest = new MenuItem("Change output file");
    private final MenuItem getChoosedLabelInputFileFolder = new MenuItem("Open source directory");
    private final MenuItem getChoosedLabelOutputFileFolder = new MenuItem("Open source directory");
    private final Tooltip tooltip = new Tooltip();

    public AnchorPane rootWindow;

    public Button fileEnDecryptFilesButton;
    public Button fileDecrypt;
    public Button fileEncrypt;
    public Button fileEnDecryptStop;

    public ComboBox<String> textAlgorithmBox;
    public ComboBox<String> fileEnDecryptAlgorithmBox;

    public ImageView minimizeWindow;
    public ImageView closeWindow;
    public ImageView textLoadingImage;
    public ImageView fileEnDecryptLoadingImage;
    public ImageView fileDeleteLoadingImage;

    public Menu settingsMenu;
    public Menu helpMenu;

    public MenuBar menubar;

    public MenuItem setDefaultOutputPath;
    public MenuItem saveSettings;
    public MenuItem loadSettings;
    public MenuItem exportSettings;
    public MenuItem importSettings;

    public RadioMenuItem removeFileFromFileBox;
    public RadioMenuItem limitNumberOfThreads;

    public ScrollPane fileEnDecryptInputScroll;

    public TextArea textDecryptedEntry;
    public TextArea textEncryptedEntry;

    public TextField textKeyEntry;
    public TextField textSaltEntry;
    public TextField fileEnDecryptKeyEntry;
    public TextField fileDecryptOutputFile;
    public TextField fileEncryptOutputFile;
    public TextField fileEnDecryptSaltEntry;
    public TextField fileDeleteIterationsEntry;

    public VBox fileEnDecryptInputFiles;
    public VBox fileDeleteInputFiles;

    //-----general-----//

    /**
     * <p>Shows a tooltip when the user type in some text in a text field, text area, etc. and the mouse is over this entry</p>
     *
     * @param event from which this method is called
     */
    public void keyTypedTooltip(KeyEvent event) {
        String id = null;
        String text = "";
        try {
            id = ((TextField) event.getSource()).getId();
            text = ((TextField) event.getSource()).getText() + event.getCharacter();
            tooltip.setText(text);
        } catch (ClassCastException e) {
            tooltip.setText(((TextArea) event.getSource()).getText() + event.getCharacter());
        }
        if (id != null) {
            switch (id) {
                case ("textKeyEntry"):
                    currentConfigSettings.replace("textKey", text);
                    break;
                case ("textSaltEntry"):
                    currentConfigSettings.replace("textSalt", text);
                    break;
                case ("fileEnDecryptKeyEntry"):
                    currentConfigSettings.replace("fileEnDecryptKey", text);
                    break;
                case ("fileEnDecryptSaltEntry"):
                    currentConfigSettings.replace("fileEnDecryptSalt", text);
                    break;
                case ("fileDeleteIterationsEntry"):
                    currentConfigSettings.replace("fileDeleteIterations", String.valueOf(Integer.parseInt(text)));
                    break;
            }
        }
    }

    /**
     * <p>Shows a tooltip when to mouse is over a text field, text area, etc.</p>
     *
     * @param event from which this method is called
     */
    public void mouseOverEntryTooltip(MouseEvent event) {
        try {
            tooltip.setText(((TextField) event.getSource()).getText());
        } catch (ClassCastException e) {
            try {
                tooltip.setText(((TextArea) event.getSource()).getText());
            } catch (ClassCastException ex) {
                tooltip.setText(((Label) event.getSource()).getText());
            }
        }
        if (!tooltip.getText().trim().isEmpty()) {
            tooltip.show(rootWindow.getScene().getWindow(), event.getScreenX(), event.getScreenY() + tooltipShow);
        }
    }

    /**
     * <p>Hides the tooltip if the mouse exit a text field, text area, etc.</p>
     */
    public void mouseExitEntryTooltip() {
        tooltip.hide();
    }

    //-----menu / close bar-----//

    /**
     * <p>Closed the application.
     * Get called if red close button is pressed</p>
     *
     * @since 1.0.0
     */
    public void closeApplication() {
        Stage rootStage = (Stage) rootWindow.getScene().getWindow();
        rootStage.close();
        System.exit(0);
    }

    /**
     * <p>Hides the application.
     * Get called if the green minimize button is pressed</p>
     *
     * @since 1.0.0
     */
    public void minimizeApplication() {
        Stage rootStage = (Stage) rootWindow.getScene().getWindow();
        rootStage.setIconified(true);
    }

    //-----text-----//

    /**
     * <p>Encrypt text in {@link Controller#textDecryptedEntry}.
     * Get called if the text 'Encrypt' button is pressed</p>
     *
     * @since 1.0.0
     */
    public void textEncryptButton() {
        final byte[] salt;
        if (!textSaltEntry.getText().isEmpty()) {
            salt = textSaltEntry.getText().getBytes(StandardCharsets.UTF_8);
        } else {
            salt = new byte[16];
        }
        if (!textLoading) {
            textLoadingImage.setImage(loadingImage);
        }
        Thread textEncrypt = new Thread(() -> {
            textThreads.getAndIncrement();
            if (limitNumberOfThreads.isSelected()) {
                while (totalThreads.get() >= Runtime.getRuntime().availableProcessors()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().stop();
                    }
                }
            }
            totalThreads.getAndIncrement();
            String textAlgorithm = textAlgorithmBox.getSelectionModel().getSelectedItem();
            EnDecrypt.AES encrypt = new EnDecrypt.AES(textKeyEntry.getText(), salt, Integer.parseInt(textAlgorithm.substring(textAlgorithm.indexOf('-') + 1)));
            try {
                String encryptedText = encrypt.encrypt(textDecryptedEntry.getText());
                Platform.runLater(() -> textEncryptedEntry.setText(encryptedText));
            } catch (NoSuchPaddingException | InvalidKeySpecException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException | IllegalBlockSizeException e) {
                e.printStackTrace();
                Platform.runLater(() -> errorAlert("Wrong text for encryption is given", e.getMessage()));
            }
            if ((textThreads.get() - 1) <= 0) {
                textLoadingImage.setImage(null);
                textLoading = false;
            }
            textThreads.getAndDecrement();
            totalThreads.getAndDecrement();
        });
        textEncrypt.setDaemon(false);
        textEncrypt.start();
        textLoading = true;
    }

    /**
     * <p>Decrypt text in {@link Controller#textEncryptedEntry}.
     * Get called if the text 'Decrypt' button is pressed</p>
     *
     * @since 1.0.0
     */
    public void textDecryptButton() {
        final byte[] salt;
        if (!textSaltEntry.getText().isEmpty()) {
            salt = textSaltEntry.getText().getBytes(StandardCharsets.UTF_8);
        } else {
            salt = new byte[16];
        }
        if (!textLoading) {
            textLoadingImage.setImage(loadingImage);
        }
        Thread textDecrypt = new Thread(() -> {
            textThreads.getAndIncrement();
            if (limitNumberOfThreads.isSelected()) {
                while (totalThreads.get() >= Runtime.getRuntime().availableProcessors()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().stop();
                    }
                }
            }
            totalThreads.getAndIncrement();
            String textAlgorithm = textAlgorithmBox.getSelectionModel().getSelectedItem();
            EnDecrypt.AES decrypt = new EnDecrypt.AES(textKeyEntry.getText(), salt, Integer.parseInt(textAlgorithm.substring(textAlgorithm.indexOf('-') + 1)));
            try {
                String DecryptedText = decrypt.decrypt(textEncryptedEntry.getText());
                Platform.runLater(() -> textDecryptedEntry.setText(DecryptedText));
            } catch (NoSuchPaddingException | InvalidKeySpecException | InvalidKeyException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
                Platform.runLater(() -> errorAlert("Wrong key and / or salt is given", e.getMessage()));
            } catch (IllegalArgumentException | IllegalBlockSizeException e) {
                e.printStackTrace();
                Platform.runLater(() -> errorAlert("Wrong text for decryption is given", e.getMessage()));
            }
            if ((textThreads.get() - 1) <= 0) {
                textLoading = false;
                Platform.runLater(() -> textLoadingImage.setImage(null));
            }
            textThreads.getAndDecrement();
            totalThreads.getAndDecrement();
        });
        textDecrypt.setDaemon(false);
        textDecrypt.start();
        textLoading = true;
    }

    //-----fileEnDecrypt-----//

    /**
     * <p>Synchronized method to get the list of threads which en- / decrypt files</p>
     *
     * @return list of en- / decryption threads
     */
    private synchronized List<Thread> getFileEnDecryptThreads() {
        return fileEnDecryptThreads;
    }

    /**
     * <p>Synchronized method to get the number of threads which en- / decrypt files</p>
     *
     * @return number of en- / decryption threads
     *
     * @since 1.2.0
     */
    private synchronized int getFileEnDecryptThreadsSize() {
        return fileEnDecryptThreads.size();
    }

    /**
     * <p>Synchronized method to add a thread to the file en- / decryption list of current running file en- / decryption threads</p>
     *
     * @param thread that should be added
     *
     * @since 1.2.0
     */
    private synchronized void addFileEnDecryptThread(Thread thread) {
        fileEnDecryptThreads.add(thread);
    }

    /**
     * <p>Synchronized method to remove a thread from the file en- / decryption list of current running file en- / decryption threads</p>
     *
     * @param thread that should be removed
     *
     * @since 1.2.0
     */
    private synchronized void removeFileEnDecryptThread(Thread thread) {
        fileEnDecryptThreads.remove(thread);
    }

    /**
     * <p>Adds a file for en- / decryption</p>
     *
     * @param file that should be added
     *
     * @since 1.0.0
     */
    private void fileEnDecryptAddFile(File file) {
        for (Label l: enDecryptInputOutputFiles.keySet()) {
            if (l.getText().equals(file.getAbsolutePath())) {
                return;
            }
        }
        Label newLabel = new Label(file.getAbsolutePath());
        newLabel.setOnKeyTyped(this::keyTypedTooltip);
        newLabel.setOnMouseMoved(this::mouseOverEntryTooltip);
        newLabel.setOnMouseExited(event -> mouseExitEntryTooltip());
        newLabel.setOnMouseClicked(event -> {
            fileEnDecryptSelected(newLabel);
            fileOutputFilesChangeText(newLabel, null, null);
            fileEnDecryptLabelEvent = event;
        });
        newLabel.setContextMenu(fileEnDecryptInputContextMenu);
        String fileAbsolutePath = file.getAbsolutePath();
        String fileName = file.getName();

        File encryptFile;
        File decryptFile;
        String fileOutputPath = file.getParent() + "/";;
        String fileEnding;
        ArrayList<File> inputOutputList = new ArrayList<>();
        if (!currentConfigSettings.get("fileOutputPath").trim().isEmpty()) {
            fileOutputPath = currentConfigSettings.get("fileOutputPath").trim() + "/";
        }
        if (file.isFile()) {
            fileEnding = ".cryptoGX";
        } else {
            fileEnding = "_cryptoGX";
        }
        encryptFile = new File(fileOutputPath + fileName + fileEnding);
        while (encryptFile.exists()) {
            encryptFile = new File(encryptFile.getAbsolutePath() + fileEnding);
        }
        if (fileAbsolutePath.endsWith(".cryptoGX") || fileAbsolutePath.endsWith("_cryptoGX")) {
            decryptFile = new File(fileOutputPath + fileName.substring(0, fileName.length() - 9));
        } else {
            decryptFile = new File(fileOutputPath + fileName + fileEnding);
        }
        while (decryptFile.exists()) {
            decryptFile = new File(decryptFile.getAbsolutePath() + fileEnding);
        }
        inputOutputList.add(0, encryptFile);
        inputOutputList.add(1, decryptFile);
        fileEnDecryptInputFiles.getChildren().add(newLabel);
        enDecryptInputOutputFiles.put(newLabel, inputOutputList);
    }

    /**
     * <p>Adds an file from the internet for en- / decryption</p>
     *
     * @param url of the file
     * @param fileType of the file
     * @throws URISyntaxException
     *
     * @since 1.5.0
     */
    private void fileEnDecryptAddInternetFile(String url, int fileType) throws URISyntaxException {
        String filename;
        switch (fileType) {
            case FILEFILEURL:
                filename = url.substring(url.lastIndexOf("/") + 1);
                break;
            case DATAFILEURL:
                filename = url.substring(5, url.indexOf("/")) + "." + url.substring(url.indexOf("/") + 1, url.indexOf(";"));
                break;
            case NONSPECIFICFILEURL:
                filename = "unknown" + System.nanoTime();
                break;
            default:
                warningAlert("Cannot read given url '" + url + "'");
                return;
        }
        for (Label l: enDecryptInputOutputInternetFiles.keySet()) {
            if (l.getText().equals(filename)) {
                return;
            }
        }
        Label newLabel = new Label(filename);
        newLabel.setOnKeyTyped(this::keyTypedTooltip);
        newLabel.setOnMouseMoved(this::mouseOverEntryTooltip);
        newLabel.setOnMouseExited(event -> mouseExitEntryTooltip());
        newLabel.setOnMouseClicked(event -> {
            fileEnDecryptSelected(newLabel);
            fileOutputFilesChangeText(newLabel, null, null);
            fileEnDecryptLabelEvent = event;
        });
        newLabel.setContextMenu(fileEnDecryptInputContextMenu);

        File encryptFile;
        File decryptFile;
        ArrayList<Object> fileSpecs = new ArrayList<>();
        ArrayList<File> inputOutputFiles = new ArrayList<>();
        fileSpecs.add(0, fileType);
        fileSpecs.add(1, url);
        String currentDir = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

        if (currentConfigSettings.get("fileOutputPath").trim().isEmpty()) {
            encryptFile = new File(currentDir + "/" + filename + ".cryptoGX");
            while (encryptFile.isFile()) {
                encryptFile = new File(encryptFile.getAbsolutePath() + ".cryptoGX");
            }
            if (url.endsWith(".cryptoGX") && filename.endsWith(".cryptoGX")) {
                decryptFile = new File(currentDir + "/" + filename.substring(0, filename.length() - 9));
            } else {
                decryptFile = new File(currentDir + "/" + filename);
            }
        } else {
            encryptFile = new File(currentConfigSettings.get("fileOutputPath") + "/" + filename + ".cryptoGX");
            while (encryptFile.isFile()) {
                encryptFile = new File(encryptFile.getAbsolutePath() + ".cryptoGX");
            }
            if (url.endsWith(".cryptoGX") && filename.endsWith(".cryptoGX")) {
                decryptFile = new File(currentConfigSettings.get("fileOutputPath") + "/" + filename.substring(0, filename.length() - 9));
            } else {
                decryptFile = new File(currentConfigSettings.get("fileOutputPath") + "/" + filename);
            }
        }
        while (decryptFile.isFile()) {
            decryptFile = new File(decryptFile.getAbsolutePath() + ".cryptoGX");
        }
        inputOutputFiles.add(0, encryptFile);
        inputOutputFiles.add(1, decryptFile);

        fileEnDecryptInputFiles.getChildren().add(newLabel);
        enDecryptInputOutputInternetFiles.put(newLabel, fileSpecs);
        enDecryptInputOutputFiles.put(newLabel, inputOutputFiles);
    }

    /**
     * <p>Adds an clipboard image for en- / decryption.
     * This can be a normal image and an image stream</p>
     *
     * @param image that should be added
     * @throws URISyntaxException
     *
     * @since 1.7.0
     */
    private void fileEnDecryptAddClipboardImage(BufferedImage image) throws URISyntaxException {
        String filename = "clipboardImage" + System.nanoTime() + ".png";
        for (Label l: enDecryptInputOutputClipboardImages.keySet()) {
            if (l.getText().equals(filename)) {
                return;
            }
        }
        Label newLabel = new Label(filename);
        newLabel.setOnKeyTyped(this::keyTypedTooltip);
        newLabel.setOnMouseMoved(this::mouseOverEntryTooltip);
        newLabel.setOnMouseExited(event -> mouseExitEntryTooltip());
        newLabel.setOnMouseClicked(event -> {
            fileEnDecryptSelected(newLabel);
            fileOutputFilesChangeText(newLabel, null, null);
            fileEnDecryptLabelEvent = event;
        });
        newLabel.setContextMenu(fileEnDecryptInputContextMenu);

        File encryptFile;
        File decryptFile;
        String currentDir = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        ArrayList<File> inputOutputFiles = new ArrayList<>();

        if (currentConfigSettings.get("fileOutputPath").trim().isEmpty()) {
            encryptFile = new File(currentDir + "/" + filename + ".cryptoGX");
            decryptFile = new File(currentDir + "/" + filename);
        } else {
            encryptFile = new File(currentConfigSettings.get("fileOutputPath").trim() + "/" + filename + ".cryptoGX");
            decryptFile = new File(currentConfigSettings.get("fileOutputPath").trim() + "/" + filename);
        }
        while (encryptFile.isFile()) {
            encryptFile = new File(encryptFile.getAbsolutePath() + ".cryptoGX");
        }
        while (decryptFile.isFile()) {
            decryptFile = new File(decryptFile.getAbsolutePath() + ".cryptoGX");
        }
        inputOutputFiles.add(0, encryptFile);
        inputOutputFiles.add(1, decryptFile);

        fileEnDecryptInputFiles.getChildren().add(newLabel);
        enDecryptInputOutputClipboardImages.put(newLabel, image);
        enDecryptInputOutputFiles.put(newLabel, inputOutputFiles);
    }

    /**
     * <p>Changes the text in the file en- / decryption output file text fields</p>
     *
     * @param label
     * @param encryptOutputFile is the filename of the file it gets encrypted
     * @param decryptOutputFile is the filename of the file it gets decrypted
     *
     * @since 1.2.0
     */
    private void fileOutputFilesChangeText(Label label, String encryptOutputFile, String decryptOutputFile) {
        File encryptFile;
        File decryptFile;
        ArrayList<File> change = new ArrayList<>();
        if (encryptOutputFile == null) {
            encryptFile = enDecryptInputOutputFiles.get(label).get(0);
        } else {
            encryptFile = new File(encryptOutputFile);
        }
        if (decryptOutputFile == null) {
            decryptFile = enDecryptInputOutputFiles.get(label).get(1);
        } else {
            decryptFile = new File(decryptOutputFile);
        }
        change.add(0, encryptFile);
        change.add(1, decryptFile);
        if (encryptFile.toString().trim().isEmpty()) {
            fileEncryptOutputFile.setText("");
        } else {
            fileEncryptOutputFile.setText(encryptFile.getAbsolutePath());
        }
        if (decryptFile.toString().trim().isEmpty()) {
            fileDecryptOutputFile.setText("");
        } else {
            fileDecryptOutputFile.setText(decryptFile.getAbsolutePath());
        }
        enDecryptInputOutputFiles.replace(label, change);
    }

    /**
     * <p>Deletes an entry for en- / decryption.
     * Get called if the user presses 'del' or delete the entry in the en- / decryption box via the right click tooltip</p>
     *
     * @param label that should be deleted
     *
     * @since 1.2.0
     */
    private void fileEnDecryptDeleteEntry(Label label) {
        enDecryptInputOutputFiles.remove(label);
        if (fileEnDecryptInputFiles.getChildren().size() - 1 >= 1) {
            for (int i = 0; i < fileEnDecryptInputFiles.getChildren().size(); i++) {
                if (fileEnDecryptInputFiles.getChildren().get(i) == label) {
                    fileEnDecryptInputFiles.getChildren().remove(label);
                    if (label == choosedLabel) {
                        try {
                            choosedLabel = (Label) fileEnDecryptInputFiles.getChildren().get(i - 1);
                            choosedLabelType = "ENDECRYPT";
                            fileOutputFilesChangeText(choosedLabel, null, null);
                            fileEnDecryptSelected(choosedLabel);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            fileOutputFileChangeDest.setDisable(true);
                            getChoosedLabelOutputFileFolder.setDisable(true);
                            fileEncryptOutputFile.setEditable(false);
                            fileDecryptOutputFile.setEditable(false);
                            fileOutputFilesChangeText(choosedLabel, "", "");
                            choosedLabel = null;
                            choosedLabelType = null;
                        }
                        break;
                    }
                }
            }
        } else {
            fileEnDecryptInputFiles.getChildren().remove(label);
            fileOutputFileChangeDest.setDisable(true);
            getChoosedLabelOutputFileFolder.setDisable(true);
            fileEncryptOutputFile.setEditable(false);
            fileDecryptOutputFile.setEditable(false);
            if (label == choosedLabel) {
                fileOutputFilesChangeText(choosedLabel, "", "");
                choosedLabel = null;
                choosedLabelType = null;
            }
        }
    }

    /**
     * <p>Changes the highlight of the clicked item in the en- / decryption box.
     * Get called if the user click an non-highlighted item in the en- / decryption box</p>
     *
     * @param changeLabel is the label that the user has clicked
     *
     * @since 1.0.0
     */
    private void fileEnDecryptSelected(Label changeLabel) {
        if (changeLabel != null) {
            fileDeleteSelected(null);
            enDecryptInputOutputFiles.keySet().forEach(label -> label.setStyle(null));
            changeLabel.setStyle("-fx-background-color: lightblue; -fx-border-color: #292929");
            fileDecryptOutputFile.setEditable(true);
            fileEncryptOutputFile.setEditable(true);
            fileOutputFileChangeDest.setDisable(false);
            getChoosedLabelOutputFileFolder.setDisable(false);
            choosedLabel = changeLabel;
            choosedLabelType = "ENDECRYPT";
        } else {
            enDecryptInputOutputFiles.keySet().forEach(label -> label.setStyle(null));
            fileDecryptOutputFile.setEditable(false);
            fileEncryptOutputFile.setEditable(false);
            fileOutputFileChangeDest.setDisable(true);
            getChoosedLabelOutputFileFolder.setDisable(true);
        }
    }

    /**
     * <p>Opens a file chooser GUI where the user can select the files that should be en- / decrypted.
     * Get called if the 'Choose files...' in the file en- / decrypt section button is pressed</p>
     *
     * @since 1.12.0
     */
    public void fileEnDecryptChooseFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose files");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
        List<File> files = fileChooser.showOpenMultipleDialog(rootWindow.getScene().getWindow());
        try {
            if (files.size() > 0) {
                files.forEach(this::fileEnDecryptAddFile);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Opens a directory chooser GUI where the user can select the directories that should be en- / decrypted.
     * Get called if the 'directories...' in the file en- / decrypt section button is pressed</p>
     *
     * @since 1.12.0
     */
    public void fileEnDecryptChooseDirectories() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directories");
        File file = directoryChooser.showDialog(rootWindow.getScene().getWindow());
        try {
            fileEnDecryptAddFile(file);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Get called if user drags a (normal or internet) file over the en- / decrypt file box</p>
     *
     * @param event source
     *
     * @since 1.2.0
     */
    public void onFileEnDecryptDragOver(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (event.getGestureSource() != fileEnDecryptInputFiles) {
            if (dragboard.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            } else if (dragboard.hasUrl()) {
                String url = dragboard.getUrl();
                String urlFilename = dragboard.getUrl().split("/")[dragboard.getUrl().split("/").length - 1];
                if (url.startsWith("data:")) {
                    try {
                        final int dataStartIndex = url.indexOf(",") + 1;
                        final String data = url.substring(dataStartIndex);
                        java.util.Base64.getDecoder().decode(data);
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (urlFilename.contains(".") && Utils.hasAnyCharacter("\\/:*?|<>\"", urlFilename)) {
                    try {
                        new URL(url);
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
            }
        }
    }

    /**
     * <p>Get called if the user drops the dragged (normal or internet) file over the en- / decrypt file box</p>
     *
     * @param event source
     * @throws URISyntaxException
     *
     * @since 1.2.0
     */
    public void onFileEnDecryptDragNDrop(DragEvent event) throws URISyntaxException {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles()) {
            dragboard.getFiles().forEach(this::fileEnDecryptAddFile);
        } else if (dragboard.hasUrl()) {
            String url = dragboard.getUrl();
            String urlFilename = dragboard.getUrl().split("/")[dragboard.getUrl().split("/").length - 1];
            if (url.startsWith("data:")) {
                fileEnDecryptAddInternetFile(url, DATAFILEURL);
            } else if (urlFilename.contains(".") && Utils.hasAnyCharacter("\\/:*?|<>\"", urlFilename)) {
                fileEnDecryptAddInternetFile(url, FILEFILEURL);
            } else {
                fileEnDecryptAddInternetFile(url, NONSPECIFICFILEURL);
            }
        }
    }

    /**
     * <p>If the user presses Ctrl + V: Adds the last object in clipboard (if file) for en- / decryption.
     * Get called if the user presses a key while selected file en- / decryption box</p>
     *
     * @param event source
     * @throws URISyntaxException
     *
     * @since 1.7.0
     */
    public void onFileEnDecryptPaste(KeyEvent event) throws URISyntaxException {
        if (paste.match(event)) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clipboard.getContents(null);
            try {
                if (transferable != null) {
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        Object objectFileList = transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        List files = (List) objectFileList;
                        files.forEach(o -> fileEnDecryptAddFile((File) o));
                    } else if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                        Object objectImage = transferable.getTransferData(DataFlavor.imageFlavor);
                        fileEnDecryptAddClipboardImage((BufferedImage) objectImage);
                    }
                }
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>Encrypt all files given files.
     * Get called if file 'Encrypt' button is pressed</p>
     *
     * @since 1.0.0
     */
    public void fileEncryptButton() {
        final byte[] salt;
        if (!fileEnDecryptSaltEntry.getText().isEmpty()) {
            salt = fileEnDecryptSaltEntry.getText().getBytes(StandardCharsets.UTF_8);
        } else {
            salt = new byte[16];
        }
        if (!enDecryptInputOutputFiles.isEmpty()) {
            removeFileFromFileBox.setDisable(true);
            limitNumberOfThreads.setDisable(true);
            for(Map.Entry<Label, ArrayList<File>> entry: enDecryptInputOutputFiles.entrySet()) {
                Thread thread = new Thread(() -> {
                    addFileEnDecryptThread(Thread.currentThread());
                    if (limitNumberOfThreads.isSelected()) {
                        while (totalThreads.get() >= Runtime.getRuntime().availableProcessors()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().stop();
                            }
                        }
                    }
                    totalThreads.getAndIncrement();
                    Label inputFileLabel = entry.getKey();
                    ArrayList<File> outputFileList = entry.getValue();
                    String fileEnDecryptAlgorithm = fileEnDecryptAlgorithmBox.getSelectionModel().getSelectedItem();
                    EnDecrypt.AES fileEncrypt = new EnDecrypt.AES(fileEnDecryptKeyEntry.getText(), salt, Integer.parseInt(fileEnDecryptAlgorithm.substring(fileEnDecryptAlgorithm.indexOf('-') + 1)));
                    if (enDecryptInputOutputInternetFiles.containsKey(inputFileLabel)) {
                        ArrayList<Object> fileSpecs = enDecryptInputOutputInternetFiles.get(inputFileLabel);
                        int urlType = (int) fileSpecs.get(0);
                        String url = (String) fileSpecs.get(1);
                        try {
                            if (urlType == FILEFILEURL || urlType == NONSPECIFICFILEURL) {
                                URLConnection openURL = new URL(url).openConnection();
                                openURL.addRequestProperty("User-Agent", "Mozilla/5.0");
                                fileEncrypt.encryptFile(openURL.getInputStream(), new FileOutputStream((File) fileSpecs.get(2)), buffer);
                            } else if (urlType == DATAFILEURL) {
                                final int dataStartIndex = url.indexOf(",") + 1;
                                final String data = url.substring(dataStartIndex);
                                byte[] decoded = java.util.Base64.getDecoder().decode(data);
                                fileEncrypt.encryptFile(new ByteArrayInputStream(decoded), new FileOutputStream((File) fileSpecs.get(2)), buffer);
                            }
                        } catch (FileNotFoundException | InvalidKeySpecException | NoSuchAlgorithmException | MalformedURLException | InvalidKeyException | NoSuchPaddingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Platform.runLater(() -> errorAlert("IO Exception occurred", e.getMessage()));
                        }
                    } else if (enDecryptInputOutputClipboardImages.containsKey(inputFileLabel)) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        BufferedImage bufferedImage = enDecryptInputOutputClipboardImages.get(inputFileLabel);
                        try {
                            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
                            fileEncrypt.encryptFile(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), new FileOutputStream(outputFileList.get(0).getAbsoluteFile()), buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Platform.runLater(() -> errorAlert("IO Exception occurred", e.getMessage()));
                        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            File inputFile = new File(inputFileLabel.getText());
                            if (inputFile.isFile()) {
                                fileEncrypt.encryptFile(new FileInputStream(inputFile), new FileOutputStream(outputFileList.get(0)), buffer);
                            } else {
                                fileEncrypt.encryptDirectory(inputFileLabel.getText(), outputFileList.get(0).getAbsolutePath(), ".cryptoGX", buffer);
                                if (!outputFileList.get(0).isDirectory()) {
                                    Platform.runLater(() -> warningAlert("Couldn't create directory\n '" + outputFileList.get(0).getAbsolutePath() + "'.\nTry again or restart cryptoGX with admin privileges"));
                                }
                            }
                        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Platform.runLater(() -> errorAlert("IO Exception occurred", e.getMessage()));
                        }
                    }
                    if (removeFileFromFileBox.isSelected()) {
                        Platform.runLater(() -> fileEnDecryptDeleteEntry(entry.getKey()));
                    }
                    if ((getFileEnDecryptThreadsSize() - 1) <= 0) {
                        fileEnDecryptLoading = false;
                        Platform.runLater(() -> {
                            fileEnDecryptLoadingImage.setImage(null);
                            removeFileFromFileBox.setDisable(false);
                            limitNumberOfThreads.setDisable(false);
                        });
                    }
                    removeFileEnDecryptThread(Thread.currentThread());
                    totalThreads.getAndDecrement();
                });
                thread.setDaemon(false);
                thread.start();
                if (!fileEnDecryptLoading) {
                    fileEnDecryptLoadingImage.setImage(loadingImage);
                }
                fileEnDecryptLoading = true;
            }
        }
    }

    /**
     * <p>Decrypt all files given files.
     * Get called if file 'Decrypt' button is pressed</p>
     *
     * @since 1.0.0
     */
    public void fileDecryptButton() {
        final byte[] salt;
        if (!fileEnDecryptSaltEntry.getText().isEmpty()) {
            salt = fileEnDecryptSaltEntry.getText().getBytes(StandardCharsets.UTF_8);
        } else {
            salt = new byte[16];
        }
        if (!enDecryptInputOutputFiles.isEmpty()) {
            removeFileFromFileBox.setDisable(true);
            limitNumberOfThreads.setDisable(true);
            for(Map.Entry<Label, ArrayList<File>> entry: enDecryptInputOutputFiles.entrySet()) {
                Thread thread = new Thread(() -> {
                    addFileEnDecryptThread(Thread.currentThread());
                    if (limitNumberOfThreads.isSelected()) {
                        while (totalThreads.get() >= Runtime.getRuntime().availableProcessors()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().stop();
                            }
                        }
                    }
                    totalThreads.getAndIncrement();
                    Label inputFileLabel = entry.getKey();
                    ArrayList<File> outputFileList = entry.getValue();
                    String fileEnDecryptAlgorithm = fileEnDecryptAlgorithmBox.getSelectionModel().getSelectedItem();
                    EnDecrypt.AES fileDecrypt = new EnDecrypt.AES(fileEnDecryptKeyEntry.getText(), salt, Integer.parseInt(fileEnDecryptAlgorithm.substring(fileEnDecryptAlgorithm.indexOf('-') + 1)));
                    if (enDecryptInputOutputInternetFiles.containsKey(entry.getKey())) {
                        ArrayList<Object> imageSpecs = enDecryptInputOutputInternetFiles.get(entry.getKey());
                        int urlType = (int) imageSpecs.get(0);
                        String url = (String) imageSpecs.get(1);
                        try {
                            if (urlType == FILEFILEURL) {
                                URLConnection openURL = new URL(url).openConnection();
                                openURL.addRequestProperty("User-Agent", "Mozilla/5.0");
                                fileDecrypt.decryptFile(openURL.getInputStream(), new FileOutputStream((File) imageSpecs.get(2)), buffer);
                                fileDecrypt.decryptFile(openURL.getInputStream(), new FileOutputStream((File) imageSpecs.get(2)), buffer);
                            } else if (urlType == DATAFILEURL) {
                                final int dataStartIndex = url.indexOf(",") + 1;
                                final String data = url.substring(dataStartIndex);
                                byte[] decoded = java.util.Base64.getDecoder().decode(data);
                                fileDecrypt.decryptFile(new ByteArrayInputStream(decoded), new FileOutputStream((File) imageSpecs.get(2)), buffer);
                            } else if (urlType == NONSPECIFICFILEURL) {
                                URLConnection openURL = new URL(url).openConnection();
                                openURL.addRequestProperty("User-Agent", "Mozilla/5.0");
                                fileDecrypt.decryptFile(openURL.getInputStream(), new FileOutputStream((File) imageSpecs.get(2)), buffer);
                            }
                        } catch (FileNotFoundException | InvalidKeySpecException | NoSuchAlgorithmException | MalformedURLException | InvalidKeyException | NoSuchPaddingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Platform.runLater(() -> errorAlert("IO Exception occurred", e.getMessage()));
                        }
                    } else if (enDecryptInputOutputClipboardImages.containsKey(inputFileLabel)) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        BufferedImage bufferedImage = enDecryptInputOutputClipboardImages.get(inputFileLabel);
                        try {
                            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
                            fileDecrypt.decryptFile(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), new FileOutputStream(outputFileList.get(1).getAbsolutePath()), buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Platform.runLater(() -> errorAlert("IO Exception occurred", e.getMessage()));
                        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            File inputFile = new File(inputFileLabel.getText());
                            if (inputFile.isFile()) {
                                fileDecrypt.decryptFile(new FileInputStream(inputFile), new FileOutputStream(outputFileList.get(1)), buffer);
                            } else {
                                fileDecrypt.decryptDirectory(inputFileLabel.getText(), outputFileList.get(1).getAbsolutePath(), "@.cryptoGX@", buffer);
                                if (!outputFileList.get(1).isDirectory()) {
                                    Platform.runLater(() -> warningAlert("Couldn't create directory\n '" + outputFileList.get(1).getAbsolutePath() + "'.\nTry again or restart cryptoGX with admin privileges"));
                                }
                            }
                        } catch (NoSuchPaddingException | InvalidKeySpecException | InvalidKeyException | NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Platform.runLater(() -> errorAlert("IO Exception occurred", e.getMessage()));
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                            Platform.runLater(() -> errorAlert("Wrong text for encryption is given", e.getMessage()));
                        }
                    }
                    if (removeFileFromFileBox.isSelected()) {
                        Platform.runLater(() -> fileEnDecryptDeleteEntry(entry.getKey()));
                    }
                    if ((getFileEnDecryptThreadsSize() - 1) <= 0) {
                        fileEnDecryptLoading = false;
                        Platform.runLater(() -> {
                            fileEnDecryptLoadingImage.setImage(null);
                            removeFileFromFileBox.setDisable(false);
                            limitNumberOfThreads.setDisable(false);
                        });
                    }
                    removeFileEnDecryptThread(Thread.currentThread());
                    totalThreads.getAndDecrement();
                });
                thread.setDaemon(false);
                thread.start();
                if (!fileEnDecryptLoading) {
                    fileEnDecryptLoadingImage.setImage(loadingImage);
                }
                fileEnDecryptLoading = true;
            }
        }
    }

    /**
     * <p>Cancels the file en- / decryption.
     * Get called if the file en- / decrypt 'Cancel' button is pressed</p>
     *
     * @since 1.12.0
     */
    public void fileEnDecryptCancelButton() {
        for (Iterator<Thread> iterator = getFileEnDecryptThreads().iterator(); iterator.hasNext();) {
            Thread thread = iterator.next();
            while (thread.isAlive() && !thread.isInterrupted()) {
                thread.stop();
                thread.interrupt();
            }
            iterator.remove();
        }
        fileEnDecryptLoading = false;
        fileEnDecryptLoadingImage.setImage(null);
        removeFileFromFileBox.setDisable(false);
        limitNumberOfThreads.setDisable(false);
    }

    //-----fileDelete-----//

    /**
     * <p>Synchronized method to get the list of threads which delete files</p>
     *
     * @return list of threads which delete files
     *
     * @since 1.2.0
     */
    private synchronized List<Thread> getFileDeleteThreads() {
        return fileDeleteThreads;
    }

    /**
     * <p>Synchronized method to get the number of threads which delete files</p>
     *
     * @return number of threads which delete files
     *
     * @since 1.2.0
     */
    private synchronized int getFileDeleteThreadsSize() {
        return fileDeleteThreads.size();
    }

    /**
     * <p>Synchronized method to add a thread to the file delete list of current running file delete threads</p>
     *
     * @param thread that should be added
     *
     * @since 1.2.0
     */
    private synchronized void addFileDeleteThread(Thread thread) {
        fileDeleteThreads.add(thread);
    }

    /**
     * <p>Synchronized method to remove a thread from the file delete list of current file delete threads</p>
     *
     * @param thread that should be removed
     *
     * @since 1.2.0
     */
    private synchronized void removeFileDeleteThread(Thread thread) {
        fileDeleteThreads.remove(thread);
    }

    /**
     * <p>Adds a file that should be deleted</p>
     *
     * @param file that should be added
     *
     * @since 1.2.0
     */
    private void fileDeleteAddFile(File file) {
        for (File f: deleteInputFiles.values()) {
            if (f.getAbsolutePath().equals(file.getAbsolutePath())) {
                return;
            }
        }
        Label newLabel = new Label(file.getAbsolutePath());
        newLabel.setOnKeyTyped(this::keyTypedTooltip);
        newLabel.setOnMouseMoved(this::mouseOverEntryTooltip);
        newLabel.setOnMouseExited(event -> mouseExitEntryTooltip());
        newLabel.setOnMouseClicked(event -> fileDeleteSelected(newLabel));
        newLabel.setContextMenu(fileDeleteInputContextMenu);
        fileDeleteInputFiles.getChildren().add(newLabel);
        deleteInputFiles.put(newLabel, file.getAbsoluteFile());
    }

    /**
     * <p>Changes the highlight of the clicked item in the file delete box.
     * Get called if the user click an non-highlighted item in the file delete box</p>
     *
     * @param changeLabel is the label that the user has clicked
     *
     * @since 1.2.0
     */
    private void fileDeleteSelected(Label changeLabel) {
        if (changeLabel != null) {
            fileEnDecryptSelected(null);
            deleteInputFiles.keySet().forEach(label -> label.setStyle(null));
            changeLabel.setStyle("-fx-background-color: lightblue; -fx-border-color: #292929");
            choosedLabel = changeLabel;
            choosedLabelType = "DELETE";
        } else {
            deleteInputFiles.keySet().forEach(label -> label.setStyle(null));
        }
    }

    /**
     * <p>Deletes an entry for file delete.
     * Get called if the user presses 'del' or delete the entry in the file delete box via the right click tooltip</p>
     *
     * @param label that should be deleted
     *
     * @since 1.12.0
     */
    private void fileDeleteDeleteEntry(Label label) {
        deleteInputFiles.remove(choosedLabel);
        if (fileDeleteInputFiles.getChildren().size() - 1 >= 1) {
            for (int i=0; i<fileDeleteInputFiles.getChildren().size(); i++) {
                if (fileDeleteInputFiles.getChildren().get(i) == choosedLabel) {
                    fileDeleteInputFiles.getChildren().remove(choosedLabel);
                    if (label == choosedLabel) {
                        try {
                            choosedLabel = (Label) fileDeleteInputFiles.getChildren().get(i - 1);
                            choosedLabelType = "DELETE";
                            fileDeleteSelected(choosedLabel);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            choosedLabel = null;
                            choosedLabelType = "DELETE";
                        }
                        break;
                    }
                }
            }
        } else {
            fileDeleteInputFiles.getChildren().remove(choosedLabel);
            choosedLabel = null;
            choosedLabelType = "DELETE";
        }
    }

    /**
     * <p>Opens a file chooser GUI where the user can select the files that should be en- / decrypted.
     * Get called if the 'Choose files...' in the delete section button is pressed</p>
     *
     * @since 1.12.0
     */
    public void fileDeleteChooseFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose files");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.*"));
        List<File> files = fileChooser.showOpenMultipleDialog(rootWindow.getScene().getWindow());
        try {
            if (files.size() > 0) {
                files.forEach(this::fileDeleteAddFile);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Opens a directory chooser GUI where the user can select the directories that should be en- / decrypted.
     * Get called if the 'Choose directories...' in the delete section button is pressed</p>
     *
     * @since 1.12.0
     */
    public void fileDeleteChooseDirectories() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directories");
        File file = directoryChooser.showDialog(rootWindow.getScene().getWindow());
        try {
            fileDeleteAddFile(file);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * <p>Get called if user drags a file over the delete file box</p>
     *
     * @param event source
     *
     * @since 1.2.0
     */
    public void onFileDeleteDragOver(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (event.getGestureSource() != fileDeleteInputFiles && dragboard.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
    }

    /**
     * <p>Get called if the user drops the dragged file over the delete file box</p>
     *
     * @param event source
     *
     * @since 1.2.0
     */
    public void onFileDeleteDragNDrop(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles()) {
            dragboard.getFiles().forEach(file -> {
                if (file.isFile() || file.isDirectory()) {
                    fileDeleteAddFile(file);
                }
            });
        }
    }

    /**
     * <p>Delete all given files.
     * Get called if 'Delete' button is pressed</p>
     *
     * @since 1.2.0
     */
    public void fileDelete() {
        if (!fileDeleteLoading && !deleteInputFiles.isEmpty()) {
            fileDeleteLoadingImage.setImage(loadingImage);
        }
        int deleteIterations = Integer.parseInt(fileDeleteIterationsEntry.getText());
        for (Map.Entry<Label, File> map : deleteInputFiles.entrySet()) {
            Label label = map.getKey();
            File file = map.getValue();
            Thread thread = new Thread(() -> {
                addFileDeleteThread(Thread.currentThread());
                if (limitNumberOfThreads.isSelected()) {
                    while (totalThreads.get() >= Runtime.getRuntime().availableProcessors()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().stop();
                        }
                    }
                }
                totalThreads.getAndIncrement();
                try {
                    if (file.isFile()) {
                        SecureDelete.deleteFile(file, deleteIterations, buffer);
                    } else if (file.isDirectory()) {
                        SecureDelete.deleteDirectory(file.getAbsolutePath(), deleteIterations, buffer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if ((getFileDeleteThreadsSize() - 1) <= 0) {
                    fileDeleteLoading = false;
                    Platform.runLater(() -> fileDeleteLoadingImage.setImage(null));
                }
                if (label == choosedLabel) {
                    choosedLabel = null;
                    choosedLabelType = null;
                }
                Platform.runLater(() -> fileDeleteInputFiles.getChildren().remove(label));
                removeFileDeleteThread(Thread.currentThread());
                totalThreads.getAndDecrement();
            });
            thread.setDaemon(false);
            thread.start();
            fileDeleteLoading = true;
        }
    }

    /**
     * <p>Cancels the file en- / decryption.
     * Get called if the file delete 'Cancel' button is pressed</p>
     *
     * @since 1.12.0
     */
    public void fileDeleteCancelButton() {
        for (Iterator<Thread> iterator = getFileDeleteThreads().iterator(); iterator.hasNext();) {
            Thread thread = iterator.next();
            while (thread.isAlive() & !thread.isInterrupted()) {
                thread.stop();
                thread.interrupt();
            }
            iterator.remove();
        }
        fileDeleteLoading = false;
        fileDeleteLoadingImage.setImage(null);
    }

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location
     * The location used to resolve relative paths for the root object, or
     * <tt>null</tt> if the location is not known.
     *
     * @param resources
     * The resources used to localize the root object, or <tt>null</tt> if
     * the root object was not localized.
     *
     * @since 1.0.0
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //-----general-----//

        currentConfigSettings.put("textKey", configDefaultTextKey);
        currentConfigSettings.put("textSalt", configDefaultTextSalt);
        currentConfigSettings.put("textAlgorithm", configDefaultTextAlgorithm);

        currentConfigSettings.put("fileEnDecryptKey", configDefaultFileEnDecryptKey);
        currentConfigSettings.put("fileEnDecryptSalt", configDefaultFileEnDecryptSalt);
        currentConfigSettings.put("fileEnDecryptAlgorithm", configDefaultFileEnDecryptAlgorithm);

        currentConfigSettings.put("fileDeleteIterations", String.valueOf(configDefaultFileDeleteIterations));

        currentConfigSettings.put("fileOutputPath", configDefaultFileOutputPath);
        currentConfigSettings.put("removeFromFileBox", String.valueOf(configDefaultRemoveFileFromFileBox));
        currentConfigSettings.put("limitNumberOfThreads", String.valueOf(configDefaultLimitNumberOfThreads));

        menubar.setOnMouseDragged(event -> {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setX(event.getScreenX() + menubarX);
            stage.setY(event.getScreenY() + menubarY);
        });
        menubar.setOnMousePressed(event -> {
            Scene scene = ((Node) event.getSource()).getScene();
            menubarX = scene.getX() - event.getSceneX();
            menubarY = scene.getY() - event.getSceneY();
        });

        rootWindow.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.DELETE && choosedLabelType != null) {
                if (choosedLabelType.equals("ENDECRYPT")) {
                    fileEnDecryptDeleteEntry(choosedLabel);
                } else if (choosedLabelType.equals("DELETE")) {
                    fileDeleteDeleteEntry(choosedLabel);
                }
            }
        });

        getChoosedLabelInputFileFolder.setOnAction(event -> {
            Desktop desktop = Desktop.getDesktop();
            String filePath = choosedLabel.getText();
            try {
                desktop.open(new File(filePath.substring(0, filePath.lastIndexOf(System.getProperty("file.separator")))));
            } catch (IOException e) {
                errorAlert("An unexpected IO Exception occurred", e.getMessage());
            }
        });

        getChoosedLabelOutputFileFolder.setOnAction(event -> {
            Desktop desktop;
            String filePath;
            if (enDecryptInputOutputFiles.containsKey(choosedLabel)) {
                desktop = Desktop.getDesktop();
                filePath = enDecryptInputOutputFiles.get(choosedLabel).get(0).getAbsolutePath();
            } else {
                return;
            }
            try {
                desktop.open(new File(filePath.substring(0, filePath.lastIndexOf(System.getProperty("file.separator")))));
            } catch (IOException e) {
                errorAlert("An unexpected IO Exception occurred", e.getMessage());
            }
        });

        setDefaultOutputPath.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File directory = directoryChooser.showDialog(rootWindow.getScene().getWindow());
            try {
                currentConfigSettings.replace("fileOutputPath", directory.getAbsolutePath());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });

        settingsMenu.setOnShowing(event -> {
            loadSettings.setDisable(!isConfig);
            exportSettings.setDisable(!isConfig);
        });

        removeFileFromFileBox.setOnAction(event -> currentConfigSettings.replace("removeFromFileBox", String.valueOf(removeFileFromFileBox.isSelected())));
        limitNumberOfThreads.setOnAction(event -> currentConfigSettings.replace("limitNumberOfThreads", String.valueOf(limitNumberOfThreads.isSelected())));
        saveSettings.setOnAction(event -> {
            try {
                addSettingGUI(rootWindow.getScene().getWindow(), currentConfigSettings);
                if (config.isFile()) {
                    isConfig = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        loadSettings.setOnAction(event -> {
            try {
                currentConfigSettings = (HashMap<String, String>) loadSettingsGUI(rootWindow.getScene().getWindow()).values().toArray()[0];
                textKeyEntry.setText(currentConfigSettings.get("textKey"));
                textSaltEntry.setText(currentConfigSettings.get("textSalt"));
                textAlgorithmBox.setValue(currentConfigSettings.get("textAlgorithm"));

                fileEnDecryptKeyEntry.setText(currentConfigSettings.get("fileEnDecryptKey"));
                fileEnDecryptSaltEntry.setText(currentConfigSettings.get("fileEnDecryptSalt"));
                fileEnDecryptAlgorithmBox.setValue(currentConfigSettings.get("fileEnDecryptAlgorithm"));

                fileDeleteIterationsEntry.setText(currentConfigSettings.get("fileDeleteIterations"));

                removeFileFromFileBox.setSelected(Boolean.parseBoolean(currentConfigSettings.get("removeFromFileBox")));
                limitNumberOfThreads.setSelected(Boolean.parseBoolean(currentConfigSettings.get("limitNumberOfThreads")));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException ex) {
                try {
                    SecureDelete.deleteFile(config, 5, buffer);
                    isConfig = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        exportSettings.setOnAction(event -> {
            try {
                exportSettingsGUI(rootWindow.getScene().getWindow());
            } catch (IOException e) {
                e.printStackTrace();
                errorAlert("IO Exception occurred", e.getMessage());
            }
        });
        importSettings.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import settings");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Config files", "*.config*", "*.xml"), new FileChooser.ExtensionFilter("All files", "*.*"));
            File file = fileChooser.showOpenDialog(rootWindow.getScene().getWindow());
            if (file != null) {
                if (isConfig) {
                    writeSettings(config, readSettings(file));
                } else {
                    writeSettings(config, readSettings(file));
                    isConfig = true;
                }
            }
        });

        //-----text------//

        textAlgorithmBox.setItems(FXCollections.observableArrayList(Utils.algorithms.keySet()));
        textAlgorithmBox.setValue(Utils.algorithms.keySet().toArray(new String[Utils.algorithms.size()])[0]);

        //-----fileEnDecrypt-----//

        fileEnDecryptAlgorithmBox.setItems(FXCollections.observableArrayList(Utils.algorithms.keySet()));
        fileEnDecryptAlgorithmBox.setValue(Utils.algorithms.keySet().toArray(new String[Utils.algorithms.size()])[0]);

        MenuItem enDecryptRemove = new MenuItem();
        enDecryptRemove.setText("Remove");
        enDecryptRemove.setOnAction(removeEvent -> fileEnDecryptDeleteEntry(choosedLabel));
        MenuItem enDecryptChangeDest = new MenuItem();
        enDecryptChangeDest.setText("Change output file / directory");
        enDecryptChangeDest.setOnAction(outputFileChangeEvent -> {
            File file;
            if (new File(choosedLabel.getText()).isFile()) {
                FileChooser destChooser = new FileChooser();
                destChooser.setTitle("Choose or create new file");
                destChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
                file = destChooser.showSaveDialog(rootWindow.getScene().getWindow());
            } else {
                DirectoryChooser destChooser = new DirectoryChooser();
                destChooser.setTitle("Choose or create new directory");
                file = destChooser.showDialog(rootWindow.getScene().getWindow());
            }
            if (file != null) {
                for (Map.Entry<Label, ArrayList<File>> entry : enDecryptInputOutputFiles.entrySet()) {
                    if (entry.getKey().getText().equals(choosedLabel.getText())) {
                        ArrayList<File> changedFile = new ArrayList<>();
                        changedFile.add(0, file);
                        changedFile.add(1, file);
                        enDecryptInputOutputFiles.replace(entry.getKey(), entry.getValue(), changedFile);
                        fileOutputFilesChangeText((Label) fileEnDecryptLabelEvent.getSource(), file.getAbsolutePath(), file.getAbsolutePath());
                        break;
                    }
                }
            }
        });
        fileEnDecryptInputContextMenu.getItems().addAll(enDecryptRemove, enDecryptChangeDest);

        ContextMenu fileEnDecryptInputFilesMenu = new ContextMenu();
        MenuItem enDecryptPaste = new MenuItem();
        enDecryptPaste.setText("Paste");
        enDecryptPaste.setOnAction(pasteEvent -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clipboard.getContents(null);
            try {
                if (transferable != null) {
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        Object objectFileList = transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        List files = (List) objectFileList;
                        files.forEach(o -> fileEnDecryptAddFile((File) o));
                    } else if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                        Object objectImage = transferable.getTransferData(DataFlavor.imageFlavor);
                        fileEnDecryptAddClipboardImage((BufferedImage) objectImage);
                    }
                }
            } catch (UnsupportedFlavorException | IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
        fileEnDecryptInputFilesMenu.getItems().add(enDecryptPaste);

        fileEnDecryptInputFiles.setOnContextMenuRequested(event -> {
            if (!fileEnDecryptInputContextMenu.isShowing()) {
                fileEnDecryptInputFilesMenu.show(((VBox) event.getSource()).getParent().getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });

        fileOutputFileChangeDest.setOnAction(event -> {
            FileChooser fileDestChooser = new FileChooser();
            fileDestChooser.setTitle("Choose or create new file");
            fileDestChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
            File file = fileDestChooser.showSaveDialog(rootWindow.getScene().getWindow());
            if (file != null) {
                for (Map.Entry<Label, ArrayList<File>> entry : enDecryptInputOutputFiles.entrySet()) {
                    if (entry.getKey().getText().equals(choosedLabel.getText())) {
                        ArrayList<File> changedFile = new ArrayList<>();
                        changedFile.add(0, file);
                        changedFile.add(1, file);
                        enDecryptInputOutputFiles.replace(entry.getKey(), entry.getValue(), changedFile);
                        fileOutputFilesChangeText((Label) fileEnDecryptLabelEvent.getSource(), file.getAbsolutePath(), file.getAbsolutePath());
                        break;
                    }
                }
            }
        });

        fileOutputFileChangeDest.setDisable(true);
        getChoosedLabelOutputFileFolder.setDisable(true);

        fileEncryptOutputFile.textProperty().addListener((observable, oldValue, newValue) -> fileOutputFilesChangeText(choosedLabel, newValue, fileDecryptOutputFile.getText()));
        fileDecryptOutputFile.textProperty().addListener((observable, oldValue, newValue) -> fileOutputFilesChangeText(choosedLabel, fileEncryptOutputFile.getText(), newValue));

        //-----fileDelete-----//

        MenuItem deleteRemove = new MenuItem();
        deleteRemove.setText("Remove");
        deleteRemove.setOnAction(removeEvent -> fileDeleteDeleteEntry(choosedLabel));
        fileDeleteInputContextMenu.getItems().add(deleteRemove);

        ContextMenu fileDeleteInputFilesMenu = new ContextMenu();
        MenuItem deletePaste = new MenuItem();
        deletePaste.setText("Paste");
        deletePaste.setOnAction(pasteEvent -> {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clipboard.getContents(null);
            try {
                if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    Object objectFileList = transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    List files = (List) objectFileList;
                    files.forEach(o -> fileDeleteAddFile((File) o));
                }
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
        });
        fileDeleteInputFilesMenu.getItems().add(deletePaste);

        fileDeleteInputFiles.setOnContextMenuRequested(event -> {
            if (!fileDeleteInputContextMenu.isShowing()) {
                fileDeleteInputFilesMenu.show(((VBox) event.getSource()).getParent().getScene().getWindow(), event.getScreenX(), event.getScreenY());
            }
        });

        fileDeleteIterationsEntry.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[0-9]*")) {
                fileDeleteIterationsEntry.setText(oldValue);
            }
        });

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isConfig) {
                Platform.runLater(() -> {
                    try {
                        currentConfigSettings = (HashMap<String, String>) loadSettingsGUI(rootWindow.getScene().getWindow()).values().toArray()[0];
                        textKeyEntry.setText(currentConfigSettings.get("textKey"));
                        textSaltEntry.setText(currentConfigSettings.get("textSalt"));
                        textAlgorithmBox.setValue(currentConfigSettings.get("textAlgorithm"));

                        fileEnDecryptKeyEntry.setText(currentConfigSettings.get("fileEnDecryptKey"));
                        fileEnDecryptSaltEntry.setText(currentConfigSettings.get("fileEnDecryptSalt"));
                        fileEnDecryptAlgorithmBox.setValue(currentConfigSettings.get("fileEnDecryptAlgorithm"));

                        fileDeleteIterationsEntry.setText(currentConfigSettings.get("fileDeleteIterations"));

                        removeFileFromFileBox.setSelected(Boolean.parseBoolean(currentConfigSettings.get("removeFromFileBox")));
                        limitNumberOfThreads.setSelected(Boolean.parseBoolean(currentConfigSettings.get("limitNumberOfThreads")));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        try {
                            SecureDelete.deleteFile(config, 5, buffer);
                            isConfig = false;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        t.start();
    }
}
