package gargoyle.ps;

import javafx.application.Application;
import javafx.stage.Stage;

public class PS extends Application {
    private final PSApplication application;

    public PS() {
        application = new PSApplication();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        application.start(primaryStage);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public static void main(String[] args) {
        Application.launch(PS.class, args);
    }
}
