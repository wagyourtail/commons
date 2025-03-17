package xyz.wagyourtail.commons.kvision.components

import io.kvision.core.*
import io.kvision.html.*
import io.kvision.panel.FlexPanel
import io.kvision.panel.gridPanel
import io.kvision.state.ObservableValue
import io.kvision.utils.auto
import io.kvision.utils.perc
import io.kvision.utils.px
import io.kvision.utils.rem

open class FooterComponent(
    poweredBy: Boolean,
    copyright: String
) : FlexPanel(justify = JustifyContent.SPACEBETWEEN) {
    init {
        flexGrow = 0
        flexShrink = 1
        flexBasis = auto

        borderTop = Border(2.px, BorderStyle.SOLID, Color.name(Col.LIGHTGRAY))
    }

    val poweredBy = ObservableValue(poweredBy)

    val copyright = ObservableValue(copyright)

    val leftContent = gridPanel(alignItems = AlignItems.CENTER) {
        height = 100.perc
        gridTemplateColumns = "auto auto auto"

        div {
            height = 90.perc
            width = 1.px
            background = Background(Color.name(Col.GRAY))
            marginLeft = 5.px
            marginRight = 5.px
        }

        div {
            fontSize = 1.rem
            marginTop = 5.px
            marginBottom = 5.px
            marginLeft = 10.px

            this@FooterComponent.poweredBy.subscribe { value ->
                if (value) {
                    removeAll()
                    +"Powered by "
                    link("KVision", "https://kvision.io/") {
                        target = "_blank"
                    }
                } else {
                    removeAll()
                }
            }
        }
    }

    val icons = Div {
        marginLeft = 10.px
        paddingLeft = 5.px
    }.also {
        leftContent.add(0, it)
    }

    fun addIcon(name: String, url: String, faIcon: String, init: Link.() -> Unit = {}) {
        Link("", url) {
            setAttribute("aria-label", name)
            target = "_blank"

            icon(faIcon)
        }.also {
            icons.add(it)
        }
    }

    fun addSeparator() {
        Div {
            width = 1.px
            height = 100.perc
            background = Background(Color.name(Col.GRAY))
            marginLeft = 5.px
            marginRight = 5.px
        }.also {
            icons.add(it)
        }
    }

    fun addKoFiIcon(url: String, init: Link.() -> Unit = {}) {
        Link("", url) {
            setAttribute("aria-label", "Ko-fi")
            target = "_blank"

            customTag("svg") {
                setAttribute("height", "20")
                setAttribute("width", "20")
                setAttribute("viewBox", "0 0 24 24")
                setAttribute("role", "img")
                setAttribute("xmlns", "http://www.w3.org/2000/svg")
                setStyle("fill", "rgba(var(--bs-link-color-rgb)")
                marginBottom = 4.px
                style(pClass = PClass.HOVER) {
                    setStyle("fill", "rgba(var(--bs-link-hover-color-rgb)")
                }
                customTag("path") {
                    setAttribute(
                        "d",
                        "M23.881 8.948c-.773-4.085-4.859-4.593-4.859-4.593H.723c-.604 0-.679.798-.679.798s-.082 7.324-.022 11.822c.164 2.424 2.586 2.672 2.586 2.672s8.267-.023 11.966-.049c2.438-.426 2.683-2.566 2.658-3.734 4.352.24 7.422-2.831 6.649-6.916zm-11.062 3.511c-1.246 1.453-4.011 3.976-4.011 3.976s-.121.119-.31.023c-.076-.057-.108-.09-.108-.09-.443-.441-3.368-3.049-4.034-3.954-.709-.965-1.041-2.7-.091-3.71.951-1.01 3.005-1.086 4.363.407 0 0 1.565-1.782 3.468-.963 1.904.82 1.832 3.011.723 4.311zm6.173.478c-.928.116-1.682.028-1.682.028V7.284h1.77s1.971.551 1.971 2.638c0 1.913-.985 2.667-2.059 3.015z"
                    )
                }
            }
        }.also {
            icons.add(it)
        }
    }

    val rightContent = div {
        fontSize = 1.rem
        marginTop = 5.px
        marginBottom = 5.px
        marginRight = 10.px

        this@FooterComponent.copyright.subscribe { value ->
            removeAll()
            +value
        }
    }

}

fun Container.footerComponent(
    poweredBy: Boolean = true,
    author: String = "",
    date: String = "",
    init: FooterComponent.() -> Unit = {}
) {
    FooterComponent(poweredBy, "$author $date").also {
        it.init()
        this.add(it)
    }
}