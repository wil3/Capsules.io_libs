package io.capsules.lib;

import android.app.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ImageView;

public class ColorEditorActivity extends Activity {


    ImageView imageFrame;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bitmap frame = BitmapFactory.decodeResource(getResources(), R.drawable.ic_cap_display_outerframe);
        imageFrame = (ImageView)findViewById(R.id.image_capsule_frame);



        Bitmap mapFocused = BitmapFactory.decodeResource(getResources(), R.drawable.ic_capsule_focused);

        Bitmap mapIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_capsule_allupgrades_map);


        Bitmap[] bitmaps = {mapFocused,mapIcon};
        imageFrame.setImageBitmap(layerBitmaps(bitmaps));




        ((ImageView)findViewById(R.id.image_capsule_inactive)).setImageBitmap(makeTransparent(mapIcon,60));





    }


    /**
     * The first layer makes the size of the new image
     * @param layers
     * @return
     */
    private static Bitmap layerBitmaps(Bitmap[] layers){

        //Defense
        if (layers == null || layers.length == 0) return null;
        if (layers.length == 1) return layers[0];


        Bitmap baseLayer = layers[0];
        Bitmap bitmap = Bitmap.createBitmap(baseLayer.getWidth(), baseLayer.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        c.drawBitmap(baseLayer, 0,0, null);

        for (int i=0; i<layers.length; i++){

            Bitmap nextLayer = layers[i];

            //Make sure they are all centered
            float left = (baseLayer.getWidth()- nextLayer.getWidth()) / 2f;
            float top = (baseLayer.getHeight()- nextLayer.getHeight()) / 2f;

            c.drawBitmap(nextLayer, left,top,null);

        }

        return bitmap;
    }

    private static Bitmap makeTransparent(Bitmap marker,int alpha ){
        Bitmap newBitmap = Bitmap.createBitmap(marker.getWidth(), marker.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(newBitmap);
        Paint paint = new Paint();
        paint.setAlpha(alpha);
        c.drawBitmap(marker, 0, 0, paint);
        return newBitmap;
    }


}
