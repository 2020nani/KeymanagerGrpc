package br.com.api.cadastrapixkey

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface NovaChavePixRepository: JpaRepository<ChavePix, UUID> {
    fun existsByChave(chave: String?): Boolean

}
