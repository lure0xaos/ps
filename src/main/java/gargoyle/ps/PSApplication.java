package gargoyle.ps;

import gargoyle.ps.config.Config;
import gargoyle.ps.pass.ComplexityChecker;
import gargoyle.ps.pass.RandomGenerator;
import gargoyle.ps.record.IPSRecord;
import gargoyle.ps.record.impl.PSRecord;
import gargoyle.ps.record.impl.PSRecordTreeItem;
import gargoyle.ps.storage.IPSRecordStorage;
import gargoyle.ps.storage.impl.PSRecordStorage;
import gargoyle.ps.util.StringUtil;
import gargoyle.ps.visitor.PSTreeItemVisitor;
import gargoyle.ps.window.IPSWindow;
import gargoyle.ps.window.impl.PSWindow;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static javafx.scene.control.TreeTableView.CONSTRAINED_RESIZE_POLICY;

public final class PSApplication {

    private static final String PREF_PATH = "path";

    private static final int PREF_WIDTH = 330;

    private static final int PREF_HEIGHT = 400;

    private static final String RES_ICON = "gargoyle/ps/icons/icon.png";

    private static final String RES_THEME = "gargoyle/ps/css/theme.css";

    private static final String RES_MESSAGES = "gargoyle/ps/messages";

    private static final String RES_VERSION = "gargoyle/ps/version";

    private static final String RES_RANDOM_CONFIG = "random.properties";

    private static final String RES_COMPLEX_CONFIG = "complex.properties";

    private static final String VER_VERSION = "version";

    private static final String DEFAULT_STORAGE = "pass.txt";

    private static final String CSS_TRANSPARENT = "-fx-text-fill: transparent";

    private static final String CSS_TEXT_FILL = "-fx-text-fill: {0}";

    private static final double DELTA_HALF = .5;

    private static final String USER_HOME = "user.home";

    private static final String MSG_ADD_GROUP = "add.group";

    private static final String MSG_ADD_VALUE = "add.value";

    private static final String MSG_REMOVE = "remove";

    private static final String MSG_RANDOM = "random";

    private static final String MSG_LOADED_0 = "loaded.0";

    private static final String MSG_SAVED_0 = "saved.0";

    private static final String MSG_SAVED_01 = "saved.01";

    private static final String MSG_SAVE_MODIFIED = "save.modified";

    private static final String MSG_SAVE_MODIFICATIONS = "save.modifications";

    private static final String MSG_TITLE = "title";

    private static final String MSG_NAME = "name";

    private static final String MSG_VALUE = "value";

    private static final String MSG_OPEN = "open";

    private static final String MSG_RELOAD = "reload";

    private static final String MSG_SAVE = "save";

    private static final String MSG_SAVE_AS = "save.as";

    private static final String MSG_SHOW = "show";

    private static final String MSG_ABOUT = "about";

    private static final String MSG_REMOVE_1 = "remove1";

    private static final String MSG_RANDOM_1 = "random1";

    private static final String MSG_VALUE_NAME = "value.name";

    private static final String MSG_GROUP_NAME = "group.name";

    private static final String MSG_REMOVED_0 = "removed.0";

    private static final String MSG_RANDOM_SET_0 = "random.set.0";

    private static final String MSG_ADDED_VALUE_0 = "added.value.0";

    private static final String MSG_ADDED_GROUP_0 = "added.group.0";

    private static final String MSG_COPIED_VALUE_OF_0 = "copied.value.of.0";

    private static final String VER_COPYRIGHT = "copyright";

    private static final String DELIMITER_CSS = ";";

    private static final String STYLE_GRAY = "gray";

    private static final String STYLE_RED = "red";

    private static final String STYLE_BLUE = "blue";

    private static final String STYLE_GREEN = "green";

    private final ResourceBundle messages = ResourceBundle.getBundle(RES_MESSAGES);

    private final ResourceBundle version = ResourceBundle.getBundle(RES_VERSION);

    private final Preferences preferences = Preferences.userNodeForPackage(PSApplication.class);

    private final SimpleBooleanProperty visibleValue = new SimpleBooleanProperty(false);

    private final FileChooser chooser = new FileChooser();

    private WatchService watcher;

    private IPSRecordStorage storage;

    private StringProperty status;

    private IPSWindow window;

    private volatile WatchKey modifyWatchKey;

    private volatile WatchKey deleteWatchKey;

    private RandomGenerator randomGenerator;

    private ComplexityChecker complexityChecker;

