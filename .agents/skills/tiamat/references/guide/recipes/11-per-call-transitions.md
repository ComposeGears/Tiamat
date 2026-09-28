# Per-Call Transitions

Override animations for a single navigation call.

## How it works

Pass a `ContentTransform` to any navigation call to override the global animation for that single transition.

```kotlin
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import com.composegears.tiamat.compose.back
import com.composegears.tiamat.compose.navigate
import com.composegears.tiamat.compose.navigationSlideInFromBottom
import com.composegears.tiamat.compose.navigationSlideOutToBottom

nc.navigate(ModalScreen, transition = scaleIn() togetherWith scaleOut())
nc.navigate(SheetScreen, transition = navigationSlideInFromBottom())
nc.back(transition = navigationSlideOutToBottom())
```

Built-in helpers: `navigationFadeInOut()`, `navigationSlideInOut(isForward)`, `navigationSlideInFromBottom()`, `navigationSlideOutToBottom()`, `navigationNone()`, `navigationPlatformDefault(isForward)`.

