package geocaching.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import geocaching.Const;
import map.test.myapplication3.app.R;

import static geocaching.Utils.getInputSteamReader;

public class LoadCommentsTask extends AsyncTask<Long, Void, JSONArray> {
    Activity ctx;

    public LoadCommentsTask(Activity activity) {
        ctx = activity;
    }

    @Override
    protected JSONArray doInBackground(Long... params) {
        long geoCacheId = params[0];
        StringBuilder html = new StringBuilder();
        char[] buffer = new char[1024];
        BufferedReader in = null;
        try {
            InputStreamReader inputStreamReader = getInputSteamReader(new URL(String.format(Const.COMMENTS_URL, geoCacheId)));
            in = new BufferedReader(inputStreamReader);
            int size;
            while ((size = in.read(buffer)) != -1) {
                html.append(buffer, 0, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String resultHtml = html.toString();
        resultHtml = resultHtml.replace("windows-1251", "UTF-8");
        resultHtml = resultHtml.replaceAll("\\r|\\n", "");

        JSONArray jsonArray = new JSONArray();

        Document doc = Jsoup.parse(resultHtml);
        Elements dateElements = doc.select("b + i");
        Elements userElements = doc.select("b > u");
        try {
            for (int i = 0; i < dateElements.size(); i++) {
                Element node = dateElements.get(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("date", node.text());
                jsonObject.put("message", node.nextSibling().nextSibling().toString().trim());
                jsonObject.put("user", userElements.get(i).text());
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonArray;
    }

    @Override
    protected void onPostExecute(JSONArray s) {
        super.onPostExecute(s);
        TextView viewById = (TextView) ctx.findViewById(R.id.commentsjson);
        try {
            viewById.setText(s.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
