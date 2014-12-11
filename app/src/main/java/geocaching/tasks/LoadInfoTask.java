package geocaching.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
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
        return parseInfo(resultHtml);
    }

    private static JSONObject parseInfo(String html) {
        Document doc = Jsoup.parse(html);

        Elements textElements = doc.select("b u");
        JSONObject jsonObject = new JSONObject();
        try {
            for (int i = 0; i < textElements.size(); i++) {
                Node node = textElements.get(i).parent();
                String key = "";
                switch (((Element) node).text()) {
                    case "Атрибуты":
                        key = "attributes";
                        break;
                    case "Описание тайника":
                        key = "description";
                        break;
                    case "Описание окружающей местности":
                        key = "surroundingArea";
                        break;
                    case "Содержимое тайника":
                        key = "content";
                        break;
                }
                String result = "";
                while (node != null && !node.nodeName().equalsIgnoreCase("hr")) {
                    node = node.nextSibling();
                    if (node != null) {
                        if (node instanceof TextNode)
                            result += ((TextNode) node).text();
                        else
                            result += ((Element) node).text();
                    }
                }
                jsonObject.put(key, result);
            }

            Elements elements = doc.select("p>b");

            jsonObject.put("name", getTextByIndex(elements, 0));

            JSONObject authorObject = new JSONObject();
            authorObject.put("name", getTextByIndex(elements, 1));
            authorObject.put("id", doc.select("a[href~=profile.php]").get(0).attr("href").split("=")[1]);
            jsonObject.put("author", authorObject);

            jsonObject.put("created", getTextByIndex(elements, 2));
            jsonObject.put("updated", getTextByIndex(elements, 3));
            jsonObject.put("coordinates", getTextByIndex(elements, 4));
            jsonObject.put("country", getTextByIndex(elements, 5));
            jsonObject.put("region", getTextByIndex(elements, 6));
            jsonObject.put("city", getTextByIndex(elements, 7));
            jsonObject.put("difficulty", getTextByIndex(elements, 8));
            jsonObject.put("terrain", getTextByIndex(elements, 9));
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
