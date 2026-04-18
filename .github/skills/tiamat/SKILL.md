---
name: tiamat
description: Learn how to use Tiamat — a Compose Multiplatform navigation library.
  Covers destination basics, arguments and results, advanced navigation actions, generated
  graphs, ViewModel and retained state, custom layouts and transitions, and extensions.
metadata:
  author: ComposeGears
  keywords:
  - recipe
  - Kotlin Multiplatform
  - Compose Multiplatform
  - Tiamat
  - navigation
  - navDestination
  - NavController
  - Navigation
  - NavigationScene
  - ViewModel
  - retain
  - TiamatGraph
  - extensions
  - transitions
  - adaptive layout
---

## Developer documentation

- *[Tiamat overview](references/guide/index.md)*: Overview of the Tiamat navigation library — core concepts, API surface, and how to get started.

## Recipes

Code examples showcasing common patterns.

### Destination basics

- *[Simple screen](references/guide/recipes/01-simple-screen.md)*: Create a screen with `navDestination` and wire it into a `Navigation` host.
- *[Navigate between screens](references/guide/recipes/02-navigate-between-screens.md)*: Perform forward and back navigation between destinations.
- *[Preview destinations](references/guide/recipes/03-preview-destinations.md)*: Render any destination inside a Compose `@Preview` with `TiamatPreview`.
- *[Global transition style](references/guide/recipes/04-global-transition-style.md)*: Override the default animation for an entire `Navigation` host.

### Arguments and results

- *[Typed arguments](references/guide/recipes/05-typed-arguments.md)*: Declare a destination with required or optional typed args.
- *[Free args](references/guide/recipes/06-free-args.md)*: Pass lightweight transient data with `freeArgs`.
- *[Serializable args](references/guide/recipes/07-serializable-args.md)*: Use `NavData` + `@Serializable` for args that survive process death.
- *[Back results](references/guide/recipes/08-back-results.md)*: Pass typed results back to the previous destination.

### Common UI

- *[Tab navigation](references/guide/recipes/09-tab-navigation.md)*: Tab navigation where each tab has its own independent back stack.

### Advanced navigation actions

- *[Replace and pop-to-top](references/guide/recipes/10-replace-and-pop-to-top.md)*: Use `replace`, `back(to=…)`, and `popToTop` for tab-style and flow navigation.
- *[Per-call transitions](references/guide/recipes/11-per-call-transitions.md)*: Override animations for a single navigation call.
- *[Route API](references/guide/recipes/12-route-api.md)*: Build a multi-step back stack in one call or navigate by destination name.
- *[Edit nav stack](references/guide/recipes/13-edit-nav-stack.md)*: Arbitrary stack manipulation with `editNavStack`.
- *[Observe NavController state](references/guide/recipes/14-observe-navcontroller-state.md)*: Drive UI from reactive NavController state.
- *[NavController configuration](references/guide/recipes/15-navcontroller-configuration.md)*: Apply a configuration callback at NavController creation time.
- *[Nested NavControllers](references/guide/recipes/16-nested-navcontrollers.md)*: Navigate across nested NavController hierarchies.

### Generated graph

- *[Graph setup and usage](references/guide/recipes/17-graph-setup-and-usage.md)*: Configure `TiamatGraph`, `@InstallIn`, install in multiple graphs, and merge graphs.

### ViewModel and state

- *[Screen-scoped ViewModel](references/guide/recipes/18-screen-scoped-viewmodel.md)*: Scope a ViewModel to a single screen.
- *[Shared ViewModel](references/guide/recipes/19-shared-viewmodel.md)*: Share a ViewModel across all destinations under a NavController.
- *[Retain](references/guide/recipes/20-retain.md)*: Retain objects across backward navigation with `retain {}`.
- *[Produce retained state](references/guide/recipes/21-produce-retained-state.md)*: Coroutine-driven retained state with `produceRetainedState`.
- *[SavedStateHandle ViewModel](references/guide/recipes/22-savedstatehandle-viewmodel.md)*: ViewModel with `SavedStateHandle` for process-death survival.
- *[Custom save and restore state](references/guide/recipes/23-custom-save-restore-state.md)*: Manually save and restore a NavController's state with `saveToSavedState()`.

### Architecture

- *[Koin ViewModel](references/guide/recipes/24-koin-viewmodel.md)*: Use Koin's `koinViewModel` with Tiamat for dependency-injected ViewModels.
- *[Hilt ViewModel](references/guide/recipes/25-hilt-viewmodel.md)*: Use Hilt's `hiltViewModel` with Tiamat for dependency-injected ViewModels on Android.

### Custom layouts and transitions

- *[NavigationScene basics](references/guide/recipes/26-navigationscene-basics.md)*: Full layout control with `NavigationScene`.
- *[Overlay destinations](references/guide/recipes/27-overlay-destinations.md)*: Dialogs and bottom sheets rendered in the nav stack.
- *[Two-pane layout](references/guide/recipes/28-two-pane-layout.md)*: List-detail / two-pane adaptive layout.
- *[Adaptive list-detail](references/guide/recipes/29-adaptive-list-detail.md)*: Responsive layout that switches between single-pane and two-pane based on window size.
- *[Gesture transitions](references/guide/recipes/30-gesture-transitions.md)*: Gesture-driven / seekable transitions with `TransitionController`.
- *[Shared-element transitions](references/guide/recipes/31-shared-element-transitions.md)*: Shared-element transitions with `LocalNavAnimatedVisibilityScope`.

### Extensions

- *[Marker extensions](references/guide/recipes/32-marker-extensions.md)*: Attach metadata to destinations with `NavExtension`.
- *[Content extensions](references/guide/recipes/33-content-extensions.md)*: Inject composable UI overlays/underlays with `ContentExtension`.
