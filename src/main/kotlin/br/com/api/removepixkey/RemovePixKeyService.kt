package br.com.api.removepixkey

import br.com.api.cadastrapixkey.NovaChavePixRepository
import br.com.api.compartilhado.ChavePixNaoExistenteException
import br.com.api.compartilhado.ValidUUID
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemovePixKeyService(
    @Inject val repository: NovaChavePixRepository
) {
    val logger = LoggerFactory.getLogger(RemovePixKeyService::class.java)

    @Transactional
    fun removeChavePix(@NotBlank @ValidUUID pixId: String,
                       @NotBlank @ValidUUID clienteId: String) {
        //verifica se chave existe no sistema
        if (!repository.existsByIdAndClienteId(UUID.fromString(pixId),UUID.fromString(clienteId))) {
            logger.info("Nao existe uma chave-pix cadastrada para a chave passada na requisicao")
            throw ChavePixNaoExistenteException("Nao existe uma chave-pix cadastrada ou a chave nao pertence ao cliente ")
        }

        repository.deleteById(UUID.fromString(pixId))
        logger.info("Chave deletada com sucesso")
    }

}
