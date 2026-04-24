package com.composegears.tiamat.navigation

import com.composegears.tiamat.TiamatExperimentalApi

/**
 * Pattern-based deep-link parser that converts URIs into a [Route]
 * consumable by [NavController.route].
 *
 * Register URI patterns with [bind] — each pattern is a path template where
 * placeholders (`{.*}` or `{name}`) are replaced by capture groups at
 * registration time. Multiple patterns can be bound to the same builder.
 *
 * On [parse], the URI is stripped of its `scheme://host` prefix, split on
 * `/`, rejoined, and matched against registered patterns in registration
 * order. The first matching pattern wins; captured values are passed to the
 * bound builder as a `List<String>`.
 *
 * Typical usage:
 * ```kotlin
 * val AppDeepLink = DeepLink {
 *     bind("shop") {
 *         destination("ShopScreen")
 *     }
 *     bind("shop/product/{id}", "shop/product?id={id}") { params ->
 *         element(ProductDetails.toNavEntry(ProductArgs(params[0].toInt())))
 *     }
 *     bind("shop/product/{pid}/feedback/{fid}") { params ->
 *         destination("HomeScreen")
 *         element(ProductDetails.toNavEntry(ProductArgs(params[0].toInt())))
 *         navController("pd-nested")
 *         element(Feedback.toNavEntry(FeedbackArgs(params[1].toInt())))
 *     }
 * }
 * navController.route(AppDeepLink.parse("app://x/shop/product/42/feedback/34"))
 * ```
 */
@TiamatExperimentalApi
public class DeepLink(builder: DeepLink.() -> Unit) {

    private companion object {
        private val schemeRegex = Regex("^[a-zA-Z][a-zA-Z0-9+\\-.]*://[^/]*/?")
        private val placeholderRegex = Regex("\\{[^}]+\\}")

        fun stripScheme(uri: String): String =
            uri.replace(schemeRegex, "").trim('/')

        fun compilePattern(pattern: String): Regex {
            val normalized = pattern.trim('/')
            val result = StringBuilder("^")
            var pos = 0
            for (match in placeholderRegex.findAll(normalized)) {
                // escape literal part before this placeholder
                if (match.range.first > pos) {
                    result.append(Regex.escape(normalized.substring(pos, match.range.first)))
                }
                result.append("([^/]+)")
                pos = match.range.last + 1
            }
            // escape literal tail
            if (pos < normalized.length) {
                result.append(Regex.escape(normalized.substring(pos)))
            }
            result.append("$")
            return Regex(result.toString())
        }
    }

    private class Binding(
        val regexes: List<Regex>,
        val builder: Route.(List<String>) -> Unit,
    )

    private val bindings = mutableListOf<Binding>()
    private val registeredPatterns = mutableSetOf<String>()

    init {
        builder()
    }

    /**
     * Registers one or more URI [patterns] and binds them to a [builder]
     * that will receive extracted placeholder values as a positional list.
     *
     * Each pattern is compiled into a regex at registration time. Placeholders
     * `{.*}` and `{name}` are both supported.
     *
     * @throws IllegalArgumentException if any pattern string was already
     * registered in a previous [bind] call.
     */
    public fun bind(vararg patterns: String, builder: Route.(params: List<String>) -> Unit) {
        for (i in patterns) {
            require(i !in registeredPatterns) { "DeepLink: duplicate pattern '$i'" }
        }
        registeredPatterns.addAll(patterns)
        bindings.add(
            Binding(
                regexes = patterns.map { compilePattern(it) },
                builder = builder,
            )
        )
    }

    /**
     * Parses the given [uri] into a [Route].
     *
     * The URI is stripped of its `scheme://host` prefix and matched against
     * registered patterns in registration order. The first full match wins;
     * captured placeholder values are passed to the bound builder.
     *
     * @throws IllegalArgumentException if no pattern matches the URI or if
     * the resulting route is empty.
     */
    public fun parse(uri: String): Route {
        val path = stripScheme(uri)
        require(path.isNotEmpty()) { "DeepLink: empty path in '$uri'" }
        val route = Route(emptyList())
        for (binding in bindings) {
            for (regex in binding.regexes) {
                val match = regex.matchEntire(path)
                if (match != null) {
                    val params = match.groupValues.drop(1)
                    binding.builder.invoke(route, params)
                    require(route.elements.isNotEmpty()) {
                        "DeepLink: parse produced an empty route for '$uri'"
                    }
                    return route
                }
            }
        }
        throw IllegalArgumentException("DeepLink: no matching pattern for '$uri'")
    }
}
