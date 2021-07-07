package br.com.api.cadastrapixkey.contaassociada

import javax.persistence.Embeddable

@Embeddable
class ContaAssociada (
    val instituicao: String,
    val nomeDoTitular: String,
    val cpfDoTitular: String,
    val agencia: String,
    val numeroDaConta: String
        ) {


}