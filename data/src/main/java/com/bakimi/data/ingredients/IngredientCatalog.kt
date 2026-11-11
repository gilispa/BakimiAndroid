package com.bakimi.data.ingredients

import com.bakimi.domain.ingredients.Category
import com.bakimi.domain.ingredients.PurchaseInfo
import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.ingredients.IngredientRepository
import com.bakimi.domain.inventory.StorageLocation
import com.bakimi.domain.inventory.MeasureUnit
import javax.inject.Inject

/**
 * Approximate weight of one piece, in grams, to compare "1 kg de tortillas" with "6 tortillas".
 * These are reference values for medium pieces; that is why the app shows these conversions as
 * approximate. Without an entry here, the ingredient is not converted between pieces and weight
 * (for example, garlic: a "piece" can be a head or a clove).
 */
internal val GRAMS_PER_PIECE: Map<String, Double> = mapOf(
    // Fruits and vegetables
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
    // Meat
    "pechuga_pollo" to 250.0,
    "muslo_pollo" to 250.0,
    "bistec_res" to 120.0,
    "chuleta_cerdo" to 200.0,
    "salchicha" to 40.0,
    // Eggs and bakery
    "huevo" to 50.0,
    "tortilla_maiz" to 30.0,
    "tortilla_harina" to 40.0,
    "tostada" to 15.0,
    "pan_caja" to 25.0,
    "bolillo" to 70.0,
)

/** Date of the reference prices; shown next to the estimates. */
const val REFERENCE_PRICES_DATE = "nov 2026"

/**
 * How each ingredient is bought and its approximate reference price in MXN, per package. These are
 * estimates without a verified source (country, currency and price source are still pending); the
 * app shows them as "aprox.". Changing a price means editing one line.
 */
internal val PURCHASE_INFO: Map<String, PurchaseInfo> = mapOf(
    // Fruits and vegetables: by the piece
    "jitomate" to PurchaseInfo(MeasureUnit.PIECE, 1.0, price = 4.0),
    "cebolla" to PurchaseInfo(MeasureUnit.PIECE, 1.0, price = 8.0),
    "chile_serrano" to PurchaseInfo(MeasureUnit.PIECE, 1.0, price = 1.0),
    "lechuga" to PurchaseInfo(MeasureUnit.PIECE, 1.0, price = 22.0),
    "pepino" to PurchaseInfo(MeasureUnit.PIECE, 1.0, price = 7.0),
    "limon" to PurchaseInfo(MeasureUnit.PIECE, 1.0, price = 2.0),
    "zanahoria" to PurchaseInfo(MeasureUnit.PIECE, 1.0, price = 3.0),
    "cilantro" to PurchaseInfo(MeasureUnit.PIECE, 1.0, "manojo", 8.0),
    "platano" to PurchaseInfo(MeasureUnit.PIECE, 1.0, price = 4.0),
    "chicharo" to PurchaseInfo(MeasureUnit.GRAM, 500.0, "bolsa de 500 g", 38.0),
    // Meat: sold loose, in 100 g steps
    "pechuga_pollo" to PurchaseInfo(MeasureUnit.GRAM, 100.0, price = 17.0),
    // Dairy and eggs
    "huevo" to PurchaseInfo(MeasureUnit.PIECE, 12.0, "docena", 45.0),
    "leche" to PurchaseInfo(MeasureUnit.MILLILITER, 1000.0, "litro", 30.0),
    "queso_fundir" to PurchaseInfo(MeasureUnit.GRAM, 100.0, price = 20.0),
    // Pantry
    "tortilla_maiz" to PurchaseInfo(MeasureUnit.GRAM, 500.0, "medio kilo", 14.0),
    "pan_caja" to PurchaseInfo(MeasureUnit.PIECE, 20.0, "paquete de 20 rebanadas", 52.0),
    "arroz" to PurchaseInfo(MeasureUnit.GRAM, 1000.0, "bolsa de 1 kg", 34.0),
    "pasta" to PurchaseInfo(MeasureUnit.GRAM, 200.0, "paquete de 200 g", 16.0),
    "avena" to PurchaseInfo(MeasureUnit.GRAM, 400.0, "bolsa de 400 g", 28.0),
    "aceite" to PurchaseInfo(MeasureUnit.MILLILITER, 1000.0, "botella de 1 l", 48.0),
    "sal" to PurchaseInfo(MeasureUnit.GRAM, 1000.0, "bolsa de 1 kg", 16.0),
    "pimienta" to PurchaseInfo(MeasureUnit.GRAM, 50.0, "frasco de 50 g", 32.0),
    "canela" to PurchaseInfo(MeasureUnit.GRAM, 30.0, "frasco de 30 g", 28.0),
    "salsa_soya" to PurchaseInfo(MeasureUnit.MILLILITER, 150.0, "botella de 150 ml", 32.0),
    "mayonesa" to PurchaseInfo(MeasureUnit.GRAM, 390.0, "frasco de 390 g", 48.0),
)

