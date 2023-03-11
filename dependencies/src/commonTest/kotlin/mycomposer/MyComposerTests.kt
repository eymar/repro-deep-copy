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

package mycomposer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.MyComposition
import androidx.compose.runtime.mycomposer.PlainTextNode
import androidx.compose.runtime.mycomposer.StringsListApplier
import androidx.compose.runtime.mycomposer.StringsNodeWrapper
import kotlin.test.Test
import kotlin.test.assertEquals

class MyComposerTests {

    @Test
    fun test1() = runMyComposerTest { composition, rootNode ->
        composition.setContent {
            AbcText("abc1")
            AbcText("abc2")
        }
        rootNode.dump().also {
            println(it)
            assertEquals("root:{abc1, abc2}", it)
        }
    }

    @Test
    fun test2() = runMyComposerTest { composition, rootNode ->
        composition.setContent {
            AbcText("start")
            AbcContent("parent") {
                AbcText("child1")
                AbcText("child2")
            }
            AbcText("end")
        }
        rootNode.dump().also {
            println(it)
            assertEquals("root:{start, parent:{child1, child2}, end}", it)
        }
    }

    @Test
    fun testValueUpdates() = runMyComposerTest { composition, rootNode ->
        val i = mutableStateOf(0)
        val j = mutableStateOf(0)
        composition.setContent {
            AbcText("I = ${i.value}")
            AbcText("J = ${j.value}")
        }

        rootNode.dump().also {
            println(it)
            assertEquals("root:{I = 0, J = 0}", it)
        }

        i.value = 100
        composition.recompose()

        rootNode.dump().also {
            println(it)
            assertEquals("root:{I = 100, J = 0}", it)
        }

        j.value = 333
        composition.recompose()

        rootNode.dump().also {
            println(it)
            assertEquals("root:{I = 100, J = 333}", it)
        }
    }

    @Test
    fun testValueUpdatesInTwoScopes() = runMyComposerTest { composition, rootNode ->
        val i = mutableStateOf(0)
        val j = mutableStateOf(0)
        var iRecomposed = 0
        var jRecomposed = 0
        var rootComposed = 0

        composition.setContent {
            Scope {
                AbcContent("p1") {
                    AbcText("I = ${i.value}")
                    iRecomposed++
                }
            }
            Scope {
                AbcContent("p2") {
                    AbcText("J = ${j.value}")
                    jRecomposed++
                }
            }
            rootComposed++
        }

        rootNode.dump().also {
            println(it)
            assertEquals("root:{p1:{I = 0}, p2:{J = 0}}", it)
        }
        assertEquals(1, iRecomposed)
        assertEquals(1, jRecomposed)
        assertEquals(1, rootComposed)

        i.value = 100
        composition.recompose()

        rootNode.dump().also {
            println(it)
            assertEquals("root:{p1:{I = 100}, p2:{J = 0}}", it)
        }

        assertEquals(2, iRecomposed)
        assertEquals(1, jRecomposed)
        assertEquals(1, rootComposed)

        j.value = 333
        composition.recompose()

        rootNode.dump().also {
            println(it)
            assertEquals("root:{p1:{I = 100}, p2:{J = 333}}", it)
        }

        assertEquals(2, iRecomposed)
        assertEquals(2, jRecomposed)
        assertEquals(1, rootComposed)
    }

}

internal fun runMyComposerTest(block: (MyComposition<*>, StringsNodeWrapper) -> Unit) {
    val rootNode = StringsNodeWrapper("root")
    val applier = StringsListApplier(rootNode)
    val composition = MyComposition(applier)
    block(composition, rootNode)
}

@Composable
@NonRestartableComposable
inline fun AbcText(text: String) {
    ComposeNode<PlainTextNode, StringsListApplier>({
        PlainTextNode(text)
    }) {
        set(text) { value -> this.text = value }
    }
}

@Composable
@NonRestartableComposable
inline fun AbcContent(name: String, content: @Composable () -> Unit) {
    ComposeNode<StringsNodeWrapper, StringsListApplier>(
        factory = {
            StringsNodeWrapper(name)
        },
        update = {
            set(name) { value -> this.text = value }
        },
        content = content
    )
}

@Composable
fun Scope(content: @Composable () -> Unit) {
    content()
}