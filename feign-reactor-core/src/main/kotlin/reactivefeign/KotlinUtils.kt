package reactivefeign

import java.lang.reflect.Method
import java.lang.reflect.Type
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

/**
 * @author Stanislav Kichatiy
 */
class KotlinUtils {
    companion object {
        fun isSuspendMethod(method: Method): Boolean {
            return method.kotlinFunction?.isSuspend ?: false
        }

        fun getKotlinMethodReturnType(method: Method): Type? {
            return method.kotlinFunction?.returnType?.javaType
        }
    }
}
