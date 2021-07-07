package br.com.api.cadastrapixkey

import br.com.api.KeyManagerGrpcRequest
import br.com.api.KeyManagerGrpcServiceGrpc
import br.com.api.TipoChave
import br.com.api.TipoConta
import br.com.api.cadastrapixkey.contaassociada.*
import com.api.utils.violations

import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastraNovaChavePixServiceTest(
    val repository: NovaChavePixRepository,
    val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub
) {
    @Inject
    lateinit var clientContaAssociada: ClientContaAssociada

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve cadastrar chave Pix`() {

        //cenario
        `when`(clientContaAssociada.buscaDadosCliente(clienteId = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(contaAssociadaForm()))

        //acao
        val response = grpcClient.cadastraPixKey(
            KeyManagerGrpcRequest.newBuilder()
                .setIdCliente(CLIENT_ID.toString())
                .setTipodechave(TipoChave.EMAIL)
                .setChave("rponte@gmail.com")
                .setTipodeconta(TipoConta.CONTA_CORRENTE)
                .build())

        //validacao
        with(response) {
            assertEquals(CLIENT_ID.toString(), idCliente)
            assertNotNull(pixId)
        }

    }

    @Test
    fun `nao deve cadastrar chave Pix ja existente`() {

        // cenário
        repository.save(chave(
            tipo = TipoDeChave.CPF,
            chave = "63657520325",
            clienteId = CLIENT_ID
        ))

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastraPixKey(
                KeyManagerGrpcRequest.newBuilder()
                    .setIdCliente(CLIENT_ID.toString())
                    .setTipodechave(TipoChave.CPF)
                    .setChave("63657520325")
                    .setTipodeconta(TipoConta.CONTA_CORRENTE)
                    .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Ja existe uma chave-pix cadastrada para a chave 63657520325 ",status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando nao encontrar dados da conta cliente`() {
        // cenário
        `when`(clientContaAssociada.buscaDadosCliente(clienteId = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastraPixKey(
                KeyManagerGrpcRequest.newBuilder()
                    .setIdCliente(CLIENT_ID.toString())
                    .setTipodechave(TipoChave.EMAIL)
                    .setChave("rponte@gmail.com")
                    .setTipodeconta(TipoConta.CONTA_CORRENTE)
                    .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Dados da conta nao foram encontrado", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastraPixKey(KeyManagerGrpcRequest.newBuilder().build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("clienteId", "não deve estar em branco"),
                Pair("clienteId", "não é um formato válido de UUID"),
                Pair("tipoConta", "não deve ser nulo"),
                Pair("tipoChave", "não deve ser nulo"),
            ))
        }
    }

    /**
     * Cenário básico de validação de chave para garantir que estamos validando a
     * chave via @ValidPixKey. Lembrando que os demais cenários são validados via testes
     * de unidade.
     */
    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos - chave invalida`() {
        // ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastraPixKey(
                KeyManagerGrpcRequest.newBuilder()
                    .setIdCliente(CLIENT_ID.toString())
                    .setTipodechave(TipoChave.CPF)
                    .setChave("378.930.cpf-invalido.389-73")
                    .setTipodeconta(TipoConta.CONTA_POUPANCA)
                    .build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(violations(), containsInAnyOrder(
                Pair("chave", "chave Pix inválida (CPF)"),
            ))
        }
    }

    @MockBean(ClientContaAssociada::class)
    fun enderecoClientMock(): ClientContaAssociada? {
        return Mockito.mock(ClientContaAssociada::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub {
            return KeyManagerGrpcServiceGrpc.newBlockingStub((channel))

        }
    }

    private fun contaAssociadaForm(): ContaAssociadaForm {
        return ContaAssociadaForm(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", null),
            agencia = "1218",
            numero = "291900",
            titular = TitularContaResponse("Rafael Ponte", "63657520325","02467781054")
        )
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