package com.wislie.bounceruler

import android.content.Context

/**
 *    author : Wislie
 *    e-mail : 254457234@qq.comn
 *    date   : 2022/3/24 6:03 下午
 *    desc   :
 *    version: 1.0
 */
fun sp2px(context: Context, spValue: Float): Float {
    val fontScale: Float = context.resources.displayMetrics.scaledDensity
    return spValue * fontScale + 0.5f
}