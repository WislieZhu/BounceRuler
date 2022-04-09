package com.wislie.bounceruler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Ruler>(R.id.ruler).setOnRulerScrollListener(object : Ruler.OnRulerScrollListener{
            override fun finishScroll(value: Float) {
                Toast.makeText(this@MainActivity,value.toString(), Toast.LENGTH_SHORT).show()
            }

        })
    }
}