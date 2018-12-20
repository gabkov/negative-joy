package com.gabor.negtivejoy;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;


public class EmotionHandler {

    private final String apiEndpoint = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0";

    private final String subscriptionKey = "33d1cbd9cd954b5fb581254114fe5a8d";

    private final FaceServiceClient faceServiceClient = new FaceServiceRestClient(apiEndpoint, subscriptionKey);

    private ImageView bottleCapImageView;

    EmotionHandler(ImageView bottleCapImageView){
        this.bottleCapImageView = bottleCapImageView;
    }


    public void detectAndFrame(final Bitmap imageBitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());

        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    String exceptionMessage = "";

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    true,        // returnFaceLandmarks
                                    // returnFaceAttributes:
                                    new FaceServiceClient.FaceAttributeType[]{
                                            FaceServiceClient.FaceAttributeType.Age,
                                            FaceServiceClient.FaceAttributeType.Gender,
                                            FaceServiceClient.FaceAttributeType.Emotion}
                            );
                            if (result == null) {
                                publishProgress(
                                        "Detection Finished. Nothing detected");
                                return null;
                            }
                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Detection failed: %s", e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Face[] result) {
                        //TODO: update face frames

                        if (!exceptionMessage.equals("")) {
                            showError(exceptionMessage);
                        }
                        if (result == null) return;


                        drawFaceRectanglesOnBitmap(result);
                        imageBitmap.recycle();
                    }
                };

        detectTask.execute(inputStream);
    }

    private void showError(String message) {
        System.out.println("ERROR: " + message);
    }

    private void drawFaceRectanglesOnBitmap(Face[] faces) {
        if (faces != null) {
            for (Face face : faces) {
                Emotion faceAttribute = face.faceAttributes.emotion;
                System.out.println("Happinnes" + faceAttribute.happiness);
                if(Math.round(faceAttribute.happiness) >= 1){
                    bottleCapImageView.setImageResource(R.drawable.emotionhappy);
                    System.out.println("Happinnes" + faceAttribute.happiness);
                }

            }
        }
    }
}
