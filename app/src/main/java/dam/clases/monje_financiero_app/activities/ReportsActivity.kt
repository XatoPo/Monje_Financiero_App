package dam.clases.monje_financiero_app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.services.ApiService
import dam.clases.monje_financiero_app.services.CategoriesService
import dam.clases.monje_financiero_app.services.ExpensesService
import dam.clases.monje_financiero_app.services.ReportsService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.util.ArrayList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportsActivity : AppCompatActivity() {

    private lateinit var calendarStartDate: CalendarView
    private lateinit var calendarEndDate: CalendarView
    private lateinit var btnGenerateReport: Button
    private lateinit var barChart: BarChart
    private lateinit var reportsService: ReportsService
    private lateinit var categoriesService: CategoriesService
    private lateinit var expensesService: ExpensesService
    private lateinit var loadingDialog: ALoadingDialog
    private var startDate: String? = null
    private var endDate: String? = null
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        // Inicialización de componentes
        calendarStartDate = findViewById(R.id.calendarStartDate)
        calendarEndDate = findViewById(R.id.calendarEndDate)
        btnGenerateReport = findViewById(R.id.btnGenerateReport)
        barChart = findViewById(R.id.barChart)

        // Inicialización de los servicios
        reportsService = ReportsService()
        categoriesService = CategoriesService(this)
        expensesService = ExpensesService(this)

        // Obtener el userId de SharedPreferences
        userId = ApiService.getUserId(this)

        // Configurar la selección de fechas
        configureDateSelection()

        // Configurar el botón de generar reporte
        btnGenerateReport.setOnClickListener {
            if (startDate != null && endDate != null) {
                generateReport()
            } else {
                Toast.makeText(this@ReportsActivity, "Por favor, selecciona las fechas", Toast.LENGTH_SHORT).show()
            }
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

    // Método para configurar la selección de fechas
    private fun configureDateSelection() {
        calendarStartDate.setOnDateChangeListener { _, year, month, dayOfMonth ->
            startDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        }

        calendarEndDate.setOnDateChangeListener { _, year, month, dayOfMonth ->
            endDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        }
    }

    // Método para generar el reporte
    private fun generateReport() {
        // Mostrar el ProgressBar mientras se cargan los datos
        showLoadingDialog()

        // Primero obtenemos las categorías
        categoriesService.getAllCategories(userId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ReportsActivity, "Error al obtener las categorías", Toast.LENGTH_SHORT).show()
                    loadingDialog.dismiss()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val categoriesResponse = response.body?.string() ?: ""
                val categories = processCategoriesResponse(categoriesResponse)

                // Luego obtenemos los gastos
                expensesService.getAllExpenses(userId, object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(this@ReportsActivity, "Error al obtener los gastos", Toast.LENGTH_SHORT).show()
                            loadingDialog.dismiss()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val expensesResponse = response.body?.string() ?: ""
                        processExpensesForCategory(expensesResponse, categories)
                    }
                })
            }
        })
    }

    // Procesa las categorías desde la respuesta JSON
    private fun processCategoriesResponse(responseString: String): List<Category> {
        val categories = mutableListOf<Category>()
        try {
            val jsonArray = JSONArray(responseString)

            if (jsonArray.length() > 0) {
                val expensesArray = jsonArray.getJSONArray(0)
                for (i in 0 until expensesArray.length()) {
                    val categoryObject = expensesArray.getJSONObject(i)
                    val category = Category(
                        id = categoryObject.getString("id"),
                        name = categoryObject.getString("name"),
                        emoji = categoryObject.getString("icon_text")
                    )
                    categories.add(category)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(applicationContext, "No hay categorías disponibles.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(applicationContext, "Error al cargar las categorías.", Toast.LENGTH_SHORT).show()
            }
        }
        return categories
    }

    // Procesa los gastos y asigna el total por categoría
    private fun processExpensesForCategory(responseString: String, categories: List<Category>) {
        try {
            val jsonArray = JSONArray(responseString)
            val categoryExpenseMap = mutableMapOf<String, Double>()

            // Inicializamos las categorías con 0 en los gastos
            categories.forEach {
                categoryExpenseMap[it.id] = 0.0
            }

            // Convertir las fechas de inicio y fin a objetos Date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDateObj = startDate?.let { dateFormat.parse(it) }
            val endDateObj = endDate?.let { dateFormat.parse(it) }

            if (jsonArray.length() > 0) {
                val expensesArray = jsonArray.getJSONArray(0)
                for (i in 0 until expensesArray.length()) {
                    val expenseObject = expensesArray.getJSONObject(i)

                    // Obtener la fecha del gasto y convertirla a un objeto Date
                    val expenseDateString = expenseObject.getString("date")
                    val expenseDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(expenseDateString)

                    // Verificar si la fecha del gasto está dentro del rango seleccionado
                    if (expenseDate != null &&
                        (startDateObj == null || !expenseDate.before(startDateObj)) &&
                        (endDateObj == null || !expenseDate.after(endDateObj))) {

                        val amount = expenseObject.getString("amount").toDoubleOrNull() ?: 0.0
                        val categoryId = expenseObject.getString("category_id")
                        categoryExpenseMap[categoryId] = categoryExpenseMap.getOrDefault(categoryId, 0.0) + amount
                    }
                }
            }

            // Creamos la lista de categorías con los gastos totales
            val categoriesWithExpenses = categories.map { category ->
                CategoryWithExpense(
                    category = category,
                    totalExpenses = categoryExpenseMap[category.id] ?: 0.0
                )
            }

            // Actualizamos la UI con el gráfico
            runOnUiThread {
                updateUICategory(categoriesWithExpenses)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this@ReportsActivity, "Error al procesar los gastos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Actualiza la UI con el gráfico de barras
    private fun updateUICategory(categoriesWithExpenses: List<CategoryWithExpense>) {
        // Limitar las categorías a las 10 más relevantes (con mayores gastos)
        val topCategories = categoriesWithExpenses.sortedByDescending { it.totalExpenses }.take(10)

        val entries = ArrayList<BarEntry>()
        val categoryNames = ArrayList<String>()

        topCategories.forEachIndexed { index, categoryWithExpense ->
            val category = categoryWithExpense.category
            val amount = categoryWithExpense.totalExpenses

            entries.add(BarEntry(index.toFloat(), amount.toFloat()))
            categoryNames.add(category.name)
        }

        // Mostrar el gráfico
        showBarChart(entries, categoryNames)

        // Ocultar el ProgressBar una vez que los datos estén cargados
        loadingDialog.dismiss()
    }

    // Muestra el gráfico de barras
    private fun showBarChart(entries: List<BarEntry>, categoryNames: List<String>) {
        val dataSet = BarDataSet(entries, "Gastos por Categoría")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        barChart.data = barData
        barChart.setFitBars(true)
        barChart.invalidate()

        // Configuración del eje X
        val xAxis = barChart.xAxis
        xAxis.granularity = 1f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setLabelCount(categoryNames.size)

        // Rotar las etiquetas del eje X para evitar que se solapen
        xAxis.labelRotationAngle = -45f

        xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index in categoryNames.indices) {
                    categoryNames[index]
                } else {
                    ""
                }
            }
        }

        // Configuración del eje Y
        val yAxisLeft = barChart.axisLeft
        yAxisLeft.granularity = 1f

        val yAxisRight = barChart.axisRight
        yAxisRight.isEnabled = false
    }

    // Modelos de datos
    data class CategoryWithExpense(
        val category: Category,
        val totalExpenses: Double
    )

    data class Category(
        val id: String,
        val name: String,
        val emoji: String // Agregamos el campo emoji
    )

    private fun configureBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.navigation_reports
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
                R.id.navigation_reports -> false
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}