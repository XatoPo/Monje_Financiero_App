package dam.clases.monje_financiero_app.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.services.ApiService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var switchRememberSession: Switch // Switch para recordar sesión
    private lateinit var apiService: ApiService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loadingDialog: ALoadingDialog // Agregamos el diálogo de carga

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        switchRememberSession = findViewById(R.id.switchRememberSession)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("MonjeFinancieroPrefs", MODE_PRIVATE)

        // Comprobar si la sesión está guardada
        checkSession()

        // Inicializar el servicio de API
        apiService = ApiService()

        // Inicializamos el diálogo de carga
        loadingDialog = ALoadingDialog(this) // Asegúrate de tener este diálogo implementado en tu proyecto

        // Evento de login
        btnLogin.setOnClickListener { loginUser() }

        // Evento para ir a la pantalla de registro
        btnRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkSession() {
        val userId = sharedPreferences.getString("user_id", null)
        val isRemembered = sharedPreferences.getBoolean("remember_session", false)

        if (isRemembered && userId != null) {
            // Si la sesión está guardada y es válida, redirigir a la HomeActivity
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this@LoginActivity, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar el loader antes de realizar la solicitud
        loadingDialog.show()

        try {
            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            apiService.post("users/login", json.toString(), object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        loadingDialog.dismiss() // Ocultar el diálogo de carga en caso de error
                        Toast.makeText(this@LoginActivity, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    // Ocultar el diálogo de carga al recibir la respuesta
                    runOnUiThread {
                        loadingDialog.dismiss()
                    }

                    if (response.isSuccessful) {
                        try {
                            val responseObject = JSONObject(response.body?.string())
                            val userId = responseObject.getString("userId")

                            // Guardar el userId y la preferencia de sesión
                            saveSession(userId)

                            // Obtener el token FCM una vez que el login sea exitoso
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                                    return@addOnCompleteListener
                                }
                                // Obtener el token de registro
                                val token = task.result
                                Log.d("FCM", "FCM Token: $token")

                                // Llamar al método para enviar el token FCM al servidor
                                sendFCMTokenToServer(userId, token)

                                // Redirigir a la pantalla principal
                                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Credenciales inválidas", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveSession(userId: String) {
        // Guardar el userId y el estado del switch en SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("user_id", userId)
        editor.putBoolean("remember_session", switchRememberSession.isChecked)
        editor.apply()
    }

    // Esta es la función que se llama después de obtener el token FCM
    fun sendFCMTokenToServer(userId: String, fcmToken: String) {
        // Validamos que los datos no sean nulos o vacíos
        if (userId.isEmpty() || fcmToken.isEmpty()) {
            Toast.makeText(this@LoginActivity, "El userId y el fcmToken son necesarios", Toast.LENGTH_SHORT).show()
            return
        }

        // Crea el JSON con los datos necesarios
        val json = JSONObject().apply {
            put("userId", userId)  // El ID del usuario
            put("fcmToken", fcmToken)  // El token FCM
        }

        // Enviar el JSON al servidor utilizando la API service
        apiService.post("user-tokens/save-token", json.toString(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Manejar el error de la solicitud
                Log.e("FCM", "Error al enviar el token FCM al servidor", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Si la solicitud fue exitosa, se maneja aquí
                    Log.d("FCM", "Token guardado correctamente en el servidor")
                } else {
                    // Si la solicitud no fue exitosa, manejamos el error
                    Log.e("FCM", "Error al guardar el token en el servidor")
                }
            }
        })
    }
}