
[![CircleCI](https://circleci.com/gh/Playtika/feign-reactive/tree/develop.svg?style=shield&circle-token=7436cccc44c3229204d0d94c3a1606feb02cb534)](https://circleci.com/gh/Playtika/feign-reactive/tree/develop)
[![codecov](https://codecov.io/gh/Playtika/feign-reactive/branch/develop/graph/badge.svg)](https://codecov.io/gh/Playtika/feign-reactive)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ce80f97d24fb4371a9f71cf44e94b0b0)](https://www.codacy.com/app/PlaytikaGithub/feign-reactive?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Playtika/feign-reactive&amp;utm_campaign=Badge_Grade)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.playtika.reactivefeign/feign-reactor/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.playtika.reactivefeign/feign-reactor)


# feign-reactive

Use Feign with Kotlin coroutines

## Overview

This is fork of reactive feign client(https://github.com/Playtika/feign-reactive) with coroutine support


## Usage

Write Feign API as usual, but every method of interface
 - may accept `org.reactivestreams.Publisher` as body
 - must return `reactor.core.publisher.Mono` or `reactor.core.publisher.Flux` or to be `suspend`
 - if method is `suspend` you can return simple type without wrapping it to `Mono` or `Flux`



## License

Library distributed under Apache License Version 2.0.
