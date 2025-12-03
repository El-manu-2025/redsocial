package com.example.redsocial

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Blob

// 1. Modelo para Usuarios
data class Usuario(
    val nombre: String = "",
    val email: String = "",
    // Nota: Guardar imágenes como Blob (bytes) no es lo ideal para apps grandes,
    // pero sirve para tu ejercicio.
    val fotoPerfilBlob: Blob? = null,
    val creadoEl: Timestamp = Timestamp.now()
)

// 2. Modelo para Publicaciones
data class Publicacion(
    val userId: String = "",
    val texto: String = "",
    val imagenBlob: Blob? = null,
    val creadoEl: Timestamp = Timestamp.now()
)

// 3. Modelo para Comentarios
data class Comentario(
    val postId: String = "",
    val userId: String = "",
    val texto: String = "",
    val creadoEl: Timestamp = Timestamp.now()
)