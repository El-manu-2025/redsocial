package com.example.redsocial.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.redsocial.FirebaseManager
import com.example.redsocial.databinding.ActivityCrearPublicacionBinding
import java.io.ByteArrayOutputStream

/**
 * Actividad encargada de la creación de nuevas publicaciones
 * Permite al usuario escribir texto y adjuntar una imagen desde la galería
 */
class CrearPublicacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearPublicacionBinding
    private val firebaseManager = FirebaseManager()

    // Códigos de solicitud para identificar respuestas de intents y permisos
    private val PICK_IMAGE_REQUEST = 1001
    private val PERMISSION_REQUEST_CODE = 100
    
    // Variable temporal para almacenar la imagen procesada en bytes
    private var imagenSeleccionada: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPublicacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la barra de acción
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Nueva Publicación"

        // Configurar listener para el botón de Publicar
        binding.btnPublicar.setOnClickListener {
            enviarPublicacion()
        }

        // Configurar listener para el botón de Agregar Foto
        // Inicia el flujo de permisos antes de abrir la galería
        binding.btnAgregarFoto.setOnClickListener {
            verificarPermisosYSeleccionar()
        }

        // Configurar navegación inferior
        setupNavigation()
    }

    /**
     * Verifica si la aplicación tiene los permisos necesarios para acceder al almacenamiento de medios
     * Solicita permisos si no se tienen, o abre la galería si ya están concedidos
     * Maneja diferencias entre versiones de Android (API 33+ vs anteriores)
     */
    private fun verificarPermisosYSeleccionar() {
        // Para Android 13+ (API 33) se usa el permiso granular READ_MEDIA_IMAGES
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_REQUEST_CODE)
            } else {
                seleccionarImagen()
            }
        } 
        // Para versiones anteriores se usa READ_EXTERNAL_STORAGE
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
            } else {
                seleccionarImagen()
            }
        }
    }

    /**
     * Callback que recibe el resultado de la solicitud de permisos
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, procedemos a abrir la galería
                seleccionarImagen()
            } else {
                // Permiso denegado, informamos al usuario
                Toast.makeText(this, "Necesitamos permiso para acceder a tus fotos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Lanza el intent del sistema para seleccionar una imagen de la galería
     */
    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    /**
     * Callback que recibe el resultado de la actividad de selección de imagen
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return

            // Mostrar la imagen seleccionada en la vista previa
            binding.imgPreview.setImageURI(uri)
            binding.imgPreview.visibility = android.view.View.VISIBLE

            // Convertir la imagen a un array de bytes optimizado para subir a Firebase
            imagenSeleccionada = getBytesFromUri(uri)
        }
    }

    /**
     * Lee la imagen desde la URI, la redimensiona y la comprime
     * @param uri La URI de la imagen seleccionada
     * @return ByteArray conteniendo la imagen comprimida en formato JPEG
     */
    private fun getBytesFromUri(uri: android.net.Uri): ByteArray {
        // Decodificar el Bitmap según la versión de Android
        val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= 29) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }

        // REDIMENSIONAR IMAGEN: Firestore tiene un límite estricto de 1MB por documento.
        // Reducr la imagen a un máximo de 1024px de lado para asegurar que quepa y optimizar red.
        val maxDimension = 1024
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val newWidth = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
            val newHeight = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        val stream = ByteArrayOutputStream()
        // Compresión JPEG al 70% para reducir peso manteniendo calidad aceptable
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
        return stream.toByteArray()
    }

    /**
     * Valida los datos y envía la publicación a Firebase a través de FirebaseManager
     */
    private fun enviarPublicacion() {
        val texto = binding.etCuerpoPost.text.toString().trim()

        // Validación: No permitir publicaciones vacías
        if (texto.isEmpty()) {
            binding.etCuerpoPost.error = "No puedes publicar algo vacío"
            return
        }

        // Recuperar información del usuario actual desde SharedPreferences
        val prefs = getSharedPreferences("RedSocialPrefs", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", null)
        val userNombre = prefs.getString("USER_NAME", "Usuario Anónimo")

        if (userId != null) {
            // Validación de seguridad: Verificar tamaño final de la imagen antes de enviar
            if (imagenSeleccionada != null && imagenSeleccionada!!.size > 1000000) {
                Toast.makeText(this, "La imagen es demasiado grande, intenta con otra.", Toast.LENGTH_LONG).show()
                return
            }

            // Delegar la creación a FirebaseManager
            firebaseManager.crearPublicacion(
                userId = userId,
                autorNombre = userNombre ?: "Usuario Anónimo",
                texto = texto,
                imagenBytes = imagenSeleccionada
            )

            Toast.makeText(this, "Publicado correctamente", Toast.LENGTH_SHORT).show()

            // Navegar de vuelta al Feed y limpiar el backstack para evitar volver a esta pantalla al pulsar 'Atrás'
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

    /**
     * Configura los botones de la barra de navegación inferior
     */
    private fun setupNavigation() {
        binding.btnNavHome.setOnClickListener {
            startActivity(Intent(this, FeedActivity::class.java))
            finish()
        }

        binding.btnNavCrear.setOnClickListener {
            Toast.makeText(this, "Ya estás creando una publicación", Toast.LENGTH_SHORT).show()
        }

        binding.btnNavPerfil.setOnClickListener {
            Toast.makeText(this, "Perfil próximamente...", Toast.LENGTH_SHORT).show()
        }
    }
}
