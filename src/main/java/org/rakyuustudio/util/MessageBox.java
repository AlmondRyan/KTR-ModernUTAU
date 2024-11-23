package org.rakyuustudio.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * This class handles the Message Box Object that we packed from {@code JavaFX.Alert}.
 */
public class MessageBox {
    /**
     * This function shows the "Information" Dialog. The content in the dialog is split into
     * "Header" and "Content".
     *
     * @param title   The title of the dialog.
     * @param header  The header in the dialog content body.
     * @param content The content in the dialog content body.
     * @since 1.0
     */
    public static void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * This function shows the "Warning" Dialog. The content in the dialog is split into
     * "Header" and "Content".
     *
     * @param title   The title of the dialog.
     * @param header  The header in the dialog content body.
     * @param content The content in the dialog content body.
     * @since 1.0
     */
    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * This function shows the "Error" Dialog. The content in the dialog is split into
     * "Header" and "Content".
     *
     * <p>By the way, this method also support to break the Main Thread like the fatal error does.</p>
     * <p>The use of this function is really simple. Just like:</p>
     *
     * <pre>
     * {@code
     *  showError("Fatal Error", "Fatal Error", "The pointer is null.", true);
     * }
     * </pre>
     * @param title   The title of the dialog.
     * @param header  The header in the dialog content body.
     * @param content The content in the dialog content body.
     * @since 1.0
     */
    public static void showError(String title, String header, String content, boolean breakTheMainThread) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);

            alert.showAndWait();
            if (breakTheMainThread) {
                throw new RuntimeException("MessageBox: showError() called with breakTheMainThread Param value: True.");
            }
        });
    }

    /**
     * The overloaded function of {@code showError()}, with the fourth parameter is {@code false}.
     *
     * @param title The title of the dialog.
     * @param header The header of the dialog content body.
     * @param content The content of the dialog content body.
     */
    public static void showError(String title, String header, String content) {
        showError(title, header, content, false);
    }

    /**
     * Enumeration. The result used in the confirm dialog. Which the user pick to click, the enumerator will return.
     */
    public enum ConfirmResult {
        OK,
        CANCEL,
        APPLY,
        CLOSE,
        YES,
        NO,
        FINISH,
        NEXT,
        PREVIOUS
    }

    /**
     * The function shows the "Confirmation" dialog.
     *
     * @param title The title of the dialog.
     * @param header The header of the dialog content body.
     * @param content The content of the dialog content body.
     * @return The enumerator of the result that user pick.
     */
    public static ConfirmResult showConfirm(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Using a collection or enum to manage the custom buttons for clarity and consistency
        alert.getButtonTypes().setAll(
                ButtonType.OK,
                ButtonType.CANCEL,
                new ButtonType("Apply"),
                new ButtonType("Close"),
                ButtonType.YES,
                ButtonType.NO,
                new ButtonType("Finish"),
                new ButtonType("Next"),
                new ButtonType("Previous")
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            ButtonType buttonType = result.get();
            return mapButtonTypeToConfirmResult(buttonType);
        }
        return ConfirmResult.CANCEL;
    }

    /**
     * The mapping of the {@code ButtonType} to the {@code ConfirmResult} enumerator.
     * @param buttonType The buttonType that we need to map.
     * @return The ConfirmResult Enumerator.
     */
    private static ConfirmResult mapButtonTypeToConfirmResult(ButtonType buttonType) {
        // Handle custom button texts, making it more maintainable
        return switch (buttonType.getText()) {
            case "Apply" -> ConfirmResult.APPLY;
            case "Close" -> ConfirmResult.CLOSE;
            case "Finish" -> ConfirmResult.FINISH;
            case "Next" -> ConfirmResult.NEXT;
            case "Previous" -> ConfirmResult.PREVIOUS;
            default -> ConfirmResult.CANCEL;
        };
    }
}
