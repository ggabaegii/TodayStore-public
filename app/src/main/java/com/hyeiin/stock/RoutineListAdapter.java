package com.hyeiin.stock;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class RoutineListAdapter extends RecyclerView.Adapter<RoutineListAdapter.VH> {

    public interface OnRoutineClickListener {
        void onClick(Routine routine);
    }

    private static final String[] KOR_DAYS = {"일", "월", "화", "수", "목", "금", "토"};

    private final List<Routine> routines;
    private final OnRoutineClickListener listener;
    private final String todayKor;

    public RoutineListAdapter(List<Routine> routines, OnRoutineClickListener listener) {
        this.routines = routines;
        this.listener = listener;
        int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        this.todayKor = KOR_DAYS[dow - 1];
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_routine, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(routines.get(position));
    }

    @Override
    public int getItemCount() {
        return routines.size();
    }

    class VH extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvDayHint;

        VH(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRoutineName);
            tvDayHint = itemView.findViewById(R.id.tvRoutineDayHint);
        }

        void bind(Routine routine) {
            tvName.setText(routine.getName());

            String dayHint = routine.getDayHint() != null ? routine.getDayHint() : "";
            String defaultHint = (dayHint.isEmpty() ? "" : dayHint + " · ")
                    + routine.getCount() + "개 항목";
            tvDayHint.setText(defaultHint);

            boolean isToday = dayHint.contains(todayKor);
            if (isToday) {
                tvDayHint.setTextColor(itemView.getContext().getColor(R.color.colorPrimary));
                tvDayHint.setText("오늘(" + todayKor + ") 할 일 · " + routine.getCount() + "개 항목");
            } else {
                tvDayHint.setTextColor(itemView.getContext().getColor(R.color.colorHint));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(routine);
                }
            });
        }
    }
}
