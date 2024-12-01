package dam.clases.monje_financiero_app.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dam.clases.monje_financiero_app.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class GuideActivity : AppCompatActivity() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        // Inicializar vistas
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val btnPlayVideo: Button = findViewById(R.id.btnPlayVideo)
        playerView = findViewById(R.id.vvGuideVideo)

        // Configurar el botón de regresar
        btnBack.setOnClickListener {
            finish()
        }

        // Inicializar ExoPlayer
        exoPlayer = ExoPlayer.Builder(this).build()
        playerView.player = exoPlayer

        // Agregar un video desde una URL
        val videoUri = Uri.parse("android.resource://${packageName}/raw/guide_monje_financiero")
        val mediaItem = MediaItem.fromUri(videoUri)
        exoPlayer.setMediaItem(mediaItem)

        // Botón para reproducir el video
        btnPlayVideo.setOnClickListener {
            exoPlayer.prepare()
            exoPlayer.play()
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        configureBottomNavigation()
    }

    override fun onStop() {
        super.onStop()
        exoPlayer.release() // Liberar recursos de ExoPlayer
    }

    private fun configureBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNavigationView.selectedItemId = R.id.navigation_home
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> false
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
}
