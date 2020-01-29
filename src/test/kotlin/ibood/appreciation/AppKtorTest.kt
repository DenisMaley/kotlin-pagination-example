package ibood.appreciation

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.HttpMethod
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.kodein.di.Kodein
import org.testng.annotations.Test

@Test
class AppKtorTest {

    private val jackson = jacksonObjectMapper()

    fun `When get products Then return 200 OK and some products`() = withKtor {
        with(handleRequest(Get, "/products")) {
            assertThat(response.status()).isEqualTo(OK)
            val json = jackson.readTree(response.content)
            assertThat(json.size()).isEqualTo(6)
            assertThat(json[0]["id"].textValue()).isEqualTo("id1")
            assertThat(json[0]["title"].textValue()).isEqualTo("TV screen")
            assertThat(json[0]["priceInCents"].intValue()).isEqualTo(499_00)
        }

        with(handleRequest(Get, "/products?limit=2&offset=2")) {
            assertThat(response.status()).isEqualTo(OK)
            val json = jackson.readTree(response.content)
            assertThat(json.size()).isEqualTo(2)
            assertThat(json[0]["id"].textValue()).isEqualTo("id3")
            assertThat(json[0]["title"].textValue()).isEqualTo("Socks")
            assertThat(json[0]["priceInCents"].intValue()).isEqualTo(990)

            assertThat(response.headers()).
        }
    }
}
