package repro.deepcopy.generation

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.defaultType
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

    val annotationToAdd = ClassId(FqName("kotlin.native"), Name.identifier("HiddenFromObjC"))
//    val annotationToAdd = ClassId(FqName("my.abc.example"), Name.identifier("AbcAnnotation"))

    val annotation = context.referenceClass(annotationToAdd)?.takeIf { it.owner.isAnnotationClass }!!

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        // This is applied to module `integration`
        if (declaration.name.asString().contains("AddAnnotationToThisFun")) {
            addAnnotation(declaration)
//            error(declaration.dumpKotlinLike())
        }
        return super.visitSimpleFunction(declaration)
    }

    private fun addAnnotation(declaration: IrSimpleFunction) {
        val annotationConstructor = annotation.owner.constructors.first()
        val annotation = IrConstructorCallImpl.fromSymbolOwner(
            type = annotation.defaultType,
            constructorSymbol = annotationConstructor.symbol
        )
        declaration.annotations += annotation
        // for Beta1
//        context.annotationsRegistrar.addMetadataVisibleAnnotationsToElement(declaration, annotation)

        // for Beta2
         context.metadataDeclarationRegistrar.addMetadataVisibleAnnotationsToElement(declaration, annotation)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val callee = expression.symbol.owner
        if (callee.name.asString().contains("AddAnnotationToThisFun")) {
            assertAboutCallFunction(callee)
        }
        return super.visitCall(expression)
    }

    private fun assertAboutCallFunction(declaration: IrSimpleFunction) {
//        error(declaration.dump())
        if (declaration.name.asString() == "AddAnnotationToThisFunToo") {
//            error(declaration.dump())
        }
        assert(declaration.annotations.isNotEmpty()) {
            "Expected non empty annotations. \nActual dump: ${declaration.dump()}"
        }
        assert(declaration.annotations.hasAnnotation(annotationToAdd.asSingleFqName())) {
            "Expected $annotationToAdd. \nActual dump: ${declaration.dump()}"
        }
    }
}
