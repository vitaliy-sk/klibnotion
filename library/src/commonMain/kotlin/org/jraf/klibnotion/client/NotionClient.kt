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

package org.jraf.klibnotion.client

import org.jraf.klibnotion.internal.client.NotionClientImpl
import org.jraf.klibnotion.model.common.UuidString
import org.jraf.klibnotion.model.user.User
import kotlin.jvm.JvmStatic

interface NotionClient {
    companion object {
        @JvmStatic
        fun newInstance(configuration: ClientConfiguration): NotionClient = NotionClientImpl(configuration)
    }

    /**
     * User related APIs.
     */
    interface Users {
        /**
         * [Retrieve a user](https://www.notion.so/Retrieve-a-user-94e3370083a74d96b8091ae28cff6058)
         */
        suspend fun getUser(id: UuidString): User
    }


    /**
     * Labels related APIs.
     */
    val users: Users

    /**
     * Dispose of this client instance.
     * This will release some resources so it is recommended to call it after use.
     *
     * **Note: this client will no longer be usable after this is called.**
     */
    fun close()
}