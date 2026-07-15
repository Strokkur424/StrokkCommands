package net.strokkur.build

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.*
import java.io.File
import java.net.URI
import java.util.function.Predicate
import java.util.jar.JarFile

abstract class ParsePaperApiJarTask : DefaultTask() {
  @get:Input
  abstract val apiUrl: Property<String>

  @get:OutputFile
  abstract val extracted: RegularFileProperty

  @TaskAction
  fun download() {
    val buildDir = extracted.get().asFile.parentFile
    val sourcesJar = File(buildDir, "api.jar")

    val url = URI(apiUrl.get()).toURL()
    url.openStream().use { input ->
      sourcesJar.outputStream().use { output ->
        input.copyTo(output)
      }
    }

    val data = extractElementData(sourcesJar)
    extracted.get().asFile.writeText(data)
  }

  private fun extractClassesMatching(file: File, predicate: Predicate<ClassReader>): List<ClassReader> {
    val out: MutableList<ClassReader> = mutableListOf()

    JarFile(file).use { jar ->
      for (entry in jar.entries()) {
        if (!entry.name.endsWith(".class")) {
          // Not a class file
          continue
        }

        jar.getInputStream(entry).use { stream ->
          val reader = ClassReader(stream.readAllBytes())
          if (predicate.test(reader)) {
            out.add(reader)
          }
        }
      }
    }

    return out
  }

  private fun extractElementData(file: File): String {
    val classesOfRelevance: List<ClassReader> = extractClassesMatching(file) {
      it.className.equals("io/papermc/paper/command/brigadier/argument/ArgumentTypes")
        || it.className.equals("io/papermc/paper/command/brigadier/argument/SignedMessageResolver")
        || it.className.startsWith("io/papermc/paper/command/brigadier/argument/resolvers")
        || it.className.equals("io/papermc/paper/registry/RegistryKey")
    }

    val out = JsonArray()

    for (one in classesOfRelevance) {
      val visitor = JsonGeneratingVisitor()
      one.accept(visitor, ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
      out.add(JsonObject().apply {
        add("class_name", JsonPrimitive(one.className.replace('/', '.')))
        visitor.supertypes?.let { add("supertypes", it) }
        if (!visitor.fields.isEmpty) {
          add("fields", visitor.fields)
        }
        if (!visitor.methods.isEmpty) {
          add("methods", visitor.methods)
        }
      })
    }

    return out.toString()
  }

  private class JsonGeneratingVisitor : ClassVisitor(Opcodes.ASM9) {
    val fields: JsonArray = JsonArray()
    val methods: JsonArray = JsonArray()
    var supertypes: JsonElement? = null

    override fun visit(
      version: Int,
      access: Int,
      name: String?,
      signature: String?,
      superName: String?,
      interfaces: Array<out String?>?
    ) {
      if (signature != null) {
        this.supertypes = parseClassType(signature)
      }
      super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitField(
      access: Int,
      name: String,
      descriptor: String,
      signature: String?,
      value: Any?
    ): FieldVisitor? {
      fields.add(JsonObject().apply {
        add("name", JsonPrimitive(name))
        add("type", parseFieldType(signature, descriptor))
      })
      return null
    }

    override fun visitMethod(
      access: Int,
      name: String?,
      descriptor: String,
      signature: String?,
      exceptions: Array<out String?>?
    ): MethodVisitor? {
      methods.add(JsonObject().apply {
        add("name", JsonPrimitive(name))
        add("type", parseMethodType(signature, descriptor))
      })
      return null
    }
  }

  companion object {
    private fun parseMethodType(signature: String?, descriptor: String): JsonElement {
      if (signature == null) {
        return JsonPrimitive(Type.getReturnType(descriptor).className)
      }

      val s = signature.substringAfter(')')
      return parseType(s, 0).first
    }

    private fun parseFieldType(signature: String?, descriptor: String): JsonElement {
      val s = signature ?: descriptor
      return parseType(s, 0).first
    }

    private fun parseClassType(signature: String): JsonObject {
      var i = 0
      val params = JsonArray()

      if (signature.startsWith("<")) {
        i++
        while (signature[i] != '>') {
          val end = signature.indexOf(':', i)
          val name = signature.substring(i, end)
          i = end + 1

          val bounds = JsonArray()
          if (signature[i] != ':') {
            parseType(signature, i).also {
              bounds.add(it.first)
              i = it.second
            }
          }
          while (signature[i] == ':') {
            parseType(signature, i + 1).also {
              bounds.add(it.first)
              i = it.second
            }
          }

          params.add(JsonObject().apply {
            add("name", JsonPrimitive(name))
            add("bounds", bounds)
          })
        }
        i++
      }

      val types = JsonArray()
      while (i < signature.length) {
        parseType(signature, i).also {
          types.add(it.first)
          i = it.second
        }
      }

      return JsonObject().apply {
        add("params", params)
        add("types", types)
      }
    }

    private fun parseType(s: String, i: Int): Pair<JsonElement, Int> = when (s[i]) {
      'L' -> {
        val end = s.indexOfAny(charArrayOf('<', ';'), i)
        val type = s.substring(i + 1, end).replace('/', '.')
        if (s[end] == ';') JsonPrimitive(type) to end + 1
        else {
          var j = end + 1
          val args = JsonArray().apply {
            while (s[j] != '>') {
              if (s[j] in "+-") j++
              add(
                if (s[j] == '*') JsonPrimitive("*").also { j++ }
                else parseType(s, j).also { j = it.second }.first
              )
            }
          }
          JsonObject().apply {
            add("type", JsonPrimitive(type))
            add("args", args)
          } to j + 2
        }
      }

      'T' -> s.indexOf(';', i).let {
        JsonPrimitive(s.substring(i + 1, it)) to it + 1
      }

      else -> JsonPrimitive(
        when (s[i]) {
          'V' -> "void"
          'Z' -> "boolean"
          'B' -> "byte"
          'C' -> "char"
          'S' -> "short"
          'I' -> "int"
          'J' -> "long"
          'F' -> "float"
          'D' -> "double"
          else -> error("Bad descriptor: ${s[i]} (position: ${i}. string: ${s})")
        }
      ) to i + 1
    }
  }
}
