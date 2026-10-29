package xyz.wagyourtail.commons.kvision.variables

import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.url.URLSearchParams


inline fun <reified T> readLocalStorageValue(key: String): T? {
    return Json.decodeFromString(window.localStorage.getItem(key) ?: return null)
}

inline fun <reified T> writeLocalStorageValue(key: String, value: T) {
    window.localStorage.setItem(key, Json.encodeToString(value))
}

inline fun <reified T> readParamValue(key: String): T? {
    val params = URLSearchParams(window.location.search)
    return Json.decodeFromString(params.get(key) ?: return null)
}

inline fun <reified T> writeParamValue(key: String, value: T) {
    val params = URLSearchParams(window.location.search)
    if (value == null) {
        params.delete(key)
    } else {
        params.set(key, Json.encodeToString(value))
    }
    val newurl =
        window.location.protocol + "//" + window.location.host + window.location.pathname + "?" + params.toString() + window.location.hash
    window.history.replaceState("update", "", newurl)
}
