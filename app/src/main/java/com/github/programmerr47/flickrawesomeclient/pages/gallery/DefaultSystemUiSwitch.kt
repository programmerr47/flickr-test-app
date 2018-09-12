package com.github.programmerr47.flickrawesomeclient.pages.gallery

import kotlin.properties.Delegates

class UiSwitch {
    private var uiStateChangeListener: (Boolean) -> Unit = {}

    var isUiVisible: Boolean by Delegates.observable(true) { _, old, new ->
        if (old != new) {
            uiStateChangeListener(new)
        }
    }

    fun setOnUiStateChangeListener(uiStateChangeListener: (Boolean) -> Unit) {
        this.uiStateChangeListener = uiStateChangeListener
    }
}
