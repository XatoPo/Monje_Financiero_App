package dam.clases.monje_financiero_app.models

import com.orm.SugarRecord

data class Session(
    val userId: String,
    val email: String,
    var sessionStatus: Boolean  // true: Activa, false: Inactiva
) : SugarRecord() {

    // Constructor vacío requerido por SugarORM
    constructor() : this("", "", false)

    // Método personalizado para obtener una sesión activa desde la base de datos
    companion object {
        fun listAllSessions(): List<Session> {
            // Usamos SugarORM para obtener todos los registros de la tabla
            return SugarRecord.listAll(Session::class.java)
        }

        fun findActiveSession(): Session? {
            // Buscar la primera sesión activa (sessionStatus = true)
            return SugarRecord.find(Session::class.java, "sessionStatus = ?", "1").firstOrNull()
        }
    }
}