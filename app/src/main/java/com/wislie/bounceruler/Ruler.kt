package com.wislie.bounceruler

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.*
import android.widget.OverScroller
import kotlin.math.abs

/**
 *    author : Wislie
 *    e-mail : 254457234@qq.comn
 *    date   : 2022/4/7 3:00 下午
 *    desc   : 卷尺(加油吧，没什么好感叹的)
 *    version: 1.0
 */
class Ruler : View {

    //////////可以修改的属性-begin//////////
    private var standardDpi = 0
    private var lineDensity = 0
    private var potsNum = 0
    private var rulerSize = 0
    private var longLineStrokeWidth = 0f
    private var lineStrokeWidth = 0f
    private var staticsTextSize = 0f
    private var rulerTextSize = 0f
    //////////可以修改的属性-end//////////

    private var lineWidth = 0f //每条线宽度
    private var lineHeight = 0f //每条线长度
    private var longLineHeight = 0f //每条长线长度

    private var potWidth = 0f //尺之间每条线的间隔
    private var rulerWidth = 0f //尺的宽度
    private var offsetX = 0f //偏移量
    private var isScroll = false
    private var topY = 0f //绘制区域的最高点
    private var bottomY = 0f //绘制区域的最低点
    private var centerX = 0f   //中心点的x值
    private var mMinimumVelocity = 0f
    private var mMaximumVelocity = 0f
    private var mVelocityTracker: VelocityTracker? = null
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mOnRulerScrollListener: OnRulerScrollListener? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        val arr = context.obtainStyledAttributes(attrs, R.styleable.Ruler)
        standardDpi = arr.getInteger(R.styleable.Ruler_standard_dpi, STANDARD_DPI)
        lineDensity = arr.getInteger(R.styleable.Ruler_line_density, LINE_DENSITY)
        potsNum = arr.getInteger(R.styleable.Ruler_pots_num, POTS_NUM)
        rulerSize = arr.getInteger(R.styleable.Ruler_ruler_size, RULER_SIZE)
        longLineStrokeWidth = arr.getFloat(R.styleable.Ruler_long_line_stroke_width, LONG_LINE_STROKE_WIDTH)
        lineStrokeWidth = arr.getFloat(R.styleable.Ruler_line_stroke_width, LINE_STROKE_WIDTH)
        staticsTextSize = arr.getFloat(R.styleable.Ruler_statics_text_size, STATICS_TEXT_SIZE)
        rulerTextSize = arr.getFloat(R.styleable.Ruler_ruler_text_size, RULER_TEXT_SIZE)
        arr.recycle()

