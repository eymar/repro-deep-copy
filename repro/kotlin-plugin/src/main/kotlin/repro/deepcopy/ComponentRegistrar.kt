package repro.deepcopy

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.serialization.DescriptorSerializerPlugin
import repro.deepcopy.generation.IrExtension

@OptIn(ExperimentalCompilerApi::class)
class ReproDeepCopyRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean
        get() = false

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(IrExtension())

        // Without MyDescriptorSerializerPlugin, the annotation won't be seen on JVM too
        // But, unfortunately, it doesn't help k/js and k/native
        DescriptorSerializerPlugin.registerExtension(MyDescriptorSerializerPlugin())
    }
}
