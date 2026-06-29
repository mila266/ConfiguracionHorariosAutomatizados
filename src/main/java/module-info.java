module horariosautomatizados.horariosautomatizados {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens horariosautomatizados.horariosautomatizados to javafx.fxml;
    opens horariosautomatizados.horariosautomatizados.model to com.google.gson;
    opens horariosautomatizados.horariosautomatizados.ui to javafx.fxml;

    exports horariosautomatizados.horariosautomatizados;
    exports horariosautomatizados.horariosautomatizados.model;
    exports horariosautomatizados.horariosautomatizados.service;
    exports horariosautomatizados.horariosautomatizados.ui;
}
