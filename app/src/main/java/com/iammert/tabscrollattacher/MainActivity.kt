package com.iammert.tabscrollattacher

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TabHost
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.iammert.tabscrollattacher.data.Category
import com.iammert.tabscrollattacher.data.DataFetcher
import com.iammert.tabscrollattacher.data.Item
import com.iammert.tabscrollattacherlib.TabScrollAttacher
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var  tabHot: TabHost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val categories = DataFetcher.fetchData(applicationContext)

        /**
         * load recyclerview adapter
         */
        val adapter = ItemListAdapter()
        adapter.setItems(getAllItems(categories))
        recyclerView.adapter = adapter

        /**
         * Load tabs
         */
        TabLoader.loadTabs(tabLayout, categories)

        /**
         * SETUP ATTACHER
         */
        val indexOffsets = getCategoryIndexOffsets(categories)
        val attacher = TabScrollAttacher(tabLayout, recyclerView, indexOffsets) {
            scrollDirectly()
        }

        attacher.attach()
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        tabLayout.layoutParams.width = screenHeight
        tabLayout.layoutParams.height = dpToPx(100)
        tabLayout.translationX = (screenWidth - dpToPx(74)).toFloat() * -1
        tabLayout.translationY = (screenHeight / 2 - dpToPx(50)).toFloat()
        recyclerView.layoutParams.width = screenWidth - dpToPx(100)
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            tab?.setCustomView(R.layout.item)
            tab?.customView?.findViewById<TextView>(R.id.tv)?.text = categories[i].categoryName
            tab?.customView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val customView = tab.customView ?: return
                    val selectedLine = customView.findViewById<View>(R.id.selected_line)
                    customView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    customView.layoutParams.height = dpToPx(100)
                    customView.rotation = -90f
                    tab.view.layoutParams.width = dpToPx(100)
                    tab.view.setPadding(0,0,0,0)
                    customView.requestLayout()
                }
            })
        }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<TextView>(R.id.tv)?.setTextColor(Color.WHITE)
                tab?.customView?.findViewById<View>(R.id.selected_line)?.visibility = View.GONE
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<TextView>(R.id.tv)?.setTextColor(Color.RED)
                tab?.customView?.findViewById<View>(R.id.selected_line)?.visibility = View.VISIBLE
            }
        })
        tabLayout.selectTab(tabLayout.getTabAt(1))
        tabLayout.selectTab(tabLayout.getTabAt(0))
    }

    /**
     * Calculate your index offset list.
     * Attacher will talk to recyclerview and tablayout
     * with offsets and indexes.
     */
    private fun getCategoryIndexOffsets(categories: List<Category>): List<Int> {
        val indexOffsetList = arrayListOf<Int>()
        categories.forEach { categoryItem ->
            if (indexOffsetList.isEmpty()) {
                indexOffsetList.add(0)
            } else {
                indexOffsetList.add(indexOffsetList.last() + categoryItem.itemList.size)
            }
        }
        return indexOffsetList
    }

    private fun getAllItems(categories: List<Category>): List<Item> {
        val items = arrayListOf<Item>()
        categories.forEach { items.addAll(it.itemList) }
        return items
    }

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }
}
