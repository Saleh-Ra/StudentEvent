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
import com.example.studentevent.data.MyEventsSync;
import com.example.studentevent.R;
import com.example.studentevent.model.Event;

import java.util.ArrayList;

/**
 * Purpose: Adapter for displaying events in a RecyclerView using CardView.
 * Input: List of events and context.
 * Output: Binds event data to UI components.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private ArrayList<Event> events;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(Context context, ArrayList<Event> events) {
        this.context = context;
        this.events = events;
    }

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.txtEventName.setText(event.getName());
        holder.txtOrganizerName.setText(event.getOrganizer());
        holder.txtCategory.setText(event.getCategory());
        holder.txtDate.setText(event.getDate());
        holder.txtRegisteredCount.setText(context.getString(R.string.registered_users_count, event.getRegisteredCount()));
        holder.imgEvent.setImageResource(event.getImageResource());

        holder.btnRegister.setOnClickListener(v -> {
            DatabaseHelper db = new DatabaseHelper(context);
            if (db.addToMyEvents(event.getId())) {
                MyEventsSync.saveToFirestore(event, "");
                Toast.makeText(context, R.string.success_added_to_my, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, R.string.error_already_in_my, Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnShare.setOnClickListener(v -> shareEvent(event));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            } else if (context instanceof ManageEventsActivity) {
                ((ManageEventsActivity) context).selectEventForEdit(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    private void shareEvent(Event event) {
        String shareText = context.getString(R.string.share_event_text,
                event.getName(), event.getOrganizer(), event.getCategory(), event.getDate());

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_event_chooser)));
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView imgEvent;
        TextView txtEventName, txtOrganizerName, txtCategory, txtDate, txtRegisteredCount;
        Button btnRegister;
        ImageButton btnShare;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            imgEvent = itemView.findViewById(R.id.imgEvent);
            txtEventName = itemView.findViewById(R.id.txtEventName);
            txtOrganizerName = itemView.findViewById(R.id.txtOrganizerName);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtRegisteredCount = itemView.findViewById(R.id.txtRegisteredCount);
            btnRegister = itemView.findViewById(R.id.btnRegister);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}