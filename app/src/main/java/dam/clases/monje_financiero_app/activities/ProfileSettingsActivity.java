package dam.clases.monje_financiero_app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import dam.clases.monje_financiero_app.R;
import dam.clases.monje_financiero_app.services.ApiService;
import dam.clases.monje_financiero_app.services.UsersService;
import dam.clases.monje_financiero_app.services.UsersService.UploadCallback; // Importa la interfaz UploadCallback
import okhttp3.Callback;
import okhttp3.Response;

public class ProfileSettingsActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputLayout textInputName;
    private Button btnSaveChanges, btnChangePhoto;
    private ImageButton btnBack;
    private ImageView ivProfilePicture;
    private UsersService usersService;
    private Uri imageUri;

    private static final int PICK_IMAGE = 1;

    private void updateUserProfile(String name, String profileImageUrl) {
        usersService.updateUser(ApiService.getUserId(this), name, profileImageUrl, new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ProfileSettingsActivity.this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(ProfileSettingsActivity.this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileSettingsActivity.this, "Error al actualizar el perfil: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.etName);
        textInputName = findViewById(R.id.textInputName);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnBack = findViewById(R.id.btnBack);

        usersService = new UsersService(this);

        btnChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        // Botón para regresar a HomeActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileSettingsActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        btnSaveChanges.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();

            // Validación: nombre no vacío
            if (name.isEmpty()) {
                textInputName.setError("El nombre no puede estar vacío");
                return; // Salimos si el nombre está vacío
            }

            // Subir imagen si se ha seleccionado
            if (imageUri != null) {
                try {
                    File imageFile = createFileFromUri(imageUri);
                    usersService.uploadImage(ProfileSettingsActivity.this, imageFile, new UploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            // Ahora que tenemos la URL de la imagen, actualizamos el usuario
                            updateUserProfile(name, imageUrl);
                        }

                        @Override
                        public void onFailure(okhttp3.Call call, IOException e) {
                            runOnUiThread(() -> Toast.makeText(ProfileSettingsActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show());
                        }
                    });
                } catch (IOException e) {
                    Toast.makeText(this, "Error al manejar la imagen", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Si no hay imagen, solo actualizamos el nombre
                updateUserProfile(name, null); // Pasar null o "" como URL
            }
        });


        // Configurar el BottomNavigationView
        configureBottomNavigation();
    }

    // Método para convertir Uri a un archivo
    private File createFileFromUri(Uri uri) throws IOException {
        File file = new File(getCacheDir(), "profile_image.jpg");
        InputStream inputStream = getContentResolver().openInputStream(uri);
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outputStream.close();
        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            ivProfilePicture.setImageURI(imageUri);
        }
    }

    // Configurar la barra de navegación inferior
    private void configureBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                startActivity(new Intent(ProfileSettingsActivity.this, HomeActivity.class));
                return true;
            } else if (id == R.id.navigation_expenses) {
                startActivity(new Intent(ProfileSettingsActivity.this, ExpensesActivity.class));
                return true;
            } else if (id == R.id.navigation_budgets) {
                startActivity(new Intent(ProfileSettingsActivity.this, BudgetsActivity.class));
                return true;
            } else if (id == R.id.navigation_reports) {
                startActivity(new Intent(ProfileSettingsActivity.this, ReportsActivity.class));
                return true;
            } else if (id == R.id.navigation_settings) {
                startActivity(new Intent(ProfileSettingsActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}