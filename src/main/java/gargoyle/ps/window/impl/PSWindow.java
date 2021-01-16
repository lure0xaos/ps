package gargoyle.ps.window.impl;

import gargoyle.ps.window.IPSWindow;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;
import java.util.Optional;

public final class PSWindow implements IPSWindow {

    private static final char CHAR_SPLIT = '|';

    private static final char CHAR_NEWLINE = '\n';

    private final Stage owner;

    private PSWindow(Stage owner) {
        this.owner = owner;
    }

    public static IPSWindow create(Stage stage) {
        return new PSWindow(stage);
    }

    @Override
    public void alert(String title, String header, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        // AbstractApplication.applyStylesheets(owner, alert.getDialogPane());
        alert.initOwner(owner);
        setTitleHeader(alert, title, header);
        alert.setContentText(message.replace(CHAR_SPLIT, CHAR_NEWLINE));
        center(alert);
        alert.showAndWait();
    }

    @Override
    @SafeVarargs
    public final <T> T choice(String title, String header, String message, T defaultChoice, T... choices) {
        ChoiceDialog<T> alert = new ChoiceDialog<>(defaultChoice, choices);
        // AbstractApplication.applyStylesheets(owner, alert.getDialogPane());
        alert.initOwner(owner);
        setTitleHeader(alert, title, header);
        alert.setContentText(message.replace(CHAR_SPLIT, CHAR_NEWLINE));
        center(alert);
        Optional<T> result = alert.showAndWait();
        return result.isPresent() ? result.get() : null;
    }

    @Override
    public boolean confirm(String title, String header, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        // AbstractApplication.applyStylesheets(owner, alert.getDialogPane());
        alert.initOwner(owner);
        setTitleHeader(alert, title, header);
        alert.setContentText(message.replace(CHAR_SPLIT, CHAR_NEWLINE));
        center(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && Objects.equals(result.get(), ButtonType.OK);
    }

    @Override
    public boolean ask(String title, String header, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        // AbstractApplication.applyStylesheets(owner, alert.getDialogPane());
        alert.initOwner(owner);
        setTitleHeader(alert, title, header);
        alert.setContentText(message.replace(CHAR_SPLIT, CHAR_NEWLINE));
        center(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && Objects.equals(result.get(), ButtonType.YES);
    }

    @Override
    public Boolean maybe(String title, String header, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        // AbstractApplication.applyStylesheets(owner, alert.getDialogPane());
        alert.initOwner(owner);
        setTitleHeader(alert, title, header);
        alert.setContentText(message.replace(CHAR_SPLIT, CHAR_NEWLINE));
        center(alert);
        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent() || Objects.equals(result.get(), ButtonType.CANCEL)) {
            return null;
        }
        if (Objects.equals(result.get(), ButtonType.YES)) {
            return Boolean.TRUE;
        }
        if (Objects.equals(result.get(), ButtonType.NO)) {
            return Boolean.FALSE;
        }
        return null;
    }

    @Override
    public void error(String title, String header, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        // AbstractApplication.applyStylesheets(owner, alert.getDialogPane());
        alert.initOwner(owner);
        setTitleHeader(alert, title, header);
        alert.setContentText(message.replace(CHAR_SPLIT, CHAR_NEWLINE));
        center(alert);
        alert.showAndWait();
    }

    @Override
    public String prompt(String header, String message) {
        return prompt(null, header, message, "");
    }

    @Override
    public String prompt(String title, String header, String message, String defaultText) {
        TextInputDialog alert = new TextInputDialog(defaultText);
        // AbstractApplication.applyStylesheets(owner, alert.getDialogPane());
        alert.initOwner(owner);
        setTitleHeader(alert, title, header);
        alert.setContentText(message.replace(CHAR_SPLIT, CHAR_NEWLINE));
        center(alert);
        Optional<String> result = alert.showAndWait();
        return result.isPresent() ? result.get() : null;
    }

    @Override
    public void warn(String title, String header, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        // AbstractApplication.applyStylesheets(owner, alert.getDialogPane());
        alert.initOwner(owner);
        setTitleHeader(alert, title, header);
        alert.setContentText(message.replace(CHAR_SPLIT, CHAR_NEWLINE));
        center(alert);
        alert.showAndWait();
    }

    @Override
    public void dialog(String title, String header, Node view) {
        Alert alert = new Alert(AlertType.INFORMATION);
        // AbstractApplication.applyStylesheets(owner, alert.getDialogPane());
        alert.setTitle(title == null ? owner.getTitle() : title);
        alert.getDialogPane().setHeaderText(header);
        alert.getDialogPane().setContent(view);
        alert.show();
        center0(alert);
        alert.hide();
        alert.showAndWait();
    }

    private void setTitleHeader(Dialog<?> alert, String title, String header) {
        alert.setTitle(title == null ? owner.getTitle() : title);
        alert.setHeaderText(header);
    }

    private void center(Dialog<?> dialog) {
        Window dialogOwner = dialog.getOwner();
        DialogPane dialogPane = dialog.getDialogPane();
        Bounds screenBounds = dialogPane.localToScreen(dialogPane.getBoundsInLocal());
        double dx = dialogPane.getBoundsInLocal().getMinX() - screenBounds.getMinX();
        double dy = dialogPane.getBoundsInLocal().getMinY() - screenBounds.getMinY();
        double x0 = owner.getX() + (owner.getWidth() - dialogOwner.getWidth()) / 2;
        double y0 = owner.getY() + (owner.getHeight() - dialogOwner.getHeight()) / 2;
        dialog.setX(x0 - dx);
        dialog.setY(y0 - dy);
    }

    private void center0(Dialog<?> dialog) {
        Window dialogOwner = dialog.getDialogPane().getScene().getWindow();
        dialogOwner.centerOnScreen();
    }
}
