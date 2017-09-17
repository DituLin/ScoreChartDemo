package atu.com.scorechartdemo.score;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import atu.com.scorechartdemo.R;


/**
 * Created by  atu on 2017/09/12.
 * 用于得分折线绘制
 */

public class ScoreChartView extends View
{
    /**
     * 重要参数，两点之间分为几段描画，数字愈大分段越多，描画的曲线就越精细.
     */
    private static final int STEPS = 20;
    private float viewWith;
    private float viewHeight;

    private float brokenLineWith = 8f;//折线
    private float dashLineWith = 0.5f;//辅助线

    private int brokenLineColor   = 0xffffffff;//折线颜色
    private int straightLineColor = 0xffe2e2e2;//0xffeaeaea 辅助线
    private int textNormalColor   = 0xffffffff;//文字颜色
    private int circleColor = 0xffff9c00;//圆点颜色
    private int floatTextBackgroundColor = 0xff0096df;//浮窗背景

    private int maxScore = 100;
    private int minScore = 0;

    private int monthCount  = 12;
    private int selectMonth = 0;//选中的月份

    private String[] monthText = new String[]{"1", "2", "3", "4", "5","6", "7", "8", "9", "10", "11", "12"};
    private int[]    score;

    private List<Point> scorePoints;

    private int textSize = dipToPx(15);

    private Paint brokenPaint;
    private Paint straightPaint;
    private Paint dottedPaint;
    private Paint textPaint;

    private Path brokenPath;


    List<Integer> points_x;
    List<Integer> points_y;

    public ScoreChartView(Context context)
    {
        super(context);
        initConfig(context,null);
        init();
    }

