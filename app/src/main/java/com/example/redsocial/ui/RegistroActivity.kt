package com.example.redsocial.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.redsocial.FirebaseManager
import com.example.redsocial.databinding.ActivityRegistroBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * Actividad encargada del registro de nuevos usuarios en la aplicación
 * Maneja la validación de campos, creación de cuenta en Firebase Auth y guardado de datos en Firestore
 */
class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private lateinit var auth: FirebaseAuth
    private val firebaseManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar instancia de Firebase Authentication
        auth = FirebaseAuth.getInstance()
        setupListeners()
    }

    /**
     * Configura los listeners para los eventos de los botones (Registrar e Ir a Login)
     */
    private fun setupListeners() {
        binding.btnRegistrar.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            // Si las validaciones pasan, proceder con el registro
            if (validarEntradas(nombre, email, password, confirmPassword)) {
                registrarUsuarioEnFirebase(nombre, email, password)
            }
        }

        binding.btnIrALogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Valida los campos de entrada del formulario de registro
     * Verifica nombre, formato de email, longitud de contraseña y coincidencia de contraseñas
     * @return True si todos los campos son válidos
     */
    private fun validarEntradas(nombre: String, email: String, pass: String, confirmPass: String): Boolean {
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
        if (pass != confirmPass) {
            binding.etConfirmPassword.error = "Las contraseñas no coinciden"
            return false
        }
        return true
    }

    /**
     * Crea un nuevo usuario en Firebase Authentication
     * Si es exitoso, guarda la información adicional (nombre, email) en Firestore y redirige al Feed
     */
    private fun registrarUsuarioEnFirebase(nombre: String, email: String, pass: String) {
        binding.btnRegistrar.isEnabled = false
        Toast.makeText(this, "Creando cuenta...", Toast.LENGTH_SHORT).show()

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: ""

                    // Guardar en SharedPreferences para acceso rápido en la app
                    val prefs = getSharedPreferences("RedSocialPrefs", MODE_PRIVATE)
                    prefs.edit()
                        .putString("USER_ID", userId)
                        .putString("USER_NAME", nombre)
                        .apply()

                    // Guardar datos del usuario en Firestore
                    firebaseManager.guardarUsuario(userId, nombre, email)

                    Toast.makeText(this, "¡Bienvenido $nombre!", Toast.LENGTH_SHORT).show()

                    // Navegar al Feed y cerrar la actividad de registro
                    val intent = Intent(this, FeedActivity::class.java)
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
