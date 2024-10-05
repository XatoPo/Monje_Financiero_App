package dam.clases.monje_financiero_app.models

data class Presupuesto(
    val id: Int,
    val nombre: String,
    val montoTotal: Double,
    val gastosTotales: Double,
    val categorias: List<Categoria>,
    val frecuencia: String
)
