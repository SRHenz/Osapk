package com.oliveira.osapp.model

import java.util.UUID

data class Material(
    var nome: String = "",
    var quantidade: String = ""
)

data class HistoricoItem(
    var data: String = "",
    var descricao: String = ""
)

enum class StatusOS {
    ABERTA, EM_ANDAMENTO, CONCLUIDA
}

data class OrdemServico(
    var id: String = UUID.randomUUID().toString(),
    var dataCriacao: String = "",
    var status: StatusOS = StatusOS.ABERTA,

    // Cliente
    var clienteNome: String = "",
    var clienteTelefone: String = "",
    var clienteEndereco: String = "",

    // Solicitação
    var solicitacaoDescricao: String = "",
    var dataSolicitacao: String = "",

    // Execução / conclusão
    var execucaoDescricao: String = "",
    var materiais: MutableList<Material> = mutableListOf(),
    var dataConclusao: String = "",
    var tecnicoResponsavel: String = "",

    var historico: MutableList<HistoricoItem> = mutableListOf()
)
