package com.bakimi.data.ingredientes

import com.bakimi.domain.ingredientes.Categoria
import com.bakimi.domain.ingredientes.InfoCompra
import com.bakimi.domain.ingredientes.Ingrediente
import com.bakimi.domain.ingredientes.IngredienteRepository
import com.bakimi.domain.inventario.Ubicacion
import com.bakimi.domain.inventario.Unidad
import javax.inject.Inject

/**
 * Peso aproximado de una pieza, en gramos, para comparar "1 kg de tortillas" con "6 tortillas".
 * Son valores de referencia de piezas medianas; por eso la app muestra esas conversiones como
 * aproximadas. Sin entrada aquí, el ingrediente no se convierte entre piezas y peso (por
 * ejemplo, el ajo: una "pieza" puede ser una cabeza o un diente).
 */
internal val GRAMOS_POR_PIEZA: Map<String, Double> = mapOf(
    // Frutas y verduras
    "jitomate" to 120.0,
    "tomate_verde" to 40.0,
    "cebolla" to 250.0,
    "cebolla_morada" to 200.0,
    "chile_serrano" to 8.0,
    "chile_jalapeno" to 30.0,
    "chile_poblano" to 120.0,
    "chile_habanero" to 10.0,
    "pimiento_morron" to 180.0,
    "aguacate" to 200.0,
    "limon" to 40.0,
    "naranja" to 200.0,
    "manzana" to 180.0,
    "platano" to 120.0,
    "pera" to 180.0,
    "mango" to 350.0,
    "papaya" to 1500.0,
    "pina" to 1500.0,
    "melon" to 1500.0,
    "lechuga" to 500.0,
    "col" to 1000.0,
    "brocoli" to 400.0,
    "coliflor" to 700.0,
    "calabacita" to 200.0,
    "zanahoria" to 80.0,
    "papa" to 170.0,
    "camote" to 250.0,
    "pepino" to 250.0,
    "elote" to 250.0,
    "nopal" to 80.0,
    "rabano" to 15.0,
    "betabel" to 150.0,
    "cilantro" to 50.0,
    "perejil" to 50.0,
    // Carnes
    "pechuga_pollo" to 250.0,
    "muslo_pollo" to 250.0,
    "bistec_res" to 120.0,
    "chuleta_cerdo" to 200.0,
    "salchicha" to 40.0,
    // Huevo y panadería
    "huevo" to 50.0,
    "tortilla_maiz" to 30.0,
    "tortilla_harina" to 40.0,
    "tostada" to 15.0,
    "pan_caja" to 25.0,
    "bolillo" to 70.0,
)

/** Fecha de los precios de referencia; se muestra junto a los estimados. */
const val FECHA_PRECIOS_REFERENCIA = "nov 2026"

/**
 * Cómo se compra cada ingrediente y su precio de referencia aproximado en MXN, por presentación.
 * Son estimaciones sin fuente verificada (país, moneda y fuente de precios siguen pendientes en
 * el documento de producto); la app las muestra como "aprox.". Cambiar un precio es editar una línea.
 */
