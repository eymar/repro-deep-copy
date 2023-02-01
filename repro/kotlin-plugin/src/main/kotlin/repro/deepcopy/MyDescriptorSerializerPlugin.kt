package repro.deepcopy

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ClassDescriptorImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrClassSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.library.metadata.KlibMetadataSerializerProtocol
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.serialization.MutableVersionRequirementTable
import org.jetbrains.kotlin.serialization.DescriptorSerializer
import org.jetbrains.kotlin.serialization.DescriptorSerializerPlugin
import org.jetbrains.kotlin.serialization.SerializerExtension
import org.jetbrains.kotlin.metadata.deserialization.Flags.HAS_ANNOTATIONS
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.serialization.builtins.BuiltInsSerializerExtension
import org.jetbrains.kotlin.types.KotlinType

class MyDescriptorSerializerPlugin(
    val annotatedIrClasses: Map<FqName, Int>,
) : DescriptorSerializerPlugin {

    private val hasAnnotationFlag = HAS_ANNOTATIONS.toFlags(true)

    override fun afterClass(
        descriptor: ClassDescriptor,
        proto: ProtoBuf.Class.Builder,
        versionRequirementTable: MutableVersionRequirementTable,
        childSerializer: DescriptorSerializer,
        extension: SerializerExtension
    ) {
//        if (descriptor.isData && descriptor.name.asString().contains("Abc")) {
        if (descriptor.fqNameSafe in annotatedIrClasses) {
            proto.flags = proto.flags or hasAnnotationFlag
            val annotationProto = ProtoBuf.Annotation.newBuilder().apply {
                id = extension.stringTable.getQualifiedClassNameIndex(ClassId.fromString("my/abc/example/AbcAnnotation"))
                val argument = ProtoBuf.Annotation.Argument.newBuilder()
                argument.nameId = extension.stringTable.getStringIndex("i")
                val value = ProtoBuf.Annotation.Argument.Value.newBuilder().apply {
                    this.intValue = annotatedIrClasses[descriptor.fqNameSafe]!!.toLong()
                    this.type = ProtoBuf.Annotation.Argument.Value.Type.INT
                }.build()
                argument.value = value
                addArgument(argument.build())
            }.build()
            proto.addExtension(KlibMetadataSerializerProtocol.classAnnotation, annotationProto)
        }
    }
}
