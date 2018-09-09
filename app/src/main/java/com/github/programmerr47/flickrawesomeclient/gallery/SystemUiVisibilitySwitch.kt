package com.github.programmerr47.flickrawesomeclient.gallery

import android.view.View
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.lang.ref.WeakReference

interface SystemUiVisibilitySwitch {

    fun isSystemUiVisible(): Boolean

    fun showSystemUi()

    fun hideSystemUi()

    fun observe(): Observable<Boolean>
}

class DefaultSystemUiSwitch(
        decorView: View
) : SystemUiVisibilitySwitch {

    private var isUiVisible = true
    private val visibilitySubject = PublishSubject.create<Boolean>()
    private val viewReference: WeakReference<View> = WeakReference(decorView)

    override fun isSystemUiVisible() = isUiVisible

    override fun showSystemUi() {
        viewReference.get()?.run {
            systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

            isUiVisible = true
            visibilitySubject.onNext(isUiVisible)
        }
    }

    override fun hideSystemUi() {
        viewReference.get()?.run {
            systemUiVisibility =
                    View.SYSTEM_UI_FLAG_IMMERSIVE or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            isUiVisible = false
            visibilitySubject.onNext(isUiVisible)
        }
    }

    override fun observe() = visibilitySubject
}
