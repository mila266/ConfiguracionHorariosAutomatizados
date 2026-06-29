package horariosautomatizados.horariosautomatizados.ui;

import horariosautomatizados.horariosautomatizados.model.EventoHorario;
import horariosautomatizados.horariosautomatizados.model.ProyectoHorarios;
import horariosautomatizados.horariosautomatizados.model.TipoHorario;
import horariosautomatizados.horariosautomatizados.service.GestorArchivos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Ventana principal de la aplicación.
 * Contiene el panel de tipos de horario y el panel de eventos.
 */
public class VentanaPrincipal extends BorderPane {

    // ── Estado ────────────────────────────────────────────────────────────
    private ProyectoHorarios proyecto;
    private Path carpetaProyecto;          // Carpeta donde se guarda JSON + TXT
    private final GestorArchivos gestor = new GestorArchivos();
    private final Stage stage;

    // ── Controles principales ─────────────────────────────────────────────
    private final ComboBox<TipoHorario> comboTipos = new ComboBox<>();
    private final ListView<EventoHorario> listaEventos = new ListView<>();
    private final ObservableList<EventoHorario> eventosObservable = FXCollections.observableArrayList();
    private final Label lblEstado = new Label("Sin proyecto guardado");

    // ── Botones de eventos ────────────────────────────────────────────────
    private final Button btnAgregarEvento   = new Button("+ Agregar evento");
    private final Button btnEditarEvento    = new Button("✎ Editar");
    private final Button btnEliminarEvento  = new Button("✖ Eliminar");

