package com.example.planck;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.planck.database.AppDatabase;
import com.example.planck.database.TodoItem;
import com.google.android.material.checkbox.MaterialCheckBox;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    public interface OnTodoActionListener {
        void onChecked(TodoItem todo);
        void onPostpone(TodoItem todo);
    }

    private List<TodoItem> todos = new ArrayList<>();
    private OnTodoActionListener listener;

    public TodoAdapter(OnTodoActionListener listener) {
        this.listener = listener;
    }

    public void setTodos(List<TodoItem> todos) {
        this.todos = todos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo_row, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoItem todo = todos.get(position);

        holder.tvTitle.setText(todo.title);

        // 截止日
        if (todo.deadlineTime > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("M/d HH:mm", Locale.TAIWAN);
            holder.tvDeadline.setText("截止：" + sdf.format(new Date(todo.deadlineTime)));
        } else {
            holder.tvDeadline.setText("無截止日");
        }

        // 優先度 badge
        switch (todo.priority) {
            case 2:
                holder.tvBadge.setText("緊急");
                holder.tvBadge.setBackgroundResource(R.drawable.badge_red);
                holder.tvBadge.setTextColor(
                        holder.itemView.getContext().getResources()
                                .getColor(R.color.planck_red, null));
                break;
            case 1:
                holder.tvBadge.setText("重要");
                holder.tvBadge.setBackgroundResource(R.drawable.badge_orange);
                holder.tvBadge.setTextColor(
                        holder.itemView.getContext().getResources()
                                .getColor(R.color.planck_orange, null));
                break;
            default:
                holder.tvBadge.setText("一般");
                holder.tvBadge.setBackgroundResource(R.drawable.badge_green);
                holder.tvBadge.setTextColor(
                        holder.itemView.getContext().getResources()
                                .getColor(R.color.planck_green, null));
                break;
        }

        // 已完成狀態
        if (todo.status.equals("DONE")) {
            holder.checkbox.setChecked(true);
            holder.tvTitle.setPaintFlags(
                    holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(
                    holder.itemView.getContext().getResources()
                            .getColor(R.color.text_secondary, null));
            holder.tvPostpone.setVisibility(View.GONE);
        } else {
            holder.checkbox.setChecked(false);
            holder.tvTitle.setPaintFlags(
                    holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(
                    holder.itemView.getContext().getResources()
                            .getColor(R.color.text_primary, null));
            holder.tvPostpone.setVisibility(View.VISIBLE);
        }

        // 勾選完成
        holder.checkbox.setOnCheckedChangeListener(null);
        holder.checkbox.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked && !todo.status.equals("DONE")) {
                listener.onChecked(todo);
            }
        });

        // 延後按鈕
        holder.tvPostpone.setOnClickListener(v -> listener.onPostpone(todo));

        holder.itemView.setOnLongClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("刪除待辦")
                    .setMessage("確定要刪除「" + todo.title + "」嗎？")
                    .setPositiveButton("刪除", (dialog, which) -> {
                        new Thread(() -> {
                            AppDatabase.getInstance(holder.itemView.getContext())
                                    .todoDao().delete(todo);
                        }).start();
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return true;
        });


    }

    @Override
    public int getItemCount() {
        return todos.size();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        MaterialCheckBox checkbox;
        TextView tvTitle;
        TextView tvDeadline;
        TextView tvBadge;
        TextView tvPostpone;

        TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox_todo);
            tvTitle = itemView.findViewById(R.id.tv_todo_title);
            tvDeadline = itemView.findViewById(R.id.tv_todo_deadline);
            tvBadge = itemView.findViewById(R.id.tv_todo_badge);
            tvPostpone = itemView.findViewById(R.id.tv_postpone_btn);
        }
    }
}