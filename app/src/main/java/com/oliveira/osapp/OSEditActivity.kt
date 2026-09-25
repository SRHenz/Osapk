package com.oliveira.osapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.oliveira.osapp.data.OSStorage
import com.oliveira.osapp.data.Preferencias
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
    private var webViewImpressao: WebView? = null

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
        findViewById<View>(R.id.btnImprimir).setOnClickListener {
            salvar()
            imprimirOuExportarPdf()
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
        return OrdemServico(
            dataCriacao = formatoData.format(Date()),
            tecnicoResponsavel = Preferencias(this).nomeTecnico
        )
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

    /** Usa o sistema de impressão nativo do Android: a mesma tela deixa o usuário
     *  escolher "Salvar como PDF" ou uma impressora de verdade (Wi-Fi/Bluetooth). */
    private fun imprimirOuExportarPdf() {
        val webView = WebView(this)
        webViewImpressao = webView
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                val printManager = getSystemService(PRINT_SERVICE) as PrintManager
                val nomeJob = "OS_${os.clienteNome.ifBlank { "sem_nome" }}"
                val adapter = view.createPrintDocumentAdapter(nomeJob)
                printManager.print(
                    nomeJob,
                    adapter,
                    PrintAttributes.Builder().build()
                )
            }
        }
        webView.loadDataWithBaseURL(null, gerarHtmlDaOS(), "text/html", "UTF-8", null)
    }

    private fun gerarHtmlDaOS(): String {
        fun escapar(texto: String) = texto
            .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\n", "<br>")

        val linhasMateriais = if (os.materiais.isEmpty()) {
            "<tr><td colspan=\"2\" style=\"color:#777\">Nenhum material informado</td></tr>"
        } else {
            os.materiais.joinToString("") { m ->
                "<tr><td>${escapar(m.nome)}</td><td>${escapar(m.quantidade)}</td></tr>"
            }
        }

        return """
            <html>
            <head>
              <meta charset="utf-8">
              <style>
                body { font-family: sans-serif; color: #1B2B27; padding: 24px; }
                h1 { color: #1E6E62; font-size: 20px; margin-bottom: 0; }
                .subtitulo { color: #777; margin-top: 4px; margin-bottom: 24px; }
                h2 { font-size: 14px; color: #1E6E62; border-bottom: 1px solid #ddd; padding-bottom: 4px; margin-top: 20px; }
                table { width: 100%; border-collapse: collapse; margin-top: 8px; }
                td, th { border: 1px solid #ddd; padding: 6px 8px; text-align: left; font-size: 13px; }
                .campo { margin: 4px 0; font-size: 14px; }
                .rotulo { color: #666; font-size: 12px; }
              </style>
            </head>
            <body>
              <h1>Ordem de Serviço</h1>
              <div class="subtitulo">Criada em ${escapar(os.dataCriacao)} · Status: ${statusLegivel(os)}</div>

              <h2>Cliente</h2>
              <div class="campo"><span class="rotulo">Nome:</span> ${escapar(os.clienteNome)}</div>
              <div class="campo"><span class="rotulo">Telefone:</span> ${escapar(os.clienteTelefone)}</div>
              <div class="campo"><span class="rotulo">Endereço:</span> ${escapar(os.clienteEndereco)}</div>

              <h2>Solicitação do serviço</h2>
              <div class="campo">${escapar(os.solicitacaoDescricao).ifBlank { "-" }}</div>

              <h2>Conclusão do serviço</h2>
              <div class="campo">${escapar(os.execucaoDescricao).ifBlank { "-" }}</div>
              <div class="campo"><span class="rotulo">Técnico responsável:</span> ${escapar(os.tecnicoResponsavel)}</div>
              <div class="campo"><span class="rotulo">Data de conclusão:</span> ${escapar(os.dataConclusao).ifBlank { "-" }}</div>

              <h2>Materiais utilizados</h2>
              <table>
                <tr><th>Material</th><th>Quantidade</th></tr>
                $linhasMateriais
              </table>
            </body>
            </html>
        """.trimIndent()
    }

    private fun statusLegivel(os: OrdemServico): String = when (os.status) {
        StatusOS.ABERTA -> "Aberta"
        StatusOS.EM_ANDAMENTO -> "Em andamento"
        StatusOS.CONCLUIDA -> "Concluída"
    }
}
