package com.pipit.waffle;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.pipit.waffle.Objects.Choice;
import com.pipit.waffle.Objects.ClientData;
import com.pipit.waffle.Objects.Question;
import com.pipit.waffle.Objects.User;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 12/7/2014.
 */
public class Network {

    public static void getAllQuestions(final Context mcontext, final int numberOfQuestionsNeeded){
        /*This function currently populates clientData directly (bad practice)*/
        //TODO: make this block with a progress dialog and return questions directly, and let the caller populate the fields
        Log.d("ConnectToBackend", "starting getAllQuestions");
        final String url = "http://obscure-fjord-2523.herokuapp.com/api/questions/";
        ToolbarActivity ma = (ToolbarActivity) mcontext;

        ProgressBar mProgress = (ProgressBar) ma.findViewById(R.id.progress_bar);
        ProgressBar progressBar = new ProgressBar(mcontext);
        progressBar.setIndeterminate(true);
        ProgressDialog progressDialog = new ProgressDialog(mcontext);
        try{
            Ion.with(mcontext)
                    .load(url)
                    .asJsonArray()
                    .setCallback(new FutureCallback<JsonArray>() {
                        @Override
                        public void onCompleted(Exception e, JsonArray result) {
                            List<JsonObject> jsonlist = new ArrayList<JsonObject>();
                            Log.d("ConnectToBackend", "result received" + result.size() + " and numberOfQuestionsNeeded " + numberOfQuestionsNeeded);
                            if (result != null) {
                                for (int i = 0; i < result.size(); i++) {
                                    jsonlist.add(result.get(i).getAsJsonObject());
                                }
                            }
                            int k = 0;
                            int jsonlistindex = 0;

                            while(k < numberOfQuestionsNeeded && k < jsonlist.size() && jsonlistindex<jsonlist.size()) {
                                Log.d("ConnectToBackend", "jsonList" + jsonlist.get(jsonlistindex).get("text").getAsString() + " k=" + k + " jsonlistindex=" + jsonlistindex + " ");

                                //Get text body, user of question
                                if (!ClientData.getInstance().getIdsOfAnsweredQuestions().contains(jsonlist.get(jsonlistindex).get("id").getAsString())) {
                                    String questionbody = jsonlist.get(jsonlistindex).get("text").getAsString();
                                    String userID = jsonlist.get(jsonlistindex).get("user_id").getAsString();
                                    User tempuser = new User(userID);
                                    Question nq = new Question(questionbody, tempuser);

                                    JsonArray answerJson = jsonlist.get(jsonlistindex).get("answers").getAsJsonArray();
                                    List<JsonObject> answerJsonList = new ArrayList<JsonObject>();
                                    for (int i = 0; i < answerJson.size(); i++) {
                                        answerJsonList.add(answerJson.get(i).getAsJsonObject());
                                    }

                                    for (int j = 0; j < answerJsonList.size(); j++) {
                                        try {
                                            String answerBody = answerJsonList.get(j).get("text").getAsString();
                                            int questionIDinteger = answerJsonList.get(j).get("id").getAsInt();
                                            String questionID = Integer.toString(questionIDinteger);
                                            int answerVotes = answerJsonList.get(j).get("votes").getAsInt();
                                            String picurl = "";
                                            if( answerJsonList.get(j).has("picture")){
                                                if (!answerJsonList.get(j).get("picture").isJsonNull()) {
                                                    picurl = answerJsonList.get(j).get("picture").getAsString();
                                                }
                                            }

                                            Choice newans = new Choice(questionID);
                                            newans.setAnswerBody(answerBody);
                                            newans.setVotes(answerVotes);
                                            newans.setQuestionID(questionID);
                                            newans.setUrl(picurl);
                                            if (picurl=="" || !picurl.contains("http")){
                                                newans.imageState = Choice.LoadState.NO_IMAGE;
                                            }else{
                                                newans.imageState = Choice.LoadState.NOT_LOADED;
                                            }
                                            nq.addChoice(newans);
                                            } catch (Exception e1){
                                                Log.d("Network", "Poorly formatted question " + e1.toString() +  " " + nq.getQuestionBody());
                                                //Poorly formatted question
                                                //Todo: Announce or log poorly formatted question.
                                            }
                                    }
                                    if (nq.getChoices().size() == 2) {
                                        ClientData.addQuestion(nq);
                                        k++;
                                    }
                                }
                                jsonlistindex++;
                            }

                            return;
                        }
                    });
        }catch(Exception e){
            Log.d("ConnectToBackend", "getAllQuestions called with url " + url);
            if (e != null) {
                Toast.makeText(mcontext, "Error loading questions " + e.toString(), Toast.LENGTH_LONG).show();
                Log.d("ConnectToBackend", e.toString());
                return;
            }
        }



    }

