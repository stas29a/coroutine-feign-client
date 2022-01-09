/**
 * Copyright 2018 The Feign Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package reactivefeign;

import feign.Contract;
import feign.MethodMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static feign.Util.checkNotNull;
import static java.util.Arrays.asList;
import static reactivefeign.utils.FeignUtils.*;

/**
 * Contract allowing only {@link Mono} and {@link Flux} return type or method should be suspended
 *
 * @author Sergii Karpenko
 */
public class ReactiveContract implements Contract {

  private final Contract delegate;

  public ReactiveContract(final Contract delegate) {
    this.delegate = checkNotNull(delegate, "delegate must not be null");
  }

  @Override
  public List<MethodMetadata> parseAndValidateMetadata(final Class<?> targetType) {
    final List<MethodMetadata> methodsMetadata =
        this.delegate.parseAndValidateMetadata(targetType);

    for (final MethodMetadata metadata : methodsMetadata) {
      final Type type = metadata.returnType();
      boolean isSuspend = KotlinUtils.Companion.isSuspendMethod(metadata.method());

      if(isSuspend) {
        Type kotlinType = KotlinUtils.Companion.getKotlinMethodReturnType(metadata.method());

        if (kotlinType == null) {
          throw new IllegalArgumentException(String.format("Method %s can't have continuation argument, only kotlin method is allowed",
                  metadata.configKey()));
        }

        metadata.returnType(kotlinType);
        int continuationIndex = metadata.method().getParameterCount() - 1;
        metadata.ignoreParamater(continuationIndex);

        if(metadata.bodyIndex() != null && metadata.bodyIndex().equals(continuationIndex)) {
          metadata.bodyIndex(null);
        }
      }

      if (!isReactorType(type) && !isSuspend) {
        throw new IllegalArgumentException(String.format(
            "Method %s of contract %s doesn't returns reactor.core.publisher.Mono or reactor.core.publisher.Flux",
            metadata.configKey(), targetType.getSimpleName()));
      }

      if(returnActualType(metadata) == byte[].class || bodyActualType(metadata) == byte[].class){
        throw new IllegalArgumentException(String.format(
                "Method %s of contract %s will cause data to be copied, use ByteBuffer instead",
                metadata.configKey(), targetType.getSimpleName()));
      }
    }

    return methodsMetadata;
  }

  private static final Set<Class> REACTOR_PUBLISHERS = new HashSet<>(asList(Mono.class, Flux.class));

  private boolean isReactorType(final Type type) {
    return (type instanceof ParameterizedType)
        && REACTOR_PUBLISHERS.contains(returnPublisherType(type));
  }
}
