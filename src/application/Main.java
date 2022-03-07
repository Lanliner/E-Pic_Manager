package application;

import application.model.Menu;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Menu.initWelcomePage(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
