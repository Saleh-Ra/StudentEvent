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
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentevent.data.DatabaseHelper;
import com.example.studentevent.R;
import com.example.studentevent.model.Event;

import java.util.ArrayList;

public class MyEventsAdapter extends RecyclerView.Adapter<MyEventsAdapter.MyEventViewHolder> {

    private Context context;
    private ArrayList<Event> myEvents;

    /**
     * Purpose: Creates adapter for user's events RecyclerView.
     * Input: Context and list of user's events.
     * Output: Adapter ready to display user's events.
     */
    public MyEventsAdapter(Context context, ArrayList<Event> myEvents) {
        this.context = context;
        this.myEvents = myEvents;
    }

    /**
     * Purpose: Creates CardView layout for user's event.
     * Input: Parent ViewGroup and view type.
     * Output: ViewHolder containing item_my_event layout.
     */
    @NonNull
    @Override
    public MyEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_event, parent, false);
        return new MyEventViewHolder(view);
    }

    /**
     * Purpose: Displays user's event data inside CardView.
     * Input: ViewHolder and position.
     * Output: Event details and buttons are displayed.
     */
    @Override
    public void onBindViewHolder(@NonNull MyEventViewHolder holder, int position) {
        Event event = myEvents.get(position);

        holder.txtMyEventName.setText(event.getName());
        holder.txtMyOrganizer.setText(event.getOrganizer());
        holder.txtMyCategory.setText(event.getCategory());
        holder.txtMyDate.setText(event.getDate());

        holder.btnSetReminder.setOnClickListener(v -> {
            int day = holder.datePickerReminder.getDayOfMonth();
            int month = holder.datePickerReminder.getMonth() + 1;
            int year = holder.datePickerReminder.getYear();

            Toast.makeText(context,
                    "התזכורת נקבעה לתאריך " + day + "." + month + "." + year,
                    Toast.LENGTH_SHORT).show();
        });

        holder.btnRemoveMyEvent.setOnClickListener(v -> {

            DatabaseHelper databaseHelper = new DatabaseHelper(context);

            boolean success = databaseHelper.removeFromMyEvents(event.getId());

            if (success) {
                myEvents.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, myEvents.size());

                Toast.makeText(context,
                        "האירוע הוסר מהרשימה שלי",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context,
                        "שגיאה במחיקת האירוע",
                        Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnShowSummary.setOnClickListener(v -> {
            int day = holder.datePickerReminder.getDayOfMonth();
            int month = holder.datePickerReminder.getMonth() + 1;
            int year = holder.datePickerReminder.getYear();

            String reminderDate = day + "." + month + "." + year;

            EventSummaryFragment fragment = EventSummaryFragment.newInstance(
                    event.getName(),
                    event.getOrganizer(),
                    event.getCategory(),
                    event.getDate(),
                    reminderDate
            );

            fragment.show(((MyEventsActivity) context).getSupportFragmentManager(), "EventSummaryFragment");
        });
    }

    /**
     * Purpose: Returns number of user's events.
     * Input: None.
     * Output: Number of events in the personal list.
     */
    @Override
    public int getItemCount() {
        return myEvents.size();
    }

    public static class MyEventViewHolder extends RecyclerView.ViewHolder {

        TextView txtMyEventName, txtMyOrganizer, txtMyCategory, txtMyDate;
        DatePicker datePickerReminder;
        Button btnSetReminder, btnRemoveMyEvent, btnShowSummary;

        /**
         * Purpose: Connects item_my_event XML components to Java variables.
         * Input: itemView from item_my_event.xml.
         * Output: All item components are ready to use.
         */
        public MyEventViewHolder(@NonNull View itemView) {
            super(itemView);

            txtMyEventName = itemView.findViewById(R.id.txtMyEventName);
            txtMyOrganizer = itemView.findViewById(R.id.txtMyOrganizer);
            txtMyCategory = itemView.findViewById(R.id.txtMyCategory);
            txtMyDate = itemView.findViewById(R.id.txtMyDate);
            datePickerReminder = itemView.findViewById(R.id.datePickerReminder);
            btnSetReminder = itemView.findViewById(R.id.btnSetReminder);
            btnRemoveMyEvent = itemView.findViewById(R.id.btnRemoveMyEvent);
            btnShowSummary = itemView.findViewById(R.id.btnShowSummary);
        }
    }
}