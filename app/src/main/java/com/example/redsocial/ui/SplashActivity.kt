package com.example.redsocial.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Actividad inicial de la aplicación (Splash Screen invisible)
 * Su responsabilidad es verificar si el usuario ya tiene una sesión activa
 * y redirigirlo a la pantalla correspondiente (Feed o Registro)
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No cargamos layout porque es una transición rápida
        verificarSesion()
    }

    /**
     * Verifica el estado de la sesión de Firebase Auth
     * - Si hay usuario autenticado, va directo al FeedActivity
     * - Si no hay usuario, va a RegistroActivity (según requerimiento del usuario)
     * Finaliza (finish) esta actividad para que no quede en el historial
     */
    private fun verificarSesion() {
        val auth = FirebaseAuth.getInstance()
        val usuarioActual = auth.currentUser

        if (usuarioActual != null) {
            // 1. SI HAY USUARIO -> Ir directo al Feed
            val intent = Intent(this, FeedActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            // 2. NO HAY USUARIO -> Ir a Registro (según solicitud)
            val intent = Intent(this, RegistroActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        finish()
    }
}
