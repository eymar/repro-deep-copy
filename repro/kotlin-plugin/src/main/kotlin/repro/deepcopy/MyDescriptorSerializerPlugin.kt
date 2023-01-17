package repro.deepcopy

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.serialization.MutableVersionRequirementTable
import org.jetbrains.kotlin.serialization.DescriptorSerializer
import org.jetbrains.kotlin.serialization.DescriptorSerializerPlugin
import org.jetbrains.kotlin.serialization.SerializerExtension
import org.jetbrains.kotlin.metadata.deserialization.Flags.HAS_ANNOTATIONS

class MyDescriptorSerializerPlugin : DescriptorSerializerPlugin {

    private val hasAnnotationFlag = HAS_ANNOTATIONS.toFlags(true)

    override fun afterClass(
        descriptor: ClassDescriptor,
        proto: ProtoBuf.Class.Builder,
        versionRequirementTable: MutableVersionRequirementTable,
        childSerializer: DescriptorSerializer,
        extension: SerializerExtension
    ) {
        if (descriptor.isData && descriptor.name.asString() == "Abc") {
            proto.flags = proto.flags or hasAnnotationFlag
        }
    }
}