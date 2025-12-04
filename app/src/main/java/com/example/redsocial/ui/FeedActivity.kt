package com.example.redsocial.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redsocial.databinding.ActivityFeedBinding
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * Actividad principal de la aplicación, que muestra el Feed de noticias (timeline)
 * Se encarga de cargar y mostrar las publicaciones de todos los usuarios en orden cronológico inverso
 */
class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        cargarPublicaciones()
        setupNavigation()
    }

    /**
     * Inicializa el RecyclerView y su adaptador para mostrar la lista de posts
     */
    private fun setupRecyclerView() {
        adapter = PostAdapter(emptyList())
        binding.rvFeed.layoutManager = LinearLayoutManager(this)
        binding.rvFeed.adapter = adapter
    }

    /**
     * Escucha en tiempo real la colección de "publicaciones" en Firestore
     * Actualiza la lista automáticamente cada vez que se agrega un nuevo post
     * Convierte los datos crudos de Firestore a objetos Post para el adaptador
     */
    private fun cargarPublicaciones() {
        db.collection("publicaciones")
            .orderBy("creadoEl", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                if (snapshots == null || snapshots.isEmpty) {
                    adapter.updateData(emptyList())
                    return@addSnapshotListener
                }

                val listaPosts = mutableListOf<Post>()

                for (doc in snapshots) {
                    val data = doc.data

                    val autor = data["autorNombre"] as? String ?: "Desconocido"
                    val texto = data["texto"] as? String ?: ""
                    
                    // Recuperar como Blob y convertir a ByteArray para mostrar la imagen
                    val blob = data["imagenBytes"] as? com.google.firebase.firestore.Blob
                    val imagenBytes = blob?.toBytes()
                    
                    val fecha = (data["creadoEl"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L

                    listaPosts.add(
                        Post(
                            id = doc.id,
                            author = autor,
                            content = texto,
                            timestamp = fecha,
                            imagenBytes = imagenBytes
                        )
                    )
                }

                adapter.updateData(listaPosts)
            }
    }

    /**
     * Configura la navegación inferior para moverse entre las pantallas principales (Home, Crear, Perfil)
     */
    private fun setupNavigation() {

        binding.btnNavHome.setOnClickListener {
            // Al pulsar Home, scrolleamos al inicio para ver lo más nuevo
            binding.rvFeed.smoothScrollToPosition(0)
        }

        binding.btnNavCrear.setOnClickListener {
            val intent = Intent(this, CrearPublicacionActivity::class.java)
            startActivity(intent)
        }

        binding.btnNavPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }
    }
}
