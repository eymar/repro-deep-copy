/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.runtime.slotTree

import androidx.compose.runtime.Composer
import androidx.compose.runtime.SlotTree
import androidx.compose.runtime.runtimeCheck
import androidx.compose.runtime.tooling.CompositionGroup


internal open class CompositionGroupImpl(
    val intKey: Int,
    val objectKey: Any?
) : CompositionGroup {

    override val key: Any
        get() = if (objectKey != null && objectKey != Composer.Empty) {
            objectKey
        } else {
            intKey
        }

    internal val childGroups = mutableListOf<CompositionGroup>()
    override val compositionGroups: Iterable<CompositionGroup>
        get() = TODO("Not yet implemented")
    override val isEmpty: Boolean
        get() = childGroups.isEmpty()

    override fun find(identityToFind: Any): CompositionGroup? {
        return super.find(identityToFind)
    }
    override val sourceInfo: String?
        get() = TODO("Not yet implemented")
    override val node: Any?
        get() = TODO("Not yet implemented")
    override val data: Iterable<Any?>
        get() = TODO("Not yet implemented")
    override val identity: Any?
        get() = super.identity
}