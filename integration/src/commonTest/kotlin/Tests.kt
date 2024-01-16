import my.abc.example.ThisClassShouldHaveAStaticFieldAddedByPluginN10
import my.abc.example.testPrivateClass
import my.abc.example.thisFunctionShouldReturnTheStaticFieldValue
import my.abc.example.thisFunctionShouldReturnTheStaticFieldValue2
import kotlin.test.Test
import kotlin.test.assertEquals

class Tests {

    @Test
    fun test1() {
        val i = thisFunctionShouldReturnTheStaticFieldValue(ThisClassShouldHaveAStaticFieldAddedByPluginN10())
        assertEquals(10, i)
    }

    @Test
    fun test1_nested() {
        val i = thisFunctionShouldReturnTheStaticFieldValue(
            ThisClassShouldHaveAStaticFieldAddedByPluginN10.ThisClassShouldHaveAStaticFieldAddedByPluginNestedN111())
        assertEquals(111, i)
    }

    @Test
    fun test1_2() {
        val i = thisFunctionShouldReturnTheStaticFieldValue2(ThisClassShouldHaveAStaticFieldAddedByPluginN10())
        assertEquals(10, i)
    }
    @Test
    fun test2() {
        val i = thisFunctionShouldReturnTheStaticFieldValue(ThisClassShouldHaveAStaticFieldAddedByPluginNoPackageN20())
        assertEquals(20, i)
    }

    @Test
    fun test3() {
        val i = thisFunctionShouldReturnTheStaticFieldValue(ThisClassShouldHaveAStaticFieldAddedByPluginNoPackageN567890())
        assertEquals(567890, i)
    }

    @Test
    fun test4() {
        val i = testPrivateClass
        assertEquals(10, i)
    }
}