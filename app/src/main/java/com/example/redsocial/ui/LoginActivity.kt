package com.example.redsocial.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.redsocial.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Actividad encargada de la autenticación de usuarios ya registrados
 * Permite iniciar sesión mediante correo electrónico y contraseña
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        setupListeners()
    }

    /**
     * Configura los listeners de los botones de la interfaz
     */
    private fun setupListeners() {
        // Botón para iniciar sesión
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmailLogin.text.toString().trim()
            val password = binding.etPasswordLogin.text.toString().trim()

            if (validarEntradas(email, password)) {
                iniciarSesionConFirebase(email, password)
            }
        }

        // Listener para navegar a la pantalla de registro
        binding.btnIrARegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Valida los campos de entrada del formulario de login
     * @return True si los datos son válidos, False en caso contrario
     */
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

    /**
     * Intenta iniciar sesión en Firebase con las credenciales proporcionadas
     * Si tiene éxito, recupera el nombre del usuario y redirige al feed
     */
    private fun iniciarSesionConFirebase(email: String, pass: String) {
        binding.btnLogin.isEnabled = false
        Toast.makeText(this, "Iniciando sesión...", Toast.LENGTH_SHORT).show()

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    if (userId != null) {
                        // Recupero el nombre del usuario desde Firestore para guardarlo en preferencias
                        db.collection("usuarios").document(userId).get()
                            .addOnSuccessListener { document ->
                                val nombre = document.getString("nombre") ?: "Usuario"
                                
                                // Guardamos en SharedPreferences para uso global en la app
                                val prefs = getSharedPreferences("RedSocialPrefs", MODE_PRIVATE)
                                prefs.edit()
                                    .putString("USER_ID", userId)
                                    .putString("USER_NAME", nombre)
                                    .apply()

                                Toast.makeText(this, "¡Bienvenido $nombre!", Toast.LENGTH_SHORT).show()
                                irAlFeed()
                            }
                            .addOnFailureListener { e ->
                                // Si falla la recuperación del nombre, procedemos igual pero con nombre por defecto
                                Log.e("Login", "Error al obtener usuario", e)
                                irAlFeed()
                            }
                    } else {
                        irAlFeed()
                    }
                } else {
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Navega a la actividad principal (FeedActivity) y cierra la actividad de login
     * Limpia la pila de actividades para que el usuario no pueda volver al login con el botón Atrás
     */
    private fun irAlFeed() {
        val intent = Intent(this, FeedActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}
