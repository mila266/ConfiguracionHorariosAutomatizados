package horariosautomatizados.horariosautomatizados.ui;

import horariosautomatizados.horariosautomatizados.model.EventoHorario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Optional;

/**
 * Diálogo modal para agregar o editar un EventoHorario.
 * Incluye validación completa de todos los campos.
 */
public class DialogoEvento extends Dialog<EventoHorario> {

    // ── Campos del formulario ─────────────────────────────────────────────
    private final TextField campoHora     = new TextField();
    private final TextField campoDuracion = new TextField();
    private final TextField campoEtiqueta = new TextField();
    private final CheckBox[] checkDias    = new CheckBox[7];

    private static final String[] NOMBRES_DIAS = {
        "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    };

    /**
     * Crea el diálogo.
     * @param eventoExistente null para nuevo evento, o el evento a editar
     */
    public DialogoEvento(EventoHorario eventoExistente) {
        boolean esEdicion = (eventoExistente != null);
        setTitle(esEdicion ? "Editar Evento" : "Nuevo Evento");
        setHeaderText(null);

        // ── Construir formulario ──────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 24, 10, 24));

        // Hora
        Label lblHora = new Label("Hora (HH:MM:SS):");
        lblHora.setFont(Font.font(null, FontWeight.BOLD, 13));
        campoHora.setPromptText("07:30:00");
        campoHora.setPrefWidth(160);
        grid.add(lblHora,   0, 0);
        grid.add(campoHora, 1, 0);

        // Duración
        Label lblDur = new Label("Duración (segundos):");
        lblDur.setFont(Font.font(null, FontWeight.BOLD, 13));
        campoDuracion.setPromptText("5");
        campoDuracion.setPrefWidth(80);
        grid.add(lblDur,       0, 1);
        grid.add(campoDuracion, 1, 1);

        // Etiqueta
        Label lblEtiq = new Label("Etiqueta:");
        lblEtiq.setFont(Font.font(null, FontWeight.BOLD, 13));
        campoEtiqueta.setPromptText("ENTRADA MAÑANA");
        campoEtiqueta.setPrefWidth(220);
        grid.add(lblEtiq,     0, 2);
        grid.add(campoEtiqueta, 1, 2);

        // Checkboxes de días
        Label lblDias = new Label("Días:");
        lblDias.setFont(Font.font(null, FontWeight.BOLD, 13));
        grid.add(lblDias, 0, 3);

        HBox boxDias = new HBox(8);
        boxDias.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < 7; i++) {
            checkDias[i] = new CheckBox(NOMBRES_DIAS[i]);
            boxDias.getChildren().add(checkDias[i]);
        }
        grid.add(boxDias, 1, 3);

        // ── Pre-cargar valores si es edición ─────────────────────────────
        if (esEdicion) {
            campoHora.setText(eventoExistente.getHora());
            campoDuracion.setText(String.valueOf(eventoExistente.getDuracion()));
            campoEtiqueta.setText(eventoExistente.getEtiqueta());
            for (int i = 0; i < 7; i++) {
                checkDias[i].setSelected(eventoExistente.tieneDia(i));
            }
        }

        // ── Botones ───────────────────────────────────────────────────────
        ButtonType btnGuardar = new ButtonType(
            esEdicion ? "Actualizar" : "Agregar", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(btnGuardar, btnCancelar);
        getDialogPane().setContent(grid);
        getDialogPane().setPrefWidth(600);

        // Deshabilitar "Guardar" si la hora está vacía (validación en tiempo real)
        javafx.scene.Node botonGuardar = getDialogPane().lookupButton(btnGuardar);
        botonGuardar.setDisable(true);
        campoHora.textProperty().addListener((obs, viejo, nuevo) ->
            botonGuardar.setDisable(nuevo.trim().isEmpty()));

        // ── Convertidor de resultado ──────────────────────────────────────
        setResultConverter(boton -> {
            if (boton == btnGuardar) {
                return construirEvento();
            }
            return null;
        });
    }

    /**
     * Valida y construye el EventoHorario con los datos del formulario.
     * Muestra una alerta si hay errores de validación.
     * @return EventoHorario válido, o null si hay errores
     */
    private EventoHorario construirEvento() {
        // Validar hora
        String hora = campoHora.getText().trim();
        if (!hora.matches("^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$")) {
            mostrarError("Hora inválida",
                "El formato de hora debe ser HH:MM:SS (ej: 07:30:00).\n" +
                "Horas: 00-23, Minutos y segundos: 00-59.");
            return null;
        }

        // Validar duración
        int duracion;
        try {
            duracion = Integer.parseInt(campoDuracion.getText().trim());
            if (duracion <= 0 || duracion > 3600) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarError("Duración inválida",
                "La duración debe ser un número entero entre 1 y 3600 segundos.");
            return null;
        }

        // Validar etiqueta
        String etiqueta = campoEtiqueta.getText().trim();
        if (etiqueta.isEmpty()) {
            mostrarError("Etiqueta vacía", "Ingresa una etiqueta descriptiva para el evento.");
            return null;
        }
        if (etiqueta.contains(",")) {
            mostrarError("Etiqueta inválida",
                "La etiqueta no puede contener comas (,) porque se usa como separador en el archivo.");
            return null;
        }

        // Validar días
        int mascara = calcularMascara();
        if (mascara == 0) {
            mostrarError("Sin días seleccionados",
                "Debes seleccionar al menos un día de la semana.");
            return null;
        }

        return new EventoHorario(hora, duracion, mascara, etiqueta);
    }

    /** Calcula la máscara de bits a partir de los checkboxes seleccionados. */
    private int calcularMascara() {
        int mascara = 0;
        for (int i = 0; i < 7; i++) {
            if (checkDias[i].isSelected()) {
                mascara |= (1 << i);
            }
        }
        return mascara;
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Método de conveniencia estático para mostrar el diálogo y obtener el resultado.
     */
    public static Optional<EventoHorario> mostrar(EventoHorario eventoExistente) {
        return new DialogoEvento(eventoExistente).showAndWait();
    }
}
