package ca.nait.bsebagabo1.githubtimev2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Layout;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;



public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private String TAG = MainActivity.class.getSimpleName();
    private String username;
    private Button btnSearch;
    private ListView lv;
    private ProgressDialog pDialog;

    // Global Variables for setting values to the UI Thread
    private TextView fullName;
    private TextView location;
    private TextView public_repos;
    private TextView followers;
    private TextView following;
    private ImageView profileAvatar;
    private LinearLayout profile;
    private LinearLayout profileparent;

    // URL to  github api  repos
    private static String url= "https://api.github.com/users/";

    // HashMap to serve the listview
    ArrayList<HashMap<String, String>> githubRepos;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT > 9) {
            // This allows everything on all threads to prevent usage of the main thread
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Context context;
        btnSearch = (Button)findViewById(R.id.search_btn);
        btnSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        EditText usernameValue = (EditText) findViewById(R.id.search_username);
        username= usernameValue.getText().toString();
        username= usernameValue.getText().toString();
        githubRepos = new ArrayList<>();
        lv= findViewById(R.id.repos_list);
        switch (view.getId()) {
            case R.id.search_btn: {
                if(username.isEmpty()){
                    Toast.makeText(this, "Please provide username", Toast.LENGTH_SHORT).show();
                }
                else{
                    // Call the Async task class to get json
                    new GetRepos().execute();
                }

            }
        }
    }
    /**
     * Async task class to get json by making HTTP call
     */
    private class GetRepos extends AsyncTask<Void, Void, Void>
    {

//        public GetRepos(ImageView profileAvatar){
//            this.profileAvatar= profileAvatar;
//        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url+username+"/repos");
            String jsonStrFollowers = sh.makeServiceCall(url+username+"/followers");
            String profileUrl = sh.makeServiceCall(url+username);
            Log.e(TAG, "Response from url: " + jsonStr);
            Log.e(TAG, "Profile from url: " + profileUrl);
            if (jsonStr != null) {
                try {
                    // Getting JSON Array node
                    JSONArray repos = new JSONArray(jsonStr);
                    // Converting String to JSON Object
                    JSONObject profileObj = new JSONObject(profileUrl);
                    Context context;
                    profile = (LinearLayout)findViewById(R.id.profile);
                    profileparent = (LinearLayout)findViewById(R.id.profile_parent);
                    fullName = (TextView) findViewById(R.id.fullname);
                    location = (TextView) findViewById(R.id.location);
                    public_repos = (TextView) findViewById(R.id.publicRepos);
                    followers = (TextView) findViewById(R.id.followers);
                    following = (TextView) findViewById(R.id.following);
                    profileAvatar= (ImageView) findViewById(R.id.avatar);

                    final String avatar_url = profileObj.getString("avatar_url");
                    final String fname = profileObj.getString("name");
                    final String loc = profileObj.getString("location");
                    final String repos_count = profileObj.getString("public_repos");
                    final String followers_count = profileObj.getString("followers");
                    final String following_count = profileObj.getString("following");
                    // looping through All Contacts
                    for (int i = 0; i < repos.length(); i++) {
                        JSONObject c = repos.getJSONObject(i);
                        String name = c.getString("name");
                        String description = c.getString("description");
                        String open_issue = c.getString("open_issues_count");
                        String updated_at = c.getString("updated_at");

                        // New Fetch for contributors
                        //contributorsUrl = sh.makeServiceCall("https://api.github.com/"+"repos/"+username+"/"+name+"/stats/contributors");
                        // owner node is JSON Object
                        JSONObject owner = c.getJSONObject("owner");
                        // tmp hash map for single contact
                        HashMap<String, String> repository = new HashMap<>();

                        // adding each child node to HashMap key => value
                        repository.put("name", name);
                        repository.put("description",description.contains("null")? "No Description yet!":description);
                        repository.put("open_issue", "Open issues: "+open_issue);
                        repository.put("updated_at", "Updated "+updated_at);
                        // adding contact to contact list
                        githubRepos.add(repository);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                profile.setVisibility(View.VISIBLE);
                                profileparent.setVisibility(View.VISIBLE);
                                URL url = new URL(avatar_url);
                                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                RoundedImg roundedImage = new RoundedImg(bmp);
                                profileAvatar.setImageDrawable(roundedImage);
                            } catch (MalformedURLException e)
                            {
                                e.printStackTrace();
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            fullName.setText(fname.contains("null")? "No Name yet!":fname);
                            location.setText(loc.contains("null")? "No Location yet!":loc);
                            public_repos.setText(repos_count);
                            followers.setText(followers_count);
                            following.setText(following_count);
                        }
                    });

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, githubRepos,
                    R.layout.list_item, new String[]{"name", "description","open_issue","updated_at",}, new int[]{R.id.reposName, R.id.description,R.id.open_issue,R.id.updated_at});
            lv.setAdapter(adapter);
        }

    }
}
