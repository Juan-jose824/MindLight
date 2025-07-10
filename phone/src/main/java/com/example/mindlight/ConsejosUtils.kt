package com.example.mindlight.utils

object ConsejosUtils {
    private val consejos = listOf(
        "Respira profundamente durante un minuto.",
        "Cierra los ojos y relaja los hombros.",
        "Cuenta lentamente hasta 10.",
        "Estira los brazos y espalda.",
        "Da un paseo corto para despejarte."
    )

    fun obtenerConsejoAleatorio(): String {
        return consejos.random()
    }
}