internal val INFO_COMPRA: Map<String, InfoCompra> = mapOf(
    // Frutas y verduras: por pieza
    "jitomate" to InfoCompra(Unidad.PIEZA, 1.0, precio = 4.0),
    "cebolla" to InfoCompra(Unidad.PIEZA, 1.0, precio = 8.0),
    "chile_serrano" to InfoCompra(Unidad.PIEZA, 1.0, precio = 1.0),
    "lechuga" to InfoCompra(Unidad.PIEZA, 1.0, precio = 22.0),
    "pepino" to InfoCompra(Unidad.PIEZA, 1.0, precio = 7.0),
    "limon" to InfoCompra(Unidad.PIEZA, 1.0, precio = 2.0),
    "zanahoria" to InfoCompra(Unidad.PIEZA, 1.0, precio = 3.0),
    "cilantro" to InfoCompra(Unidad.PIEZA, 1.0, "manojo", 8.0),
    "platano" to InfoCompra(Unidad.PIEZA, 1.0, precio = 4.0),
    "chicharo" to InfoCompra(Unidad.GRAMO, 500.0, "bolsa de 500 g", 38.0),
    // Carnes: a granel, de 100 en 100 g
    "pechuga_pollo" to InfoCompra(Unidad.GRAMO, 100.0, precio = 17.0),
    // Lácteos y huevo
    "huevo" to InfoCompra(Unidad.PIEZA, 12.0, "docena", 45.0),
    "leche" to InfoCompra(Unidad.MILILITRO, 1000.0, "litro", 30.0),
    "queso_fundir" to InfoCompra(Unidad.GRAMO, 100.0, precio = 20.0),
    // Despensa
    "tortilla_maiz" to InfoCompra(Unidad.GRAMO, 500.0, "medio kilo", 14.0),
    "pan_caja" to InfoCompra(Unidad.PIEZA, 20.0, "paquete de 20 rebanadas", 52.0),
    "arroz" to InfoCompra(Unidad.GRAMO, 1000.0, "bolsa de 1 kg", 34.0),
    "pasta" to InfoCompra(Unidad.GRAMO, 200.0, "paquete de 200 g", 16.0),
    "avena" to InfoCompra(Unidad.GRAMO, 400.0, "bolsa de 400 g", 28.0),
    "aceite" to InfoCompra(Unidad.MILILITRO, 1000.0, "botella de 1 l", 48.0),
    "sal" to InfoCompra(Unidad.GRAMO, 1000.0, "bolsa de 1 kg", 16.0),
    "pimienta" to InfoCompra(Unidad.GRAMO, 50.0, "frasco de 50 g", 32.0),
    "canela" to InfoCompra(Unidad.GRAMO, 30.0, "frasco de 30 g", 28.0),
    "salsa_soya" to InfoCompra(Unidad.MILILITRO, 150.0, "botella de 150 ml", 32.0),
    "mayonesa" to InfoCompra(Unidad.GRAMO, 390.0, "frasco de 390 g", 48.0),
)

/** Básicos que casi toda receta usa; no cuentan como "ingrediente compartido". */
internal val BASICOS: Set<String> = setOf(
    "sal", "pimienta", "aceite", "comino", "oregano", "laurel", "canela", "vinagre", "azucar",
)

private fun ingrediente(
    id: String,
    nombre: String,
    sinonimos: Array<out String>,
    categoria: Categoria,
    unidad: Unidad,
    ubicacion: Ubicacion,
) = Ingrediente(
    id = id,
    nombre = nombre,
    sinonimos = sinonimos.toList(),
    categoria = categoria,
    unidadSugerida = unidad,
    ubicacionSugerida = ubicacion,
    gramosPorPieza = GRAMOS_POR_PIEZA[id],
    compra = INFO_COMPRA[id],
    basico = id in BASICOS,
)

private fun frutaVerdura(
    id: String,
    nombre: String,
    vararg sinonimos: String,
    unidad: Unidad = Unidad.PIEZA,
    ubicacion: Ubicacion = Ubicacion.REFRIGERADOR,
) = ingrediente(id, nombre, sinonimos, Categoria.FRUTAS_VERDURAS, unidad, ubicacion)

private fun carne(
    id: String,
    nombre: String,
    vararg sinonimos: String,
    unidad: Unidad = Unidad.GRAMO,
    ubicacion: Ubicacion = Ubicacion.REFRIGERADOR,
) = ingrediente(id, nombre, sinonimos, Categoria.CARNES, unidad, ubicacion)

private fun lacteo(
    id: String,
    nombre: String,
    vararg sinonimos: String,
    unidad: Unidad = Unidad.GRAMO,
) = ingrediente(id, nombre, sinonimos, Categoria.LACTEOS_HUEVO, unidad, Ubicacion.REFRIGERADOR)

private fun despensa(
    id: String,
    nombre: String,
    vararg sinonimos: String,
    unidad: Unidad = Unidad.GRAMO,
    ubicacion: Ubicacion = Ubicacion.DESPENSA,
) = ingrediente(id, nombre, sinonimos, Categoria.DESPENSA, unidad, ubicacion)

/**
 * Ingredientes que usan las recetas. Están separados para poder referirlos por nombre desde
 * el catálogo de recetas; todos forman parte de [CatalogoIngredientesLocal].
 */