/** Staples that almost every recipe uses; they do not count as a "shared ingredient". */
internal val STAPLES: Set<String> = setOf(
    "sal", "pimienta", "aceite", "comino", "oregano", "laurel", "canela", "vinagre", "azucar",
)

private fun ingredient(
    id: String,
    name: String,
    synonyms: Array<out String>,
    category: Category,
    unit: MeasureUnit,
    location: StorageLocation,
) = Ingredient(
    id = id,
    name = name,
    synonyms = synonyms.toList(),
    category = category,
    suggestedUnit = unit,
    suggestedLocation = location,
    gramsPerPiece = GRAMS_PER_PIECE[id],
    purchase = PURCHASE_INFO[id],
    staple = id in STAPLES,
)

private fun produce(
    id: String,
    name: String,
    vararg synonyms: String,
    unit: MeasureUnit = MeasureUnit.PIECE,
    location: StorageLocation = StorageLocation.FRIDGE,
) = ingredient(id, name, synonyms, Category.PRODUCE, unit, location)

private fun meat(
    id: String,
    name: String,
    vararg synonyms: String,
    unit: MeasureUnit = MeasureUnit.GRAM,
    location: StorageLocation = StorageLocation.FRIDGE,
) = ingredient(id, name, synonyms, Category.MEAT, unit, location)

private fun dairy(
    id: String,
    name: String,
    vararg synonyms: String,
    unit: MeasureUnit = MeasureUnit.GRAM,
) = ingredient(id, name, synonyms, Category.DAIRY_EGGS, unit, StorageLocation.FRIDGE)

private fun pantry(
    id: String,
    name: String,
    vararg synonyms: String,
    unit: MeasureUnit = MeasureUnit.GRAM,
    location: StorageLocation = StorageLocation.PANTRY,
) = ingredient(id, name, synonyms, Category.PANTRY, unit, location)

/**
 * Ingredients used by the recipes. They are separate so the recipe catalog can refer to them by
 * name; all of them are part of [LocalIngredientCatalog].
 */
internal object Ingredients {
    val EGG = dairy("huevo", "Huevo", "Blanquillo", unit = MeasureUnit.PIECE)
    val MILK = dairy("leche", "Leche", "Leche entera", "Leche deslactosada", unit = MeasureUnit.MILLILITER)
    val MELTING_CHEESE = dairy(
        "queso_fundir",
        "Queso para fundir",
        "Queso",
        "Queso Oaxaca",
        "Queso manchego",
        "Queso Chihuahua",
        "Queso mozzarella",
    )

    val TOMATO = produce("jitomate", "Jitomate", "Tomate", "Tomate rojo", "Jitomate saladet")
    val ONION = produce("cebolla", "Cebolla", "Cebolla blanca", location = StorageLocation.PANTRY)
    val SERRANO_PEPPER = produce("chile_serrano", "Chile serrano", "Serrano")
    val LETTUCE = produce("lechuga", "Lechuga", "Lechuga romana", "Lechuga orejona", "Lechuga italiana")
    val CUCUMBER = produce("pepino", "Pepino")
    val LIME = produce("limon", "Limón")
    val CARROT = produce("zanahoria", "Zanahoria")
    val CILANTRO = produce("cilantro", "Cilantro")
    val BANANA = produce("platano", "Plátano", "Banana", "Banano", location = StorageLocation.PANTRY)
    val PEAS = produce(
        "chicharo",
        "Chícharo",
        "Guisante",
        "Arveja",
        unit = MeasureUnit.GRAM,
        location = StorageLocation.FREEZER,
    )

    val CHICKEN_BREAST = meat("pechuga_pollo", "Pechuga de pollo", "Pechuga", "Pollo")

    val OIL = pantry(
        "aceite",
        "Aceite",
        "Aceite vegetal",
        "Aceite de cocina",
        "Aceite de oliva",
        unit = MeasureUnit.MILLILITER,
    )
    val SALT = pantry("sal", "Sal", "Sal de mesa")
    val BLACK_PEPPER = pantry("pimienta", "Pimienta", "Pimienta negra", "Pimienta molida")
    val CINNAMON = pantry("canela", "Canela molida", "Canela")
    val SOY_SAUCE = pantry("salsa_soya", "Salsa de soya", "Salsa de soja", "Soya", unit = MeasureUnit.MILLILITER)
    val MAYONNAISE = pantry("mayonesa", "Mayonesa", location = StorageLocation.FRIDGE)
    val RICE = pantry("arroz", "Arroz", "Arroz blanco", "Arroz largo")
    val PASTA = pantry("pasta", "Pasta", "Espagueti", "Spaghetti", "Pasta larga")
    val OATS = pantry("avena", "Avena", "Avena en hojuelas", "Hojuelas de avena")
    val CORN_TORTILLA = pantry(
        "tortilla_maiz",
        "Tortilla de maíz",
        "Tortilla",
        unit = MeasureUnit.PIECE,
        location = StorageLocation.FRIDGE,
    )
    val SLICED_BREAD = pantry("pan_caja", "Pan de caja", "Pan de molde", "Pan blanco", unit = MeasureUnit.PIECE)
}

