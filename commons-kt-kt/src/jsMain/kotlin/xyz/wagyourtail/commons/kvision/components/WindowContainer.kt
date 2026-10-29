package xyz.wagyourtail.commons.kvision.components

import io.kvision.core.*
import io.kvision.html.div
import io.kvision.panel.FlexPanel
import io.kvision.panel.StackPanel
import io.kvision.panel.VPanel
import io.kvision.panel.flexPanel
import io.kvision.utils.auto
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.vh

open class WindowContainer() : VPanel() {
    init {
        height = 100.vh
    }

    val titlebar = flexPanel(justify = JustifyContent.SPACEBETWEEN) {
        width = 100.perc
        borderBottom = Border(2.px, BorderStyle.SOLID, Color.name(Col.LIGHTGRAY))
    }

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

    inline fun titlebar(function: FlexPanel.() -> Unit) {
        titlebar.function()
    }

    inline fun main(function: StackPanel.() -> Unit) {
        main.function()
    }

    inline fun footer(function: FooterComponent.() -> Unit) {
        footer.function()
    }

}

inline fun Container.windowContainer(init: WindowContainer.() -> Unit): WindowContainer {
    return WindowContainer().also {
        it.init()
        this.add(it)
    }
}