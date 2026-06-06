package com.example.planck;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.planck.database.AppDatabase;
import com.example.planck.database.TodoItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class TodoFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView tvProgressCount;
    private TextView tvPostponedLabel;
    private View cardPostponed;

    private RecyclerView rvTodayTodos;
    private RecyclerView rvPostponedTodos;
    private RecyclerView rvDoneTodos;

    private TodoAdapter todayAdapter;
    private TodoAdapter postponedAdapter;
    private TodoAdapter doneAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo, container, false);

        progressBar = view.findViewById(R.id.progress_bar);
        tvProgressCount = view.findViewById(R.id.tv_progress_count);
        tvPostponedLabel = view.findViewById(R.id.tv_postponed_label);
        cardPostponed = view.findViewById(R.id.card_postponed);

        // 設定三個 RecyclerView
        rvTodayTodos = view.findViewById(R.id.rv_today_todos);
        rvPostponedTodos = view.findViewById(R.id.rv_postponed_todos);
        rvDoneTodos = view.findViewById(R.id.rv_done_todos);

        rvTodayTodos.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPostponedTodos.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDoneTodos.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 建立 Adapter
        todayAdapter = new TodoAdapter(new TodoAdapter.OnTodoActionListener() {
            @Override
            public void onChecked(TodoItem todo) { markTodoDone(todo); }
            @Override
            public void onPostpone(TodoItem todo) { postponeTodo(todo); }
        });

        postponedAdapter = new TodoAdapter(new TodoAdapter.OnTodoActionListener() {
            @Override
            public void onChecked(TodoItem todo) { markTodoDone(todo); }
            @Override
            public void onPostpone(TodoItem todo) { postponeTodo(todo); }
        });

        doneAdapter = new TodoAdapter(new TodoAdapter.OnTodoActionListener() {
            @Override
            public void onChecked(TodoItem todo) {}
            @Override
            public void onPostpone(TodoItem todo) {}
        });

        rvTodayTodos.setAdapter(todayAdapter);
        rvPostponedTodos.setAdapter(postponedAdapter);
        rvDoneTodos.setAdapter(doneAdapter);

        // FAB 新增按鈕
        FloatingActionButton fab = view.findViewById(R.id.fab_add_todo);
        fab.setOnClickListener(v -> showAddTodoDialog());

        loadTodos();
        return view;
    }

    private void loadTodos() {
        AppDatabase db = AppDatabase.getInstance(requireContext());

        db.todoDao().getActiveTodos().observe(getViewLifecycleOwner(), todos -> {
            List<TodoItem> todayList = new ArrayList<>();
            List<TodoItem> postponedList = new ArrayList<>();

            for (TodoItem todo : todos) {
                if (todo.status.equals("POSTPONED")) {
                    postponedList.add(todo);
                } else {
                    todayList.add(todo);
                }
            }

            todayAdapter.setTodos(todayList);
            postponedAdapter.setTodos(postponedList);

            if (postponedList.isEmpty()) {
                tvPostponedLabel.setVisibility(View.GONE);
                cardPostponed.setVisibility(View.GONE);
            } else {
                tvPostponedLabel.setVisibility(View.VISIBLE);
                cardPostponed.setVisibility(View.VISIBLE);
            }

            updateProgress(todos.size());
        });

        db.todoDao().getCompletedTodos().observe(getViewLifecycleOwner(), doneTodos -> {
            doneAdapter.setTodos(doneTodos);
            updateProgressWithDone(doneTodos.size());
        });
    }

    private int activeCount = 0;
    private int doneCount = 0;

    private void updateProgress(int active) {
        activeCount = active;
        refreshProgress();
    }

    private void updateProgressWithDone(int done) {
        doneCount = done;
        refreshProgress();
    }

    private void refreshProgress() {
        int total = activeCount + doneCount;
        if (total > 0) {
            progressBar.setProgress(doneCount * 100 / total);
            tvProgressCount.setText(doneCount + " / " + total + " 完成");
        } else {
            progressBar.setProgress(0);
            tvProgressCount.setText("0 / 0 完成");
        }
    }

    private void markTodoDone(TodoItem todo) {
        new Thread(() -> {
            todo.status = "DONE";
            todo.completedAt = System.currentTimeMillis();
            AppDatabase.getInstance(requireContext()).todoDao().update(todo);
        }).start();
    }

    private void postponeTodo(TodoItem todo) {
        new Thread(() -> {
            todo.status = "POSTPONED";
            todo.postponeCount += 1;
            AppDatabase.getInstance(requireContext()).todoDao().update(todo);
        }).start();
    }

    private void showAddTodoDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_todo);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextInputEditText etTitle = dialog.findViewById(R.id.et_todo_title);
        RadioGroup rgPriority = dialog.findViewById(R.id.rg_priority);

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String title = etTitle.getText() != null ?
                    etTitle.getText().toString().trim() : "";
            if (title.isEmpty()) {
                etTitle.setError("請輸入待辦名稱");
                return;
            }

            int priority = 0;
            int selectedId = rgPriority.getCheckedRadioButtonId();
            if (selectedId == R.id.rb_urgent) priority = 2;
            else if (selectedId == R.id.rb_important) priority = 1;

            int finalPriority = priority;
            new Thread(() -> {
                TodoItem newTodo = new TodoItem();
                newTodo.title = title;
                newTodo.priority = finalPriority;
                newTodo.status = "TODO";
                newTodo.deadlineTime = 0;
                newTodo.reminderTime = 0;
                newTodo.linkedEventId = -1;
                newTodo.postponeCount = 0;
                newTodo.createdAt = System.currentTimeMillis();
                newTodo.completedAt = 0;
                AppDatabase.getInstance(requireContext()).todoDao().insert(newTodo);
            }).start();

            dialog.dismiss();
        });

        dialog.show();
    }
}