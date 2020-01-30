package ibood.appreciation

import io.ktor.application.call
import io.ktor.features.BadRequestException
import io.ktor.features.NotFoundException

data class Product(
        val id: String,
        val title: String,
        val priceInCents: Int
)

interface ProductRepository {
    fun all(): List<Product>
    fun count(): Int
    fun getChunk(limit: Int, offset: Int): List<Product>
}

class InMemoryProductRepository : ProductRepository {

    //  TODO
    //  There is no problem with id as a string in general,
    //  but for such ids Int would better.
    private val products = listOf(
        Product("id1", "TV screen", 499_00),
        Product("id2", "XBox", 149_00),
        Product("id3", "Socks", 9_90),
        Product("id4", "Screw Driver", 2_00),
        Product("id5", "Rice Cooker", 49_00),
        Product("id6", "Shoes", 14_99)
    )

    override fun all() = products
    override fun count() = products.count()
    override fun getChunk(limit: Int, offset: Int): List<Product> {
        val total = count()
        if (offset < 0 || limit < 0) {
            throw BadRequestException("The offset and limit must be zero or a positive integers")
        }
        if (offset >= total) {
            throw NotFoundException("Offset cannot be more than size of the repo: ${total - 1}")
        }
        val end = (offset + limit).coerceAtMost(total)
        return products.slice(offset until end)
    }

}
