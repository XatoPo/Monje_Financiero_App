package dam.clases.monje_financiero_app.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.services.ApiService
import dam.clases.monje_financiero_app.services.UsersService
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var textInputName: TextInputLayout
    private lateinit var btnSaveChanges: Button
    private lateinit var btnChangePhoto: Button
    private lateinit var btnBack: ImageButton
    private lateinit var ivProfilePicture: ImageView
    private lateinit var usersService: UsersService
    private var imageUri: Uri? = null

    private val PICK_IMAGE = 1
    private lateinit var loadingDialog: ALoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLoadingDialog()
        setContentView(R.layout.activity_profile)

        // Inicialización de las vistas
        etName = findViewById(R.id.etName)
        textInputName = findViewById(R.id.textInputName)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        btnBack = findViewById(R.id.btnBack)

        usersService = UsersService(this)

        // Llamar a la API para cargar los datos del perfil
        loadUserProfile()

        // Cambiar foto
        btnChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        // Botón para guardar cambios
        btnSaveChanges.setOnClickListener {
            showLoadingDialog()
            val name = etName.text.toString().trim()

            if (name.isEmpty()) {
                textInputName.error = "El nombre no puede estar vacío"
                return@setOnClickListener
                loadingDialog.dismiss()
            }

            // Subir imagen si fue seleccionada
            if (imageUri != null) {
                try {
                    val imageFile = createFileFromUri(imageUri!!)
                    usersService.uploadImage(this, imageFile, object : UsersService.UploadCallback {
                        override fun onSuccess(imageUrl: String) {
                            updateUserProfile(name, imageUrl)
                        }

                        override fun onFailure(call: okhttp3.Call, e: IOException) {
                            runOnUiThread {
                                Toast.makeText(this@ProfileSettingsActivity, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                } catch (e: IOException) {
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Error al manejar la imagen", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Si no hay imagen, solo actualizamos el nombre
                loadingDialog.dismiss()
                updateUserProfile(name, null)
            }
        }

        // Botón para regresar a HomeActivity
        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Configurar la barra de navegación inferior
        configureBottomNavigation()
    }

    private fun showLoadingDialog() {
        loadingDialog = ALoadingDialog(this) // Inicializar el diálogo de carga
        loadingDialog.show() // Mostrar el diálogo
    }

    // Cargar los datos del usuario (nombre e imagen de perfil)
    private fun loadUserProfile() {
        val userId = ApiService.getUserId(this) // Obtener ID del usuario

        // Realizar la llamada a la API para obtener datos del usuario
        usersService.getUser(userId, object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    loadingDialog.dismiss() // Ocultar el diálogo de carga
                    Toast.makeText(this@ProfileSettingsActivity, "Error al obtener los datos del perfil", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        // Obtener la respuesta como string
                        val responseBody = response.body?.string()
                        Log.d("ProfileSettingsActivity", "Respuesta del servidor: $responseBody")

                        // Aquí el JSON es un array, así que lo parseamos como JSONArray
                        val jsonResponseArray = JSONArray(responseBody)

                        if (jsonResponseArray.length() > 0) {
                            // Obtener el primer objeto dentro del array
                            val userData = jsonResponseArray.getJSONArray(0).getJSONObject(0)

                            val userName = userData.optString("name", "")
                            val profileImageUrl = userData.optString("profile_image_url", "")

                            runOnUiThread {
                                etName.setText(userName)

                                // Cargar la imagen de perfil
                                if (profileImageUrl.isNotEmpty()) {
                                    Glide.with(this@ProfileSettingsActivity)
                                        .load(profileImageUrl)
                                        .circleCrop()
                                        .into(ivProfilePicture)
                                    loadingDialog.dismiss()
                                } else {
                                    // Si no hay URL de imagen, usar la imagen predeterminada
                                    ivProfilePicture.setImageResource(R.drawable.ic_placeholder_avatar)
                                    loadingDialog.dismiss()
                                }
                            }
                        } else {
                            runOnUiThread {
                                loadingDialog.dismiss()
                                Toast.makeText(this@ProfileSettingsActivity, "No se encontró información de perfil", Toast.LENGTH_SHORT).show()
                            }
                        }

                    } catch (e: JSONException) {
                        // Capturar el error de parsing y registrar más detalles
                        Log.e("ProfileSettingsActivity", "Error al procesar los datos del perfil", e)
                        runOnUiThread {
                            loadingDialog.dismiss()
                            Toast.makeText(this@ProfileSettingsActivity, "Error al procesar los datos del perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        loadingDialog.dismiss()
                        Toast.makeText(this@ProfileSettingsActivity, "Error del servidor: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // Actualizar los datos del perfil en la base de datos
    private fun updateUserProfile(name: String, profileImageUrl: String?) {
        val userId = ApiService.getUserId(this)
        usersService.updateUser(userId, name, profileImageUrl, object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    loadingDialog.dismiss()
                    Toast.makeText(this@ProfileSettingsActivity, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        loadingDialog.dismiss()
                        Toast.makeText(this@ProfileSettingsActivity, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                    } else {
                        loadingDialog.dismiss()
                        Toast.makeText(this@ProfileSettingsActivity, "Error al actualizar el perfil: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // Crear archivo desde la URI de la imagen seleccionada
    private fun createFileFromUri(uri: Uri): File {
        val file = File(cacheDir, "profile_image.jpg")
        val inputStream: InputStream = contentResolver.openInputStream(uri)!!
        val outputStream = FileOutputStream(file)
        val buffer = ByteArray(4096)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        inputStream.close()
        outputStream.close()
        return file
    }

    // Configurar la barra de navegación inferior
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

    // Manejo de resultados de la selección de imagen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            ivProfilePicture.setImageURI(imageUri)
        }
    }
}