        initAttrs()
    }

    private val mDetector by lazy {
        GestureDetector(context, OnRulerGestureListener())
    }

    private val mScroller by lazy {
        OverScroller(context)
    }

    private val mRunnable by lazy {
        RulerRunnable()
    }

    private fun initAttrs(){
        //设置paint的颜色，粗细，是否抗锯齿
        mPaint.color = Color.GREEN
        mPaint.textSize = sp2px(context, staticsTextSize)
        mPaint.style = Paint.Style.FILL
        mPaint.isDither = true
        mPaint.strokeWidth = lineStrokeWidth

        //总长度设置为100cm, 为了目标，一定要努力
        val widthPixels = resources.displayMetrics.widthPixels
        //修改后的屏幕像素密度
        val density = widthPixels * 1.0f / standardDpi
        //设置每条线的宽度
        lineWidth = density * lineDensity
        //每条线的长度
        lineHeight = density * potsNum
        longLineHeight = lineHeight * 2
        //设置30个点可以占据一个宽度的屏幕,每个点的宽度
        potWidth = widthPixels * 1.0f / potsNum
        rulerWidth = potWidth * 10 * rulerSize
        centerX = (potsNum * 1.0f / 2) / 10
        topY = -txtHeight() * 3 / 2
        mPaint.textSize = sp2px(context, rulerTextSize)
        bottomY = longLineHeight + txtHeight() * 5 / 4

        mVelocityTracker = VelocityTracker.obtain()
        mMaximumVelocity = ViewConfiguration.get(context)
            .scaledMaximumFlingVelocity.toFloat()
        mMinimumVelocity = ViewConfiguration.get(context)
            .scaledMinimumFlingVelocity.toFloat()
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //开始速度检测
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker?.addMovement(event)
        when (event?.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isScroll) { //需要判断是否结束滑动
                    mVelocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity)
                    val velocityX = mVelocityTracker?.xVelocity ?: 0f
                    if (abs(velocityX) < mMinimumVelocity) { //小于最小速度的时候，调整并停下来
                        adjustOffsetXToInt()
                    }
                    invalidate()
                    isScroll = false
                }
                mVelocityTracker?.recycle()
                mVelocityTracker = null
            }
        }
        var result = mDetector.onTouchEvent(event)
        if (!result) {
            result = super.onTouchEvent(event)
        }
        return result
    }


    inner class OnRulerGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            isScroll = true
            offsetX -= distanceX
            fixOffsets()
            invalidate()
            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            mScroller.fling(
                offsetX.toInt(),
                0,
                velocityX.toInt(),
                velocityY.toInt(),
                -(rulerWidth - width * 1.0f / 2).toInt(),
                width / 2,
                0,
                0,
                30, 0
            )
            postOnAnimation(mRunnable)
            return super.onFling(e1, e2, velocityX, velocityY)
        }
    }

    //矫正偏移量
    private fun fixOffsets() {
        if (offsetX <= -(rulerWidth - width * 1.0f / 2)) {
            offsetX = -(rulerWidth - width * 1.0f / 2)
        }
        if (offsetX >= width * 1.0f / 2) {
            offsetX = width * 1.0f / 2
        }
    }

    //fling的情况, scroll的情况
    inner class RulerRunnable : Runnable {
        override fun run() {
            if (mScroller.computeScrollOffset()) {
                offsetX = mScroller.currX.toFloat()
                invalidate()
                postOnAnimation(this)
                return
            }
            adjustOffsetXToInt()
            invalidate()
        }
    }

    //偏移量调整
    private fun adjustOffsetXToInt() {
        if (mScroller.computeScrollOffset()) {
            return
        }
        //前一个数大，还是后一个数大  -14.8/5 = -3 ; -15.1/5 = -4;要调整为-3
        val num = (offsetX / potWidth).toInt()
        val width = num * potWidth
        val width2 = (num - 1) * potWidth

        val deltaX = width - offsetX
        val deltaX2 = width2 - offsetX
        val minDeltaX = if (abs(deltaX) > abs(deltaX2)) deltaX2 else deltaX
        offsetX += minDeltaX
        centerX = (-offsetX / potWidth + potsNum / 2) / 10
        mOnRulerScrollListener?.finishScroll(centerX)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //平移height/2
        canvas?.translate(0f, height * 1.0f / 2)
        //设置绘制区域,避免过度绘制
        canvas?.clipRect(0f, topY, width * 1.0f, bottomY)
        mPaint.strokeWidth = lineStrokeWidth
        drawStaticsText(canvas)
        drawScrollPart(canvas)
        drawCenterLine(canvas)
    }

    private fun drawStaticsText(canvas: Canvas?) {
        mPaint.textSize = sp2px(context, staticsTextSize)
        mPaint.color = Color.GREEN
        //中心坐标字符串
        val centerVal = centerX.toString()
        //文字宽高
        val txtWidth = txtWidth(centerVal)
        val txtHeight = txtHeight()
        canvas?.drawText(
            centerX.toString(),
            width * 1.0f / 2 - txtWidth / 2,
            -txtHeight / 2,
            mPaint
        )
    }

    private fun drawScrollPart(canvas: Canvas?) {
        //寻找在 0 到 half*potWidth之间的线
        val startIndex: Int = (-offsetX / potWidth).toInt() //初始startIndex=-15
        val endIndex = startIndex + potsNum
        (startIndex..endIndex).forEach { index ->
            mPaint.color = Color.GRAY
            if (index % 10 == 0) {
                //绘制长线
                canvas?.drawLine(
                    potWidth * index + offsetX, 0f,
                    potWidth * index + offsetX, longLineHeight, mPaint
                )
                mPaint.textSize = sp2px(context, rulerTextSize)
                mPaint.color = Color.BLACK

                //坐标字符串
                val coordinateVal = (index / 10).toString()
                //文字宽高
                val txtWidth = txtWidth(coordinateVal)
                val txtHeight = txtHeight()
                canvas?.drawText(
                    coordinateVal,
                    potWidth * index - txtWidth / 2 + offsetX,
                    longLineHeight + txtHeight,
                    mPaint
                )
            } else {
                //绘制短线
                canvas?.drawLine(
                    potWidth * index + offsetX, 0f,
                    potWidth * index + offsetX, lineHeight, mPaint
                )
            }
        }
    }

    private fun drawCenterLine(canvas: Canvas?) {
        mPaint.strokeWidth = longLineStrokeWidth
        mPaint.color = Color.GREEN
        canvas?.drawLine(
            width * 1.0f / 2, 0f,
            width * 1.0f / 2, longLineHeight, mPaint
        )
    }

    //文字宽
    private fun txtWidth(value: String): Float {
        return mPaint.measureText(value)
    }

    //文字高
    private fun txtHeight(): Float {
        val fontMetrics: Paint.FontMetrics = mPaint.fontMetrics
        return fontMetrics.descent - fontMetrics.ascent
    }


    companion object {
        const val STANDARD_DPI = 440 //标准的屏幕像素密度

        const val LINE_DENSITY = 3 //每条线3个density

        const val POTS_NUM = 30 //30个点

        const val RULER_SIZE = 5 //设置尺为100cm

        const val LONG_LINE_STROKE_WIDTH = 5f //长的线宽

        const val LINE_STROKE_WIDTH = 2f //短的线宽

        const val STATICS_TEXT_SIZE = 20f //不变的触笔大小

        const val RULER_TEXT_SIZE = 15f //尺的触笔大小
    }

    fun setOnRulerScrollListener(onRulerScrollListener: OnRulerScrollListener) {
        mOnRulerScrollListener = onRulerScrollListener
    }

    interface OnRulerScrollListener {
        fun finishScroll(value: Float)
    }

}