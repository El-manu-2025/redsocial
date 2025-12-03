package com.example.redsocial.ui

import android.content.Intent // Importante para navegar
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.redsocial.databinding.ActivityCrearPublicacionBinding
import com.example.redsocial.FirebaseManager

class CrearPublicacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearPublicacionBinding
    private val firebaseManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPublicacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la barra superior
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nueva Publicación"

        // Listener para el botón principal de publicar
        binding.btnPublicar.setOnClickListener {
            enviarPublicacion()
        }

        // CONFIGURAR LA BARRA DE NAVEGACIÓN INFERIOR (FOOTER)
        setupNavigation()
    }

    private fun setupNavigation() {
        // 1. Botón HOME (Izquierda) -> Regresa al Feed
        binding.btnNavHome.setOnClickListener {
            val intent = Intent(this, FeedActivity::class.java)
            // Estas banderas son importantes: "Si ya existe el Feed, vuelve a él en vez de crear uno nuevo encima"
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish() // Cierra la pantalla de crear publicación
        }

        // 2. Botón CREAR (Centro) -> Ya estamos aquí
        binding.btnNavCrear.setOnClickListener {
            Toast.makeText(this, "Ya estás creando una publicación", Toast.LENGTH_SHORT).show()
        }

        // 3. Botón PERFIL (Derecha) -> Futura implementación
        binding.btnNavPerfil.setOnClickListener {
            Toast.makeText(this, "Perfil próximamente...", Toast.LENGTH_SHORT).show()
            // Cuando tengas PerfilActivity:
            // startActivity(Intent(this, PerfilActivity::class.java))
        }
    }

    private fun enviarPublicacion() {
        val texto = binding.etCuerpoPost.text.toString().trim()

        if (texto.isEmpty()) {
            binding.etCuerpoPost.error = "No puedes publicar algo vacío"
            return
        }

        val prefs = getSharedPreferences("RedSocialPrefs", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", null)

        if (userId != null) {
            firebaseManager.crearPublicacion(userId, texto, null)
            Toast.makeText(this, "Publicado correctamente", Toast.LENGTH_SHORT).show()

            // Al terminar de publicar, también volvemos al Feed automáticamente
            val intent = Intent(this, FeedActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Error de sesión. Reinicia la app.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
