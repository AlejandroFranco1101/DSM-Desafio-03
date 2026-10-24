# BiblioLara — Desafío Práctico 3 (DSM441)

**Universidad Don Bosco**  
**Facultad de Ingeniería — Escuela de Computación**  
- **Asignatura:** DSM441 - Desarrollo de Software para Móviles  
- **Grupo:** 01T | **Ciclo:** 02-2026  
- **Docente:** Ing. Mario Meléndez  

---

## Descripción del Proyecto

**BiblioLara** es una aplicación móvil nativa para Android desarrollada en **Kotlin** que permite a los usuarios llevar el control personalizado de sus lecturas, establecer y monitorear una **meta anual de libros**, registrar su avance por páginas y consultar estadísticas detalladas en tiempo real.

La aplicación integra **Firebase Authentication** para la gestión segura de sesiones y **Firebase Realtime Database** (con persistencia en disco habilitada) para garantizar que cada usuario visualice y administre únicamente sus propios datos, incluso al cerrar la aplicación o reiniciar el dispositivo.

---

## Funcionalidades Implementadas

### 1. Registro y Autenticación de Usuarios
- Inicio de sesión y registro con correo electrónico y contraseña mediante **Firebase Authentication** ([`LoginActivity.kt`](app/src/main/java/com/example/dsm_desafio_03/ui/auth/LoginActivity.kt), [`RegisterActivity.kt`](app/src/main/java/com/example/dsm_desafio_03/ui/auth/RegisterActivity.kt)).
- Validaciones previas al envío ([`ValidationUtils.kt`](app/src/main/java/com/example/dsm_desafio_03/utils/ValidationUtils.kt)): formato de correo electrónico válido, contraseña de al menos 6 caracteres y confirmación de contraseña en el registro.
- Persistencia automática de sesión al cerrar la aplicación y opción de cierre de sesión con confirmación.

### 2. Pantalla Principal (Dashboard)
- Saludo personalizado con el nombre o correo del usuario autenticado ([`MainActivity.kt`](app/src/main/java/com/example/dsm_desafio_03/MainActivity.kt)).
- Panel de **Meta Anual de Lectura** del año actual: muestra la meta definida, libros terminados en el año y libros restantes para cumplirla, permitiendo definir o modificar la meta anual (validando que sea un número entero mayor que cero).
- Listado en tiempo real de los libros en estado **"Leyendo"** con su barra y porcentaje de avance.
- Accesos directos para agregar un libro, abrir la biblioteca completa y consultar las estadísticas.

### 3. Registro y Edición de Libros
- Formulario completo ([`BookFormActivity.kt`](app/src/main/java/com/example/dsm_desafio_03/ui/book/BookFormActivity.kt)): título, autor, género, total de páginas, páginas leídas, estado y calificación.
- Cuatro géneros obligatorios: `Novela`, `Ciencia ficción`, `Historia` y `Desarrollo personal`.
- Estados soportados: `Por leer`, `Leyendo` y `Terminado`.
- Validaciones estrictas con mensajes detallados por campo:
  - Título y autor obligatorios (entre 2 y 60 caracteres).
  - Total de páginas entero mayor que cero.
  - Páginas leídas entero entre `0` y el total de páginas.
  - Calificación de 1 a 5 estrellas habilitada y obligatoria **únicamente** cuando el estado es `Terminado`.
- Registro automático de la fecha de finalización al marcar un libro como `Terminado`.

### 4. Biblioteca Completa
- Lista desplazable (`RecyclerView`) con todos los libros del usuario y filtros rápidos por estado ([`LibraryActivity.kt`](app/src/main/java/com/example/dsm_desafio_03/ui/library/LibraryActivity.kt)).
- Cada tarjeta muestra título, autor, género, estado, porcentaje de avance y calificación (si el libro está terminado).
- Mensaje ilustrado de estado vacío cuando no existen libros registrados.

### 5. Estadísticas del Año Actual
- Desglose de libros terminados en el año actual por cada uno de los cuatro géneros ([`StatisticsActivity.kt`](app/src/main/java/com/example/dsm_desafio_03/ui/stats/StatisticsActivity.kt)).
- Indicador del **género más leído** y **total de páginas leídas en el año**.
- Tarjeta del **libro mejor calificado del año** (aplicando como criterio de desempate el libro que se terminó primero según su fecha de finalización).

---

## Estructura del Proyecto

```text
app/src/main/java/com/example/dsm_desafio_03/
├── ReadingTrackerApp.kt          # Inicialización de persistencia offline de Firebase
├── MainActivity.kt               # Pantalla Principal (Dashboard y Meta Anual)
├── data/
│   └── ReadingRepository.kt      # Repositorio Firebase Realtime Database aislado por UID
├── model/
│   └── Book.kt                   # Modelo de datos del libro y cálculo de porcentaje
├── ui/
│   ├── auth/
│   │   ├── LoginActivity.kt      # Inicio de sesión
│   │   └── RegisterActivity.kt   # Registro de nuevos usuarios
│   ├── book/
│   │   └── BookFormActivity.kt   # Registro, edición y validación de libros
│   ├── library/
│   │   ├── LibraryActivity.kt    # Biblioteca completa desplazable
│   │   └── LibraryAdapter.kt     # Adaptador de libros
│   ├── main/
│   │   └── CurrentlyReadingAdapter.kt
│   └── stats/
│       └── StatisticsActivity.kt # Estadísticas anuales y libro mejor calificado
└── utils/
    └── ValidationUtils.kt        # Reglas de validación de formularios
```
