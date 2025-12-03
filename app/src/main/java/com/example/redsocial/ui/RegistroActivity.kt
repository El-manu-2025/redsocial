package com.example.redsocial.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.redsocial.FirebaseManager
import com.example.redsocial.databinding.ActivityRegistroBinding
import com.example.redsocial.ui.FeedActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.redsocial.ui.LoginActivity

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private lateinit var auth: FirebaseAuth
    private val firebaseManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnRegistrar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validarEntradas(nombre, email, password)) {
                registrarUsuarioEnFirebase(nombre, email, password)
            }
        }

        binding.btnIrALogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validarEntradas(nombre: String, email: String, pass: String): Boolean {
        if (nombre.isEmpty()) {
            binding.etNombre.error = "El nombre es obligatorio"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Formato de correo inválido"
            return false
        }
        if (pass.length < 6) {
            binding.etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            return false
        }
        return true
    }

    private fun registrarUsuarioEnFirebase(nombre: String, email: String, pass: String) {
        binding.btnRegistrar.isEnabled = false
        Toast.makeText(this, "Creando cuenta...", Toast.LENGTH_SHORT).show()

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: ""

                    val prefs = getSharedPreferences("RedSocialPrefs", MODE_PRIVATE)
                    prefs.edit().putString("USER_ID", userId).apply()

                    firebaseManager.guardarUsuario(userId, nombre, email)

                    Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()

                    // La clase FeedActivity ahora es reconocida por el import
                    val intent = Intent(this, FeedActivity::class.java)
                    // CORRECCIÓN: Era .addFlags(), no .flags()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    binding.btnRegistrar.isEnabled = true
                    val errorMsg = task.exception?.message ?: "Error desconocido"
                    Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
    }
}