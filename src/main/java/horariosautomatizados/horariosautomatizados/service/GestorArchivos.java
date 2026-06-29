package horariosautomatizados.horariosautomatizados.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import horariosautomatizados.horariosautomatizados.model.EventoHorario;
import horariosautomatizados.horariosautomatizados.model.ProyectoHorarios;
import horariosautomatizados.horariosautomatizados.model.TipoHorario;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Servicio de persistencia: guarda/carga el proyecto en JSON
 * y exporta el HORARIO.txt para el Arduino.
 *
 * Formato de línea en HORARIO.txt:
 *   tipoHorario,HH:MM:SS,duracionSegundos,mascaraDias,ETIQUETA
 *
 * Ejemplo:
 *   1,07:00:00,5,31,ENTRADA MANANA
 */
public class GestorArchivos {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String NOMBRE_JSON = "proyecto_horarios.json";
    private static final String NOMBRE_TXT  = "HORARIO.txt";

    // ── Guardado ──────────────────────────────────────────────────────────

    /**
     * Guarda el proyecto completo en la carpeta indicada:
     *  - proyecto_horarios.json  (para reabrir y editar)
     *  - HORARIO.txt             (listo para el Arduino)
     *
     * @param proyecto  El proyecto a guardar
     * @param carpeta   Carpeta destino (se crea si no existe)
     * @throws IOException si hay error de escritura
     */
    public void guardar(ProyectoHorarios proyecto, Path carpeta) throws IOException {
        Files.createDirectories(carpeta);
        guardarJson(proyecto, carpeta.resolve(NOMBRE_JSON));
        exportarTxt(proyecto, carpeta.resolve(NOMBRE_TXT));
    }

    /** Guarda solo el JSON del proyecto. */
    private void guardarJson(ProyectoHorarios proyecto, Path destino) throws IOException {
        String json = GSON.toJson(proyecto);
        Files.writeString(destino, json, StandardCharsets.UTF_8);
    }

    /**
     * Genera y escribe el HORARIO.txt con el formato que lee el Arduino.
     * Líneas ordenadas por tipo, luego por hora.
     */
    public void exportarTxt(ProyectoHorarios proyecto, Path destino) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("# HORARIO.txt - Generado por ConfiguracionHorariosAutomatizados v")
          .append(proyecto.getVersion()).append("\n");
        sb.append("# Proyecto: ").append(proyecto.getNombreProyecto()).append("\n");
        sb.append("# Formato: tipo,HH:MM:SS,duracion_seg,mascara_dias,ETIQUETA\n");
        sb.append("# Mascara dias: Lun=1, Mar=2, Mie=4, Jue=8, Vie=16, Sab=32, Dom=64\n");
        sb.append("#\n");

        for (TipoHorario tipo : proyecto.getTipos()) {
            sb.append("# --- Tipo ").append(tipo.getNumero())
              .append(": ").append(tipo.getNombre()).append(" ---\n");

            tipo.getEventos().stream()
                .sorted((a, b) -> a.getHora().compareTo(b.getHora()))
                .forEach(ev -> sb.append(tipo.getNumero()).append(",")
                    .append(ev.getHora()).append(",")
                    .append(ev.getDuracion()).append(",")
                    .append(ev.getMascaraDias()).append(",")
                    .append(ev.getEtiqueta().toUpperCase()).append("\n"));
        }

        Files.writeString(destino, sb.toString(), StandardCharsets.UTF_8);
    }

    // ── Carga ─────────────────────────────────────────────────────────────

    /**
     * Carga un proyecto desde un archivo JSON.
     *
     * @param archivo Ruta al archivo .json
     * @return El proyecto deserializado
     * @throws IOException si hay error de lectura o el archivo no es válido
     */
    public ProyectoHorarios cargarJson(Path archivo) throws IOException {
        String contenido = Files.readString(archivo, StandardCharsets.UTF_8);
        ProyectoHorarios proyecto = GSON.fromJson(contenido, ProyectoHorarios.class);
        if (proyecto == null) throw new IOException("El archivo JSON no contiene un proyecto válido.");
        return proyecto;
    }

    /**
     * Exporta el HORARIO.txt a una carpeta destino diferente (Exportar a SD).
     * Simplemente copia el .txt ya generado al destino con el nombre elegido.
     *
     * @param carpetaOrigen  Carpeta del proyecto donde está el HORARIO.txt generado
     * @param destino        Ruta completa de destino (carpeta + nombre de archivo elegido)
     * @throws IOException si hay error de copia
     */
    public void exportarASD(Path carpetaOrigen, Path destino) throws IOException {
        Path origen = carpetaOrigen.resolve(NOMBRE_TXT);
        if (!Files.exists(origen)) {
            throw new IOException("No se encontró el HORARIO.txt. Guarda el proyecto primero.");
        }
        Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
    }

    // ── Utilidades ────────────────────────────────────────────────────────

    public String getNombreJson() { return NOMBRE_JSON; }
    public String getNombreTxt()  { return NOMBRE_TXT; }
}
