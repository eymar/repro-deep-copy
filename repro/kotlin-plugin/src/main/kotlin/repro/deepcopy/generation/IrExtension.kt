package repro.deepcopy.generation

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
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

    protected fun irConst(value: Int): IrConst<Int> = IrConstImpl(
        UNDEFINED_OFFSET,
        UNDEFINED_OFFSET,
        context.irBuiltIns.intType,
        IrConstKind.Int,
        value
    )

    fun makeStabilityField(): IrField {
        return context.irFactory.buildField {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            name = Name.identifier("fieldAddedByPlugin")
            isStatic = true
            isFinal = true
            type = context.irBuiltIns.intType
            visibility = DescriptorVisibilities.PUBLIC
        }
    }

    override fun visitClass(declaration: IrClass): IrStatement {
        declaration.declarations += makeStabilityField().apply {
            parent = declaration
            initializer = IrExpressionBodyImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                irConst(10)
            )
        }
        return super.visitClass(declaration)
    }
}
