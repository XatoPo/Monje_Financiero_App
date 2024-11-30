package dam.clases.monje_financiero_app.activities

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.services.UsersService
import dam.clases.monje_financiero_app.services.BudgetsService
import dam.clases.monje_financiero_app.services.CategoriesService
import dam.clases.monje_financiero_app.services.ExpensesService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvTotalGastos: TextView
    private lateinit var tvTotalPresupuestos: TextView
    private lateinit var progressBudgetComparison: ProgressBar
    private lateinit var btnProfile: ImageButton
    private lateinit var usersService: UsersService
    private lateinit var budgetsService: BudgetsService
    private lateinit var expensesService: ExpensesService

    private var userId: String? = null
    private lateinit var loadingDialog: ALoadingDialog

    private var totalBudgets = 0.0 // Suma de presupuestos
    private var totalExpenses = 0.0 // Suma de gastos

    private lateinit var rvCategories: RecyclerView
    private lateinit var tvNoData: TextView
    private lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var categoriesService: CategoriesService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLoadingDialog()
        setContentView(R.layout.activity_home)
        rvCategories = findViewById(R.id.rvCategories)

        // Inicializar el adaptador con una lista vacía al principio
        categoriesAdapter = CategoriesAdapter(mutableListOf())
        rvCategories.adapter = categoriesAdapter // Ahora el adaptador está inicializado

        // Inicializar servicios y variables
        usersService = UsersService(this)
        budgetsService = BudgetsService(this)
        expensesService = ExpensesService(this)
        categoriesService = CategoriesService(this)
        rvCategories.layoutManager = LinearLayoutManager(this)

        // Inicializar el TextView y botón de perfil
        tvWelcome = findViewById(R.id.tvWelcome)
        btnProfile = findViewById(R.id.btnProfile)
        tvTotalPresupuestos = findViewById(R.id.tvTotalPresupuestos)
        tvTotalGastos = findViewById(R.id.tvTotalGastos)
        progressBudgetComparison = findViewById(R.id.progressBudgetComparison)
        tvNoData = findViewById(R.id.tvNoData)

        // Obtener el userId de SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("MonjeFinancieroPrefs", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        userId?.let {
            loadUserData(it)
            fetchData(it)
            // Llamamos al método para obtener y procesar los gastos por categoría
            fetchAndProcessExpensesCategory(it) { isSuccess ->
                if (isSuccess) {
                    // El proceso fue exitoso
                    Toast.makeText(this, "Gastos y categorías procesados correctamente.", Toast.LENGTH_SHORT).show()
                } else {
                    // Hubo un error
                    Toast.makeText(this, "Hubo un error al procesar los datos.", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }

        // Configurar el botón de perfil
        btnProfile.setOnClickListener { showProfileMenu() }

        configureBottomNavigation()
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

    /**
     * Llamar a servicios de presupuestos y gastos, y procesar los datos
     */
    private fun fetchData(userId: String) {
        // Llamar al servicio de presupuestos
        fetchAndProcessBudgets(userId) { budgetsSuccess ->
            if (budgetsSuccess) {
                println("Total de Presupuestos: $totalBudgets")
            } else {
                Toast.makeText(this, "Error al cargar presupuestos", Toast.LENGTH_SHORT).show()
            }
            // Asegurarse de que los datos de presupuestos se procesaron antes de calcular el progreso
            calculateProgressBar()
        }

        // Llamar al servicio de gastos
        fetchAndProcessExpenses(userId) { expensesSuccess ->
            if (expensesSuccess) {
                println("Total de Gastos: $totalExpenses")
            } else {
                Toast.makeText(this, "Error al cargar gastos", Toast.LENGTH_SHORT).show()
            }
            // Asegurarse de que los datos de gastos se procesaron antes de calcular el progreso
            calculateProgressBar()
        }
    }

    /**
     * Llamar al servicio para obtener presupuestos
     */
    private fun fetchAndProcessBudgets(userId: String, onResult: (Boolean) -> Unit) {
        budgetsService.getAllBudgets(userId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread { onResult(false) }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseString = response.body?.string() ?: ""
                    processBudgetsResponse(responseString)
                    runOnUiThread { onResult(true) }
                } else {
                    runOnUiThread { onResult(false) }
                }
            }
        })
    }

    /**
     * Llamar al servicio para obtener gastos
     */
    private fun fetchAndProcessExpenses(userId: String, onResult: (Boolean) -> Unit) {
        expensesService.getAllExpenses(userId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread { onResult(false) }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseString = response.body?.string() ?: ""
                    processExpensesResponse(responseString)
                    runOnUiThread { onResult(true) }
                } else {
                    runOnUiThread { onResult(false) }
                }
            }
        })
    }

    /**
     * Procesar respuesta de presupuestos
     */
    private fun processBudgetsResponse(responseString: String) {
        try {
            val jsonArray = JSONArray(responseString)

            if (jsonArray.length() > 0) {
                val budgetsArray = jsonArray.getJSONArray(0)
                totalBudgets = 0.0
                for (i in 0 until budgetsArray.length()) {
                    val budgetObject = budgetsArray.getJSONObject(i)
                    val budgetLimit = budgetObject.getString("budget_limit").toDouble()
                    totalBudgets += budgetLimit
                }
            }

            Log.i("HomeActivity", "Total de Presupuestos: $totalBudgets")

            runOnUiThread {
                updateBudgetsUI()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            totalBudgets = 0.0
            runOnUiThread {
                tvTotalPresupuestos.text = "Error al procesar presupuestos."
            }
        }
    }

    /**
     * Procesar respuesta de gastos
     */
    private fun processExpensesResponse(responseString: String) {
        try {
            val jsonArray = JSONArray(responseString)

            if (jsonArray.length() > 0) {
                val expensesArray = jsonArray.getJSONArray(0)
                totalExpenses = 0.0
                for (i in 0 until expensesArray.length()) {
                    val expenseObject = expensesArray.getJSONObject(i)
                    val amount = expenseObject.getString("amount").toDouble()
                    totalExpenses += amount
                }
            }

            Log.i("HomeActivity", "Total de Gastos: $totalExpenses")

            runOnUiThread {
                updateExpensesUI()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            totalExpenses = 0.0
            runOnUiThread {
                tvTotalGastos.text = "Error al procesar gastos."
            }
        }
    }

    /**
     * Actualizar la UI de presupuestos
     */
    private fun updateBudgetsUI() {
        tvTotalPresupuestos.text = String.format("Total Presupuestos: S/. %.2f", totalBudgets)
    }

    /**
     * Actualizar la UI de gastos
     */
    private fun updateExpensesUI() {
        tvTotalGastos.text = String.format("Total Gastos: S/. %.2f", totalExpenses)
    }

    /**
     * Calcular y actualizar la barra de progreso
     */
    private fun calculateProgressBar() {
        // Asegurarse de que ambos valores estén listos antes de calcular el progreso
        if (totalBudgets > 0) {
            val progress = if (totalExpenses > 0) {
                (totalExpenses / totalBudgets * 100).toInt() // Cambié la fórmula a gastos/presupuestos
            } else {
                0
            }

            // Establecer el progreso de la barra
            progressBudgetComparison.progress = progress

            // Cambiar el color de la barra dependiendo de si los gastos superan el presupuesto
            if (totalExpenses > totalBudgets) {
                // Exceder presupuesto, color rojo
                progressBudgetComparison.progressDrawable.setColorFilter(
                    ContextCompat.getColor(this, R.color.progress_bar_over_budget),
                    PorterDuff.Mode.SRC_IN
                )
            } else if (progress >= 80) {
                // Si el progreso es mayor o igual al 80%, advertir (naranja)
                progressBudgetComparison.progressDrawable.setColorFilter(
                    ContextCompat.getColor(this, R.color.progress_bar_warning),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                // Si está dentro del presupuesto, verde
                progressBudgetComparison.progressDrawable.setColorFilter(
                    ContextCompat.getColor(this, R.color.progress_bar_normal),
                    PorterDuff.Mode.SRC_IN
                )
            }
        } else {
            // Si no hay presupuesto, el progreso es 0 y no se muestra ningún color
            progressBudgetComparison.progress = 0
            progressBudgetComparison.progressDrawable.setColorFilter(
                ContextCompat.getColor(this, R.color.progress_bar_normal),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun configureBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.navigation_home
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> false
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

    private fun configureQuickActions() {
        val btnRegisterExpense: Button = findViewById(R.id.btnRegisterExpense)
        val btnViewBudgets: Button = findViewById(R.id.btnViewBudgets)
        val btnGenerateReport: Button = findViewById(R.id.btnGenerateReport)
        val btnCreateCategories: Button = findViewById(R.id.btnCreateCategories)
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
        btnCreateCategories.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }
        btnManageCategories.setOnClickListener {
            startActivity(Intent(this, ManageCategoriesActivity::class.java))
        }
    }

    private fun processExpensesForCategory(responseString: String, categories: List<Category>) {
        try {
            val jsonArray = JSONArray(responseString)
            val categoryExpenseMap = mutableMapOf<String, Double>()

            categories.forEach {
                categoryExpenseMap[it.id] = 0.0
            }

            if (jsonArray.length() > 0) {
                val expensesArray = jsonArray.getJSONArray(0)
                for (i in 0 until expensesArray.length()) {
                    val expenseObject = expensesArray.getJSONObject(i)
                    val amount = expenseObject.getString("amount").toDoubleOrNull() ?: 0.0
                    val categoryId = expenseObject.getString("category_id")

                    categoryExpenseMap[categoryId] = categoryExpenseMap.getOrDefault(categoryId, 0.0) + amount
                }
            }

            // Creamos la lista de categorías con los gastos totales
            val categoriesWithExpenses = categories.map { category ->
                CategoryWithExpense(
                    category = category,
                    totalExpenses = categoryExpenseMap[category.id] ?: 0.0
                )
            }

            // Actualizamos la interfaz de usuario
            runOnUiThread {
                updateUICategory(categoriesWithExpenses)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
            runOnUiThread {
                tvTotalGastos.text = "Error al procesar gastos."
            }
        }
    }

    private fun processCategoriesResponse(responseString: String): List<Category> {
        val categories = mutableListOf<Category>()
        try {
            val jsonArray = JSONArray(responseString)

            if (jsonArray.length() > 0) {
                val expensesArray = jsonArray.getJSONArray(0)
                for (i in 0 until expensesArray.length()) {
                    val categoryObject = expensesArray.getJSONObject(i)

                    // Creamos la categoría a partir del objeto JSON
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

    private fun fetchAndProcessExpensesCategory(userId: String, onResult: (Boolean) -> Unit) {
        categoriesService.getAllCategories(userId, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread { onResult(false) }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseString = response.body?.string() ?: ""
                    val categories = processCategoriesResponse(responseString)

                    // Ahora realizamos la solicitud de los gastos
                    expensesService.getAllExpenses(userId, object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                            runOnUiThread { onResult(false) }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful) {
                                val responseString = response.body?.string() ?: ""
                                processExpensesForCategory(responseString, categories)

                                // Llamamos a onResult(true) cuando ambas solicitudes fueron exitosas
                                runOnUiThread { onResult(true) }
                            } else {
                                runOnUiThread { onResult(false) }
                            }
                        }
                    })
                } else {
                    runOnUiThread { onResult(false) }
                }
            }
        })
    }

    private fun updateUICategory(categoriesWithExpenses: List<CategoryWithExpense>) {
        Log.d("HomeActivity", "Actualizando categorías, total: ${categoriesWithExpenses.size}")

        // Asegúrate de que el adaptador tiene los datos actualizados
        categoriesAdapter.updateCategories(categoriesWithExpenses)

        // Si tienes otros elementos de la UI que necesitas actualizar, hazlo aquí
        val totalExpenses = categoriesWithExpenses.sumOf { it.totalExpenses }
        Log.d("HomeActivity", "Total de gastos: $totalExpenses")

        // Mostrar el total de gastos
        tvTotalGastos.text = "Total Gastos: S/. ${"%.2f".format(totalExpenses)}"

        // Validación: Si no hay gastos, mostrar mensaje de error
        if (totalExpenses == 0.0) {
            tvNoData.visibility = View.VISIBLE // Mostrar mensaje de error
            rvCategories.visibility = View.GONE  // Ocultar el RecyclerView de categorías
        } else {
            tvNoData.visibility = View.GONE  // Ocultar mensaje de error
            rvCategories.visibility = View.VISIBLE // Mostrar el RecyclerView
        }
    }

    data class CategoryWithExpense(
        val category: Category,
        val totalExpenses: Double
    )

    data class Category(
        val id: String,
        val name: String,
        val emoji: String // Agregamos el campo emoji
    )

}