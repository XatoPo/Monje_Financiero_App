package dam.clases.monje_financiero_app.activities

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.models.Category
import dam.clases.monje_financiero_app.services.CategoriesService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class CategoriesActivity : AppCompatActivity() {
    private lateinit var etCategoryName: TextInputEditText
    private lateinit var btnAddCategory: Button
    private lateinit var gridIcons: GridLayout
    private lateinit var categoriesService: CategoriesService
    private var userId: String? = null
    private var selectedColor: String = "#FF5733" // Color por defecto
    private var selectedIconText: String = "🍔" // Ícono por defecto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        etCategoryName = findViewById(R.id.etCategoryName)
        btnAddCategory = findViewById(R.id.btnAddCategory)
        gridIcons = findViewById(R.id.iconContainer)

        // Obtener el userId de SharedPreferences
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        categoriesService = CategoriesService(this)

        // Configurar el botón para agregar categoría
        btnAddCategory.setOnClickListener {
            addCategory()
        }

        // Configurar iconos en el GridLayout
        setupIconSelection()
    }

    private fun addCategory() {
        val categoryName = etCategoryName.text.toString().trim()

        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Ingresa el nombre de la categoría", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val json = JSONObject().apply {
                put("user_id", userId)
                put("name", categoryName)
                put("color", selectedColor)
                put("icon_text", selectedIconText)
            }

            categoriesService.addCategory(userId!!, categoryName, selectedColor, selectedIconText, object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@CategoriesActivity, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@CategoriesActivity, "Categoría agregada", Toast.LENGTH_SHORT).show()
                            etCategoryName.setText("") // Limpiar el campo
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@CategoriesActivity, "Error al agregar categoría", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showColorPickerDialog() {
        // Más opciones de colores
        val colors = arrayOf(
            "#FF5733", "#FF8D1F", "#FFC300", "#DAF7A6", "#33FF57",
            "#57FF33", "#33FFB2", "#33B2FF", "#3357FF", "#5733FF",
            "#FF33F6", "#FF3333", "#8D33FF", "#FF33A8", "#FF1F57"
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccionar un color")
            .setItems(colors) { dialog, which ->
                selectedColor = colors[which]
                btnAddCategory.setBackgroundColor(Color.parseColor(selectedColor)) // Cambiar color de fondo del botón
                Toast.makeText(this, "Color seleccionado: $selectedColor", Toast.LENGTH_SHORT).show()
            }
        builder.show()
    }

    private fun setupIconSelection() {
        // Lista de emojis para seleccionar
        val emojis = arrayOf("🍔", "🍕", "🍣", "🍩", "🍦", "🍇", "🌟",
            "💰", "🛒", "🏡", "📚", "🎉", "🚗", "⚽",
            "📈", "📅", "💼", "🎮", "🎤", "🎨", "🎁")

        for (emoji in emojis) {
            val emojiView = TextView(this).apply {
                text = emoji
                textSize = 24f
                setPadding(16, 16, 16, 16)
                setOnClickListener {
                    selectedIconText = emoji // Guardar el ícono seleccionado
                    Toast.makeText(this@CategoriesActivity, "Ícono seleccionado: $selectedIconText", Toast.LENGTH_SHORT).show()
                }
            }
            gridIcons.addView(emojiView)
        }
    }
}