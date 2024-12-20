<h1 align="center">Tiamat</h1>
<h2 align="center">Compose multiplatform navigation library</h2>

<p align="center">
    <a target="_blank" href="https://github.com/ComposeGears/Tiamat/stargazers"><img src="https://img.shields.io/github/stars/ComposeGears/Tiamat.svg"></a>
    <a href="https://github.com/ComposeGears/Tiamat/network"><img alt="API" src="https://img.shields.io/github/forks/ComposeGears/Tiamat.svg"/></a>
    <a target="_blank" href="https://github.com/ComposeGears/Tiamat/blob/main/LICENSE"><img src="https://img.shields.io/github/license/ComposeGears/Tiamat.svg"></a>
</p>

![](.readme/promo.jpeg)

Add the dependency below to your **module**'s `build.gradle.kts` file:

| Module      |                                                                                            Version                                                                                            |
|-------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| tiamat      |      [![Maven Central](https://img.shields.io/maven-central/v/io.github.composegears/tiamat.svg?style=flat-square)](https://central.sonatype.com/artifact/io.github.composegears/tiamat)      |
| tiamat-koin | [![Maven Central](https://img.shields.io/maven-central/v/io.github.composegears/tiamat-koin.svg?style=flat-square)](https://central.sonatype.com/artifact/io.github.composegears/tiamat-koin) |

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

#### Android / jvm

Use same dependencies in the `dependencies { ... }` section

Why Tiamat?
-----------

- Code generation free
- Pure compose
- Support nested navigation
- Support back-stack alteration (+deep-links)
- Easy to use
- Allow to pass ANY types as data, even lambdas (!under small condition)
- Customizable transitions
- Customizable save-state logic
- Support of Extensions

Setup
-----

1) Define your screens in one of 3 available ways:

   - chaotic good (screen name eq to value name)

       ```kotlin
       val Screen by navDestination<Unit> {
           // content
       }
       ```
   - chaotic neutral
       ```kotlin
    
       val Screen = NavDestination<Unit>("ScreenName") {
           // content
       }
       ```
   - chaotic evil
       ```kotlin
       object Screen : NavDestination<Unit> {
           override val name: String = "ScreenName"
    
           @Composable
           override fun NavDestinationScope<Unit>.Content() {
               // content
           }
    
       }
       ```
2) Create navController
    ```kotlin
     val navController = rememberNavController(
        startDestination = Screen,
        destinations = arrayOf(
            Screen,
            AnotherScreen,
            // ...
        )
     )
    ```
3) Setup navigation
    ```kotlin
    Navigation(navController)
    ```
4) Navigate
    ```kotlin
    val Screen by navDestination<Unit> {
        val navController = navController()
        Column {
            Text("Screen")
            Button(onClick = { navController.navigate(AnotherScreen) }){
                Text("Navigate")
            }
        }
    }
   ```

