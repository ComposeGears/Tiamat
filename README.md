<h2 align="center">Tiamat - Compose multiplatform navigation library</h2>

<div align="center">

[![Stars][badge:stars]][url:gh-stars]
[![Forks][badge:forks]][url:gh-forks]
[![License][badge:license]][url:gh-license]

[![Telegram][badge:telegram-invite]][url:telegram-invite]
[![Slack][badge:slack-invite]][url:slack-invite]

[![Slack][badge:wasm-sample]][url:wasm-sample]

</div>

https://github.com/user-attachments/assets/daa73bec-47f6-42bf-b38f-6378793540ee

Add the dependency below to your **module**'s `build.gradle.kts` file:

| Module                       |                                                       Version                                                        |
|------------------------------|:--------------------------------------------------------------------------------------------------------------------:|
| tiamat                       |                                  [![Tiamat][badge:maven-tiamat]][url:maven-tiamat]                                   |
| tiamat-destinations          |               [![Tiamat destinations][badge:maven-tiamat-destinations]][url:maven-tiamat-destinations]               |
| tiamat-destinations (plugin) | [![Tiamat destinations][badge:maven-tiamat-destinations-gradle-plugin]][url:maven-tiamat-destinations-gradle-plugin] |

Latest stable version: `1.5.2`

[Tiamat Destinations README](doc/tiamat-destinations.md)

[Migration Tiamat 1.* -> Tiamat 2.*](doc/migration-1.5-2.0.md)

#### Multiplatform
```kotlin
sourceSets {
    commonMain.dependencies {
        // core library
        implementation("io.github.composegears:tiamat:$version")
        // Koin integration (https://github.com/InsertKoinIO/koin) 
        implementation("io.github.composegears:tiamat-koin:$version")
    }
}
```

#### Tiamat destinations
```kotlin
plugins {
    // Tiamat-destinations kotlin compiler plugin
    id("io.github.composegears.tiamat.destinations.compiler") version "$version"
}

sourceSets {
    commonMain.dependencies {
        // InstallIn annotations and Graph base class  
        implementation("io.github.composegears:tiamat-destinations:$version")
    }
}

```

#### Android / jvm

Use same dependencies in the `dependencies { ... }` section

Why Tiamat?
-----------

- Code generation free
- Pure compose
- Support nested navigation
- Support back-stack alteration and deep-links
- Easy to use
- Allow to pass ANY types as data, even lambdas (!under small condition)
- Customizable transitions
- Customizable screen placement logic
- Customizable save-state logic
- Support of Extensions

Setup
-----

1) Define your screens:
    ```kotlin
       val Screen by navDestination<Args> {
           // content
       }
    ```
2) Create navController
    ```kotlin
     val navController = rememberNavController(
        key = "Some nav controller",
        startDestination = Screen,
     )
    ```
3) Setup navigation
    ```kotlin
    Navigation(
        navController = navController,
        destinations = arrayOf(
            Screen,
            AnotherScreen,
            // ...,
        ),
        modifier = Modifier.fillMaxSize(),
        contentTransformProvider = { navigationPlatformDefault(it) }
    )
    ```
4) Navigate
    ```kotlin
    val Screen by navDestination<Args> {
        val navController = navController()
        Column {
            Text("Screen")
            Button(onClick = { navController.navigate(AnotherScreen) }){
                Text("Navigate")
            }
        }
    }
   ```

see example: [App.kt](sample/composeApp/src/commonMain/kotlin/composegears/tiamat/sample/App.kt)

Overview
--------
### Screen

The screens in `Tiamat` should be an entities (similar to composable functions)

the `Args` generic define the type of data, acceptable by screen as `input parameters` in the `NavController:navigate` fun

```kotlin
val RootScreen by navDestination<Args> {
    // ...
    val nc = navController()
    // ...
    nc.navigate(DataScreen, DataScreenArgs(1))
    // ...
}

data class DataScreenArgs(val t: Int)

val DataScreen by navDestination<DataScreenArgs> {
    val args = navArgs()
}

```
----
The screen content scoped in `NavDestinationScope<Args>`

The scope provides a number of composable functions:

