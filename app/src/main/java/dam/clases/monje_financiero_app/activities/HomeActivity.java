package dam.clases.monje_financiero_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import dam.clases.monje_financiero_app.R;
import dam.clases.monje_financiero_app.services.ApiService;

public class HomeActivity extends AppCompatActivity {

    private TextView welcomeMessage;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        welcomeMessage = findViewById(R.id.tvWelcome);
        apiService = new ApiService();

        // Cargar el nombre del usuario desde SharedPreferences
        String userId = ApiService.getUserId(this);
        loadUserData(userId);
    }

    // Método para cargar los datos del usuario
    private void loadUserData(String userId) {
        apiService.get("/users/" + userId, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> {
                    welcomeMessage.setText("Error al cargar los datos del usuario");
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        String userName = jsonResponse.getString("name");

                        runOnUiThread(() -> {
                            welcomeMessage.setText("Hola, " + userName);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> {
                        welcomeMessage.setText("Error al cargar los datos del usuario");
                    });
                }
            }
        });
    }
}