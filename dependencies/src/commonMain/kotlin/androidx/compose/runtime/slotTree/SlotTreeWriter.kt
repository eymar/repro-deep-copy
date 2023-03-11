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

private const val NodeKey = 125

internal class SlotTreeWriter(private val slotTree: SlotTree) {

    /**
     * The number of times [beginInsert] has been called.
     */
    private var insertCount = 0

    fun close() {
        slotTree.writer = false
    }

    fun beginInsert() {
        if (insertCount++ == 0) {
        }
    }

    fun endInsert() {
        check(insertCount > 0) { "Unbalanced begin/end insert" }
        if (--insertCount == 0) {
        }
    }

    fun skipToGroupEnd() {
        cci = current.childGroups.size
    }

    private var current: CompositionGroupImpl = slotTree

    private val groupsStack = mutableListOf<CompositionGroupImpl>()
    private val cciStack = mutableListOf<Int>()

    private var cci = 0

    private fun startGroup(key: Int, objectKey: Any?, isNode: Boolean, aux: Any?) {
        val inserting = insertCount > 0

        if (inserting) {
            val ng = CompositionGroupImpl(key, objectKey)
            current.childGroups.add(cci, ng)
            groupsStack.add(current)
            cciStack.add(cci)
            current = ng
            cci = 0
        } else {
            groupsStack.add(current)
            current = current.childGroups[cci] as CompositionGroupImpl
            cciStack.add(cci)
            cci = 0
            //TODO("implement startGroup when inserting = $inserting")
        }
    }

    fun advanceBy(amount: Int) {
        runtimeCheck(amount >= 0) { "Cannot seek backwards" }
        check(insertCount <= 0) { "Cannot call seek() while inserting" }
        if (amount == 0) return

        if (cci + amount > current.childGroups.size) {
            error("Cannot seek outside the current group - amount = $amount, " +
                "cci = $cci, groupSize = ${current.childGroups.size}")
        }

        cci += amount
    }

    fun removeGroup(): Boolean {
        current.childGroups.removeAt(cci)
        return false
    }

    fun skipGroup(): Int {
        cci++
        return 1
    }

    fun startGroup() {
        runtimeCheck(insertCount == 0) { "Key must be supplied when inserting" }
        startGroup(key = 0, objectKey = Composer.Empty, isNode = false, aux = Composer.Empty)
    }

    fun startGroup(key: Int) = startGroup(key, Composer.Empty, isNode = false, aux = Composer.Empty)

    fun startGroup(key: Int, dataKey: Any?) = startGroup(
        key,
        dataKey,
        isNode = false,
        aux = Composer.Empty
    )

    fun startNode(key: Any?) = startGroup(NodeKey, key, isNode = true, aux = Composer.Empty)
    fun startNode(key: Any?, node: Any?) = startGroup(NodeKey, key, isNode = true, aux = node)
    fun startData(key: Int, objectKey: Any?, aux: Any?) = startGroup(
        key,
        objectKey,
        isNode = false,
        aux = aux
    )
    fun startData(key: Int, aux: Any?) = startGroup(key, Composer.Empty, isNode = false, aux = aux)

    fun endGroup(): Int {
        current = groupsStack.removeLast()
        cci = cciStack.removeLast() + 1
        return 0
    }

    fun update(value: Any?): Any? { return null }

}