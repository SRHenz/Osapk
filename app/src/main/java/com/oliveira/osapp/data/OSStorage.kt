package com.oliveira.osapp.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.GsonBuilder
import com.oliveira.osapp.model.OrdemServico
import java.io.File

/**
 * Cada Ordem de Serviço é o próprio arquivo .osrv (JSON) salvo em
 * filesDir/ordens/<id>.osrv. Isso resolve "salvamento local" e
 * "arquivo próprio compartilhável" com a mesma fonte de dados.
 */
class OSStorage(private val context: Context) {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    val pasta: File
        get() {
            val f = File(context.filesDir, "ordens")
            if (!f.exists()) f.mkdirs()
            return f
        }

    fun listar(): List<File> =
        pasta.listFiles { file -> file.name.endsWith(".osrv") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

    fun salvar(os: OrdemServico): File {
        val arquivo = File(pasta, "${os.id}.osrv")
        arquivo.writeText(gson.toJson(os))
        return arquivo
    }

    fun carregar(arquivo: File): OrdemServico =
        gson.fromJson(arquivo.readText(), OrdemServico::class.java)

    fun carregarDeTexto(texto: String): OrdemServico =
        gson.fromJson(texto, OrdemServico::class.java)

    /** Copia um .osrv recebido de fora (ex: baixado do WhatsApp) para a pasta local. */
    fun importar(uri: Uri): OrdemServico {
        val texto = context.contentResolver.openInputStream(uri)!!
            .bufferedReader().use { it.readText() }
        val os = carregarDeTexto(texto)
        salvar(os)
        return os
    }

    fun excluir(os: OrdemServico) {
        File(pasta, "${os.id}.osrv").delete()
    }

    /** Uri via FileProvider, pronta para ser usada num Intent.ACTION_SEND (ex: WhatsApp). */
    fun uriParaCompartilhar(os: OrdemServico): Uri {
        val arquivo = salvar(os)
        return FileProvider.getUriForFile(
            context,
            "com.oliveira.osapp.fileprovider",
            arquivo
        )
    }
}
