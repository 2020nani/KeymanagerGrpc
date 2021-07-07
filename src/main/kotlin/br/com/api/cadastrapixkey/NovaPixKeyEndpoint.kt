package br.com.api.cadastrapixkey

import br.com.api.*
import br.com.api.cadastrapixkey.contaassociada.ClientContaAssociada
import br.com.api.compartilhado.exception.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class NovaPixKeyEndpoint(
    @Inject val cadastraNovaChavePixService: CadastraNovaChavePixService
) : KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {
    val logger = LoggerFactory.getLogger(NovaPixKeyEndpoint::class.java)
    override fun cadastraPixKey(
        request: KeyManagerGrpcRequest?,
        responseObserver: StreamObserver<KeyManagerGrpcResponse>?
    ) {
        logger.info("Iniciando Requisicao")

        val chavePix: NovaChavePix = request.converte();

        logger.info("NovaChaveKey convertida com sucesso ")

        val chaveCriada = cadastraNovaChavePixService.registraChave(chavePix)

        responseObserver?.onNext(KeyManagerGrpcResponse.newBuilder()
           .setIdCliente(chaveCriada.clienteId.toString())
            .setPixId(chaveCriada.id.toString())
            .build())
        responseObserver?.onCompleted()
    }
}

private fun KeyManagerGrpcRequest?.converte(): NovaChavePix {
    return NovaChavePix(
        clienteId = this?.idCliente,
        tipoChave = when (this!!.tipodechave) {
            TipoChave.UNKNOW_CHAVE -> null
            else -> TipoDeChave.valueOf(this.tipodechave.name)
        },
        chave = chave,
        tipoConta = when(this!!.tipodeconta) {
            TipoConta.UNKNOW_CONTA -> null
            else -> TipoDeConta.valueOf(this.tipodeconta.name)
        }
    )
}
