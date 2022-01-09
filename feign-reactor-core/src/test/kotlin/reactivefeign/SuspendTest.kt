package reactivefeign

import com.fasterxml.jackson.core.JsonProcessingException
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.jupiter.api.Assertions
import reactivefeign.resttemplate.client.RestTemplateFakeReactiveFeign
import reactivefeign.testcase.SuspendIceCreamServiceApi
import reactivefeign.testcase.domain.OrderGenerator
import reactor.core.publisher.Mono
import reactor.netty.DisposableServer
import reactor.netty.http.HttpProtocol
import reactor.netty.http.server.HttpServer
import reactor.netty.http.server.HttpServerRequest
import reactor.netty.http.server.HttpServerResponse
import reactor.netty.http.server.HttpServerRoutes
import java.time.Duration

/**
 * @author Stanislav Kichatiy
 */
class SuspendTest {
    val CALLS_NUMBER = 500

    fun builder(): ReactiveFeign.Builder<SuspendIceCreamServiceApi> {
        return RestTemplateFakeReactiveFeign.builder()
    }

    companion object {
        private var server: DisposableServer? = null
        val DELAY_IN_MILLIS = 500
        val etalon = OrderGenerator().generate(1)

        @BeforeClass
        @JvmStatic
        @Throws(JsonProcessingException::class)
        fun startServer() {
            val data = TestUtils.MAPPER.writeValueAsString(etalon).toByteArray()
            server = HttpServer.create()
                .protocol(HttpProtocol.HTTP11, HttpProtocol.H2C)
                .route { r: HttpServerRoutes ->
                    r[
                        "/icecream/orders/1", { req: HttpServerRequest?, res: HttpServerResponse ->
                            res.header("Content-Type", "application/json")
                            Mono
                                .delay(Duration.ofMillis(DELAY_IN_MILLIS.toLong()))
                                .thenEmpty(res.sendByteArray(Mono.just(data)))
                        }
                    ]
                }
                .bindNow()
        }

        @JvmStatic
        @AfterClass
        fun stopServer() {
            server!!.disposeNow()
        }
    }

    @Test
    fun shouldRunReactively() {
        val client = builder()
            .target(
                SuspendIceCreamServiceApi::class.java,
                "http://localhost:" + server!!.port()
            )

        runBlocking {
            val firstOrder = client.findOrder(1)

            Assertions.assertEquals(SuspendTest.etalon, firstOrder)
        }
    }
}
