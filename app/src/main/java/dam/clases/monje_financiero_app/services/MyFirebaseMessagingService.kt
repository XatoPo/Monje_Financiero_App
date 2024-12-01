package dam.clases.monje_financiero_app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import dam.clases.monje_financiero_app.R
import dam.clases.monje_financiero_app.activities.HomeActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val channelId = "default_channel"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Si el mensaje contiene una notificación
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    // Muestra la notificación en la barra de estado
    private fun showNotification(title: String?, message: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación (solo para Android Oreo o superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones de la aplicación",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la HomeActivity al hacer clic en la notificación
        val notificationIntent = Intent(this, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)  // Para que se abra la actividad al hacer click
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val notificationIcon = R.drawable.ic_monje_logo

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(notificationIcon)  // El ícono de la notificación
            .setAutoCancel(true)  // La notificación se descarta al tocarla
            .setContentIntent(pendingIntent)  // Acción al tocar la notificación
            .build()

        notificationManager.notify(0, notification)
    }
}