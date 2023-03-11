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

import androidx.compose.runtime.AbstractApplier

open class StringsNodeWrapper(
    var text: String? = null,
    val list: MutableList<StringsNodeWrapper> = mutableListOf()
) {
    override fun toString(): String {
        return list.joinToString(prefix = "${text ?: "_"}:{", postfix = "}")
    }

    fun dump() = toString()
}

class PlainTextNode(text: String? = null) : StringsNodeWrapper(text) {
    override fun toString(): String {
        return text ?: ""
    }
}
class StringsListApplier(root: StringsNodeWrapper) : AbstractApplier<StringsNodeWrapper>(root) {
    override fun insertBottomUp(index: Int, instance: StringsNodeWrapper) {
        current.list.add(index, instance)
    }

    override fun insertTopDown(index: Int, instance: StringsNodeWrapper) {
//        current.list.add(index, instance)
    }

    override fun move(from: Int, to: Int, count: Int) {
        current.list.move(from, to, count)
    }

    override fun remove(index: Int, count: Int) {
        current.list.remove(index, count)
    }

    override fun onClear() {
        current.list.clear()
    }
}