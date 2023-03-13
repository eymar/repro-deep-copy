package androidx.compose.runtime.abc

import androidx.compose.runtime.mycomposer.AvlBstNode
import androidx.compose.runtime.mycomposer.TreeIntArray
import kotlin.math.roundToInt
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class TreeIntArrayTest {

    @Test
    fun canCreate() {
        val size = 5
        val t = TreeIntArray(size)
        assertEquals(size, t.size)

        val ixes = mutableListOf<Int>()
        t.inOrder { it, ix, isRoot ->
            println("Ix = ${ix}" + if (isRoot) " - root" else "")
            ixes.add(ix)
        }
        println("---\n")

        assertEquals(size, ixes.distinct().size)
        assertEquals((0 until size).toList(), ixes)

        t.insert(0, 100)
        assertEquals(size + 1, t.size)
        (1..5).forEach {
            t[it] = it * it
        }
        assertEquals(size + 1, t.size)

        val expected = mutableListOf(100)
        expected.addAll((1..5).map { it * it })
        assertEquals(expected, t.toList())
        t.printDump()

        t.validateAllReachable()
    }

    @Test
    fun canCreate2() {
        val size = 33
        val t = TreeIntArray(size)
        assertEquals(size, t.size)

        repeat(size) {
            t[it] = it
        }

        assertEquals((0 until 33).toList(), t.toList())

        t.insert(10, 111)
        t.insert(20, 222)
        t.insert(30, 333)

        assertEquals(size + 3, t.size)
        assertEquals(111, t[10])
        assertEquals(222, t[20])
        assertEquals(333, t[30])

        t.insert(5, 505)
        t.insert(15, 1515)
        t.insert(25, 2525)

        assertEquals(size + 6, t.size)
        assertEquals(505, t[5])
        assertEquals(1515, t[15])
        assertEquals(2525, t[25])
        // shifted to right:
        assertEquals(111, t[11])
        assertEquals(222, t[22])
        assertEquals(333, t[33])

        t.validateAllReachable()
    }

    @Test
    fun canCreateLarge() {
        val size = 100000
        val t = TreeIntArray(size)
        assertEquals(size, t.size)

        repeat(size) {
            t[it] = it
        }

        assertEquals(size, t.size)
        assertEquals((0 until size).toList(), t.toList())

        (size - 1 downTo 0).forEachIndexed { index, i ->
            t[index] = i
        }
        assertEquals(size, t.size)
        assertEquals((size - 1 downTo 0).toList(), t.toList())

        repeat(size) {
            assertEquals(size - it - 1, t[it])
        }

        val addMore = 10000
        val insertMoreAt = 60000
        repeat(addMore) {
            t.insert(atIx = insertMoreAt, value = it)
        }

        assertEquals(size + addMore, t.size)

        repeat(addMore) {
            assertEquals(addMore - it - 1, t[insertMoreAt + it])
        }

        t.validateAllReachable()
    }

    @Test
    fun canCreateSmall() {
        val size = 10
        val t = TreeIntArray(size)
        assertEquals(size, t.size)

        repeat(size) {
            t[it] = it
        }

        assertEquals(size, t.size)
        assertEquals((0 until size).toList(), t.toList())

        (size - 1 downTo 0).forEachIndexed { index, i ->
            t[index] = i
        }
        assertEquals(size, t.size)
        assertEquals((size - 1 downTo 0).toList(), t.toList())

        repeat(size) {
            assertEquals(size - it - 1, t[it])
        }

        repeat(5) {
            println("I = $it")
            t.insert(atIx = 5, value = 333 + it)
        }

        t.validateAllReachable()
    }

    @Test
    fun canInsertInDifferentPositions() {
        var expectedSize = 500
        val t = TreeIntArray(expectedSize)
        repeat(expectedSize) {
            t[it] = it
        }
        assertEquals(expectedSize, t.size)
        assertEquals((0 until 500).toList(), t.toList())

        // insert at the end
        repeat(100) {
            t.insert(expectedSize++, 500 + it)
        }
        assertEquals(600, expectedSize)
        assertEquals(expectedSize, t.size)
        assertEquals((0 until 600).toList(), t.toList())

        // insert at the start
        repeat(100) {
            t.insert(0, -1 - it)
            expectedSize++
        }

        assertEquals(700, expectedSize)
        assertEquals(expectedSize, t.size)
        assertEquals((-100 until 600).toList(), t.toList())

        // insert in the middle 1
        repeat(100) {
            t.insert(100 + it, 10000 + it)
            expectedSize++
        }
        assertEquals(800, expectedSize)
        assertEquals(expectedSize, t.size)

        val expectedList = (-100 until 600).toMutableList().apply {
            addAll(100, (10_000 until 10_100).toList())
        }
        assertEquals(expectedList, t.toList())

        // insert in the middle 2
        repeat(100) {
            t.insert(500 + it, -10000 + it)
            expectedSize++
        }
        assertEquals(900, expectedSize)
        assertEquals(expectedSize, t.size)

        expectedList.addAll(500, (-10000 until -9900).toList())
        assertEquals(expectedList, t.toList())


        // insert in the middle 3 (random)
        repeat(100) {
            val pos = (Math.random() * 900).roundToInt()
            val value = pos * 100_000
            t.insert(pos, value)
            expectedList.add(pos, value)
            expectedSize++
        }

        assertEquals(1000, expectedSize)
        assertEquals(expectedSize, t.size)
        assertEquals(expectedList, t.toList())
        t.validateAllReachable()

        val addMore2 = 500_000

        // large random inserts
        repeat(addMore2) {
            val random = Math.random()
            val pos = (random * expectedSize).roundToInt()
            val value = (random * addMore2).roundToInt()
            t.insert(pos, value)
            expectedList.add(pos, value)
            expectedSize++
        }

        assertEquals(1000 + addMore2, expectedSize)
        assertEquals(expectedSize, t.size)
        assertEquals(expectedList, t.toList())
        t.validateAllReachable()
    }


    @Test
    fun newInsert() {
        var t = AvlBstNode.create(1)
        t = t.mainInsert(1, 10)
        t.printDump()
        t = t.mainInsert(1, 20)
        t.printDump()
        t = t.mainInsert(0, -10)
        t.printDump()
        t = t.mainInsert(2, -20)
        t.printDump()
        t = t.mainInsert(0, 220)
        t.printDump()
    }

    @Test
    fun canInsert() {
        var t = AvlBstNode.create(1)
        t[0] = 10
        t = t.mainInsert(0,)
        t.printDump()

        t[0] = 11
        t.printDump()

        t = t.mainInsert(0)
        t.printDump()

        t[0] = 12
        t.printDump()

        t = t.mainInsert(0)
        t.printDump()

        t[0] = 13
        t.printDump()

        t = t.mainInsert(1)
        t.printDump()

        t[1] = 14
        t.printDump()

        t = t.mainInsert(3)
        t.printDump()
    }

    @Test
    fun canDelete() {
        var t: AvlBstNode? = AvlBstNode.create(10)
        repeat(10) { t!![it] = it}
        val expectedList = (0 .. 9).toMutableList()
        assertEquals(expectedList, t!!.toList())

        t = t.delete(5)!!
        assertEquals(8, t.lastIndex())
        expectedList.removeAt(5)
        assertEquals(expectedList, t.toList())
        t.validateValidDeltas()

        t = t.delete(5)!!
        assertEquals(7, t.lastIndex())
        expectedList.removeAt(5)
        assertEquals(expectedList, t.toList())
        t.validateValidDeltas()

        t = t.delete(5)!!
        assertEquals(6, t.lastIndex())
        expectedList.removeAt(5)
        assertEquals(expectedList, t.toList())
        t.validateValidDeltas()

        t = t.delete(5)!!
        assertEquals(5, t.lastIndex())
        expectedList.removeAt(5)
        assertEquals(expectedList, t.toList())
        t.validateValidDeltas()

        t = t.delete(5)!!
        assertEquals(4, t.lastIndex())
        expectedList.removeAt(5)
        assertEquals(expectedList, t.toList())
        t.validateValidDeltas()

        t = t.delete(0)!!
        assertEquals(3, t.lastIndex())
        expectedList.removeAt(0)
        assertEquals(expectedList, t.toList())
        t.validateValidDeltas()

        t = t.delete(0)!!
        assertEquals(2, t.lastIndex())
        expectedList.removeAt(0)
        assertEquals(expectedList, t.toList())
        t.validateValidDeltas()

        t = t.delete(0)!!
        assertEquals(1, t.lastIndex())
        expectedList.removeAt(0)
        assertEquals(expectedList, t.toList())
        t.validateValidDeltas()

        t = t.delete(0)!!
        assertEquals(0, t.lastIndex())
        expectedList.removeAt(0)
        assertEquals(expectedList, t.toList())
        t.validateValidDeltas()

        t = t.delete(0)
        assertEquals(null, t)
    }

    @Test
    fun deleteInRandomPositions() {
        val size = 1_00
        val expectedList = (0 until size).toMutableList()
        val t = TreeIntArray(size)
        assertEquals(size, t.size)

        expectedList.forEachIndexed { index, i ->
            t[index] = i
        }
        assertEquals(expectedList, t.toList())

        val deleteAmout = 50
        repeat(deleteAmout) {
            val random = Math.random()
            val pos = (expectedList.size * random)
                .roundToInt().coerceAtMost(expectedList.lastIndex)
            expectedList.removeAt(pos)
            t.delete(pos)
            assertEquals(expectedList.size, t.size)
        }
    }
}

//Ix = 0, v = 13
//Ix = 2, v = 14
//Ix = 1, v = 0
//Ix = 3, v = 12 - root
//Ix = 4, v = 11
//Ix = 5, v = 10
