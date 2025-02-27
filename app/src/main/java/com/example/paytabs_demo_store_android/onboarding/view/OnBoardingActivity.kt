package com.example.paytabs_demo_store_android.onboarding.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.example.paytabs_demo_store_android.MainActivity
import com.example.paytabs_demo_store_android.R
import com.example.paytabs_demo_store_android.onboarding.config.AppPrefs
import kotlinx.android.synthetic.main.activity_on_boarding.*

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var sliderAdapter: SliderAdapter
    private lateinit var layouts: Array<Int>
    private val sliderChangeListener = object : ViewPager.OnPageChangeListener {

        override fun onPageSelected(position: Int) {
            addBottomDots(position)

            if (position == layouts.size.minus(1)) {
                nextBtn.hide()
                skipBtn.hide()
                startBtn.show()
            } else {
                nextBtn.show()
                skipBtn.show()
                startBtn.hide()
            }
        }

        override fun onPageScrollStateChanged(state: Int) {

        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        init()
        dataSet()
        interactions()
    }

    private fun init() {
        /** Layouts of the three onBoarding Screens.
         *  Add more layouts if you wish.
         **/
        layouts = arrayOf(
            R.layout.onboarding_slide1,
            R.layout.onboarding_slide2,
            R.layout.onboarding_slide3
        )

        sliderAdapter = SliderAdapter(this, layouts)
    }

    private fun dataSet() {
        /**
         * Adding bottom dots
         * */
        addBottomDots(0)

        slider.apply {
            adapter = sliderAdapter
            addOnPageChangeListener(sliderChangeListener)
        }
    }

    private fun interactions() {
        skipBtn.setOnClickListener {
            // Launch login screen
            navigateToMain()
        }
        startBtn.setOnClickListener {
            // Launch login screen
            navigateToMain()
        }
        nextBtn.setOnClickListener {
            /**
             *  Checking for last page, if last page
             *  login screen will be launched
             * */
            val nextItemPosition = getNextItemPosition()
            if (nextItemPosition < layouts.size) {
                /**
                 * Move to next screen
                 * */
                slider.currentItem = nextItemPosition
            }
        }
    }

    private fun navigateToMain() {
        AppPrefs(this).setFirstTimeLaunch(false)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun addBottomDots(currentPage: Int) {
        val dots: Array<TextView?> = arrayOfNulls(layouts.size)

        dotsLayout.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]?.text = Html.fromHtml("&#8226;")
            dots[i]?.textSize = 35f
            dots[i]?.setTextColor(resources.getColor(R.color.Grey))
            dotsLayout.addView(dots[i])
        }

        if (dots.isNotEmpty()) {
            dots[currentPage]?.setTextColor(resources.getColor(R.color.black))
        }
    }

    private fun getNextItemPosition(): Int = slider.currentItem.plus(+1)

}