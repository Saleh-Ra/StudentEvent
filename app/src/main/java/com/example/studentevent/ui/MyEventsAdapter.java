package com.example.studentevent.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentevent.data.DatabaseHelper;
import com.example.studentevent.data.MyEventsSync;
import com.example.studentevent.R;
import com.example.studentevent.model.Event;

import java.util.ArrayList;

/**
 * Purpose: Adapter for the user's personal events list.
 * Input: Context and list of personal events.
 * Output: CardView items with reminder and action buttons.
 */
public class MyEventsAdapter extends RecyclerView.Adapter<MyEventsAdapter.MyEventViewHolder> {

    private Context context;
    private ArrayList<Event> myEvents;

    public MyEventsAdapter(Context context, ArrayList<Event> myEvents) {
        this.context = context;
        this.myEvents = myEvents;
    }

    @NonNull
    @Override
    public MyEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_event, parent, false);
        return new MyEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyEventViewHolder holder, int position) {
        Event event = myEvents.get(position);

        holder.txtMyEventName.setText(event.getName());
        holder.txtMyOrganizer.setText(event.getOrganizer());
        holder.txtMyCategory.setText(event.getCategory());
        holder.txtMyDate.setText(event.getDate());

        if (event.isUserRegistered()) {
            holder.btnWriteReview.setVisibility(View.VISIBLE);
        } else {
            holder.btnWriteReview.setVisibility(View.GONE);
        }

        holder.btnSetReminder.setOnClickListener(v -> {
            String reminderDate = getSelectedDate(holder.datePickerReminder);
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            if (dbHelper.updateReminderDate(event.getId(), reminderDate)) {
                event.setReminderDate(reminderDate);
                MyEventsSync.saveToFirestore(event, reminderDate);
                Toast.makeText(context, context.getString(R.string.success_reminder_set, reminderDate), Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnRemoveMyEvent.setOnClickListener(v -> {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            if (dbHelper.removeFromMyEvents(event.getId())) {
                MyEventsSync.removeFromFirestore(event);
                myEvents.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, myEvents.size());
                Toast.makeText(context, R.string.success_removed_my_event, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, R.string.error_remove_my_event, Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnWriteReview.setOnClickListener(v -> {
            if (event.getFirestoreId() == null || !event.isUserRegistered()) {
                Toast.makeText(context, R.string.error_review_not_registered, Toast.LENGTH_SHORT).show();
                return;
            }
            WriteReviewFragment fragment = WriteReviewFragment.newInstance(
                    event.getFirestoreId(), event.getName(), event.isUserRegistered());
            fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "WriteReviewFragment");
        });

        holder.btnShowSummary.setOnClickListener(v -> {
            String reminderDate = getSelectedDate(holder.datePickerReminder);
            EventSummaryFragment fragment = EventSummaryFragment.newInstance(
                    event.getName(),
                    event.getOrganizer(),
                    event.getCategory(),
                    event.getDate(),
                    reminderDate,
                    event.isUserRegistered()
            );
            fragment.show(((FragmentActivity) context).getSupportFragmentManager(), "EventSummaryFragment");
        });
    }

    @Override
    public int getItemCount() {
        return myEvents.size();
    }

    private String getSelectedDate(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();
        return day + "." + month + "." + year;
    }

    public static class MyEventViewHolder extends RecyclerView.ViewHolder {

        TextView txtMyEventName, txtMyOrganizer, txtMyCategory, txtMyDate;
        DatePicker datePickerReminder;
        Button btnSetReminder, btnRemoveMyEvent, btnWriteReview, btnShowSummary;

        public MyEventViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMyEventName = itemView.findViewById(R.id.txtMyEventName);
            txtMyOrganizer = itemView.findViewById(R.id.txtMyOrganizer);
            txtMyCategory = itemView.findViewById(R.id.txtMyCategory);
            txtMyDate = itemView.findViewById(R.id.txtMyDate);
            datePickerReminder = itemView.findViewById(R.id.datePickerReminder);
            btnSetReminder = itemView.findViewById(R.id.btnSetReminder);
            btnRemoveMyEvent = itemView.findViewById(R.id.btnRemoveMyEvent);
            btnWriteReview = itemView.findViewById(R.id.btnWriteReview);
            btnShowSummary = itemView.findViewById(R.id.btnShowSummary);
        }
    }
}
