package com.github.programmerr47.flickrawesomeclient

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import kotlinx.android.synthetic.main.activity_flow.*

class FlowActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow)

        progress.visibility = VISIBLE
        FlickrApplication.api.searchPhotos("bird")
                .observeOn(mainThread())
                .subscribe(
                        {
                            Log.v("FUCK", "response: ${it::class.java.name} $it")
                        },
                        {
                            progress.visibility = GONE
                            Log.v("FUCK", "error: $it")
                        },
                        {
                            progress.visibility = GONE
                            Log.v("FUCK", "finish")
                        }
                )
    }
}
