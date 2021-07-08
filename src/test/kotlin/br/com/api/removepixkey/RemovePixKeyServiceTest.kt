package br.com.api.removepixkey

import br.com.api.PixKeyChaveRequest
import br.com.api.RemovePixKeyServiceGrpc
import br.com.api.cadastrapixkey.*
import br.com.api.cadastrapixkey.CadastraNovaChavePixServiceTest
import br.com.api.cadastrapixkey.contaassociada.ContaAssociada
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemovePixKeyServiceTest(
    val repository: NovaChavePixRepository,
    val grpcClient: RemovePixKeyServiceGrpc.RemovePixKeyServiceBlockingStub
) {

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `nao deve excluir se ClienteId e pixId nao existirem ou nao pertencer ao mesmo usuario`() {
        //cenario
        val chavePix = repository.save(
            chave(
                tipo = TipoDeChave.CPF,
                chave = "63657520325",
                clienteId = CadastraNovaChavePixServiceTest.CLIENT_ID
            )
        )
        //acao
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.excluiPixKey(
                PixKeyChaveRequest.newBuilder()
                    .setIdCliente("8e5379fe-2ccd-42e6-9ce5-afbf0008df03")
                    .setPixId(chavePix.id.toString())
                    .build()
            )
        }
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Nao existe uma chave-pix cadastrada ou a chave nao pertence ao cliente ", status.description)
        }
    }

    @Test
    fun `deve excluir se ClienteId e pixId existirem `() {
        // cenário
        val chavePix = repository.save(
            chave(
                tipo = TipoDeChave.CPF,
                chave = "63657520325",
                clienteId = CadastraNovaChavePixServiceTest.CLIENT_ID
            )
        )

        //acao
        val response = grpcClient.excluiPixKey(
            PixKeyChaveRequest.newBuilder()
                .setIdCliente(chavePix.clienteId.toString())
                .setPixId(chavePix.id.toString())
                .build()
        )
        //validacao

        with(response) {
            assertFalse(repository.existsById(chavePix.id))
        }
    }

    @Test
    fun `nao deve excluir se dados invalidos`() {
        //cenario

        //acao
        val thrown = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.excluiPixKey(
                PixKeyChaveRequest.newBuilder()
                    .setIdCliente("teste-5546352-rteste")
                    .setPixId("teste-teste-ghgr56647")
                    .build()
            )
        }
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RemovePixKeyServiceGrpc.RemovePixKeyServiceBlockingStub {
            return RemovePixKeyServiceGrpc.newBlockingStub((channel))

        }
    }

    private fun chave(
        tipo: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipoChave = tipo,
            chave = chave,
            tipoConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "63657520325",
                agencia = "1218",
                numeroDaConta = "291900"
            )
        )
    }
}