package com.abborg.glom.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abborg.glom.ApplicationState;
import com.abborg.glom.R;
import com.abborg.glom.interfaces.BoardItemClickListener;
import com.abborg.glom.interfaces.MultiSelectionListener;
import com.abborg.glom.model.BoardItem;
import com.abborg.glom.model.DrawItem;
import com.abborg.glom.model.EventItem;
import com.abborg.glom.model.FeedAction;
import com.abborg.glom.model.FileItem;
import com.abborg.glom.model.LinkItem;
import com.abborg.glom.utils.CircleTransform;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Handles the view logic to display items in a RecyclerView. The adapter can support
 * showing items in two layouts: the traditional linear layout and in a staggered grid.
 */
public class BoardRecyclerViewAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements MultiSelectionListener {

    private static String TAG = "BoardRecyclerViewAdapter";

    private Context context;

    private List<BoardItem> items;
    private List<Integer> staleItems;
    private SparseBooleanArray selectedItems;

    private BoardItemClickListener listener;

    /**************************************************
     * VIEW HOLDERS
     **************************************************/

    public static class EventHolder extends RecyclerView.ViewHolder {
        Button actionButton1;

        ImageView posterAvatar;
        TextView posterName;
        TextView postTime;
        ImageView syncStatus;

        TextView eventName;
        TextView eventVenue;
        TextView eventNote;

        ImageView googleLogo;

        CardView card;

        public EventHolder(View itemView) {
            super(itemView);

            actionButton1 = (Button) itemView.findViewById(R.id.card_action_1);

            posterAvatar = (ImageView) itemView.findViewById(R.id.card_user_avatar);
            posterName = (TextView) itemView.findViewById(R.id.card_user_name);
            postTime = (TextView) itemView.findViewById(R.id.card_user_post_time);
            syncStatus = (ImageView) itemView.findViewById(R.id.card_sync_status);

            eventName = (TextView) itemView.findViewById(R.id.card_event_name);
            eventVenue = (TextView) itemView.findViewById(R.id.card_event_venue);
            eventNote = (TextView) itemView.findViewById(R.id.card_event_note);

            googleLogo = (ImageView) itemView.findViewById(R.id.card_powered_by_google);

            card = (CardView) itemView.findViewById(R.id.card_view);
        }