    public VentanaPrincipal(Stage stage) {
        this.stage = stage;
        this.proyecto = new ProyectoHorarios("Nuevo Proyecto");

        construirUI();
        actualizarComboTipos();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Construcción de la UI
    // ═══════════════════════════════════════════════════════════════════════

    private void construirUI() {
        setTop(construirBarra());
        setCenter(construirCentro());
        setBottom(construirBarraEstado());
    }

    /** Barra superior: título + botones globales */
    private VBox construirBarra() {
        // Título
        Label titulo = new Label("Configuración de Horarios Automatizados");
        titulo.setFont(Font.font(null, FontWeight.BOLD, 18));

        // Botones globales
        Button btnNuevo    = new Button("📄 Nuevo");
        Button btnAbrir    = new Button("📂 Abrir");
        Button btnGuardar  = new Button("💾 Guardar");
        Button btnExportar = new Button("📤 Exportar a SD");

        btnNuevo.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        btnAbrir.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
        btnGuardar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        btnExportar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");

        for (Button b : new Button[]{btnNuevo, btnAbrir, btnGuardar, btnExportar}) {
            b.setPrefHeight(32);
            b.setCursor(javafx.scene.Cursor.HAND);
        }

        btnNuevo.setOnAction(e -> accionNuevoProyecto());
        btnAbrir.setOnAction(e -> accionAbrirProyecto());
        btnGuardar.setOnAction(e -> accionGuardar());
        btnExportar.setOnAction(e -> accionExportarSD());

        HBox barraBtn = new HBox(8, btnNuevo, btnAbrir, new Separator(javafx.geometry.Orientation.VERTICAL), btnGuardar, btnExportar);
        barraBtn.setAlignment(Pos.CENTER_LEFT);

        // Campo nombre del proyecto
        Label lblNombre = new Label("Proyecto:");
        lblNombre.setFont(Font.font(null, FontWeight.BOLD, 13));
        TextField campoNombre = new TextField(proyecto.getNombreProyecto());
        campoNombre.setPrefWidth(280);
        campoNombre.textProperty().addListener((obs, v, n) -> proyecto.setNombreProyecto(n));
        HBox barraProyecto = new HBox(8, lblNombre, campoNombre);
        barraProyecto.setAlignment(Pos.CENTER_LEFT);

        VBox barra = new VBox(8, titulo, barraBtn, barraProyecto);
        barra.setPadding(new Insets(14, 16, 10, 16));
        barra.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");

        // Actualizar campo nombre al cambiar proyecto (en Abrir/Nuevo)
        campoNombre.textProperty().bindBidirectional(
            new javafx.beans.property.SimpleStringProperty() {
                // No se puede bind directo a proyecto (cambia al abrir), se actualiza manualmente
            }
        );
        // Guardamos referencia para actualizar en accionAbrirProyecto
        barra.setUserData(campoNombre);

        return barra;
    }

    /** Panel central: panel de tipos (izquierda) + panel de eventos (derecha) */
    private SplitPane construirCentro() {
        SplitPane split = new SplitPane();
        split.getItems().addAll(construirPanelTipos(), construirPanelEventos());
        split.setDividerPositions(0.35);
        return split;
    }

    // ── Panel de Tipos ────────────────────────────────────────────────────

    private VBox construirPanelTipos() {
        Label lblTitulo = new Label("Tipos de Horario");
        lblTitulo.setFont(Font.font(null, FontWeight.BOLD, 14));

        comboTipos.setPrefWidth(Double.MAX_VALUE);
        comboTipos.setPromptText("-- Selecciona un tipo --");
        comboTipos.setOnAction(e -> onSeleccionarTipo());

        Button btnNuevoTipo    = new Button("+ Nuevo tipo");
        Button btnEliminarTipo = new Button("✖ Eliminar tipo");
        Button btnRenombrar    = new Button("✎ Renombrar");

        btnNuevoTipo.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnEliminarTipo.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        btnRenombrar.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529;");

        for (Button b : new Button[]{btnNuevoTipo, btnEliminarTipo, btnRenombrar}) {
            b.setPrefWidth(Double.MAX_VALUE);
            b.setCursor(javafx.scene.Cursor.HAND);
        }

        btnNuevoTipo.setOnAction(e -> accionNuevoTipo());
        btnEliminarTipo.setOnAction(e -> accionEliminarTipo());
        btnRenombrar.setOnAction(e -> accionRenombrarTipo());

        // Info de cuántos tipos hay
        Label lblInfo = new Label("");
        lblInfo.setFont(Font.font(null, FontWeight.NORMAL, 11));
        lblInfo.setStyle("-fx-text-fill: #6c757d;");
        comboTipos.getItems().addListener((javafx.collections.ListChangeListener<TipoHorario>) c ->
            lblInfo.setText("Tipos: " + comboTipos.getItems().size() + " / " + ProyectoHorarios.MAX_TIPOS));

        VBox panel = new VBox(10, lblTitulo, comboTipos, lblInfo, btnNuevoTipo, btnRenombrar, btnEliminarTipo);
        panel.setPadding(new Insets(16));
        return panel;
    }

    // ── Panel de Eventos ──────────────────────────────────────────────────

    private VBox construirPanelEventos() {
        Label lblTitulo = new Label("Eventos del tipo seleccionado");
        lblTitulo.setFont(Font.font(null, FontWeight.BOLD, 14));

        listaEventos.setItems(eventosObservable);
        listaEventos.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(EventoHorario item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Fila con hora, días, duración y etiqueta
                    Label lblHora  = new Label(item.getHora());
                    lblHora.setFont(Font.font(null, FontWeight.BOLD, 13));
                    lblHora.setMinWidth(80);

                    Label lblDias = new Label(item.diasLegibles());
                    lblDias.setStyle("-fx-text-fill: #495057;");
                    lblDias.setMinWidth(200);

                    Label lblDur  = new Label(item.getDuracion() + "s");
                    lblDur.setStyle("-fx-text-fill: #6c757d;");
                    lblDur.setMinWidth(50);

                    Label lblEtiq = new Label(item.getEtiqueta());
                    lblEtiq.setStyle("-fx-text-fill: #343a40; -fx-font-style: italic;");

                    HBox fila = new HBox(12, lblHora, lblDias, lblDur, lblEtiq);
                    fila.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(fila);
                    setText(null);
                }
            }
        });

        // Botones de evento
        btnAgregarEvento.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        btnEditarEvento.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529;");
        btnEliminarEvento.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

        for (Button b : new Button[]{btnAgregarEvento, btnEditarEvento, btnEliminarEvento}) {
            b.setCursor(javafx.scene.Cursor.HAND);
            b.setDisable(true); // Se habilitan al seleccionar tipo
        }

        btnAgregarEvento.setOnAction(e -> accionAgregarEvento());
        btnEditarEvento.setOnAction(e -> accionEditarEvento());
        btnEliminarEvento.setOnAction(e -> accionEliminarEvento());

        // Habilitar Editar/Eliminar solo cuando hay selección
        listaEventos.getSelectionModel().selectedItemProperty().addListener((obs, v, nuevo) -> {
            boolean haySeleccion = (nuevo != null);
            btnEditarEvento.setDisable(!haySeleccion);
            btnEliminarEvento.setDisable(!haySeleccion);
        });

        HBox barraEventos = new HBox(8, btnAgregarEvento, btnEditarEvento, btnEliminarEvento);
        barraEventos.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(10, lblTitulo, barraEventos, listaEventos);
        VBox.setVgrow(listaEventos, Priority.ALWAYS);
        panel.setPadding(new Insets(16));
        return panel;
    }

    /** Barra de estado inferior */
    private HBox construirBarraEstado() {
        lblEstado.setFont(Font.font(null, FontWeight.NORMAL, 11));
        lblEstado.setStyle("-fx-text-fill: #6c757d;");
        HBox barra = new HBox(lblEstado);
        barra.setPadding(new Insets(6, 16, 6, 16));
        barra.setStyle("-fx-background-color: #f1f3f5; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");
        return barra;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Acciones de Proyecto
    // ═══════════════════════════════════════════════════════════════════════

    private void accionNuevoProyecto() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Crear un nuevo proyecto? Los cambios no guardados se perderán.",
            ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Nuevo proyecto");
        confirmacion.setHeaderText(null);
        if (confirmacion.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            proyecto = new ProyectoHorarios("Nuevo Proyecto");
            carpetaProyecto = null;
            actualizarComboTipos();
            eventosObservable.clear();
            setEstado("Nuevo proyecto creado.");
            actualizarNombreVentana();
        }
    }

    private void accionAbrirProyecto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Abrir proyecto de horarios");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Proyecto de Horarios (*.json)", "*.json"));
        File archivo = fc.showOpenDialog(stage);
        if (archivo == null) return;

        try {
            proyecto = gestor.cargarJson(archivo.toPath());
            carpetaProyecto = archivo.getParentFile().toPath();
            actualizarComboTipos();
            eventosObservable.clear();
            comboTipos.getSelectionModel().clearSelection();
            setEstado("Proyecto abierto: " + archivo.getAbsolutePath());
            actualizarNombreVentana();
        } catch (IOException ex) {
            mostrarError("Error al abrir", "No se pudo leer el archivo:\n" + ex.getMessage());
        }
    }

    private void accionGuardar() {
        if (carpetaProyecto == null) {
            // Primera vez: pedir carpeta
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Selecciona dónde guardar el proyecto");
            File carpeta = dc.showDialog(stage);
            if (carpeta == null) return;
            carpetaProyecto = carpeta.toPath();
        }

        try {
            gestor.guardar(proyecto, carpetaProyecto);
            setEstado("✔ Guardado en: " + carpetaProyecto.toAbsolutePath());
            actualizarNombreVentana();
        } catch (IOException ex) {
            mostrarError("Error al guardar", ex.getMessage());
        }
    }

    private void accionExportarSD() {
        if (carpetaProyecto == null) {
            mostrarInfo("Guardar primero",
                "Guarda el proyecto antes de exportar a la tarjeta SD.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Exportar HORARIO.txt a tarjeta SD");
        fc.setInitialFileName("HORARIO.txt");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivo de texto (*.txt)", "*.txt"));
        File destino = fc.showSaveDialog(stage);
        if (destino == null) return;

        try {
            gestor.exportarASD(carpetaProyecto, destino.toPath());
            mostrarInfo("Exportación exitosa",
                "HORARIO.txt exportado correctamente a:\n" + destino.getAbsolutePath());
            setEstado("✔ Exportado a SD: " + destino.getAbsolutePath());
        } catch (IOException ex) {
            mostrarError("Error al exportar", ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Acciones de Tipos
    // ═══════════════════════════════════════════════════════════════════════

    private void accionNuevoTipo() {
        if (!proyecto.puedeAgregarTipo()) {
            mostrarInfo("Límite alcanzado",
                "Ya tienes " + ProyectoHorarios.MAX_TIPOS + " tipos de horario. " +
                "Elimina uno antes de agregar otro.");
            return;
        }

        TextInputDialog dlg = new TextInputDialog("Horario Normal");
        dlg.setTitle("Nuevo tipo de horario");
        dlg.setHeaderText(null);
        dlg.setContentText("Nombre del nuevo tipo:");
        Optional<String> resultado = dlg.showAndWait();
        resultado.ifPresent(nombre -> {
            if (nombre.isBlank()) return;
            TipoHorario nuevo = proyecto.agregarNuevoTipo(nombre.trim());
            actualizarComboTipos();
            comboTipos.getSelectionModel().select(nuevo);
            setEstado("Tipo '" + nuevo.etiquetaCombo() + "' creado.");
        });
    }

    private void accionEliminarTipo() {
        TipoHorario seleccionado = comboTipos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarInfo("Sin selección", "Selecciona un tipo de horario para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar el tipo '" + seleccionado.etiquetaCombo() + "'?\n" +
            "Se eliminarán también sus " + seleccionado.getEventos().size() + " evento(s).",
            ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Eliminar tipo");
        confirmacion.setHeaderText(null);

        if (confirmacion.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            proyecto.eliminarTipo(seleccionado.getNumero());
            actualizarComboTipos();
            eventosObservable.clear();
            setEstado("Tipo '" + seleccionado.etiquetaCombo() + "' eliminado.");
        }
    }

    private void accionRenombrarTipo() {
        TipoHorario seleccionado = comboTipos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarInfo("Sin selección", "Selecciona un tipo de horario para renombrar.");
            return;
        }

        TextInputDialog dlg = new TextInputDialog(seleccionado.getNombre());
        dlg.setTitle("Renombrar tipo");
        dlg.setHeaderText(null);
        dlg.setContentText("Nuevo nombre para Tipo " + seleccionado.getNumero() + ":");
        dlg.showAndWait().ifPresent(nombre -> {
            if (!nombre.isBlank()) {
                seleccionado.setNombre(nombre.trim());
                actualizarComboTipos();
                comboTipos.getSelectionModel().select(seleccionado);
            }
        });
    }

    private void onSeleccionarTipo() {
        TipoHorario tipo = comboTipos.getSelectionModel().getSelectedItem();
        boolean hayTipo = (tipo != null);

        btnAgregarEvento.setDisable(!hayTipo);
        btnEditarEvento.setDisable(true);
        btnEliminarEvento.setDisable(true);

        eventosObservable.clear();
        if (hayTipo) {
            eventosObservable.addAll(tipo.getEventos());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Acciones de Eventos
    // ═══════════════════════════════════════════════════════════════════════

    private void accionAgregarEvento() {
        TipoHorario tipo = comboTipos.getSelectionModel().getSelectedItem();
        if (tipo == null) return;

        DialogoEvento.mostrar(null).ifPresent(nuevo -> {
            EventoHorario conflicto = tipo.buscarConflicto(nuevo, -1);
            if (conflicto != null) {
                mostrarAlertaConflicto(conflicto);
                return;
            }
            tipo.getEventos().add(nuevo);
            refrescarListaEventos(tipo);
            setEstado("Evento agregado: " + nuevo);
        });
    }

    private void accionEditarEvento() {
        TipoHorario tipo = comboTipos.getSelectionModel().getSelectedItem();
        EventoHorario seleccionado = listaEventos.getSelectionModel().getSelectedItem();
        if (tipo == null || seleccionado == null) return;

        int indice = tipo.getEventos().indexOf(seleccionado);

        DialogoEvento.mostrar(seleccionado).ifPresent(modificado -> {
            EventoHorario conflicto = tipo.buscarConflicto(modificado, indice);
            if (conflicto != null) {
                mostrarAlertaConflicto(conflicto);
                return;
            }
            tipo.getEventos().set(indice, modificado);
            refrescarListaEventos(tipo);
            setEstado("Evento actualizado: " + modificado);
        });
    }

    private void accionEliminarEvento() {
        TipoHorario tipo = comboTipos.getSelectionModel().getSelectedItem();
        EventoHorario seleccionado = listaEventos.getSelectionModel().getSelectedItem();
        if (tipo == null || seleccionado == null) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar el evento '" + seleccionado.getEtiqueta() + "' a las " + seleccionado.getHora() + "?",
            ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Eliminar evento");
        confirmacion.setHeaderText(null);

        if (confirmacion.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            tipo.getEventos().remove(seleccionado);
            refrescarListaEventos(tipo);
            setEstado("Evento eliminado.");
        }
    }

    private void mostrarAlertaConflicto(EventoHorario conflicto) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Conflicto de horario");
        alerta.setHeaderText("⚠ Ya existe un evento con la misma hora y día(s)");
        alerta.setContentText(
            "El evento en conflicto es:\n" +
            "  Hora: " + conflicto.getHora() + "\n" +
            "  Días: " + conflicto.diasLegibles() + "\n" +
            "  Etiqueta: " + conflicto.getEtiqueta() + "\n\n" +
            "Cambia la hora, o modifica los días para que no se solapencon otro evento del mismo tipo.");
        alerta.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Helpers de UI
    // ═══════════════════════════════════════════════════════════════════════

    private void actualizarComboTipos() {
        TipoHorario seleccionadoAntes = comboTipos.getSelectionModel().getSelectedItem();
        comboTipos.getItems().setAll(proyecto.getTipos());
        // Reseleccionar el mismo tipo si sigue existiendo
        if (seleccionadoAntes != null) {
            proyecto.getTipos().stream()
                .filter(t -> t.getNumero() == seleccionadoAntes.getNumero())
                .findFirst()
                .ifPresent(t -> comboTipos.getSelectionModel().select(t));
        }
    }

    private void refrescarListaEventos(TipoHorario tipo) {
        eventosObservable.setAll(tipo.getEventos());
    }

    private void setEstado(String mensaje) {
        lblEstado.setText(mensaje);
    }

    private void actualizarNombreVentana() {
        stage.setTitle("Horarios Automatizados — " + proyecto.getNombreProyecto());
    }

    private void mostrarError(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void mostrarInfo(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
