package br.com.api.cadastrapixkey

import br.com.api.cadastrapixkey.contaassociada.ClientContaAssociada
import br.com.api.compartilhado.ChavePixExistenteException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class CadastraNovaChavePixService(
    @Inject val novaChavePixRepository: NovaChavePixRepository,
    @Inject val clientContaAssociada: ClientContaAssociada
) {
    val logger = LoggerFactory.getLogger(CadastraNovaChavePixService::class.java)

    @Transactional
    fun registraChave(@Valid chavePix: NovaChavePix): ChavePix {
        //verifica se chave existe no sistema
        if (novaChavePixRepository.existsByChave(chavePix.chave)) {
            logger.info("Ja existe uma chave-pix cadastrada para a chave passada na requisicao")
            throw ChavePixExistenteException("Ja existe uma chave-pix cadastrada para a chave ${chavePix.chave} ")

        }
        //busca dados do cliente no ERP do ITAU

        val dadosCliente = clientContaAssociada.buscaDadosCliente(chavePix.clienteId!!, chavePix.tipoConta!!.name)
        val conta =
            dadosCliente.body()?.converte() ?: throw IllegalStateException("Dados da conta nao foram encontrado")
        logger.info("Busca dos dados ERP ITAU concluida com sucesso")

        //cadastra ChavePix
        val chave = chavePix.converte(conta)
        novaChavePixRepository.save(chave)
        logger.info("Chave cadastrada com sucesso no banco de dados")
        return chave
    }

}
