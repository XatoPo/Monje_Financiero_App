package dam.clases.monje_financiero_app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import dam.clases.monje_financiero_app.R;
import dam.clases.monje_financiero_app.services.UsersService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private ImageButton btnProfile;
    private UsersService usersService;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializar el servicio de usuarios
        usersService = new UsersService(this);

        // Inicializar el TextView y botón de perfil
        tvWelcome = findViewById(R.id.tvWelcome);
        btnProfile = findViewById(R.id.btnProfile);

        // Obtener el userId de SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MonjeFinancieroPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getString("user_id", null);

        if (userId != null) {
            loadUserData(userId);  // Método que carga los datos del usuario
        } else {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }

        // Configurar el BottomNavigationView
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_home) {
                    return true;
                } else if (id == R.id.navigation_expenses) {
                    startActivity(new Intent(HomeActivity.this, ExpensesActivity.class));
                    return true;
                } else if (id == R.id.navigation_budgets) {
                    startActivity(new Intent(HomeActivity.this, BudgetsActivity.class));
                    return true;
                } else if (id == R.id.navigation_reports) {
                    startActivity(new Intent(HomeActivity.this, ReportsActivity.class));
                    return true;
                } else if (id == R.id.navigation_settings) {
                    startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                    return true;
                }
                return false;
            }
        });

        // Configurar el botón de perfil
        btnProfile.setOnClickListener(v -> showProfileMenu());

        // Configurar botones de acciones rápidas
        configureQuickActions();
    }

    // Método para cargar los datos del usuario
    private void loadUserData(String userId) {
        usersService.getUser(userId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, "Error al cargar los datos del usuario", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONArray jsonResponseArray = new JSONArray(responseBody);

                        if (jsonResponseArray.length() > 0) {
                            JSONArray userArray = jsonResponseArray.getJSONArray(0);
                            if (userArray.length() > 0) {
                                JSONObject userData = userArray.getJSONObject(0);
                                String userName = userData.getString("name");
                                String profileImageUrl = userData.getString("profile_image_url");

                                // Actualizar la UI con los datos del usuario
                                runOnUiThread(() -> {
                                    tvWelcome.setText("Hola, " + userName);

                                    // Verificar si la URL de la imagen de perfil está vacía o nula
                                    if (profileImageUrl == null || profileImageUrl.isEmpty()) {
                                        // Cargar una imagen predeterminada si el campo está vacío o es nulo
                                        Glide.with(HomeActivity.this)
                                                .load(R.drawable.ic_placeholder_avatar) // Imagen predeterminada
                                                .apply(RequestOptions.circleCropTransform()) // Transformación para hacer la imagen circular
                                                .into(btnProfile);
                                    } else {
                                        // Cargar la imagen de perfil proporcionada
                                        Glide.with(HomeActivity.this)
                                                .load(profileImageUrl)
                                                .apply(RequestOptions.circleCropTransform()) // Transformación para hacer la imagen circular
                                                .into(btnProfile);
                                    }
                                });
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(HomeActivity.this, "No se encontraron datos del usuario", Toast.LENGTH_LONG).show();
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(HomeActivity.this, "Respuesta vacía del servidor", Toast.LENGTH_LONG).show();
                            });
                        }

                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(HomeActivity.this, "Error al procesar JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(HomeActivity.this, "Error del servidor: " + response.code(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    // Método para mostrar el menú de perfil
    private void showProfileMenu() {
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.btnProfile));
        popupMenu.getMenuInflater().inflate(R.menu.dropdown_menu_profile, popupMenu.getMenu());

        // Forzar la visualización de íconos en el PopupMenu
        try {
            Field popup = PopupMenu.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            Object menuPopupHelper = popup.get(popupMenu);
            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
            Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
            setForceIcons.invoke(menuPopupHelper, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_edit_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileSettingsActivity.class));
                return true;
            } else if (id == R.id.menu_guide) {
                // Lógica para mostrar guía
                return true;
            } else if (id == R.id.menu_office_location) {
                // Lógica para mostrar ubicación de oficinas
                return true;
            } else if (id == R.id.menu_logout) {
                SharedPreferences sharedPreferences = getSharedPreferences("MonjeFinancieroPrefs", MODE_PRIVATE);
                sharedPreferences.edit().clear().apply();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                finish();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    // Método para configurar las acciones rápidas
    private void configureQuickActions() {
        Button btnRegisterExpense = findViewById(R.id.btnRegisterExpense);
        Button btnViewBudgets = findViewById(R.id.btnViewBudgets);
        Button btnGenerateReport = findViewById(R.id.btnGenerateReport);
        Button btnManageCategories = findViewById(R.id.btnManageCategories);

        btnRegisterExpense.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ExpensesActivity.class)));
        btnViewBudgets.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, BudgetsActivity.class)));
        btnGenerateReport.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ReportsActivity.class)));
        btnManageCategories.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CategoriesActivity.class)));
    }
}