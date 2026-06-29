package horariosautomatizados.horariosautomatizados.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Contenedor raíz del proyecto.
 * Contiene todos los tipos de horario y metadatos del proyecto.
 * Este objeto es el que se serializa/deserializa en JSON.
 */
public class ProyectoHorarios {

    public static final int MAX_TIPOS = 9;

    private String nombreProyecto;
    private String version;
    private List<TipoHorario> tipos;

    // Constructor vacío requerido por Gson
    public ProyectoHorarios() {
        this.tipos = new ArrayList<>();
        this.version = "1.0";
        this.nombreProyecto = "Mi Proyecto de Horarios";
    }

    public ProyectoHorarios(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
        this.version = "1.0";
        this.tipos = new ArrayList<>();
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    public String getNombreProyecto() { return nombreProyecto; }
    public void setNombreProyecto(String nombreProyecto) { this.nombreProyecto = nombreProyecto; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public List<TipoHorario> getTipos() { return tipos; }
    public void setTipos(List<TipoHorario> tipos) { this.tipos = tipos; }

    // ── Gestión de tipos ──────────────────────────────────────────────────

    /** ¿Se puede agregar otro tipo? Máximo 9. */
    public boolean puedeAgregarTipo() {
        return tipos.size() < MAX_TIPOS;
    }

    /** Agrega un nuevo tipo, asignándole el primer número libre entre 1 y 9. */
    public TipoHorario agregarNuevoTipo(String nombre) {
        if (!puedeAgregarTipo()) return null;
        int numero = primerNumeroLibre();
        TipoHorario nuevo = new TipoHorario(numero, nombre);
        tipos.add(nuevo);
        tipos.sort((a, b) -> Integer.compare(a.getNumero(), b.getNumero()));
        return nuevo;
    }

    /** Elimina un tipo por su número (1-9). */
    public boolean eliminarTipo(int numero) {
        return tipos.removeIf(t -> t.getNumero() == numero);
    }

    /** Busca un tipo por número. */
    public TipoHorario buscarTipo(int numero) {
        return tipos.stream().filter(t -> t.getNumero() == numero).findFirst().orElse(null);
    }

    /** Primer número (1-9) no usado actualmente. */
    private int primerNumeroLibre() {
        for (int i = 1; i <= MAX_TIPOS; i++) {
            final int n = i;
            if (tipos.stream().noneMatch(t -> t.getNumero() == n)) return i;
        }
        return -1; // no debería ocurrir si se valida puedeAgregarTipo()
    }

    /** Lista de números ya usados, para mostrar al usuario. */
    public List<Integer> numerosUsados() {
        return tipos.stream().map(TipoHorario::getNumero).sorted().toList();
    }
}
