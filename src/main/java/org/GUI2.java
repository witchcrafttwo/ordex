package org;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GUI2 {

    private final boolean startInTray;
    private final ObservableList<String> rules = FXCollections.observableArrayList();
    private final ObservableList<String> rulePreview = FXCollections.observableArrayList();
    private final ArrayList<PropertySettings.SavedRule> savedRules = new ArrayList<>();
    private final PropertySettings settings = new PropertySettings();
    private final StartupManager startupManager = new StartupManager();
    private TrayIcon trayIcon;
    private ToggleButton autoStartButtonRef;
    private CheckboxMenuItem autoStartMenuRef;

    private File selectedWatchFolder;
    private File selectedDestinationFolder;

    public GUI2() {
        this(false);
    }

    public GUI2(boolean startInTray) {
        this.startInTray = startInTray;
    }

    public void show(Stage stage) {
        TextField watchFolderField = new TextField();
        watchFolderField.setEditable(false);

        TextField keywordField = new TextField();
        TextField extensionField = new TextField();
        TextField destinationField = new TextField();
        destinationField.setEditable(false);

        ListView<String> ruleListView = new ListView<>(rules);
        ruleListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        ListView<String> previewListView = new ListView<>(rulePreview);
        previewListView.setFocusTraversable(false);

        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(5);

        ToggleButton autoStartButton = new ToggleButton();
        autoStartButton.setMinWidth(140);
        setupAutoStartButton(autoStartButton, logArea);

        Button selectWatchButton = new Button("選択");
        selectWatchButton.setOnAction(e -> {
            File folder = chooseDirectory(stage, "監視フォルダを選択");
            if (folder == null) {
                appendLog(logArea, "監視フォルダの選択をキャンセルしました。");
                return;
            }

            selectedWatchFolder = folder;
            watchFolderField.setText(folder.getAbsolutePath());
            appendLog(logArea, "監視フォルダ: " + folder.getAbsolutePath());
        });

        Button addKeywordButton = new Button("追加");
        addKeywordButton.setOnAction(e -> {
            String keyword = normalize(keywordField.getText());
            if (keyword.isEmpty()) {
                appendLog(logArea, "キーワードが空です。");
                return;
            }

            rulePreview.add("キーワード: " + keyword);
            keywordField.clear();
            appendLog(logArea, "キーワードを追加しました。");
        });

        Button addExtensionButton = new Button("追加");
        addExtensionButton.setOnAction(e -> {
            String extension = normalize(extensionField.getText());
            if (extension.isEmpty()) {
                appendLog(logArea, "拡張子が空です。");
                return;
            }

            if (!extension.startsWith(".")) {
                extension = "." + extension;
            }

            rulePreview.add("拡張子: " + extension);
            extensionField.clear();
            appendLog(logArea, "拡張子を追加しました。");
        });

        Button selectDestinationButton = new Button("選択");
        selectDestinationButton.setOnAction(e -> {
            File folder = chooseDirectory(stage, "保存先フォルダを選択");
            if (folder == null) {
                appendLog(logArea, "保存先フォルダの選択をキャンセルしました。");
                return;
            }

            selectedDestinationFolder = folder;
            destinationField.setText(folder.getAbsolutePath());
            syncDestinationPreview(destinationField.getText());
            appendLog(logArea, "保存先フォルダ: " + folder.getAbsolutePath());
        });

        Button addRuleButton = new Button("ルール追加");
        addRuleButton.setMaxWidth(Double.MAX_VALUE);
        addRuleButton.setOnAction(e -> {
            if (selectedWatchFolder == null) {
                appendLog(logArea, "監視フォルダを先に選択してください。");
                return;
            }
            if (selectedDestinationFolder == null) {
                appendLog(logArea, "保存先フォルダを先に選択してください。");
                return;
            }
            if (rulePreview.isEmpty()) {
                appendLog(logArea, "キーワードまたは拡張子を追加してください。");
                return;
            }

            String summary = createRuleSummary(selectedWatchFolder.getAbsolutePath(), selectedDestinationFolder.getAbsolutePath());
            rules.add(summary);
            saveCurrentRule();
            appendLog(logArea, "ルールを追加しました。");
            startWatch(logArea);
        });

        Button clearPreviewButton = new Button("入力クリア");
        clearPreviewButton.setMaxWidth(Double.MAX_VALUE);
        clearPreviewButton.setOnAction(e -> {
            keywordField.clear();
            extensionField.clear();
            destinationField.clear();
            selectedDestinationFolder = null;
            rulePreview.clear();
            appendLog(logArea, "入力中のルールをクリアしました。");
        });

        Button deleteRuleButton = new Button("ルール削除");
        deleteRuleButton.setMaxWidth(Double.MAX_VALUE);
        deleteRuleButton.setOnAction(e -> deleteSelectedRule(ruleListView, watchFolderField, destinationField, keywordField, extensionField, logArea));

        ruleListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            int selectedIndex = ruleListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < savedRules.size()) {
                loadRuleToEditor(savedRules.get(selectedIndex), watchFolderField, destinationField);
            }
            appendLog(logArea, "ルールを選択しました: " + newValue);
        });

        loadSavedRule(watchFolderField, destinationField, logArea);
        startWatchIfConfigured(logArea);

        VBox watchPane = createWatchPane(watchFolderField, selectWatchButton);
        VBox ruleEditorPane = createRuleEditorPane(
                keywordField,
                extensionField,
                destinationField,
                addKeywordButton,
                addExtensionButton,
                selectDestinationButton,
                addRuleButton,
                clearPreviewButton,
                deleteRuleButton);

        GridPane centerGrid = new GridPane();
        centerGrid.setHgap(12);
        centerGrid.setVgap(12);
        centerGrid.add(createTitledPane("ルール一覧", ruleListView), 0, 0);
        centerGrid.add(createTitledPane("現在のルール詳細", previewListView), 1, 0);

        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(50);
        column.setHgrow(Priority.ALWAYS);
        centerGrid.getColumnConstraints().addAll(column, column);

        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        centerGrid.getRowConstraints().add(row);

        VBox logPane = createTitledPane("Log", logArea);
        VBox.setVgrow(logPane, Priority.ALWAYS);

        Label autoStartLabel = new Label("起動オプション");
        autoStartLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox autoStartHeader = new HBox(8, autoStartLabel, spacer, autoStartButton);
        autoStartHeader.setAlignment(Pos.CENTER_LEFT);

        HBox topRow = new HBox(12, watchPane, ruleEditorPane);
        HBox.setHgrow(watchPane, Priority.ALWAYS);
        HBox.setHgrow(ruleEditorPane, Priority.ALWAYS);
        VBox topContainer = new VBox(8, autoStartHeader, topRow);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setTop(topContainer);
        root.setCenter(centerGrid);
        root.setBottom(logPane);
        BorderPane.setMargin(topRow, new Insets(0, 0, 12, 0));
        BorderPane.setMargin(centerGrid, new Insets(0, 0, 12, 0));

        Scene scene = new Scene(root, 980, 640);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setTitle("ordex");
        stage.setScene(scene);
        setupBackgroundMode(stage, logArea);
        stage.show();
        if (startInTray && SystemTray.isSupported()) {
            stage.hide();
            appendLog(logArea, "自動起動のためシステムトレイで開始しました。");
        }
    }

    private VBox createWatchPane(TextField watchFolderField, Button selectWatchButton) {
        Label title = new Label("監視フォルダ");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox row = new HBox(8, watchFolderField, selectWatchButton);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(watchFolderField, Priority.ALWAYS);

        VBox pane = new VBox(12, title, row);
        pane.setPadding(new Insets(14));
        pane.setStyle("-fx-border-color: #9aa4b2; -fx-border-radius: 4; -fx-background-color: #f8fafc;");
        pane.setPrefWidth(420);
        return pane;
    }

    private VBox createRuleEditorPane(
            TextField keywordField,
            TextField extensionField,
            TextField destinationField,
            Button addKeywordButton,
            Button addExtensionButton,
            Button selectDestinationButton,
            Button addRuleButton,
            Button clearPreviewButton,
            Button deleteRuleButton) {
        Label title = new Label("ルール");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(10);

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(70);
        ColumnConstraints inputCol = new ColumnConstraints();
        inputCol.setHgrow(Priority.ALWAYS);
        ColumnConstraints buttonCol = new ColumnConstraints();
        buttonCol.setMinWidth(90);
        form.getColumnConstraints().addAll(labelCol, inputCol, buttonCol);

        addFormRow(form, 0, "キーワード", keywordField, addKeywordButton);
        addFormRow(form, 1, "拡張子", extensionField, addExtensionButton);
        addFormRow(form, 2, "保存先", destinationField, selectDestinationButton);

        HBox actionRow = new HBox(8, addRuleButton, clearPreviewButton, deleteRuleButton);
        HBox.setHgrow(addRuleButton, Priority.ALWAYS);
        HBox.setHgrow(clearPreviewButton, Priority.ALWAYS);
        HBox.setHgrow(deleteRuleButton, Priority.ALWAYS);

        VBox pane = new VBox(12, title, form, actionRow);
        pane.setPadding(new Insets(14));
        pane.setStyle("-fx-border-color: #9aa4b2; -fx-border-radius: 4; -fx-background-color: #f8fafc;");
        return pane;
    }

    private void addFormRow(GridPane form, int rowIndex, String labelText, TextField field, Button actionButton) {
        Label label = new Label(labelText + ":");
        form.add(label, 0, rowIndex);
        form.add(field, 1, rowIndex);
        form.add(actionButton, 2, rowIndex);
    }

    private VBox createTitledPane(String titleText, Region content) {
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox pane = new VBox(10, title, content);
        pane.setPadding(new Insets(14));
        pane.setStyle("-fx-border-color: #9aa4b2; -fx-border-radius: 4; -fx-background-color: white;");
        VBox.setVgrow(content, Priority.ALWAYS);
        return pane;
    }

    private File chooseDirectory(Stage stage, String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        return chooser.showDialog(stage);
    }

    private void syncDestinationPreview(String destinationPath) {
        rulePreview.removeIf(item -> item.startsWith("保存先: "));
        rulePreview.add("保存先: " + destinationPath);
    }

    private String createRuleSummary(String watchPath, String destinationPath) {
        long keywordCount = rulePreview.stream().filter(item -> item.startsWith("キーワード: ")).count();
        long extensionCount = rulePreview.stream().filter(item -> item.startsWith("拡張子: ")).count();
        return "監視: " + watchPath
                + " | 保存先: " + destinationPath
                + " | キーワード " + keywordCount
                + "件 | 拡張子 " + extensionCount + "件";
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim();
    }

    private void appendLog(TextArea logArea, String message) {
        if (!logArea.getText().isEmpty()) {
            logArea.appendText(System.lineSeparator());
        }
        logArea.appendText(message);
    }

    /**
     * GUIで入力した内容をバックエンド処理につなぐ起点。
     * 実処理はこのメソッドから呼ぶと役割が分かれます。
     */
    private void startWatch(TextArea logArea) {
        if (selectedWatchFolder == null || selectedDestinationFolder == null) {
            appendLog(logArea, "監視フォルダと保存先フォルダを選択してください。");
            return;
        }

        List<String> keywords = extractValues("キーワード: ");
        List<String> extensions = extractValues("拡張子: ")
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        if (keywords.isEmpty() && extensions.isEmpty()) {
            appendLog(logArea, "キーワードまたは拡張子を設定してください。");
            return;
        }

        PropertySettings.SavedRule rule = new PropertySettings.SavedRule(
                selectedWatchFolder.getAbsolutePath(),
                selectedDestinationFolder.getAbsolutePath(),
                keywords,
                extensions);
        startWatchForRule(rule, logArea);
    }

    private void startWatchForRule(PropertySettings.SavedRule rule, TextArea logArea) {
        if (rule.getWatchPath() == null || rule.getDestinationPath() == null) {
            appendLog(logArea, "監視設定が不完全なルールはスキップします。");
            return;
        }
        if (rule.getKeywords().isEmpty() && rule.getExtensions().isEmpty()) {
            appendLog(logArea, "条件が空のルールはスキップします。");
            return;
        }

        File watchFolder = new File(rule.getWatchPath());
        File destinationFolder = new File(rule.getDestinationPath());
        List<String> keywords = rule.getKeywords();
        List<String> extensions = rule.getExtensions()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        appendLog(logArea, "監視処理を開始します: " + watchFolder.getAbsolutePath() + " -> " + destinationFolder.getAbsolutePath());
        Thread watcherThread = new Thread(() -> {
            try {
                FileWatcher.watchservice(
                        watchFolder,
                        destinationFolder,
                        new java.util.ArrayList<>(keywords),
                        new java.util.ArrayList<>(extensions));
            } catch (Exception ex) {
                Platform.runLater(() -> appendLog(logArea, "監視処理でエラー: " + ex.getMessage()));
            }
        }, "ordex-watcher");
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    private void saveCurrentRule() {
        List<String> keywords = extractValues("キーワード: ");
        List<String> extensions = extractValues("拡張子: ");
        PropertySettings.SavedRule rule = new PropertySettings.SavedRule(
                selectedWatchFolder.getAbsolutePath(),
                selectedDestinationFolder.getAbsolutePath(),
                keywords,
                extensions);
        savedRules.add(rule);
        settings.writeRules(savedRules);
    }

    private void loadSavedRule(TextField watchFolderField, TextField destinationField, TextArea logArea) {
        savedRules.clear();
        savedRules.addAll(settings.readRules());
        if (savedRules.isEmpty()) {
            appendLog(logArea, "保存済みルールはありません。");
            return;
        }

        rules.clear();
        for (PropertySettings.SavedRule savedRule : savedRules) {
            String watchPath = savedRule.getWatchPath() == null ? "(未設定)" : savedRule.getWatchPath();
            String destinationPath = savedRule.getDestinationPath() == null ? "(未設定)" : savedRule.getDestinationPath();
            rules.add(createRuleSummary(watchPath, destinationPath, savedRule.getKeywords().size(), savedRule.getExtensions().size()));
        }

        loadRuleToEditor(savedRules.get(0), watchFolderField, destinationField);
        appendLog(logArea, "保存済みルールを " + savedRules.size() + " 件読み込みました。");
    }


    private void startWatchIfConfigured(TextArea logArea) {
        if (savedRules.isEmpty()) {
            return;
        }
        int startedCount = 0;
        for (PropertySettings.SavedRule rule : savedRules) {
            if (rule.getWatchPath() == null || rule.getDestinationPath() == null) {
                continue;
            }
            if (rule.getKeywords().isEmpty() && rule.getExtensions().isEmpty()) {
                continue;
            }
            startWatchForRule(rule, logArea);
            startedCount++;
        }
        if (startedCount == 0) {
            appendLog(logArea, "保存済みルールに有効な監視条件がないため監視は開始しません。");
        }
    }

    private void deleteSelectedRule(
            ListView<String> ruleListView,
            TextField watchFolderField,
            TextField destinationField,
            TextField keywordField,
            TextField extensionField,
            TextArea logArea) {
        String selected = ruleListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            appendLog(logArea, "削除するルールを選択してください。");
            return;
        }

        int selectedIndex = ruleListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < rules.size()) {
            rules.remove(selectedIndex);
        }
        if (selectedIndex >= 0 && selectedIndex < savedRules.size()) {
            savedRules.remove(selectedIndex);
            settings.writeRules(savedRules);
        }
        rulePreview.clear();
        selectedWatchFolder = null;
        selectedDestinationFolder = null;
        watchFolderField.clear();
        destinationField.clear();
        keywordField.clear();
        extensionField.clear();
        if (savedRules.isEmpty()) {
            settings.clearConfig();
        } else {
            loadRuleToEditor(savedRules.get(0), watchFolderField, destinationField);
        }
        appendLog(logArea, "選択したルールを削除しました。");
    }

    private void loadRuleToEditor(PropertySettings.SavedRule rule, TextField watchFolderField, TextField destinationField) {
        rulePreview.clear();

        if (rule.getWatchPath() != null && !rule.getWatchPath().isBlank()) {
            selectedWatchFolder = new File(rule.getWatchPath());
            watchFolderField.setText(selectedWatchFolder.getAbsolutePath());
        } else {
            selectedWatchFolder = null;
            watchFolderField.clear();
        }

        if (rule.getDestinationPath() != null && !rule.getDestinationPath().isBlank()) {
            selectedDestinationFolder = new File(rule.getDestinationPath());
            destinationField.setText(selectedDestinationFolder.getAbsolutePath());
            syncDestinationPreview(selectedDestinationFolder.getAbsolutePath());
        } else {
            selectedDestinationFolder = null;
            destinationField.clear();
        }

        rule.getKeywords().forEach(keyword -> rulePreview.add("キーワード: " + keyword));
        rule.getExtensions().forEach(extension -> rulePreview.add("拡張子: " + extension));
    }

    private String createRuleSummary(String watchPath, String destinationPath, int keywordCount, int extensionCount) {
        return "監視: " + watchPath
                + " | 保存先: " + destinationPath
                + " | キーワード " + keywordCount
                + "件 | 拡張子 " + extensionCount + "件";
    }

    private List<String> extractValues(String prefix) {
        return rulePreview.stream()
                .filter(item -> item.startsWith(prefix))
                .map(item -> item.substring(prefix.length()).trim())
                .collect(Collectors.toList());
    }

    private void setupAutoStartButton(ToggleButton autoStartButton, TextArea logArea) {
        autoStartButtonRef = autoStartButton;
        boolean enabled = settings.isAutoStartEnabled();
        autoStartButton.setSelected(enabled);
        updateAutoStartStyle(autoStartButton, enabled);

        if (!startupManager.isSupportedPlatform()) {
            autoStartButton.setDisable(true);
            appendLog(logArea, "自動起動はWindowsでのみ有効です。");
            return;
        }

        if (enabled && !startupManager.isAutoStartRegistered()) {
            boolean registered = startupManager.setAutoStartEnabled(true);
            if (!registered) {
                appendLog(logArea, "自動起動の再登録に失敗しました。設定はONのまま保持します。");
            }
            updateAutoStartStyle(autoStartButton, enabled);
        }

        autoStartButton.setOnAction(e -> {
            boolean target = autoStartButton.isSelected();
            applyAutoStartSetting(target, logArea, "自動起動設定の変更に失敗しました。");
        });
    }

    private void applyAutoStartSetting(boolean target, TextArea logArea, String errorMessage) {
        boolean changed = startupManager.setAutoStartEnabled(target);
        if (!changed) {
            syncAutoStartState(!target);
            appendLog(logArea, errorMessage);
            return;
        }
        settings.setAutoStartEnabled(target);
        syncAutoStartState(target);
        appendLog(logArea, target ? "自動起動をONにしました。" : "自動起動をOFFにしました。");
    }

    private void syncAutoStartState(boolean enabled) {
        if (autoStartButtonRef != null) {
            autoStartButtonRef.setSelected(enabled);
            updateAutoStartStyle(autoStartButtonRef, enabled);
        }
        if (autoStartMenuRef != null) {
            autoStartMenuRef.setState(enabled);
        }
    }

    private void updateAutoStartStyle(ToggleButton autoStartButton, boolean enabled) {
        if (enabled) {
            autoStartButton.setText("⊞ 自動起動 ON");
            autoStartButton.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            autoStartButton.setText("⊞ 自動起動 OFF");
            autoStartButton.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #0f172a; -fx-font-weight: bold;");
        }
    }

    private void setupBackgroundMode(Stage stage, TextArea logArea) {
        javafx.application.Platform.setImplicitExit(false);
        if (!SystemTray.isSupported()) {
            appendLog(logArea, "この環境はシステムトレイ未対応のため、バックグラウンド動作は無効です。");
            return;
        }

        if (trayIcon == null) {
            trayIcon = createTrayIcon(stage, logArea);
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (AWTException e) {
                appendLog(logArea, "トレイアイコンの追加に失敗しました。");
                return;
            }
        }

        stage.setOnCloseRequest(event -> {
            event.consume();
            stage.hide();
            appendLog(logArea, "バックグラウンドで動作中です（トレイアイコンから再表示できます）。");
        });
    }

    private TrayIcon createTrayIcon(Stage stage, TextArea logArea) {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = image.createGraphics();
        g2.setColor(java.awt.Color.decode("#2563eb"));
        g2.fillRect(0, 0, 16, 16);
        g2.setColor(java.awt.Color.WHITE);
        g2.drawString("O", 4, 12);
        g2.dispose();

        PopupMenu popup = new PopupMenu();
        CheckboxMenuItem autoStartMenu = new CheckboxMenuItem("自動起動", settings.isAutoStartEnabled());
        autoStartMenuRef = autoStartMenu;
        autoStartMenu.addItemListener(e -> {
            boolean target = autoStartMenu.getState();
            javafx.application.Platform.runLater(
                    () -> applyAutoStartSetting(target, logArea, "トレイからの自動起動変更に失敗しました。"));
        });

        MenuItem openItem = new MenuItem("開く");
        openItem.addActionListener(e -> javafx.application.Platform.runLater(() -> {
            stage.show();
            stage.toFront();
        }));

        MenuItem exitItem = new MenuItem("終了");
        exitItem.addActionListener(e -> {
            SystemTray.getSystemTray().remove(trayIcon);
            javafx.application.Platform.exit();
            System.exit(0);
        });

        popup.add(openItem);
        popup.add(autoStartMenu);
        popup.addSeparator();
        popup.add(exitItem);

        TrayIcon icon = new TrayIcon(image, "ordex", popup);
        icon.setImageAutoSize(true);
        icon.addActionListener(e -> javafx.application.Platform.runLater(() -> {
            stage.show();
            stage.toFront();
        }));
        return icon;
    }
}
