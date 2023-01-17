package repro.deepcopy.generation

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.platform.jvm.isJvm

class IrExtension(
    val remapDescriptors: Boolean = false // it can be changed manually here only
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        try {
            moduleFragment.transformChildrenVoid(Transformation(pluginContext))

            if (remapDescriptors) { // JVM works when this either true or false anyway. But it doesn't help k/js and k/native
                // It just ensures that all declarations have new IrBasedDescriptors (with add @AbcAnnotation),
                // which is checked in assertAboutDependenciesModule
                deepCopyToReinitializeDescriptors(moduleFragment)
            }

            if (moduleFragment.name.asString().contains("dependencies")) {
                // Here we check the IR and descriptor of class Abc to contain an annotation added in IR Transformation.
                assertAboutDependenciesModule(moduleFragment)
            }
        } catch (e: Throwable) {
            throw Exception("Failure! Module = ${moduleFragment.name}; Cause: ${e.message}", e)
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun assertAboutDependenciesModule(moduleFragment: IrModuleFragment) {
        val file = moduleFragment.files.let {
            assert(it.size == 1)
            it.first()
        }
        val expectedAnnotation = FqName("my.abc.example.AbcAnnotation")
        val abcClass = file.declarations.last().also {
            assert(it.nameForIrSerialization.asString() == "Abc") {
                "Unexpected class name = ${it.nameForIrSerialization}"
            }
            assert(it.annotations.hasAnnotation(expectedAnnotation)) {
                "IR of class ${it.nameForIrSerialization} should contain $expectedAnnotation annotation"
            }
        }
        if (remapDescriptors) {
            val abcDescriptor = abcClass.descriptor
            assert(abcDescriptor.annotations.findAnnotation(expectedAnnotation) != null) {
                val allAnnotations = abcDescriptor.annotations.joinToString(prefix = "[", postfix = "]") {
                    it.fqName?.asString() ?: "null"
                }
                "The Descriptor of class ${abcClass.nameForIrSerialization} should contain $expectedAnnotation annotation. All annotations = $allAnnotations"
            }
        }
    }
}


class Transformation(
    val context: IrPluginContext
) : IrElementTransformerVoid() {

    val fqName = FqName("my.abc.example.AbcAnnotation")
    val annotation = context.referenceClass(ClassId.topLevel(fqName))!!
    val targetClassFqName = FqName("my.abc.example.Abc")

    override fun visitClass(declaration: IrClass): IrStatement {
        // This is applied to module `dependencies` (class my.abc.example.Abc)
        val original = super.visitClass(declaration) as IrClass

        if (original.fqNameForIrSerialization == targetClassFqName) {
            original.annotations = original.annotations + IrConstructorCallImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                annotation.defaultType,
                annotation.constructors.first(),
                0, 0, 0
            )
        }

        return original
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        // This is applied to module `integration` (my.abc.example.UseAbc)
        if (declaration.name.asString().contains("UseAbc")) {
            assertAboutUseAbcFun(declaration)
        }
        return super.visitSimpleFunction(declaration)
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun assertAboutUseAbcFun(declaration: IrSimpleFunction) {
        val parameter = declaration.valueParameters.first()
        assert(parameter.name.asString() == "abc") {
            "Expected 1 parameter - abc: Abc"
        }

        val cls = (parameter.type.classifierOrNull!!.owner as IrClass)
        val clsDescriptor = cls.descriptor

        assert(!clsDescriptor.annotations.isEmpty()) {
            // This assert passes thanks to MyDescriptorSerializerPlugin,
            // but it's actually empty in k/js and k/native
            "clsDescriptor.annotations is Empty"
        }

        assert(clsDescriptor.annotations.hasAnnotation(fqName)) {
            // This assert fails in k/js and k/native
            "No $fqName annotation found in descriptor. " +
                    "Annotations = " + clsDescriptor.annotations.joinToString(prefix = "[", postfix = "]") { it.fqName?.asString() ?: "null" }
        }

        assert(cls.annotations.size == 1) {
            // This assert fails in k/js and k/native
            "Expected 1 annotation, but were - ${cls.annotations.size}"
        }

        (cls.annotations.first().type.classifierOrNull!!.owner as IrClass).also {
            assert(it.fqNameForIrSerialization == fqName) {
                "Expected annotation - $fqName, but was - ${it.fqNameForIrSerialization}"
            }
        }
    }
}

private fun deepCopyToReinitializeDescriptors(moduleFragment: IrModuleFragment) {
    val sr = object : DeepCopySymbolRemapper() {}
    val tr = DeepCopyTypeRemapper(sr)

    moduleFragment.acceptVoid(sr)

    val dc = object : DeepCopyIrTreeWithSymbols(
        symbolRemapper = sr,
        typeRemapper = tr
    ) {

        override fun visitClass(declaration: IrClass): IrClass {
            return super.visitClass(declaration).also {
                it.metadata = declaration.metadata
            }
        }

        override fun visitSimpleFunction(declaration: IrSimpleFunction): IrSimpleFunction {
            return super.visitSimpleFunction(declaration).also {
                it.metadata = declaration.metadata
            }
        }

    }
    tr.deepCopy = dc

    moduleFragment.transformChildrenVoid(dc)
    moduleFragment.patchDeclarationParents()
}