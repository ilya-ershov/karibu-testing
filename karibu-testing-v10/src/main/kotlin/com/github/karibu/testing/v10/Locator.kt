package com.github.karibu.testing.v10

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.router.InternalServerError
import java.util.*
import java.util.function.Predicate

/**
 * A criterion for matching components. The component must match all of non-null fields.
 *
 * You can add more properties, simply by creating a write-only property which will register a new [predicate] on write. See
 * [Adding support for custom search criteria](https://github.com/mvysny/karibu-testing/tree/master/karibu-testing-v10#adding-support-for-custom-search-criteria)
 * for more details.
 * @property clazz the class of the component we are searching for.
 * @property id the required [Component.getId]; if null, no particular id is matched.
 * @property caption the required [Component.caption]; if null, no particular caption is matched.
 * @property placeholder the required [Component.placeholder]; if null, no particular placeholder is matched.
 * @property text the [com.vaadin.flow.dom.Element.getText]
 * @property count expected count of matching components, defaults to `0..Int.MAX_VALUE`
 * @property predicates the predicates the component needs to match, not null. May be empty - in such case it is ignored. By default empty.
 */
class SearchSpec<T : Component>(
    val clazz: Class<T>,
    var id: String? = null,
    var caption: String? = null,
    var placeholder: String? = null,
    var text: String? = null,
    var count: IntRange = 0..Int.MAX_VALUE,
    var predicates: MutableList<Predicate<T>> = mutableListOf()
) {

    override fun toString(): String {
        val list = mutableListOf<String>(if (clazz.simpleName.isBlank()) clazz.name else clazz.simpleName)
        if (id != null) list.add("id='$id'")
        if (caption != null) list.add("caption='$caption'")
        if (placeholder != null) list.add("placeholder='$placeholder'")
        if (text != null) list.add("text='$text'")
        if (count != (0..Int.MAX_VALUE) && count != 1..1) list.add("count=$count")
        list.addAll(predicates.map { it.toString() })
        return list.joinToString(" and ")
    }

    @Suppress("UNCHECKED_CAST")
    fun toPredicate(): (Component) -> Boolean {
        val p = mutableListOf<(Component)->Boolean>()
        p.add({ component -> clazz.isInstance(component)} )
        if (id != null) p.add({ component -> component.id_ == id })
        if (caption != null) p.add({ component -> component.caption == caption })
        if (placeholder != null) p.add({ component -> component.placeholder == placeholder })
        if (text != null) p.add { component -> component.element.text == text }
        p.addAll(predicates.map { predicate -> { component: Component -> clazz.isInstance(component) && predicate.test(component as T) } })
        return p.and()
    }
}

/**
 * Finds a VISIBLE component of given type which matches given [block]. This component and all of its descendants are searched.
 * @param block the search specification
 * @return the only matching component, never null.
 * @throws IllegalArgumentException if no component matched, or if more than one component matches.
 */
inline fun <reified T: Component> Component._get(noinline block: SearchSpec<T>.()->Unit = {}): T = this._get(T::class.java, block)

/**
 * Finds a VISIBLE component of given [clazz] which matches given [block]. This component and all of its descendants are searched.
 * @param clazz the component must be of this class.
 * @param block the search specification
 * @return the only matching component, never null.
 * @throws IllegalArgumentException if no component matched, or if more than one component matches.
 */
fun <T: Component> Component._get(clazz: Class<T>, block: SearchSpec<T>.()->Unit = {}): T {
    val result = _find(clazz) {
        count = 1..1
        block()
        check(count == 1..1) { "You're calling _get which is supposed to return exactly 1 component, yet you tried to specify the count of $count" }
    }
    return clazz.cast(result.single())
}

/**
 * Finds a VISIBLE component in the current UI of given type which matches given [block]. The [UI.getCurrent] and all of its descendants are searched.
 * @return the only matching component, never null.
 * @throws IllegalArgumentException if no component matched, or if more than one component matches.
 */
inline fun <reified T: Component> _get(noinline block: SearchSpec<T>.()->Unit = {}): T =
    _get(T::class.java, block)

/**
 * Finds a VISIBLE component in the current UI of given [clazz] which matches given [block]. The [UI.getCurrent] and all of its descendants are searched.
 * @param clazz the component must be of this class.
 * @param block the search specification
 * @return the only matching component, never null.
 * @throws IllegalArgumentException if no component matched, or if more than one component matches.
 */
fun <T: Component> _get(clazz: Class<T>, block: SearchSpec<T>.()->Unit = {}): T = UI.getCurrent()._get(clazz, block)

/**
 * Finds a list of VISIBLE components of given [clazz] which matches [block]. This component and all of its descendants are searched.
 * @return the list of matching components, may be empty.
 */