/**
 * Ingredient catalog to search when adding foods in Mi refri.
 *
 * Synonyms only include names of the same product, never substitutes: "tortilla de harina" is
 * not a synonym of corn tortilla.
 */
class LocalIngredientCatalog @Inject constructor() : IngredientRepository {

    private val ingredients: List<Ingredient> = with(Ingredients) {
        listOf(
            // Fruits and vegetables
            TOMATO,
            produce("tomate_verde", "Tomate verde", "Tomatillo", "Tomate de cáscara"),
            ONION,
            produce("cebolla_morada", "Cebolla morada", "Cebolla roja", location = StorageLocation.PANTRY),
            produce("ajo", "Ajo", "Cabeza de ajo", "Diente de ajo", location = StorageLocation.PANTRY),
            SERRANO_PEPPER,
            produce("chile_jalapeno", "Chile jalapeño", "Jalapeño"),
            produce("chile_poblano", "Chile poblano", "Poblano"),
            produce("chile_habanero", "Chile habanero", "Habanero"),
            produce("pimiento_morron", "Pimiento morrón", "Pimiento", "Morrón"),
            produce("aguacate", "Aguacate", "Palta", location = StorageLocation.PANTRY),
            LIME,
            produce("naranja", "Naranja"),
            produce("manzana", "Manzana"),
            BANANA,
            produce("pera", "Pera"),
            produce("mango", "Mango"),
            produce("papaya", "Papaya"),
            produce("pina", "Piña"),
            produce("sandia", "Sandía"),
            produce("melon", "Melón"),
            produce("fresa", "Fresa", unit = MeasureUnit.GRAM),
            produce("uva", "Uva", unit = MeasureUnit.GRAM),
            LETTUCE,
            produce("espinaca", "Espinaca", unit = MeasureUnit.GRAM),
            produce("acelga", "Acelga"),
            produce("col", "Col", "Repollo"),
            produce("brocoli", "Brócoli"),
            produce("coliflor", "Coliflor"),
            produce("calabacita", "Calabacita", "Calabaza italiana", "Zucchini", "Calabacín"),
            CARROT,
            produce("papa", "Papa", "Patata", location = StorageLocation.PANTRY),
            produce("camote", "Camote", "Batata", location = StorageLocation.PANTRY),
            CUCUMBER,
            produce("elote", "Elote", "Mazorca"),
            produce("ejote", "Ejote", "Judía verde", unit = MeasureUnit.GRAM),
            produce("champinon", "Champiñón", "Hongo blanco", unit = MeasureUnit.GRAM),
            produce("nopal", "Nopal"),
            produce("apio", "Apio"),
            produce("rabano", "Rábano"),
            produce("betabel", "Betabel", "Remolacha"),
            produce("jengibre", "Jengibre", unit = MeasureUnit.GRAM),
            CILANTRO,
            produce("perejil", "Perejil"),
            PEAS,
            produce(
                "verduras_congeladas",
                "Verduras congeladas",
                "Mezcla de verduras",
                unit = MeasureUnit.GRAM,
                location = StorageLocation.FREEZER,
            ),

            // Meat and fish
            CHICKEN_BREAST,
            meat("muslo_pollo", "Pierna y muslo de pollo", "Muslo de pollo", "Pierna de pollo"),
            meat("carne_molida_res", "Carne molida de res", "Carne molida", "Molida de res"),
            meat("bistec_res", "Bistec de res", "Bistec"),
            meat("carne_cerdo", "Carne de cerdo", "Cerdo", "Puerco", "Carne de puerco"),
            meat("chuleta_cerdo", "Chuleta de cerdo", "Chuleta"),
            meat("jamon", "Jamón", "Jamón de pavo"),
            meat("salchicha", "Salchicha", "Salchicha de pavo", unit = MeasureUnit.PIECE),
            meat("tocino", "Tocino"),
            meat("chorizo", "Chorizo"),
            meat("filete_pescado", "Filete de pescado", "Pescado", "Tilapia", location = StorageLocation.FREEZER),
            meat("camaron", "Camarón", location = StorageLocation.FREEZER),

            // Dairy and eggs
            EGG,
            MILK,
            MELTING_CHEESE,
            dairy("queso_fresco", "Queso fresco"),
            dairy("queso_panela", "Queso panela", "Panela"),
            dairy("queso_crema", "Queso crema", "Queso Philadelphia"),
            dairy("crema", "Crema", "Crema ácida", "Crema de leche", unit = MeasureUnit.MILLILITER),
            dairy("mantequilla", "Mantequilla"),
            dairy("yogur", "Yogur", "Yogurt"),

            // Pantry
            RICE,
            pantry("frijol", "Frijol", "Frijol negro", "Frijol bayo", "Frijol pinto"),
            pantry("frijoles_lata", "Frijoles refritos", "Frijoles en lata", unit = MeasureUnit.PIECE),
            pantry("lenteja", "Lenteja"),
            pantry("garbanzo", "Garbanzo"),
            PASTA,
            pantry("pasta_corta", "Pasta corta", "Coditos", "Penne", "Fusilli", "Macarrones"),
            pantry("sopa_pasta", "Sopa de pasta", "Fideo", "Letras", "Estrellitas"),
            OATS,
            pantry("harina_trigo", "Harina de trigo", "Harina"),
            pantry("harina_maiz", "Harina de maíz", "Masa harina"),
            pantry("azucar", "Azúcar", "Azúcar blanca", "Azúcar estándar"),
            pantry("azucar_morena", "Azúcar morena", "Mascabado"),
            SALT,
            BLACK_PEPPER,
            pantry("comino", "Comino"),
            pantry("oregano", "Orégano"),
            pantry("laurel", "Hoja de laurel", "Laurel"),
            CINNAMON,
            pantry("chile_guajillo", "Chile guajillo", "Guajillo"),
            pantry("chile_ancho", "Chile ancho", "Ancho"),
            pantry("chipotle", "Chile chipotle", "Chipotle", "Chipotles en adobo", unit = MeasureUnit.PIECE),
            OIL,
            pantry("vinagre", "Vinagre", "Vinagre blanco", unit = MeasureUnit.MILLILITER),
            SOY_SAUCE,
            pantry("salsa_picante", "Salsa picante", "Salsa botanera", unit = MeasureUnit.MILLILITER),
            pantry("pure_tomate", "Puré de tomate", "Salsa de tomate", "Puré de jitomate"),
            pantry("catsup", "Cátsup", "Ketchup", location = StorageLocation.FRIDGE),
            pantry("mostaza", "Mostaza", location = StorageLocation.FRIDGE),
            MAYONNAISE,
            pantry("consome_pollo", "Consomé de pollo", "Consomé", "Caldo de pollo en polvo"),
            pantry("caldo_pollo", "Caldo de pollo", unit = MeasureUnit.MILLILITER),
            pantry("atun_lata", "Atún en lata", "Atún", unit = MeasureUnit.PIECE),
            pantry("sardina_lata", "Sardina en lata", "Sardina", unit = MeasureUnit.PIECE),
            pantry("elote_lata", "Granos de elote", "Elote en lata", "Maíz dulce"),
            pantry("leche_evaporada", "Leche evaporada", unit = MeasureUnit.MILLILITER),
            pantry("leche_condensada", "Leche condensada", unit = MeasureUnit.MILLILITER),
            CORN_TORTILLA,
            pantry(
                "tortilla_harina",
                "Tortilla de harina",
                unit = MeasureUnit.PIECE,
                location = StorageLocation.FRIDGE,
            ),
            pantry("tostada", "Tostada", unit = MeasureUnit.PIECE),
            SLICED_BREAD,
            pantry("bolillo", "Bolillo", "Telera", unit = MeasureUnit.PIECE),
            pantry("cereal", "Cereal"),
            pantry("granola", "Granola"),
            pantry("galletas_saladas", "Galletas saladas", unit = MeasureUnit.PIECE),
            pantry("cafe", "Café", "Café soluble", "Café molido"),
            pantry("te", "Té", unit = MeasureUnit.PIECE),
            pantry("miel", "Miel", "Miel de abeja"),
            pantry("mermelada", "Mermelada"),
            pantry("crema_cacahuate", "Crema de cacahuate", "Mantequilla de maní", "Crema de maní"),
            pantry("cacahuate", "Cacahuate", "Maní"),
            pantry("nuez", "Nuez"),
            pantry("chocolate", "Chocolate de mesa", "Chocolate"),
            pantry("polvo_hornear", "Polvo para hornear", "Royal"),
        )
    }

    private val byId: Map<String, Ingredient> = ingredients.associateBy { it.id }

    override fun all(): List<Ingredient> = ingredients

    override fun get(id: String): Ingredient? = byId[id]
}
