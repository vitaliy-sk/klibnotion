/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2021-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jraf.klibnotion.internal.client

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.features.UserAgent
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.URLBuilder
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.json.Json
import org.jraf.klibnotion.client.ClientConfiguration
import org.jraf.klibnotion.client.HttpLoggingLevel
import org.jraf.klibnotion.client.NotionClient
import org.jraf.klibnotion.internal.api.model.user.ApiUserConverter
import org.jraf.klibnotion.model.common.UuidString
import org.jraf.klibnotion.model.user.User

internal class NotionClientImpl(
    clientConfiguration: ClientConfiguration
) : NotionClient,
    NotionClient.Users {

    override val users = this

    @OptIn(KtorExperimentalAPI::class)
    private val httpClient by lazy {
        createHttpClient() {
            install(JsonFeature) {
                serializer = KotlinxSerializer(
                    Json {
                        // XXX Comment this to have API changes make the parsing fail
                        ignoreUnknownKeys = true
                    }
                )
            }
            defaultRequest {
                header(
                    "Authorization",
                    "Bearer ${clientConfiguration.authentication.apiToken}"
                )
            }
            install(UserAgent) {
                agent = clientConfiguration.userAgent
            }
            engine {
                // Setup a proxy if requested
                clientConfiguration.httpConfiguration.httpProxy?.let { httpProxy ->
                    proxy = ProxyBuilder.http(URLBuilder().apply {
                        host = httpProxy.host
                        port = httpProxy.port
                    }.build())
                }
            }
            // Setup logging if requested
            if (clientConfiguration.httpConfiguration.loggingLevel != HttpLoggingLevel.NONE) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = when (clientConfiguration.httpConfiguration.loggingLevel) {
                        HttpLoggingLevel.NONE -> LogLevel.NONE
                        HttpLoggingLevel.INFO -> LogLevel.INFO
                        HttpLoggingLevel.HEADERS -> LogLevel.HEADERS
                        HttpLoggingLevel.BODY -> LogLevel.BODY
                        HttpLoggingLevel.ALL -> LogLevel.ALL
                    }
                }
            }
        }
    }

    private val service: NotionService by lazy {
        NotionService(httpClient)
    }

    override suspend fun getUser(id: UuidString): User {
        return service.getUser(id)
            .let(ApiUserConverter::apiToModel)
    }

    override fun close() = httpClient.close()
}

internal expect fun createHttpClient(
    block: HttpClientConfig<*>.() -> Unit
): HttpClient