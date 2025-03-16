package xyz.wagyourtail.commonskt.collection.enum

sealed interface EnumSet<T: Enum<T>> : Set<T>

sealed interface MutableEnumSet<T: Enum<T>> : EnumSet<T>, MutableSet<T>

inline fun <reified  T: Enum<T>> enumSetOf(): EnumSet<T> {
    val values = enumValues<T>()
    return if (values.size > 64) {
        JumboEnumSet.noneOf(values)
    } else {
        RegularEnumSet.noneOf(values)
    }
}

inline fun <reified T: Enum<T>> enumSetOf(vararg values: T): EnumSet<T> {
    val universe = enumValues<T>()
    return if (universe.size > 64) {
        JumboEnumSet.of(*values, universe = universe)
    } else {
        RegularEnumSet.of(*values, universe = universe)
    }
}

inline fun <reified T: Enum<T>> enumSetOf(value: T): EnumSet<T> {
    val universe = enumValues<T>()
    return if (universe.size > 64) {
        JumboEnumSet.of(value, universe = universe)
    } else {
        RegularEnumSet.of(value, universe = universe)
    }
}

inline fun <reified T: Enum<T>> enumSetAllOf(): EnumSet<T> {
    val universe = enumValues<T>()
    return if (universe.size > 64) {
        JumboEnumSet.allOf(universe)
    } else {
        RegularEnumSet.allOf(universe)
    }
}

inline fun <reified  T: Enum<T>> mutableEnumSetOf(): MutableEnumSet<T> {
    val values = enumValues<T>()
    return if (values.size > 64) {
        MutableJumboEnumSet.noneOf(values)
    } else {
        MutableRegularEnumSet.noneOf(values)
    }
}

inline fun <reified T: Enum<T>> mutableEnumSetOf(vararg values: T): MutableEnumSet<T> {
    val universe = enumValues<T>()
    return if (universe.size > 64) {
        MutableJumboEnumSet.of(*values, universe = universe)
    } else {
        MutableRegularEnumSet.of(*values, universe = universe)
    }
}

inline fun <reified T: Enum<T>> mutableEnumSetOf(value: T): MutableEnumSet<T> {
    val universe = enumValues<T>()
    return if (universe.size > 64) {
        MutableJumboEnumSet.of(value, universe = universe)
    } else {
        MutableRegularEnumSet.of(value, universe = universe)
    }
}

inline fun <reified T: Enum<T>> mutableEnumSetAllOf(): MutableEnumSet<T> {
    val universe = enumValues<T>()
    return if (universe.size > 64) {
        MutableJumboEnumSet.allOf(universe)
    } else {
        MutableRegularEnumSet.allOf(universe)
    }
}