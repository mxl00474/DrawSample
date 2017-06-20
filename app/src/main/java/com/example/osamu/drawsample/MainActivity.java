package com.example.osamu.drawsample;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MySurfaceView _surfaceView;

    FrameLayout _frameLayout;
    RelativeLayout _myRelativeLayout;
    FrameLayout _imgViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _frameLayout = (FrameLayout)findViewById(R.id.myFrameLayout);
        _myRelativeLayout = (RelativeLayout)findViewById(R.id.myRelativeLayout);

        _surfaceView = new MySurfaceView(this);
        _myRelativeLayout.addView(_surfaceView);

        _imgViews = new FrameLayout(this);
        _frameLayout.addView(_imgViews);

        if(!OpenCVLoader.initDebug()){
            Log.i("OpenCV", "Failed");
        }else{
            Log.i("OpenCV", "successfully built !");
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.i("OpenCV", "Loading OpenCV...");
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch(itemId) {
            case R.id.saveImg:
                saveImage();
                Toast.makeText(this, "Imave Saved", Toast.LENGTH_SHORT).show();
                break;
            case R.id.flipImg:
                flipImage();
                Toast.makeText(this, "Imave Flipped", Toast.LENGTH_SHORT).show();
                break;
            case R.id.findContours:
                findContours();
                Toast.makeText(this, "Display Contours", Toast.LENGTH_SHORT).show();
                break;
            case R.id.clearImg:
                clearImage();
                Toast.makeText(this, "Imave Cleared", Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void saveImage(){

        Bitmap bmp = _surfaceView.getBitmap();

        File file = new File(this.getFilesDir(), "image.jpg");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 95,fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void flipImage(){

        // surfaceViewから手書き画像を取得
        Bitmap bmp = _surfaceView.getBitmap();

        // Matに変換し、openCVで画像処理を実行
        Mat img = new Mat();
        Mat img2 = new Mat();

        Utils.bitmapToMat(bmp, img); // Bitmap -> Mat
        Core.flip(img, img2, 0); // x軸方向に反転
        Utils.matToBitmap(img2, bmp); // Mat -> Bitmap

        // 新しいImageViewを作成し、貼り付け
        ImageView im = new ImageView(this);
        im.setImageBitmap(bmp);
        _imgViews.addView(im);

        img.release();
        img2.release();

        // _surfaceViewをクリア
        _surfaceView.clearScreen();

    }

    private void clearImage() {
        // _surfaceViewをクリア
        _surfaceView.clearScreen();

        // イメージをクリア
        _imgViews.removeAllViews();
    }

    private void findContours() {

        // surfaceViewから手書き画像を取得
        Bitmap bmp = _surfaceView.getBitmap();

        // Bitmap -> Matに変換
        Mat img = new Mat();
        Utils.bitmapToMat(bmp, img);

        // Gray Scale/2値化
        Mat gray = new Mat();
        Mat dst_bin = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(gray, dst_bin, 0, 255, Imgproc.THRESH_OTSU);

        // 輪郭を取得
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        //Imgproc.findContours(dst_bin, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
        Imgproc.findContours(dst_bin, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_KCOS);

        List<MatOfPoint> approxContours = new ArrayList<>();

        // ポリゴンで近似
        for (int i = 0 ; i < contours.size() ; i++) {
            MatOfPoint2f contours2f = new MatOfPoint2f(contours.get(i).toArray());
            MatOfPoint2f approx2f = new MatOfPoint2f();
            Imgproc.approxPolyDP(contours2f, approx2f, 0.03 * Imgproc.arcLength(contours2f, true), true);
            approxContours.add(new MatOfPoint(approx2f.toArray()));
        }

        Mat res = new Mat(img.size(), img.type());
        //Mat res = img.clone();
        //Imgproc.drawContours(res, contours, -1, new Scalar(255, 0, 0, 255), 10);
        Imgproc.drawContours(res, approxContours, -1, new Scalar(0, 255, 0, 255), 10);

        // Mat -> Bitmapに変換
        Utils.matToBitmap(res, bmp);

        // 新しいImageViewを作成し、貼り付け
        ImageView im = new ImageView(this);
        im.setImageBitmap(bmp);
        _imgViews.addView(im);

        img.release();
        gray.release();
        dst_bin.release();

        // _surfaceViewをクリア
        _surfaceView.clearScreen();

    }
}
