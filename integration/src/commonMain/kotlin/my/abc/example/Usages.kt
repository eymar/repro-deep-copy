package my.abc.example

import ThisClassShouldHaveAStaticFieldAddedByPluginNoPackageN20
import ThisClassShouldHaveAStaticFieldAddedByPluginNoPackageN567890

fun CallFunction() {
    AddAnnotationToThisFun()
    AddAnnotationToThisFunToo()
}

fun thisFunctionShouldReturnTheStaticFieldValue(
    instance: ThisClassShouldHaveAStaticFieldAddedByPluginN10
): Int {
    error("The body is expected to be replaced by plugin")
}
fun thisFunctionShouldReturnTheStaticFieldValue2(
    instance: ThisClassShouldHaveAStaticFieldAddedByPluginN10
): Int {
    error("The body is expected to be replaced by plugin")
}

fun thisFunctionShouldReturnTheStaticFieldValue(
    instance: ThisClassShouldHaveAStaticFieldAddedByPluginNoPackageN20
): Int {
    error("The body is expected to be replaced by plugin")
}

fun thisFunctionShouldReturnTheStaticFieldValue(
    instance: ThisClassShouldHaveAStaticFieldAddedByPluginNoPackageN567890
): Int {
    error("The body is expected to be replaced by plugin")
}