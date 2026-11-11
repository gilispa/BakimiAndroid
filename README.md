<p align="center">
  <img src="design/logos/bakimiHorizontal.png" alt="Bakimi" width="360">
</p>

<p align="center"><strong>Planea tus comidas. Aprovecha lo que tienes.</strong></p>

Bakimi es una app Android que ayuda a organizar la alimentación y las compras de comida, pensada sobre todo para estudiantes foráneos y personas que empiezan a vivir solas. Registra lo que hay en casa, sugiere recetas que se pueden preparar con eso, arma el plan de la semana, calcula lo que falta comprar y cuánto costaría, genera la lista de compras y guía la preparación paso a paso con el Modo Cocina.

> **Estado:** en desarrollo (MVP). El ciclo completo funciona de punta a punta: registrar → descubrir recetas → planear → comprar → cocinar → actualizar el refri.

## Qué hace

| Sección | Funciones |
| --- | --- |
| **Inicio** | Carrusel de recetas según lo que hay en casa; secciones *Puedes cocinar ahora*, *Te faltan pocos ingredientes*, *Listo en menos de 20 minutos* y *Más ideas*. |
| **Mi refri** | Inventario de refrigerador, congelador y despensa. Se busca en un catálogo de más de 100 ingredientes con tolerancia a errores de escritura ("jitomte" encuentra Jitomate), o se agrega un alimento "otro". Botones +/−, agotados, caducidad y eliminar con *Deshacer*. |
| **Recetas** | 11 recetas con foto, porciones ajustables, disponibilidad de cada ingrediente y faltantes reales. Convierte entre unidades (1 kg alcanza para 600 g) y entre piezas y peso de forma aproximada (1 kg de tortillas ≈ 33 piezas). |
| **Mi semana** | Personas, presupuesto y recetas con porciones. Recomienda recetas que aprovechan lo que sobraría del plan (si la ensalada usa media lechuga, sugiere el sándwich para la otra mitad). Barra fija con la compra estimada contra el presupuesto. |
| **Compras** | Lista generada sola desde el plan y el refri, agrupada por categoría y en presentaciones completas (faltan 200 g de arroz → 1 bolsa de 1 kg). Productos manuales, cantidad y precio reales, y *Terminar compra y actualizar mi refri* sin duplicar. |
| **Modo Cocina** | Pasos guiados con progreso, imagen de referencia cuando existe y la pantalla siempre encendida. Al terminar se confirman porciones y cantidades usadas, y se descuentan del refri empezando por lo que caduca antes. |

## Tecnología

- Kotlin 2.0, Jetpack Compose y Material 3
- Navigation Compose
- Hilt para inyección de dependencias
- Room para el almacenamiento local, con migraciones (versión actual: 4)
- Coroutines y Flow
- JUnit 4
- Android 8.0 o superior (minSdk 26), compileSdk y targetSdk 35, Java 17

## Arquitectura

```
:app     Interfaz (Compose), navegación, ViewModels
  │
  ├──> :data    Room, catálogos de ingredientes y recetas, módulo de Hilt
  │      │
  └──────┴──> :domain   Reglas de negocio en Kotlin puro, sin Android
```

| Módulo | Contenido |
| --- | --- |
| `:domain` | Inventario, catálogo de ingredientes (búsqueda, sinónimos, equivalencias), evaluación de recetas, plan semanal, lista de compras, presupuesto y Modo Cocina. Es donde están casi todas las pruebas. |
| `:data` | Entidades, DAOs y repositorios de Room; catálogo de ingredientes con precios de referencia; catálogo de recetas. El esquema de la base se exporta a `data/schemas/`. |
| `:app` | Pantallas, ViewModels, tema y navegación. Todos los textos están en `app/src/main/res/values/strings.xml`. |

## Cómo ejecutarlo

### Requisitos

- Android Studio reciente.
- **JDK 17 o 21 como Gradle JDK.** Gradle 8.10.2 no se ejecuta con JDK 25, que es el que trae Android Studio por defecto.
  Cámbialo en *Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK*.

### Pasos

1. Clona el repositorio y ábrelo en Android Studio.
2. Elige un JDK 17 o 21 como Gradle JDK y sincroniza.
3. Ejecuta la configuración `app` en un emulador o dispositivo.

Desde la terminal:

```bash
./gradlew :app:assembleDebug
```

## Pruebas

```bash
./gradlew :domain:test :data:testDebugUnitTest
```

