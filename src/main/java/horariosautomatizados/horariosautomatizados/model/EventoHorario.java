package horariosautomatizados.horariosautomatizados.model;

/**
 * Representa un evento de timbre individual dentro de un tipo de horario.
 * Contiene hora, duración, máscara de días y etiqueta descriptiva.
 *
 * Formato de máscara de días (bits):
 *   Bit 0 (1)  = Lunes
 *   Bit 1 (2)  = Martes
 *   Bit 2 (4)  = Miércoles
 *   Bit 3 (8)  = Jueves
 *   Bit 4 (16) = Viernes
 *   Bit 5 (32) = Sábado
 *   Bit 6 (64) = Domingo
 */
public class EventoHorario {

    private String hora;        // Formato HH:MM:SS
    private int duracion;       // Duración en segundos
    private int mascaraDias;    // Máscara de bits de días (1-127)
    private String etiqueta;    // Etiqueta descriptiva del evento

    // Constructor vacío requerido por Gson
    public EventoHorario() {}

    public EventoHorario(String hora, int duracion, int mascaraDias, String etiqueta) {
        this.hora = hora;
        this.duracion = duracion;
        this.mascaraDias = mascaraDias;
        this.etiqueta = etiqueta;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = duracion; }

    public int getMascaraDias() { return mascaraDias; }
    public void setMascaraDias(int mascaraDias) { this.mascaraDias = mascaraDias; }

    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }

    // ── Utilidades de máscara ─────────────────────────────────────────────

    /** Devuelve true si el bit del día está activo en la máscara. Día: 0=Lun … 6=Dom */
    public boolean tieneDia(int diaBit) {
        return (mascaraDias & (1 << diaBit)) != 0;
    }

    /** Texto legible de los días activos, ej. "Lun Mar Vie" */
    public String diasLegibles() {
        String[] nombres = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (tieneDia(i)) {
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(nombres[i]);
            }
        }
        return sb.isEmpty() ? "(sin días)" : sb.toString();
    }

    /** Verifica si este evento comparte al menos un día con otro (para detectar duplicados) */
    public boolean comparteAlgunDia(EventoHorario otro) {
        return (this.mascaraDias & otro.mascaraDias) != 0;
    }

    @Override
    public String toString() {
        return String.format("%s | %ds | %s | %s", hora, duracion, diasLegibles(), etiqueta);
    }
}
