# Configuración de Horarios Automatizados

Aplicación de escritorio JavaFX para configurar horarios del sistema Arduino con timbre automático.

---

## Requisitos

- **Java 21** (JDK)
- **Maven 3.8+**
- **IntelliJ IDEA** (recomendado)

---

## Cómo abrir en IntelliJ

1. Abre IntelliJ → **File → Open**
2. Selecciona la carpeta `horariosautomatizados/`
3. IntelliJ detectará el `pom.xml` automáticamente → acepta importar como proyecto Maven
4. Espera que Maven descargue las dependencias (JavaFX 21, Gson)

---

## Cómo ejecutar

### Opción A – Desde IntelliJ
- Haz clic derecho en `MainApp.java` → **Run 'MainApp'**

### Opción B – Desde la terminal
```bash
mvn clean javafx:run
```

---

## Estructura del proyecto

```
src/main/java/
├── module-info.java
└── horariosautomatizados/horariosautomatizados/
    ├── MainApp.java                  ← Punto de entrada
    ├── model/
    │   ├── EventoHorario.java        ← Un evento (hora, duración, días, etiqueta)
    │   ├── TipoHorario.java          ← Un tipo de horario (1-9) con sus eventos
    │   └── ProyectoHorarios.java     ← Contenedor raíz (se guarda en JSON)
    ├── service/
    │   └── GestorArchivos.java       ← Guardar/cargar JSON y exportar HORARIO.txt
    └── ui/
        ├── VentanaPrincipal.java     ← Ventana principal de la app
        └── DialogoEvento.java        ← Formulario para agregar/editar un evento
```

---

## Cómo usar la aplicación

### 1. Gestión de tipos de horario
- Usa el **combo desplegable** de la izquierda para seleccionar un tipo
- **+ Nuevo tipo**: crea un nuevo tipo (máximo 9 tipos en total)
- **✎ Renombrar**: cambia el nombre del tipo seleccionado
- **✖ Eliminar tipo**: borra el tipo y todos sus eventos

### 2. Gestión de eventos
- Selecciona un tipo para ver sus eventos
- **+ Agregar evento**: abre el formulario de nuevo evento
- **✎ Editar**: edita el evento seleccionado en la lista
- **✖ Eliminar**: elimina el evento seleccionado

### 3. Formulario de evento
Campos requeridos:
- **Hora** en formato `HH:MM:SS` (ej: `07:30:00`)
- **Duración** en segundos (1–3600)
- **Etiqueta** descriptiva (sin comas)
- **Días**: marca los días de la semana en que aplica el evento

> ⚠ La app **bloquea eventos duplicados**: no puedes tener dos eventos del mismo tipo, con la misma hora y al menos un día en común.

### 4. Guardar
- **💾 Guardar**: guarda el proyecto en la carpeta elegida. Genera dos archivos:
  - `proyecto_horarios.json` → archivo de proyecto (para seguir editando después)
  - `HORARIO.txt` → archivo listo para el Arduino
- La primera vez pedirá la carpeta; las siguientes guarda en el mismo lugar.

### 5. Exportar a SD
- **📤 Exportar a SD**: copia el `HORARIO.txt` generado a la ubicación y nombre que elijas (ej: directamente a la raíz de la tarjeta SD)

---

## Formato del HORARIO.txt

Cada línea representa un evento:
```
tipo,HH:MM:SS,duracion_segundos,mascara_dias,ETIQUETA
```

Ejemplo:
```
1,07:00:00,5,31,ENTRADA MANANA
1,12:30:00,10,31,ALMUERZO
2,07:00:00,5,64,ENTRADA SABADO
```

**Máscara de días** (suma de bits):
| Día       | Valor |
|-----------|-------|
| Lunes     | 1     |
| Martes    | 2     |
| Miércoles | 4     |
| Jueves    | 8     |
| Viernes   | 16    |
| Sábado    | 32    |
| Domingo   | 64    |

Ejemplo: Lunes a Viernes = 1+2+4+8+16 = **31**

---

## Generar instalador (jpackage)

Para distribuir la app como instalador (.exe en Windows, .dmg en Mac, .deb en Linux):

```bash
# 1. Compilar y generar imagen de runtime con jlink
mvn clean javafx:jlink

# 2. Crear instalador con jpackage (requiere WiX en Windows)
jpackage \
  --input target/app \
  --name "HorariosAutomatizados" \
  --main-jar app.jar \
  --main-class horariosautomatizados.horariosautomatizados.MainApp \
  --type exe \
  --app-version 1.0 \
  --win-shortcut \
  --win-menu
```

> Para Windows: instala [WiX Toolset](https://wixtoolset.org/) antes de ejecutar jpackage.
