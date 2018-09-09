package com.github.programmerr47.flickrawesomeclient

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import com.github.programmerr47.flickrawesomeclient.util.commitTransaction
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.FragmentActivityInjector
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import kotlinx.android.synthetic.main.activity_flow.*

class FlowActivity : AppCompatActivity(), FragmentActivityInjector {
    override val injector: KodeinInjector = KodeinInjector()
    override fun provideOverridingModule() = Kodein.Module {
        bind<AppCompatActivity>("Activity") with instance(this@FlowActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow)
        initializeInjector()

        supportFragmentManager.commitTransaction {
            replace(R.id.fragment_container, SearchPhotoFragment())
        }
    }
}
