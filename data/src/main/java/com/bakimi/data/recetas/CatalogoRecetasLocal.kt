package com.bakimi.data.recetas

import com.bakimi.data.ingredientes.Ingredientes.ACEITE
import com.bakimi.data.ingredientes.Ingredientes.ARROZ
import com.bakimi.data.ingredientes.Ingredientes.AVENA
import com.bakimi.data.ingredientes.Ingredientes.CANELA
import com.bakimi.data.ingredientes.Ingredientes.CEBOLLA
import com.bakimi.data.ingredientes.Ingredientes.CHICHARO
import com.bakimi.data.ingredientes.Ingredientes.CHILE_SERRANO
import com.bakimi.data.ingredientes.Ingredientes.CILANTRO
import com.bakimi.data.ingredientes.Ingredientes.HUEVO
import com.bakimi.data.ingredientes.Ingredientes.JITOMATE
import com.bakimi.data.ingredientes.Ingredientes.LECHE
import com.bakimi.data.ingredientes.Ingredientes.LECHUGA
import com.bakimi.data.ingredientes.Ingredientes.LIMON
import com.bakimi.data.ingredientes.Ingredientes.MAYONESA
import com.bakimi.data.ingredientes.Ingredientes.PAN_CAJA
import com.bakimi.data.ingredientes.Ingredientes.PASTA
import com.bakimi.data.ingredientes.Ingredientes.PECHUGA
import com.bakimi.data.ingredientes.Ingredientes.PEPINO
import com.bakimi.data.ingredientes.Ingredientes.PIMIENTA
import com.bakimi.data.ingredientes.Ingredientes.PLATANO
import com.bakimi.data.ingredientes.Ingredientes.QUESO
import com.bakimi.data.ingredientes.Ingredientes.SAL
import com.bakimi.data.ingredientes.Ingredientes.SALSA_SOYA
import com.bakimi.data.ingredientes.Ingredientes.TORTILLA
import com.bakimi.data.ingredientes.Ingredientes.ZANAHORIA
import com.bakimi.domain.inventario.Unidad.GRAMO
import com.bakimi.domain.inventario.Unidad.MILILITRO
import com.bakimi.domain.inventario.Unidad.PIEZA
import com.bakimi.domain.recetas.CreditoFoto
import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.inventario.Unidad
import com.bakimi.domain.recetas.IngredienteReceta
import com.bakimi.domain.recetas.Paso
import com.bakimi.domain.recetas.Receta
import com.bakimi.domain.recetas.RecetaRepository
import javax.inject.Inject

/**
 * Catálogo de recetas incluido en la app. Las fotos son de Unsplash (licencia de Unsplash) y
 * están en app/src/main/res/drawable-nodpi como `receta_<id>.jpg`.
 *
 * La proteína por porción queda sin dato hasta tener una fuente confiable.
 */
class CatalogoRecetasLocal @Inject constructor() : RecetaRepository {