internal object Ingredientes {
    val HUEVO = lacteo("huevo", "Huevo", "Blanquillo", unidad = Unidad.PIEZA)
    val LECHE = lacteo("leche", "Leche", "Leche entera", "Leche deslactosada", unidad = Unidad.MILILITRO)
    val QUESO = lacteo(
        "queso_fundir",
        "Queso para fundir",
        "Queso",
        "Queso Oaxaca",
        "Queso manchego",
        "Queso Chihuahua",
        "Queso mozzarella",
    )

    val JITOMATE = frutaVerdura("jitomate", "Jitomate", "Tomate", "Tomate rojo", "Jitomate saladet")
    val CEBOLLA = frutaVerdura("cebolla", "Cebolla", "Cebolla blanca", ubicacion = Ubicacion.DESPENSA)
    val CHILE_SERRANO = frutaVerdura("chile_serrano", "Chile serrano", "Serrano")
    val LECHUGA = frutaVerdura("lechuga", "Lechuga", "Lechuga romana", "Lechuga orejona", "Lechuga italiana")
    val PEPINO = frutaVerdura("pepino", "Pepino")
    val LIMON = frutaVerdura("limon", "Limón")
    val ZANAHORIA = frutaVerdura("zanahoria", "Zanahoria")
    val CILANTRO = frutaVerdura("cilantro", "Cilantro")
    val PLATANO = frutaVerdura("platano", "Plátano", "Banana", "Banano", ubicacion = Ubicacion.DESPENSA)
    val CHICHARO = frutaVerdura(
        "chicharo",
        "Chícharo",
        "Guisante",
        "Arveja",
        unidad = Unidad.GRAMO,
        ubicacion = Ubicacion.CONGELADOR,
    )

    val PECHUGA = carne("pechuga_pollo", "Pechuga de pollo", "Pechuga", "Pollo")

    val ACEITE = despensa(
        "aceite",
        "Aceite",
        "Aceite vegetal",
        "Aceite de cocina",
        "Aceite de oliva",
        unidad = Unidad.MILILITRO,
    )
    val SAL = despensa("sal", "Sal", "Sal de mesa")
    val PIMIENTA = despensa("pimienta", "Pimienta", "Pimienta negra", "Pimienta molida")
    val CANELA = despensa("canela", "Canela molida", "Canela")
    val SALSA_SOYA = despensa("salsa_soya", "Salsa de soya", "Salsa de soja", "Soya", unidad = Unidad.MILILITRO)
    val MAYONESA = despensa("mayonesa", "Mayonesa", ubicacion = Ubicacion.REFRIGERADOR)
    val ARROZ = despensa("arroz", "Arroz", "Arroz blanco", "Arroz largo")
    val PASTA = despensa("pasta", "Pasta", "Espagueti", "Spaghetti", "Pasta larga")
    val AVENA = despensa("avena", "Avena", "Avena en hojuelas", "Hojuelas de avena")
    val TORTILLA = despensa(
        "tortilla_maiz",
        "Tortilla de maíz",
        "Tortilla",
        unidad = Unidad.PIEZA,
        ubicacion = Ubicacion.REFRIGERADOR,
    )
    val PAN_CAJA = despensa("pan_caja", "Pan de caja", "Pan de molde", "Pan blanco", unidad = Unidad.PIEZA)
}

/**
 * Catálogo de ingredientes para buscar al registrar alimentos en Mi refri.
 *
 * Los sinónimos solo incluyen nombres del mismo producto, nunca sustitutos: "tortilla de harina"
 * no es sinónimo de tortilla de maíz.
 */
class CatalogoIngredientesLocal @Inject constructor() : IngredienteRepository {

