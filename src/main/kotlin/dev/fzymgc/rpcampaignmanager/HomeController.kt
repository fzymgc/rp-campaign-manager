package dev.fzymgc.rpcampaignmanager

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import reactor.core.publisher.Mono

@Controller("/")
@Secured(SecurityRule.IS_ANONYMOUS)
class HomeController {

    @Get("/")
    fun index(): Mono<HttpStatus> {
        return Mono.just(HttpStatus.OK)
    }
}