package geocaching.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import geocaching.Const;

import static geocaching.Utils.getInputSteamReader;

public class LoadInfoTask extends AsyncTask<Long, Void, JSONObject> {

    Context ctx;

    public LoadInfoTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected JSONObject doInBackground(Long... params) {
        long geoCacheId = params[0];
        StringBuilder html = new StringBuilder();
        char[] buffer = new char[1024];
        BufferedReader in = null;
        try {
            InputStreamReader inputStreamReader = getInputSteamReader(new URL(String.format(Const.INFO_URL, geoCacheId)));
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

        Document doc = Jsoup.parse(resultHtml);
        Elements elements = doc.getElementsByTag("b");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", elements.get(0).text());
            jsonObject.put("author", elements.get(1).text());
            jsonObject.put("created", elements.get(2).text());
            jsonObject.put("updated", elements.get(3).text());
            jsonObject.put("coordinates", elements.get(4).text());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }


    @Override
    protected void onPostExecute(JSONObject s) {
        super.onPostExecute(s);
        Toast.makeText(ctx, s.toString(), Toast.LENGTH_LONG).show();
    }
}
