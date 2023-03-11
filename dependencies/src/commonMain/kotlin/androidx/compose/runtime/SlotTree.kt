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

package androidx.compose.runtime

import androidx.compose.runtime.slotTree.CompositionGroupImpl
import androidx.compose.runtime.slotTree.SlotTreeReader
import androidx.compose.runtime.slotTree.SlotTreeWriter
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionGroup

internal class SlotTree :
    CompositionData,
    CompositionGroupImpl(0, Composer.Empty) {

    internal var writer = false

    internal var readers = 0
    private var version = 0

    override val isEmpty: Boolean
        get() = childGroups.isEmpty()

    override val key: Any
        get() = "<root>"
    override val sourceInfo: String?
        get() = TODO("Not yet implemented")
    override val node: Any?
        get() = TODO("Not yet implemented")
    override val data: Iterable<Any?>
        get() = TODO("Not yet implemented")
    override val identity: Any?
        get() = super.identity
    override val compositionGroups: Iterable<CompositionGroup>
        get() = TODO("Not yet implemented")

    override fun find(identityToFind: Any): CompositionGroup? {
        TODO("SlotTree find not implemented")
    }

    inline fun <T> write(block: (writer: SlotTreeWriter) -> T): T = openWriter().let { writer ->
        try {
            block(writer)
        } finally {
            writer.close()
        }
    }

    fun openWriter(): SlotTreeWriter {
        runtimeCheck(!writer) { "Cannot start a writer when another writer is pending" }
        runtimeCheck(readers <= 0) { "Cannot start a writer when a reader is pending" }
        writer = true
        version++
        return SlotTreeWriter(this)
    }

    inline fun <T> read(block: (reader: SlotTreeReader) -> T): T = openReader()
        .let { reader ->
            try {
                block(reader)
            } finally {
                reader.close()
            }
        }

    fun openReader(): SlotTreeReader {
        if (writer) error("Cannot read while a writer is pending")
        readers++
        return SlotTreeReader(this)
    }

    fun verifyWellFormed() {
        println("Implement verifyWellFormed!!!")
    }

}