see example: [App.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/App.kt#L27)

Overview
--------
### Screen

The screens in `Tiamat` should be an entities (similar to composable functions)

the `Args` generic define the type of data, acceptable by screen as `input parameters` in the `NavController:navigate` fun

```kotlin
val RootScreen by navDestination<Unit> {
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

You may create NavController using one of `rememberNavController` functions:

```kotlin
fun rememberNavController(
    key: String? = null,
    storageMode: StorageMode? = null,
    startDestination: NavDestination<*>? = null,
    destinations: Array<NavDestination<*>>,
    configuration: NavController.() -> Unit = {}
)
```

and display as part of any composable function
```kotlin
@Composable
fun Content() {
    val navController = rememberNavController( /*... */)
    Navigation(
        navController = navController,
        modifier = Modifier.fillMaxSize().systemBarsPadding()
    )
}
```

### Extensions

You can attach an extension to any destination, common case is to track screens

```kotlin

// define extension
class AnalyticsExt<T>(private val name: String) : Extension<T>() {
    @Composable
    override fun NavDestinationScope<T>.content() {
        LaunchedEffect(Unit) {
            val service = ... // receive tracker
            service.trackScreen(name)
        }
    }
}

// apply ext to screen
val SomeScreen by navDestination<Unit>(
    AnalyticsExt("SomeScreen")
) {
    // screen content
}

```

NavController will keep the screens data, view models, and states during navigation

> [!IMPORTANT]
> The data may be cleared by system (eg: Android may clear memory)
> 
> Upon restoration state there is few cases depend on `storageMode`

### Storage mode

- `null` - will take parent NavController mode or `Memory` for root controller
- `StorageMode.SavedState` - will store data in `savable` storage (eg: Android -> Bundle) 
> [!IMPORTANT]
> Only 'Savable' types of params & args will be available to use
>
> eg: Android - Parcelable + any bundlable primitives
- `StorageMode.Memory` - store data in memory, allow to use any types of args & params (including lambdas). Reset nav controller upon data loss

### Known limitations
 
> [!IMPORTANT] 
>  `Type checking has run into a recursive problem. Easiest workaround: specify types of your declarations explicitly` ide error.
> 
> 
> ```kotlin
> val SomeScreen1 by navDestination<Unit> {
>   val navController = navController()
>   Button(
>       onClick = { navController.navigate(SomeScreen2) }, // << error here
>       content = { Text("goScreen2") }
>   )
> }
> 
> val SomeScreen2 by navDestination<Unit> {
> val navController = navController()
>   Button(
>       onClick = { navController.navigate(SomeScreen1) }, // << or here
>       content = { Text("goScreen2") }
>   )
> }
> ```
> 
> Appears when it is circular initialization happen (Screen1 knows about Screen2 whot knows about Screen1 ...)
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

#### Simple back and forward navigation:

[1-simple-fb.webm](https://github.com/ComposeGears/Tiamat/assets/3141818/fbf88bc1-d366-4088-ad34-5ac9471d0b18)

#### Bottom bar navigation:

[2-bot-bar.webm](https://github.com/ComposeGears/Tiamat/assets/3141818/e541d4a4-119a-41d4-a1e5-26ef35dc7073)

#### Passing data to next screen:

[3-data-params.webm](https://github.com/ComposeGears/Tiamat/assets/3141818/9d151430-7fe9-47f6-83d2-9c58b700fe9a)

#### Passing data to previous screen:

[4-data-result.webm](https://github.com/ComposeGears/Tiamat/assets/3141818/9706867d-2c88-4d50-8c3d-d5d7d44aade3)

Custom transition:

[5-custom-transition.webm](https://github.com/ComposeGears/Tiamat/assets/3141818/9bfe1545-a321-495f-8d64-8d928746bc81)

### Examples code

- [SimpleForwardBack.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/SimpleForwardBack.kt) - Simple back and forward navigation
- [SimpleReplace.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/SimpleReplace.kt) - Example of `replace` navigation
- [Tabs.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/Tabs.kt) - Bottom navigation example
- [NestedNavigation.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/NestedNavigation.kt) - Nested nav controller interaction
- [DataPassingParams.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/DataPassingParams.kt) - How to pass data to next screen
- [DataPassingFreeArgs.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/DataPassingFreeArgs.kt) - How to pass addition type-free data to next screen (useful to metadata/deeplink)
- [DataPassingResult.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/DataPassingResult.kt) - How to provide result
- [ViewModels.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/ViewModels.kt) - ViewModels usage
- [CustomTransition.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/CustomTransition.kt) - Custom animations/transition
- [CustomStateSaver.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/CustomStateSaver.kt) - Custom save/restore state
- [Root.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/multimodule/Root.kt) - Multi-module communication example (using Signals/Broadcast-api) 
- [BackStackAlteration.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/BackStackAlteration.kt) - Alteration(modification) of backstack (deeplinks)
- [TwoPaneResizableExample.kt](example/app/composeApp/src/commonMain/kotlin/composegears/tiamat/example/TwoPaneResizableExample.kt) - 2 pane example (list+details, dynamic switch between 1-pane or 2-pane layout)
- [KoinIntegrationScreen.kt](example/sample-koin/src/commonMain/kotlin/composegears/tiamat/sample/koin/KoinIntegrationScreen.kt) - Koin integration

Hint
----

### Multiplatform

I want to navigate true multiple nav steps in 1 call (e.g handle deeplink)

```kotlin
// there is 2 common ideas behind handle complex navigation

//---- idea 1 -----
// create some data/param that will be passed via free args 
// each screen handle this arg and opens `next` screen

val DeeplinkScreen by navDestination<Unit> {
    val deeplink = freeArgs<DeeplinkData>() // take free args 

    val deeplinkNavController = rememberNavController(
        key = "deeplinkNavController",
        startDestination = ShopScreen,
        destinations = arrayOf(ShopScreen, CategoryScreen, DetailScreen)
    ) {
        // handle deeplink and open next screen
        // passing eitthe same data or appropriate parts of it
        if (deeplink != null) {  
            editBackStack {
                clear()
                add(ShopScreen)
                add(CategoryScreen, deeplink.categoryId)
            }
            replace(
                dest = DetailScreen,
                navArgs = DetailParams(deeplink.productName, deeplink.productId),
                transition = navigationNone()
            )
            clearFreeArgs()
        }
    }

    Navigation(modifier = Modifier.fillMaxSize(), navController = deeplinkNavController)
}

//---- idea 2 -----
// use route-api

if (deeplink != null) {
    @OptIn(TiamatExperimentalApi::class)
    navController?.route(Route
        .start {
            // we are in the root nav controller
            // in case we are not at DeeplinkScreen
            // clear history and open it
            // else do nothing, route will be continued in the DeeplinkScreen screen
            if (current != DeeplinkScreen) {
                editBackStack {
                    clear()
                    add(MainScreen)
                    add(PlatformExamplesScreen)
                }
                replace(DeeplinkScreen)
            }
        }
        .next {
            // we are in the DeeplinkScreen`s nested nav controller
            // if there is multiple nested controlled - use `next(selector=...)
            // we can check a backstack or just open full flow for deeplink
            editBackStack {
                clear()
                add(ShopScreen)
                add(CategoryScreen, deeplink.categoryId)
            }
            replace(
                dest = DetailScreen,
                navArgs = DetailParams(deeplink.productName, deeplink.productId),
                transition = navigationNone()
            )
        }
    )
    deepLinkController.clearDeepLink()
}
```

---

I use `startDestination = null` + `LaunchEffect` \ `DisposableEffect` to make start destination dynamic and see 1 frame
of animation

```kotlin
    // LaunchEffect & DisposableEffect are executed on `next` frame, so you may see 1 frame of animation
    // to avoid this effect use `configuration` lambda within `rememberNavController` fun
    // see DeeplinkScreen.kt

    val deeplinkNavController = rememberNavController(
        key = "deeplinkNavController",
        startDestination = ShopScreen,
        destinations = arrayOf(ShopScreen, CategoryScreen, DetailScreen)
    ) { // executed right after being created or restored
        // we can do nav actions before 1st screen bing draw without seeing 1st frame
        if (deeplink != null) {
            editBackStack {
                clear()
                add(ShopScreen)
                add(CategoryScreen, deeplink.categoryId)
            }
            replace(
                dest = DetailScreen,
                navArgs = DetailParams(deeplink.productName, deeplink.productId),
                transition = navigationNone()
            )
            clearFreeArgs()
        }
    }

``` 

### Desktop

There is no default 'back' action on desktop

If you want to add one into the `Tiamat` navigation just use the code below:

```kotlin
fun main() = application {
    val backHandler = LocalNavBackHandler.current // < get ref to Global back handler
    Window(
        // ...
        onKeyEvent = { // < add global key event handler
            it.key == Key.Escape && it.type == KeyEventType.KeyUp && backHandler.back() // < call backHandler.back()
        },
        // ...
    ) {
        App()
    }
}
```

### Android

`Tiamat-android` overrides `LocalLifecycleOwner` for each destination and compatible with lifecycle-aware components

See an example of camera usage: [AndroidViewLifecycleScreen.kt](example/app/composeApp/src/androidMain/kotlin/composegears/tiamat/example/platform/AndroidViewLifecycleScreen.kt)

### iOS

Nothing specific (yet)

### Run/Build sample

Android: `./gradlew example:app:composeApp:assembleDebug`

Desktop: `./gradlew example:app:composeApp:run`

Web: `./gradlew example:app:composeApp:wasmJsBrowserRun`

iOS: run XCode project or else use [KMM](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) plugin iOS target

other commands:

- dumps public API: `./gradlew apiDump`

- check API changes: `./gradlew apiCheck`

## Contributors

Thank you for your help! ❤️

<a href="https://github.com/ComposeGears/Tiamat/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=ComposeGears/Tiamat" />
</a>

# License
```
Developed by ComposeGears 2024

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