fun <T: Component> Component._find(clazz: Class<T>, block: SearchSpec<T>.()->Unit = {}): List<T> {
    val spec = SearchSpec(clazz)
    spec.block()
    val result = find(spec.toPredicate())
    if (result.size !in spec.count) {
        val loc: String = UI.getCurrent().internals.activeViewLocation.pathWithQueryParameters
        val message = when {
            result.isEmpty() -> "/$loc: No visible ${clazz.simpleName}"
            result.size < spec.count.first -> "/$loc: Too few (${result.size}) visible ${clazz.simpleName}s"
            else -> "/$loc: Too many visible ${clazz.simpleName}s (${result.size})"
        }
        throw IllegalArgumentException("$message in ${toPrettyString()} matching $spec: [${result.joinToString { it.toPrettyString() }}]. Component tree:\n${toPrettyTree()}")
    }
    return result.filterIsInstance(clazz)
}

/**
 * Finds a list of VISIBLE components of given type which matches [block]. This component and all of its descendants are searched.
 * @return the list of matching components, may be empty.
 */
inline fun <reified T: Component> Component._find(noinline block: SearchSpec<T>.()->Unit = {}): List<T> = this._find(T::class.java, block)

/**
 * Finds a list of VISIBLE components in the current UI of given type which matches given [block]. The [UI.getCurrent] and all of its descendants are searched.
 * @param block the search specification
 * @return the list of matching components, may be empty.
 */
inline fun <reified T: Component> _find(noinline block: SearchSpec<T>.()->Unit = {}): List<T> =
    _find(T::class.java, block)

/**
 * Finds a list of VISIBLE components of given [clazz] which matches [block]. The [UI.getCurrent] and all of its descendants are searched.
 * @return the list of matching components, may be empty.
 */
fun <T: Component> _find(clazz: Class<T>, block: SearchSpec<T>.()->Unit = {}): List<T> =
        UI.getCurrent()._find(clazz, block)

private fun Component.find(predicate: (Component)->Boolean): List<Component> {
    cleanupDialogs()
    val descendants = walk()
    val error: InternalServerError? = descendants.filterIsInstance<InternalServerError>().firstOrNull()
    if (error != null) throw AssertionError("An internal server error occurred; check log for the actual stack-trace. Error text: ${error.element.text}\n${UI.getCurrent().toPrettyTree()}")
    return descendants.filter { predicate(it) }
}

/**
 * Flow Server does not close the dialog when close() is called; instead it tells client-side dialog to close,
 * which then fires event back to the server that the dialog was closed, and removes itself from the DOM.
 * Since there's no browser with browserless testing, we need to cleanup closed dialogs manually, hence this method.
 *
 * Also see [MockedUI] for more details
 */
internal fun cleanupDialogs() {
    UI.getCurrent().children.forEach {
        if (it is Dialog && !it.isOpened) it.element.removeFromParent()
    }
}

private fun <T: Component> Iterable<(T)->Boolean>.and(): (T)->Boolean = { component -> all { it(component) } }

private class TreeIterator<out T>(root: T, private val children: (T) -> Iterator<T>) : Iterator<T> {
    private val queue: Queue<T> = LinkedList<T>(listOf(root))
    override fun hasNext() = !queue.isEmpty()
    override fun next(): T {
        if (!hasNext()) throw NoSuchElementException()
        val result = queue.remove()
        children(result).forEach { queue.add(it) }
        return result
    }
}
private fun Component.walk(): Iterable<Component> = Iterable {
    TreeIterator(this, { component -> component.children.iterator() })
}

/**
 * Expects that there are no VISIBLE components of given type which matches [block]. This component and all of its descendants are searched.
 * @throws IllegalArgumentException if one or more components matched.
 */
inline fun <reified T: Component> Component._expectNone(noinline block: SearchSpec<T>.()->Unit = {}): Unit = this._expectNone(T::class.java, block)

/**
 * Expects that there are no VISIBLE components of given [clazz] which matches [block]. This component and all of its descendants are searched.
 * @throws IllegalArgumentException if one or more components matched.
 */
fun <T: Component> Component._expectNone(clazz: Class<T>, block: SearchSpec<T>.()->Unit = {}): Unit {
    val result: List<T> = _find(clazz) {
        count = 0..0
        block()
        check(count == 0..0) { "You're calling _expectNone which expects 0 component, yet you tried to specify the count of $count" }
    }
    check(result.isEmpty()) // safety check that _find works as expected
}

/**
 * Expects that there are no VISIBLE components in the current UI of given type which matches [block]. The [UI.getCurrent] and all of its descendants are searched.
 * @throws IllegalArgumentException if one or more components matched.
 */
inline fun <reified T: Component> _expectNone(noinline block: SearchSpec<T>.()->Unit = {}): Unit =
    _expectNone(T::class.java, block)

/**
 * Expects that there are no VISIBLE components in the current UI of given [clazz] which matches [block]. The [UI.getCurrent] and all of its descendants are searched.
 * @throws IllegalArgumentException if one or more components matched.
 */
fun <T: Component> _expectNone(clazz: Class<T>, block: SearchSpec<T>.()->Unit = {}): Unit = UI.getCurrent()._expectNone(clazz, block)
