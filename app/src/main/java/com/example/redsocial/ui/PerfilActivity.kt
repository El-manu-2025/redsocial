package com.example.redsocial.ui // Asegúrate de que este paquete coincida con el de tus otros archivos

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.redsocial.databinding.ActivityPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

import com.example.redsocial.ui.FeedActivity
import com.example.redsocial.ui.CrearPublicacionActivity

/**
 * Actividad que gestiona el perfil del usuario
 * Permite visualizar y editar la información del usuario, como nombre y foto de perfil
 * También maneja el cierre de sesión y la validación de cambios (e.g., frecuencia de cambio de nombre).
 */
class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var imageUri: Uri? = null
    private var ultimaFechaCambio: Long = 0

    // RegisterForActivityResult
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.ivFotoPerfil.setImageURI(uri)
            binding.ivFotoPerfil.setPadding(0, 0, 0, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarDatosUsuario()
        setupBotones()
        setupNavigation()
    }

    /**
     * Configura los listeners para los botones de la interfaz:
     * - Cambiar foto
     * - Guardar cambios
     * - Cerrar sesión
     */
    private fun setupBotones() {
        binding.btnCambiarFoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnGuardarCambios.setOnClickListener {
            validarYGuardar()
        }

        binding.btnCerrarSesion.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }

    /**
     * Carga los datos actuales del usuario desde Firestore para mostrarlos en la UI
     */
    private fun cargarDatosUsuario() {
        val userId = auth.currentUser?.uid ?: return


        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("name") ?: ""
                    ultimaFechaCambio = document.getLong("lastInfoUpdate") ?: 0

                    val fotoBase64 = document.getString("photoBlob")

                    binding.etNombreUsuario.setText(nombre)

                    if (!fotoBase64.isNullOrEmpty()) {
                        binding.ivFotoPerfil.setPadding(0, 0, 0, 0)
                        val bitmap = decodificarBase64(fotoBase64)
                        binding.ivFotoPerfil.setImageBitmap(bitmap)
                    }
                }
            }
    }

    /**
     * Valida si el usuario puede cambiar su nombre (regla de 360 días) y procede al guardado
     */
    private fun validarYGuardar() {
        val userId = auth.currentUser?.uid ?: return
        val nuevoNombre = binding.etNombreUsuario.text.toString().trim()

        if (nuevoNombre.isEmpty()) {
            binding.etNombreUsuario.error = "El nombre no puede estar vacío"
            return
        }

        val hoy = System.currentTimeMillis()
        val diasPasados = TimeUnit.MILLISECONDS.toDays(hoy - ultimaFechaCambio)

        if (diasPasados < 360 && ultimaFechaCambio != 0L) {
            db.collection("users").document(userId).get().addOnSuccessListener { doc ->
                val nombreActual = doc.getString("name") ?: ""
                if (nombreActual != nuevoNombre) {
                    Toast.makeText(this, "Espera ${360 - diasPasados} días para cambiar el nombre.", Toast.LENGTH_LONG).show()
                } else {
                    procesarGuardado(userId, nuevoNombre, false)
                }
            }
        } else {
            procesarGuardado(userId, nuevoNombre, true)
        }
    }

    /**
     * Procesa la imagen (si hay una nueva) y actualiza los datos en Firestore
     */
    private fun procesarGuardado(userId: String, nombre: String, actualizarFecha: Boolean) {
        binding.btnGuardarCambios.isEnabled = false
        binding.btnGuardarCambios.text = "Comprimiendo..."

        if (imageUri != null) {
            val blobString = convertirUriABase64(imageUri!!)
            if (blobString != null) {
                actualizarFirestore(userId, nombre, blobString, actualizarFecha)
            } else {
                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                binding.btnGuardarCambios.isEnabled = true
            }
        } else {
            actualizarFirestore(userId, nombre, null, actualizarFecha)
        }
    }

    /**
     * Convierte una imagen desde URI a un String Base64 comprimido
     */
    private fun convertirUriABase64(uri: Uri): String? {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmapOriginal = BitmapFactory.decodeStream(inputStream)
            val bitmapReducido = Bitmap.createScaledBitmap(bitmapOriginal, 500, 500, true)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmapReducido.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Decodifica un String Base64 a Bitmap
     */
    private fun decodificarBase64(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    /**
     * Realiza la actualización final en la base de datos Firestore
     */
    private fun actualizarFirestore(userId: String, nombre: String, fotoBlob: String?, cambiarFecha: Boolean) {
        val datos = mutableMapOf<String, Any>("name" to nombre)
        if (fotoBlob != null) datos["photoBlob"] = fotoBlob
        if (cambiarFecha) datos["lastInfoUpdate"] = System.currentTimeMillis()

        db.collection("users").document(userId).update(datos)
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil guardado", Toast.LENGTH_SHORT).show()
                binding.btnGuardarCambios.isEnabled = true
                binding.btnGuardarCambios.text = "Guardar Cambios"
                if (cambiarFecha) ultimaFechaCambio = System.currentTimeMillis()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_LONG).show()
                binding.btnGuardarCambios.isEnabled = true
                binding.btnGuardarCambios.text = "Guardar Cambios"
            }
    }

    /**
     * Configura la barra de navegación inferior para ir a otras actividades
     */
    private fun setupNavigation() {
        binding.btnNavHome.setOnClickListener {
            val intent = Intent(this, FeedActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        binding.btnNavCrear.setOnClickListener {
            val intent = Intent(this, CrearPublicacionActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.btnNavPerfil.setOnClickListener {
            Toast.makeText(this, "Ya estás en tu perfil", Toast.LENGTH_SHORT).show()
        }
    }
}
