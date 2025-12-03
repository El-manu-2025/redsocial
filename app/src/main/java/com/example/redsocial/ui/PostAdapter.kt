package com.example.redsocial.ui

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.redsocial.databinding.ItemPostBinding // Asegúrate que este import funcione

// Modelo de datos simple
data class Post(
    val id: String = "",
    val author: String = "",
    val content: String = "",
    val timestamp: Long = 0
)

class PostAdapter(private var posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Asignar los textos
        holder.binding.tvAutorPost.text = post.author
        holder.binding.tvContenidoPost.text = post.content

        // Convertir fecha a "Hace x minutos"
        val timeAgo = DateUtils.getRelativeTimeSpanString(post.timestamp)
        holder.binding.tvFechaPost.text = timeAgo
    }

    override fun getItemCount() = posts.size

    // Función para actualizar la lista cuando llegan datos nuevos
    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
