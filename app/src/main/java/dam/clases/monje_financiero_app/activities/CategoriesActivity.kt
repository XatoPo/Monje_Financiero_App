package dam.clases.monje_financiero_app.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.services.CategoriesService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class CategoriesActivity : AppCompatActivity() {
    private lateinit var etCategoryName: TextInputEditText
    private lateinit var btnAddCategory: Button
    private lateinit var gridIcons: GridLayout
    private lateinit var colorContainer: LinearLayout
    private lateinit var categoriesService: CategoriesService
    private var userId: String? = null
    private var selectedColor: String? = null
    private var selectedIconText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        etCategoryName = findViewById(R.id.etCategoryName)
        btnAddCategory = findViewById(R.id.btnAddCategory)
        gridIcons = findViewById(R.id.gridIcons)
        colorContainer = findViewById(R.id.colorContainer)

        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        categoriesService = CategoriesService(this)

        btnAddCategory.setOnClickListener {
            validateAndAddCategory()
        }

        setupIconSelection()
        setupColorSelection()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        configureBottomNavigation()
    }

    private fun validateAndAddCategory() {
        val categoryName = etCategoryName.text.toString().trim()

        // Limpiar errores previos
        etCategoryName.error = null

        when {
            categoryName.isEmpty() -> {
                etCategoryName.error = "El nombre no puede estar vacío"
            }
            selectedColor == null -> {
                Toast.makeText(this, "Selecciona un color", Toast.LENGTH_SHORT).show()
            }
            selectedIconText == null -> {
                Toast.makeText(this, "Selecciona un ícono", Toast.LENGTH_SHORT).show()
            }
            else -> {
                addCategory(categoryName)
            }
        }
    }

    private fun addCategory(categoryName: String) {
        // Obtener el userId de SharedPreferences
        val sharedPreferences = getSharedPreferences("MonjeFinancieroPrefs", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)
        if (userId == null) {
            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
            return
        }
        categoriesService.addCategory(userId!!, categoryName, selectedColor!!, selectedIconText!!, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CategoriesActivity, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@CategoriesActivity, "Categoría agregada", Toast.LENGTH_SHORT).show()
                        etCategoryName.setText("")
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CategoriesActivity, "Error al agregar categoría", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
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
                    val toast = Toast.makeText(this@CategoriesActivity, "Ícono seleccionado: $selectedIconText", Toast.LENGTH_SHORT)
                    toast.show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        toast.cancel()
                    }, 1500) // Duración personalizada
                }
            }
            gridIcons.addView(emojiView)
        }

        gridIcons.post {
            adjustGridItemSize(gridIcons)
        }
    }

    private fun updateIconSelection(selectedView: TextView) {
        for (i in 0 until gridIcons.childCount) {
            val icon = gridIcons.getChildAt(i) as TextView
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

                val toast = Toast.makeText(this@CategoriesActivity, "Color seleccionado: $selectedColor", Toast.LENGTH_SHORT)
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
        val totalWidth = gridLayout.width
        val columnCount = gridLayout.columnCount
        val itemSize = totalWidth / columnCount

        for (i in 0 until gridLayout.childCount) {
            val view = gridLayout.getChildAt(i)
            val params = view.layoutParams as GridLayout.LayoutParams
            params.width = itemSize - 16
            params.height = itemSize - 16
            view.layoutParams = params
            view.requestLayout()
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