package horariosautomatizados.horariosautomatizados;

import horariosautomatizados.horariosautomatizados.ui.VentanaPrincipal;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación JavaFX.
 * Inicializa la ventana principal y configura el tamaño mínimo de la ventana.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        VentanaPrincipal ventana = new VentanaPrincipal(stage);

        Scene scene = new Scene(ventana, 1000, 650);

        stage.setTitle("Horarios Automatizados — Nuevo Proyecto");
        stage.setMinWidth(780);
        stage.setMinHeight(500);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
