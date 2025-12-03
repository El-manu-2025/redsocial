package com.example.redsocial.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redsocial.databinding.ActivityFeedBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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

    private fun setupRecyclerView() {
        // Configuramos la lista vacía inicialmente
        adapter = PostAdapter(emptyList())
        binding.rvFeed.layoutManager = LinearLayoutManager(this)
        binding.rvFeed.adapter = adapter
    }

    private fun cargarPublicaciones() {
        // Pedimos los posts a la colección "posts", ordenados por fecha (el más nuevo primero)
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error al cargar: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val listaPosts = mutableListOf<Post>()

                if (snapshots != null) {
                    for (doc in snapshots) {
                        // Convertimos cada documento de Firebase a nuestro objeto Post
                        val post = Post(
                            id = doc.id,
                            author = doc.getString("author") ?: "Anónimo",
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                        listaPosts.add(post)
                    }
                }

                // Actualizamos el adaptador con la nueva lista
                adapter.updateData(listaPosts)
            }
    }

    private fun setupNavigation() {
        // Botón Home
        binding.btnNavHome.setOnClickListener {
            binding.rvFeed.smoothScrollToPosition(0)
        }

        // Botón Crear
        binding.btnNavCrear.setOnClickListener {
            val intent = Intent(this, CrearPublicacionActivity::class.java)
            // No cerramos el feed (finish) para poder volver atrás con el botón físico
            startActivity(intent)
        }

        // Botón Perfil
        binding.btnNavPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }
    }
}
