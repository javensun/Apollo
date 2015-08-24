package com.dreamone.dev.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class Util {
    protected static void showToast(final Activity activity,final String content) {
        activity.runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                Toast toast = Toast.makeText(activity, content, Toast.LENGTH_SHORT);
                toast.show();
                
            }
        });
       
    }
    protected static void setTextViewContent(final Activity activity,final TextView textView,final String content) {
        activity.runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
               if(textView!=null){
                   textView.setText(content);
               }
                
            }
        });
       
    }
   

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        return bmpToByteArray(bitmap, true);
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
