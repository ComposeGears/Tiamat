Tiamat Destinations
-------------------

> [!IMPORTANT]
> This is experimental api

Tiamat destinations library allow to generate destination list for navController automatically
by applying `InstallIn` annotation on `navDestiantions`

## Setup

```kotlin
// Apply compiler plugin in the plugins section
plugins {
    id("io.github.composegears.tiamat.destinations.compiler") version "1.0.0"
}

```
```kotlin
// Add dependency
sourceSets {
    commonMain.dependencies {
        implementation("io.github.composegears:tiamat-destinations:$version")
    }
}

```

## Usage

Create graph `object` extends from `TiamatGraph`

```kotlin
private object Graph : TiamatGraph
```
Annotate `navDestination` with `InstallIn` annotation (multiple installations allowed), here is some valid options:

```kotlin
// Using delegate
@InstallIn(Graph::class)
val Screen1 by navDestination<Unit> { }

// Using constructor
@InstallIn(Graph::class)
val Screen2 = NavDestination<Unit>(name = "Screen2", extensions = emptyList()) {}

// using object
@InstallIn(Graph::class)
object Screen3 : NavDestination<Int> {
    override val name: String = "Screen3"
    override val extensions: List<Extension<Int>> = emptyList()

    @Composable
    override fun NavDestinationScope<Int>.Content() {
    }
}

// NOT ALLOWED HERE
class Screen4Class : NavDestination<Int> {
    override val name: String = "Screen4"
    override val extensions: List<Extension<Int>> = emptyList()

    @Composable
    override fun NavDestinationScope<Int>.Content() {
    }
}

// Using global instance property
@InstallIn(Graph::class)
@InstallIn(SomneOtherGraph::class)
val Screen4 = Screen4Class()
```
Use graph instead of `destinations`

```kotlin
val nc = rememberNavController(
    key = "Key",
    startDestination = Screen1,
    graph = Graph // use it here
)
```

Multiple graphs usage also allowed
```kotlin
///...
private object Graph1 : TiamatGraph
private object Graph2 : TiamatGraph

//...
val nc = rememberNavController(
    key = "Key",
    startDestination = Screen1,
    graph = Graph1 + Graph2
)
```

## Emergency case

In case the plugin problems, there is backport solution: override graph `destinations` function manually and disable compiler plugin

```kotlin
private object Graph : TiamatGraph{
    override fun destinations(): Array<NavDestination<*>> = arrayOf(
        Screen1, Screen2, Screen3
    )
}
```