    private static void putClipboard(String value) {
        ClipboardContent content = new ClipboardContent();
        content.putString(value);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void load() {
        if (storage == null) {
            Path
                path =
                Paths.get(
                    preferences.get(PREF_PATH, Paths.get(System.getProperty(USER_HOME), DEFAULT_STORAGE).toString()));
            newStorage(path);
        }
        storage.load();
        statusMessage(MSG_LOADED_0, storage.getPath());
    }

    private void newStorage(Path path) {
        storage = PSRecordStorage.create(path);
        storage.load();
        try {
            Path parent = path.getParent();
            if (parent != null) {
                modifyWatchKey = parent.register(watcher, ENTRY_MODIFY);
                deleteWatchKey = parent.register(watcher, ENTRY_DELETE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path chooseLoadFile(Stage stage, Path storagePath) {
        // chooser.getExtensionFilters().add(new ExtensionFilter(description, extensions));
        chooser.setInitialDirectory(storagePath.getParent().toFile());
        return chooser.showOpenDialog(stage).toPath();
    }

    private TreeItem<IPSRecord> prepare() {
        return PSTreeItemVisitor.create((Consumer<TreeItem<IPSRecord>>) t -> t.setExpanded(true))
            .visit(PSRecordTreeItem.create(storage.getRoot()));
    }

    private void refresh(TreeTableView<IPSRecord> treeTableView) {
        treeTableView.setRoot(prepare());
        treeTableView.resizeColumn(treeTableView.getTreeColumn(), DELTA_HALF);
    }

    private Path chooseSaveFile(Stage stage, Path path) {
        chooser.setInitialDirectory(path.toFile());
        chooser.setInitialFileName(path.toFile().getName());
        Path selPath = chooser.showSaveDialog(stage).toPath();
        storage.save(selPath);
        return selPath;
    }

    private Boolean saveStorage(Stage stage) {
        Boolean maybe = Boolean.TRUE;
        if (storage.isDirty()) {
            maybe =
                window.maybe(stage.getTitle(), messages.getString(MSG_SAVE_MODIFIED),
                    messages.getString(MSG_SAVE_MODIFICATIONS));
            if (maybe != null && maybe) {
                onSave();
                putClipboard("");
            }
        }
        return maybe;
    }

    public void start(Stage stage) {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        randomGenerator = RandomGenerator.from(Config.load(PSApplication.class.getResource(RES_RANDOM_CONFIG)));
        complexityChecker = ComplexityChecker.from(Config.load(PSApplication.class.getResource(RES_COMPLEX_CONFIG)));
        window = PSWindow.create(stage);
        TreeTableView<IPSRecord> treeTableView = initGUI(stage);
        load();
        refresh(treeTableView);
    }

    private TreeTableView<IPSRecord> initGUI(Stage stage) {
        stage.setTitle(MessageFormat.format("{0} {1}", messages.getString(MSG_TITLE), version.getString(VER_VERSION)));
        stage.getIcons().add(new Image(RES_ICON));
        Text txtStatus = new Text();
        status = txtStatus.textProperty();
        status(version.getString(VER_COPYRIGHT));
        TreeTableView<IPSRecord> treeTableView = createTreeTableView(createNameColumn(), createValueColumn());
        ScrollPane scroll = new ScrollPane(treeTableView);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPrefSize(PREF_WIDTH, PREF_HEIGHT);
        BorderPane content = new BorderPane();
        content.setCenter(scroll);
        content.setBottom(txtStatus);
        content.setTop(createToolBar(stage, treeTableView));
        Scene scene = new Scene(content);
        scene.getStylesheets().add(RES_THEME);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setX(0);
        stage.setOnCloseRequest(event -> {
            Boolean maybe = onClose(stage);
            if (maybe == null) {
                event.consume();
            }
        });
        stage.show();
        stage.setY(Screen.getPrimary().getVisualBounds().getHeight() - stage.getHeight());
        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (modifyWatchKey != null && modifyWatchKey.isValid()) {
                    List<WatchEvent<?>> watchEvents = modifyWatchKey.pollEvents();
                    if (watchEvents != null && !watchEvents.isEmpty()) { onModified(stage); }
                }
                if (deleteWatchKey != null && deleteWatchKey.isValid()) {
                    List<WatchEvent<?>> watchEvents = deleteWatchKey.pollEvents();
                    if (watchEvents != null && !watchEvents.isEmpty()) { onDeleted(stage); }
                }
            }
        });
        return treeTableView;
    }

    private void onModified(Stage stage) {
        onReload();
    }

    private void onDeleted(Stage stage) {
        onClose(stage);
    }

    private TreeTableColumn<IPSRecord, String> createNameColumn() {
        TreeTableColumn<IPSRecord, String> colName = new TreeTableColumn<>(messages.getString(MSG_NAME));
        colName.setCellValueFactory((CellDataFeatures<IPSRecord, String> param) -> new ReadOnlyStringWrapper(
            param.getValue().getValue().getName()));
        return colName;
    }

    private TreeTableColumn<IPSRecord, String> createValueColumn() {
        TreeTableColumn<IPSRecord, String> colValue = new TreeTableColumn<>(messages.getString(MSG_VALUE));
        colValue.setCellValueFactory((CellDataFeatures<IPSRecord, String> param) -> new ReadOnlyStringWrapper(
            param.getValue().getValue().getValue()));
        colValue.setEditable(true);
        colValue.setCellFactory(param -> {
            TreeTableCell<IPSRecord, String> cell = TextFieldTreeTableCell.<IPSRecord>forTreeTableColumn().call(param);
            //            cell.setStyle(StringUtil.join(DELIMITER_CSS, visibleStyle));
            cell.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    String visibleStyle = StringUtil.optional(!visibleValue.get(), CSS_TRANSPARENT);
                    String
                        complexityStyle =
                        StringUtil.optional(newValue != null, MessageFormat.format(CSS_TEXT_FILL, getStyle(newValue)));
                    cell.setStyle(StringUtil.join(DELIMITER_CSS, complexityStyle, visibleStyle));
                }

                private String getStyle(String text) {
                    ComplexityChecker.Rating rating = complexityChecker.getRating(text);
                    if (rating != null) {
                        switch (rating) {
                            case INSUFFICIENT:
                                return STYLE_GRAY;
                            case BAD:
                                return STYLE_RED;
                            case GOOD:
                                return STYLE_BLUE;
                            case EXCELLENT:
                                return STYLE_GREEN;
                        }
                    }
                    return "";
                }
            });
            return cell;
        });
        colValue.setOnEditCommit(event -> {
            event.getTreeTableView()
                .getTreeItem(event.getTreeTablePosition().getRow())
                .getValue()
                .setValue(event.getNewValue());
            storage.makeDirty();
        });
        return colValue;
    }

    @SuppressWarnings("unchecked")
    private TreeTableView<IPSRecord> createTreeTableView(TreeTableColumn<IPSRecord, String> colName,
                                                         TreeTableColumn<IPSRecord, String> colValue) {
        TreeTableView<IPSRecord> treeTableView = new TreeTableView<>();
        treeTableView.setEditable(true);
        treeTableView.getColumns().setAll(colName, colValue);
        treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Platform.runLater(() -> {
                    if (!newValue.isLeaf()) {
                        treeTableView.getSelectionModel().clearSelection();
                    }
                });
            }
        });
        treeTableView.setRowFactory(ttv -> {
            TreeTableRow<IPSRecord> row = new TreeTableRow<IPSRecord>() {
                @Override
                public void updateItem(IPSRecord item, boolean empty) {
                    super.updateItem(item, empty);
                    setContextMenu(empty ? null : createContextMenu(item));
                }

                private ContextMenu createContextMenu(IPSRecord item) {
                    MenuItem addGroupMenuItem = new MenuItem(messages.getString(MSG_ADD_GROUP));
                    addGroupMenuItem.setOnAction(evt -> {
                        onAddGroup(getItem());
                        refresh(getTreeTableView());
                    });
                    MenuItem addValueMenuItem = new MenuItem(messages.getString(MSG_ADD_VALUE));
                    addValueMenuItem.setOnAction(evt -> {
                        onAddValue(getItem());
                        refresh(getTreeTableView());
                    });
                    MenuItem removeMenuItem = new MenuItem(messages.getString(MSG_REMOVE));
                    removeMenuItem.setOnAction(evt -> {
                        onRemoveItem(getItem());
                        refresh(getTreeTableView());
                    });
                    MenuItem randomMenuItem = new MenuItem(messages.getString(MSG_RANDOM));
                    randomMenuItem.setOnAction(evt -> {
                        onRandomItem(getItem());
                        refresh(getTreeTableView());
                    });
                    ContextMenu contextMenu = new ContextMenu();
                    ObservableList<MenuItem> items = contextMenu.getItems();
                    items.clear();
                    if (item.getValue() == null) {
                        items.add(addGroupMenuItem);
                        items.add(addValueMenuItem);
                    }
                    if (item.getParent() != null) {
                        items.add(removeMenuItem);
                    }
                    if (item.getValue() != null) {
                        items.add(randomMenuItem);
                    }
                    return contextMenu;
                }
            };
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY &&
                    (event.getClickCount() == 1 || event.getClickCount() == 3)) {
                    onItemClicked(row.getItem());
                }
            });
            return row;
        });
        treeTableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        return treeTableView;
    }

    private ToolBar createToolBar(Stage stage, TreeTableView<IPSRecord> treeTableView) {
        Button btnOpen = new Button(messages.getString(MSG_OPEN));
        btnOpen.setOnAction(event -> onOpen(stage));
        Button btnReload = new Button(messages.getString(MSG_RELOAD));
        btnReload.setOnAction(event -> onReload());
        Button btnSave = new Button(messages.getString(MSG_SAVE));
        btnSave.setOnAction(event -> onSave());
        Button btnSaveAs = new Button(messages.getString(MSG_SAVE_AS));
        btnSaveAs.setOnAction(event -> onSaveAs(stage));
        ToggleButton btnVisible = new ToggleButton(messages.getString(MSG_SHOW));
        btnVisible.setOnAction(event -> treeTableView.refresh());
        visibleValue.bind(btnVisible.selectedProperty());
        Button btnAbout = new Button(messages.getString(MSG_ABOUT));
        btnAbout.setOnAction(event -> onAbout());
        return new ToolBar(btnOpen, btnReload, new Separator(), btnSave, btnSaveAs, new Separator(), btnVisible,
            new Separator(), btnAbout);
    }

    private void onAbout() {
        WebView webView = new WebView();
        webView.getEngine().load(PSApplication.class.getResource("about.html").toExternalForm());
        window.dialog(null, version.getString(VER_COPYRIGHT), webView);
    }

    private Boolean onClose(Stage stage) {
        Boolean maybe = saveStorage(stage);
        try {
            preferences.flush();
        } catch (BackingStoreException ex) {
            throw new RuntimeException(ex);
        }
        return maybe;
    }

    private void onRemoveItem(IPSRecord rowItem) {
        if (window.ask(null, messages.getString(MSG_REMOVE), messages.getString(MSG_REMOVE_1))) {
            rowItem.getParent().removeChild(rowItem);
            storage.makeDirty();
            statusMessage(MSG_REMOVED_0, rowItem.getName());
        }
    }

    private void onRandomItem(IPSRecord rowItem) {
        if (window.ask(null, messages.getString(MSG_RANDOM), messages.getString(MSG_RANDOM_1))) {
            rowItem.setValue(randomGenerator.nextString());
            storage.makeDirty();
            statusMessage(MSG_RANDOM_SET_0, rowItem.getName());
        }
    }

    private void onAddValue(IPSRecord rowItem) {
        String name = window.prompt(messages.getString(MSG_ADD_VALUE), messages.getString(MSG_VALUE_NAME));
        if (name != null) {
            rowItem.addChild(PSRecord.createValue(name));
            storage.makeDirty();
            statusMessage(MSG_ADDED_VALUE_0, name);
        }
    }

    private void onAddGroup(IPSRecord rowItem) {
        String name = window.prompt(messages.getString(MSG_ADD_GROUP), messages.getString(MSG_GROUP_NAME));
        if (name != null) {
            rowItem.addChild(PSRecord.createGroup(name));
            storage.makeDirty();
            statusMessage(MSG_ADDED_GROUP_0, name);
        }
    }

    private void onItemClicked(IPSRecord item) {
        String value = item.getValue();
        if (value != null) {
            putClipboard(value);
            statusMessage(MSG_COPIED_VALUE_OF_0, item.getPath());
        }
    }

    private void onOpen(Stage stage) {
        saveStorage(stage);
        Path storagePath = storage.getPath();
        Path chosenPath = chooseLoadFile(stage, storagePath);
        newStorage(chosenPath);
        preferences.put(PREF_PATH, storagePath.toString());
    }

    private void onReload() {
        storage.makeDirty();
        storage.load();
    }

    private void onSave() {
        storage.save();
        statusMessage(MSG_SAVED_0, storage.getPath());
    }

    private void onSaveAs(Stage stage) {
        Path path = storage.getPath();
        Path selPath = chooseSaveFile(stage, path);
        String pathValue = selPath.toString();
        preferences.put(PREF_PATH, pathValue);
        statusMessage(MSG_SAVED_01, pathValue);
    }

    private void statusMessage(String formatKey, Object... args) {
        String format = messages.getString(formatKey);
        status(format, args);
    }

    private void status(String formatKey, Object... args) {
        status.set(args.length == 0 ? formatKey : MessageFormat.format(formatKey, args));
    }
}
