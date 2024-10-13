package dam.clases.monje_financiero_app.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.models.Category
import dam.clases.monje_financiero_app.services.CategoriesService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class CategorySelectionActivity : AppCompatActivity(), CategoryAdapter.CategoryClickListener {
    private lateinit var btnClose: Button
    private lateinit var recyclerViewCategories: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoriesService: CategoriesService
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_category_selection)

        recyclerViewCategories = findViewById(R.id.recyclerViewCategories)
        btnClose = findViewById(R.id.btnClose)

        categoryAdapter = CategoryAdapter(this, this)
        recyclerViewCategories.adapter = categoryAdapter

        // Usar GridLayoutManager con 2 columnas
        val layoutManager = GridLayoutManager(this, 2)
        recyclerViewCategories.layoutManager = layoutManager

        categoriesService = CategoriesService(this)

        // Obtener el userId de SharedPreferences
        val sharedPreferences = getSharedPreferences("MonjeFinancieroPrefs", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        // Cargar las categorías desde la base de datos
        if (userId != null) {
            loadCategories(userId!!)
        } else {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }

        // Configurar el botón de cerrar
        btnClose.setOnClickListener {
            finish() // Cerrar la actividad
        }
    }

    private fun loadCategories(userId: String) {
        categoriesService.getAllCategories(userId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CategorySelectionActivity, "Error al cargar categorías", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    try {
                        val jsonArray = JSONArray(responseBody)
                        val categoriesJsonArray = jsonArray.getJSONArray(0)

                        if (categoriesJsonArray.length() == 0) {
                            runOnUiThread {
                                Toast.makeText(this@CategorySelectionActivity, "No se encontraron categorías", Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

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
                                categoryAdapter.addCategory(category)
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this@CategorySelectionActivity, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CategorySelectionActivity, "Error al obtener categorías del servidor", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    override fun onCategoryClicked(category: Category) {
        val resultIntent = Intent().apply {
            putExtra("selectedCategory", category) // Pasar la categoría como Parcelable
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish() // Cerrar la actividad y devolver la categoría seleccionada
    }
}