package net.strokkur.build

import net.strokkur.jap.code.CodeGenUtil
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.net.URI

@DisableCachingByDefault
abstract class ParsePaperApiJarTask : DefaultTask() {
  @get:Input
  abstract val url: Property<String>

  @get:Input
  abstract val target: Property<String>

  @get:OutputDirectory
  abstract val targetDir: DirectoryProperty

  @TaskAction
  fun run() {
    val apiJar = targetDir.file("external-data/api.jar").get().asFile
    apiJar.parentFile.mkdirs()

    val url = URI(url.get()).toURL()
    url.openStream().use { input ->
      apiJar.outputStream().use { output ->
        input.copyTo(output)
      }
    }

    val createdClass = PaperArgumentConverterBuilder(target.get(), apiJar.toPath()).createClass()
    val javaSource = CodeGenUtil.createJavaFile(createdClass)

    val targetFile = targetDir.file("generated/${target.get().replace(".", "/")}.java").get().asFile
    targetFile.parentFile.mkdirs()
    targetFile.writeText(javaSource)
  }
}
