package repro.deepcopy.generation

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class IrExtension : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        moduleFragment.transformChildrenVoid(Transformation(pluginContext))
    }

}

class Transformation(
    val context: IrPluginContext
) : IrElementTransformerVoid() {

    val annotationToAddOnFun = context.referenceClass(
        ClassId(FqName("kotlin.native"), Name.identifier("HiddenFromObjC"))
    )?.takeIf { it.owner.isAnnotationClass }!!

    val annotationToAddOnClass = context.referenceClass(
        ClassId(FqName("my.abc.example"), Name.identifier("AnnotationToAddOnClasses"))
    )?.takeIf { it.owner.isAnnotationClass }!!

    private fun IrType.isMarkerType() = getClass()?.name?.asString() == "MarkerType"
    private fun IrFunction.shouldBeMarked(): Boolean {
        return this.valueParameters.any {
            it.type.isMarkerType()
        } || returnType.isMarkerType()
    }

    override fun visitClass(declaration: IrClass): IrStatement {
        val cls = super.visitClass(declaration) as IrClass
        if (cls.name.asString().startsWith("TestMarker")) {
            addAnnotation(cls, annotationToAddOnClass)
        }
        return cls
    }

    override fun visitFunction(declaration: IrFunction): IrStatement {
        val f = super.visitFunction(declaration) as IrFunction
        if (f.shouldBeMarked()) {
            addAnnotation(f, annotationToAddOnFun)
        }

        return f
    }

    private fun addAnnotation(declaration: IrDeclaration, annotation: IrClassSymbol) {
        val annotationConstructor = annotation.owner.constructors.first()
        val annotation = IrConstructorCallImpl.fromSymbolOwner(
            type = annotation.defaultType,
            constructorSymbol = annotationConstructor.symbol
        )
        declaration.annotations += annotation
        // For 2.0.0-Beta3
        context.metadataDeclarationRegistrar.addMetadataVisibleAnnotationsToElement(declaration, annotation)

        // For 1.9.22
        // context.annotationsRegistrar.addMetadataVisibleAnnotationsToElement(declaration, annotation)
    }
}
