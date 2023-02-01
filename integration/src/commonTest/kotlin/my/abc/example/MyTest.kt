package my.abc.example

import kotlin.test.Test

class MyTest {

    @Test
    fun test1() {
        UseAbc(Abc(7))
        UseAbc2(MyAbc2(78))
    }
}
