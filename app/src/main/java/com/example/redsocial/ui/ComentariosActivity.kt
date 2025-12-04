package com.example.redsocial.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redsocial.databinding.ActivityComentariosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Actividad para visualizar y agregar comentarios a una publicación específica
 */
class ComentariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComentariosBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: ComentariosAdapter
    private var postId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComentariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Recibimos el ID del post desde el intent que llamó a esta actividad
        postId = intent.getStringExtra("POST_ID") ?: ""

        // Validación: No podemos cargar comentarios sin un ID de post válido
        if (postId.isEmpty()) {
            Toast.makeText(this, "Error al cargar post", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        cargarComentarios()

        // 2. Configurar el botón enviar
        binding.btnEnviarComentario.setOnClickListener {
            enviarComentario()
        }
    }

    /**
     * Configura el RecyclerView con su adaptador y layout manager
     */
    private fun setupRecyclerView() {
        adapter = ComentariosAdapter(emptyList())
        binding.rvComentarios.layoutManager = LinearLayoutManager(this)
        binding.rvComentarios.adapter = adapter
    }

    /**
     * Envía un nuevo comentario a Firestore asociado al post actual
     * Obtiene primero el nombre del usuario actual para guardarlo junto al comentario
     */
    private fun enviarComentario() {
        val texto = binding.etComentario.text.toString().trim()
        val userId = auth.currentUser?.uid

        // Validaciones básicas
        if (texto.isEmpty()) return
        if (userId == null) return

        binding.btnEnviarComentario.isEnabled = false

        // Obtener el nombre del usuario desde la colección "usuarios"
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { doc ->
                val nombreAutor = doc.getString("nombre") ?: "Usuario"

                // Crear el mapa de datos del comentario
                val comentarioData = hashMapOf(
                    "userId" to userId,
                    "autorNombre" to nombreAutor,
                    "texto" to texto,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                // Guardar en una subcolección "comentarios" dentro del documento de la publicación
                db.collection("publicaciones").document(postId).collection("comentarios")
                    .add(comentarioData)
                    .addOnSuccessListener {
                        binding.etComentario.setText("") 
                        binding.btnEnviarComentario.isEnabled = true
                        // El teclado podría ocultarse aquí si se desea una mejor UX
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al comentar", Toast.LENGTH_SHORT).show()
                        binding.btnEnviarComentario.isEnabled = true
                    }
            }
            .addOnFailureListener { 
                // Fallback en caso de error al obtener el usuario
                binding.btnEnviarComentario.isEnabled = true
                Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Escucha en tiempo real los cambios en la colección de comentarios de este post
     * Actualiza la lista automáticamente cuando llegan nuevos comentarios
     */
    private fun cargarComentarios() {
        db.collection("publicaciones").document(postId).collection("comentarios")
            .orderBy("timestamp", Query.Direction.ASCENDING) // Ordenar cronológicamente (antiguos primero)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                val lista = mutableListOf<Comentario>()
                if (snapshots != null) {
                    for (doc in snapshots) {
                        val c = Comentario(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            autorNombre = doc.getString("autorNombre") ?: "Usuario",
                            texto = doc.getString("texto") ?: ""
                        )
                        lista.add(c)
                    }
                }
                adapter.updateData(lista)

                // Scroll automático al último comentario para ver lo más reciente
                if (lista.isNotEmpty()) {
                    binding.rvComentarios.scrollToPosition(lista.size - 1)
                }
            }
    }
}

/**
 * Modelo de datos simple para representar un comentario en la UI
 */
data class Comentario(
    val id: String = "",
    val userId: String = "",
    val autorNombre: String = "",
    val texto: String = "",
    val timestamp: Any? = null
)
