package dam.clases.monje_financiero_app.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.models.Budget
import dam.clases.monje_financiero_app.models.Category
import dam.clases.monje_financiero_app.services.BudgetsService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class BudgetsActivity : AppCompatActivity() {
    private lateinit var inputLayoutBudgetName: TextInputLayout
    private lateinit var inputLayoutBudgetAmount: TextInputLayout
    private lateinit var etBudgetName: TextInputEditText
    private lateinit var etBudgetAmount: TextInputEditText
    private lateinit var btnSelectCategory: Button
    private lateinit var btnSaveBudget: Button
    private lateinit var radioGroupBudgetPeriod: RadioGroup
    private var userId: String? = null
    private lateinit var budgetsService: BudgetsService
    private var selectedCategoryId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budgets)

        // Inicialización de componentes
        inputLayoutBudgetName = findViewById(R.id.inputLayoutBudgetName)
        inputLayoutBudgetAmount = findViewById(R.id.inputLayoutBudgetAmount)
        etBudgetName = findViewById(R.id.etBudgetName)
        etBudgetAmount = findViewById(R.id.etBudgetAmount)
        btnSelectCategory = findViewById(R.id.btnSelectCategory)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)
        radioGroupBudgetPeriod = findViewById(R.id.radioGroupBudgetPeriod)

        // Obtener el userId de SharedPreferences
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        budgetsService = BudgetsService(this)

        btnSelectCategory.setOnClickListener {
            showCategorySelectionDialog()
        }

        btnSaveBudget.setOnClickListener {
            validateAndSaveBudget()
        }
    }

    private fun showCategorySelectionDialog() {
        // Crear un Intent para iniciar la actividad de selección de categorías
        val intent = Intent(this, CategorySelectionActivity::class.java)
        startActivityForResult(intent, CATEGORY_SELECTION_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CATEGORY_SELECTION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedCategory = data?.getParcelableExtra<Category>("selectedCategory")
            selectedCategoryId = selectedCategory?.id // Asignar el ID de la categoría seleccionada
            btnSelectCategory.text = selectedCategory?.name // Actualizar el texto del botón con el nombre de la categoría
        }
    }

    private fun validateAndSaveBudget() {
        val budgetName = etBudgetName.text.toString().trim()
        val budgetAmountStr = etBudgetAmount.text.toString().trim()

        // Validaciones
        if (budgetName.isEmpty()) {
            inputLayoutBudgetName.error = "El nombre del presupuesto es obligatorio"
            return
        } else {
            inputLayoutBudgetName.error = null // Limpiar el error
        }

        if (budgetAmountStr.isEmpty()) {
            inputLayoutBudgetAmount.error = "El monto límite es obligatorio"
            return
        } else {
            inputLayoutBudgetAmount.error = null // Limpiar el error
        }

        val budgetAmount = budgetAmountStr.toDouble()
        val period = getSelectedBudgetPeriod()

        // Crear el objeto Budget
        val budget = Budget(null.toString(), userId!!, budgetName, budgetAmount, selectedCategoryId!!, period)
        budgetsService.addBudget(userId!!, budgetName, budgetAmount, selectedCategoryId!!, period, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@BudgetsActivity, "Error al guardar el presupuesto", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@BudgetsActivity, "Presupuesto guardado exitosamente", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@BudgetsActivity, "Error al guardar el presupuesto", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun getSelectedBudgetPeriod(): String {
        val selectedId = radioGroupBudgetPeriod.checkedRadioButtonId
        val selectedRadioButton = findViewById<RadioButton>(selectedId)
        return selectedRadioButton.text.toString() // Devolver el texto del botón seleccionado (semanal, mensual, anual)
    }

    companion object {
        private const val CATEGORY_SELECTION_REQUEST_CODE = 1
    }
}