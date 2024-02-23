[![Maven Central](https://img.shields.io/maven-central/v/io.github.composegears/tiamat)](https://central.sonatype.com/artifact/io.github.composegears/tiamat)

Tiamat
======

`Tiamat` is a kmm navigation library

![](.readme/promo.jpeg)

```kotlin
implementation("io.github.composegears:tiamat:0.1.0-alpha02")
```

Why Tiamat?
-----------

- Code generation free
- Pure compose
- Support nested navigation
- Support back-stack alteration (+deep-links)
- Easy to use
- Allow to pass ANY types as data, even lambdas (!under small condition)
- Customizable transitions
- No lifecycle. Just use compose DisposableEffect.

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
        Column{
            Text("Screen")
            Button(onClick = {
                navController.navigate(AnotherScreen)
            }){
                Text("Navigate")
            }
        }
    }
   ```

see example: [App.kt](example/composeApp/src/commonMain/kotlin/App.kt#L16)

Overview
--------
### Screen

todo: explain screen params & args


### NavController

You may create NavController using `rememberNavController` function:

```kotlin
fun rememberNavController(
    key: Any? = null,
    storageMode: StorageMode? = null,
    startDestination: NavDestination<*>? = null,
    destinations: Array<NavDestination<*>>
)
```

NavController will keep the screens data, view models, and stated during navigation

> [!IMPORTANT]
> The data may be cleared by system (eg: Android may clear memory)
> 
> Upon restoration state there is few cases depend on `storageMode`

### Storage mode

- `null` - will take parent NavController mode or ResetOnDataLoss for root controller
- `StorageMode.Savable` - will store data in `savable` storage (eg: Android -> Bundle) 
> [!IMPORTANT]
> Only 'Savable' types of params & args will be available to use
>
> eg: Android - Parcelable + any bundlable primitives
- `StorageMode.ResetOnDataLoss` - store data in memory, reset nav controller upon data loss
- `StorageMode.IgnoreDataLoss` - store data in memory, restore nav back stack, ignore data loss

### Known limitations
 
> [!IMPORTANT] 
>  Type checking has run into a recursive problem. Easiest workaround: specify types of your declarations explicitly
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
> val SomeScreen1: NavDestination<Unit> by navDestination { // ...
> ```

> [!IMPORTANT]
> No data exception
> 
> Using  `storageMode = StorageMode.DataStore.IgnoreDataLoss` in the `rememberNavController`
> 
> Within screens `val navArgs = navArgs()` 
> 
> May cause error in case internal data storage where cleared (eg: Android may release memory of activity)
> 
> Solution: there is safe version `val navArgs = navArgsOrNull()` 


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

### Run/Build sample

Android: `./gradlew example:composeApp:assembleDebug`

Desktop: `./gradlew example:composeApp:run`

iOs: run XCode project or else use [KMM](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) plugin iOs target