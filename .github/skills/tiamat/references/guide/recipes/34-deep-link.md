# Deep Link

Parse deep-link URIs into routes using pattern-based matching.

## How it works

`DeepLink` lets you register URI patterns with placeholders and bind them to
route builders. On `parse`, the URI is matched against registered patterns and
captured values are delivered as a `List<String>`.

### Placeholder syntax

| Form | Meaning |
|------|---------|
| `{.*}` | Anonymous placeholder — matches any single path segment |
| `{name}` | Named placeholder — name is cosmetic; value is delivered positionally |

### Basic usage

```kotlin
import com.composegears.tiamat.TiamatExperimentalApi
import com.composegears.tiamat.navigation.DeepLink

@OptIn(TiamatExperimentalApi::class)
val AppDeepLink = DeepLink {
    // zero-param pattern — matches "shop" exactly
    bind("shop") {
        destination("ShopScreen")
    }
    // single param — {id} captures the product id
    bind("shop/product/{id}") { params ->
        destination("ShopScreen")
        element(ProductScreen.toNavEntry(navArgs = params[0].toInt()))
    }
}

// Parse and route:
val route = AppDeepLink.parse("app://example.com/shop/product/42")
navController.route(route)
```

### Multiple patterns per builder

Bind several URI shapes to the same builder when path-style and query-style
deep links should produce the same route:

```kotlin
@OptIn(TiamatExperimentalApi::class)
val AppDeepLink = DeepLink {
    bind(
        "shop/product/{id}",       // path form:  shop/product/42
        "shop/product?id={id}",    // query form: shop/product?id=42
    ) { params ->
        destination("ShopScreen")
        element(ProductScreen.toNavEntry(navArgs = params[0].toInt()))
    }
}
```

### Multi-param patterns with nested NavControllers

Use multiple placeholders and `navController(key)` to build deep routes that
span nested navigation hosts:

```kotlin
@OptIn(TiamatExperimentalApi::class)
val AppDeepLink = DeepLink {
    bind("shop/product/{pid}/feedback/{fid}") { params ->
        destination("ShopScreen")
        element(ProductScreen.toNavEntry(navArgs = params[0].toInt()))
        navController("product-nested-nc")
        element(FeedbackScreen.toNavEntry(navArgs = params[1].toInt()))
    }
}

val route = AppDeepLink.parse("app://x/shop/product/42/feedback/34")
navController.route(route)
// Result: ShopScreen → ProductScreen[42] → (nested nc) → FeedbackScreen[34]
```

### Using `element()` with typed NavEntries

In real apps you'll typically use `element(...)` with `toNavEntry()` instead of
`destination("name")`:

```kotlin
import com.composegears.tiamat.navigation.NavDestination.Companion.toNavEntry

@OptIn(TiamatExperimentalApi::class)
val AppDeepLink = DeepLink {
    bind("shop") {
        element(ShopScreen.toNavEntry())
    }
    bind("shop/product/{id}", "shop/product?id={id}") { params ->
        element(ProductScreen.toNavEntry(navArgs = params[0].toInt()))
    }
    bind(
        "shop/product/{pid}/feedback/{fid}",
        "shop/product?id={pid}/feedback?id={fid}",
    ) { params ->
        element(ProductScreen.toNavEntry(navArgs = params[0].toInt()))
        navController("product-nested-nc")
        element(FeedbackScreen.toNavEntry(navArgs = params[1].toInt()))
    }
}
```

## Notes

- Patterns are matched in **registration order** — the first match wins. Register more-specific patterns before less-specific ones if there is potential ambiguity.
- The `scheme://host` prefix (e.g. `app://example.com/`) is stripped automatically before matching; patterns should only describe the path portion.
- `?` is treated as a literal character within a path segment (the URI is split on `/` only), so `product?id={id}` works as expected.
- Duplicate pattern strings across `bind` calls throw `IllegalArgumentException` at construction time.
- An unmatched URI or an empty route result throw `IllegalArgumentException` at parse time.
- `DeepLink` is marked `@TiamatExperimentalApi`.

