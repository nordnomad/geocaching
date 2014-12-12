package geocaching.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import map.test.myapplication3.app.R;

public class CommentsTabAdapter extends RecyclerView.Adapter<CommentsTabAdapter.ViewHolder> {
    private JSONArray jsonArray;

    public CommentsTabAdapter(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    @Override
    public CommentsTabAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            JSONObject jo = jsonArray.getJSONObject(position);
            holder.messageView.setText(jo.getString("message"));
            holder.dateView.setText(jo.getString("date"));
            holder.userView.setText(jo.getString("user"));
        } catch (JSONException e) {
            e.printStackTrace();
            holder.messageView.setText("error");
        }

    }

    @Override
    public int getItemCount() {
        return jsonArray.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView messageView;
        public TextView dateView;
        public TextView userView;

        public ViewHolder(View v) {
            super(v);
            messageView = (TextView) v.findViewById(R.id.commentMessageLabel);
            dateView = (TextView) v.findViewById(R.id.commentDateLabel);
            userView = (TextView) v.findViewById(R.id.commentAuthorNameLabel);
        }
    }
}
