package br.com.api.removepixkey

import br.com.api.compartilhado.exception.ErrorHandler
import br.com.api.ExcluiPixKeyResponse
import br.com.api.PixKeyChaveRequest
import br.com.api.RemovePixKeyServiceGrpc
import br.com.api.cadastrapixkey.NovaChavePixRepository
import br.com.api.cadastrapixkey.NovaPixKeyEndpoint
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemovePixKeyEndpoint(
    @Inject val removePixKeyService: RemovePixKeyService,
) : RemovePixKeyServiceGrpc.RemovePixKeyServiceImplBase() {

    val logger = LoggerFactory.getLogger(RemovePixKeyEndpoint::class.java)

    override fun excluiPixKey(request: PixKeyChaveRequest?, responseObserver: StreamObserver<ExcluiPixKeyResponse>?) {
        logger.info("Iniciando requisicao")

        removePixKeyService.removeChavePix(request!!.pixId, request!!.idCliente)

        responseObserver?.onNext(
            ExcluiPixKeyResponse.newBuilder()
                .setMessage("Chave Pix Deletada com sucesso")
                .build()
        )

        responseObserver?.onCompleted()

    }
}