154 pruebas unitarias cubren las reglas del producto: faltantes (600 g requeridos con 400 g en casa → faltan 200 g), consolidación de ingredientes entre recetas, presentaciones de compra, totales con precios desconocidos (nunca cuentan como $0), compras parciales, confirmaciones repetidas que no duplican, descuentos al cocinar que nunca dejan cantidades negativas, búsqueda con errores de escritura y consistencia de los catálogos. No hay pruebas automáticas de las pantallas.

## Contenido y datos de referencia

- **Recetas:** `data/src/main/java/com/bakimi/data/recetas/CatalogoRecetasLocal.kt`. Las imágenes y los videos de los pasos son opcionales; sin ellos, el Modo Cocina muestra solo el texto.
- **Ingredientes:** `data/src/main/java/com/bakimi/data/ingredientes/CatalogoIngredientes.kt`, con sinónimos (solo del mismo producto, nunca sustitutos), categoría, unidad y ubicación sugeridas.
- **Peso por pieza** (`GRAMOS_POR_PIEZA`): valores de referencia para convertir entre piezas y peso. Las conversiones se muestran como aproximadas.
- **Precios** (`INFO_COMPRA`): **precios de referencia aproximados en MXN, sin fuente verificada** (país, moneda y fuente de precios siguen pendientes). La app los marca como "aprox." y los fecha en noviembre de 2026.

## Imágenes de los pasos (Modo Cocina)

Las ilustraciones de los pasos están en `app/src/main/assets/pasos/<receta>_<n>.jpg` (960 px). Los originales están en `design/pasos/`. Por ahora solo *Huevos a la mexicana* tiene imagen en cada paso.

## Fotos de las recetas

Las fotos vienen de [Unsplash](https://unsplash.com) bajo la [licencia de Unsplash](https://unsplash.com/license) y están en `app/src/main/res/drawable-nodpi/receta_<id>.jpg` (1080×720). El crédito de cada una aparece en el detalle de la receta.

| Receta | Foto | Autor |
| --- | --- | --- |
| Huevos a la mexicana | [SBc3QKmqwpg](https://unsplash.com/photos/SBc3QKmqwpg) | Andrijana Bozic |
| Huevos estrellados con salsa | [mkOpQDcFrx0](https://unsplash.com/photos/mkOpQDcFrx0) | Paul Hanaoka |
| Ensalada de pollo | [FFu3-WGnas0](https://unsplash.com/photos/FFu3-WGnas0) | Kenneth Mallia |
| Sándwich de pollo | [BmP0dv6WfSo](https://unsplash.com/photos/BmP0dv6WfSo) | Dillon Lobo |
| Arroz con pollo | [cwbA14iDDP4](https://unsplash.com/photos/cwbA14iDDP4) | Pablo Arroyo |
| Tacos de pollo | [n5jDzmKMT8w](https://unsplash.com/photos/n5jDzmKMT8w) | Alma Agencia D |
| Quesadillas | [NTZpME8URX8](https://unsplash.com/photos/NTZpME8URX8) | Abdullah Guc |
| Omelette de queso y jitomate | [8gYyDWCH6RQ](https://unsplash.com/photos/8gYyDWCH6RQ) | Roman Shalkevic |
| Pasta con salsa de jitomate | [qits91IZv1o](https://unsplash.com/photos/qits91IZv1o) | Danijela Prijovic |
| Avena con plátano | [by7IeC6Qa0k](https://unsplash.com/photos/by7IeC6Qa0k) | Lex Sirikiat |
| Arroz frito con huevo | [rQX9eVpSFz8](https://unsplash.com/photos/rQX9eVpSFz8) | Christopher Alvarenga |

## Pendiente

- Imágenes y videos para los pasos de las demás recetas, y reproducción de videos.
- País, moneda y fuente de precios reales.
- Fuente para estimar proteína (por ahora muestra "Sin dato").
- Funcionamiento sin conexión del Modo Cocina, cuentas y sincronización.
- Modo oscuro y tipografía definitiva (Nunito).

## Estructura

```
app/                    Interfaz Android
  src/main/assets/pasos/      Imágenes de los pasos
  src/main/res/drawable-nodpi Fotos de recetas y logo
data/                   Room y catálogos
  schemas/              Esquemas exportados de Room (versionarlos)
domain/                 Reglas de negocio en Kotlin puro
design/                 Logos e ilustraciones originales
gradle/libs.versions.toml     Versiones de dependencias
```
