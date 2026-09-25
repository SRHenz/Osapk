package com.oliveira.osapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.oliveira.osapp.data.OSStorage
import com.oliveira.osapp.model.Material
import com.oliveira.osapp.model.OrdemServico
import com.oliveira.osapp.model.StatusOS
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OSEditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OS_ID = "extra_os_id"
    }

    private lateinit var storage: OSStorage
    private lateinit var os: OrdemServico
    private lateinit var containerMateriais: LinearLayout

    private lateinit var editClienteNome: TextInputEditText
    private lateinit var editClienteTelefone: TextInputEditText
    private lateinit var editClienteEndereco: TextInputEditText
    private lateinit var editSolicitacao: TextInputEditText
    private lateinit var editExecucao: TextInputEditText
    private lateinit var editTecnico: TextInputEditText

    private val formatoData = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_os_edit)
        storage = OSStorage(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        editClienteNome = findViewById(R.id.editClienteNome)
        editClienteTelefone = findViewById(R.id.editClienteTelefone)
        editClienteEndereco = findViewById(R.id.editClienteEndereco)
        editSolicitacao = findViewById(R.id.editSolicitacao)
        editExecucao = findViewById(R.id.editExecucao)
        editTecnico = findViewById(R.id.editTecnico)
        containerMateriais = findViewById(R.id.containerMateriais)

        os = resolverOrigemDaOS()
        toolbar.title = if (os.clienteNome.isBlank()) "Nova OS" else "OS - ${os.clienteNome}"
        preencherCampos()

        findViewById<View>(R.id.btnAddMaterial).setOnClickListener {
            adicionarLinhaMaterial(Material())
        }
        findViewById<View>(R.id.btnSalvar).setOnClickListener {
            salvar()
            Toast.makeText(this, "OS salva localmente", Toast.LENGTH_SHORT).show()
            finish()
        }
        findViewById<View>(R.id.btnCompartilhar).setOnClickListener {
            salvar()
            compartilhar()
        }
    }

    /** Decide se a OS vem de um arquivo .osrv aberto de fora (ACTION_VIEW),
     *  recebida pelo botão "Compartilhar" do WhatsApp (ACTION_SEND),
     *  da lista local, ou se é uma OS nova. */
    private fun resolverOrigemDaOS(): OrdemServico {
        val uriRecebida: Uri? = when (intent?.action) {
            Intent.ACTION_VIEW -> intent?.data
            Intent.ACTION_SEND -> intent?.getParcelableExtra(Intent.EXTRA_STREAM)
            else -> null
        }
        if (uriRecebida != null) {
            return try {
                storage.importar(uriRecebida)
            } catch (e: Exception) {
                Toast.makeText(this, "Não foi possível ler o arquivo .osrv", Toast.LENGTH_LONG).show()
                OrdemServico(dataCriacao = formatoData.format(Date()))
            }
        }
        val idExistente = intent?.getStringExtra(EXTRA_OS_ID)
        if (idExistente != null) {
            val arquivo = File(storage.pasta, "$idExistente.osrv")
            if (arquivo.exists()) return storage.carregar(arquivo)
        }
        return OrdemServico(dataCriacao = formatoData.format(Date()))
    }

    private fun preencherCampos() {
        editClienteNome.setText(os.clienteNome)
        editClienteTelefone.setText(os.clienteTelefone)
        editClienteEndereco.setText(os.clienteEndereco)
        editSolicitacao.setText(os.solicitacaoDescricao)
        editExecucao.setText(os.execucaoDescricao)
        editTecnico.setText(os.tecnicoResponsavel)

        containerMateriais.removeAllViews()
        if (os.materiais.isEmpty()) {
            adicionarLinhaMaterial(Material())
        } else {
            os.materiais.forEach { adicionarLinhaMaterial(it) }
        }
    }

    private fun adicionarLinhaMaterial(material: Material) {
        val linha = LayoutInflater.from(this).inflate(R.layout.item_material, containerMateriais, false)
        val editNome = linha.findViewById<EditText>(R.id.editNomeMaterial)
        val editQtd = linha.findViewById<EditText>(R.id.editQtdMaterial)
        editNome.setText(material.nome)
        editQtd.setText(material.quantidade)
        linha.findViewById<View>(R.id.btnRemoverMaterial).setOnClickListener {
            containerMateriais.removeView(linha)
        }
        containerMateriais.addView(linha)
    }

    private fun coletarMateriais(): MutableList<Material> {
        val lista = mutableListOf<Material>()
        for (i in 0 until containerMateriais.childCount) {
            val linha = containerMateriais.getChildAt(i)
            val nome = linha.findViewById<EditText>(R.id.editNomeMaterial).text.toString().trim()
            val qtd = linha.findViewById<EditText>(R.id.editQtdMaterial).text.toString().trim()
            if (nome.isNotEmpty()) lista.add(Material(nome, qtd))
        }
        return lista
    }

    private fun salvar() {
        os.clienteNome = editClienteNome.text.toString().trim()
        os.clienteTelefone = editClienteTelefone.text.toString().trim()
        os.clienteEndereco = editClienteEndereco.text.toString().trim()
        os.solicitacaoDescricao = editSolicitacao.text.toString().trim()
        if (os.dataSolicitacao.isBlank() && os.solicitacaoDescricao.isNotBlank()) {
            os.dataSolicitacao = formatoData.format(Date())
        }
        os.execucaoDescricao = editExecucao.text.toString().trim()
        os.tecnicoResponsavel = editTecnico.text.toString().trim()
        os.materiais = coletarMateriais()

        os.status = when {
            os.execucaoDescricao.isNotBlank() -> {
                if (os.dataConclusao.isBlank()) os.dataConclusao = formatoData.format(Date())
                StatusOS.CONCLUIDA
            }
            os.solicitacaoDescricao.isNotBlank() -> StatusOS.EM_ANDAMENTO
            else -> StatusOS.ABERTA
        }

        storage.salvar(os)
    }

    private fun compartilhar() {
        val uri = storage.uriParaCompartilhar(os)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Compartilhar OS"))
    }
}