    /*
        getOneQuestionWithCallback is used when the queue is empty. It will start the loading of the question
        and use a callback to fill it out when the data arrives, allowing the question to be displayed immediately
        with a spinner and get updated when ready.
     */
    public static void getOneQuestionWithCallback(final Context mcontext, final Question nq){
    /*This function currently populates clientData directly (bad practice)*/
        //TODO: make this block with a progress dialog and return questions directly, and let the caller populate the fields
        Log.d("ConnectToBackend", "starting getQuestions");
        final String url = "http://obscure-fjord-2523.herokuapp.com/api/questions/";
        ToolbarActivity ma = (ToolbarActivity) mcontext;


        JsonArray result = new JsonArray();
        try{
            Ion.with(mcontext)
                    .load(url)
                    .asJsonArray()
                    .setCallback(new FutureCallback<JsonArray>() {
                        @Override
                        public void onCompleted(Exception e, JsonArray result) {
                            List<JsonObject> jsonlist = new ArrayList<JsonObject>();
                            if (result != null) {
                                for (int i = 0; i < result.size(); i++) {
                                    jsonlist.add(result.get(i).getAsJsonObject());
                                }
                            }
                            int k = 0;
                            int jsonlistindex = 0;

                            while(k < 1 && k < jsonlist.size() && jsonlistindex<jsonlist.size()) {
                                Log.d("ConnectToBackend", "jsonList" + jsonlist.get(k).get("text").getAsString() + " k=" + k + " jsonlistindex=" + jsonlistindex + " ");

                                //Get text body, user of question
                                if (!ClientData.getInstance().getIdsOfAnsweredQuestions().contains(jsonlist.get(jsonlistindex).get("id").getAsString())) {
                                    try {
                                        nq.setQuestionBody(jsonlist.get(jsonlistindex).get("text").getAsString());
                                    }catch (Exception e1){}
                                    String userID = jsonlist.get(jsonlistindex).get("user_id").getAsString();
                                    User tempuser = new User(userID);

                                    JsonArray answerJson = jsonlist.get(jsonlistindex).get("answers").getAsJsonArray();
                                    List<JsonObject> answerJsonList = new ArrayList<JsonObject>();
                                    for (int i = 0; i < answerJson.size(); i++) {
                                        answerJsonList.add(answerJson.get(i).getAsJsonObject());
                                    }

                                    for (int j = 0; j < answerJsonList.size(); j++) {
                                        try {
                                            String answerBody = answerJsonList.get(j).get("text").getAsString();
                                            int questionIDinteger = answerJsonList.get(j).get("id").getAsInt();
                                            String questionID = Integer.toString(questionIDinteger);
                                            int answerVotes = answerJsonList.get(j).get("votes").getAsInt();
                                            String picurl = "";
                                            if( answerJsonList.get(j).has("picture")){
                                                picurl = answerJsonList.get(j).get("picture").getAsString();
                                            }

                                            Choice newans = new Choice(questionID);
                                            newans.setAnswerBody(answerBody);
                                            newans.setVotes(answerVotes);
                                            newans.setQuestionID(questionID);
                                            if (picurl.isEmpty() || !picurl.contains("http")){
                                                newans.imageState = Choice.LoadState.NO_IMAGE;
                                            }else{
                                                newans.imageState = Choice.LoadState.NOT_LOADED;
                                            }
                                            newans.setUrl(picurl);
                                            nq.addChoice(newans);
                                        } catch (Exception except){
                                            //Poorly formatted question
                                            //Todo: Announce or log poorly formatted question.
                                        }
                                    }
                                    if (nq.getChoices().size() == 2) {
                                        //ClientData.addQuestion(nq);
                                        k++;
                                    }
                                }
                                jsonlistindex++;
                            }
                            return;
                        }
                    });

        }catch(Exception e){
            Log.d("ConnectToBackend", "getAllQuestions called with url " + url);
            if (e != null) {
                Toast.makeText(mcontext, "Error loading questions " + e.toString(), Toast.LENGTH_LONG).show();
                Log.d("ConnectToBackend", e.toString());
                return;
            }
        }

    }

