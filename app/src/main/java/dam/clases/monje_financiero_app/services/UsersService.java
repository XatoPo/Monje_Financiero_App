package dam.clases.monje_financiero_app.services;

import android.content.Context;

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

    public void uploadImage(Context context, File imageFile, Callback callback) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("key", IMGBB_API_KEY)
                .addFormDataPart("image", imageFile.getName(), RequestBody.create(MediaType.parse("image/*"), imageFile)) // Agregar imagen directamente
                .build();

        Request request = new Request.Builder()
                .url(IMGBB_URL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Pasar el fallo al callback original
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        boolean success = jsonResponse.getBoolean("success");
                        if (success) {
                            String imageUrl = jsonResponse.getJSONObject("data").getString("url");

                            // Ejecutar en el hilo principal para actualizar la UI si es necesario
                            ((ProfileSettingsActivity) context).runOnUiThread(() -> {
                                try {
                                    // Pasar la respuesta al callback original
                                    callback.onResponse(call, response);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } else {
                            callback.onFailure(call, new IOException("Failed to upload image"));
                        }
                    } else {
                        callback.onFailure(call, new IOException("Unexpected response " + response));
                    }
                } catch (JSONException | IOException e) {
                    // Manejar la excepción correctamente
                    callback.onFailure(call, new IOException("Error processing response", e));
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