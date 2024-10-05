package dam.clases.monje_financiero_app.models

data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String,
    val password: String,
    val presupuestos: List<Presupuesto>,
    val gastos: List<Gasto>
)