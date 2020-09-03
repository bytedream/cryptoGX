package org.bytedream.cryptoGX;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.bytedream.cryptogx.Main.*;

/**
 * <p>Class for the user configuration / settings</p>
 *
 * @since 1.12.0
 */
public class Settings {

    private static double addSettingsGUIX, addSettingsGUIY;

    private static final HashSet<String> protectedSettingsNames = new HashSet<>(Arrays.asList("cryptoGX", "settings"));

    /**
     * <p>Shows a GUI where the user can save settings, which can load later</p>
     *
     * @param rootWindow from which this GUI will get called
     * @param userSetting
     * @throws IOException
     *
     * @since 1.11.0
     */
    public static void addSettingGUI(Window rootWindow, Map<String, String> userSetting) throws IOException {
        Map<String, String> newSettingItems = new HashMap<>();

        Stage rootStage = new Stage();
        rootStage.initOwner(rootWindow);
        Parent addSettingsRoot = FXMLLoader.load(Settings.class.getResource("resources/addSettingsGUI.fxml"));
        rootStage.initStyle(StageStyle.UNDECORATED);
        rootStage.initModality(Modality.WINDOW_MODAL);
        rootStage.setResizable(false);
        rootStage.setTitle("cryptoGX");
        Scene scene = new Scene(addSettingsRoot, 320, 605);

        rootStage.setScene(scene);

        scene.setOnMouseDragged(event -> {
            rootStage.setX(event.getScreenX() + addSettingsGUIX);
            rootStage.setY(event.getScreenY() + addSettingsGUIY);
        });
        scene.setOnMousePressed(event -> {
            addSettingsGUIX = scene.getX() - event.getSceneX();
            addSettingsGUIY = scene.getY() - event.getSceneY();
        });

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Platform.runLater(() -> {
                MenuBar menuBar = (MenuBar) addSettingsRoot.lookup("#menuBar");
                menuBar.setOnMouseDragged(event -> {
                    rootStage.setX(event.getScreenX() + addSettingsGUIX);
                    rootStage.setY(event.getScreenY() + addSettingsGUIY);
                });
                menuBar.setOnMousePressed(event -> {
                    addSettingsGUIX = menuBar.getLayoutX() - event.getSceneX();
                    addSettingsGUIY = menuBar.getLayoutY() - event.getSceneY();
                });

                ImageView closeButton = (ImageView) addSettingsRoot.lookup("#closeButton");
                closeButton.setOnMouseClicked(event -> rootStage.close());

                TextField settingsNameEntry = (TextField) addSettingsRoot.lookup("#settingsNameEntry");

                TextField textKeyEntry = (TextField) addSettingsRoot.lookup("#textKeyEntry");
                textKeyEntry.setText(userSetting.get("textKey"));
                TextField textSaltEntry = (TextField) addSettingsRoot.lookup("#textSaltEntry");
                textSaltEntry.setText(userSetting.get("textSalt"));
                ComboBox textAlgorithmBox = (ComboBox) addSettingsRoot.lookup("#textAlgorithmComboBox");
                textAlgorithmBox.setItems(FXCollections.observableArrayList(Utils.algorithms.keySet()));
                textAlgorithmBox.setValue(userSetting.get("textAlgorithm"));

                TextField fileEnDecryptKeyEntry = (TextField) addSettingsRoot.lookup("#fileEnDecryptKeyEntry");
                fileEnDecryptKeyEntry.setText(userSetting.get("fileEnDecryptKey"));
                TextField fileEnDecryptSaltEntry = (TextField) addSettingsRoot.lookup("#fileEnDecryptSaltEntry");
                fileEnDecryptSaltEntry.setText(userSetting.get("fileEnDecryptSalt"));
                ComboBox fileEnDecryptAlgorithmBox = (ComboBox) addSettingsRoot.lookup("#fileEnDecryptAlgorithmComboBox");
                fileEnDecryptAlgorithmBox.setItems(FXCollections.observableArrayList(Utils.algorithms.keySet()));
                fileEnDecryptAlgorithmBox.setValue(userSetting.get("fileEnDecryptAlgorithm"));

                TextField fileDeleteIterationEntry = (TextField) addSettingsRoot.lookup("#fileDeleteIterationsEntry");
                fileDeleteIterationEntry.setText(userSetting.get("fileDeleteIterations"));
                fileDeleteIterationEntry.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.matches("[0-9]*")) {
                        fileDeleteIterationEntry.setText(oldValue);
                    }
                });

