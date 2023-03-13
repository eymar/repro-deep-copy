package androidx.compose.runtime.abc

import androidx.compose.runtime.mycomposer.AvlBstNode
import androidx.compose.runtime.mycomposer.TreeIntArray
import kotlin.test.Test

class TreeIntArrayTest {

    @Test
    fun canCreate() {
        val t = AvlBstNode.create(33, 50)
        var c = 0
        t.inOrder { it, ix ->
            println("Ix = ${ix}" + if(it == t) " - root" else "")
            c++
        }
        println("total items = $c")
    }

    @Test
    fun canInsert() {
        var t = AvlBstNode.create(1)
        t[0] = 10
        t = t.internalInsert(0,)
        t.inOrder { it, ix ->
            println("Ix = ${ix}, v = ${it.value}" + if(it == t) " - root" else "")
        }
        println("---\n")

        t[0] = 11
        t.inOrder { it, ix ->
            println("Ix = ${ix}, v = ${it.value}" + if(it == t) " - root" else "")
        }
        println("---\n")

        t = t.internalInsert(0)
        t.inOrder { it, ix ->
            println("Ix = ${ix}, v = ${it.value}" + if(it == t) " - root" else "")
        }
        println("---\n")

        t[0] = 12
        t.inOrder { it, ix ->
            println("Ix = ${ix}, v = ${it.value}" + if(it == t) " - root" else "")
        }
        println("---\n")

        t = t.internalInsert(0)
        t.inOrder { it, ix ->
            println("Ix = ${ix}, v = ${it.value}" + if(it == t) " - root" else "")
        }
        println("---\n")

        t[0] = 13
        t.inOrder { it, ix ->
            println("Ix = ${ix}, v = ${it.value}" + if(it == t) " - root" else "")
        }
        println("---\n")


        t = t.internalInsert(1)
        t.inOrder { it, ix ->
            println("Ix = ${ix}, v = ${it.value}" + if(it == t) " - root" else "")
        }
        println("---\n")

        t[1] = 14
        t.inOrder { it, ix ->
            println("Ix = ${ix}, v = ${it.value}" + if(it == t) " - root" else "")
        }
        println("---\n")

        t = t.internalInsert(3)
        t.inOrder { it, ix ->
            println("Ix = ${ix}, v = ${it.value}" + if(it == t) " - root" else "")
        }
        println("---\n")
    }
}

//Ix = 0, v = 13
//Ix = 2, v = 14
//Ix = 1, v = 0
//Ix = 3, v = 12 - root
//Ix = 4, v = 11
//Ix = 5, v = 10