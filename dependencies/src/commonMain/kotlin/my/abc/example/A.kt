package my.abc.example

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class AnnotationToAddOnClasses
interface MarkerType

data class TestMarkerDataClass(val i: Int, val t: MarkerType)

class TestMarkerPlainClass(val i: Int, val t: MarkerType) {

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return super.toString()
    }

    fun copy(i: Int = this.i, t: MarkerType = this.t): TestMarkerPlainClass {
        return TestMarkerPlainClass(i, t)
    }
}


@AnnotationToAddOnClasses
data class DataClassWithManuallyAddedAnnotation(val i: Int)