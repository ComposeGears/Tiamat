package com.composegears.tiamat.destinations

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Assert.assertEquals
import org.junit.Test

class TiamatDestinationsCompilerPluginTest {

    // Rest of the test setup is the same as the previous test...
    val annotationSource = SourceFile.kotlin(
        "Annotations.kt", """
            package com.composegears.tiamat.destinations
            
            import kotlin.reflect.KClass
            
            @Repeatable
            @Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
            @Retention(AnnotationRetention.RUNTIME)
            annotation class InstallIn(val target: KClass<*>)
            
            interface TiamatGraph {
                fun destinations(): Array<com.composegears.tiamat.NavDestination<*>> = emptyArray()
            }
        """.trimIndent()
    )

    val navDestinationSource = SourceFile.kotlin(
        "NavDestination.kt", """
            package com.composegears.tiamat.navigation
            
            import kotlin.properties.ReadOnlyProperty
            import kotlin.reflect.KProperty
            
            interface NavDestination<T> {
                val name: String
                val extensions: List<Extension<T>>             
                fun NavDestinationScope<T>.Content()
            }
            
            interface NavDestinationScope<T>
            
            interface Extension<T>
            
            class NavDestinationImpl<T>(
                override val name: String,
                override val extensions: List<Extension<T>>,
                private val content: NavDestinationScope<T>.() -> Unit
            ) : NavDestination<T> {
                override fun NavDestinationScope<T>.Content() { 
                    content() 
                }
            }
            
            class NavDestinationInstanceDelegate<Args>(
                private val extensions: List<Extension<Args>>,
                private val content: NavDestinationScope<Args>.() -> Unit,
            ) : ReadOnlyProperty<Nothing?, NavDestination<Args>> {
                private var destination: NavDestination<Args>? = null
            
                override fun getValue(thisRef: Nothing?, property: KProperty<*>): NavDestination<Args> {
                    if (destination == null) destination = NavDestinationImpl(property.name, extensions, content)
                    return destination!!
                }
            }
            
            fun <Args> NavDestination(
                name: String,
                extensions: List<Extension<Args>> = emptyList(),
                content: NavDestinationScope<Args>.() -> Unit
            ): NavDestination<Args> = NavDestinationImpl(name, extensions, content)
            
            fun <Args> navDestination(
                vararg extensions: Extension<Args>? = emptyArray(),
                content: NavDestinationScope<Args>.() -> Unit
            ): NavDestinationInstanceDelegate<Args> = NavDestinationInstanceDelegate(listOfNotNull(*extensions), content)
        """.trimIndent()
    )


    @Test
    @OptIn(ExperimentalCompilerApi::class)
    fun `test plugin handles multiple destination types`() {
        val source = SourceFile.kotlin(
            "Test.kt", """
            package com.test
            
            import com.composegears.tiamat.navigation.*
            import com.composegears.tiamat.destinations.*
            
            object MyGraph : TiamatGraph
            object OtherGraph : TiamatGraph
            
            @InstallIn(MyGraph::class)
            val Screen1 by navDestination<Unit> { }
            
            @InstallIn(MyGraph::class)
            val Screen2 = NavDestination<Unit>(name = "Screen2", extensions = emptyList()) {}
            
            @InstallIn(MyGraph::class)
            object Screen3 : NavDestination<Int> {
                override val name: String = "Screen3"
                override val extensions: List<Extension<Int>> = emptyList()
                override fun NavDestinationScope<Int>.Content() {}
            }
            
            class Screen4Class : NavDestination<Int> {
                override val name: String = "Screen4"
                override val extensions: List<Extension<Int>> = emptyList()
                override fun NavDestinationScope<Int>.Content() {}
            }
            
            @InstallIn(MyGraph::class)
            @InstallIn(OtherGraph::class)
            val Screen4 = Screen4Class()
            
            fun main() {
                println(MyGraph.destinations().joinToString("\n") { it.name })
                println("--")
                println(OtherGraph.destinations().joinToString("\n") { it.name })
            }
        """.trimIndent()
        )

        val result = KotlinCompilation().apply {
            sources = listOf(source, annotationSource, navDestinationSource)
            compilerPluginRegistrars = listOf(TiamatDestinationsComponentRegistrar())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)

        val kClazz = result.classLoader.loadClass("com.test.TestKt")
        val main = kClazz.declaredMethods.find { it.name == "main" }

        // Execute main function to verify our generated items() function works
        print("\n\n---------- BEGIN OF INVOCATION ----------\n\n")
        main?.invoke(null)
        print("\n\n----------- END OF INVOCATION -----------\n\n")
    }

    @Test
    @OptIn(ExperimentalCompilerApi::class)
    fun `test plugin failed with incorrect annotation use`() {
        val source = SourceFile.kotlin(
            "Test.kt", """
            package com.test
            
            import com.composegears.tiamat.navigation.*
            import com.composegears.tiamat.destinations.*
            
            object MyGraph : TiamatGraph
            object OtherGraph : TiamatGraph
            
            @InstallIn(MyGraph::class)
            val Screen1 by navDestination<Unit> { }
            
            @InstallIn(MyGraph::class)
            val Screen2 = NavDestination<Unit>(name = "Screen2", extensions = emptyList()) {}
            
            @InstallIn(MyGraph::class)
            object Screen3 : NavDestination<Int> {
                override val name: String = "Screen3"
                override val extensions: List<Extension<Int>> = emptyList()
                override fun NavDestinationScope<Int>.Content() {}
            }
            
            @InstallIn(OtherGraph::class)
            class Screen4Class : NavDestination<Int> {
                override val name: String = "Screen4"
                override val extensions: List<Extension<Int>> = emptyList()
                override fun NavDestinationScope<Int>.Content() {}
            }
            
            @InstallIn(OtherGraph::class)
            class Foo
            
            @InstallIn(OtherGraph::class)
            object Boo
            
            @InstallIn(MyGraph::class)
            @InstallIn(OtherGraph::class)
            @InstallIn(TiamatGraph::class)
            val Screen4 = Screen4Class()
            
            @InstallIn(OtherGraph::class)
            val a = 1
            
            fun main() {
                println(MyGraph.destinations().joinToString("\n") { it.name })
            }
        """.trimIndent()
        )

        val result = KotlinCompilation().apply {
            sources = listOf(source, annotationSource, navDestinationSource)
            compilerPluginRegistrars = listOf(TiamatDestinationsComponentRegistrar())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertEquals(KotlinCompilation.ExitCode.INTERNAL_ERROR, result.exitCode)
    }
}