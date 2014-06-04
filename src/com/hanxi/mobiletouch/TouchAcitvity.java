package com.hanxi.mobiletouch;

import android.app.Activity;  
import android.content.Context;  
import android.graphics.Bitmap;  
import android.graphics.BitmapFactory;  
import android.graphics.Canvas;  
import android.graphics.Color;  
import android.graphics.Paint;  
import android.os.Bundle;  
import android.util.Log;  
import android.view.MotionEvent;  
import android.view.SurfaceHolder;  
import android.view.SurfaceView;  
import android.view.Window;  
import android.view.WindowManager;  
import android.view.SurfaceHolder.Callback;  
import android.view.GestureDetector; 
import android.view.GestureDetector.OnGestureListener;  
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse; 
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpStatus;
import java.io.IOException; 

public class TouchAcitvity extends Activity {  
 
    MyView mAnimView = null;  
 
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        // 全屏显示窗口  
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
            WindowManager.LayoutParams.FLAG_FULLSCREEN);  
        // 显示自定义的游戏View  
        mAnimView = new MyView(this);  
        setContentView(mAnimView);  
    }  
 
    public class MyView extends SurfaceView implements Callback {  
 
    final int MOVE_MIN_DISTANCE = 200;
    final int MOVE_LEFT  = 0;
    final int MOVE_RIGHT = 1;
    final int MOVE_UP    = 2;
    final int MOVE_DOWN  = 3;
    final int MAX_POINT_COUNT = 10;
    final String[] MOVE_STRS = {"MOVE_LEFT","MOVE_RIGHT","MOVE_UP","MOVE_DOWN"};

    /** 触摸后绘制的图片 **/  
    Bitmap mBitmap = null;  
    /** 游戏画笔 **/  
    Paint mPaint = null;  
    SurfaceHolder mSurfaceHolder = null;  
    /** 游戏画布 **/  
    Canvas mCanvas = null;  
    int[] mVStartX = new int[MAX_POINT_COUNT];
    int[] mVStartY = new int[MAX_POINT_COUNT];
    int pointCount = 0;
 
    public MyView(Context context) {  
        super(context);  
        /** 设置当前View拥有控制焦点 **/  
        this.setFocusable(true);  
        /** 设置当前View拥有触摸事件 **/  
        this.setFocusableInTouchMode(true);  
        this.setLongClickable(true);
        /** 加载图片 **/  
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);  
        /** 拿到SurfaceHolder对象 **/  
        mSurfaceHolder = this.getHolder();  
        /** 将mSurfaceHolder添加到Callback回调函数中 **/  
        mSurfaceHolder.addCallback(this);  
        /** 创建画布 **/  
        mCanvas = new Canvas();  
        /**创建画笔**/   
        mPaint = new Paint();  
        mPaint.setColor(Color.WHITE);  
    }  
 
    @Override  
    public boolean onTouchEvent(MotionEvent event) {  
        /** 拿到触摸的状态 **/  
        int action = event.getAction();  
        /** 控制当触摸抬起时清屏 **/  
        boolean reset = false;  
        switch (action&MotionEvent.ACTION_MASK) {  
            case MotionEvent.ACTION_DOWN:  
                pointCount = 1;
                mVStartX[0] = (int) event.getX(0);  
                mVStartY[0] = (int) event.getY(0);  
                break;  
            case MotionEvent.ACTION_POINTER_DOWN:
                pointCount += 1;
                if (pointCount>MAX_POINT_COUNT) {
                    pointCount = MAX_POINT_COUNT;
                    break;  
                }
                mVStartX[pointCount-1] = (int) event.getX(pointCount-1);  
                mVStartY[pointCount-1] = (int) event.getY(pointCount-1);  
                break;  
            case MotionEvent.ACTION_UP:  
                pointCount = 0;
                mVStartX[0] = 0;  
                mVStartY[0] = 0;  
                reset = true;
                break;  
            case MotionEvent.ACTION_POINTER_UP:
                pointCount -= 1;
                if (pointCount==0) {
                    pointCount = 1;
                    break;  
                }
                mVStartX[pointCount-1] = 0;  
                mVStartY[pointCount-1] = 0;  
                break;  
        }  
 
        // 在这里加上线程安全锁  
        synchronized (mSurfaceHolder) {  
            /** 拿到当前画布 然后锁定 **/  
            mCanvas = mSurfaceHolder.lockCanvas();  
            /** 清屏 **/  
            mCanvas.drawColor(Color.BLACK);  
     
            if (!reset) {  
                /** 使用循环将每个触摸点图片都绘制出来 **/  
                int moveDis = 0;
                int moveDir = MOVE_LEFT;
                for (int i = 0; i < pointCount; i++) {  
                    /** 根据触摸点的ID 可以讲每个触摸点的X Y坐标拿出来 **/  
                    int x = (int) event.getX(i);  
                    int y = (int) event.getY(i);  
                    if (action==MotionEvent.ACTION_MOVE) {
                        int disX = x-mVStartX[i];
                        int absDisX = java.lang.Math.abs(disX);
                        int disY = y-mVStartY[i];
                        int absDisY = java.lang.Math.abs(disY);
                        int dis = (int)java.lang.Math.pow(absDisX<<2+absDisY<<2,0.5);
                        if (dis>moveDis) {
                            moveDis = dis;
                            if (absDisX>absDisY) {
                                if (disX>0) {
                                    //right;
                                    moveDir = MOVE_RIGHT;
                                }
                                else if (disX<0) {
                                    //left;
                                    moveDir = MOVE_LEFT;
                                }
                            }
                            else {
                                if (disY>0) {
                                    //down;
                                    moveDir = MOVE_DOWN;
                                }
                                else if (disY<0) {
                                    //up;
                                    moveDir = MOVE_UP;
                                }
                            }
                        }
                    }
                    int showX = i * 150;  
                    mCanvas.drawBitmap(mBitmap, x, y, mPaint);  
                    mCanvas.drawText("当前X坐标："+x, showX, 20, mPaint);  
                    mCanvas.drawText("当前Y坐标："+y, showX, 40, mPaint);  
                    mCanvas.drawText("事件触发时间："+event.getEventTime(), showX, 60, mPaint);  
                }
                if (pointCount==2
                    && action==MotionEvent.ACTION_MOVE
                    && moveDis>MOVE_MIN_DISTANCE) {
                    String s = String.format("ACTION_MOVE2: %d,direction:%s",moveDis,MOVE_STRS[moveDir]);
                    Log.v("test", s);
                    // 双指滑动
                    String cmd = String.format("doublemove?direction=%d&distance=%d",moveDir,moveDis);
                    CmdSender.SendCmd(cmd);
                    for (int i = 0; i < pointCount; i++) {  
                        mVStartX[i] = (int) event.getX(i);  
                        mVStartY[i] = (int) event.getY(i);  
                    }
                }
            }else {  
                mCanvas.drawText("请多点触摸当前手机屏幕" ,0, 20, mPaint);  
            }  
            /** 绘制结束后解锁显示在屏幕上 **/  
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);  
        }  
 
        // return super.onTouchEvent(event);  
        return true;  
    }  
      
    @Override  
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}  
 
    @Override  
    public void surfaceCreated(SurfaceHolder holder) {}  
 
    @Override  
    public void surfaceDestroyed(SurfaceHolder holder) {}  
    } 
}

class CmdSender {
    static public String SendCmd(String cmd) {
        String strResult;
        // http地址  
        String httpUrl = "http://192.168.16.14:8080/"+cmd;  
        //HttpGet连接对象
        HttpGet httpRequest = new HttpGet(httpUrl);  
        //取得HttpClient对象  
        HttpClient httpclient = new DefaultHttpClient();  
        //请求HttpClient，取得HttpResponse  
        try{
            HttpResponse httpResponse = httpclient.execute(httpRequest);  
            //请求成功  
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {  
                //取得返回的字符串  
                strResult = EntityUtils.toString(httpResponse.getEntity());  
                Log.v("test",strResult);  
            }  
            else {  
                strResult = "请求错误!";  
                Log.v("test",strResult);  
            }  
        }
        catch (IOException e) {
            strResult = "请求错误!";  
            Log.v("test",strResult);  
        }
        return strResult;
    }
}
