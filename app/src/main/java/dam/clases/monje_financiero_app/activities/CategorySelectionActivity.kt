package dam.clases.monje_financiero_app.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
import java.io.IOException

class CategorySelectionActivity : AppCompatActivity() {
    private lateinit var linearLayoutCategories: LinearLayout
    private lateinit var btnClose: Button
    private lateinit var categoriesService: CategoriesService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_category_selection) // Asegúrate de que el layout tenga este nombre

        linearLayoutCategories = findViewById(R.id.linearLayoutCategories)
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
                    val categoriesJsonArray = JSONArray(responseBody)

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
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CategorySelectionActivity, "Error al obtener categorías", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun addCategoryToView(category: Category) {
        val categoryTextView = TextView(this).apply {
            text = category.name
            textSize = 18f
            setPadding(8, 8, 8, 8)
            setOnClickListener {
                val resultIntent = Intent().apply {
                    putExtra("selectedCategory", category) // Pasar la categoría como Parcelable
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish() // Cerrar el diálogo
            }
        }
        linearLayoutCategories.addView(categoryTextView)
    }
}