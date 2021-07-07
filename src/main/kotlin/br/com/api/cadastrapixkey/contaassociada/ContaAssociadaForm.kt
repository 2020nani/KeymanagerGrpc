package br.com.api.cadastrapixkey.contaassociada

data class ContaAssociadaForm(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularContaResponse
) {
    fun converte(): ContaAssociada {
      return ContaAssociada(
          instituicao = this.instituicao.nome,
          nomeDoTitular = this.titular.nome,
          cpfDoTitular = this.titular.cpf,
          agencia = this.agencia,
          numeroDaConta = this.numero
      )
    }

}
