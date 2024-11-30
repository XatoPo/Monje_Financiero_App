package dam.clases.monje_financiero_app.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.models.Expense
import dam.clases.monje_financiero_app.models.Category
import dam.clases.monje_financiero_app.services.ExpensesService
import com.google.android.material.bottomnavigation.BottomNavigationView
import dam.clases.monje_financiero_app.services.ApiService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.*

class ExpensesActivity : AppCompatActivity() {
    private lateinit var inputLayoutDescription: TextInputLayout
    private lateinit var inputLayoutAmount: TextInputLayout
    private lateinit var etDescription: TextInputEditText
    private lateinit var etAmount: TextInputEditText
    private lateinit var btnSelectCategory: Button
    private lateinit var btnSelectDate: Button
    private lateinit var switchRecurring: Switch
    private lateinit var btnSaveExpense: Button

    private lateinit var expensesService: ExpensesService
    private var selectedCategoryId: String? = null
    private var selectedDate: String = "" // Fecha seleccionada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses)

        // Inicialización de componentes
        inputLayoutDescription = findViewById(R.id.inputLayoutDescription)
        inputLayoutAmount = findViewById(R.id.inputLayoutAmount)
        etDescription = findViewById(R.id.etDescription)
        etAmount = findViewById(R.id.etAmount)
        btnSelectCategory = findViewById(R.id.btnSelectCategory)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        switchRecurring = findViewById(R.id.switchRecurring)
        btnSaveExpense = findViewById(R.id.btnSaveExpense)

        expensesService = ExpensesService(this)

        // Configurar botones
        btnSelectCategory.setOnClickListener {
            showCategorySelectionDialog()
        }

        btnSelectDate.setOnClickListener {
            showDatePickerDialog()
        }

        btnSaveExpense.setOnClickListener {
            validateAndSaveExpense()
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        configureBottomNavigation()
    }

    private fun showCategorySelectionDialog() {
        val intent = Intent(this, CategorySelectionActivity::class.java)
        startActivityForResult(intent, CATEGORY_SELECTION_REQUEST_CODE)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            btnSelectDate.text = selectedDate
        }, year, month, day)

        datePickerDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CATEGORY_SELECTION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedCategory = data?.getParcelableExtra<Category>("selectedCategory")
            selectedCategoryId = selectedCategory?.id
            btnSelectCategory.text = selectedCategory?.name
        }
    }

    private fun validateAndSaveExpense() {
        val description = etDescription.text.toString().trim()
        val amountStr = etAmount.text.toString().trim()

        // Validaciones
        if (description.isEmpty()) {
            inputLayoutDescription.error = "La descripción es obligatoria"
            return
        } else {
            inputLayoutDescription.error = null
        }

        if (amountStr.isEmpty()) {
            inputLayoutAmount.error = "El monto es obligatorio"
            return
        } else {
            inputLayoutAmount.error = null
        }

        if (selectedCategoryId == null) {
            Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: run {
            inputLayoutAmount.error = "Monto inválido"
            return
        }

        // Crear el objeto Expense
        val expense = Expense(
            id = "",
            userId = ApiService.getUserId(this)!!,
            description = description,
            amount = amount,
            categoryId = selectedCategoryId!!,
            date = selectedDate,
            isRecurring = switchRecurring.isChecked
        )

        // Guardar el gasto
        saveExpense(expense)
    }

    private fun saveExpense(expense: Expense) {
        val json = JSONObject().apply {
            put("user_id", expense.userId)
            put("description", expense.description)
            put("amount", expense.amount)
            put("category_id", expense.categoryId)
            put("date", expense.date)
            put("is_recurring", expense.isRecurring)
        }

        expensesService.addExpense(
            expense.userId,
            expense.description,
            expense.amount,
            expense.categoryId,
            expense.date,
            expense.isRecurring,
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@ExpensesActivity, "Error al guardar el gasto", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@ExpensesActivity, "Gasto guardado exitosamente", Toast.LENGTH_SHORT).show()
                            clearFields()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@ExpensesActivity, "Error al guardar el gasto", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }

    private fun clearFields() {
        etDescription.setText("")
        etAmount.setText("")
        btnSelectCategory.text = "Seleccionar categoría"
        btnSelectDate.text = "Seleccionar fecha"
        switchRecurring.isChecked = false
        selectedCategoryId = null
        selectedDate = ""
    }

    companion object {
        private const val CATEGORY_SELECTION_REQUEST_CODE = 1
    }

    private fun configureBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.navigation_expenses
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navigation_expenses -> false
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