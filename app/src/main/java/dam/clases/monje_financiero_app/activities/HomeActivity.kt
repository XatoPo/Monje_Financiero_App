package dam.clases.monje_financiero_app.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.services.UsersService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method

class HomeActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnProfile: ImageButton
    private lateinit var usersService: UsersService
    private var userId: String? = null
    private lateinit var loadingDialog: ALoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLoadingDialog()
        setContentView(R.layout.activity_home)

        // Inicializar el servicio de usuarios
        usersService = UsersService(this)

        // Inicializar el TextView y botón de perfil
        tvWelcome = findViewById(R.id.tvWelcome)
        btnProfile = findViewById(R.id.btnProfile)

        // Obtener el userId de SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("MonjeFinancieroPrefs", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        userId?.let {
            loadUserData(it)  // Método que carga los datos del usuario
        } ?: run {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }

        // Configurar el BottomNavigationView
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.navigation_home
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
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

        // Configurar el botón de perfil
        btnProfile.setOnClickListener { showProfileMenu() }

        // Configurar botones de acciones rápidas
        configureQuickActions()
    }

    private fun showLoadingDialog() {
        loadingDialog = ALoadingDialog(this) // Inicializar el diálogo de carga
        loadingDialog.show() // Mostrar el diálogo
    }

    // Método para cargar los datos del usuario
    private fun loadUserData(userId: String) {
        usersService.getUser(userId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    loadingDialog.dismiss() // Ocultar el diálogo de carga
                    Toast.makeText(this@HomeActivity, "Error al cargar los datos del usuario", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        val responseBody = response.body?.string()
                        val jsonResponseArray = JSONArray(responseBody)

                        if (jsonResponseArray.length() > 0) {
                            val userArray = jsonResponseArray.getJSONArray(0)
                            if (userArray.length() > 0) {
                                val userData = userArray.getJSONObject(0)
                                val userName = userData.getString("name")
                                val profileImageUrl = userData.getString("profile_image_url")

                                // Actualizar la UI con los datos del usuario
                                runOnUiThread {
                                    tvWelcome.text = "Hola, $userName"

                                    // Verificar si la URL de la imagen de perfil está vacía o nula
                                    if (profileImageUrl.isNullOrEmpty()) {
                                        // Cargar una imagen predeterminada si el campo está vacío o es nulo
                                        loadingDialog.dismiss() // Ocultar el diálogo de carga
                                        Glide.with(this@HomeActivity)
                                            .load(R.drawable.ic_placeholder_avatar) // Imagen predeterminada
                                            .apply(RequestOptions.circleCropTransform()) // Transformación para hacer la imagen circular
                                            .into(btnProfile)
                                    } else {
                                        // Cargar la imagen de perfil proporcionada
                                        loadingDialog.dismiss() // Ocultar el diálogo de carga
                                        Glide.with(this@HomeActivity)
                                            .load(profileImageUrl)
                                            .apply(RequestOptions.circleCropTransform()) // Transformación para hacer la imagen circular
                                            .into(btnProfile)
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    loadingDialog.dismiss() // Ocultar el diálogo de carga
                                    Toast.makeText(this@HomeActivity, "No se encontraron datos del usuario", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            runOnUiThread {
                                loadingDialog.dismiss() // Ocultar el diálogo de carga
                                Toast.makeText(this@HomeActivity, "Respuesta vacía del servidor", Toast.LENGTH_LONG).show()
                            }
                        }

                    } catch (e: JSONException) {
                        runOnUiThread {
                            loadingDialog.dismiss() // Ocultar el diálogo de carga
                            Toast.makeText(this@HomeActivity, "Error al procesar JSON: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        loadingDialog.dismiss() // Ocultar el diálogo de carga
                        Toast.makeText(this@HomeActivity, "Error del servidor: ${response.code}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    // Método para mostrar el menú de perfil
    private fun showProfileMenu() {
        val popupMenu = PopupMenu(this, findViewById(R.id.btnProfile))
        popupMenu.menuInflater.inflate(R.menu.dropdown_menu_profile, popupMenu.menu)

        // Forzar la visualización de íconos en el PopupMenu
        try {
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menuPopupHelper = popup.get(popupMenu)
            val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
            val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
            setForceIcons.invoke(menuPopupHelper, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit_profile -> {
                    startActivity(Intent(this, ProfileSettingsActivity::class.java))
                    true
                }
                R.id.menu_guide -> {
                    // Lógica para mostrar guía
                    true
                }
                R.id.menu_office_location -> {
                    startActivity(Intent(this, LocationActivity::class.java))
                    true
                }
                R.id.menu_logout -> {
                    val sharedPreferences = getSharedPreferences("MonjeFinancieroPrefs", MODE_PRIVATE)
                    sharedPreferences.edit().clear().apply()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    // Método para configurar las acciones rápidas
    private fun configureQuickActions() {
        val btnRegisterExpense: Button = findViewById(R.id.btnRegisterExpense)
        val btnViewBudgets: Button = findViewById(R.id.btnViewBudgets)
        val btnGenerateReport: Button = findViewById(R.id.btnGenerateReport)
        val btnManageCategories: Button = findViewById(R.id.btnManageCategories)

        btnRegisterExpense.setOnClickListener {
            startActivity(Intent(this, ExpensesActivity::class.java))
        }
        btnViewBudgets.setOnClickListener {
            startActivity(Intent(this, BudgetsActivity::class.java))
        }
        btnGenerateReport.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
        btnManageCategories.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }
    }
}