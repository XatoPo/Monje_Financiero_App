package dam.clases.monje_financiero_app.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.models.Category
import dam.clases.monje_financiero_app.services.CategoriesService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class ManageCategoriesActivity : AppCompatActivity() {
    private lateinit var categoriesService: CategoriesService
    private var userId: String? = null
    private lateinit var loadingDialog: ALoadingDialog
    private lateinit var manageCategoriesAdapter: ManageCategoriesAdapter
    private lateinit var recyclerView: RecyclerView
    private var selectedColor: String? = null
    private var selectedIconText: String? = null
    private lateinit var gridIcons2: GridLayout
    private lateinit var colorContainer: LinearLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)

        categoriesService = CategoriesService(this)

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_category, null)
        gridIcons2 = dialogView.findViewById(R.id.gridIcons2)
        colorContainer = dialogView.findViewById(R.id.colorContainer)

        // Configuración de la lista de categorías y la navegación
        recyclerView = findViewById(R.id.rvCategories)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar las categorías
        val sharedPreferences = getSharedPreferences("MonjeFinancieroPrefs", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)
        if (userId != null) {
            showLoadingDialog()
            loadCategories(userId!!)
        } else {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        configureBottomNavigation()
    }

    private fun showLoadingDialog() {
        loadingDialog = ALoadingDialog(this) // Inicializar el diálogo de carga
        loadingDialog.show() // Mostrar el diálogo
    }

    private fun loadCategories(userId: String) {
        categoriesService.getAllCategories(userId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    loadingDialog.dismiss() // Ocultar el diálogo de carga
                    Toast.makeText(this@ManageCategoriesActivity, "Error al cargar categorías", Toast.LENGTH_SHORT).show()
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
                                loadingDialog.dismiss() // Ocultar el diálogo de carga
                                Toast.makeText(this@ManageCategoriesActivity, "No se encontraron categorías", Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        val categoryList = mutableListOf<Category>()
                        for (i in 0 until categoriesJsonArray.length()) {
                            val categoryJson = categoriesJsonArray.getJSONObject(i)
                            val category = Category(
                                id = categoryJson.getString("id"),
                                userId = categoryJson.getString("user_id"),
                                name = categoryJson.getString("name"),
                                color = categoryJson.getString("color"),
                                iconText = categoryJson.getString("icon_text")
                            )
                            categoryList.add(category)
                        }

                        runOnUiThread {
                            manageCategoriesAdapter = ManageCategoriesAdapter(
                                categoryList,
                                onEditClick = { category ->
                                    showEditCategoryDialog(category)
                                },
                                onDeleteClick = { category ->
                                    deleteCategory(category.id)
                                }
                            )
                            recyclerView.adapter = manageCategoriesAdapter
                            loadingDialog.dismiss() // Ocultar el diálogo de carga
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        runOnUiThread {
                            loadingDialog.dismiss() // Ocultar el diálogo de carga
                            Toast.makeText(this@ManageCategoriesActivity, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        loadingDialog.dismiss() // Ocultar el diálogo de carga
                        Toast.makeText(this@ManageCategoriesActivity, "Error al obtener categorías del servidor", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun deleteCategory(categoryId: String) {
        categoriesService.deleteCategory(categoryId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ManageCategoriesActivity, "Error al eliminar la categoría", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@ManageCategoriesActivity, "Categoría eliminada exitosamente", Toast.LENGTH_SHORT).show()
                        loadCategories(userId!!) // Recargar categorías después de la eliminación
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ManageCategoriesActivity, "Error al eliminar la categoría", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun showEditCategoryDialog(category: Category) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_edit_category, null)

        // Inicializar componentes de la vista
        val nameEditText = dialogView.findViewById<TextInputEditText>(R.id.etCategoryManageName)
        nameEditText.setText(category.name)

        // Configurar íconos y colores si es necesario
        gridIcons2 = dialogView.findViewById(R.id.gridIcons2)
        colorContainer = dialogView.findViewById(R.id.colorContainer)
        setupIconSelection()
        setupColorSelection()

        builder.setView(dialogView)
            .setPositiveButton("Actualizar") { _, _ ->
                val updatedName = nameEditText.text.toString()
                val updatedColor = selectedColor
                val updatedIconText = selectedIconText

                categoriesService.updateCategory(
                    category.id,
                    updatedName,
                    updatedColor,
                    updatedIconText,
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            runOnUiThread {
                                Toast.makeText(this@ManageCategoriesActivity, "Error al actualizar la categoría", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful) {
                                runOnUiThread {
                                    Toast.makeText(this@ManageCategoriesActivity, "Categoría actualizada exitosamente", Toast.LENGTH_SHORT).show()
                                    loadCategories(userId!!)
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@ManageCategoriesActivity, "Error al actualizar la categoría", Toast.LENGTH_SHORT).show()
                                    Log.e("AAA", response.toString())
                                }
                            }
                        }
                    }
                )
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    private fun setupIconSelection() {
        val emojis = arrayOf("🍔", "🍕", "🍣", "🍩", "🍦", "🍇", "🌟",
            "💰", "🛒", "🏡", "📚", "🎉", "🚗", "⚽",
            "📈", "📅", "💼", "🎮", "🎤", "🎨", "🎁")

        for (emoji in emojis) {
            val emojiView = TextView(this).apply {
                text = emoji
                textSize = 24f
                setPadding(16, 16, 16, 16)
                setBackgroundResource(R.drawable.icon_selector)
                setOnClickListener {
                    selectedIconText = emoji
                    updateIconSelection(this)
                    val toast = Toast.makeText(this@ManageCategoriesActivity, "Ícono seleccionado: $selectedIconText", Toast.LENGTH_SHORT)
                    toast.show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        toast.cancel()
                    }, 1500) // Duración personalizada
                }
            }
            gridIcons2.addView(emojiView)
        }

        gridIcons2.post {
            adjustGridItemSize(gridIcons2)
        }
    }

    private fun updateIconSelection(selectedView: TextView) {
        for (i in 0 until gridIcons2.childCount) {
            val icon = gridIcons2.getChildAt(i) as TextView
            icon.setBackgroundColor(if (icon == selectedView) Color.LTGRAY else Color.TRANSPARENT)
        }
    }

    private fun setupColorSelection() {
        val colors = arrayOf(
            "#FF5733", "#FF8D1F", "#FFC300", "#DAF7A6", "#33FF57",
            "#57FF33", "#33FFB2", "#33B2FF", "#3357FF", "#5733FF",
            "#FF33F6", "#FF3333", "#8D33FF", "#FF33A8", "#FF1F57",
            "#FAD6A5", "#B9F6CA", "#E1BEE7", "#C5CAE9", "#FFCDD2"
        )

        for (color in colors) {
            val colorFrame = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(100, 100)
                setPadding(8, 8, 8, 8)
            }

            val colorView = View(this).apply {
                setBackgroundColor(Color.parseColor(color))
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            val borderView = View(this).apply {
                background = resources.getDrawable(R.drawable.color_selected_background, null)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                visibility = View.GONE
            }

            colorFrame.addView(colorView)
            colorFrame.addView(borderView)

            colorFrame.setOnClickListener {
                selectedColor = color
                updateColorSelection(borderView)

                val toast = Toast.makeText(this@ManageCategoriesActivity, "Color seleccionado: $selectedColor", Toast.LENGTH_SHORT)
                toast.show()

                Handler(Looper.getMainLooper()).postDelayed({
                    toast.cancel()
                }, 1500) // Duración personalizada
            }

            colorContainer.addView(colorFrame)
        }
    }

    private fun updateColorSelection(selectedBorder: View) {
        for (i in 0 until colorContainer.childCount) {
            val colorFrame = colorContainer.getChildAt(i) as FrameLayout
            val borderView = colorFrame.getChildAt(1)
            borderView.visibility = if (borderView == selectedBorder) View.VISIBLE else View.GONE
        }
    }

    private fun adjustGridItemSize(gridLayout: GridLayout) {
        gridLayout.post {
            val totalWidth = gridLayout.width
            val columnCount = gridLayout.columnCount
            val itemSize = totalWidth / columnCount

            for (i in 0 until gridLayout.childCount) {
                val view = gridLayout.getChildAt(i)
                val params = view.layoutParams as GridLayout.LayoutParams
                params.width = itemSize - 8 // Ajusta el tamaño según sea necesario
                params.height = itemSize - 8
                view.layoutParams = params
                view.requestLayout()
            }
        }
    }

    private fun configureBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.navigation_home
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
}