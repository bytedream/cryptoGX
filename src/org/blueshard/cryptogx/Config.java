package org.blueshard.cryptogx;

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
import javax.xml.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.blueshard.cryptogx.Main.*;

/**
 * <p>Class for the user configuration / settings</p>
 */
public class Config {

    private static double addConfigGUIX, addConfigGUIY;

    private static final HashSet<String> protectedConfigNames = new HashSet<>(Arrays.asList("cryptoGX", "config"));

    /**
     * <p>Shows a GUI where the user can save settings, which can load later</p>
     *
     * @param rootWindow from which this GUI will get called
     * @param userSetting
     * @throws IOException
     */
    public static void addSettingGUI(Window rootWindow, Map<String, String> userSetting) throws IOException {
        Map<String, String> newSettingItems = new HashMap<>();

        Stage rootStage = new Stage();
        rootStage.initOwner(rootWindow);
        Parent addSettingsRoot = FXMLLoader.load(Config.class.getResource("resources/addSettingsGUI.fxml"));
        rootStage.initStyle(StageStyle.UNDECORATED);
        rootStage.initModality(Modality.WINDOW_MODAL);
        rootStage.setResizable(false);
        rootStage.setTitle("cryptoGX");
        Scene scene = new Scene(addSettingsRoot, 320, 605);

        rootStage.setScene(scene);

        scene.setOnMouseDragged(event -> {
            rootStage.setX(event.getScreenX() + addConfigGUIX);
            rootStage.setY(event.getScreenY() + addConfigGUIY);
        });
        scene.setOnMousePressed(event -> {
            addConfigGUIX = scene.getX() - event.getSceneX();
            addConfigGUIY = scene.getY() - event.getSceneY();
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
                    rootStage.setX(event.getScreenX() + addConfigGUIX);
                    rootStage.setY(event.getScreenY() + addConfigGUIY);
                });
                menuBar.setOnMousePressed(event -> {
                    addConfigGUIX = menuBar.getLayoutX() - event.getSceneX();
                    addConfigGUIY = menuBar.getLayoutY() - event.getSceneY();
                });

                ImageView closeButton = (ImageView) addSettingsRoot.lookup("#closeButton");
                closeButton.setOnMouseClicked(event -> rootStage.close());

                TextField settingsNameEntry = (TextField) addSettingsRoot.lookup("#settingsNameEntry");

                TextField textKeyEntry = (TextField) addSettingsRoot.lookup("#textKeyEntry");
                textKeyEntry.setText(userSetting.get("textKey"));
                TextField textSaltEntry = (TextField) addSettingsRoot.lookup("#textSaltEntry");
                textSaltEntry.setText(userSetting.get("textSalt"));
                ComboBox textAlgorithmBox = (ComboBox) addSettingsRoot.lookup("#textAlgorithmComboBox");
                textAlgorithmBox.setItems(FXCollections.observableArrayList(textAlgorithms));
                textAlgorithmBox.setValue(userSetting.get("textAlgorithm"));

                TextField fileEnDecryptKeyEntry = (TextField) addSettingsRoot.lookup("#fileEnDecryptKeyEntry");
                fileEnDecryptKeyEntry.setText(userSetting.get("fileEnDecryptKey"));
                TextField fileEnDecryptSaltEntry = (TextField) addSettingsRoot.lookup("#fileEnDecryptSaltEntry");
                fileEnDecryptSaltEntry.setText(userSetting.get("fileEnDecryptSalt"));
                ComboBox fileEnDecryptAlgorithmBox = (ComboBox) addSettingsRoot.lookup("#fileEnDecryptAlgorithmComboBox");
                fileEnDecryptAlgorithmBox.setItems(FXCollections.observableArrayList(fileEnDecryptAlgorithms));
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
                CheckBox removeFromFileBoxCheckBox = (CheckBox) addSettingsRoot.lookup("#removeFromFileBoxCheckBox");
                removeFromFileBoxCheckBox.setSelected(Boolean.parseBoolean(userSetting.get("removeFromFileBox")));
                CheckBox limitNumberOfThreadsCheckBox = (CheckBox) addSettingsRoot.lookup("#limitNumberOfThreadsCheckBox");
                limitNumberOfThreadsCheckBox.setSelected(Boolean.parseBoolean(userSetting.get("limitNumberOfThreads")));

                PasswordField hiddenPasswordEntry = (PasswordField) addSettingsRoot.lookup("#hiddenPasswordEntry");
                TextField showedPasswordEntry = (TextField) addSettingsRoot.lookup("#showedPasswordEntry");
                CheckBox showPassword = (CheckBox) addSettingsRoot.lookup("#showPassword");

                showPassword.setOnAction(event -> {
                    if (showPassword.isSelected()) {
                        showedPasswordEntry.setText(hiddenPasswordEntry.getText());
                        showedPasswordEntry.setVisible(true);
                        hiddenPasswordEntry.setVisible(false);
                    } else {
                        hiddenPasswordEntry.setText(showedPasswordEntry.getText());
                        hiddenPasswordEntry.setVisible(true);
                        showedPasswordEntry.setVisible(false);
                    }
                });
                CheckBox encryptSettings = (CheckBox) addSettingsRoot.lookup("#encryptSettings");
                encryptSettings.setOnAction(event -> {
                    if (encryptSettings.isSelected()) {
                        hiddenPasswordEntry.setDisable(false);
                        showedPasswordEntry.setDisable(false);
                        showPassword.setDisable(false);
                    } else {
                        hiddenPasswordEntry.setDisable(true);
                        showedPasswordEntry.setDisable(true);
                        showPassword.setDisable(true);
                    }
                });
                Button saveButton = (Button) addSettingsRoot.lookup("#saveButton");
                saveButton.setOnAction(event -> {
                    if (settingsNameEntry.getText().trim().isEmpty()) {
                        warningAlert("Add a name for the setting");
                    } else if (protectedConfigNames.contains(settingsNameEntry.getText())) {
                        warningAlert("Please choose another name for this setting");
                    } else if (encryptSettings.isSelected()){
                        try {
                            EnDecrypt.AES encrypt;
                            if (!hiddenPasswordEntry.isDisabled()) {
                                encrypt = new EnDecrypt.AES(hiddenPasswordEntry.getText(), new byte[16]);
                                newSettingItems.put("encryptHash", encrypt.encrypt(hiddenPasswordEntry.getText()));
                            } else {
                                encrypt = new EnDecrypt.AES(showedPasswordEntry.getText(), new byte[16]);
                                newSettingItems.put("encryptHash", encrypt.encrypt(showedPasswordEntry.getText()));
                            }

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

                            addSetting(settingsNameEntry.getText(), newSettingItems);

                            rootStage.close();
                        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
                            e.printStackTrace();
                        }
                    } else {
                        newSettingItems.put("encryptHash", "");

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

                        addSetting(settingsNameEntry.getText(), newSettingItems);

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
     */
    public static void exportSettingsGUI(Window rootWindow) throws IOException {
        Stage rootStage = new Stage();
        rootStage.initOwner(rootWindow);
        Parent exportSettingsRoot = FXMLLoader.load(Config.class.getResource("resources/exportSettingsGUI.fxml"));
        rootStage.initStyle(StageStyle.UNDECORATED);
        rootStage.initModality(Modality.WINDOW_MODAL);
        rootStage.setResizable(false);
        rootStage.setTitle("cryptoGX");
        Scene scene = new Scene(exportSettingsRoot, 254, 253);

        rootStage.setScene(scene);

        scene.setOnMouseDragged(event -> {
            rootStage.setX(event.getScreenX() + addConfigGUIX);
            rootStage.setY(event.getScreenY() + addConfigGUIY);
        });
        scene.setOnMousePressed(event -> {
            addConfigGUIX = scene.getX() - event.getSceneX();
            addConfigGUIY = scene.getY() - event.getSceneY();
        });

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MenuBar menuBar = (MenuBar) exportSettingsRoot.lookup("#menuBar");
            menuBar.setOnMouseDragged(event -> {
                rootStage.setX(event.getScreenX() + addConfigGUIX);
                rootStage.setY(event.getScreenY() + addConfigGUIY);
            });
            menuBar.setOnMousePressed(event -> {
                addConfigGUIX = menuBar.getLayoutX() - event.getSceneX();
                addConfigGUIY = menuBar.getLayoutY() - event.getSceneY();
            });
            ImageView closeButton = (ImageView) exportSettingsRoot.lookup("#closeButton");
            closeButton.setOnMouseClicked(event -> rootStage.close());

            VBox settingsBox = (VBox) exportSettingsRoot.lookup("#settingsBox");
            Platform.runLater(() -> readUserSettings().keySet().forEach(s -> {
                CheckBox newCheckBox = new CheckBox();
                newCheckBox.setText(s);
                settingsBox.getChildren().add(newCheckBox);
            }));

            Button exportButton = (Button) exportSettingsRoot.lookup("#exportButton");
            exportButton.setOnAction(event -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Export settings");
                fileChooser.setInitialFileName("settings.config");
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Config files", "*.config", "*.xml"),
                        new FileChooser.ExtensionFilter("All files", "*.*"));
                File file = fileChooser.showSaveDialog(exportSettingsRoot.getScene().getWindow());
                if (file != null) {
                    TreeMap<String, Map<String, String>> writeInfos = new TreeMap<>();
                    TreeMap<String, Map<String, String>> settings = readUserSettings();
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
                    writeConfig(file, writeInfos);
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
     */
    public static TreeMap<String, Map<String, String>> loadSettingsGUI(Window rootWindow) throws IOException {
        Button[] outerLoadButton = new Button[1];
        HashMap<String, String> setting = new HashMap<>();
        TreeMap<String, Map<String, String>> settingItems = readUserSettings();
        TreeMap<String, Map<String, String>> returnItems = new TreeMap<>();

        Stage rootStage = new Stage();
        rootStage.initOwner(rootWindow);
        AnchorPane loadSettingsRoot = FXMLLoader.load(Config.class.getResource("resources/loadSettingsGUI.fxml"));
        rootStage.initStyle(StageStyle.UNDECORATED);
        rootStage.initModality(Modality.WINDOW_MODAL);
        rootStage.setResizable(false);
        rootStage.setTitle("cryptoGX");
        rootStage.getIcons().add(new Image(Config.class.getResource("resources/cryptoGX.png").toExternalForm()));
        Scene scene = new Scene(loadSettingsRoot, 242, 235);

        scene.setOnMouseDragged(event -> {
            rootStage.setX(event.getScreenX() + addConfigGUIX);
            rootStage.setY(event.getScreenY() + addConfigGUIY);
        });
        scene.setOnMousePressed(event -> {
            addConfigGUIX = scene.getX() - event.getSceneX();
            addConfigGUIY = scene.getY() - event.getSceneY();
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
                    rootStage.setX(event.getScreenX() + addConfigGUIX);
                    rootStage.setY(event.getScreenY() + addConfigGUIY);
                });
                menuBar.setOnMousePressed(event -> {
                    addConfigGUIX = menuBar.getLayoutX() - event.getSceneX();
                    addConfigGUIY = menuBar.getLayoutY() - event.getSceneY();
                });

                ImageView closeButton = (ImageView) loadSettingsRoot.lookup("#closeButton");
                if (settingItems.isEmpty()) {
                    rootStage.close();
                }

                closeButton.setOnMouseClicked(event -> {
                    setting.put("encryptHash", configDefaultEncryptHash);

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
                if (settingItems.firstEntry().getValue().get("encryptHash").trim().isEmpty()) {
                    keyHideEntry.clear();
                    keyHideEntry.setDisable(true);
                    keyShowEntry.setDisable(true);
                    showPassword.setDisable(true);
                }
                settingsBox.setOnAction(event -> {
                    try {
                        if (settingItems.get(settingsBox.getSelectionModel().getSelectedItem().toString()).get("encryptHash").trim().isEmpty()) {
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
                        setting.put("encryptHash", "");

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
                            if (keyHideEntry.isVisible() && !decryptSetting.encrypt(keyHideEntry.getText()).equals(settingItems.get(settingsBox.getSelectionModel().getSelectedItem().toString()).get("encryptHash").trim())) {
                                warningAlert("Wrong key is given");
                            } else if (keyShowEntry.isVisible() && !decryptSetting.encrypt(keyShowEntry.getText()).equals(settingItems.get(settingsBox.getSelectionModel().getSelectedItem().toString()).get("encryptHash").trim())) {
                                warningAlert("Wrong key is given");
                            } else {
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
                            }
                        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException e) {
                            e.printStackTrace();
                            warningAlert("Wrong key is given");
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
                    ((Stage) deleteQuestion.getDialogPane().getScene().getWindow()).getIcons().add(new Image(Config.class.getResource("resources/cryptoGX.png").toExternalForm()));

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
                        deleteUserSetting(settingsBox.getSelectionModel().getSelectedItem().toString());
                        settingItems.clear();
                        settingItems.putAll(readUserSettings());
                        if (settingItems.size() == 0) {
                            for (int i=0; i<100; i++) {
                                if (config.isFile()) {
                                    if (config.delete()) {
                                        isConfig = false;
                                        rootStage.close();
                                        break;
                                    }
                                }
                            }
                            rootStage.close();
                            return;
                        }
                        settingsBox.setItems(FXCollections.observableArrayList(settingItems.keySet()));
                        settingsBox.setValue(settingItems.firstKey());
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
     * @param userSetting the current settings
     */
    public static void addSetting(String settingName, Map<String, String> userSetting) {
        TreeMap<String, Map<String, String>> newConfig = new TreeMap<>(readUserSettings());
        newConfig.put(settingName, userSetting);
        writeConfig(newConfig);
    }

    /**
     * <p>Shows a GUI where the user can save the current settings</p>
     *
     * @param settingName name of the new setting
     * @param userSetting the current settings
     * @param encryptPassword to encrypt the settings
     */
    public static void addSetting(String settingName, Map<String, String> userSetting, String encryptPassword) {
        TreeMap<String, Map<String, String>> newConfig = new TreeMap<>(readUserSettings());
        newConfig.put(settingName, userSetting);
        writeConfig(newConfig, Collections.singletonMap(settingName, encryptPassword));
    }

    /**
     * <p>Deletes a saved setting</p>
     *
     * @param name of the setting
     * @return if the setting could be found
     */
    public static boolean deleteUserSetting(String name) {
        TreeMap<String, Map<String, String>> newSetting = new TreeMap<>();
        TreeMap<String, Map<String, String>> oldSetting = readUserSettings();
        boolean found = false;

        for (Map.Entry<String, Map<String, String>> entry: oldSetting.entrySet()) {
            if (!entry.getKey().equals(name)) {
                newSetting.put(entry.getKey(), entry.getValue());
            } else {
                found = true;
            }
        }
        writeConfig(newSetting);
        return found;
    }

    public static TreeMap<String, Map<String, String>> readUserSettings() {
        return readUserSettings(config);
    }

    /**
     * @see Config#readUserSettings(String)
     */
    public static TreeMap<String, Map<String, String>> readUserSettings(File file) {
        TreeMap<String, Map<String, String>> rootInfos = new TreeMap<>();
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            XMLStreamReader xmlStreamReader;
            try {
                xmlStreamReader = xmlInputFactory.createXMLStreamReader(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                return rootInfos;
            }

            HashMap<String, String> infos = new HashMap<>();

            String infoName = null;
            StringBuilder infoCharacters = new StringBuilder();
            String rootName = null;

            while (xmlStreamReader.hasNext()) {

                int eventType = xmlStreamReader.next();

                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        String startTag = xmlStreamReader.getLocalName().trim();
                        if (startTag != null) {
                            if (protectedConfigNames.contains(startTag)) {
                                continue;
                            } else if (rootName == null) {
                                rootName = startTag;
                            } else {
                                infoName = startTag;
                            }
                        }
                        break;
                    case XMLStreamReader.CHARACTERS:
                        if (infoName != null) {
                            if (!xmlStreamReader.getText().trim().equals("")) {
                                infoCharacters.append(xmlStreamReader.getText());
                            }
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        String endTag = xmlStreamReader.getLocalName().trim();
                        if (endTag != null) {
                            if (protectedConfigNames.contains(endTag)) {
                                continue;
                            } else if (endTag.equals(rootName)) {
                                rootInfos.put(rootName, infos);
                                rootName = null;
                                infos = new HashMap<>();
                                infoCharacters = new StringBuilder();
                            } else {
                                infos.put(infoName, infoCharacters.toString());
                                infoName = null;
                                infoCharacters = new StringBuilder();
                            }
                        }
                        break;
                }
            }
            xmlStreamReader.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        System.out.println(rootInfos);

        return rootInfos;
    }

    /**
     * <p>Shows a GUI where the user can choose and load saved settings </p>
     *
     * @param filename of the file with the settings
     * @return the setting that the user has chosen
     */
    public static TreeMap<String, Map<String, String>> readUserSettings(String filename) {
        return readUserSettings(new File(filename));
    }

    /**
     * <p>Writes settings (could be more than one) to the pre-defined config file</p>
     *
     * @see Config#writeConfig(File, TreeMap, Map)
     */
    public static void writeConfig(TreeMap<String, Map<String, String>> userSettings) {
        writeConfig(config, userSettings, null);
    }

    /**
     * <p>Writes settings (could be more than one) to the pre-defined config file</p>
     *
     * @see Config#writeConfig(File, TreeMap, Map)
     */
    public static void writeConfig(TreeMap<String, Map<String, String>> userSettings, Map<String, String> encryptedSettings) {
        writeConfig(config, userSettings, encryptedSettings);
    }

    /**
     * <p>Writes settings (could be more than one) to the pre-defined config file</p>
     *
     * @see Config#writeConfig(String, TreeMap, Map)
     */
    public static void writeConfig(String filename, TreeMap<String, Map<String, String>> userSettings) {
        writeConfig(filename, userSettings, null);
    }

    /**
     * <p>Writes settings (could be more than one) to the pre-defined config file</p>
     *
     * @see Config#writeConfig(File, TreeMap, Map)
     */
    public static void writeConfig(File file, TreeMap<String, Map<String, String>> userSettings) {
        writeConfig(file, userSettings, null);
    }

    /**
     * <p>Writes settings (could be more than one) to a file</p>
     *
     * @see Config#writeConfig(String, TreeMap, Map)
     */
    public static void writeConfig(String filename, TreeMap<String, Map<String, String>> userSettings, Map<String, String> encryptedSettings) {
        writeConfig(new File(filename), userSettings, encryptedSettings);
    }

    /**
     * <p>Writes settings (could be more than one) to a file</p>
     *
     * @param file where the settings should be written in
     * @param userSettings of the user
     * @param encryptedSettings says which settings from {@param userSettings} should be encrypted with a password
     */
    public static void writeConfig(File file, TreeMap<String, Map<String, String>> userSettings, Map<String, String> encryptedSettings) {
        EnDecrypt.AES encryptSetting;
        StringWriter stringWriter = new StringWriter();

        if (encryptedSettings == null) {
            encryptedSettings = new HashMap<>();
        }

        try {
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter(stringWriter);

            xmlStreamWriter.writeStartDocument();
            xmlStreamWriter.writeStartElement("cryptoGX");
            for (Map.Entry<String, Map<String, String>> settingElement: userSettings.entrySet()) {
                xmlStreamWriter.writeStartElement(settingElement.getKey());
                if (encryptedSettings.containsKey(settingElement.getKey())) {
                    encryptSetting = new EnDecrypt.AES(settingElement.getKey(), new byte[16]);
                    for (Map.Entry<String, String> entry: settingElement.getValue().entrySet()) {
                        xmlStreamWriter.writeStartElement(entry.getKey());
                        xmlStreamWriter.writeCharacters(encryptSetting.encrypt(entry.getValue()));
                        xmlStreamWriter.writeEndElement();
                    }
                } else {
                    for (Map.Entry<String, String> entry: settingElement.getValue().entrySet()) {
                        xmlStreamWriter.writeStartElement(entry.getKey());
                        xmlStreamWriter.writeCharacters(entry.getValue());
                        xmlStreamWriter.writeEndElement();
                    }
                }
                xmlStreamWriter.writeEndElement();
            }
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.writeEndDocument();

            //prettify

            Source xmlInput = new StreamSource(new StringReader(stringWriter.toString()));
            StringWriter prettifyStringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(prettifyStringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 2);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(xmlInput, xmlOutput);

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            for (String s: prettifyStringWriter.getBuffer().toString().split(System.lineSeparator())) {
                bufferedWriter.write(s);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (XMLStreamException | IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | TransformerException e) {
            e.printStackTrace();
        }
    }

}