        public void bind(final EventItem item, final BoardItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClicked(item, getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return listener.onItemLongClicked(item, getAdapterPosition());
                }
            });
        }
    }

    public static class FileHolder extends RecyclerView.ViewHolder {
        Button actionButton1;
        Button actionButton2;

        ImageView posterAvatar;
        TextView posterName;
        TextView postTime;
        ImageView syncStatus;

        TextView fileName;
        TextView fileNote;
        ImageView fileThumbnail;

        ProgressBar progressBar;

        CardView card;

        public FileHolder(View itemView) {
            super(itemView);

            actionButton1 = (Button) itemView.findViewById(R.id.card_action_1);
            actionButton2 = (Button) itemView.findViewById(R.id.card_action_2);

            posterAvatar = (ImageView) itemView.findViewById(R.id.card_user_avatar);
            posterName = (TextView) itemView.findViewById(R.id.card_user_name);
            postTime = (TextView) itemView.findViewById(R.id.card_user_post_time);
            syncStatus = (ImageView) itemView.findViewById(R.id.card_sync_status);

            fileName = (TextView) itemView.findViewById(R.id.file_name);
            fileNote = (TextView) itemView.findViewById(R.id.file_note);
            fileThumbnail = (ImageView) itemView.findViewById(R.id.file_thumbnail);

            progressBar = (ProgressBar) itemView.findViewById(R.id.file_progress);

            card = (CardView) itemView.findViewById(R.id.card_view);
        }

        public void bind(final FileItem item, final BoardItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClicked(item, getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return listener.onItemLongClicked(item, getAdapterPosition());
                }
            });
        }
    }

    public static class DrawingHolder extends RecyclerView.ViewHolder {
        ImageView posterAvatar;
        TextView posterName;
        TextView postTime;
        ImageView syncStatus;

        ImageView thumbnail;

        CardView card;

        public DrawingHolder(View itemView) {
            super(itemView);

            posterAvatar = (ImageView) itemView.findViewById(R.id.card_user_avatar);
            posterName = (TextView) itemView.findViewById(R.id.card_user_name);
            postTime = (TextView) itemView.findViewById(R.id.card_user_post_time);
            syncStatus = (ImageView) itemView.findViewById(R.id.card_sync_status);

            thumbnail = (ImageView) itemView.findViewById(R.id.note_thumbnail);

            card = (CardView) itemView.findViewById(R.id.card_view);
        }

        public void bind(final DrawItem item, final BoardItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClicked(item, getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return listener.onItemLongClicked(item, getAdapterPosition());
                }
            });
        }
    }

    public static class LinkHolder extends RecyclerView.ViewHolder {
        ImageView posterAvatar;
        TextView posterName;
        TextView postTime;
        ImageView syncStatus;

        TextView url;
        ImageView thumbnail;
        TextView title;
        TextView description;

        CardView card;

        public LinkHolder(View itemView) {
            super(itemView);

            posterAvatar = (ImageView) itemView.findViewById(R.id.card_user_avatar);
            posterName = (TextView) itemView.findViewById(R.id.card_user_name);
            postTime = (TextView) itemView.findViewById(R.id.card_user_post_time);
            syncStatus = (ImageView) itemView.findViewById(R.id.card_sync_status);

            url = (TextView) itemView.findViewById(R.id.link_url);
            thumbnail = (ImageView) itemView.findViewById(R.id.link_thumbnail);
            title = (TextView) itemView.findViewById(R.id.link_title);
            description = (TextView) itemView.findViewById(R.id.link_description);

            card = (CardView) itemView.findViewById(R.id.card_view);
        }

        public void bind(final LinkItem item, final BoardItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClicked(item, getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return listener.onItemLongClicked(item, getAdapterPosition());
                }
            });
        }
    }

    /**************************************************
     * VIEW CALLBACKS
     **************************************************/

    public BoardRecyclerViewAdapter(Context ctx, List<BoardItem> models, BoardItemClickListener clickListener) {
        context = ctx;
        items = models;
        listener = clickListener;
        staleItems = new ArrayList<>();
        selectedItems = new SparseBooleanArray();
        setHasStableIds(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == BoardItem.TYPE_EVENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_event, parent, false);
            return new EventHolder(view);
        }
        else if (viewType == BoardItem.TYPE_FILE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_file, parent, false);
            return new FileHolder(view);
        }
        else if (viewType == BoardItem.TYPE_DRAWING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_drawing, parent, false);
            return new DrawingHolder(view);
        }
        else if (viewType == BoardItem.TYPE_LINK) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_link, parent, false);
            return new LinkHolder(view);
        }
        else return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder recyclerViewHolder, int position) {
        if (recyclerViewHolder instanceof EventHolder)
            setEventViewHolder(position, recyclerViewHolder);
        else if (recyclerViewHolder instanceof FileHolder)
            setFileViewHolder(position, recyclerViewHolder);
        else if (recyclerViewHolder instanceof DrawingHolder)
            setDrawingViewHolder(position, recyclerViewHolder);
        else if (recyclerViewHolder instanceof LinkHolder)
            setLinkViewHolder(position, recyclerViewHolder);
    }

    @Override
    /**
     * When notifyDataSetChanged() is called, getItemId will depend upon the values of what is
     * combined to be the hashcode
     */
    public long getItemId(int position) {
        if (items != null && !items.isEmpty()) {
            BoardItem item = items.get(position);
            long id = RecyclerView.NO_ID;
            if (item.getType() == BoardItem.TYPE_EVENT) {
                EventItem event = (EventItem) item;
                String name = TextUtils.isEmpty(event.getName()) ? "" : event.getName();
                long startTime = event.getStartTime() == null ? 0L : event.getStartTime().getMillis();
                long endTime = event.getEndTime() == null ? 0l : event.getEndTime().getMillis();
                String place = TextUtils.isEmpty(event.getPlace()) ? "" : event.getPlace();
                double lat = event.getLocation() == null ? 0 : event.getLocation().getLatitude();
                double lng = event.getLocation() == null ? 0 : event.getLocation().getLongitude();
                String note = TextUtils.isEmpty(event.getNote()) ? "" : event.getNote();
                id = (event.getId() + event.getType() + event.getUpdatedTime() + name + startTime + endTime +
                        place + lat + lng + note + event.getSyncStatus()).hashCode();
            }
            else if (item.getType() == BoardItem.TYPE_FILE) {
                FileItem file = (FileItem) item;
                String name = TextUtils.isEmpty(file.getName()) ? "" : file.getName();
                String note = TextUtils.isEmpty(file.getNote()) ? "" : file.getNote();
                String mimetype = TextUtils.isEmpty(file.getMimetype()) ? "" : file.getMimetype();
                long size = file.getSize();
                String path = file.getLocalCache()==null? "" : file.getLocalCache().getPath();
                long created = file.getCreatedTime() == null ? 0L : file.getCreatedTime().getMillis();
                long updated = file.getUpdatedTime() == null ? 0L : file.getUpdatedTime().getMillis();
                id = (name + note + mimetype + size + path + created + updated + + file.getSyncStatus()).hashCode();
            }
            else if (item.getType() == BoardItem.TYPE_DRAWING) {
                DrawItem note = (DrawItem) item;
                String name = TextUtils.isEmpty(note.getName()) ? "" : note.getName();
                long created = note.getCreatedTime() == null ? 0L : note.getCreatedTime().getMillis();
                long updated = note.getUpdatedTime() == null ? 0L : note.getUpdatedTime().getMillis();
                id = (name + created + updated + note.getSyncStatus()).hashCode();
            }
            else if (item.getType() == BoardItem.TYPE_LINK) {
                LinkItem link = (LinkItem) item;
                String url = TextUtils.isEmpty(link.getUrl()) ? "" : link.getUrl();
                String title = TextUtils.isEmpty(link.getTitle()) ? "" : link.getTitle();
                String thumbnail = TextUtils.isEmpty(link.getThumbnail()) ? "" : link.getThumbnail();
                String description = TextUtils.isEmpty(link.getDescription()) ? "" : link.getDescription();
                id = (link.getId() + url + title + thumbnail + description).hashCode();
            }
            return id;
        }

        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**************************************************
     * MODEL UPDATES
     **************************************************/

    public void addItem(String id) {
        notifyDataSetChanged();
    }

    public void updateItem(String id) {
        notifyDataSetChanged();
    }

    public void deleteItem(String id) {
        notifyDataSetChanged();
    }

    public void update(List<BoardItem> items) {
        // update from specific list of items
        if (items != null) {
            this.items = items;
            notifyDataSetChanged();
        }
        else {
            for (int position : staleItems) {
                notifyItemChanged(position);
            }
            staleItems.clear();
        }
    }

    /**************************************************
     * UI HELPERS
     **************************************************/

    private void attachPostInfo(FeedAction feedAction, TextView posterName, ImageView posterAvatar, TextView postTime) {
        if (feedAction != null) {
            if (feedAction.user != null) {

                // set update text
                switch(feedAction.type) {
                    case FeedAction.CREATE_EVENT:
                        posterName.setText(Html.fromHtml("<b>" + feedAction.user.getName() + "</b> " +
                                context.getResources().getString(R.string.card_post_info)));
                        break;
                    case FeedAction.CANCEL_EVENT:
                        posterName.setText(Html.fromHtml("<b>" + feedAction.user.getName() + "</b> " +
                                context.getResources().getString(R.string.card_cancel_info)));
                        break;
                    case FeedAction.UPDATE_EVENT:
                        posterName.setText(Html.fromHtml("<b>" + feedAction.user.getName() + "</b> " +
                                context.getResources().getString(R.string.card_edit_info)));
                        break;
                    default:
                        posterName.setText(feedAction.user.getName());
                }

                // load avatar
                Glide.with(context)
                        .load(feedAction.user.getAvatar()).fitCenter()
                        .transform(new CircleTransform(context))
                        .override(context.getResources().getDimensionPixelSize(R.dimen.card_avatar_size),
                                context.getResources().getDimensionPixelSize(R.dimen.card_avatar_size))
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .crossFade(1000)
                        .into(posterAvatar);
            }
            if (feedAction.dateTime != null) {
                DateTime now = new DateTime();
                Duration duration = new Duration(feedAction.dateTime, now);
                String displayTime;
                if (duration.getStandardMinutes() < 5)
                    displayTime = context.getResources().getString(R.string.time_info_just_now);
                else if (duration.getStandardMinutes() < 60)
                    displayTime = duration.getStandardMinutes() + " " + context.getResources().getString(R.string.time_unit_minute);
                else if (duration.getStandardHours() < 24)
                    displayTime = duration.getStandardHours() + " " + context.getResources().getString(R.string.time_unit_hour);
                else
                    displayTime = duration.getStandardDays() + " " + context.getResources().getString(R.string.time_unit_day);

                postTime.setText(displayTime);
            }
        }
    }

    private void attachSelectionOverlay(int position, CardView card) {
        card.setCardBackgroundColor(selectedItems.get(position) ? ContextCompat.getColor(context, R.color.selectItemOverlay) :
                Color.WHITE);
    }

    private void setFileViewHolder(int position, RecyclerView.ViewHolder recyclerViewHolder) {
        FileItem file = (FileItem) items.get(position);
        final String id = file.getId();
        final FileHolder holder = (FileHolder) recyclerViewHolder;

        holder.bind(file, listener);

        // attach the last feed info about this post
        attachPostInfo(file.getLastAction(), holder.posterName, holder.posterAvatar, holder.postTime);

        // set activation change
        attachSelectionOverlay(position, holder.card);

        // set action buttons
        if (file.getLocalCache() == null || !file.getLocalCache().exists()) {
            holder.actionButton1.setText(context.getResources().getString(R.string.card_action_download));
            holder.actionButton2.setText(context.getResources().getString(R.string.card_action_share));
            holder.actionButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.itemView.performClick();
                }
            });
        }
        else {
            holder.actionButton1.setText(context.getResources().getString(R.string.card_action_view));
            holder.actionButton2.setText(context.getResources().getString(R.string.card_action_share));
            holder.actionButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.itemView.performClick();
                }
            });
        }

        // set sync status and progress bar
        if (file.getSyncStatus() == BoardItem.SYNC_COMPLETE) {
            holder.syncStatus.setVisibility(View.INVISIBLE);
            holder.progressBar.setVisibility(View.GONE);
        }
        else {
            holder.syncStatus.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.VISIBLE);

            if (file.getSyncStatus() == BoardItem.SYNC_ERROR)
                holder.syncStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync_failed));
            else if (file.getSyncStatus() == BoardItem.SYNC_IN_PROGRESS) {
                holder.syncStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync));
                if (file.getProgress() > 0) {
                    holder.progressBar.setIndeterminate(false);
                    holder.progressBar.setProgress(file.getProgress());
                }
                else {
                    holder.progressBar.setIndeterminate(true);
                }
            }
        }

        // update file info and thumbnail
        String name = !TextUtils.isEmpty(file.getName()) ? file.getName()
                : context.getResources().getString(R.string.file_name_placeholder);
        holder.fileName.setText(name);
        String note = !TextUtils.isEmpty(file.getName()) ? file.getNote()
                : "";
        holder.fileNote.setText(note);

        // set up image icons
        int icon;
        if (file.isImage() && file.getLocalCache() != null && file.getLocalCache().exists()) {
            icon = R.drawable.ic_placeholder_image;
            if (file.isGif()) {
                Glide.with(context)
                        .load(file.getLocalCache()).asGif().centerCrop()
                        .signature(new StringSignature(String.valueOf(file.getLocalCache().lastModified())))
                        .placeholder(icon)
                        .error(icon)
                        .crossFade(1000)
                        .into(holder.fileThumbnail);
            }
            else {
                Glide.with(context)
                        .load(file.getLocalCache()).centerCrop()
                        .signature(new StringSignature(String.valueOf(file.getLocalCache().lastModified())))
                        .placeholder(icon)
                        .error(icon)
                        .crossFade(1000)
                        .into(holder.fileThumbnail);
            }
        }
        else {
            icon = file.isImage() ? R.drawable.ic_placeholder_image : R.drawable.ic_placeholder_file;
            Glide.with(context)
                    .load(icon).centerCrop()
                    .crossFade(1000)
                    .into(holder.fileThumbnail);
        }
    }

    private void setEventViewHolder(int position, RecyclerView.ViewHolder recyclerViewHolder) {
        EventItem event = (EventItem) items.get(position);
        final String id = event.getId();
        EventHolder holder = (EventHolder) recyclerViewHolder;

        holder.bind(event, listener);

        // attach the last feed info about this post
        attachPostInfo(event.getLastAction(), holder.posterName, holder.posterAvatar, holder.postTime);

        // set activation change
        attachSelectionOverlay(position, holder.card);

        // if the hosts contains the user, set action and text accordingly (Edit, Share)
        // if the hosts doesn't contain the user, set action to (Attend, Miss)
        if (event.getPlace() != null || event.getLocation() != null) {
            holder.actionButton1.setVisibility(View.VISIBLE);
            holder.actionButton1.setText(context.getResources().getString(R.string.card_action_get_directions));

            final String placeId = event.getPlace();
            if (!TextUtils.isEmpty(placeId)) {
                holder.actionButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GoogleApiClient apiClient = ApplicationState.getInstance().getGoogleApiClient();
                        if (apiClient != null && apiClient.isConnected()) {
                            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(apiClient, placeId);
                            placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {

                                @Override
                                public void onResult(PlaceBuffer places) {
                                    if (!places.getStatus().isSuccess()) {
                                        Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                                        places.release();
                                        return;
                                    }

                                    final Place place = places.get(0);
                                    Log.d(TAG, "Place query succeeded for " + place.getName());
                                    double lat = place.getLatLng().latitude;
                                    double lng = place.getLatLng().longitude;
                                    places.release();

                                    launchGoogleMapsNavigation(lat, lng);
                                }
                            });
                        }
                    }
                });
            }
            else if (event.getLocation() != null) {
                final double lat = event.getLocation().getLatitude();
                final double lng = event.getLocation().getLongitude();

                holder.actionButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchGoogleMapsNavigation(lat, lng);
                    }
                });
            }
        }
        else {
            holder.actionButton1.setVisibility(View.GONE);
        }

        // set sync status
        if (event.getSyncStatus() == BoardItem.SYNC_COMPLETE)
            holder.syncStatus.setVisibility(View.INVISIBLE);
        else {
            holder.syncStatus.setVisibility(View.VISIBLE);

            if (event.getSyncStatus() == BoardItem.SYNC_ERROR)
                holder.syncStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync_failed));
            else if (event.getSyncStatus() == BoardItem.SYNC_IN_PROGRESS)
                holder.syncStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync));
        }

        // update event info
        // always show time if available
        // if place is specified, show place, otherwise show coordinates
        holder.eventName.setText(event.getName());
        String time = "";
        String duration = "";
        if (event.getStartTime() != null) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern(context.getResources().getString(R.string.card_event_datetime_format));
            DateTimeFormatter timeFormatter = DateTimeFormat.forPattern(context.getResources().getString(R.string.card_event_time_format));
            DateTime now = new DateTime();
            Period period = new Period(now, event.getStartTime());

            int years = period.getYears() * -1;
            int months = period.getMonths() * -1;
            int weeks = period.getWeeks() * -1;
            int hours = period.getHours() * -1;
            int days = period.getDays() * -1;
            int minutes = period.getMinutes() * -1;

            // positive periods
            if (period.getYears() >= 1)
                duration = period.getYears() + " " + context.getResources().getString(R.string.time_unit_year) + " " +
                        context.getResources().getString(R.string.time_unit_and_seperator) + " " +
                        period.getMonths() + " " + context.getResources().getString(R.string.time_unit_month);
            else if (period.getMonths() >= 1)
                duration = period.getMonths() + " " + context.getResources().getString(R.string.time_unit_month) + " " +
                        context.getResources().getString(R.string.time_unit_and_seperator) + " " +
                        period.getDays() + " " + context.getResources().getString(R.string.time_unit_day);
            else if (period.getWeeks() >= 1)
                duration = period.getWeeks() + " " + context.getResources().getString(R.string.time_unit_week) + " " +
                        context.getResources().getString(R.string.time_unit_and_seperator) + " " +
                        period.getDays() + " " + context.getResources().getString(R.string.time_unit_day);
            else if (period.getDays() >= 1)
                duration = period.getDays() + " " + context.getResources().getString(R.string.time_unit_day) + " " +
                        context.getResources().getString(R.string.time_unit_and_seperator) + " " +
                        period.getHours() + " " + context.getResources().getString(R.string.time_unit_hour);
            else if (period.getHours() >= 1)
                duration = period.getHours() + " " + context.getResources().getString(R.string.time_unit_hour) + " " +
                        context.getResources().getString(R.string.time_unit_and_seperator) + " " +
                        period.getMinutes() + " " + context.getResources().getString(R.string.time_unit_minute);
            else if (period.getMinutes() >= 0)
                duration = period.getMinutes() + " " + context.getResources().getString(R.string.time_unit_minute);

                // negative periods (already passed)
            else if (period.getYears() <= -1) {
                duration = years + " " + context.getResources().getString(R.string.time_unit_year) + " " +
                        context.getResources().getString(R.string.time_unit_and_seperator) + " " +
                        months + " " + context.getResources().getString(R.string.time_unit_month) + " " +
                        context.getResources().getString(R.string.time_suffix_ago);
            }
            else if (period.getMonths() <= -1) {
                duration = months + " " + context.getResources().getString(R.string.time_unit_month) + " " +
                        context.getResources().getString(R.string.time_unit_and_seperator) + " " +
                        days + " " + context.getResources().getString(R.string.time_unit_day) + " " +
                        context.getResources().getString(R.string.time_suffix_ago);
            }
            else if (period.getWeeks() <= -1) {
                duration = weeks + " " + context.getResources().getString(R.string.time_unit_week) + " " +
                        context.getResources().getString(R.string.time_unit_and_seperator) + " " +
                        days + " " + context.getResources().getString(R.string.time_unit_day) + " " +
                        context.getResources().getString(R.string.time_suffix_ago);
            }
            else if (period.getDays() <= -1) {
                duration = days + " " + context.getResources().getString(R.string.time_unit_day) + " " +
                        context.getResources().getString(R.string.time_unit_and_seperator) + " " +
                        hours + " " + context.getResources().getString(R.string.time_unit_hour) + " " +
                        context.getResources().getString(R.string.time_suffix_ago);
            }
            else if (period.getHours() <= -1) {
                duration = hours + " " + context.getResources().getString(R.string.time_unit_hour) + " " +
                        context.getResources().getString(R.string.time_unit_and_seperator) + " " +
                        minutes + " " + context.getResources().getString(R.string.time_unit_minute) + " " +
                        context.getResources().getString(R.string.time_suffix_ago);
            }
            else {
                duration = minutes + " " + context.getResources().getString(R.string.time_unit_minute) + " " +
                        context.getResources().getString(R.string.time_suffix_ago);
            }

            String formattedStartDateTime = "";
            String formattedEndDateTime = "";
            int daysBetween = Days.daysBetween(now.withTimeAtStartOfDay(), event.getStartTime().withTimeAtStartOfDay()).getDays();
            DateTimeFormatter dayWithTimeFormatter = DateTimeFormat.forPattern("EEEE, " +
                    context.getResources().getString(R.string.card_event_time_format));
            if (daysBetween == -1) {
                formattedStartDateTime = context.getResources().getString(R.string.time_yesterday)
                        + ", " + timeFormatter.print(event.getStartTime());
            }
            else if (daysBetween == 0) {
                formattedStartDateTime = context.getResources().getString(R.string.time_today)
                        + ", " + timeFormatter.print(event.getStartTime());
            }
            else if (daysBetween == 1) {
                formattedStartDateTime = context.getResources().getString(R.string.time_tomorrow)
                        + ", " + timeFormatter.print(event.getStartTime());
            }
            else if (daysBetween < 7 && daysBetween > 0) {
                formattedStartDateTime = dayWithTimeFormatter.print(event.getStartTime());
            }
            else {
                formattedStartDateTime = formatter.print(event.getStartTime());
            }

            if (event.getEndTime() != null) {
                daysBetween = Days.daysBetween(now.withTimeAtStartOfDay(), event.getEndTime().withTimeAtStartOfDay()).getDays();
                int daysDuration = Days.daysBetween(event.getStartTime().withTimeAtStartOfDay(),
                        event.getEndTime().withTimeAtStartOfDay()).getDays();
                if (daysDuration == 0) {
                    formattedEndDateTime = " - " + timeFormatter.print(event.getEndTime()) + " ";
                }
                else {
                    if (daysBetween == -1) {
                        formattedEndDateTime = " - " + context.getResources().getString(R.string.time_yesterday)
                                + ", " + timeFormatter.print(event.getEndTime()) + " ";
                    }
                    else if (daysBetween == 0) {
                        formattedEndDateTime = " - " + context.getResources().getString(R.string.time_today)
                                + ", " + timeFormatter.print(event.getEndTime()) + " ";
                    }
                    else if (daysBetween == 1) {
                        formattedEndDateTime = " - " + context.getResources().getString(R.string.time_tomorrow)
                                + ", " + timeFormatter.print(event.getEndTime()) + " ";
                    }
                    else if (daysBetween < 7 && daysBetween > 0) {
                        formattedEndDateTime = " - " + dayWithTimeFormatter.print(event.getEndTime()) + " ";
                    }
                    else {
                        formattedEndDateTime = " - " + formatter.print(event.getEndTime()) + " ";
                    }
                }
            }

            String durationPrefix = period.getMillis() > 0 ? context.getResources().getString(R.string.duration_incoming_prefix) :
                    context.getResources().getString(R.string.duration_started_prefix);
            time = formattedStartDateTime + formattedEndDateTime + " (" + durationPrefix + " " + duration + ")\n";
        }

        if (!TextUtils.isEmpty(event.getPlace())) {

            // retrieve place from its ID
            final EventHolder viewHolder = holder;
            final String placeTime = time;
            String placeName = event.getLocation()!=null ?
                    event.getLocation().getLatitude() + ", " + event.getLocation().getLongitude() :
                    context.getResources().getString(R.string.notify_retrieving_place_info);
            GoogleApiClient apiClient = ApplicationState.getInstance().getGoogleApiClient();
            if (apiClient != null && apiClient.isConnected()) {
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(apiClient, event.getPlace());
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {

                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (!places.getStatus().isSuccess()) {
                            Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                            places.release();
                            return;
                        }

                        // display the first place in the list
                        final Place place = places.get(0);
                        viewHolder.eventVenue.setText(placeTime + Html.fromHtml(place.getName() + ""));
                        Log.d(TAG, "Place query succeeded for " + place.getName());

                        int updatedEventIndex = staleItems.indexOf(viewHolder.getAdapterPosition());
                        if (updatedEventIndex >= 0 && updatedEventIndex < staleItems.size())
                            staleItems.remove(updatedEventIndex);

                        places.release();
                    }
                });
            }
            else {
                Log.e(TAG, "Google API client is not connected, error retrieving place info");
            }

            holder.eventVenue.setText(time + placeName);
            holder.googleLogo.setVisibility(View.VISIBLE);

            // add to list of items that contain places info to be refreshed
            staleItems.add(holder.getAdapterPosition());
        }
        else {
            if (event.getLocation() != null) {
                holder.eventVenue.setText(time + event.getLocation().getLatitude() + ", " +
                        event.getLocation().getLongitude());
            }
            else {
                holder.eventVenue.setText(time);
            }

            holder.googleLogo.setVisibility(View.INVISIBLE);
        }

        holder.eventNote.setText(event.getNote());

        if (TextUtils.isEmpty(holder.eventVenue.getText())) holder.eventVenue.setVisibility(View.GONE);
        else holder.eventVenue.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(event.getNote())) holder.eventNote.setVisibility(View.GONE);
        else holder.eventNote.setVisibility(View.VISIBLE);
    }

    private void setDrawingViewHolder(int position, RecyclerView.ViewHolder recyclerViewHolder) {
        DrawItem drawing = (DrawItem) items.get(position);
        final String id = drawing.getId();
        final DrawingHolder holder = (DrawingHolder) recyclerViewHolder;

        holder.bind(drawing, listener);

        // attach the last feed info about this post
        attachPostInfo(drawing.getLastAction(), holder.posterName, holder.posterAvatar, holder.postTime);

        // set activation change
        attachSelectionOverlay(position, holder.card);

        // set sync status and progress bar
        if (drawing.getSyncStatus() == BoardItem.SYNC_COMPLETE) {
            holder.syncStatus.setVisibility(View.INVISIBLE);
        }
        else {
            holder.syncStatus.setVisibility(View.VISIBLE);

            if (drawing.getSyncStatus() == BoardItem.SYNC_ERROR)
                holder.syncStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync_failed));
            else if (drawing.getSyncStatus() == BoardItem.SYNC_IN_PROGRESS) {
                holder.syncStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync));
            }
        }

        // update drawing thumbnail
        File file = drawing.getLocalCache();
        if (file != null && file.exists()) {
            Glide.with(context)
                    .load(file).centerCrop()
                    .crossFade(1000)
                    .signature(new StringSignature(String.valueOf(file.lastModified())))
                    .placeholder(R.drawable.ic_placeholder_drawing)
                    .error(R.drawable.ic_placeholder_drawing)
                    .into(holder.thumbnail);
        }
    }

    private void setLinkViewHolder(int position, RecyclerView.ViewHolder recyclerViewHolder) {
        LinkItem link = (LinkItem) items.get(position);
        final LinkHolder holder = (LinkHolder) recyclerViewHolder;

        holder.bind(link, listener);

        // attach the last feed info about this post
        attachPostInfo(link.getLastAction(), holder.posterName, holder.posterAvatar, holder.postTime);

        // set activation change
        attachSelectionOverlay(position, holder.card);

        // set sync status and progress bar
        if (link.getSyncStatus() == BoardItem.SYNC_COMPLETE) {
            holder.syncStatus.setVisibility(View.INVISIBLE);
        }
        else {
            holder.syncStatus.setVisibility(View.VISIBLE);

            if (link.getSyncStatus() == BoardItem.SYNC_ERROR)
                holder.syncStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync_failed));
            else if (link.getSyncStatus() == BoardItem.SYNC_IN_PROGRESS) {
                holder.syncStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sync));
            }
        }

        // update the info
        holder.url.setText(trimUrl(link.getUrl()));
        holder.title.setText(link.getTitle());
        holder.description.setText(link.getDescription());
        if (!TextUtils.isEmpty(link.getThumbnail())) {
            holder.thumbnail.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(link.getThumbnail()).centerCrop()
                    .crossFade(1000)
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(holder.thumbnail);
        }
        else {
            holder.thumbnail.setVisibility(View.GONE);
        }
    }

    private String trimUrl(String url) {
        if (TextUtils.isEmpty(url)) return null;

        return Uri.parse(url).getHost();
    }

    private void launchGoogleMapsNavigation(double lat, double lng) {
        Uri gmmIntentUri = Uri.parse(
                String.format(Locale.ENGLISH, "google.navigation:q=%1f,%2f", lat, lng));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        context.startActivity(mapIntent);
    }

    /**************************************************************
     * MULTIPLE ITEM SELECTION LISTENER
     **************************************************************/

    @Override
    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            Log.d(TAG, "Unselected item at " + pos);
            selectedItems.delete(pos);
        }
        else {
            Log.d(TAG, "Selected item at " + pos);
            selectedItems.put(pos, true);
        }
        Log.d(TAG, "Total selections is " + getSelectedItemCount());
        notifyItemChanged(pos);
    }

    @Override
    public void clearSelections() {
        Log.d(TAG, "Clear all item selections");
        selectedItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    @Override
    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }
}