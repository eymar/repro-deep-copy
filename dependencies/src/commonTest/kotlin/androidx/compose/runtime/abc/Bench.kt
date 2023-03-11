@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:OptIn(ExperimentalTime::class)

package androidx.compose.runtime.abc

import androidx.compose.runtime.*
import androidx.compose.runtime.collection.identitySetOf
import androidx.compose.runtime.mycomposer.StringsListApplier
import androidx.compose.runtime.mycomposer.StringsNodeWrapper
import mycomposer.AbcText
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class Bench {

    @Composable
    fun Item(ix: Int, twice: Boolean) {
        AbcText("abc = $ix - $twice")
        if (twice) {
            AbcText("abc = ${ix * ix}")
        }
    }

    @Test
    fun t1() {
        val root = StringsNodeWrapper("root")
        val recomposer = Recomposer(DefaultMonotonicFrameClock)
        val c = ControlledComposition(StringsListApplier(root), recomposer)

        val v = mutableStateOf(30000)
        val twice = mutableStateOf(false)

        c.setContent {
            repeat(v.value) {
                Item(it, twice.value)
            }
        }

//        println(root.dump())

//        v.value = 10000
        twice.value = true


        var rRes: ControlledComposition? = null
        measureTime {
            rRes = recomposer.performRecompose(c, identitySetOf(twice))
        }.also {
            println(">>> performRecompose time = ${it.inWholeMilliseconds}")
        }

        println("rRes = $rRes")

        measureTime {
            rRes!!.applyChanges()
        }.also {
            println(">>> applyChanges time = ${it.inWholeMilliseconds}")
        }

        v.value = 10000
        measureTime {
            rRes = recomposer.performRecompose(c, identitySetOf(v))
        }.also {
            println(">>> recompose 2 = ${it.inWholeMilliseconds}")
        }
        measureTime {
            rRes!!.applyChanges()
        }.also {
            println(">>> applyChanges 2 time = ${it.inWholeMilliseconds}")
        }

        twice.value = false
        measureTime {
            rRes = recomposer.performRecompose(c, identitySetOf(twice))
        }.also {
            println(">>> recompose 3 = ${it.inWholeMilliseconds}")
        }
        measureTime {
            rRes!!.applyChanges()
        }.also {
            println(">>> applyChanges 3 time = ${it.inWholeMilliseconds}")
        }

        v.value = 20000
        measureTime {
            rRes = recomposer.performRecompose(c, identitySetOf(v))
        }.also {
            println(">>> recompose 4 = ${it.inWholeMilliseconds}")
        }
        measureTime {
            rRes!!.applyChanges()
        }.also {
            println(">>> applyChanges 4 time = ${it.inWholeMilliseconds}")
        }

//        println(root.dump())
    }
}