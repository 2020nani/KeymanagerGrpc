package br.com.api.cadastrapixkey

import br.com.api.cadastrapixkey.contaassociada.ContaAssociada
import br.com.api.compartilhado.ValidPixKey
import br.com.api.compartilhado.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(
    @field:ValidUUID
    @field:NotBlank
    val clienteId: String?,

    @field:NotNull
    val tipoChave: TipoDeChave?,

    @field:Size(max = 77)
    val chave: String?,

    @field:NotNull
    val tipoConta: TipoDeConta?
) {
    fun converte(conta: ContaAssociada): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipoChave = TipoDeChave.valueOf(this.tipoChave!!.name),
            chave = if (this.tipoChave == TipoDeChave.ALEATORIA) UUID.randomUUID().toString() else this.chave!!,
            tipoConta = TipoDeConta.valueOf(this.tipoConta!!.name),
            conta = conta
        )
    }

}
