package com.example.redsocial.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.redsocial.databinding.ItemComentarioBinding

/**
 * Adaptador para mostrar la lista de comentarios en el RecyclerView de ComentariosActivity
 */
class ComentariosAdapter(private var lista: List<Comentario>) :
    RecyclerView.Adapter<ComentariosAdapter.ViewHolder>() {

    /**
     * ViewHolder que mantiene las referencias a las vistas de cada item de comentario
     * Utiliza ViewBinding para acceder a los elementos de la UI
     */
    class ViewHolder(val binding: ItemComentarioBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * Crea una nueva vista para un elemento de la lista
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemComentarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * Vincula los datos de un comentario con las vistas del ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.binding.tvAutorComentario.text = item.autorNombre
        holder.binding.tvTextoComentario.text = item.texto
    }

    /**
     * Devuelve el número total de comentarios en la lista
     */
    override fun getItemCount() = lista.size

    /**
     * Actualiza la lista de comentarios y notifica al adaptador para refrescar la vista
     */
    fun updateData(nuevaLista: List<Comentario>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
