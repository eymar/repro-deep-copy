package repro.deepcopy

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.serialization.DescriptorSerializerPlugin
import repro.deepcopy.generation.IrExtension

@OptIn(ExperimentalCompilerApi::class)
class ReproDeepCopyRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean
        get() = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val useK2 = configuration.languageVersionSettings.languageVersion.usesK2
        KotlinCompilerVersion.getVersion()?.let {
            println(it + " use k2 = $useK2")
        }
        IrGenerationExtension.registerExtension(IrExtension())
    }
}
