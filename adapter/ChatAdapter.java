package com.mitra.ai.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mitra.ai.R;
import com.mitra.ai.model.Message;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messages;
    private final Context ctx;

    public ChatAdapter(List<Message> messages, Context ctx) {
        this.messages = messages;
        this.ctx = ctx;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutInflater inf = LayoutInflater.from(ctx);
        switch (type) {
            case Message.TYPE_USER:
                return new UserVH(inf.inflate(R.layout.item_user_message, parent, false));
            case Message.TYPE_TYPING:
                return new TypingVH(inf.inflate(R.layout.item_typing, parent, false));
            default:
                return new BotVH(inf.inflate(R.layout.item_bot_message, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        Message msg = messages.get(pos);
        if (holder instanceof UserVH) {
            ((UserVH) holder).tvMessage.setText(msg.getText());
            ((UserVH) holder).tvTime.setText(msg.getTime());
        } else if (holder instanceof BotVH) {
            ((BotVH) holder).tvMessage.setText(msg.getText());
            ((BotVH) holder).tvTime.setText(msg.getTime());
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class UserVH extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        UserVH(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTime = v.findViewById(R.id.tv_time);
        }
    }

    static class BotVH extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        BotVH(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tv_message);
            tvTime = v.findViewById(R.id.tv_time);
        }
    }

    static class TypingVH extends RecyclerView.ViewHolder {
        TypingVH(View v) { super(v); }
    }
}