    private val ingredientes: List<Ingrediente> = with(Ingredientes) {
        listOf(
            // Frutas y verduras
            JITOMATE,
            frutaVerdura("tomate_verde", "Tomate verde", "Tomatillo", "Tomate de cáscara"),
            CEBOLLA,
            frutaVerdura("cebolla_morada", "Cebolla morada", "Cebolla roja", ubicacion = Ubicacion.DESPENSA),
            frutaVerdura("ajo", "Ajo", "Cabeza de ajo", "Diente de ajo", ubicacion = Ubicacion.DESPENSA),
            CHILE_SERRANO,
            frutaVerdura("chile_jalapeno", "Chile jalapeño", "Jalapeño"),
            frutaVerdura("chile_poblano", "Chile poblano", "Poblano"),
            frutaVerdura("chile_habanero", "Chile habanero", "Habanero"),
            frutaVerdura("pimiento_morron", "Pimiento morrón", "Pimiento", "Morrón"),
            frutaVerdura("aguacate", "Aguacate", "Palta", ubicacion = Ubicacion.DESPENSA),
            LIMON,
            frutaVerdura("naranja", "Naranja"),
            frutaVerdura("manzana", "Manzana"),
            PLATANO,
            frutaVerdura("pera", "Pera"),
            frutaVerdura("mango", "Mango"),
            frutaVerdura("papaya", "Papaya"),
            frutaVerdura("pina", "Piña"),
            frutaVerdura("sandia", "Sandía"),
            frutaVerdura("melon", "Melón"),
            frutaVerdura("fresa", "Fresa", unidad = Unidad.GRAMO),
            frutaVerdura("uva", "Uva", unidad = Unidad.GRAMO),
            LECHUGA,
            frutaVerdura("espinaca", "Espinaca", unidad = Unidad.GRAMO),
            frutaVerdura("acelga", "Acelga"),
            frutaVerdura("col", "Col", "Repollo"),
            frutaVerdura("brocoli", "Brócoli"),
            frutaVerdura("coliflor", "Coliflor"),
            frutaVerdura("calabacita", "Calabacita", "Calabaza italiana", "Zucchini", "Calabacín"),
            ZANAHORIA,
            frutaVerdura("papa", "Papa", "Patata", ubicacion = Ubicacion.DESPENSA),
            frutaVerdura("camote", "Camote", "Batata", ubicacion = Ubicacion.DESPENSA),
            PEPINO,
            frutaVerdura("elote", "Elote", "Mazorca"),
            frutaVerdura("ejote", "Ejote", "Judía verde", unidad = Unidad.GRAMO),
            frutaVerdura("champinon", "Champiñón", "Hongo blanco", unidad = Unidad.GRAMO),
            frutaVerdura("nopal", "Nopal"),
            frutaVerdura("apio", "Apio"),
            frutaVerdura("rabano", "Rábano"),
            frutaVerdura("betabel", "Betabel", "Remolacha"),
            frutaVerdura("jengibre", "Jengibre", unidad = Unidad.GRAMO),
            CILANTRO,
            frutaVerdura("perejil", "Perejil"),
            CHICHARO,
            frutaVerdura(
                "verduras_congeladas",
                "Verduras congeladas",
                "Mezcla de verduras",
                unidad = Unidad.GRAMO,
                ubicacion = Ubicacion.CONGELADOR,
            ),

            // Carnes y pescados
            PECHUGA,
            carne("muslo_pollo", "Pierna y muslo de pollo", "Muslo de pollo", "Pierna de pollo"),
            carne("carne_molida_res", "Carne molida de res", "Carne molida", "Molida de res"),
            carne("bistec_res", "Bistec de res", "Bistec"),
            carne("carne_cerdo", "Carne de cerdo", "Cerdo", "Puerco", "Carne de puerco"),
            carne("chuleta_cerdo", "Chuleta de cerdo", "Chuleta"),
            carne("jamon", "Jamón", "Jamón de pavo"),
            carne("salchicha", "Salchicha", "Salchicha de pavo", unidad = Unidad.PIEZA),
            carne("tocino", "Tocino"),
            carne("chorizo", "Chorizo"),
            carne("filete_pescado", "Filete de pescado", "Pescado", "Tilapia", ubicacion = Ubicacion.CONGELADOR),
            carne("camaron", "Camarón", ubicacion = Ubicacion.CONGELADOR),

            // Lácteos y huevo
            HUEVO,
            LECHE,
            QUESO,
            lacteo("queso_fresco", "Queso fresco"),
            lacteo("queso_panela", "Queso panela", "Panela"),
            lacteo("queso_crema", "Queso crema", "Queso Philadelphia"),
            lacteo("crema", "Crema", "Crema ácida", "Crema de leche", unidad = Unidad.MILILITRO),
            lacteo("mantequilla", "Mantequilla"),
            lacteo("yogur", "Yogur", "Yogurt"),

            // Despensa
            ARROZ,
            despensa("frijol", "Frijol", "Frijol negro", "Frijol bayo", "Frijol pinto"),
            despensa("frijoles_lata", "Frijoles refritos", "Frijoles en lata", unidad = Unidad.PIEZA),
            despensa("lenteja", "Lenteja"),
            despensa("garbanzo", "Garbanzo"),
            PASTA,
            despensa("pasta_corta", "Pasta corta", "Coditos", "Penne", "Fusilli", "Macarrones"),
            despensa("sopa_pasta", "Sopa de pasta", "Fideo", "Letras", "Estrellitas"),
            AVENA,
            despensa("harina_trigo", "Harina de trigo", "Harina"),
            despensa("harina_maiz", "Harina de maíz", "Masa harina"),
            despensa("azucar", "Azúcar", "Azúcar blanca", "Azúcar estándar"),
            despensa("azucar_morena", "Azúcar morena", "Mascabado"),
            SAL,
            PIMIENTA,
            despensa("comino", "Comino"),
            despensa("oregano", "Orégano"),
            despensa("laurel", "Hoja de laurel", "Laurel"),
            CANELA,
            despensa("chile_guajillo", "Chile guajillo", "Guajillo"),
            despensa("chile_ancho", "Chile ancho", "Ancho"),
            despensa("chipotle", "Chile chipotle", "Chipotle", "Chipotles en adobo", unidad = Unidad.PIEZA),
            ACEITE,
            despensa("vinagre", "Vinagre", "Vinagre blanco", unidad = Unidad.MILILITRO),
            SALSA_SOYA,
            despensa("salsa_picante", "Salsa picante", "Salsa botanera", unidad = Unidad.MILILITRO),
            despensa("pure_tomate", "Puré de tomate", "Salsa de tomate", "Puré de jitomate"),
            despensa("catsup", "Cátsup", "Ketchup", ubicacion = Ubicacion.REFRIGERADOR),
            despensa("mostaza", "Mostaza", ubicacion = Ubicacion.REFRIGERADOR),
            MAYONESA,
            despensa("consome_pollo", "Consomé de pollo", "Consomé", "Caldo de pollo en polvo"),
            despensa("caldo_pollo", "Caldo de pollo", unidad = Unidad.MILILITRO),
            despensa("atun_lata", "Atún en lata", "Atún", unidad = Unidad.PIEZA),
            despensa("sardina_lata", "Sardina en lata", "Sardina", unidad = Unidad.PIEZA),
            despensa("elote_lata", "Granos de elote", "Elote en lata", "Maíz dulce"),
            despensa("leche_evaporada", "Leche evaporada", unidad = Unidad.MILILITRO),
            despensa("leche_condensada", "Leche condensada", unidad = Unidad.MILILITRO),
            TORTILLA,
            despensa(
                "tortilla_harina",
                "Tortilla de harina",
                unidad = Unidad.PIEZA,
                ubicacion = Ubicacion.REFRIGERADOR,
            ),
            despensa("tostada", "Tostada", unidad = Unidad.PIEZA),
            PAN_CAJA,
            despensa("bolillo", "Bolillo", "Telera", unidad = Unidad.PIEZA),
            despensa("cereal", "Cereal"),
            despensa("granola", "Granola"),
            despensa("galletas_saladas", "Galletas saladas", unidad = Unidad.PIEZA),
            despensa("cafe", "Café", "Café soluble", "Café molido"),
            despensa("te", "Té", unidad = Unidad.PIEZA),
            despensa("miel", "Miel", "Miel de abeja"),
            despensa("mermelada", "Mermelada"),
            despensa("crema_cacahuate", "Crema de cacahuate", "Mantequilla de maní", "Crema de maní"),
            despensa("cacahuate", "Cacahuate", "Maní"),
            despensa("nuez", "Nuez"),
            despensa("chocolate", "Chocolate de mesa", "Chocolate"),
            despensa("polvo_hornear", "Polvo para hornear", "Royal"),
        )
    }

    private val porId: Map<String, Ingrediente> = ingredientes.associateBy { it.id }

    override fun todos(): List<Ingrediente> = ingredientes

    override fun obtener(id: String): Ingrediente? = porId[id]
}
