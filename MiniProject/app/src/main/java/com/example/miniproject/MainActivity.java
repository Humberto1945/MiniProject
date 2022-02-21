package com.example.miniproject;

import static java.lang.System.*;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.MemoryFormat;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    //private Module module = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         Bitmap bitmap = null;
         Module module = null;
         //try method to run through and get the image and pretrained model
         try{
             bitmap = BitmapFactory.decodeStream(getAssets().open("puppyImage.jpg"));
             module = LiteModuleLoader.load(readingAsset(this, "model.pt"));
         } catch (IOException e) {
             e.printStackTrace();
         }
         ImageView imageView = findViewById(R.id.imageView);
         imageView.setImageBitmap(bitmap);
         // initalize button to load and transfer the image to the view

        Button button = findViewById(R.id.loadImage);
        //Creating a second set of bitmap and module to allow for usage in onClick
        Bitmap finalBitmap = bitmap;
        Module finalModule = module;
        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //Input output tensor transformation
               final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(finalBitmap, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB, MemoryFormat.CHANNELS_LAST);
               final Tensor outputTensor = finalModule.forward(IValue.from(inputTensor)).toTensor();
                /*
                TODO
                Make it read from imageClassesFile and find the mean and std that way
                Instead of using the other test class set
                 */
                final float[] tally = outputTensor.getDataAsFloatArray();
                // searching for the index with maximum score
                float maxScore = -Float.MAX_VALUE;
                int maxScoreIdx = -1;
                //finding which image best fits the inputed image
                for (int i = 0; i < tally.length; i++) {
                    if (tally[i] > maxScore) {
                        maxScore = tally[i];
                        maxScoreIdx = i;
                    }
                }
                String className = ImageClasses.ImageClassesNet[maxScoreIdx];
                TextView textView = findViewById(R.id.guess);
                textView.setText(className);
            }
        });
    }
    // helper method to read the file paths
    public static String readingAsset(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }
        // basic try method to get the file path
        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

}