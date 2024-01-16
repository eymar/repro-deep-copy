package repro.deepcopy.generation

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.declarations.buildProperty
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.types.classifierOrNull
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
        moduleFragment.transformChildrenVoid(Transformation(pluginContext, TransformationStage.Classes))
        moduleFragment.transformChildrenVoid(Transformation(pluginContext, TransformationStage.Functions))

//        error(moduleFragment.dumpKotlinLike())
    }

}

enum class TransformationStage {
    Classes, Functions,
    ;
}

class Transformation(
    val context: IrPluginContext,
    val stage: TransformationStage
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

    private fun Name.toFieldNameStr() = asString() + "_staticFieldAddedByPlugin"
    private fun Name.toPropNameStr() = asString() + "_propertyAddedByPlugin"

    fun makeStabilityField(baseName: Name): IrField {
        return context.irFactory.buildField {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            name = Name.identifier(baseName.toFieldNameStr())
            isStatic = true
            isFinal = true
            type = context.irBuiltIns.intType
            visibility = DescriptorVisibilities.PUBLIC
        }
    }

    protected fun makeStabilityProp(
        baseName: Name,
        backingField: IrField,
        parent: IrDeclarationContainer
    ): IrProperty {
        return context.irFactory.buildProperty {
            startOffset = SYNTHETIC_OFFSET
            endOffset = SYNTHETIC_OFFSET
            name = Name.identifier(baseName.asString() + "_propertyAddedByPlugin")
            visibility = DescriptorVisibilities.PUBLIC
        }.also { property ->
            backingField.correspondingPropertySymbol = property.symbol
            property.backingField = backingField
            property.parent = parent
//            property.getter = context.irFactory.buildFun {
//                name = Name.special("<get-${property.name}>")
//                returnType = backingField.type
//            }.also { getter ->
//                val fieldGetter = IrGetFieldImpl(
//                    UNDEFINED_OFFSET,
//                    UNDEFINED_OFFSET,
//                    backingField.symbol,
//                    backingField.type
//                )
//                getter.body = DeclarationIrBuilder(context, getter.symbol)
//                    .irBlockBody { +irReturn(fieldGetter) }
//                getter.parent = parent
//            }
        }
    }

    override fun visitFunction(declaration: IrFunction): IrStatement {
        if (stage != TransformationStage.Functions) return super.visitFunction(declaration)
//        return declaration
        if (declaration.name.asString().startsWith("thisFunctionShouldReturnTheStaticFieldValue")) {
            val clsName = declaration.valueParameters.first().type.getClass()!!.name
            val cls = declaration.valueParameters.first().type.getClass()!!.parent as IrDeclarationContainer

            // check for a case when we are in the same module
            val existingProp = cls.declarations.firstOrNull {
                (it as? IrDeclarationWithName)?.name?.asString() == clsName.toPropNameStr()
            } as? IrProperty

            var actualField = existingProp?.backingField
            if (actualField == null) {
                val field = makeStabilityField(clsName).apply {
                    parent = cls
                }
                val property = makeStabilityProp(clsName, field, cls)
                cls.addChild(property)
                field.correspondingPropertySymbol = property.symbol
                actualField = field
            }

            val fieldGetter = IrGetFieldImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                actualField.symbol,
                actualField.type
            )
            declaration.body = DeclarationIrBuilder(context, declaration.symbol).irBlockBody {
                +irReturn(fieldGetter)
            }
        }
        return super.visitFunction(declaration)
    }

    override fun visitClass(declaration: IrClass): IrStatement {
        if (stage != TransformationStage.Classes) return super.visitClass(declaration)

        if (declaration.name.asString().startsWith("ThisClassShouldHaveAStaticFieldAddedByPlugin")) {
            val fieldConstValue = declaration.name.asString().split("N").last().toInt()
            val root = declaration.parent as IrDeclarationContainer

            val existingProp = root.declarations.firstOrNull {
                (it as? IrDeclarationWithName)?.name?.asString() == declaration.name.toPropNameStr()
            } as? IrField

            if (existingProp != null) {
                error(declaration.dump())
            }

            val field = makeStabilityField(declaration.name)
            val prop = makeStabilityProp(declaration.name, field, root)
            field.correspondingPropertySymbol = prop.symbol

            field.apply {
                parent = root
                initializer = IrExpressionBodyImpl(
                    UNDEFINED_OFFSET,
                    UNDEFINED_OFFSET,
                    irConst(fieldConstValue)
                )
            }
            root.addChild(prop)
        }
        return super.visitClass(declaration)
    }


}
