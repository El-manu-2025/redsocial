package com.example.redsocial.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.redsocial.databinding.ActivityLoginBinding
import com.example.redsocial.ui.CrearPublicacionActivity
import com.example.redsocial.ui.FeedActivity
import com.google.firebase.auth.FirebaseAuth



class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmailLogin.text.toString().trim()
            val password = binding.etPasswordLogin.text.toString().trim()

            if (validarEntradas(email, password)) {
                iniciarSesionConFirebase(email, password)
            }
        }
    }

    private fun validarEntradas(email: String, pass: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailLogin.error = "Formato de correo inválido"
            return false
        }
        if (pass.isEmpty()) {
            binding.etPasswordLogin.error = "La contraseña no puede estar vacía"
            return false
        }
        return true
    }

    private fun iniciarSesionConFirebase(email: String, pass: String) {
        binding.btnLogin.isEnabled = false
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()

                    val prefs = getSharedPreferences("RedSocialPrefs", MODE_PRIVATE)
                    prefs.edit().putString("USER_ID", user?.uid).apply()

                    // La clase FeedActivity ahora es reconocida por el import
                    val intent = Intent(this, FeedActivity::class.java)
                    // CORRECCIÓN: Era .addFlags(), no .flags()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()

                } else {
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
}