Some examples:
- `navController` - provides current NavController to navigate back/further
- `navArgs` - the arguments provided to this screen by `NavControllr:navigate(screen, args)` fun
- `navArgsOrNull` - same as `navArgs` but provides `null` if there is no data passed or if it was lost
- `freeArgs` - free type arguments, useful to store metadata or pass deeplink info
- `clearFreeArgs` - clear free type arguments (eg: clear handled deeplink info)
- `navResult` - provide the data passed to `NavControllr:back(screen, navResult)` as result
- `clearNavResult` - clear passed nav result (eg: you want to show notification base on result and clear it not to re-show)
- `rememberViewModel` - create or provide view model scoped(linked) to current screen
- `rememberSharedViewModel` - create or provide view model scoped(linked) to current/provided `NavController`
- `rememberSaveableViewModel` - create or provide saveable view model scoped(linked) to current/provided `NavController` , ViewModel should extend from `TiamatViewModel` and implements `Saveable`

### NavController

You can create NavController using one of `rememberNavController` functions:

```kotlin
fun rememberNavController(
    //...
)
```

and display as part of any composable function
```kotlin
@Composable
fun Content() {
    val navController = rememberNavController( /*... */)
    Navigation(
        navController = navController,
        destinations = arrayOf(
            // ...
        ),
        modifier = Modifier.fillMaxSize()
    )
}
```

NavController will keep the screens data, view models, and states during navigation

> [!IMPORTANT]
> The data may be cleared by system (eg: Android may clear memory)
> 
> ```kotlin
> fun rememberNavController(
>   // ...
>   saveable: Boolean? = null,
>   // ...
> )
> ```
> `saveable` property of remembered nav controller will indicate if we need to save/restore state or no 

### Extensions

You can attach an extension to any destination<br>
There is 2 extension types: with and without content<br>
The content-extension allows to process content before destination body and after by specifying type (`Overlay`, `Underlay`)<br>
Here is simple tracker extension:

```kotlin

// define extension
class AnalyticsExt(private val name: String) : ContentExtension<Any?>() {

    @Composable
    override fun NavDestinationScope<out Any?>.Content() {
        val entry = navEntry()
        LaunchedEffect(Unit) {
            val service = /*...*/ // receive tracker
            service.trackScreen(screenName = name, destination = entry.destination.name)
        }
    }
}

// apply ext to screen
val SomeScreen by navDestination<Args>(
    AnalyticsExt("SomeScreen")
) {
    // screen content
}

```

### Storage mode

> [!IMPORTANT]
> Only 'Savable' types of params & args will be available to use within `saveable` nav controllers
>
> eg: Android - Parcelable + any bundlable primitives

### Known limitations
 
> [!IMPORTANT] 
>  `Type checking has run into a recursive problem. Easiest workaround: specify types of your declarations explicitly` ide error.
> 
> 
> ```kotlin
> val SomeScreen1 by navDestination<Args> {
>   val navController = navController()
>   Button(
>       onClick = { navController.navigate(SomeScreen2) }, // << error here
>       content = { Text("goScreen2") }
>   )
> }
> 
> val SomeScreen2 by navDestination<Args> {
> val navController = navController()
>   Button(
>       onClick = { navController.navigate(SomeScreen1) }, // << or here
>       content = { Text("goScreen2") }
>   )
> }
> ```
> 
> Appears when it is circular initialization happen (Screen1 knows about Screen2 who knows about Screen1 ...)
> 
> Solution: just define types of root(any in chain) screens explicitly 
> 
> ```kotlin
> val SomeScreen1: NavDestination<Unit> by navDestination {  /* ... */ }
> ```

> [!IMPORTANT]
> Why is my system back button works wired with custom back handler?
> 
> While using custom back handler do not forget 2 rules
> 1) Always place `NavBackHandler` before `Navigation`
> 2) use `Navigation(handleSystemBackEvent = false)` flag to disable extra back handler

Samples
-------

See the examples [here](sample/composeApp/src/commonMain/kotlin/composegears/tiamat/sample/content)

