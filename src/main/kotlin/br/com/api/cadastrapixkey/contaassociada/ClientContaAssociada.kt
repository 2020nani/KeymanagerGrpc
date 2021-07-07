package br.com.api.cadastrapixkey.contaassociada
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


@Client("\${itau.contas.url}")
interface ClientContaAssociada {
    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscaDadosCliente(@PathVariable("clienteId") clienteId: String, @QueryValue tipo: String): HttpResponse<ContaAssociadaForm>
}