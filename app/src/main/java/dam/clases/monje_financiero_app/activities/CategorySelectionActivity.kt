package dam.clases.monje_financiero_app.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.services.CategoriesService
import dam.clases.monje_financiero_app.models.Category
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import androidx.core.graphics.ColorUtils

class CategorySelectionActivity : AppCompatActivity() {
    private lateinit var linearLayoutCategories: LinearLayout
    private lateinit var btnClose: Button
    private lateinit var categoriesService: CategoriesService
    private lateinit var gridLayoutCategories: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_category_selection)

        gridLayoutCategories = findViewById(R.id.gridLayoutCategories)
        btnClose = findViewById(R.id.btnClose)

        categoriesService = CategoriesService(this)

        // Cargar las categorías desde la base de datos
        loadCategories()

        // Configurar el botón de cerrar
        btnClose.setOnClickListener {
            finish() // Cerrar la actividad
        }
    }

    private fun loadCategories() {
        categoriesService.getAllCategories(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CategorySelectionActivity, "Error al cargar categorías", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("CategorySelectionActivity", "Response Body: $responseBody")
                    try {
                        // Parsear el JSONArray principal
                        val jsonArray = JSONArray(responseBody)

                        // Verificar que el primer elemento sea un JSONArray
                        val categoriesJsonArray = jsonArray.getJSONArray(0)

                        runOnUiThread {
                            for (i in 0 until categoriesJsonArray.length()) {
                                val categoryJson = categoriesJsonArray.getJSONObject(i)
                                val category = Category(
                                    id = categoryJson.getString("id"),
                                    userId = categoryJson.getString("user_id"),
                                    name = categoryJson.getString("name"),
                                    color = categoryJson.getString("color"),
                                    iconText = categoryJson.getString("icon_text")
                                )
                                addCategoryToView(category)
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this@CategorySelectionActivity, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CategorySelectionActivity, "Error al obtener categorías", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun addCategoryToView(category: Category) {
        // Inflar el layout de la categoría
        val categoryView = layoutInflater.inflate(R.layout.category_item, null)

        // Obtener referencias a los elementos del layout
        val iconTextView = categoryView.findViewById<TextView>(R.id.iconText)
        val categoryNameTextView = categoryView.findViewById<TextView>(R.id.categoryName)

        // Crear un GradientDrawable
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f // Radio de los bordes redondeados
            setStroke(1, Color.DKGRAY) // Color del borde
            val originalColor = Color.parseColor(category.color)
            val alpha = (0.8 * 255).toInt() // 80% opacidad
            setColor((alpha shl 24) or (originalColor and 0x00FFFFFF)) // Color de fondo con opacidad
        }

        // Aplicar el Drawable al fondo del categoryView
        categoryView.background = drawable

        // Establecer el icono y el nombre
        iconTextView.text = category.iconText
        categoryNameTextView.text = category.name

        // Cambiar el color del texto a negro
        iconTextView.setTextColor(Color.BLACK)
        categoryNameTextView.setTextColor(Color.BLACK)

        // Establecer el click listener
        categoryView.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("selectedCategory", category) // Pasar la categoría como Parcelable
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish() // Cerrar el diálogo
        }

        // Crear LayoutParams para el espaciado
        val params = GridLayout.LayoutParams()
        params.setMargins(8, 8, 8, 8) // Margen de 8dp en todos los lados
        params.width = 0 // Ancho flexible
        params.height = GridLayout.LayoutParams.WRAP_CONTENT // Alto adaptable
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // Ocupa toda la fila
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // Ocupa toda la columna
        categoryView.layoutParams = params

        // Añadir la vista al GridLayout principal
        gridLayoutCategories.addView(categoryView)
    }
}
