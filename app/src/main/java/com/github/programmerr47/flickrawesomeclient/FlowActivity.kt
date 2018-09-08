package com.github.programmerr47.flickrawesomeclient

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import com.github.programmerr47.flickrawesomeclient.util.commitTransaction
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import kotlinx.android.synthetic.main.activity_flow.*

class FlowActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow)

        supportFragmentManager.commitTransaction {
            replace(R.id.fragment_container, SearchPhotoFragment())
        }
    }
}
