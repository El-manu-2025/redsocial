package com.example.redsocial.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.redsocial.databinding.ItemPostBinding

/**
 * Modelo de datos interno para el adaptador de publicaciones.
 * Representa la información necesaria para renderizar la tarjeta de publicación en el feed
 */
data class Post(
    val id: String = "",
    val author: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    val imagenBytes: ByteArray? = null
)

/**
 * Adaptador de RecyclerView encargado de mostrar la lista de publicaciones en el feed principal
 */
class PostAdapter(private var posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    /**
     * ViewHolder que encapsula las vistas de un item de publicación usando ViewBinding
     */
    class PostViewHolder(val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Infla el layout XML para un item de publicación
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    /**
     * Enlaza los datos de un objeto Post con las vistas correspondientes
     * Se encarga de:
     * - Mostrar texto del autor y contenido
     * - Formatear la fecha relativa (hace x minutos)
     * - Decodificar y mostrar la imagen si existe
     * - Configurar el listener para ir a los comentarios
     */
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Asignar nombre del autor
        holder.binding.tvAutorPost.text = post.author

        // Asignar texto del post
        holder.binding.tvContenidoPost.text = post.content

        // Calcular y mostrar fecha relativa
        val timeAgo = if (post.timestamp > 0) {
            DateUtils.getRelativeTimeSpanString(post.timestamp)
        } else {
            "Reciente"
        }
        holder.binding.tvFechaPost.text = timeAgo

        // Lógica para mostrar u ocultar la imagen
        if (post.imagenBytes != null) {
            // Decodificar bytes a Bitmap para mostrar en ImageView
            val bmp = BitmapFactory.decodeByteArray(post.imagenBytes, 0, post.imagenBytes.size)
            holder.binding.imgPost.visibility = View.VISIBLE
            holder.binding.imgPost.setImageBitmap(bmp)
        } else {
            // Si no hay imagen, ocultar el ImageView para ahorrar espacio
            holder.binding.imgPost.visibility = View.GONE
            holder.binding.imgPost.setImageBitmap(null)
        }

        // Configurar botón de comentarios para navegar a ComentariosActivity
        holder.binding.btnComentar.setOnClickListener {
            val ctx = holder.itemView.context
            val intent = Intent(ctx, ComentariosActivity::class.java)
            // Pasar el ID del post para cargar sus comentarios
            intent.putExtra("POST_ID", post.id)
            ctx.startActivity(intent)
        }
    }

    /**
     * Retorna la cantidad de items en la lista
     */
    override fun getItemCount() = posts.size

    /**
     * Actualiza la lista de publicaciones y refresca toda la vista
     */
    fun updateData(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
