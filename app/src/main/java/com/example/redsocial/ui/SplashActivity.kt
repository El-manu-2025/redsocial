package com.example.redsocial.ui


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.redsocial.ui.FeedActivity
import com.example.redsocial.ui.LoginActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // No necesitamos setContentView porque es una pantalla invisible de paso rápido
        // (O puedes poner un logo si quieres)

        verificarSesion()
    }

    private fun verificarSesion() {
        val auth = FirebaseAuth.getInstance()
        val usuarioActual = auth.currentUser

        if (usuarioActual != null) {
            // 1. SI HAY USUARIO -> Ir directo al Feed
            val intent = Intent(this, FeedActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            // 2. NO HAY USUARIO -> Ir al Login (o Registro)
            val intent = Intent(this, LoginActivity::class.java) // Asegúrate de tener LoginActivity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        finish() // Cierra esta actividad para que no se pueda volver atrás
    }
}