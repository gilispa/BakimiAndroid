package com.bakimi.data.recipes

import com.bakimi.data.ingredients.Ingredients.OIL
import com.bakimi.data.ingredients.Ingredients.RICE
import com.bakimi.data.ingredients.Ingredients.OATS
import com.bakimi.data.ingredients.Ingredients.CINNAMON
import com.bakimi.data.ingredients.Ingredients.ONION
import com.bakimi.data.ingredients.Ingredients.PEAS
import com.bakimi.data.ingredients.Ingredients.SERRANO_PEPPER
import com.bakimi.data.ingredients.Ingredients.CILANTRO
import com.bakimi.data.ingredients.Ingredients.EGG
import com.bakimi.data.ingredients.Ingredients.TOMATO
import com.bakimi.data.ingredients.Ingredients.MILK
import com.bakimi.data.ingredients.Ingredients.LETTUCE
import com.bakimi.data.ingredients.Ingredients.LIME
import com.bakimi.data.ingredients.Ingredients.MAYONNAISE
import com.bakimi.data.ingredients.Ingredients.SLICED_BREAD
import com.bakimi.data.ingredients.Ingredients.PASTA
import com.bakimi.data.ingredients.Ingredients.CHICKEN_BREAST
import com.bakimi.data.ingredients.Ingredients.CUCUMBER
import com.bakimi.data.ingredients.Ingredients.BLACK_PEPPER
import com.bakimi.data.ingredients.Ingredients.BANANA
import com.bakimi.data.ingredients.Ingredients.MELTING_CHEESE
import com.bakimi.data.ingredients.Ingredients.SALT
import com.bakimi.data.ingredients.Ingredients.SOY_SAUCE
import com.bakimi.data.ingredients.Ingredients.CORN_TORTILLA
import com.bakimi.data.ingredients.Ingredients.CARROT
import com.bakimi.domain.inventory.MeasureUnit.GRAM
import com.bakimi.domain.inventory.MeasureUnit.MILLILITER
import com.bakimi.domain.inventory.MeasureUnit.PIECE
import com.bakimi.domain.recipes.PhotoCredit
import com.bakimi.domain.ingredients.Ingredient
import com.bakimi.domain.inventory.MeasureUnit
import com.bakimi.domain.recipes.RecipeIngredient
import com.bakimi.domain.recipes.Step
import com.bakimi.domain.recipes.Recipe
import com.bakimi.domain.recipes.RecipeRepository
import javax.inject.Inject

/**
 * Recipe catalog bundled with the app. Photos come from Unsplash (Unsplash License) and live in
 * app/src/main/res/drawable-nodpi as `recipe_<id>.jpg`.
 *
 * Protein per serving stays without data until there is a reliable source.
 */
class LocalRecipeCatalog @Inject constructor() : RecipeRepository {

