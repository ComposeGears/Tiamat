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
https://github.com/ComposeGears/Tiamat/blob/feature/readme/.readme/1-simple-fb.webm

A1
[!(renamed webm)](https://github.com/ComposeGears/Tiamat/blob/feature/readme/.readme/1-simple-fb.webm)

A2
[!(renamed webm)](.readme/1-simple-fb.webm)

![1-simple-fb.webm](.readme/1-simple-fb.webm)

![2-bot-bar.webm](.readme/2-bot-bar.webm)

![3-data-params.webm](.readme/3-data-params.webm)

![4-data-result.webm](.readme/4-data-result.webm)

![5-custom-transition.webm](.readme/5-custom-transition.webm)

### Run sample

Android: `./gradlew example:composeApp:assembleDebug`

Desktop: `./gradlew example:composeApp:run`

[//]: # (TODO add iOs run action)