    private val recetas: List<Receta> = listOf(
        Receta(
            id = "huevos_mexicana",
            nombre = "Huevos a la mexicana",
            descripcion = "El desayuno clásico: huevo revuelto con jitomate, cebolla y chile. " +
                "Ideal para empezar a cocinar; cada paso trae una ilustración de referencia.",
            minutos = 15,
            porcionesBase = 2,
            ingredientes = listOf(
                ing(HUEVO, 4.0, PIEZA),
                ing(JITOMATE, 1.0, PIEZA),
                ing(CEBOLLA, 0.25, PIEZA),
                ing(CHILE_SERRANO, 1.0, PIEZA, "sin semillas pica menos"),
                ing(ACEITE, 15.0, MILILITRO),
                ing(SAL, 1.0, GRAMO),
            ),
            pasos = listOf(
                Paso(
                    "Lava el jitomate y el chile. Corta el jitomate y la cebolla en cubos pequeños, del tamaño " +
                        "de un garbanzo, y pica el chile muy fino.",
                    imagen = "pasos/huevos_mexicana_1.jpg",
                ),
                Paso(
                    "Rompe los huevos en un tazón, agrega una pizca de sal y bátelos con un tenedor hasta que " +
                        "la yema y la clara se vean del mismo color.",
                    imagen = "pasos/huevos_mexicana_2.jpg",
                ),
                Paso(
                    "Calienta el aceite en un sartén a fuego medio. Está listo cuando se ve brillante y se " +
                        "mueve fácil al inclinar el sartén.",
                    minutos = 1,
                    imagen = "pasos/huevos_mexicana_3.jpg",
                ),
                Paso(
                    "Agrega el jitomate, la cebolla y el chile. Cocina moviendo con una espátula hasta que la " +
                        "cebolla se vea transparente.",
                    minutos = 3,
                    imagen = "pasos/huevos_mexicana_4.jpg",
                ),
                Paso(
                    "Vierte el huevo y muévelo despacio con la espátula, del borde hacia el centro. Apaga el " +
                        "fuego cuando ya no se vea líquido pero siga brillante; se termina de cocer solo.",
                    minutos = 3,
                    imagen = "pasos/huevos_mexicana_5.jpg",
                ),
                Paso(
                    "Sirve de inmediato. Si tienes tortillas, caliéntalas para acompañar.",
                    imagen = "pasos/huevos_mexicana_6.jpg",
                ),
            ),
            creditoFoto = CreditoFoto("Andrijana Bozic", "vesnikproleca", "SBc3QKmqwpg"),
        ),
        Receta(
            id = "huevos_estrellados",
            nombre = "Huevos estrellados con salsa",
            descripcion = "Huevos fritos sobre tortilla con una salsa rápida de jitomate. " +
                "Un desayuno sencillo para empezar a cocinar.",
            minutos = 20,
            porcionesBase = 2,
            ingredientes = listOf(
                ing(HUEVO, 4.0, PIEZA),
                ing(TORTILLA, 4.0, PIEZA),
                ing(JITOMATE, 2.0, PIEZA),
                ing(CEBOLLA, 0.25, PIEZA),
                ing(CHILE_SERRANO, 1.0, PIEZA, "sin semillas pica menos"),
                ing(ACEITE, 30.0, MILILITRO),
                ing(SAL, 2.0, GRAMO),
            ),
            pasos = listOf(
                Paso("Lava los jitomates y el chile. Corta los jitomates en cuartos y la cebolla en trozos grandes."),
                Paso(
                    "Licúa el jitomate, la cebolla, el chile y una pizca de sal con un cuarto de taza de agua, " +
                        "hasta que no queden trozos.",
                ),
                Paso(
                    "Calienta 1 cucharada de aceite en una olla a fuego medio. Vierte la salsa con cuidado porque " +
                        "salpica. Cocínala moviendo de vez en cuando hasta que se vea de un rojo más oscuro.",
                    minutos = 8,
                ),
                Paso(
                    "En un sartén con un poco de aceite, calienta las tortillas unos segundos por lado, " +
                        "solo hasta que estén suaves. Pon dos en cada plato.",
                ),
                Paso(
                    "En el mismo sartén, a fuego medio, rompe los huevos uno por uno. Están listos cuando la clara " +
                        "se ve blanca y firme; la yema puede quedar suave.",
                    minutos = 3,
                ),
                Paso("Pon los huevos sobre las tortillas, báñalos con la salsa caliente y agrega sal al gusto."),
            ),
            creditoFoto = CreditoFoto("Paul Hanaoka", "plhnk", "mkOpQDcFrx0"),
        ),
        Receta(
            id = "ensalada_pollo",
            nombre = "Ensalada de pollo",
            descripcion = "Pollo dorado sobre lechuga fresca con aderezo de limón. " +
                "Usa media lechuga; la otra mitad sirve para el sándwich de pollo.",
            minutos = 25,
            porcionesBase = 2,
            ingredientes = listOf(
                ing(PECHUGA, 300.0, GRAMO),
                ing(LECHUGA, 0.5, PIEZA),
                ing(JITOMATE, 1.0, PIEZA),
                ing(PEPINO, 1.0, PIEZA),
                ing(LIMON, 1.0, PIEZA),
                ing(ACEITE, 30.0, MILILITRO),
                ing(SAL, 2.0, GRAMO),
                ing(PIMIENTA, 1.0, GRAMO),
            ),
            pasos = listOf(
                Paso("Corta la pechuga en tiras del grosor de un dedo y sazónalas con sal y pimienta."),
                Paso(
                    "Calienta 1 cucharada de aceite en un sartén a fuego medio-alto. Cocina las tiras 4 minutos " +
                        "sin moverlas, voltéalas y cocina 4 minutos más. Por dentro deben verse blancas, " +
                        "sin partes rosadas.",
                    minutos = 8,
                ),
                Paso("Mientras, lava la lechuga hoja por hoja, sécala y córtala en trozos con las manos."),
                Paso("Corta el jitomate y el pepino en cubos o rodajas."),
                Paso("Mezcla el jugo del limón con 1 cucharada de aceite y una pizca de sal para hacer el aderezo."),
                Paso(
                    "Junta la lechuga, el jitomate y el pepino, y agrega el pollo. Pon el aderezo justo antes de " +
                        "servir para que la lechuga no se marchite.",
                ),
            ),
            creditoFoto = CreditoFoto("Kenneth Mallia", "kmallia77", "FFu3-WGnas0"),
        ),
        Receta(
            id = "sandwich_pollo",
            nombre = "Sándwich de pollo",
            descripcion = "Pollo deshebrado con mayonesa, lechuga y jitomate. " +
                "Comparte lechuga y pollo con la ensalada de pollo.",
            minutos = 30,
            porcionesBase = 2,
            ingredientes = listOf(
                ing(PAN_CAJA, 4.0, PIEZA, "rebanadas"),
                ing(PECHUGA, 200.0, GRAMO),
                ing(LECHUGA, 0.25, PIEZA, "unas hojas"),
                ing(JITOMATE, 1.0, PIEZA),
                ing(MAYONESA, 30.0, GRAMO),
                ing(SAL, 1.0, GRAMO),
                ing(PIMIENTA, 1.0, GRAMO),
            ),
            pasos = listOf(
                Paso(
                    "Pon la pechuga en una olla, cúbrela con agua y agrega una pizca de sal. Hiérvela a fuego " +
                        "medio. Está lista cuando al cortarla por la mitad ya no se ve rosada.",
                    minutos = 18,
                ),
                Paso("Deja enfriar el pollo unos minutos y deshébralo con dos tenedores, jalando en direcciones opuestas."),
                Paso("Mezcla el pollo con la mayonesa, la sal y la pimienta."),
                Paso("Lava las hojas de lechuga y corta el jitomate en rodajas delgadas."),
                Paso(
                    "Unta la mezcla de pollo sobre una rebanada de pan, agrega lechuga y jitomate, y tapa con " +
                        "otra rebanada. Corta a la mitad en diagonal.",
                ),
            ),
            creditoFoto = CreditoFoto("Dillon Lobo", "dillon_lobo", "BmP0dv6WfSo"),
        ),
        Receta(
            id = "arroz_con_pollo",
            nombre = "Arroz con pollo",
            descripcion = "Un plato completo en una sola olla, con verduras. Rinde bien y se recalienta fácil.",
            minutos = 45,
            porcionesBase = 2,
            ingredientes = listOf(
                ing(ARROZ, 200.0, GRAMO),
                ing(PECHUGA, 300.0, GRAMO),
                ing(CEBOLLA, 0.5, PIEZA),
                ing(ZANAHORIA, 1.0, PIEZA),
                ing(CHICHARO, 100.0, GRAMO),
                ing(JITOMATE, 1.0, PIEZA),
                ing(ACEITE, 30.0, MILILITRO),
                ing(SAL, 4.0, GRAMO),
            ),
            pasos = listOf(
                Paso(
                    "Corta la pechuga en cubos medianos y sazónala con sal. Pica la cebolla, corta la zanahoria " +
                        "en cubitos y licúa el jitomate con 2 tazas de agua.",
                ),
                Paso(
                    "Calienta el aceite en una olla a fuego medio y dora el pollo por todos lados. " +
                        "Sácalo y apártalo en un plato.",
                    minutos = 5,
                ),
                Paso(
                    "En la misma olla, fríe el arroz crudo moviendo seguido hasta que los granos se vean " +
                        "blancos opacos y algunos empiecen a dorarse.",
                    minutos = 5,
                ),
                Paso(
                    "Agrega la cebolla y cocina 2 minutos. Vierte el jitomate licuado y agrega el pollo, " +
                        "la zanahoria, los chícharos y sal.",
                    minutos = 2,
                ),
                Paso(
                    "Cuando hierva, baja el fuego al mínimo, tapa la olla y no la destapes. " +
                        "El vapor es el que termina de cocer el arroz.",
                    minutos = 20,
                ),
                Paso("Apaga el fuego y deja reposar 5 minutos tapado. Esponja el arroz con un tenedor antes de servir.", minutos = 5),
            ),
            creditoFoto = CreditoFoto("Pablo Arroyo", "pablogamedev", "cwbA14iDDP4"),
        ),
        Receta(
            id = "tacos_pollo",
            nombre = "Tacos de pollo",
            descripcion = "Pollo dorado en tortillas calientes con cebolla, cilantro y limón.",
            minutos = 25,
            porcionesBase = 2,
            ingredientes = listOf(
                ing(PECHUGA, 300.0, GRAMO),
                ing(TORTILLA, 8.0, PIEZA),
                ing(CEBOLLA, 0.5, PIEZA),
                ing(CILANTRO, 0.25, PIEZA, "manojo"),
                ing(LIMON, 2.0, PIEZA),
                ing(ACEITE, 15.0, MILILITRO),
                ing(SAL, 2.0, GRAMO),
            ),
            pasos = listOf(
                Paso("Corta la pechuga en tiras delgadas o cubos pequeños y sazónala con sal."),
                Paso("Pica la cebolla y el cilantro en trozos muy pequeños y mézclalos en un plato."),
                Paso(
                    "Calienta el aceite en un sartén a fuego medio-alto. Cocina el pollo moviéndolo de vez en " +
                        "cuando hasta que esté dorado por fuera y sin partes rosadas por dentro.",
                    minutos = 8,
                ),
                Paso(
                    "Calienta las tortillas en un comal o sartén sin aceite, unos 30 segundos por lado, " +
                        "hasta que estén suaves y con algunas manchas doradas.",
                ),
                Paso("Sirve el pollo en las tortillas, agrega cebolla con cilantro y exprime limón encima."),
            ),
            creditoFoto = CreditoFoto("Alma Agencia D", "almaagenciad", "n5jDzmKMT8w"),
        ),
        Receta(
            id = "quesadillas",
            nombre = "Quesadillas",
            descripcion = "La receta más rápida del libro: solo necesitas tortillas y queso.",
            minutos = 10,
            porcionesBase = 2,
            ingredientes = listOf(
                ing(TORTILLA, 6.0, PIEZA),
                ing(QUESO, 150.0, GRAMO),
            ),
            pasos = listOf(
                Paso(
                    "Si el queso es de hebra, como el Oaxaca, deshébralo con las manos. " +
                        "Si es de bloque, rállalo o córtalo en tiras delgadas.",
                ),
                Paso("Calienta un comal o sartén a fuego medio, sin aceite."),
                Paso("Pon una tortilla, caliéntala 20 segundos, voltéala y coloca queso sobre una mitad."),
                Paso(
                    "Dobla la tortilla a la mitad y presiona suave con una espátula. Voltéala cuando esté dorada. " +
                        "Está lista cuando el queso se derrite y se ve brillante.",
                    minutos = 3,
                ),
            ),
            creditoFoto = CreditoFoto("Abdullah Guc", "abdullahguch", "NTZpME8URX8"),
        ),
        Receta(
            id = "omelette",
            nombre = "Omelette de queso y jitomate",
            descripcion = "Huevo esponjoso relleno de queso derretido. Listo en 15 minutos.",
            minutos = 15,
            porcionesBase = 1,
            ingredientes = listOf(
                ing(HUEVO, 2.0, PIEZA),
                ing(QUESO, 40.0, GRAMO),
                ing(JITOMATE, 0.5, PIEZA),
                ing(CEBOLLA, 0.25, PIEZA),
                ing(ACEITE, 5.0, MILILITRO),
                ing(SAL, 1.0, GRAMO),
            ),
            pasos = listOf(
                Paso("Pica el jitomate y la cebolla en cubos pequeños. Ralla o deshebra el queso."),
                Paso(
                    "Bate los huevos en un tazón con un tenedor y una pizca de sal, hasta que la yema y la clara " +
                        "se vean del mismo color.",
                ),
                Paso(
                    "Calienta el aceite en un sartén antiadherente a fuego medio-bajo y cocina la cebolla " +
                        "y el jitomate.",
                    minutos = 2,
                ),
                Paso("Vierte el huevo y mueve el sartén para que cubra todo el fondo. No lo revuelvas."),
                Paso(
                    "Cuando la orilla se vea cocida y el centro casi firme, pon el queso en una mitad, " +
                        "dobla con una espátula y cocina 1 minuto más.",
                    minutos = 3,
                ),
            ),
            creditoFoto = CreditoFoto("Roman Shalkevic", "ramshteks", "8gYyDWCH6RQ"),
        ),
        Receta(
            id = "pasta_jitomate",
            nombre = "Pasta con salsa de jitomate",
            descripcion = "Salsa casera con jitomate fresco y cebolla. Económica y para cualquier día.",
            minutos = 25,
            porcionesBase = 2,
            ingredientes = listOf(
                ing(PASTA, 200.0, GRAMO),
                ing(JITOMATE, 4.0, PIEZA),
                ing(CEBOLLA, 0.5, PIEZA),
                ing(ACEITE, 30.0, MILILITRO),
                ing(SAL, 6.0, GRAMO),
                ing(PIMIENTA, 1.0, GRAMO),
            ),
            pasos = listOf(
                Paso("Pon a hervir 2 litros de agua con 1 cucharadita de sal en una olla grande."),
                Paso("Mientras, pica la cebolla y corta los jitomates en cubos pequeños."),
                Paso(
                    "Cuando el agua hierva con burbujas grandes, agrega la pasta y revuelve para que no se pegue. " +
                        "Cocínala el tiempo que indique el paquete. Debe quedar suave pero firme al morderla.",
                    minutos = 10,
                ),
                Paso(
                    "En un sartén con el aceite a fuego medio, cocina la cebolla hasta que se vea transparente.",
                    minutos = 3,
                ),
                Paso(
                    "Agrega el jitomate, sal y pimienta. Cocina aplastando con una cuchara hasta que se forme " +
                        "una salsa espesa.",
                    minutos = 8,
                ),
                Paso("Escurre la pasta con cuidado, porque el vapor quema. Mézclala con la salsa y sirve."),
            ),
            creditoFoto = CreditoFoto("Danijela Prijovic", "dacakockica", "qits91IZv1o"),
        ),
        Receta(
            id = "avena_platano",
            nombre = "Avena con plátano",
            descripcion = "Desayuno caliente, económico y rápido.",
            minutos = 10,
            porcionesBase = 1,
            ingredientes = listOf(
                ing(AVENA, 50.0, GRAMO),
                ing(LECHE, 250.0, MILILITRO),
                ing(PLATANO, 1.0, PIEZA),
                ing(CANELA, 1.0, GRAMO),
            ),
            pasos = listOf(
                Paso("Pon la leche y la avena en una olla pequeña a fuego medio."),
                Paso(
                    "Mueve con una cuchara de vez en cuando. Cuando empiece a hervir, baja el fuego y cocina " +
                        "hasta que espese y la cuchara deje un camino al pasar.",
                    minutos = 5,
                ),
                Paso("Rebana el plátano en rodajas."),
                Paso("Sirve la avena, acomoda el plátano encima y espolvorea la canela."),
            ),
            creditoFoto = CreditoFoto("Lex Sirikiat", "pundalex", "by7IeC6Qa0k"),
        ),
        Receta(
            id = "arroz_frito",
            nombre = "Arroz frito con huevo",
            descripcion = "Arroz salteado con verduras, huevo y salsa de soya. " +
                "Si te sobró arroz cocido, sáltate el primer paso.",
            minutos = 40,
            porcionesBase = 2,
            ingredientes = listOf(
                ing(ARROZ, 150.0, GRAMO),
                ing(HUEVO, 2.0, PIEZA),
                ing(ZANAHORIA, 1.0, PIEZA),
                ing(CHICHARO, 80.0, GRAMO),
                ing(CEBOLLA, 0.5, PIEZA),
                ing(SALSA_SOYA, 30.0, MILILITRO),
                ing(ACEITE, 30.0, MILILITRO),
                ing(SAL, 2.0, GRAMO),
            ),
            pasos = listOf(
                Paso(
                    "Cuece el arroz: ponlo en una olla con el doble de agua y una pizca de sal. Cuando hierva, " +
                        "tapa, baja el fuego al mínimo y espera 18 minutos. Extiéndelo en un plato para que se enfríe.",
                    minutos = 20,
                ),
                Paso("Pica la cebolla y la zanahoria en cubitos."),
                Paso(
                    "Bate los huevos. En un sartén grande con 1 cucharada de aceite, cocínalos revolviendo " +
                        "hasta que estén firmes. Sácalos y apártalos.",
                    minutos = 2,
                ),
                Paso(
                    "Agrega el resto del aceite, la cebolla, la zanahoria y los chícharos. Cocina a fuego " +
                        "medio-alto moviendo seguido hasta que la zanahoria esté suave.",
                    minutos = 5,
                ),
                Paso(
                    "Agrega el arroz frío y la salsa de soya. Mezcla y deja que el arroz se caliente bien.",
                    minutos = 3,
                ),
                Paso("Regresa el huevo, revuelve y sirve."),
            ),
            creditoFoto = CreditoFoto("Christopher Alvarenga", "krizphoto", "rQX9eVpSFz8"),
        ),
    )

    private val porId: Map<String, Receta> = recetas.associateBy { it.id }

    override fun todas(): List<Receta> = recetas

    override fun obtener(id: String): Receta? = porId[id]
}

private fun ing(ingrediente: Ingrediente, cantidad: Double, unidad: Unidad, nota: String? = null) =
    IngredienteReceta(ingrediente, cantidad, unidad, nota)
