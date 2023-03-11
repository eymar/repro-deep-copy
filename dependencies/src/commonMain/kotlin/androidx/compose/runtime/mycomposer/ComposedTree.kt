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

package androidx.compose.runtime.mycomposer

import androidx.compose.runtime.Composer
import kotlin.jvm.JvmInline

class StackOfGroups {
    private val list = mutableListOf<Group>()

    val size: Int
        get() = list.size

    fun push(group: Group) {
        list.add(group)
    }

    fun peek(): Group? {
        return list.lastOrNull()
    }

    fun peekFromTop(ix: Int = 0): Group {
        return list[list.lastIndex - ix]
    }

    fun peekFromTop(predicate: (Group) -> Boolean): Group? {
        return list.lastOrNull {
            predicate(it)
        }
    }

    fun peekFirstFromTop(predicate: (Group) -> Boolean): Group? {
        return (list.lastIndex downTo 0).firstOrNull {
            predicate(list[it])
        }?.let {
            list[it]
        }
    }

    override fun toString(): String {
        return list.toString()
    }

    fun pop(): Group? {
        return list.removeLastOrNull()
    }

    fun clear() {
        list.clear()
    }

    fun isEmpty() = list.isEmpty()
}

class Anchor(var ix: Int) {

    var group: Group? = null

    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun toString(): String {
        return "Anchor(ix=$ix, group = $group)"
    }
}

@JvmInline
value class GroupKind private constructor(private val value: Int) {
    companion object {
        val Root = GroupKind(-1)
        val Node = GroupKind(0)
        val Restart = GroupKind(1)
        val Replaceable = GroupKind(2)
//        val WrapConditional = GroupKind(2)
//        val Branch = GroupKind(3)
//        val Loop = GroupKind(4)
    }
}

class Group(
    val key: Int,
    val parent: Group?,
    val anchor: Anchor = parent!!.anchor,
    val content: MutableList<Group> = mutableListOf(),
    val kind: GroupKind
) {
    val setOfReadValues = mutableSetOf<Any>()

    var restartBlock: ((Composer, Int) -> Unit)? = null

    var currentChildIx = 0

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other === this
    }

    fun rightMostLeafChild(): Group {
        if (node != null) return this
        return content.lastOrNull() ?.rightMostLeafChild() ?: this
    }

    fun lastKnownAnchor(cci: Int = -1): Anchor {
        return content.getOrNull(
            if (cci != -1 && content.isNotEmpty()) (cci - 1).coerceIn(0, content.lastIndex) else content.lastIndex
        )?.rightMostLeafChild()?.anchor ?: anchor
    }

    override fun toString(): String {
        return "$key: ${content.size} children"
    }

    fun dump(): String {
        return "key=$key,${if (node != null) " nodeKey='" + node!! + "'," else ""} " +
            "content: ${content.joinToString(prefix = "(", postfix = ")", separator = "; ") { it.dump() } }"
    }

    var node: Any? = null

    var used = false
}