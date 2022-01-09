package reactivefeign.testcase

import feign.Param
import feign.RequestLine
import reactivefeign.testcase.domain.IceCreamOrder
import reactor.core.publisher.Mono

interface SuspendIceCreamServiceApi {
    @RequestLine("GET /icecream/orders/{orderId}")
    suspend fun findOrder(@Param("orderId") orderId: Int): IceCreamOrder

    @RequestLine("GET /icecream/orders/{orderId}")
    fun findOrderMono(@Param("orderId") orderId: Int): Mono<IceCreamOrder>
}