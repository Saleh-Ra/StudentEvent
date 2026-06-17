package com.example.studentevent.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentevent.data.DatabaseHelper;
import com.example.studentevent.R;
import com.example.studentevent.model.Event;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private ArrayList<Event> events;

    /**
     * Purpose: Creates adapter for RecyclerView.
     * Input: Context and list of events.
     * Output: Adapter ready to display events.
     */
    public EventAdapter(Context context, ArrayList<Event> events) {
        this.context = context;
        this.events = events;
    }

    /**
     * Purpose: Creates the CardView layout for each event.
     * Input: Parent ViewGroup and viewType.
     * Output: EventViewHolder containing item_event layout.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Purpose: Puts event data inside the CardView.
     * Input: ViewHolder and event position.
     * Output: Event details are displayed on screen.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.txtEventName.setText(event.getName());
        holder.txtOrganizerName.setText(event.getOrganizer());
        holder.txtCategory.setText(event.getCategory());
        holder.txtDate.setText(event.getDate());
        holder.imgEvent.setImageResource(event.getImageResource());

        holder.btnRegister.setOnClickListener(v -> {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);

            boolean success = databaseHelper.addToMyEvents(event.getId());

            if (success) {
                Toast.makeText(context, "האירוע נוסף לאירועים שלי", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "האירוע כבר קיים ברשימה שלי", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnShare.setOnClickListener(v -> {
            shareEvent(event);
        });

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof ManageEventsActivity) {
                ((ManageEventsActivity) context).selectEventForEdit(event);
            }
        });
    }

    /**
     * Purpose: Returns number of events in the list.
     * Input: None.
     * Output: Number of events.
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Purpose: Shares event details using implicit Intent.
     * Input: Event object.
     * Output: Opens share menu.
     */
    private void shareEvent(Event event) {
        String shareText = "שם האירוע: " + event.getName()
                + "\nמארגן: " + event.getOrganizer()
                + "\nקטגוריה: " + event.getCategory()
                + "\nתאריך: " + event.getDate();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);

        context.startActivity(Intent.createChooser(intent, "שתף אירוע"));
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        ImageView imgEvent;
        TextView txtEventName, txtOrganizerName, txtCategory, txtDate;
        Button btnRegister;
        ImageButton btnShare;

        /**
         * Purpose: Connects CardView XML components to Java variables.
         * Input: itemView from item_event.xml.
         * Output: All item views are ready to use.
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            imgEvent = itemView.findViewById(R.id.imgEvent);
            txtEventName = itemView.findViewById(R.id.txtEventName);
            txtOrganizerName = itemView.findViewById(R.id.txtOrganizerName);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtDate = itemView.findViewById(R.id.txtDate);
            btnRegister = itemView.findViewById(R.id.btnRegister);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}