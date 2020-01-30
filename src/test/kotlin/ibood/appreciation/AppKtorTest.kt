package ibood.appreciation
// TODO
// Clean up imports
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.HttpMethod
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
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
    }

    fun `When get products with params Then return 200 OK and paged products`() = withKtor {
        with(handleRequest(Get, "/products?limit=2&offset=2")) {
            assertThat(response.status()).isEqualTo(OK)
            val json = jackson.readTree(response.content)
            assertThat(json.size()).isEqualTo(2)
            assertThat(json[0]["id"].textValue()).isEqualTo("id3")
            assertThat(json[0]["title"].textValue()).isEqualTo("Socks")
            assertThat(json[0]["priceInCents"].intValue()).isEqualTo(9_90)

            assertThat(response.headers["Content-Type"]).isEqualTo("application/json; charset=UTF-8")
            assertThat(response.headers["ibood.appreciation.pagination.totalCount"]).isEqualTo("6")
            assertThat(response.headers["ibood.appreciation.pagination.pageSize"]).isEqualTo("2")
            assertThat(response.headers["ibood.appreciation.pagination.currentPage"]).isEqualTo("2")
            assertThat(response.headers["ibood.appreciation.pagination.previousPage"]).isEqualTo("1")

            val previousPageUrl = response.headers["ibood.appreciation.pagination.previousPageUrl"] ?: ""

            assertThat(previousPageUrl.endsWith("/products?limit=2&offset=0")).isTrue()

            assertThat(response.headers["ibood.appreciation.pagination.nextPage"]).isEqualTo("3")

            val nextPageUrl = response.headers["ibood.appreciation.pagination.nextPageUrl"] ?: ""

            assertThat(nextPageUrl.endsWith("/products?limit=2&offset=4")).isTrue()
        }
    }

    fun `When get products with negative offset Then return 400 Bad request and message`() = withKtor {
        with(handleRequest(Get, "/products?offset=-1&limit=2")) {
            assertThat(response.status()).isEqualTo(BadRequest)
            val json = jackson.readTree(response.content)
            assertThat(json.size()).isEqualTo(1)
            assertThat(json["msg"].textValue())
                    .isEqualTo("The offset and limit must be zero or a positive integers")
        }
    }

    fun `When get products with negative limit Then return 400 Bad request and message`() = withKtor {
        with(handleRequest(Get, "/products?offset=2&limit=-2")) {
            assertThat(response.status()).isEqualTo(BadRequest)
            val json = jackson.readTree(response.content)
            assertThat(json.size()).isEqualTo(1)
            assertThat(json["msg"].textValue())
                    .isEqualTo("The offset and limit must be zero or a positive integers")
        }
    }

    fun `When get products with offset more than size Then return 404 Not found and message`() = withKtor {
        with(handleRequest(Get, "/products?offset=8&limit=2")) {
            assertThat(response.status()).isEqualTo(NotFound)
            val json = jackson.readTree(response.content)
            assertThat(json.size()).isEqualTo(1)
            assertThat(json["msg"].textValue())
                    .isEqualTo("Offset cannot be more than size of the repo: 5")
        }
    }

}
