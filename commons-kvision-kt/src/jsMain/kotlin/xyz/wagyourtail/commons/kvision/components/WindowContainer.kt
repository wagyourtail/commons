package xyz.wagyourtail.commons.kvision.components

import io.kvision.core.Overflow
import io.kvision.html.div
import io.kvision.panel.StackPanel
import io.kvision.panel.VPanel
import io.kvision.utils.auto
import io.kvision.utils.perc
import io.kvision.utils.vh

open class WindowContainer(init: WindowContainer.() -> Unit) : VPanel() {
    init {
        height = 100.vh
    }

    val titlebar = div {}

    val main = StackPanel {

    }.also {
        div {
            flexGrow = 1
            flexShrink = 1
            flexBasis = auto
            height = 100.perc
            overflow = Overflow.HIDDEN

            this@div.add(it)
        }
    }

    val footer = footerComponent {

    }

}