package dam.clases.monje_financiero_app.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
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
    private lateinit var colorContainer: LinearLayout // Cambiado a LinearLayout
    private lateinit var categoriesService: CategoriesService
    private var userId: String? = null
    private var selectedColor: String = "#FF5733" // Color por defecto
    private var selectedIconText: String = "🍔" // Ícono por defecto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        etCategoryName = findViewById(R.id.etCategoryName)
        btnAddCategory = findViewById(R.id.btnAddCategory)
        gridIcons = findViewById(R.id.gridIcons)
        colorContainer = findViewById(R.id.colorContainer) // Inicializa el LinearLayout

        // Obtener el userId de SharedPreferences
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        categoriesService = CategoriesService(this)

        // Configurar el botón para agregar categoría
        btnAddCategory.setOnClickListener {
            addCategory()
        }

        // Configurar iconos y colores
        setupIconSelection()
        setupColorSelection()

        // Agregar el botón de regresar
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish() // Regresar a la actividad anterior
        }


        // Configurar el BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.navigation_home
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            val id = item.itemId
            if (id == R.id.navigation_home) {
                startActivity(Intent(this@CategoriesActivity, HomeActivity::class.java))
                return@OnNavigationItemSelectedListener true
            } else if (id == R.id.navigation_expenses) {
                startActivity(Intent(this@CategoriesActivity, ExpensesActivity::class.java))
                return@OnNavigationItemSelectedListener true
            } else if (id == R.id.navigation_budgets) {
                startActivity(Intent(this@CategoriesActivity, BudgetsActivity::class.java))
                return@OnNavigationItemSelectedListener true
            } else if (id == R.id.navigation_reports) {
                startActivity(Intent(this@CategoriesActivity, ReportsActivity::class.java))
                return@OnNavigationItemSelectedListener true
            } else if (id == R.id.navigation_settings) {
                startActivity(Intent(this@CategoriesActivity, SettingsActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            false
        })
    }

    private fun addCategory() {
        val categoryName = etCategoryName.text.toString().trim()

        // Validar que el nombre de la categoría no esté vacío
        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Ingresa el nombre de la categoría", Toast.LENGTH_SHORT).show()
            return
        }

        // Llamar al servicio para agregar la categoría
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
                setBackgroundResource(R.drawable.icon_selector) // Fondo circular para el ícono
                setOnClickListener {
                    selectedIconText = emoji // Guardar el ícono seleccionado
                    updateIconSelection(this) // Resaltar el ícono seleccionado
                    Toast.makeText(this@CategoriesActivity, "Ícono seleccionado: $selectedIconText", Toast.LENGTH_SHORT).show()
                }
            }
            gridIcons.addView(emojiView)
        }

        // Hacer que los íconos aprovechen todo el espacio lateral
        gridIcons.post {
            adjustGridItemSize(gridIcons)
        }
    }

    private fun updateIconSelection(selectedView: TextView) {
        for (i in 0 until gridIcons.childCount) {
            val icon = gridIcons.getChildAt(i) as TextView
            if (icon == selectedView) {
                icon.setBackgroundColor(Color.LTGRAY) // Cambia el color de fondo del ícono seleccionado
            } else {
                icon.setBackgroundColor(Color.TRANSPARENT) // Restablece el color de fondo para los demás íconos
            }
        }
    }

    private fun setupColorSelection() {
        val colors = arrayOf(
            "#FF5733", "#FF8D1F", "#FFC300", "#DAF7A6", "#33FF57",
            "#57FF33", "#33FFB2", "#33B2FF", "#3357FF", "#5733FF",
            "#FF33F6", "#FF3333", "#8D33FF", "#FF33A8", "#FF1F57",
            "#FAD6A5", "#B9F6CA", "#E1BEE7", "#C5CAE9", "#FFCDD2"
        )

        val colorContainer = findViewById<LinearLayout>(R.id.colorContainer)

        for (color in colors) {
            val colorFrame = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(100, 100)
                setPadding(8, 8, 8, 8) // Espaciado
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
                visibility = View.GONE // Inicialmente invisible
            }

            colorFrame.addView(colorView)
            colorFrame.addView(borderView)

            colorFrame.setOnClickListener {
                selectedColor = color
                updateColorSelection(borderView)

                // Crear el Toast
                val toast = Toast.makeText(this@CategoriesActivity, "Color seleccionado: $selectedColor", Toast.LENGTH_SHORT)

                // Mostrar el Toast
                toast.show()

                // Cancelar el Toast después de 500 ms (ajusta el tiempo según desees)
                Handler(Looper.getMainLooper()).postDelayed({
                    toast.cancel()
                }, 1500) // Duración personalizada
            }

            colorContainer.addView(colorFrame)
        }
    }


    private fun updateColorSelection(selectedBorder: View) {
        val colorContainer = findViewById<LinearLayout>(R.id.colorContainer)
        for (i in 0 until colorContainer.childCount) {
            val colorFrame = colorContainer.getChildAt(i) as FrameLayout
            val borderView = colorFrame.getChildAt(1) // El borderView

            if (borderView == selectedBorder) {
                borderView.visibility = View.VISIBLE // Muestra el marco para el color seleccionado
            } else {
                borderView.visibility = View.GONE // Oculta el marco para los demás
            }
        }
    }

    // Ajusta el tamaño de los elementos de LinearLayout y GridLayout para que ocupen todo el espacio disponible
    private fun adjustGridItemSize(layout: ViewGroup) {
        val totalWidth = layout.width

        for (i in 0 until layout.childCount) {
            val view = layout.getChildAt(i)
            val params = view.layoutParams

            // Ajusta el tamaño de los elementos para LinearLayout y GridLayout
            if (params is LinearLayout.LayoutParams || params is GridLayout.LayoutParams) {
                params.width = totalWidth / layout.childCount - 16 // Ajuste con márgenes
                params.height = 100 // Altura fija
                view.layoutParams = params
            }
        }
    }
}