    public static void answerQuestion(final Context mcontext, Choice providedAnswer){
        JsonObject json = new JsonObject();
        json.addProperty("vote", "true");
        final String url = "http://obscure-fjord-2523.herokuapp.com/api/answers/"+providedAnswer.getQuestionID()+"/";
        Ion.with(mcontext)
                .load("PUT", url)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null){
                            Log.d("ConnectToBackend", "answerquestions called with "+url+" and has error" + e.toString());
                            if (result==null){
                                Log.d("ConnectToBackend", "answerQuestions returns result with NULL");
                            }
                        }
                        else{
                            Log.d("ConnectToBackend", "answerQuestion asked with url " + url + " : and result " + result.toString());
                        }

                    }
                });
    }

    public static void postQuestion(final Context mcontext, Question mquestion){
        JsonArray answerarray = new JsonArray();
        JsonObject answerjson = new JsonObject();
        JsonObject answerjson2 = new JsonObject();

        answerjson.addProperty("text", mquestion.getChoices().get(0).getAnswerBody());
        answerjson.addProperty("votes", 0);
        answerjson.addProperty("id", mquestion.getId());

        answerjson2.addProperty("text", mquestion.getChoices().get(1).getAnswerBody());
        answerjson2.addProperty("votes", 0);
        answerjson2.addProperty("id", mquestion.getId());


        answerarray.add(answerjson);
        answerarray.add(answerjson2);


        JsonObject json = new JsonObject();
        json.addProperty("text", mquestion.getQuestionBody());
        json.add("answers", answerarray);
        json.addProperty("user_id", "temp user id");
        final String url = "http://obscure-fjord-2523.herokuapp.com/api/questions/";
        Log.d("ConnectToBackend", json.toString());
        Ion.with(mcontext)
                .load(url)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null){
                            Log.d("ConnectToBackend", "postQuestion called with "+url+" and has error" + e.toString());
                            if (result==null){
                                Log.d("ConnectToBackend", "postQuestion returns result with NULL");
                            }
                            Toast.makeText(mcontext, "Error submitting question " + e.toString(), Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(mcontext, "Sucessfully posted question", Toast.LENGTH_LONG).show();
                            Log.d("ConnectToBackend", "postQuestion asked with url " + url + " : and result " + result.toString());

                            //Switch to questions fragment
                         //   ToolbarActivity ma = (ToolbarActivity) mcontext;
                            /*
                            if(ma.getShowingFragmentID().equals(Constants.CREATE_QUESTION_FRAGMENT_ID))
                                ma.switchToFragment(Constants.QUESTION_ANSWER_FRAGMENT_ID);
                                */
                         //   ma.switchFragments();
                        }

                    }
                });
    }

    public static void postQuestionWithImage(final Context mcontext, Question mquestion, final ProgressBar uploadProgressBar){
        JsonObject json = new JsonObject();
        json.addProperty("text", mquestion.getQuestionBody());
        json.addProperty("user_id", "temp user id");
        final String url = "http://obscure-fjord-2523.herokuapp.com/api/questions/";
        Ion.with(mcontext)
                .load(url)

                .setMultipartParameter("answerOneText", mquestion.getChoices().get(0).getAnswerBody())
                .setMultipartParameter("answerTwoText", mquestion.getChoices().get(1).getAnswerBody())
                .setMultipartParameter("text", mquestion.getQuestionBody())
                .setMultipartParameter("user_id", "temp_user_posting_picture")
                .setMultipartFile("imageOne", persistImage(mcontext, sampleBitmap(mcontext), "sample"))
                .setMultipartFile("imageTwo", persistImage(mcontext, sampleBitmap(mcontext), "sample"))
                        .asJsonObject()
                        .setCallback(new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if (e != null) {
                                    Log.d("ConnectToBackend", "postQuestionWithPicture called with " + url + " and has error" + e.toString());
                                    if (result == null) {
                                        Log.d("ConnectToBackend", "postQuestionWithPicture returns result with NULL");
                                    }
                                    Toast.makeText(mcontext, "Error submitting question " + e.toString(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(mcontext, "Sucessfully posted questionWithPicture", Toast.LENGTH_LONG).show();
                                    Log.d("ConnectToBackend", "postQuestionWithPicture asked with url " + url + " : and result " + result.toString());
                                }

                            }
                        });
    }

    /**
     * Used for testing
     * @return A file with an image
     */
    private static File persistImage(Context mcontext, Bitmap bitmap, String name) {
        File filesDir = mcontext.getFilesDir();
        File imageFile = new File(filesDir, name + ".png");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e("ConnectToBackend", "Error writing bitmap", e);
        }
        return imageFile;
    }

    private static Bitmap sampleBitmap(Context mcontext){
        Bitmap bm = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.test_image4);
        return bm;
    }
}
