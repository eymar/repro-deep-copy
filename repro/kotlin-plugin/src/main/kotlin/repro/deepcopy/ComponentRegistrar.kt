package repro.deepcopy

import repro.deepcopy.generation.IrExtensionsRepro
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.KotlinCompilerVersion

class ReproDeepCopyRegistrar @JvmOverloads constructor(
    private val enabled: Boolean = false
): ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        require(KotlinCompilerVersion.getVersion() == "1.6.0-M1")
        IrGenerationExtension.registerExtension(project, IrExtensionsRepro())
    }

}
