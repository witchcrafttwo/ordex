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
import java.util.List;
import java.util.stream.Collectors;

public class GUI2 {

    private final ObservableList<String> rules = FXCollections.observableArrayList();
    private final ObservableList<String> rulePreview = FXCollections.observableArrayList();
    private final PropertySettings settings = new PropertySettings();

    private File selectedWatchFolder;
    private File selectedDestinationFolder;

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
        Button startWatchButton = new Button("監視開始");
        startWatchButton.setMaxWidth(Double.MAX_VALUE);
        startWatchButton.setOnAction(e -> startWatch(logArea));

        ruleListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            appendLog(logArea, "ルールを選択しました: " + newValue);
        });

        loadSavedRule(watchFolderField, destinationField, logArea);

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
                startWatchButton);

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

        HBox topRow = new HBox(12, watchPane, ruleEditorPane);
        HBox.setHgrow(watchPane, Priority.ALWAYS);
        HBox.setHgrow(ruleEditorPane, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setTop(topRow);
        root.setCenter(centerGrid);
        root.setBottom(logPane);
        BorderPane.setMargin(topRow, new Insets(0, 0, 12, 0));
        BorderPane.setMargin(centerGrid, new Insets(0, 0, 12, 0));

        Scene scene = new Scene(root, 980, 640);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setTitle("ordex");
        stage.setScene(scene);
        stage.show();
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
            Button startWatchButton) {
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
        HBox actionRow = new HBox(8, addRuleButton, clearPreviewButton, startWatchButton);
        HBox.setHgrow(addRuleButton, Priority.ALWAYS);
        HBox.setHgrow(clearPreviewButton, Priority.ALWAYS);
        HBox.setHgrow(startWatchButton, Priority.ALWAYS);


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

        appendLog(logArea, "監視処理を開始します。");
        Thread watcherThread = new Thread(() -> {
            try {
                FileWatcher.watchservice(
                        selectedWatchFolder,
                        selectedDestinationFolder,
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
        settings.writeConfig(selectedWatchFolder, selectedDestinationFolder, keywords, extensions);
    }

    private void loadSavedRule(TextField watchFolderField, TextField destinationField, TextArea logArea) {
        PropertySettings.SavedConfig saved = settings.readConfig();
        if (saved == null) {
            appendLog(logArea, "保存済みルールはありません。");
            return;
        }

        if (saved.getWatchPath() != null && !saved.getWatchPath().isBlank()) {
            selectedWatchFolder = new File(saved.getWatchPath());
            watchFolderField.setText(selectedWatchFolder.getAbsolutePath());
        }
        if (saved.getDestinationPath() != null && !saved.getDestinationPath().isBlank()) {
            selectedDestinationFolder = new File(saved.getDestinationPath());
            destinationField.setText(selectedDestinationFolder.getAbsolutePath());
            syncDestinationPreview(selectedDestinationFolder.getAbsolutePath());
        }

        saved.getKeywords().forEach(keyword -> rulePreview.add("キーワード: " + keyword));
        saved.getExtensions().forEach(extension -> rulePreview.add("拡張子: " + extension));

        if (selectedWatchFolder != null && selectedDestinationFolder != null) {
            rules.add(createRuleSummary(selectedWatchFolder.getAbsolutePath(), selectedDestinationFolder.getAbsolutePath()));
            appendLog(logArea, "保存済みルールを自動読み込みしました。");
        } else {
            appendLog(logArea, "保存済みデータを読み込みました（フォルダ設定は未完了）。");
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

        rules.remove(selected);
        rulePreview.clear();
        selectedWatchFolder = null;
        selectedDestinationFolder = null;
        watchFolderField.clear();
        destinationField.clear();
        keywordField.clear();
        extensionField.clear();
        settings.writeConfig(null, null, java.util.Collections.emptyList(), java.util.Collections.emptyList());
        appendLog(logArea, "選択したルールを削除しました。");
    }


    private List<String> extractValues(String prefix) {
        return rulePreview.stream()
                .filter(item -> item.startsWith(prefix))
                .map(item -> item.substring(prefix.length()).trim())
                .collect(Collectors.toList());
    }
}