    public ScoreChartView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initConfig(context,attrs);
        init();
    }

    public ScoreChartView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initConfig(context,attrs);
        init();

    }

    /**
     * 初始化布局配置
     *
     * @param context
     * @param attrs
     */
    private void initConfig(Context context, AttributeSet attrs)
    {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScoreView);

        maxScore=a.getInt(R.styleable.ScoreView_max_score,100);
        minScore=a.getInt(R.styleable.ScoreView_min_score,0);
        brokenLineColor=a.getColor(R.styleable.ScoreView_broken_line_color,brokenLineColor);

        a.recycle();

    }

    private void init()
    {
        brokenPath = new Path();

        brokenPaint = new Paint();
        brokenPaint.setAntiAlias(true);
        brokenPaint.setStyle(Paint.Style.STROKE);
        brokenPaint.setStrokeWidth(dipToPx(brokenLineWith));
        brokenPaint.setStrokeCap(Paint.Cap.ROUND);

        straightPaint = new Paint();
        straightPaint.setAntiAlias(true);
        straightPaint.setStyle(Paint.Style.STROKE);
        straightPaint.setStrokeWidth(dashLineWith);
        straightPaint.setColor((straightLineColor));
        straightPaint.setStrokeCap(Paint.Cap.ROUND);

        dottedPaint = new Paint();
        dottedPaint.setAntiAlias(true);
        dottedPaint.setStyle(Paint.Style.STROKE);
        dottedPaint.setStrokeWidth(dashLineWith);
        dottedPaint.setColor((straightLineColor));
        dottedPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor((textNormalColor));
        textPaint.setTextSize(dipToPx(15));


    }

    private void initData()
    {
        scorePoints = new ArrayList<>();
        points_x = new LinkedList<Integer>();
        points_y = new LinkedList<Integer>();
        float maxScoreYCoordinate = viewHeight * 0.15f;
        float minScoreYCoordinate = viewHeight * 0.80f;

        Log.d("ScoreChartView", "initData: " + maxScoreYCoordinate);

        float newWith = viewWith - (viewWith * 0.1f) * 2;//分隔线距离最左边和最右边的距离是0.1倍的viewWith
        int   coordinateX;

        if (score == null) {
            return;
        }

        selectMonth = score.length;

        for(int i = 0; i < score.length; i++)
        {
            Log.d("ScoreChartView", "initData: " + score[i]);
            Point point = new Point();
            //计算x轴坐标
            coordinateX = (int) (newWith * ((float) (i) / (monthCount - 1)) + (viewWith * 0.1f));
            point.x = coordinateX;
            if(score[i] > maxScore)
            {
                score[i] = maxScore;
            }
            else if(score[i] < minScore)
            {
                score[i] = minScore;
            }
            //计算y轴坐标
            point.y = (int) (((float) (maxScore - score[i]) / (maxScore - minScore)) * (minScoreYCoordinate - maxScoreYCoordinate) + maxScoreYCoordinate);
            scorePoints.add(point);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWith = w;
        viewHeight = h;
        initData();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
//        drawDottedLine(canvas, viewWith * 0.15f, viewHeight * 0.15f, viewWith, viewHeight * 0.15f);
//        drawDottedLine(canvas, viewWith * 0.15f, viewHeight * 0.4f, viewWith, viewHeight * 0.4f);
//        drawMonthLine(canvas);
        drawText(canvas);
        drawBrokenLine(canvas);
        drawPoint(canvas);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        this.getParent().requestDisallowInterceptTouchEvent(true);//一旦底层View收到touch的action后调用这个方法那么父层View就不会再调用onInterceptTouchEvent了，也无法截获以后的action

        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                onActionUpEvent(event);
                this.getParent().requestDisallowInterceptTouchEvent(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                this.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return true;
    }

    private void onActionUpEvent(MotionEvent event)
    {
        boolean isValidTouch = validateTouch(event.getX(), event.getY());

        if(isValidTouch)
        {
            invalidate();
        }
    }

    //是否是有效的触摸范围
    private boolean validateTouch(float x, float y)
    {

        //曲线触摸区域
        for(int i = 0; i < scorePoints.size(); i++)
        {
            // dipToPx(8)乘以2为了适当增大触摸面积
            if(x > (scorePoints.get(i).x - dipToPx(8) * 2) && x < (scorePoints.get(i).x + dipToPx(8) * 2))
            {
                if(y > (scorePoints.get(i).y - dipToPx(8) * 2) && y < (scorePoints.get(i).y + dipToPx(8) * 2))
                {
                    selectMonth = i + 1;
                    return true;
                }
            }
        }

        //月份触摸区域
        //计算每个月份X坐标的中心点
        float monthTouchY = viewHeight * 0.85f - dipToPx(3);//减去dipToPx(3)增大触摸面积

        float newWith       = viewWith - (viewWith * 0.1f) * 2;//分隔线距离最左边和最右边的距离是0.15倍的viewWith
        float validTouchX[] = new float[monthText.length];
        for(int i = 0; i < scorePoints.size(); i++)//超出不可点击
        {
            validTouchX[i] = newWith * ((float) (i) / (monthCount - 1)) + (viewWith * 0.1f);
        }

        if(y > monthTouchY)
        {
            for(int i = 0; i < validTouchX.length; i++)
            {
                Log.d("ScoreChartView", "validateTouch: validTouchX:" + validTouchX[i]);
                if(x < validTouchX[i] + dipToPx(8) && x > validTouchX[i] - dipToPx(8))
                {
                    Log.d("ScoreChartView", "validateTouch: " + (i + 1));
                    selectMonth = i + 1;
                    return true;
                }
            }
        }

        return false;
    }


    //绘制折线穿过的点
    protected void drawPoint(Canvas canvas)
    {
        if(scorePoints == null)
        {
            return;
        }
        brokenPaint.setStrokeWidth(dipToPx(1));
        for(int i = 0; i < scorePoints.size(); i++)
        {

            //选中月份绘制圆点
            if(i == selectMonth - 1)
            {
                //中层
//                brokenPaint.setColor(0xffd0f3f2);
//                canvas.drawCircle(scorePoints.get(i).x, scorePoints.get(i).y, dipToPx(8f), brokenPaint);
                brokenPaint.setStyle(Paint.Style.FILL);
                brokenPaint.setColor(0xffffffff);
                canvas.drawCircle(scorePoints.get(i).x, scorePoints.get(i).y, dipToPx(8f), brokenPaint);

                //绘制浮动文本背景框
                drawFloatTextBackground(canvas, scorePoints.get(i).x, scorePoints.get(i).y - dipToPx(8.5f));

                textPaint.setColor(textNormalColor);
                textPaint.setTextSize(dipToPx(24f));
                //绘制浮动文字
                canvas.drawText(String.valueOf(score[i]), scorePoints.get(i).x, scorePoints.get(i).y - dipToPx(8f) - textSize, textPaint);

                //内层
                brokenPaint.setColor(circleColor);
                canvas.drawCircle(scorePoints.get(i).x, scorePoints.get(i).y, dipToPx(2.5f), brokenPaint);
                brokenPaint.setStyle(Paint.Style.FILL);
                brokenPaint.setColor(circleColor);
                canvas.drawCircle(scorePoints.get(i).x, scorePoints.get(i).y, dipToPx(4f), brokenPaint);
            }

        }
    }

    //绘制月份的直线(包括刻度)
    private void drawMonthLine(Canvas canvas)
    {
        straightPaint.setStrokeWidth(dipToPx(1));
        canvas.drawLine(0, viewHeight * 0.85f, viewWith, viewHeight * 0.85f, straightPaint);

        float newWith = viewWith - (viewWith * 0.1f) * 2;//分隔线距离最左边和最右边的距离是0.15倍的viewWith
        float coordinateX;//分隔线X坐标
        for(int i = 0; i < monthCount; i++)
        {
            coordinateX = newWith * ((float) (i) / (monthCount - 1)) + (viewWith * 0.15f);
            canvas.drawLine(coordinateX, viewHeight * 0.85f, coordinateX, viewHeight * 0.85f + dipToPx(4), straightPaint);
        }
    }

    //绘制折线
    private void drawBrokenLine(Canvas canvas)
    {
        brokenPath.reset();
        brokenPaint.setColor(brokenLineColor);
        brokenPaint.setStrokeWidth(brokenLineWith);
        brokenPaint.setStyle(Paint.Style.STROKE);
        if(score == null || score.length == 0 || score.length == 1)
        {
            return;
        }

        points_x.clear();
        points_y.clear();
        for (int i = 0; i < scorePoints.size(); i++) {
            points_x.add(scorePoints.get(i).x);//存入所有 X 坐标
            points_y.add(scorePoints.get(i).y);//存入所有 Y 坐标
        }
        List<Cubic> calculate_x = calculate(points_x);//计算
        List<Cubic> calculate_y = calculate(points_y);

        brokenPath
                .moveTo(calculate_x.get(0).eval(0), calculate_y.get(0).eval(0));

        for (int i = 0; i < calculate_x.size(); i++) {
            for (int j = 1; j <= STEPS; j++) {//两点之间指定的数量
                float u = j / (float) STEPS;
                brokenPath.lineTo(calculate_x.get(i).eval(u), calculate_y.get(i)
                        .eval(u));
            }
        }
        canvas.drawPath(brokenPath, brokenPaint);

    }

    //绘制文本
    private void drawText(Canvas canvas)
    {
        textPaint.setTextSize(dipToPx(12));
        textPaint.setColor(textNormalColor);

//        canvas.drawText(String.valueOf(maxScore), viewWith * 0.1f - dipToPx(10), viewHeight * 0.15f + textSize * 0.25f, textPaint);
//        canvas.drawText(String.valueOf(minScore), viewWith * 0.1f - dipToPx(10), viewHeight * 0.4f + textSize * 0.25f, textPaint);

        textPaint.setColor(0xff7c7c7c);

        float newWith = viewWith - (viewWith * 0.1f) * 2;//分隔线距离最左边和最右边的距离是0.15倍的viewWith
        float coordinateX;//分隔线X坐标
        textPaint.setTextSize(dipToPx(12));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(textNormalColor);
        textSize = (int) textPaint.getTextSize();
        for(int i = 0; i < monthText.length; i++)
        {
            coordinateX = newWith * ((float) (i) / (monthCount - 1)) + (viewWith * 0.1f);

            if(i == selectMonth - 1)
            {

                textPaint.setStyle(Paint.Style.STROKE);
                textPaint.setColor(circleColor);
                //绘制月份外边框
//                RectF r2 = new RectF();
//                r2.left = coordinateX - textSize - dipToPx(4);
//                r2.top = viewHeight * 0.7f + dipToPx(4) + textSize / 2;
//                r2.right = coordinateX + textSize + dipToPx(4);
//                r2.bottom = viewHeight * 0.7f + dipToPx(4) + textSize + dipToPx(8);
//                canvas.drawRoundRect(r2, 10, 10, textPaint);

            }
            //绘制月份
            canvas.drawText(monthText[i], coordinateX, viewHeight * 0.85f + dipToPx(4) + textSize + dipToPx(5), textPaint);

            textPaint.setColor(textNormalColor);

        }

    }

    //绘制显示浮动文字的背景
    private void drawFloatTextBackground(Canvas canvas, int x, int y)
    {
        brokenPath.reset();
        brokenPaint.setColor(floatTextBackgroundColor);
        brokenPaint.setStyle(Paint.Style.FILL);

        //P1
        Point point = new Point(x, y);
        brokenPath.moveTo(point.x, point.y);

        //P2
        point.x = point.x + dipToPx(8);
        point.y = point.y - dipToPx(8);
        brokenPath.lineTo(point.x, point.y);

        //P3
        point.x = point.x + dipToPx(17);//右下角
        brokenPath.lineTo(point.x, point.y);

        //P4
        point.y = point.y - dipToPx(25);//右上角
        brokenPath.lineTo(point.x, point.y);

        //P5
        point.x = point.x - dipToPx(50);
        brokenPath.lineTo(point.x, point.y);

        //P6
        point.y = point.y + dipToPx(25);//左下角
        brokenPath.lineTo(point.x, point.y);

        //P7
        point.x = point.x + dipToPx(17);
        brokenPath.lineTo(point.x, point.y);

        //最后一个点连接到第一个点
        brokenPath.lineTo(x, y);

        canvas.drawPath(brokenPath, brokenPaint);
    }

    /**
     * 画虚线
     *
     * @param canvas 画布
     * @param startX 起始点X坐标
     * @param startY 起始点Y坐标
     * @param stopX  终点X坐标
     * @param stopY  终点Y坐标
     */
    private void drawDottedLine(Canvas canvas, float startX, float startY, float stopX, float stopY)
    {
        dottedPaint.setPathEffect(new DashPathEffect(new float[]{20, 10}, 4));
        dottedPaint.setStrokeWidth(1);
        // 实例化路径
        Path mPath = new Path();
        mPath.reset();
        // 定义路径的起点
        mPath.moveTo(startX, startY);
        mPath.lineTo(stopX, stopY);
        canvas.drawPath(mPath, dottedPaint);

    }


    public int[] getScore()
    {
        return score;
    }

    public void setScore(List<Integer> list)
    {
        this.score = convertIntegers(list);
        initData();
    }

    public void setMaxScore(int maxScore)
    {
        this.maxScore = maxScore;
    }

    public void setMinScore(int minScore)
    {
        this.minScore = minScore;
    }

    /**
     * dip 转换成px
     *
     * @param dip
     * @return
     */
    private int dipToPx(float dip)
    {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }


    /**
     * list 转换为 int[]
     * @param list
     * @return
     */
    private int[] convertIntegers(List<Integer> list) {
        if (list == null || list.size() <= 0) {
            return null;
        }
        int[] ret = new int[list.size()];
        for (int i=0;i < list.size();i++) {
            ret[i] = list.get(i);
        }
        return ret;
    }


    /**
     * 计算曲线.
     *
     * @param x
     * @return
     */
    private List<Cubic> calculate(List<Integer> x) {
        int n = x.size() - 1;
        float[] gamma = new float[n + 1];
        float[] delta = new float[n + 1];
        float[] D = new float[n + 1];
        int i;
		/*
		 * We solve the equation [2 1 ] [D[0]] [3(x[1] - x[0]) ] |1 4 1 | |D[1]|
		 * |3(x[2] - x[0]) | | 1 4 1 | | . | = | . | | ..... | | . | | . | | 1 4
		 * 1| | . | |3(x[n] - x[n-2])| [ 1 2] [D[n]] [3(x[n] - x[n-1])]
		 *
		 * by using row operations to convert the matrix to upper triangular and
		 * then back sustitution. The D[i] are the derivatives at the knots.
		 */

        gamma[0] = 1.0f / 2.0f;
        for (i = 1; i < n; i++) {
            gamma[i] = 1 / (4 - gamma[i - 1]);
        }
        gamma[n] = 1 / (2 - gamma[n - 1]);

        delta[0] = 3 * (x.get(1) - x.get(0)) * gamma[0];
        for (i = 1; i < n; i++) {
            delta[i] = (3 * (x.get(i + 1) - x.get(i - 1)) - delta[i - 1])
                    * gamma[i];
        }
        delta[n] = (3 * (x.get(n) - x.get(n - 1)) - delta[n - 1]) * gamma[n];

        D[n] = delta[n];
        for (i = n - 1; i >= 0; i--) {
            D[i] = delta[i] - gamma[i] * D[i + 1];
        }

		/* now compute the coefficients of the cubics */
        List<Cubic> cubics = new LinkedList<Cubic>();
        for (i = 0; i < n; i++) {
            Cubic c = new Cubic(x.get(i), D[i], 3 * (x.get(i + 1) - x.get(i))
                    - 2 * D[i] - D[i + 1], 2 * (x.get(i) - x.get(i + 1)) + D[i]
                    + D[i + 1]);
            cubics.add(c);
        }
        return cubics;
    }

}
