package dam.clases.monje_financiero_app.services;

import android.content.Context;
import android.widget.Toast;

import dam.clases.monje_financiero_app.activities.ProfileSettingsActivity;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class UsersService {

    private ApiService apiService;
    private static final String IMGBB_API_KEY = "9434e7bc3ef6c2f67099dcab237c3a56"; // Tu API key
    private static final String IMGBB_URL = "https://api.imgbb.com/1/upload";

    // Cambiado el constructor para que acepte un contexto genérico
    public UsersService(Context context) {
        this.apiService = new ApiService();
    }

    public void loginUser(String email, String password, Context context, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiService.post("users/login", json.toString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String userId = jsonResponse.getString("userId");
                        ApiService.saveUserId(context, userId);  // Guardar el user_id en SharedPreferences
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                callback.onResponse(call, response);
            }
        });
    }

    public void registerUser(String name, String email, String password, String dateOfBirth, String profileImageUrl, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("email", email);
            json.put("password", password);
            json.put("date_of_birth", dateOfBirth);
            json.put("profile_image_url", profileImageUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiService.post("users/register", json.toString(), callback);
    }

    public void updateUser(String userId, String name, String profileImageUrl, Callback callback) {
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("profile_image_url", profileImageUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        apiService.put("users/" + userId, json.toString(), callback);
    }

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(Call call, IOException e);
    }

    public void uploadImage(Context context, File imageFile, UploadCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // Cuerpo de la solicitud para subir la imagen
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", IMGBB_API_KEY)
                .addFormDataPart("image", imageFile.getName(),
                        RequestBody.create(MediaType.parse("image/*"), imageFile)) // Agregar imagen
                .build();

        Request request = new Request.Builder()
                .url(IMGBB_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Manejo de fallo en la subida
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.isSuccessful()) {
                        // Procesar la respuesta exitosa
                        String responseBody = response.body().string(); // Lee el cuerpo de la respuesta
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        boolean success = jsonResponse.getBoolean("success");

                        if (success) {
                            String imageUrl = jsonResponse.getJSONObject("data").getString("url");

                            // Ejecutar en el hilo principal para mostrar el resultado
                            ((ProfileSettingsActivity) context).runOnUiThread(() -> {
                                Toast.makeText(context, "URL de la imagen: " + imageUrl, Toast.LENGTH_LONG).show();
                                // Pasar la respuesta al callback original
                                callback.onSuccess(imageUrl);
                            });
                        } else {
                            callback.onFailure(call, new IOException("Fallo al subir la imagen"));
                        }
                    } else {
                        callback.onFailure(call, new IOException("Error de respuesta inesperada: " + response.code()));
                    }
                } catch (JSONException | IOException e) {
                    callback.onFailure(call, new IOException("Error al procesar la respuesta", e));
                } finally {
                    if (response.body() != null) {
                        response.body().close(); // Cerrar el cuerpo de la respuesta
                    }
                }
            }
        });
    }

    // Método para obtener los datos del usuario
    public void getUser(String userId, Callback callback) {
        apiService.get("users/" + userId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(call, response);
            }
        });
    }
}