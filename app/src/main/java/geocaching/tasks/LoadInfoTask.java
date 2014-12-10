package geocaching.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

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

public class LoadInfoTask extends AsyncTask<Long, Void, JSONObject> {

    Activity ctx;

    public LoadInfoTask(Activity ctx) {
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
        Elements elements = doc.select("p>b");

        Elements textElements = doc.select("b u");
        JSONObject jsonObject = new JSONObject();
        try {
            for (int i = 0; i < textElements.size(); i++) {
                Element node = textElements.get(i).parent();
                String result = "";
                while (!node.nodeName().equalsIgnoreCase("hr")) {
                    node = node.nextElementSibling();
                    // TODO verify NPE, parse other 4 properties
                    result += node.text();
                }
                jsonObject.put("prop_" + i, result);
                Log.i("i", node.parent().nextSibling().nextSibling().toString());
            }
            jsonObject.put("name", getTextByIndex(elements, 0));
            jsonObject.put("authorName", getTextByIndex(elements, 1));
//            jsonObject.put("authorId", elements.get(1).text());
            jsonObject.put("created", getTextByIndex(elements, 2));
            jsonObject.put("updated", getTextByIndex(elements, 3));
            jsonObject.put("coordinates", getTextByIndex(elements, 4));
            jsonObject.put("country", getTextByIndex(elements, 5));
            jsonObject.put("region", getTextByIndex(elements, 6));
            jsonObject.put("city", getTextByIndex(elements, 7));
            jsonObject.put("difficulty", getTextByIndex(elements, 8));
            jsonObject.put("terrain", getTextByIndex(elements, 9));

            jsonObject.put("properties", getTextByIndex(elements, 9));
            jsonObject.put("description", getTextByIndex(elements, 9));
            jsonObject.put("environment", getTextByIndex(elements, 9));
            jsonObject.put("contains", getTextByIndex(elements, 9));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private static String getTextByIndex(Elements els, int idx) {
        if (els.size() < idx) return "";
        if (els.get(idx) != null) return els.get(idx).text();
        return "";
    }


    @Override
    protected void onPostExecute(JSONObject s) {
        super.onPostExecute(s);
        TextView viewById = (TextView) ctx.findViewById(R.id.cachejson);
        try {
            viewById.setText(s.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
