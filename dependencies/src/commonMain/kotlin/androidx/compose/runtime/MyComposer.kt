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

import androidx.compose.runtime.mycomposer.Anchor
import androidx.compose.runtime.mycomposer.Group
import androidx.compose.runtime.mycomposer.GroupKind
import androidx.compose.runtime.mycomposer.StackOfGroups
import androidx.compose.runtime.tooling.CompositionData
import kotlin.coroutines.CoroutineContext

class MyComposer(
    override val applier: Applier<*>
) : Composer {

    private var isRecomposing = false

    private val rootGroup = Group(
        -1, parent = null,
        anchor = Anchor(0),
        kind = GroupKind.Root
    ).also {
        it.anchor.group = it
        it.node = applier.current!!
    }

    private val mapOfNodesAnchors = mutableMapOf<Any, MutableList<Anchor>>()

    private val groupsStack = StackOfGroups().apply {
        push(rootGroup)
    }

    internal val currentGroup: Group
        get() = groupsStack.peek()!!

    override val inserting: Boolean
        get() = !isRecomposing

    override val skipping: Boolean
        get() {
            println("! TODO: implement skipping")
            return false
        }

    override val defaultsInvalid: Boolean
        get() = TODO("defaultsInvalid Not yet implemented")

    @InternalComposeApi
    override val recomposeScope: RecomposeScope?
        get() {
            println("! TODO: <get-recomposeScope>")
            //MyRecomposeImpl()
            return null
        }

    override val recomposeScopeIdentity: Any?
        get() = TODO("Not yet implemented")

    @InternalComposeApi
    override val compoundKeyHash: Int
        get() = TODO("Not yet implemented")

    override fun startReplaceableGroup(key: Int) {
        startGroup(key, GroupKind.Replaceable)
    }

    override fun endReplaceableGroup() {
        println("endReplaceableGroup")
        endGroup(GroupKind.Replaceable)
    }

    override fun startMovableGroup(key: Int, dataKey: Any?) {
        TODO("Not yet implemented")
    }

    override fun endMovableGroup() {
        TODO("Not yet implemented")
    }

    override fun startDefaults() {
        TODO("Not yet implemented")
    }

    override fun endDefaults() {
        TODO("Not yet implemented")
    }

    private class MyRecomposeImpl : RecomposeScope {
        override fun invalidate() {
            TODO("Not yet implemented")
        }

    }

    private var initiatedInsertGroup: Group? = null

    private fun startGroup(key: Int, kind: GroupKind): Group {
        val child: Group? = if (!inserting) {
            currentGroup.content.getOrNull(currentGroup.currentChildIx)
        } else {
            null
        }
        if (child != null) {
            println("startGroup($key, $kind) - child = $child")
            check(child.key == key && child.kind == kind) {
                "Unexpected child: key = ${child.key}, kind = ${child.kind}"
            }
            currentGroup.currentChildIx++
            groupsStack.push(child)
        } else {
            // insert
            isRecomposing = false
            val anchor = currentGroup.lastKnownAnchor(currentGroup.currentChildIx).let {
                val insertNewAnchorAt =
                    mapOfNodesAnchors.getOrPut(applier.current as Any) { mutableListOf() }.indexOf(it) + 1
                val res = Anchor(if (insertNewAnchorAt == 0) 0 else it.ix)
                if (insertNewAnchorAt != 0 && it.group != null && it.group!!.node != null) {
                    res.ix++
                }
                mapOfNodesAnchors[applier.current]!!.add(insertNewAnchorAt, res)
                res
            }
            val newGroup = Group(
                key, currentGroup, anchor = anchor, kind = kind
            )
            anchor.group = newGroup
            initiatedInsertGroup = initiatedInsertGroup ?: newGroup
            println("startGroup($key, $kind) - anch = $anchor insert = $newGroup")
            currentGroup.content.add(currentGroup.currentChildIx++, newGroup)
            groupsStack.push(newGroup)
        }
        return currentGroup
    }

    private fun endGroup(expectedKind: GroupKind) {
        val pop = groupsStack.pop()!!
        if (inserting) {
            check(initiatedInsertGroup != null) { "Inserting but, initiatedInsertGroup is null" }
            if (pop == initiatedInsertGroup) {
                isRecomposing = true
                initiatedInsertGroup = null
            }
        }
        check(pop.kind == expectedKind)
        pop.currentChildIx = 0
    }

    override fun startRestartGroup(key: Int): Composer {
        startGroup(key, GroupKind.Restart)
        return this
    }

    override fun endRestartGroup(): ScopeUpdateScope? {
        println("endRestartGroup")
        val cg = currentGroup
        check(cg.kind == GroupKind.Restart)

        val scope = if (cg.used) {
            val node = applier.current ?:  error("Unexpected null applier.current")
            object : ScopeUpdateScope {
                override fun updateScope(block: (Composer, Int) -> Unit) {
                    cg.restartBlock = { composer, i ->
                        isRecomposing = true
                        @Suppress("UNCHECKED_CAST")
                        val apl = composer.applier as Applier<Any>
                        val cci = cg.parent!!.content.indexOf(cg)
                        check(cci >= 0)
                        cg.parent.currentChildIx = cci
                        (composer as MyComposer).groupsStack.push(cg.parent)
                        apl.down(node)
                        block(composer, i)
                        apl.up()
                        cg.parent.currentChildIx = 0
                        composer.groupsStack.pop()
                        isRecomposing = false
                    }
                }
            }
        } else {
            cg.restartBlock = null
            null
        }

        endGroup(GroupKind.Restart)
        return scope
    }

    @InternalComposeApi
    override fun insertMovableContent(value: MovableContent<*>, parameter: Any?) {
        TODO("Not yet implemented")
    }

    @InternalComposeApi
    override fun insertMovableContentReferences(references: List<Pair<MovableContentStateReference, MovableContentStateReference?>>) {
        TODO("Not yet implemented")
    }

    override fun sourceInformation(sourceInformation: String) {
        println("! TODO: SI = $sourceInformation")
    }

    override fun sourceInformationMarkerStart(key: Int, sourceInformation: String) {
        println("! TODO: sourceInformationMarkerStart key = $key, si = $sourceInformation")
    }

    override fun sourceInformationMarkerEnd() {
        println("! TODO: sourceInformationMarkerEnd")
    }

    override fun skipToGroupEnd() {
        TODO("Not yet implemented")
    }

    override fun deactivateToEndGroup(changed: Boolean) {
        TODO("Not yet implemented")
    }

    override fun skipCurrentGroup() {
        TODO("Not yet implemented")
    }

    override fun startNode() {
        println(">> startNode")
        startGroup(-100, GroupKind.Node)
    }

    override fun startReusableNode() {
        TODO("Not yet implemented")
    }

    override fun <T> createNode(factory: () -> T) {
        println(">> createNode - $factory")
        check(currentGroup.kind == GroupKind.Node)
        check(currentGroup.node == null)
        currentGroup.node = factory()

        @Suppress("UNCHECKED_CAST")
        (applier as Applier<Any>).apply {
            insertBottomUp(currentGroup.anchor.ix, currentGroup.node!!)
            down(currentGroup.node!!)
        }
    }

    override fun useNode() {
        println(">> useNode")
        check(currentGroup.kind == GroupKind.Node)
        check(currentGroup.node != null)
        @Suppress("UNCHECKED_CAST")
        (applier as Applier<Any>).down(currentGroup.node!!)
    }

    override fun endNode() {
        println(">> endNode")
        applier.up()
        endGroup(GroupKind.Node)
    }

    override fun startReusableGroup(key: Int, dataKey: Any?) {
        TODO("Not yet implemented")
    }

    override fun endReusableGroup() {
        TODO("Not yet implemented")
    }

    override fun disableReusing() {
        TODO("Not yet implemented")
    }

    override fun enableReusing() {
        TODO("Not yet implemented")
    }

    override fun <V, T> apply(value: V, block: T.(V) -> Unit) {
        println("apply - $value, block = $block")
        (applier.current as T).block(value)
    }

    override fun joinKey(left: Any?, right: Any?): Any {
        TODO("Not yet implemented")
    }

    override fun rememberedValue(): Any? {
        println("! TODO: rememberedValue")
        // TODO("Not yet implemented")
        return Composer.Empty
    }

    override fun updateRememberedValue(value: Any?) {
        println("! TODO: updateRememberedValue - $value")
        // TODO("Not yet implemented")
    }

    override fun changed(value: Any?): Boolean {
        println("! TODO: Changed - $value")
        return true
    }

    @InternalComposeApi
    override fun recordUsed(scope: RecomposeScope) {
        TODO("Not yet implemented")
    }

    @InternalComposeApi
    override fun recordSideEffect(effect: () -> Unit) {
        TODO("Not yet implemented")
    }

    @InternalComposeApi
    override fun <T> consume(key: CompositionLocal<T>): T {
        TODO("Not yet implemented")
    }

    @InternalComposeApi
    override fun startProviders(values: Array<out ProvidedValue<*>>) {
        TODO("Not yet implemented")
    }

    @InternalComposeApi
    override fun endProviders() {
        TODO("Not yet implemented")
    }

    override val compositionData: CompositionData
        get() = TODO("Not yet implemented")

    override fun collectParameterInformation() {
        TODO("Not yet implemented")
    }

    @InternalComposeApi
    override fun buildContext(): CompositionContext {
        TODO("Not yet implemented")
    }

    @InternalComposeApi
    override val applyCoroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")
    override val composition: ControlledComposition
        get() = TODO("Not yet implemented")
}