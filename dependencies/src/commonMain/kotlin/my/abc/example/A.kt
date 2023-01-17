package my.abc.example

@Retention(AnnotationRetention.RUNTIME)
annotation class AbcAnnotation // To be added by compiler plugin


// The target class
data class Abc(val i: Int)