package advanced_tools

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

import org.slf4j.LoggerFactory
import java.util.*
import kotlin.random.Random

/**
 * A potentially infinite stream of elements
 */

data class Product(val id: Int, val name: String, val price: Double)
data class Order(val pid: Int, val quantity: Int)


object Flows {

    val LOGGER = LoggerFactory.getLogger(this::class.java)

    val products = listOf(
        Product(1, "laptop", 999.99),
        Product(1, "smartphone", 1999.99),
        Product(1, "tablet", 799.99),
        Product(1, "smartwatch", 399.99),
    )

    // flow = potentially infinite "list"
    val productsFlow: Flow<Product> = flowOf(
        Product(1, "laptop", 999.99),
        Product(1, "smartphone", 1999.99),
        Product(1, "tablet", 799.99),
        Product(1, "smartwatch", 399.99),
        // emitted at a later point
    )

    // emit values
    val delayedProducts: Flow<Product> = flow {
        // emit elements in this scope
        for (product in products) {
            emit(product)
            delay(500) // semantic blocking
        }
    }

    // transformers
    // map
    val prodNamesCaps: Flow<String> =
        delayedProducts.map { it.name.uppercase(Locale.getDefault()) }

    // filter
    val filteredProducts =
        delayedProducts.filter { it.price > 500 }

    // fold - collapse the flow to a single value
    suspend fun totalInventoryValue(): Double =
        delayedProducts.fold(0.0) { acc, product -> acc + product.price }

    // similar to fold but each intermediate value is emitted.
    val scannedValue: Flow<Double> =
        delayedProducts.scan(0.0) { acc, product -> acc + product.price }

    // handle exceptions
    val flowWithExceptions: Flow<Product> = flow {
        emit(Product(1, "laptop", 999.99))
        if (Random.nextBoolean())
            throw RuntimeException("Network error, cannot fetch product")
        emit(Product(2, "smartphone", 1999.99))
        delay(300)
        emit(Product(3, "tablet", 799.99))
    }.retry(2) { e ->
        e is RuntimeException
    }.catch { e ->
        LOGGER.info("Caught error: $e")
        emit(Product(0, "Unknown", 0.0)) // emit a fallback product
    }

    // side effects on emission
    val productsWithSideEffects: Flow<Product> = delayedProducts.onEach {
        LOGGER.info("generated product: $it")
    }

    val orders: Flow<Int> = flow {
        (1..4).forEach {
            delay(600)
            emit(it)
        }
    }

    val zippedOrders: Flow<Order> =
        delayedProducts.zip(orders) { prod, q -> Order(prod.id, q) }

    // combine multiple flows: merging, concatenating, zipping
    val mergedProducts =
        merge(delayedProducts, productsFlow)

    val concatenatedProducts = flow {
        emitAll(delayedProducts)
        emitAll(productsFlow)
    }

}