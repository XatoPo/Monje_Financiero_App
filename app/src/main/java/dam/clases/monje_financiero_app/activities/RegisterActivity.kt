package dam.clases.monje_financiero_app.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.services.UsersService
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var inputLayoutUsername: TextInputLayout
    private lateinit var inputLayoutEmail: TextInputLayout
    private lateinit var inputLayoutPassword: TextInputLayout
    private lateinit var inputLayoutConfirmPassword: TextInputLayout
    private lateinit var inputLayoutGender: TextInputLayout
    private lateinit var inputLayoutBirthDate: TextInputLayout

    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var btnSelectDate: Button
    private lateinit var btnRegisterSubmit: Button

    private lateinit var usersService: UsersService
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        inputLayoutUsername = findViewById(R.id.textInputUsername)
        inputLayoutEmail = findViewById(R.id.textInputEmailRegister)
        inputLayoutPassword = findViewById(R.id.textInputPasswordRegister)
        inputLayoutConfirmPassword = findViewById(R.id.textInputConfirmPassword)
        inputLayoutGender = findViewById(R.id.tvGender)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmailRegister)
        etPassword = findViewById(R.id.etPasswordRegister)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit)

        usersService = UsersService(this)

        // Configurar el botón de selección de fecha
        btnSelectDate.setOnClickListener {
            showDatePickerDialog()
        }

        // Configurar el botón de registro
        btnRegisterSubmit.setOnClickListener {
            registerUser()
        }
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

    private fun registerUser() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Validaciones
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        val gender = when (radioGroupGender.checkedRadioButtonId) {
            R.id.radioMale -> "Masculino"
            R.id.radioFemale -> "Femenino"
            R.id.radioOther -> "Otro"
            else -> ""
        }

        // Crear el objeto JSON para la solicitud
        val json = JSONObject().apply {
            put("name", username)
            put("email", email)
            put("password", password)
            put("date_of_birth", selectedDate)
            put("profile_image_url", "") // Si hay un campo para la imagen, agregarlo aquí
        }

        // Realizar el registro
        usersService.registerUser(username, email, password, selectedDate, "", object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, "Error al registrar el usuario", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        // Redirigir al login
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Error al registrar el usuario", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
