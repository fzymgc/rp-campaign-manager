package dev.fzymgc.rpcampaignmanager

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.info.*

@OpenAPIDefinition(
    info = Info(
            title = "dev.fzymgc.rpcampaignmanager",
            version = "0.0"
    )
)
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("dev.fzymgc.rpcampaignmanager")
                .mainClass(
                  Application.javaClass)
                .start()
    }
}
