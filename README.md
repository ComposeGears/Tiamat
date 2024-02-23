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

Overview
--------

classes and theis understanding

Samples
-------
[5-custom-transition.webm](https://github.com/ComposeGears/Tiamat/assets/3141818/9bfe1545-a321-495f-8d64-8d928746bc81)

[4-data-result.webm](https://github.com/ComposeGears/Tiamat/assets/3141818/9706867d-2c88-4d50-8c3d-d5d7d44aade3)

[3-data-params.webm](https://github.com/ComposeGears/Tiamat/assets/3141818/9d151430-7fe9-47f6-83d2-9c58b700fe9a)

[2-bot-bar.webm](https://github.com/ComposeGears/Tiamat/assets/3141818/e541d4a4-119a-41d4-a1e5-26ef35dc7073)

[1-simple-fb.webm](https://github.com/ComposeGears/Tiamat/assets/3141818/fbf88bc1-d366-4088-ad34-5ac9471d0b18)




### Run sample

Android: `./gradlew example:composeApp:assembleDebug`

Desktop: `./gradlew example:composeApp:run`

[//]: # (TODO add iOs run action)
