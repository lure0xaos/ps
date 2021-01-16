package gargoyle.ps.window;

import javafx.scene.Node;

public interface IPSWindow {

    @SuppressWarnings("SameParameterValue")
    void alert(String title, String header, String message);

    @SuppressWarnings("unchecked")
    <T> T choice(String title, String header, String message, T defaultChoice, T... choices);

    boolean confirm(String title, String header, String message);

    boolean ask(String title, String header, String message);

    Boolean maybe(String title, String header, String message);

    void error(String title, String header, String message);

    String prompt(String header, String message);

    @SuppressWarnings("SameParameterValue")
    String prompt(String title, String header, String message, String defaultText);

    void warn(String title, String header, String message);

    void dialog(String title, String header, Node view);
}