Or try them in browser (require WASM support) [here](https://composegears.github.io/Tiamat/) 

Hint
----

### Multiplatform

#### I want to navigate through multiple nav steps in 1 call (e.g. handle deeplink)

```kotlin
// there is 2 common ideas behind handle complex navigation

//---- idea 1 -----
// create some data/param that will be passed via free args 
// each screen handle this arg and opens `next` screen

val DeeplinkScreen by navDestination<Args> {
    val deeplink = freeArgs<DeeplinkData>() // take free args 

    val deeplinkNavController = rememberNavController(
        key = "deeplinkNavController",
        startDestination = ShopScreen
    ) {
        // handle deeplink and open next screen
        if (deeplink != null) {
            editNavStack { _->
                listOf(
                    ShopScreen.toNavEntry(),
                    CategoryScreen.toNavEntry(navArgs = deeplink.categoryId),
                    DetailScreen.toNavEntry(navArgs = DetailParams(deeplink.productName, deeplink.productId))
                )
            }
            clearFreeArgs()
        }
    }

    Navigation(/*...*/)
}

//---- idea 2 -----
// use route-api

if (deeplink != null) {
    navController?.route {
        element(ShopScreen)
        element(CategoryScreen.toNavEntry(navArgs = deeplink.categoryId))
        element(DetailScreen.toNavEntry(navArgs = DetailParams(deeplink.productName, deeplink.productId)))
    }
    deepLinkController.clearDeepLink()
}
```

---

#### I use `startDestination = null` + `LaunchEffect` \ `DisposableEffect` to make start destination dynamic and see 1 frame of animation

```kotlin
    // LaunchEffect & DisposableEffect are executed on `next` frame, so you may see 1 frame of animation
    // to avoid this effect use `configuration` lambda within `rememberNavController` fun

    val deeplinkNavController = rememberNavController(
        key = "deeplinkNavController",
        startDestination = ShopScreen,
    ) { // executed right after being created or restored
        // so you can handle initial navigation here without any animations
    }

```
---

#### How about 2-pane & custom layout?

```kotlin
    // Yep, there is 2-pane layout example. You can also create fully custom layout by using `scene` api

    val nc = rememberNavController(
        key = "nav controller",
        startDestination = SomeDest1,
    )
    // using scene api
    NavigationScene(
        navController = nc,
        destinations = arrayOf(
            SomeDest1,
            SomeDest2,
            SomeDest3,
        )
    ) {
        // place you destinations as you want ( !!!CAUTION!!! do not render same entry twice in a frame)
        AnimatedContent(
            targetState = nc.currentNavEntryAsState(),
            contentKey = { it?.contentKey() },
            transitionSpec = { navigationFadeInOut() }
        ) {
            // you can also draw an entries from the whole nav stack if you need (but be careful)
            EntryContent(it)
        }
    }

```

#### Compose Preview for NavDestination

Library provides a utility
function [TiamatPreview](tiamat/src/commonMain/kotlin/com/composegears/tiamat/compose/ComposablePreview.kt)
for previewing individual navigation destinations in Compose Preview.

> [!NOTE]
> Preview works best for pure Compose UI code. If your destination contains ViewModels, dependency injection, or complex app logic, consider creating separate preview functions for specific UI components instead of the entire destination.

**Usage:**

```kotlin
// Define your destination
val DemoScreen by navDestination<Unit> {
    Text("Demo Screen")
}

// Create preview
@Preview
@Composable
private fun DemoScreenPreview() {
    TiamatPreview(destination = DemoScreen)
}
```

**For screens with arguments:**

```kotlin
data class UserProfileArgs(val userId: String, val userName: String)

val UserProfileScreen by navDestination<UserProfileArgs> {
    val args = navArgs()
    Column {
        Text("User: ${args.userName}")
        Text("ID: ${args.userId}")
    }
}

@Preview
@Composable
private fun UserProfileScreenPreview() {
    TiamatPreview(
        destination = UserProfileScreen,
        navArgs = UserProfileArgs(userId = "123", userName = "John")
    )
}
```

**For complex destinations with ViewModels or app logic:**

```kotlin
// Instead of previewing the entire destination
val ComplexScreen by navDestination<Unit> {
    val viewModel = viewModel<MyViewModel>()
    val data by viewModel.data.collectAsState()
    
    ComplexScreenContent(data = data)
}

// Create preview for the UI component
@Composable
private fun ComplexScreenContent(data: MyData) {
    Column {
        Text("Title: ${data.title}")
        // ... rest of UI
    }
}

@Preview
@Composable
private fun ComplexScreenContentPreview() {
    ComplexScreenContent(
        data = MyData(title = "Preview Title")
    )
}
```

### Desktop

Nothing specific (yet)

### Android

`Tiamat` overrides `LocalLifecycleOwner` for each destination. This makes it compatible with lifecycle-aware components

See an example of CameraX usage: [CameraXLifecycleScreen.kt](sample/composeApp/src/androidMain/kotlin/composegears/tiamat/sample/platform/CameraXLifecycleScreen.kt)

### iOS

Nothing specific (yet)

### Run/Build sample

Android: `./gradlew sample:composeApp:assembleDebug`

Jvm: `./gradlew sample:composeApp:run`

Jvm + hot-reload: `./gradlew sample:composeApp:hotRunJvm`

Web: `./gradlew sample:composeApp:wasmJsBrowserDevelopmentRun`

iOS: run XCode project or else use [KMP plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform) iOS target

other commands:

- check ABI: `./gradlew checkAbi`

- update ABI: `./gradlew updateAbi`
  
- kover html report: `./gradlew :tiamat:koverHtmlReport`
  
- print test coverage: `./gradlew :tiamat:koverLog`

- run detekt checks: `./gradlew detekt`

## Contributors

Thank you for your help! ❤️

<a href="https://github.com/ComposeGears/Tiamat/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=ComposeGears/Tiamat" />
</a>

# License
```
Developed by ComposeGears 2025

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[badge:stars]: https://img.shields.io/github/stars/ComposeGears/Tiamat.svg?style=for-the-badge&labelColor=black&color=white
[badge:forks]: https://img.shields.io/github/forks/ComposeGears/Tiamat.svg?style=for-the-badge&labelColor=black&color=white
[badge:license]: https://img.shields.io/github/license/ComposeGears/Tiamat?style=for-the-badge&labelColor=black&color=white

[badge:slack-invite]: https://img.shields.io/badge/slack-blue.svg?logo=slack&style=for-the-badge&labelColor=black&color=white
[badge:telegram-invite]: https://img.shields.io/badge/Telegram-2CA5E0?logo=telegram&style=for-the-badge&labelColor=black&color=white&logoColor=white
[badge:wasm-sample]: https://img.shields.io/badge/Kotlin%2FWASM%20%7C%20Online%20demo-000000?logo=webassembly&style=for-the-badge&color=black&logoColor=white

[badge:maven-tiamat]: https://img.shields.io/maven-central/v/io.github.composegears/tiamat.svg?style=for-the-badge&logo=apachemaven&label=&labelColor=black&color=white
[badge:maven-tiamat-koin]: https://img.shields.io/maven-central/v/io.github.composegears/tiamat-koin.svg?style=for-the-badge&logo=apachemaven&label=&labelColor=black&color=white
[badge:maven-tiamat-destinations]: https://img.shields.io/maven-central/v/io.github.composegears/tiamat-destinations.svg?style=for-the-badge&logo=apachemaven&label=&labelColor=black&color=white
[badge:maven-tiamat-destinations-gradle-plugin]: https://img.shields.io/maven-central/v/io.github.composegears/tiamat-destinations-gradle-plugin.svg?style=for-the-badge&logo=gradle&label=&labelColor=black&color=white

[url:gh-stars]: https://github.com/ComposeGears/Tiamat/stargazers
[url:gh-forks]: https://github.com/ComposeGears/Tiamat/network
[url:gh-license]: https://github.com/ComposeGears/Tiamat/blob/main/LICENSE

[url:telegram-invite]: https://t.me/composegears
[url:slack-invite]: https://join.slack.com/t/composegears/shared_invite/zt-2noleve52-D~zrFPmC1cdhThsuQUW61A
[url:wasm-sample]: https://composegears.github.io/Tiamat/

[url:maven-tiamat]: https://central.sonatype.com/artifact/io.github.composegears/tiamat
[url:maven-tiamat-koin]: https://central.sonatype.com/artifact/io.github.composegears/tiamat-koin
[url:maven-tiamat-destinations]: https://central.sonatype.com/artifact/io.github.composegears/tiamat-destinations
[url:maven-tiamat-destinations-gradle-plugin]: https://central.sonatype.com/artifact/io.github.composegears/tiamat-destinations-gradle-plugin
