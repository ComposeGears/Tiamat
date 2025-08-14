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
            import com.composegears.tiamat.navigation.*
            
            @Repeatable
            @Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
            @Retention(AnnotationRetention.RUNTIME)
            annotation class InstallIn(val target: KClass<*>)
            
            // graph prototype  
            interface TiamatGraph {
                fun destinations(): Array<NavDestination<*>> = emptyArray()
            }
        """.trimIndent()
    )

    val navDestinationSource = SourceFile.kotlin(
        "NavDestination.kt", """
            package com.composegears.tiamat.navigation
            
            import kotlin.properties.ReadOnlyProperty
            import kotlin.reflect.KProperty
            import kotlin.jvm.JvmName
            
            // destination prototype
            abstract class NavDestination<Args>()
            
            // destination impl prototype
            class NavDestinationImpl<T>() : NavDestination<T>()
            
            // nav-destination delegate prototype
            class NavDestinationInstanceDelegate<Args>() : ReadOnlyProperty<Nothing?, NavDestination<Args>> {
                private var destination: NavDestination<Args>? = null
            
                override fun getValue(thisRef: Nothing?, property: KProperty<*>): NavDestination<Args> {
                    if (destination == null) destination = NavDestinationImpl()
                    return destination!!
                }
            }
            
            // destination builder / direct    
            fun <Args> buildNavDestination(): NavDestination<Args> = 
                NavDestinationImpl()
            
            // simple nav destination delegate prototype
            @JvmName("unitNavDestination")
            fun navDestination(): NavDestinationInstanceDelegate<Unit> = 
                NavDestinationInstanceDelegate<Unit>()
            
            // typed nav destination delegate prototype
            inline fun <reified Args : Any> navDestination(): NavDestinationInstanceDelegate<Args> = 
                NavDestinationInstanceDelegate()
        """.trimIndent()
    )

    @Test
    @OptIn(ExperimentalCompilerApi::class)
    fun `compiler # plugin handles multiple destination types`() {
        val source = SourceFile.kotlin(
            "Test.kt", """
            package com.test
            
            import com.composegears.tiamat.navigation.*
            import com.composegears.tiamat.destinations.*
            import org.junit.Assert.assertEquals
            
            object MyGraph : TiamatGraph
            object OtherGraph : TiamatGraph
            
            @InstallIn(MyGraph::class)
            val Screen1 by navDestination<Unit>()
            
            @InstallIn(MyGraph::class)
            val Screen2 = buildNavDestination<Unit>()
            
            @InstallIn(MyGraph::class)
            object Screen3 : NavDestination<Int>()
            
            class Screen4Class : NavDestination<Int>()
            
            @InstallIn(MyGraph::class)
            @InstallIn(OtherGraph::class)
            val Screen4 = Screen4Class()
            
            fun main() {
                val mgd = MyGraph.destinations()
                val ogd = OtherGraph.destinations()
                assertEquals(true, mgd.size == 4)
                assertEquals(true, mgd.contains(Screen1))
                assertEquals(true, mgd.contains(Screen2))
                assertEquals(true, mgd.contains(Screen3))
                assertEquals(true, mgd.contains(Screen4))
                assertEquals(true, ogd.size == 1)
                assertEquals(true, ogd.contains(Screen4))
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
        val params = Array<Any?>(main?.parameterCount ?: 0) { null }
        main?.invoke(null, *params)
        print("\n\n----------- END OF INVOCATION -----------\n\n")
    }

    @Test
    @OptIn(ExperimentalCompilerApi::class)
    fun `compiler # plugin handles nested objects`() {
        val source = SourceFile.kotlin(
            "Test.kt", """
            package com.test
            
            import com.composegears.tiamat.navigation.*
            import com.composegears.tiamat.destinations.*
            import org.junit.Assert.assertEquals
            
            object MyGraph : TiamatGraph
            
            @InstallIn(MyGraph::class)
            val Screen1 by navDestination<Unit>()
            
            class Foo{
                companion object{
                    @InstallIn(MyGraph::class)
                    val Screen2 = buildNavDestination<Unit>()
                }
            }
            
            object Boo{
                @InstallIn(MyGraph::class)
                val Screen3 = buildNavDestination<Unit>()
            }
            
            fun main() {
                val mgd = MyGraph.destinations()
                assertEquals(true, mgd.size == 3)
                assertEquals(true, mgd.contains(Screen1))
                assertEquals(true, mgd.contains(Foo.Screen2))
                assertEquals(true, mgd.contains(Boo.Screen3))
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
        val params = Array<Any?>(main?.parameterCount ?: 0) { null }
        main?.invoke(null, *params)
        print("\n\n----------- END OF INVOCATION -----------\n\n")
    }

    @Test
    @OptIn(ExperimentalCompilerApi::class)
    fun `compiler # plugin failed with incorrect annotation use`() {
        val source = SourceFile.kotlin(
            "Test.kt", """
            package com.test
            
            import com.composegears.tiamat.navigation.*
            import com.composegears.tiamat.destinations.*
            
            object MyGraph : TiamatGraph
            object OtherGraph : TiamatGraph
            
            @InstallIn(MyGraph::class)
            val Screen1 by navDestination<Unit>()
            
            @InstallIn(MyGraph::class)
            val Screen2 = buildNavDestination<Unit>()
            
            @InstallIn(MyGraph::class)
            object Screen3 : NavDestination<Int>()
            
            @InstallIn(OtherGraph::class)
            class Screen4Class : NavDestination<Int>()
            
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
                println(MyGraph.destinations().joinToString("\n") { it::class.simpleName ?: "Unknown" })
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