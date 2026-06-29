package horariosautomatizados.horariosautomatizados.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un tipo de horario (1 a 9).
 * Cada tipo contiene un nombre descriptivo y una lista de eventos.
 */
public class TipoHorario {

    private int numero;             // Número de tipo: 1 a 9
    private String nombre;          // Nombre descriptivo, ej. "Horario Normal"
    private List<EventoHorario> eventos;

    // Constructor vacío requerido por Gson
    public TipoHorario() {
        this.eventos = new ArrayList<>();
    }

    public TipoHorario(int numero, String nombre) {
        this.numero = numero;
        this.nombre = nombre;
        this.eventos = new ArrayList<>();
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<EventoHorario> getEventos() { return eventos; }
    public void setEventos(List<EventoHorario> eventos) { this.eventos = eventos; }

    // ── Utilidades ────────────────────────────────────────────────────────

    /**
     * Busca si ya existe un evento que entre en conflicto con el nuevo.
     * Conflicto: misma hora exacta Y al menos un día en común.
     * Si se pasa un índice a ignorar (para edición), lo excluye de la comparación.
     *
     * @param nuevo         Evento a validar
     * @param ignorarIndice Índice del evento a ignorar (-1 si es nuevo)
     * @return El evento conflictivo, o null si no hay conflicto
     */
    public EventoHorario buscarConflicto(EventoHorario nuevo, int ignorarIndice) {
        for (int i = 0; i < eventos.size(); i++) {
            if (i == ignorarIndice) continue;
            EventoHorario existente = eventos.get(i);
            if (existente.getHora().equals(nuevo.getHora()) && existente.comparteAlgunDia(nuevo)) {
                return existente;
            }
        }
        return null;
    }

    /** Etiqueta para mostrar en el combo: "Tipo 1 - Horario Normal" */
    public String etiquetaCombo() {
        return "Tipo " + numero + " - " + nombre;
    }

    @Override
    public String toString() {
        return etiquetaCombo();
    }
}