    private val recipes: List<Recipe> = listOf(
        Recipe(
            id = "huevos_mexicana",
            name = "Huevos a la mexicana",
            description = "El desayuno clásico: huevo revuelto con jitomate, cebolla y chile. " +
                "Ideal para empezar a cocinar; cada paso trae una ilustración de referencia.",
            minutes = 15,
            baseServings = 2,
            ingredients = listOf(
                ing(EGG, 4.0, PIECE),
                ing(TOMATO, 1.0, PIECE),
                ing(ONION, 0.25, PIECE),
                ing(SERRANO_PEPPER, 1.0, PIECE, "sin semillas pica menos"),
                ing(OIL, 15.0, MILLILITER),
                ing(SALT, 1.0, GRAM),
            ),
            steps = listOf(
                Step(
                    "Lava el jitomate y el chile. Corta el jitomate y la cebolla en cubos pequeños, del tamaño " +
                        "de un garbanzo, y pica el chile muy fino.",
                    image = "pasos/huevos_mexicana_1.jpg",
                ),
                Step(
                    "Rompe los huevos en un tazón, agrega una pizca de sal y bátelos con un tenedor hasta que " +
                        "la yema y la clara se vean del mismo color.",
                    image = "pasos/huevos_mexicana_2.jpg",
                ),
                Step(
                    "Calienta el aceite en un sartén a fuego medio. Está listo cuando se ve brillante y se " +
                        "mueve fácil al inclinar el sartén.",
                    minutes = 1,
                    image = "pasos/huevos_mexicana_3.jpg",
                ),
                Step(
                    "Agrega el jitomate, la cebolla y el chile. Cocina moviendo con una espátula hasta que la " +
                        "cebolla se vea transparente.",
                    minutes = 3,
                    image = "pasos/huevos_mexicana_4.jpg",
                ),
                Step(
                    "Vierte el huevo y muévelo despacio con la espátula, del borde hacia el centro. Apaga el " +
                        "fuego cuando ya no se vea líquido pero siga brillante; se termina de cocer solo.",
                    minutes = 3,
                    image = "pasos/huevos_mexicana_5.jpg",
                ),
                Step(
                    "Sirve de inmediato. Si tienes tortillas, caliéntalas para acompañar.",
                    image = "pasos/huevos_mexicana_6.jpg",
                ),
            ),
            photoCredit = PhotoCredit("Andrijana Bozic", "vesnikproleca", "SBc3QKmqwpg"),
        ),
        Recipe(
            id = "huevos_estrellados",
            name = "Huevos estrellados con salsa",
            description = "Huevos fritos sobre tortilla con una salsa rápida de jitomate. " +
                "Un desayuno sencillo para empezar a cocinar.",
            minutes = 20,
            baseServings = 2,
            ingredients = listOf(
                ing(EGG, 4.0, PIECE),
                ing(CORN_TORTILLA, 4.0, PIECE),
                ing(TOMATO, 2.0, PIECE),
                ing(ONION, 0.25, PIECE),
                ing(SERRANO_PEPPER, 1.0, PIECE, "sin semillas pica menos"),
                ing(OIL, 30.0, MILLILITER),
                ing(SALT, 2.0, GRAM),
            ),
            steps = listOf(
                Step("Lava los jitomates y el chile. Corta los jitomates en cuartos y la cebolla en trozos grandes."),
                Step(
                    "Licúa el jitomate, la cebolla, el chile y una pizca de sal con un cuarto de taza de agua, " +
                        "hasta que no queden trozos.",
                ),
                Step(
                    "Calienta 1 cucharada de aceite en una olla a fuego medio. Vierte la salsa con cuidado porque " +
                        "salpica. Cocínala moviendo de vez en cuando hasta que se vea de un rojo más oscuro.",
                    minutes = 8,
                ),
                Step(
                    "En un sartén con un poco de aceite, calienta las tortillas unos segundos por lado, " +
                        "solo hasta que estén suaves. Pon dos en cada plato.",
                ),
                Step(
                    "En el mismo sartén, a fuego medio, rompe los huevos uno por uno. Están listos cuando la clara " +
                        "se ve blanca y firme; la yema puede quedar suave.",
                    minutes = 3,
                ),
                Step("Pon los huevos sobre las tortillas, báñalos con la salsa caliente y agrega sal al gusto."),
            ),
            photoCredit = PhotoCredit("Paul Hanaoka", "plhnk", "mkOpQDcFrx0"),
        ),
        Recipe(
            id = "ensalada_pollo",
            name = "Ensalada de pollo",
            description = "Pollo dorado sobre lechuga fresca con aderezo de limón. " +
                "Usa media lechuga; la otra mitad sirve para el sándwich de pollo.",
            minutes = 25,
            baseServings = 2,
            ingredients = listOf(
                ing(CHICKEN_BREAST, 300.0, GRAM),
                ing(LETTUCE, 0.5, PIECE),
                ing(TOMATO, 1.0, PIECE),
                ing(CUCUMBER, 1.0, PIECE),
                ing(LIME, 1.0, PIECE),
                ing(OIL, 30.0, MILLILITER),
                ing(SALT, 2.0, GRAM),
                ing(BLACK_PEPPER, 1.0, GRAM),
            ),
            steps = listOf(
                Step("Corta la pechuga en tiras del grosor de un dedo y sazónalas con sal y pimienta."),
                Step(
                    "Calienta 1 cucharada de aceite en un sartén a fuego medio-alto. Cocina las tiras 4 minutos " +
                        "sin moverlas, voltéalas y cocina 4 minutos más. Por dentro deben verse blancas, " +
                        "sin partes rosadas.",
                    minutes = 8,
                ),
                Step("Mientras, lava la lechuga hoja por hoja, sécala y córtala en trozos con las manos."),
                Step("Corta el jitomate y el pepino en cubos o rodajas."),
                Step("Mezcla el jugo del limón con 1 cucharada de aceite y una pizca de sal para hacer el aderezo."),
                Step(
                    "Junta la lechuga, el jitomate y el pepino, y agrega el pollo. Pon el aderezo justo antes de " +
                        "servir para que la lechuga no se marchite.",
                ),
            ),
            photoCredit = PhotoCredit("Kenneth Mallia", "kmallia77", "FFu3-WGnas0"),
        ),
        Recipe(
            id = "sandwich_pollo",
            name = "Sándwich de pollo",
            description = "Pollo deshebrado con mayonesa, lechuga y jitomate. " +
                "Comparte lechuga y pollo con la ensalada de pollo.",
            minutes = 30,
            baseServings = 2,
            ingredients = listOf(
                ing(SLICED_BREAD, 4.0, PIECE, "rebanadas"),
                ing(CHICKEN_BREAST, 200.0, GRAM),
                ing(LETTUCE, 0.25, PIECE, "unas hojas"),
                ing(TOMATO, 1.0, PIECE),
                ing(MAYONNAISE, 30.0, GRAM),
                ing(SALT, 1.0, GRAM),
                ing(BLACK_PEPPER, 1.0, GRAM),
            ),
            steps = listOf(
                Step(
                    "Pon la pechuga en una olla, cúbrela con agua y agrega una pizca de sal. Hiérvela a fuego " +
                        "medio. Está lista cuando al cortarla por la mitad ya no se ve rosada.",
                    minutes = 18,
                ),
                Step("Deja enfriar el pollo unos minutos y deshébralo con dos tenedores, jalando en direcciones opuestas."),
                Step("Mezcla el pollo con la mayonesa, la sal y la pimienta."),
                Step("Lava las hojas de lechuga y corta el jitomate en rodajas delgadas."),
                Step(
                    "Unta la mezcla de pollo sobre una rebanada de pan, agrega lechuga y jitomate, y tapa con " +
                        "otra rebanada. Corta a la mitad en diagonal.",
                ),
            ),
            photoCredit = PhotoCredit("Dillon Lobo", "dillon_lobo", "BmP0dv6WfSo"),
        ),
        Recipe(
            id = "arroz_con_pollo",
            name = "Arroz con pollo",
            description = "Un plato completo en una sola olla, con verduras. Rinde bien y se recalienta fácil.",
            minutes = 45,
            baseServings = 2,
            ingredients = listOf(
                ing(RICE, 200.0, GRAM),
                ing(CHICKEN_BREAST, 300.0, GRAM),
                ing(ONION, 0.5, PIECE),
                ing(CARROT, 1.0, PIECE),
                ing(PEAS, 100.0, GRAM),
                ing(TOMATO, 1.0, PIECE),
                ing(OIL, 30.0, MILLILITER),
                ing(SALT, 4.0, GRAM),
            ),
            steps = listOf(
                Step(
                    "Corta la pechuga en cubos medianos y sazónala con sal. Pica la cebolla, corta la zanahoria " +
                        "en cubitos y licúa el jitomate con 2 tazas de agua.",
                ),
                Step(
                    "Calienta el aceite en una olla a fuego medio y dora el pollo por todos lados. " +
                        "Sácalo y apártalo en un plato.",
                    minutes = 5,
                ),
                Step(
                    "En la misma olla, fríe el arroz crudo moviendo seguido hasta que los granos se vean " +
                        "blancos opacos y algunos empiecen a dorarse.",
                    minutes = 5,
                ),
                Step(
                    "Agrega la cebolla y cocina 2 minutos. Vierte el jitomate licuado y agrega el pollo, " +
                        "la zanahoria, los chícharos y sal.",
                    minutes = 2,
                ),
                Step(
                    "Cuando hierva, baja el fuego al mínimo, tapa la olla y no la destapes. " +
                        "El vapor es el que termina de cocer el arroz.",
                    minutes = 20,
                ),
                Step("Apaga el fuego y deja reposar 5 minutos tapado. Esponja el arroz con un tenedor antes de servir.", minutes = 5),
            ),
            photoCredit = PhotoCredit("Pablo Arroyo", "pablogamedev", "cwbA14iDDP4"),
        ),
        Recipe(
            id = "tacos_pollo",
            name = "Tacos de pollo",
            description = "Pollo dorado en tortillas calientes con cebolla, cilantro y limón.",
            minutes = 25,
            baseServings = 2,
            ingredients = listOf(
                ing(CHICKEN_BREAST, 300.0, GRAM),
                ing(CORN_TORTILLA, 8.0, PIECE),
                ing(ONION, 0.5, PIECE),
                ing(CILANTRO, 0.25, PIECE, "manojo"),
                ing(LIME, 2.0, PIECE),
                ing(OIL, 15.0, MILLILITER),
                ing(SALT, 2.0, GRAM),
            ),
            steps = listOf(
                Step("Corta la pechuga en tiras delgadas o cubos pequeños y sazónala con sal."),
                Step("Pica la cebolla y el cilantro en trozos muy pequeños y mézclalos en un plato."),
                Step(
                    "Calienta el aceite en un sartén a fuego medio-alto. Cocina el pollo moviéndolo de vez en " +
                        "cuando hasta que esté dorado por fuera y sin partes rosadas por dentro.",
                    minutes = 8,
                ),
                Step(
                    "Calienta las tortillas en un comal o sartén sin aceite, unos 30 segundos por lado, " +
                        "hasta que estén suaves y con algunas manchas doradas.",
                ),
                Step("Sirve el pollo en las tortillas, agrega cebolla con cilantro y exprime limón encima."),
            ),
            photoCredit = PhotoCredit("Alma Agencia D", "almaagenciad", "n5jDzmKMT8w"),
        ),
        Recipe(
            id = "quesadillas",
            name = "Quesadillas",
            description = "La receta más rápida del libro: solo necesitas tortillas y queso.",
            minutes = 10,
            baseServings = 2,
            ingredients = listOf(
                ing(CORN_TORTILLA, 6.0, PIECE),
                ing(MELTING_CHEESE, 150.0, GRAM),
            ),
            steps = listOf(
                Step(
                    "Si el queso es de hebra, como el Oaxaca, deshébralo con las manos. " +
                        "Si es de bloque, rállalo o córtalo en tiras delgadas.",
                ),
                Step("Calienta un comal o sartén a fuego medio, sin aceite."),
                Step("Pon una tortilla, caliéntala 20 segundos, voltéala y coloca queso sobre una mitad."),
                Step(
                    "Dobla la tortilla a la mitad y presiona suave con una espátula. Voltéala cuando esté dorada. " +
                        "Está lista cuando el queso se derrite y se ve brillante.",
                    minutes = 3,
                ),
            ),
            photoCredit = PhotoCredit("Abdullah Guc", "abdullahguch", "NTZpME8URX8"),
        ),
        Recipe(
            id = "omelette",
            name = "Omelette de queso y jitomate",
            description = "Huevo esponjoso relleno de queso derretido. Listo en 15 minutos.",
            minutes = 15,
            baseServings = 1,
            ingredients = listOf(
                ing(EGG, 2.0, PIECE),
                ing(MELTING_CHEESE, 40.0, GRAM),
                ing(TOMATO, 0.5, PIECE),
                ing(ONION, 0.25, PIECE),
                ing(OIL, 5.0, MILLILITER),
                ing(SALT, 1.0, GRAM),
            ),
            steps = listOf(
                Step("Pica el jitomate y la cebolla en cubos pequeños. Ralla o deshebra el queso."),
                Step(
                    "Bate los huevos en un tazón con un tenedor y una pizca de sal, hasta que la yema y la clara " +
                        "se vean del mismo color.",
                ),
                Step(
                    "Calienta el aceite en un sartén antiadherente a fuego medio-bajo y cocina la cebolla " +
                        "y el jitomate.",
                    minutes = 2,
                ),
                Step("Vierte el huevo y mueve el sartén para que cubra todo el fondo. No lo revuelvas."),
                Step(
                    "Cuando la orilla se vea cocida y el centro casi firme, pon el queso en una mitad, " +
                        "dobla con una espátula y cocina 1 minuto más.",
                    minutes = 3,
                ),
            ),
            photoCredit = PhotoCredit("Roman Shalkevic", "ramshteks", "8gYyDWCH6RQ"),
        ),
        Recipe(
            id = "pasta_jitomate",
            name = "Pasta con salsa de jitomate",
            description = "Salsa casera con jitomate fresco y cebolla. Económica y para cualquier día.",
            minutes = 25,
            baseServings = 2,
            ingredients = listOf(
                ing(PASTA, 200.0, GRAM),
                ing(TOMATO, 4.0, PIECE),
                ing(ONION, 0.5, PIECE),
                ing(OIL, 30.0, MILLILITER),
                ing(SALT, 6.0, GRAM),
                ing(BLACK_PEPPER, 1.0, GRAM),
            ),
            steps = listOf(
                Step("Pon a hervir 2 litros de agua con 1 cucharadita de sal en una olla grande."),
                Step("Mientras, pica la cebolla y corta los jitomates en cubos pequeños."),
                Step(
                    "Cuando el agua hierva con burbujas grandes, agrega la pasta y revuelve para que no se pegue. " +
                        "Cocínala el tiempo que indique el paquete. Debe quedar suave pero firme al morderla.",
                    minutes = 10,
                ),
                Step(
                    "En un sartén con el aceite a fuego medio, cocina la cebolla hasta que se vea transparente.",
                    minutes = 3,
                ),
                Step(
                    "Agrega el jitomate, sal y pimienta. Cocina aplastando con una cuchara hasta que se forme " +
                        "una salsa espesa.",
                    minutes = 8,
                ),
                Step("Escurre la pasta con cuidado, porque el vapor quema. Mézclala con la salsa y sirve."),
            ),
            photoCredit = PhotoCredit("Danijela Prijovic", "dacakockica", "qits91IZv1o"),
        ),
        Recipe(
            id = "avena_platano",
            name = "Avena con plátano",
            description = "Desayuno caliente, económico y rápido.",
            minutes = 10,
            baseServings = 1,
            ingredients = listOf(
                ing(OATS, 50.0, GRAM),
                ing(MILK, 250.0, MILLILITER),
                ing(BANANA, 1.0, PIECE),
                ing(CINNAMON, 1.0, GRAM),
            ),
            steps = listOf(
                Step("Pon la leche y la avena en una olla pequeña a fuego medio."),
                Step(
                    "Mueve con una cuchara de vez en cuando. Cuando empiece a hervir, baja el fuego y cocina " +
                        "hasta que espese y la cuchara deje un camino al pasar.",
                    minutes = 5,
                ),
                Step("Rebana el plátano en rodajas."),
                Step("Sirve la avena, acomoda el plátano encima y espolvorea la canela."),
            ),
            photoCredit = PhotoCredit("Lex Sirikiat", "pundalex", "by7IeC6Qa0k"),
        ),
        Recipe(
            id = "arroz_frito",
            name = "Arroz frito con huevo",
            description = "Arroz salteado con verduras, huevo y salsa de soya. " +
                "Si te sobró arroz cocido, sáltate el primer paso.",
            minutes = 40,
            baseServings = 2,
            ingredients = listOf(
                ing(RICE, 150.0, GRAM),
                ing(EGG, 2.0, PIECE),
                ing(CARROT, 1.0, PIECE),
                ing(PEAS, 80.0, GRAM),
                ing(ONION, 0.5, PIECE),
                ing(SOY_SAUCE, 30.0, MILLILITER),
                ing(OIL, 30.0, MILLILITER),
                ing(SALT, 2.0, GRAM),
            ),
            steps = listOf(
                Step(
                    "Cuece el arroz: ponlo en una olla con el doble de agua y una pizca de sal. Cuando hierva, " +
                        "tapa, baja el fuego al mínimo y espera 18 minutos. Extiéndelo en un plato para que se enfríe.",
                    minutes = 20,
                ),
                Step("Pica la cebolla y la zanahoria en cubitos."),
                Step(
                    "Bate los huevos. En un sartén grande con 1 cucharada de aceite, cocínalos revolviendo " +
                        "hasta que estén firmes. Sácalos y apártalos.",
                    minutes = 2,
                ),
                Step(
                    "Agrega el resto del aceite, la cebolla, la zanahoria y los chícharos. Cocina a fuego " +
                        "medio-alto moviendo seguido hasta que la zanahoria esté suave.",
                    minutes = 5,
                ),
                Step(
                    "Agrega el arroz frío y la salsa de soya. Mezcla y deja que el arroz se caliente bien.",
                    minutes = 3,
                ),
                Step("Regresa el huevo, revuelve y sirve."),
            ),
            photoCredit = PhotoCredit("Christopher Alvarenga", "krizphoto", "rQX9eVpSFz8"),
        ),
    )

    private val byId: Map<String, Recipe> = recipes.associateBy { it.id }

    override fun all(): List<Recipe> = recipes

    override fun get(id: String): Recipe? = byId[id]
}

private fun ing(ingredient: Ingredient, quantity: Double, unit: MeasureUnit, note: String? = null) =
    RecipeIngredient(ingredient, quantity, unit, note)
