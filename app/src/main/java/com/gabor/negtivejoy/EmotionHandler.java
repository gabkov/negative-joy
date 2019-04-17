package com.gabor.negtivejoy;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.gabor.negtivejoy.Interfaces.DetectionProgressDialogHandler;
import com.gabor.negtivejoy.Interfaces.Toaster;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;


public class EmotionHandler {

    private final String apiEndpoint = "https://westeurope.api.cognitive.microsoft.com/face/v1.0";

    private final String subscriptionKey = "";

    private final FaceServiceClient faceServiceClient = new FaceServiceRestClient(apiEndpoint, subscriptionKey);

    private ImageView bottleCapImageView;
    private Toaster toaster;
    private DetectionProgressDialogHandler detectionProgressDialogHandler;

    EmotionHandler(ImageView bottleCapImageView, Toaster toaster, DetectionProgressDialogHandler detectionProgressDialogHandler) {
        this.bottleCapImageView = bottleCapImageView;
        this.toaster = toaster;
        this.detectionProgressDialogHandler = detectionProgressDialogHandler;
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
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                        detectionProgressDialogHandler.showDetectionDialog();
                    }

                    @Override
                    protected void onPostExecute(Face[] result) {
                        //TODO: update face frames
                        detectionProgressDialogHandler.dismissDetectionDialog();

                        if (!exceptionMessage.equals("")) {
                            showError(exceptionMessage);
                        }
                        if (result == null) return;


                        detectEmotion(result);
                        imageBitmap.recycle();
                    }
                };

        detectTask.execute(inputStream);
    }

    private void showError(String message) {
        System.out.println("ERROR: " + message);
    }

    private void detectEmotion(Face[] faces) {
        Map<Emotions, Double> emotionsMap = new HashMap<>();

        if (faces != null) {
            for (Face face : faces) {
                Emotion faceAttribute = face.faceAttributes.emotion;
                emotionsMap.put(Emotions.HAPPY, faceAttribute.happiness);
                emotionsMap.put(Emotions.ANGRY, faceAttribute.anger);
                emotionsMap.put(Emotions.FEAR, faceAttribute.fear);
                emotionsMap.put(Emotions.SAD, faceAttribute.sadness);
                emotionsMap.put(Emotions.SURPRISE, faceAttribute.surprise);
                emotionsMap.put(Emotions.DISGUST, faceAttribute.disgust);
                emotionsMap.put(Emotions.NEUTRAL, faceAttribute.neutral);
            }
        }

        createEmotionFeedback(emotionsMap);
    }

    private void createEmotionFeedback(Map<Emotions, Double> emotionsDoubleMap){
        DecimalFormat df = new DecimalFormat("#.##");

        Map.Entry<Emotions, Double> maxEntry = null;

        for (Map.Entry<Emotions, Double> entry : emotionsDoubleMap.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }

        Emotions emotion;
        if(maxEntry == null){
            emotion = Emotions.FAIL;
        }else{
            emotion = maxEntry.getKey();
        }

        switch (emotion){
            case HAPPY:
                bottleCapImageView.setImageResource(R.drawable.emotionhappy);
                toaster.displayToast("Happiness " + df.format(emotionsDoubleMap.get(emotion) * 100) + " %");
                break;
            case ANGRY:
                bottleCapImageView.setImageResource(R.drawable.angryemotion);
                toaster.displayToast("Anger " + df.format(emotionsDoubleMap.get(emotion) * 100) + " %");
                break;
            case FEAR:
                bottleCapImageView.setImageResource(R.drawable.fearemotion);
                toaster.displayToast("Fear " + df.format(emotionsDoubleMap.get(emotion) * 100) + " %");
                break;
            case SAD:
                bottleCapImageView.setImageResource(R.drawable.sademotion);
                toaster.displayToast("Sadness " + df.format(emotionsDoubleMap.get(emotion) * 100) + " %");
                break;
            case SURPRISE:
                bottleCapImageView.setImageResource(R.drawable.surpriseemotion);
                toaster.displayToast("Surprise " + df.format(emotionsDoubleMap.get(emotion) * 100) + " %");
                break;
            case DISGUST:
                bottleCapImageView.setImageResource(R.drawable.disgustemotion);
                toaster.displayToast("Disgust " + df.format(emotionsDoubleMap.get(emotion) * 100) + " %");
                break;
            case NEUTRAL:
                toaster.displayToast("Neutral " + df.format(emotionsDoubleMap.get(emotion) * 100) + " %");
                bottleCapImageView.setImageResource(R.drawable.neutralemotion);
                break;
            case FAIL:
                toaster.displayToast("Please try again.");
                break;
        }
    }

    private enum Emotions{
        HAPPY,
        ANGRY,
        FEAR,
        SAD,
        SURPRISE,
        DISGUST,
        NEUTRAL,
        FAIL,
    }
}
