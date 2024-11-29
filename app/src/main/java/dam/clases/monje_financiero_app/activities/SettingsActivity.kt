package dam.clases.monje_financiero_app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.services.ApiService
import android.content.SharedPreferences
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import com.google.android.material.bottomnavigation.BottomNavigationView
import dam.clases.monje_financiero_app.services.UsersService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchNotifications: Switch
    private lateinit var spinnerNotificationFrequency: Spinner
    private lateinit var btnResetSettings: Button
    private lateinit var apiService: ApiService
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Inicializar vistas
        apiService = ApiService()
        switchNotifications = findViewById(R.id.switchNotifications)
        spinnerNotificationFrequency = findViewById(R.id.spinnerNotificationFrequency)
        btnResetSettings = findViewById(R.id.btnResetSettings)

        // Inicializamos SharedPreferences
        sharedPreferences = getSharedPreferences("MonjeFinancieroPrefs", MODE_PRIVATE)

        // Llenar el Spinner con opciones de frecuencia
        val frequencyOptions = arrayOf("Diario", "Semanal", "Mensual")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, frequencyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNotificationFrequency.adapter = adapter


        // Manejo del botón de restablecer configuración
        btnResetSettings.setOnClickListener {
            resetSettings()
        }

        // Cargar las preferencias del usuario
        loadNotificationSettings()

        // Cambiar la configuración de notificaciones
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val frequency = spinnerNotificationFrequency.selectedItem.toString()

            // Llamar al backend para actualizar la configuración
            updateNotificationSettings(isChecked, frequency)
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.navigation_settings
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navigation_expenses -> {
                    startActivity(Intent(this, ExpensesActivity::class.java))
                    true
                }
                R.id.navigation_budgets -> {
                    startActivity(Intent(this, BudgetsActivity::class.java))
                    true
                }
                R.id.navigation_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadNotificationSettings() {
        val userId = sharedPreferences.getString("user_id", null)
        val isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false)
        val frequency = sharedPreferences.getString("notification_frequency", "Diario")

        // Pre-llenar la UI con los datos existentes
        switchNotifications.isChecked = isNotificationsEnabled
        spinnerNotificationFrequency.setSelection(getFrequencyPosition(frequency))
    }

    private fun getFrequencyPosition(frequency: String?): Int {
        return when (frequency) {
            "Diario" -> 0
            "Semanal" -> 1
            "Mensual" -> 2
            else -> 0
        }
    }

    private fun updateNotificationSettings(notificationsEnabled: Boolean, frequency: String) {
        val userId = sharedPreferences.getString("user_id", null) ?: return

        // Actualizar la configuración en el backend
        val json = JSONObject().apply {
            put("userId", userId)
            put("notificationsEnabled", notificationsEnabled)
            put("notificationFrequency", frequency)
        }

        apiService.post("user-tokens/update-notification-settings", json.toString(), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@SettingsActivity, "Error al actualizar configuración", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@SettingsActivity, "Configuración actualizada", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@SettingsActivity, "Error en la actualización", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // Restablecer la configuración a los valores predeterminados
    private fun resetSettings() {
        // Restablecer las preferencias a los valores predeterminados
        val sharedPref = getSharedPreferences("MonjeFinancieroPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.remove("notification_frequency")
        editor.remove("notifications_enabled")
        editor.apply()

        // Volver a establecer las vistas a los valores predeterminados
        switchNotifications.isChecked = false
        spinnerNotificationFrequency.setSelection(0) // Diario
    }
}