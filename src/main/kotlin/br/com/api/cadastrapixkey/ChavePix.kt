package br.com.api.cadastrapixkey


import br.com.api.cadastrapixkey.contaassociada.ContaAssociada
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotNull
    @Column(nullable = false)
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoDeChave,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    val chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoConta: TipoDeConta,

    @field:Valid
    @Embedded
    val conta: ContaAssociada
) {

    @Id
    @GeneratedValue
    val id : UUID? = null

    @CreationTimestamp
    val criadaEm : LocalDateTime = LocalDateTime.now()
}
