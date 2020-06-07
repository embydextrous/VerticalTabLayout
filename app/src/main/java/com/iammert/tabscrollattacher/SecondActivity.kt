package com.iammert.tabscrollattacher

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TabHost
import androidx.appcompat.app.AppCompatActivity


class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        val tabs = findViewById<View>(R.id.tabhost) as TabHost
        tabs.setup()
        var spec = tabs.newTabSpec("tag1")
        spec.setContent(R.id.tab1)
        spec.setIndicator("First")
        tabs.addTab(spec)
        spec = tabs.newTabSpec("tag2")
        spec.setContent(R.id.tab2)
        spec.setIndicator("second")
        tabs.addTab(spec)
    }

    private fun setNewTab(
        context: Context,
        tabHost: TabHost,
        tag: String,
        title: Int,
        icon: Int,
        contentID: Int
    ) {
        val tabSpec = tabHost.newTabSpec(tag)
        val titleString = "Modi"
        tabSpec.setIndicator(
            titleString,
            context.getResources().getDrawable(android.R.drawable.star_on)
        )
        tabSpec.setContent(contentID)
        tabHost.addTab(tabSpec)
    }
}