package com.bakimi.domain.inventario

/**
 * Calcula la cantidad que falta comprar de un ingrediente.
 *
 * faltante = max(0, requerido total - existencia)
 *
 * Ambas cantidades deben expresarse en la misma unidad base (g, ml o piezas).
 *
 * @param requerido cantidad total requerida por las recetas seleccionadas.
 * @param existencia cantidad disponible en el inventario.
 * @return la cantidad faltante; nunca es negativa.
 */
fun calcularFaltante(requerido: Double, existencia: Double): Double =
    maxOf(0.0, requerido - existencia)
