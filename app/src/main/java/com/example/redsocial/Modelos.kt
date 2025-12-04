package com.example.redsocial

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Blob


 // Modelo de datos que representa un Usuario en la aplicación

data class Usuario(
    val nombre: String = "",
    val email: String = "",
    val fotoPerfilBlob: Blob? = null,
    val creadoEl: Timestamp = Timestamp.now()
)

/**
 * Modelo de datos que representa una Publicación en el feed
 */
data class Publicacion(
    val userId: String = "",
    val autorNombre: String = "",
    val texto: String = "",
    val imagenBytes: ByteArray? = null,
    val creadoEl: Timestamp = Timestamp.now()
)

/**
 * Modelo de datos que representa un Comentario en una publicación

 */
data class Comentario(
    val postId: String = "",
    val userId: String = "",
    val autorNombre: String = "",
    val texto: String = "",
    val creadoEl: Timestamp = Timestamp.now()
)