                TextField fileOutputPathEntry = (TextField) addSettingsRoot.lookup("#fileOutputPathEntry");
                fileOutputPathEntry.setText(userSetting.get("fileOutputPath"));
                Button fileOutputPathButton = (Button) addSettingsRoot.lookup("#fileOutputPathButton");
                fileOutputPathButton.setOnAction(event -> {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    File directory = directoryChooser.showDialog(rootWindow.getScene().getWindow());
                    try {
                        fileOutputPathEntry.setText(directory.getAbsolutePath());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                });
                CheckBox removeFromFileBoxCheckBox = (CheckBox) addSettingsRoot.lookup("#removeFromFileBoxCheckBox");
                removeFromFileBoxCheckBox.setSelected(Boolean.parseBoolean(userSetting.get("removeFromFileBox")));
                CheckBox limitNumberOfThreadsCheckBox = (CheckBox) addSettingsRoot.lookup("#limitNumberOfThreadsCheckBox");
                limitNumberOfThreadsCheckBox.setSelected(Boolean.parseBoolean(userSetting.get("limitNumberOfThreads")));

                PasswordField hiddenPasswordEntry = (PasswordField) addSettingsRoot.lookup("#hiddenPasswordEntry");
                TextField visiblePasswordEntry = (TextField) addSettingsRoot.lookup("#visiblePasswordEntry");
                CheckBox showPassword = (CheckBox) addSettingsRoot.lookup("#showPassword");

                showPassword.setOnAction(event -> {
                    if (showPassword.isSelected()) {
                        visiblePasswordEntry.setText(hiddenPasswordEntry.getText());
                        visiblePasswordEntry.setVisible(true);
                        hiddenPasswordEntry.setVisible(false);
                    } else {
                        hiddenPasswordEntry.setText(visiblePasswordEntry.getText());
                        hiddenPasswordEntry.setVisible(true);
                        visiblePasswordEntry.setVisible(false);
                    }
                });
                CheckBox encryptSettings = (CheckBox) addSettingsRoot.lookup("#encryptSettings");
                encryptSettings.setOnAction(event -> {
                    if (encryptSettings.isSelected()) {
                        hiddenPasswordEntry.setDisable(false);
                        visiblePasswordEntry.setDisable(false);
                        showPassword.setDisable(false);
                    } else {
                        hiddenPasswordEntry.setDisable(true);
                        visiblePasswordEntry.setDisable(true);
                        showPassword.setDisable(true);
                    }
                });
                Button saveButton = (Button) addSettingsRoot.lookup("#saveButton");
                saveButton.setOnAction(event -> {
                    if (settingsNameEntry.getText().trim().isEmpty()) {
                        warningAlert("Add a name for the setting");
                    } else if (protectedSettingsNames.contains(settingsNameEntry.getText())) {
                        warningAlert("Please choose another name for this setting");
                    } else if (settingsNameEntry.getText().trim().contains(" ")) {
                        warningAlert("Setting name must not contain free space");
                    } else if (encryptSettings.isSelected()) {
                        try {
                            EnDecrypt.AES encrypt;
                            if (!hiddenPasswordEntry.isDisabled() && !hiddenPasswordEntry.getText().trim().isEmpty()) {
                                encrypt = new EnDecrypt.AES(hiddenPasswordEntry.getText(), new byte[16]);
                            } else if (!visiblePasswordEntry.getText().trim().isEmpty()) {
                                encrypt = new EnDecrypt.AES(visiblePasswordEntry.getText(), new byte[16]);
                            } else {
                                throw new InvalidKeyException("The key must not be empty");
                            }

                            newSettingItems.put("encrypted", "true");

                            newSettingItems.put("textKey", encrypt.encrypt(textKeyEntry.getText()));
                            newSettingItems.put("textSalt", encrypt.encrypt(textSaltEntry.getText()));
                            newSettingItems.put("textAlgorithm", encrypt.encrypt(textAlgorithmBox.getSelectionModel().getSelectedItem().toString()));

                            newSettingItems.put("fileEnDecryptKey", encrypt.encrypt(fileEnDecryptKeyEntry.getText()));
                            newSettingItems.put("fileEnDecryptSalt", encrypt.encrypt(fileEnDecryptSaltEntry.getText()));
                            newSettingItems.put("fileEnDecryptAlgorithm", encrypt.encrypt(fileEnDecryptAlgorithmBox.getSelectionModel().getSelectedItem().toString()));

                            newSettingItems.put("fileDeleteIterations", encrypt.encrypt(fileDeleteIterationEntry.getText()));

                            newSettingItems.put("fileOutputPath", encrypt.encrypt(fileOutputPathEntry.getText()));
                            newSettingItems.put("removeFromFileBox", encrypt.encrypt(String.valueOf(removeFromFileBoxCheckBox.isSelected())));
                            newSettingItems.put("limitNumberOfThreads", encrypt.encrypt(String.valueOf(limitNumberOfThreadsCheckBox.isSelected())));

                            if (!config.isFile()) {
                                try {
                                    if (!config.createNewFile()) {
                                        warningAlert("Couldn't create config file");
                                    } else {
                                        addSetting(config, settingsNameEntry.getText().trim(), newSettingItems);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    errorAlert("Couldn't create config file", e.getMessage());
                                }
                            } else {
                                addSetting(config, settingsNameEntry.getText().trim(), newSettingItems);
                            }

                            rootStage.close();
                        } catch (InvalidKeyException e) {
                            warningAlert("The key must not be empty");
                        } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                    } else {
                        newSettingItems.put("textKey", textKeyEntry.getText());
                        newSettingItems.put("textSalt", textSaltEntry.getText());
                        newSettingItems.put("textAlgorithm", textAlgorithmBox.getSelectionModel().getSelectedItem().toString());

                        newSettingItems.put("fileEnDecryptKey", fileEnDecryptKeyEntry.getText());
                        newSettingItems.put("fileEnDecryptSalt", fileEnDecryptSaltEntry.getText());
                        newSettingItems.put("fileEnDecryptAlgorithm", fileEnDecryptAlgorithmBox.getSelectionModel().getSelectedItem().toString());

                        newSettingItems.put("fileDeleteIterations", fileDeleteIterationEntry.getText());

                        newSettingItems.put("fileOutputPath", fileOutputPathEntry.getText());
                        newSettingItems.put("removeFromFileBox", String.valueOf(removeFromFileBoxCheckBox.isSelected()));
                        newSettingItems.put("limitNumberOfThreads", String.valueOf(limitNumberOfThreadsCheckBox.isSelected()));

                        if (!config.isFile()) {
                            try {
                                if (!config.createNewFile()) {
                                    warningAlert("Couldn't create config file");
                                } else {
                                    addSetting(config, settingsNameEntry.getText().trim(), newSettingItems);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                errorAlert("Couldn't create config file", e.getMessage());
                            }
                        } else {
                            addSetting(config, settingsNameEntry.getText().trim(), newSettingItems);
                        }

                        rootStage.close();
                    }
                });
            });
        });

        thread.start();

        rootStage.showAndWait();
    }

    /**
     * <p>Shows a GUI where the user can export settings to a extra file</p>
     *
     * @param rootWindow from which this GUI will get called
     * @throws IOException
     *
     * @since 1.11.0
     */
    public static void exportSettingsGUI(Window rootWindow) throws IOException {
        Stage rootStage = new Stage();
        rootStage.initOwner(rootWindow);
        Parent exportSettingsRoot = FXMLLoader.load(Settings.class.getResource("resources/exportSettingsGUI.fxml"));
        rootStage.initStyle(StageStyle.UNDECORATED);
        rootStage.initModality(Modality.WINDOW_MODAL);
        rootStage.setResizable(false);
        rootStage.setTitle("cryptoGX");
        Scene scene = new Scene(exportSettingsRoot, 254, 253);

        rootStage.setScene(scene);

        scene.setOnMouseDragged(event -> {
            rootStage.setX(event.getScreenX() + addSettingsGUIX);
            rootStage.setY(event.getScreenY() + addSettingsGUIY);
        });
        scene.setOnMousePressed(event -> {
            addSettingsGUIX = scene.getX() - event.getSceneX();
            addSettingsGUIY = scene.getY() - event.getSceneY();
        });

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MenuBar menuBar = (MenuBar) exportSettingsRoot.lookup("#menuBar");
            menuBar.setOnMouseDragged(event -> {
                rootStage.setX(event.getScreenX() + addSettingsGUIX);
                rootStage.setY(event.getScreenY() + addSettingsGUIY);
            });
            menuBar.setOnMousePressed(event -> {
                addSettingsGUIX = menuBar.getLayoutX() - event.getSceneX();
                addSettingsGUIY = menuBar.getLayoutY() - event.getSceneY();
            });
            ImageView closeButton = (ImageView) exportSettingsRoot.lookup("#closeButton");
            closeButton.setOnMouseClicked(event -> rootStage.close());

            VBox settingsBox = (VBox) exportSettingsRoot.lookup("#settingsBox");
            Platform.runLater(() -> readSettings(config).keySet().forEach(s -> {
                CheckBox newCheckBox = new CheckBox();
                newCheckBox.setText(s);
                settingsBox.getChildren().add(newCheckBox);
            }));

            Button exportButton = (Button) exportSettingsRoot.lookup("#exportButton");
            exportButton.setOnAction(event -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Export settings");
                fileChooser.setInitialFileName("settings.config");
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Config files", "*.config"),
                        new FileChooser.ExtensionFilter("All files", "*.*"));
                File file = fileChooser.showSaveDialog(exportSettingsRoot.getScene().getWindow());
                if (file != null) {
                    TreeMap<String, Map<String, String>> writeInfos = new TreeMap<>();
                    TreeMap<String, Map<String, String>> settings = readSettings(config);
                    for (int i=0; i<settingsBox.getChildren().size(); i++) {
                        CheckBox checkBox = (CheckBox) settingsBox.getChildren().get(i);
                        if (checkBox.isSelected()) {
                            String checkBoxText = checkBox.getText();
                            writeInfos.put(checkBoxText, settings.get(checkBoxText));
                        }
                    }
                    if (!file.getAbsolutePath().contains(".")) {
                        file = new File(file.getAbsolutePath() + ".config");
                    }
                    writeSettings(file, writeInfos);
                }
            });
        });

        thread.start();

        rootStage.showAndWait();
    }

    /**
     * <p>Shows a GUI where the user can load saved settings</p>
     *
     * @param rootWindow from which this GUI will get called
     * @return the settings that the user has chosen
     * @throws IOException
     *
     * @since 1.11.0
     */
    public static TreeMap<String, Map<String, String>> loadSettingsGUI(Window rootWindow) throws IOException {
        Button[] outerLoadButton = new Button[1];
        HashMap<String, String> setting = new HashMap<>();
        TreeMap<String, Map<String, String>> settingItems = readSettings(config);
        TreeMap<String, Map<String, String>> returnItems = new TreeMap<>();

        Stage rootStage = new Stage();
        rootStage.initOwner(rootWindow);
        AnchorPane loadSettingsRoot = FXMLLoader.load(Settings.class.getResource("resources/loadSettingsGUI.fxml"));
        rootStage.initStyle(StageStyle.UNDECORATED);
        rootStage.initModality(Modality.WINDOW_MODAL);
        rootStage.setResizable(false);
        rootStage.setTitle("cryptoGX");
        rootStage.getIcons().add(new Image(Settings.class.getResource("resources/cryptoGX.png").toExternalForm()));
        Scene scene = new Scene(loadSettingsRoot, 242, 235);

        scene.setOnMouseDragged(event -> {
            rootStage.setX(event.getScreenX() + addSettingsGUIX);
            rootStage.setY(event.getScreenY() + addSettingsGUIY);
        });
        scene.setOnMousePressed(event -> {
            addSettingsGUIX = scene.getX() - event.getSceneX();
            addSettingsGUIY = scene.getY() - event.getSceneY();
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                outerLoadButton[0].fire();
            }
        });

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                MenuBar menuBar = (MenuBar) loadSettingsRoot.lookup("#menuBar");
                menuBar.setOnMouseDragged(event -> {
                    rootStage.setX(event.getScreenX() + addSettingsGUIX);
                    rootStage.setY(event.getScreenY() + addSettingsGUIY);
                });
                menuBar.setOnMousePressed(event -> {
                    addSettingsGUIX = menuBar.getLayoutX() - event.getSceneX();
                    addSettingsGUIY = menuBar.getLayoutY() - event.getSceneY();
                });

                ImageView closeButton = (ImageView) loadSettingsRoot.lookup("#closeButton");
                if (settingItems.isEmpty()) {
                    rootStage.close();
                }

                closeButton.setOnMouseClicked(event -> {
                    setting.put("textKey", configDefaultTextKey);
                    setting.put("textSalt", configDefaultTextSalt);
                    setting.put("textAlgorithm", configDefaultTextAlgorithm);

                    setting.put("fileEnDecryptKey", configDefaultFileEnDecryptKey);
                    setting.put("fileEnDecryptSalt", configDefaultFileEnDecryptSalt);
                    setting.put("fileEnDecryptAlgorithm", configDefaultFileEnDecryptAlgorithm);

                    setting.put("fileDeleteIterations", String.valueOf(configDefaultFileDeleteIterations));

                    setting.put("fileOutputPath", configDefaultFileOutputPath);
                    setting.put("removeFromFileBox", String.valueOf(configDefaultRemoveFileFromFileBox));
                    setting.put("limitNumberOfThreads", String.valueOf(configDefaultLimitNumberOfThreads));

                    returnItems.put("default", setting);

                    rootStage.close();
                });

                PasswordField keyHideEntry = (PasswordField) loadSettingsRoot.lookup("#passwordEntryHide");
                TextField keyShowEntry = (TextField) loadSettingsRoot.lookup("#passwordEntryShow");

                CheckBox showPassword = (CheckBox) loadSettingsRoot.lookup("#showPassword");
                showPassword.setOnAction(event -> {
                    if (showPassword.isSelected()) {
                        keyShowEntry.setText(keyHideEntry.getText());
                        keyShowEntry.setVisible(true);
                        keyHideEntry.setVisible(false);
                    } else {
                        keyHideEntry.setText(keyShowEntry.getText());
                        keyHideEntry.setVisible(true);
                        keyShowEntry.setVisible(false);
                    }
                });

                ComboBox settingsBox = (ComboBox) loadSettingsRoot.lookup("#settingsBox");
                settingsBox.setItems(FXCollections.observableArrayList(settingItems.keySet()));
                settingsBox.setValue(settingItems.firstKey());
                if (!Boolean.parseBoolean(settingItems.firstEntry().getValue().get("encrypted").trim())) {
                    keyHideEntry.clear();
                    keyHideEntry.setDisable(true);
                    keyShowEntry.setDisable(true);
                    showPassword.setDisable(true);
                }
                settingsBox.setOnAction(event -> {
                    try {
                        if (!Boolean.parseBoolean(settingItems.get(settingsBox.getSelectionModel().getSelectedItem().toString()).get("encrypted").trim())) {
                            keyHideEntry.clear();
                            keyHideEntry.setDisable(true);
                            keyShowEntry.clear();
                            keyShowEntry.setDisable(true);
                            showPassword.setDisable(true);
                        } else {
                            keyHideEntry.clear();
                            keyHideEntry.setDisable(false);
                            keyShowEntry.clear();
                            keyShowEntry.setDisable(false);
                            showPassword.setDisable(false);
                        }
                    } catch (NullPointerException e) {
                        //get called when delete button is pressed
                    }
                });

                Button loadButton = (Button) loadSettingsRoot.lookup("#loadButton");
                loadButton.setOnAction(event -> {
                    String settingName = settingsBox.getSelectionModel().getSelectedItem().toString();
                    Map<String, String> selectedSetting = settingItems.get(settingName);
                    if (keyHideEntry.isDisabled() && showPassword.isDisabled() && showPassword.isDisabled()) {
                        setting.put("textKey", selectedSetting.get("textKey"));
                        setting.put("textSalt", selectedSetting.get("textSalt"));
                        setting.put("textAlgorithm", selectedSetting.get("textAlgorithm"));

                        setting.put("fileEnDecryptKey", selectedSetting.get("fileEnDecryptKey"));
                        setting.put("fileEnDecryptSalt", selectedSetting.get("fileEnDecryptSalt"));
                        setting.put("fileEnDecryptAlgorithm", selectedSetting.get("fileEnDecryptAlgorithm"));

                        setting.put("fileDeleteIterations", selectedSetting.get("fileDeleteIterations"));

                        setting.put("fileOutputPath", selectedSetting.get("fileOutputPath"));
                        setting.put("removeFromFileBox", selectedSetting.get("removeFromFileBox"));
                        setting.put("limitNumberOfThreads", selectedSetting.get("limitNumberOfThreads"));

                        returnItems.put(settingsBox.getSelectionModel().getSelectedItem().toString(), setting);

                        rootStage.close();
                    } else {
                        EnDecrypt.AES decryptSetting;
                        if (keyHideEntry.isVisible()) {
                            decryptSetting = new EnDecrypt.AES(keyHideEntry.getText(), new byte[16]);
                        } else {
                            decryptSetting = new EnDecrypt.AES(keyShowEntry.getText(), new byte[16]);
                        }
                        try {
                            Map<String, String> selectedEncryptedSetting = settingItems.get(settingName);
                            setting.put("textKey", decryptSetting.decrypt(selectedEncryptedSetting.get("textKey")));
                            setting.put("textSalt", decryptSetting.decrypt(selectedEncryptedSetting.get("textSalt")));
                            setting.put("textAlgorithm", decryptSetting.decrypt(selectedEncryptedSetting.get("textAlgorithm")));

                            setting.put("fileEnDecryptKey", decryptSetting.decrypt(selectedEncryptedSetting.get("fileEnDecryptKey")));
                            setting.put("fileEnDecryptSalt", decryptSetting.decrypt(selectedEncryptedSetting.get("fileEnDecryptSalt")));
                            setting.put("fileEnDecryptAlgorithm", decryptSetting.decrypt(selectedEncryptedSetting.get("fileEnDecryptAlgorithm")));

                            setting.put("fileDeleteIterations", String.valueOf(Integer.parseInt(decryptSetting.decrypt(selectedEncryptedSetting.get("fileDeleteIterations")))));

                            setting.put("fileOutputPath", decryptSetting.decrypt(selectedEncryptedSetting.get("fileOutputPath")));
                            setting.put("removeFromFileBox", decryptSetting.decrypt(selectedEncryptedSetting.get("removeFromFileBox")));
                            setting.put("limitNumberOfThreads", decryptSetting.decrypt(selectedEncryptedSetting.get("limitNumberOfThreads")));

                            returnItems.put(settingsBox.getSelectionModel().getSelectedItem().toString(), setting);

                            rootStage.close();
                        } catch (InvalidKeyException e) {
                            warningAlert("Wrong key is given");
                        } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
                            e.printStackTrace();
                            warningAlert("Wrong key is given or the config wasn't\nsaved correctly");
                        }
                    }
                });
                outerLoadButton[0] = loadButton;

                Button deleteButton = (Button) loadSettingsRoot.lookup("#deleteButton");
                deleteButton.setOnAction(event -> {
                    AtomicReference<Double> deleteQuestionX = new AtomicReference<>((double) 0);
                    AtomicReference<Double> deleteQuestionY = new AtomicReference<>((double) 0);
                    Alert deleteQuestion = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + settingsBox.getSelectionModel().getSelectedItem().toString() + "?", ButtonType.OK, ButtonType.CANCEL);
                    deleteQuestion.initStyle(StageStyle.UNDECORATED);
                    deleteQuestion.setTitle("Confirmation");
                    ((Stage) deleteQuestion.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Settings.class.getResource("resources/cryptoGX.png").toExternalForm()));

                    Scene window = deleteQuestion.getDialogPane().getScene();

                    window.setOnMouseDragged(dragEvent -> {
                        deleteQuestion.setX(dragEvent.getScreenX() + deleteQuestionX.get());
                        deleteQuestion.setY(dragEvent.getScreenY() + deleteQuestionY.get());
                    });
                    window.setOnMousePressed(pressEvent -> {
                        deleteQuestionX.set(window.getX() - pressEvent.getSceneX());
                        deleteQuestionY.set(window.getY() - pressEvent.getSceneY());
                    });

                    Optional<ButtonType> result = deleteQuestion.showAndWait();
                    if (result.get() == ButtonType.OK) {
                        if (settingItems.size() - 1 <= 0) {
                            for (int i = 0; i < 100; i++) {
                                if (config.isFile()) {
                                    try {
                                        SecureDelete.deleteFile(config, 5, new byte[64]);
                                        isConfig = false;
                                        rootStage.close();
                                        break;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            rootStage.close();
                        } else if (deleteSetting(config, settingsBox.getSelectionModel().getSelectedItem().toString())) {
                            settingItems.remove(settingsBox.getSelectionModel().getSelectedItem().toString());

                            settingsBox.setItems(FXCollections.observableArrayList(settingItems.keySet()));
                            settingsBox.setValue(settingItems.firstKey());
                        } else {
                            warningAlert("Couldn't delete setting '" + settingsBox.getSelectionModel().getSelectedItem().toString() + "'");
                        }
                    }
                });
            });
        });

        thread.start();

        rootStage.setScene(scene);
        rootStage.showAndWait();

        return returnItems;
    }

    /**
     * <p>Shows a GUI where the user can save the current settings</p>
     *
     * @param settingName name of the new setting
     * @param newSetting is the new setting key value pair
     *
     * @since 1.12.0
     */
    public static void addSetting(File file, String settingName, Map<String, String> newSetting) {
        TreeMap<String, Map<String, String>> settings = readSettings(file);
        settings.put(settingName, newSetting);
        writeSettings(file, settings);
    }

    /**
     * <p>Deletes a saved setting</p>
     *
     * @param settingName of the setting
     * @return if the setting could be found
     *
     * @since 1.12.0
     */
    public static boolean deleteSetting(File file, String settingName) {
        StringBuilder newConfig = new StringBuilder();
        boolean delete = false;
        boolean found = false;

        try {
            BufferedReader configReader = new BufferedReader(new FileReader(file));

            String line;

            while ((line = configReader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("[") && line.endsWith("]")) {
                    if (line.replace("[", "").replace("]", "").split(" ")[0].equals(settingName)) {
                        delete = true;
                        found = true;
                    } else if (delete) {
                        delete = false;
                        newConfig.append(line).append("\n");
                    } else {
                        newConfig.append(line).append("\n");
                    }
                } else if (!delete) {
                    newConfig.append(line).append("\n");
                }
            }

            configReader.close();

            BufferedWriter configFile = new BufferedWriter(new FileWriter(file));
            configFile.write(newConfig.toString());
            configFile.newLine();

            configFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return found;
    }

    /**
     * <p>Reads all settings saved in a file</>
     *
     * @param file from which the settings should be read from
     * @return the settings
     *
     * @since 1.12.0
     */
    public static TreeMap<String, Map<String, String>> readSettings(File file) {
        TreeMap<String, Map<String, String>> returnMap = new TreeMap<>();
        String settingName = null;
        Map<String, String> settingValues = new HashMap<>();

        try {
            BufferedReader configReader = new BufferedReader(new FileReader(file));

            String line;

            while ((line = configReader.readLine()) != null) {

                if (line.isEmpty()) {
                    continue;
                } else if (line.startsWith("[") && line.endsWith("]")) {
                    if (settingName != null) {
                        returnMap.put(settingName, settingValues);
                        settingValues = new HashMap<>();
                    }
                    String[] newSetting = line.replace("[", "").replace("]", "").split(" ");
                    settingName = newSetting[0].trim();
                    String[] encoded = newSetting[1].split("=");
                    settingValues.put("encrypted", encoded[1]);
                } else {
                    String[] keyValue = line.split("=");
                    try {
                        settingValues.put(keyValue[0], keyValue[1]);
                    } catch (IndexOutOfBoundsException e) {
                        settingValues.put(keyValue[0], "");
                    }
                }
            }

            if (settingName != null) {
                returnMap.put(settingName, settingValues);
            }

            configReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            warningAlert("Couldn't find file '" + file.getAbsolutePath() + "'"); // this should never raise
        } catch (IOException e) {
            e.printStackTrace();
            errorAlert("An IO Exception occurred", e.getMessage());
        }

        return returnMap;
    }

    /**
     * <p>Writes settings (could be more than one) to a file</p>
     *
     * @param file where the settings should be written in
     * @param settings of the user
     *
     * @since 1.12.0
     */
    public static void writeSettings(File file, TreeMap<String, Map<String, String>> settings) {
        try {
            BufferedWriter configWriter = new BufferedWriter(new FileWriter(file));

            for (Map.Entry<String, Map<String, String>> settingElement: settings.entrySet()) {
                    configWriter.write("[" + settingElement.getKey() + " encrypted=" + Boolean.parseBoolean(settingElement.getValue().get("encrypted")) + "]");
                    configWriter.newLine();
                    for (Map.Entry<String, String> entry : settingElement.getValue().entrySet()) {
                        String key = entry.getKey();
                        if (!key.equals("encrypted")) {
                            configWriter.write(entry.getKey() + "=" + entry.getValue());
                            configWriter.newLine();
                        }
                    }
                }
                configWriter.newLine();

            configWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            errorAlert("An error occurred while saving the settings", e.getMessage());
        }
    }

}
