package com.example.redsocial

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseManager {

    // Instancia de la base de datos
    private val db = FirebaseFirestore.getInstance()

    // ----------------------------------------------------
    // FUNCIÓN 1: GUARDAR UN USUARIO NUEVO
    // ----------------------------------------------------
    fun guardarUsuario(
        idUsuario: String, // El UID que te da Firebase Auth o uno inventado
        nombre: String,
        email: String,
        fotoBytes: ByteArray? = null // La imagen en bytes (opcional)
    ) {
        // Si mandaron foto, la convertimos a Blob. Si no, queda null.
        val blobImagen = if (fotoBytes != null) Blob.fromBytes(fotoBytes) else null

        // Creamos el objeto usando tu modelo 'Usuario'
        val nuevoUsuario = Usuario(
            nombre = nombre,
            email = email,
            fotoPerfilBlob = blobImagen,
            creadoEl = Timestamp.now()
        )

        // Guardamos en la colección "usuarios" usando el ID específico
        db.collection("usuarios").document(idUsuario).set(nuevoUsuario)
            .addOnSuccessListener {
                Log.d("Firebase", "¡Usuario guardado con éxito!")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al guardar usuario", e)
            }
    }

    // ----------------------------------------------------
    // FUNCIÓN 2: GUARDAR UNA PUBLICACIÓN
    // ----------------------------------------------------
    fun crearPublicacion(
        userId: String,
        texto: String,
        imagenBytes: ByteArray? = null
    ) {
        val blobImagen = if (imagenBytes != null) Blob.fromBytes(imagenBytes) else null

        val nuevaPublicacion = Publicacion(
            userId = userId,
            texto = texto,
            imagenBlob = blobImagen,
            creadoEl = Timestamp.now()
        )

        // Usamos .add() para que Firebase genere un ID automático para el post
        db.collection("publicaciones").add(nuevaPublicacion)
            .addOnSuccessListener { documentReference ->
                Log.d("Firebase", "Publicación creada. ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al publicar", e)
            }
    }
    // ----------------------------------------------------
    // FUNCIÓN 3: GUARDAR UN COMENTARIO
    // ----------------------------------------------------
    /**
     * Guarda un nuevo comentario asociado a una publicación específica.
     * @param postId El ID del documento de la publicación padre.
     * @param userId El ID del usuario que escribe el comentario.
     * @param textoComentario El contenido del comentario.
     * @param onResult Callback opcional para saber si se guardó correctamente (Booleano).
     */
    fun crearComentario(
        postId: String,
        userId: String,
        textoComentario: String,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        // Validaciones básicas de seguridad antes de enviar
        if (postId.isBlank() || userId.isBlank() || textoComentario.isBlank()) {
            Log.w("FirebaseManager", "Intento de comentario con datos vacíos")
            onResult?.invoke(false)
            return
        }

        val nuevoComentario = Comentario(
            postId = postId,
            userId = userId,
            texto = textoComentario,
            creadoEl = Timestamp.now()
        )

        // Guardamos en la colección "comentarios"
        db.collection("comentarios")
            .add(nuevoComentario)
            .addOnSuccessListener { documentReference ->
                Log.i("FirebaseManager", "Comentario agregado con ID: ${documentReference.id}")
                onResult?.invoke(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseManager", "Error crítico al agregar comentario", e)
                onResult?.invoke(false)
            }
    }
}
