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

internal class SlotTreeReader(
    private val slotTree: SlotTree
) {

    private var current: CompositionGroupImpl = slotTree

    private var cci = 0

    val currentGroup: Int
        get() = cci

    val groupKey: Int
        get() = (current.childGroups[cci] as CompositionGroupImpl).intKey

    private var emptyCount = 0

    private val groupsStack = mutableListOf<CompositionGroupImpl>()

    fun beginEmpty() {
        emptyCount++
    }

    fun startGroup() {
        groupsStack.add(current)
        current = current.childGroups[cci] as CompositionGroupImpl
        cci = 0
    }

    fun skipGroup() {
        cci++
    }

    fun skipToGroupEnd() {
        cci = current.childGroups.size
    }

    fun endGroup() {
        cci = 0 // reset correctly
        current = groupsStack.removeLast()
    }

    /**
     * End reporting [Composer.Empty] for calls to [next] and [get],
     */
    fun endEmpty() {
        require(emptyCount > 0) { "Unbalanced begin/end empty" }
        emptyCount--
    }

    fun close() {
        slotTree.readers--
    }

}