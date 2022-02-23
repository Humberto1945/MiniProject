package com.example.miniproject;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import org.pytorch.IValue;
// import org.pytorch.LiteModuleLoader;
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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Uri selectedImage;
    Bitmap imageBitmap;
    Module moduleResNet;
    int imageSize = 224;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Set buttons
        Button selectButton = findViewById(R.id.select);
        Button guessButton = findViewById(R.id.loadImage);
        try {
            moduleResNet = Module.load(readingAsset(this,"model.ptl"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Lets the user select an image from their camera roll
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 50);
            }
        });

        // Gets the guess from the model
        guessButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //resize the bitmap
                Bitmap resize = Bitmap.createScaledBitmap(imageBitmap, imageSize,imageSize, false);
                final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resize,
                        TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);

                final Tensor outputTensor = moduleResNet.forward(IValue.from(inputTensor)).toTensor();
                final float[] tally = outputTensor.getDataAsFloatArray( );

                float maxScore = 0;
                int maxScoreIdx = 0;
                //finding which image best fits the inputted image
                for (int i = 0; i < tally.length; i++) {
                    if (tally[i] > maxScore) {
                        maxScore = tally[i];
                        maxScoreIdx = i;
                    }
                }
                Log.d(Integer.toString(maxScoreIdx), "This is my max score");


                String className = ImageClasses.ImageClassesNet[maxScoreIdx];
                TextView guessTextView = findViewById(R.id.guess);
                guessTextView.setText(String.valueOf(className));
               // guessTextView.setText(className);

            }
        });
    }//end of on create
    // Override our onActivityResult to input what result we want to happen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            ImageView imageView = findViewById(R.id.imageView);
            selectedImage = data.getData();
            imageView.setImageURI(selectedImage);
            try {
                //Getting the bitmap image
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }


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