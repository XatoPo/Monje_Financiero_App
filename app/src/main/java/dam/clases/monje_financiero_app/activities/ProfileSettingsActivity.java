package dam.clases.monje_financiero_app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import dam.clases.monje_financiero_app.R;
import dam.clases.monje_financiero_app.services.ApiService;
import dam.clases.monje_financiero_app.services.UsersService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProfileSettingsActivity extends AppCompatActivity {

    private EditText etName;
    private Button btnSaveChanges, btnChangePhoto;
    private ImageView ivProfilePicture;
    private UsersService usersService;
    private Uri imageUri; // URI de la imagen seleccionada
    private String userId; // Almacenar el userId

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.etName);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        ivProfilePicture = findViewById(R.id.ivProfilePicture);

        usersService = new UsersService(this);
        userId = ApiService.getUserId(this); // Obtener el userId de SharedPreferences

        // Cargar los datos del usuario al entrar en la actividad
        loadUserData();

        btnChangePhoto.setOnClickListener(v -> {
            // Abrir la galería para seleccionar una imagen
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        btnSaveChanges.setOnClickListener(v -> {
            String name = etName.getText().toString();

            if (imageUri != null) {
                File imageFile = new File(imageUri.getPath());
                usersService.uploadImage(ProfileSettingsActivity.this, imageFile, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> Toast.makeText(ProfileSettingsActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String imageUrl = response.body().string();
                            // Actualizar los datos del usuario con el nuevo nombre y la nueva imagen
                            updateUserProfile(name, imageUrl);
                        } else {
                            runOnUiThread(() -> Toast.makeText(ProfileSettingsActivity.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
            } else {
                // Si no se seleccionó imagen, solo actualizamos el nombre
                updateUserProfile(name, "");
            }
        });
    }

    // Cargar los datos del usuario desde la API
    private void loadUserData() {
        usersService.getUser(userId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ProfileSettingsActivity.this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray resultArray = new JSONArray(responseBody);
                        JSONObject userData = resultArray.getJSONObject(0);

                        String name = userData.getString("name");
                        String profileImageUrl = userData.getString("profile_image_url");

                        runOnUiThread(() -> {
                            etName.setText(name);
                            if (!profileImageUrl.isEmpty()) {
                                Picasso.get().load(profileImageUrl).into(ivProfilePicture);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    // Actualizar el perfil del usuario con el nuevo nombre y/o la imagen
    private void updateUserProfile(String name, String imageUrl) {
        usersService.updateUser(userId, name, imageUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ProfileSettingsActivity.this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(ProfileSettingsActivity.this, "Perfil actualizado", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(ProfileSettingsActivity.this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            ivProfilePicture.setImageURI(imageUri); // Mostrar la imagen seleccionada
        }
    }
}