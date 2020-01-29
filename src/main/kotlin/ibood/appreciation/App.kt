package ibood.appreciation

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.BadRequestException
import io.ktor.features.ContentNegotiation
import io.ktor.features.NotFoundException
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import mu.KotlinLogging.logger
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import java.lang.IllegalArgumentException


object App {
    private val log = logger {}
    @JvmStatic
    fun main(args: Array<String>) {
        log.info { "Starting up application." }
        embeddedServer(factory = Netty, port = 8081) {
            mainKodeined(applicationKodein())
        }.start(wait = true)
    }
}

fun applicationKodein() = Kodein {
    bind<ProductRepository>() with singleton { InMemoryProductRepository() }
}

fun Application.mainKodeined(kodein: Kodein) {
    val productRepository by kodein.instance<ProductRepository>()

    //  TODO
    //  We should encapsulate exceptions handling
    routing {
        get("/products") {
            val total = productRepository.count()
            val offset = call.parameters["offset"]?.toInt() ?: 0
            val limit = call.parameters["limit"]?.toInt() ?: total - offset

            try {
                val chunk = productRepository.getChunk(limit, offset)
                PaginationHeaderGenerator.buildHeaders(total, limit, offset, call).forEach { header ->
                    call.response.headers.append(header.name, header.value)
                }
                call.respond(chunk)
            }
            catch (e: NotFoundException) {
                e.message?.let { it1 -> call.respond(NotFound, mapOf("msg" to it1)) }
            }
            catch (e: BadRequestException) {
                e.message?.let { it1 -> call.respond(BadRequest, mapOf("msg" to it1)) }
            }
        }
    }

    install(ContentNegotiation) {
        jackson { }
    }
}
