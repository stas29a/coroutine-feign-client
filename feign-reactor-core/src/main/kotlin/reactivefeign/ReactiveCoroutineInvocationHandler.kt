package reactivefeign

import feign.InvocationHandlerFactory
import feign.InvocationHandlerFactory.MethodHandler
import feign.Target
import reactivefeign.client.ReactiveHttpResponse
import reactor.core.publisher.Mono
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException

/**
 * @author Stanislav Kichatiy
 */
class ReactiveCoroutineInvocationHandler(
    val target: Target<*>?,
    dispatch: Map<Method, MethodHandler>
) : ReactiveInvocationHandler(target, dispatch) {

    class Factory : InvocationHandlerFactory {
        override fun create(
            target: Target<*>?,
            dispatch: Map<Method, InvocationHandlerFactory.MethodHandler>
        ): InvocationHandler {
            return ReactiveCoroutineInvocationHandler(target, dispatch)
        }
    }

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any {
        val nonNullArgs = args ?: arrayOf()

        val lastArg = nonNullArgs.lastOrNull()
        if (lastArg == null || lastArg !is Continuation<*>) {
            return super.invoke(proxy, method, args)
        } else {
            @Suppress("UNCHECKED_CAST")
            val originalContinuation = lastArg as Continuation<Any?>
            val newArgs = nonNullArgs.copyOfRange(0, nonNullArgs.size - 1)
            val invokeResult = super.invoke(proxy, method, newArgs)

            if (invokeResult is Mono<*>) {
                invokeResult.doOnError { error -> originalContinuation.resumeWithException(error) }

                invokeResult.subscribe { result ->

                    if (result is ReactiveHttpResponse<*>) {
                        val body = result.body()

                        if (body is Mono) {
                            body.subscribe { originalContinuation.resumeWith(Result.success(it)) }
                        } else {
                            throw IllegalArgumentException("Only Mono type is allowed for suspend method")
                        }
                    }
                }
            } else {
                throw IllegalArgumentException("Only Mono type is allowed for suspend method")
            }

            return kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
        }
    }
}
