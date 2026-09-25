package com.oliveira.osapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oliveira.osapp.R
import com.oliveira.osapp.model.OrdemServico
import com.oliveira.osapp.model.StatusOS

class OSAdapter(
    private var itens: List<OrdemServico>,
    private val onClick: (OrdemServico) -> Unit
) : RecyclerView.Adapter<OSAdapter.VH>() {

    class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val textCliente: android.widget.TextView = view.findViewById(R.id.textCliente)
        val textEndereco: android.widget.TextView = view.findViewById(R.id.textEndereco)
        val textStatus: android.widget.TextView = view.findViewById(R.id.textStatus)
        val viewStatus: android.view.View = view.findViewById(R.id.viewStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_os, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val os = itens[position]
        holder.textCliente.text = os.clienteNome.ifBlank { "(sem nome)" }
        holder.textEndereco.text = os.clienteEndereco
        val (label, cor) = when (os.status) {
            StatusOS.ABERTA -> "Aberta" to R.color.status_aberta
            StatusOS.EM_ANDAMENTO -> "Em andamento" to R.color.status_andamento
            StatusOS.CONCLUIDA -> "Concluída" to R.color.status_concluida
        }
        holder.textStatus.text = label
        holder.viewStatus.setBackgroundColor(holder.itemView.context.getColor(cor))
        holder.itemView.setOnClickListener { onClick(os) }
    }

    override fun getItemCount() = itens.size

    fun atualizar(novos: List<OrdemServico>) {
        itens = novos
        notifyDataSetChanged()
    }
}
