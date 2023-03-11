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

import androidx.compose.runtime.mycomposer.Group
import androidx.compose.runtime.mycomposer.GroupKind
import androidx.compose.runtime.snapshots.MutableSnapshot
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotApplyResult

class MyComposition<T>(
    val applier: Applier<T>,
): ControlledComposition {

    override val hasInvalidations: Boolean
        get() = TODO("Not yet implemented")
    override val isDisposed: Boolean
        get() = TODO("Not yet implemented")

    override fun dispose() {
        TODO("Not yet implemented")
    }

    override val isComposing: Boolean
        get() = TODO("Not yet implemented")
    override val hasPendingChanges: Boolean
        get() = TODO("Not yet implemented")

    override fun composeContent(content: @Composable () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun recordModificationsOf(values: Set<Any>) {
        TODO("Not yet implemented")
    }

    override fun observesAnyOf(values: Set<Any>): Boolean {
        TODO("Not yet implemented")
    }

    override fun prepareCompose(block: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun recordReadOf(value: Any) {
        TODO("Not yet implemented")
    }

    override fun recordWriteOf(value: Any) {
        TODO("Not yet implemented")
    }

    @InternalComposeApi
    override fun insertMovableContent(
        references: List<Pair<MovableContentStateReference, MovableContentStateReference?>>
    ) {
        TODO("Not yet implemented")
    }

    @InternalComposeApi
    override fun disposeUnusedMovableContent(state: MovableContentState) {
        TODO("Not yet implemented")
    }

    override fun applyChanges() {
        TODO("Not yet implemented")
    }

    override fun applyLateChanges() {
        TODO("Not yet implemented")
    }

    override fun changesApplied() {
        TODO("Not yet implemented")
    }

    override fun invalidateAll() {
        TODO("Not yet implemented")
    }

    @InternalComposeApi
    override fun verifyConsistent() {
        TODO("Not yet implemented")
    }

    override fun <R> delegateInvalidations(to: ControlledComposition?, groupIndex: Int, block: () -> R): R {
        TODO("Not yet implemented")
    }

    private val composer = MyComposer(applier)

    private val readObserver: (Any) -> Unit = {
        valuesToGroups.getOrPut(it) {
            mutableSetOf()
        }.add(nearestRestartGroup().also {
            it.used = true
        })
    }

    private val valuesToGroups = mutableMapOf<Any, MutableSet<Group>>()

    private fun nearestRestartGroup(cg: Group = composer.currentGroup): Group {
        return if (cg.kind == GroupKind.Restart) {
            cg
        } else {
            nearestRestartGroup(cg.parent!!)
        }
    }

    private val writtenValuesSet = mutableSetOf<Any>()

    init {
        Snapshot.registerGlobalWriteObserver {
            writtenValuesSet.add(it)
        }
    }

    override fun setContent(content: @Composable () -> Unit) {
        val snapshot = Snapshot.takeMutableSnapshot(readObserver)
        snapshot.enter {
            invokeComposable(composer, content)
        }
        applyAndCheck(snapshot)
    }

    override fun recompose(): Boolean {
        val snapshot = Snapshot.takeMutableSnapshot(readObserver)
        snapshot.enter {
            val changedValues = writtenValuesSet.toList()
            writtenValuesSet.clear()
            changedValues.forEach {
                val affectedGroups = valuesToGroups[it]?.toMutableList()
                valuesToGroups[it]?.clear()
                affectedGroups?.forEach {
                    if (it.restartBlock == null) error("Restart block is null for ${it.kind}")
                    it.restartBlock!!.invoke(composer, 0)
                }
            }
        }
        applyAndCheck(snapshot)
        return true
    }

    private fun applyAndCheck(snapshot: MutableSnapshot) {
        try {
            val applyResult = snapshot.apply()
            if (applyResult is SnapshotApplyResult.Failure) {
                error(
                    "Unsupported concurrent change during composition. A state object was " +
                        "modified by composition as well as being modified outside composition."
                )
            }
        } finally {
            snapshot.dispose()
        }
    }

}