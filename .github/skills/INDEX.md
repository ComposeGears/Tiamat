# Tiamat Skills Index

- `tiamat-01-destination-basics/SKILL.md`
  - Create simple screens with `navDestination`.
  - Attach screens to a `Navigation` host.
  - Perform basic forward/back navigation.
  - Preview destinations in isolation with `TiamatPreview`.
  - Configure `saveable`, `handleSystemBackEvent`, and `contentTransformProvider`.

- `tiamat-02-arguments-and-results/SKILL.md`
  - Create typed screens with `navDestination<Args>`.
  - Pass args during `navigate`; read with `navArgs()` or `navArgsOrNull()`.
  - Pass transient in-memory data with `freeArgs`.
  - Use `NavData` + `@Serializable` for args that survive process death.
  - Pass typed results back to previous destinations.

- `tiamat-03-extra-navigation-actions/SKILL.md`
  - Use `replace`, `back(to = ...)`, `back(inclusive)`, `back(recursive)`, and `popToTop`.
  - Per-call transition overrides and built-in animation helpers.
  - Route-based navigation by reference or destination name.
  - Stack editing with `editNavStack`.
  - Observe NavController state: `navStackAsState`, `canNavigateBackAsState`, `currentNavDestinationAsState`.
  - Navigation listener and `findParentNavController`.

- `tiamat-04-graph-usage/SKILL.md`
  - Configure graph generation prerequisites (`libs.plugins.tiamat.destinations.compiler`).
  - Register destinations via `@InstallIn` and `TiamatGraph`.
  - Install a destination in multiple graphs (repeatable annotation).
  - Merge graphs from different modules with the `+` operator.

- `tiamat-05-viewmodel-and-state/SKILL.md`
  - Screen-scoped and NavController-scoped ViewModels.
  - `retain {}` for object retention across backward navigation.
  - `produceRetainedState` for coroutine-driven retained state.
  - `SavedStateHandle` ViewModel and `saveable` parameter for process-death survival.

- `tiamat-06-custom-layouts/SKILL.md`
  - `NavigationScene` for full layout control.
  - Overlay destinations pattern (dialogs/sheets in the nav stack).
  - Two-pane / list-detail adaptive layout.
  - `TransitionController` for gesture-driven/seekable transitions.
  - Shared-element transitions with `LocalNavAnimatedVisibilityScope`.

- `tiamat-07-extensions/SKILL.md`
  - Marker extensions (`NavExtension`) for metadata and runtime guards.
  - Data-carrying marker extensions.
  - `ContentExtension` for composable UI overlays and underlays.
  - `extension {}` helper for quick one-off content extensions.
  - Query extensions with `ext<T>()` from outside or inside a destination.
