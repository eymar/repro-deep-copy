package my.abc.example



fun AddAnnotationToThisFunToo() { // to test a case w/o an annotation in the source code

}

class ThisClassShouldHaveAStaticFieldAddedByPluginN10

private class ThisClassShouldHaveAStaticFieldAddedByPluginPrivateN10


private fun thisFunctionShouldReturnTheStaticFieldValue(
    instance: ThisClassShouldHaveAStaticFieldAddedByPluginPrivateN10
): Int {
    error("The body is expected to be replaced by plugin")
}

val testPrivateClass: Int
    get() = thisFunctionShouldReturnTheStaticFieldValue(ThisClassShouldHaveAStaticFieldAddedByPluginPrivateN10())
