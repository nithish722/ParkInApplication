package com.example.parkin.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkin.R;
import com.example.parkin.database.DatabaseHelper;
import com.example.parkin.database.MockDatabaseStore;
import com.example.parkin.models.*;

import java.util.ArrayList;
import java.util.List;

public class AdminPortalActivity extends AppCompatActivity {

    private FrameLayout contentFrame;
    private TextView[] adminNavPills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_portal);

        contentFrame = findViewById(R.id.adminContentFrame);

        initNavPills();
        findViewById(R.id.btnAdminLogout).setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Default: Admin Dashboard
        selectTab(0);
    }

    private void initNavPills() {
        adminNavPills = new TextView[]{
                findViewById(R.id.adminNavDashboard),
                findViewById(R.id.adminNavEvents),
                findViewById(R.id.adminNavAreas),
                findViewById(R.id.adminNavSlots),
                findViewById(R.id.adminNavStatusMonitor),
                findViewById(R.id.adminNavBookings),
                findViewById(R.id.adminNavUsers),
                findViewById(R.id.adminNavAnnouncements),
                findViewById(R.id.adminNavReports)
        };

        for (int i = 0; i < adminNavPills.length; i++) {
            final int index = i;
            adminNavPills[i].setOnClickListener(v -> selectTab(index));
        }
    }

    private void selectTab(int index) {
        for (int i = 0; i < adminNavPills.length; i++) {
            if (i == index) {
                adminNavPills[i].setBackgroundResource(R.drawable.bg_nav_pill_active);
                adminNavPills[i].setTextColor(Color.WHITE);
            } else {
                adminNavPills[i].setBackgroundResource(R.drawable.bg_nav_pill_inactive);
                adminNavPills[i].setTextColor(Color.parseColor("#CBD5E1"));
            }
        }
        contentFrame.removeAllViews();

        switch (index) {
            case 0: loadAdminDashboard(); break;
            case 1: loadAdminEvents(); break;
            case 2: loadAdminAreas(); break;
            case 3: loadAdminSlots(); break;
            case 4: loadAdminStatusMonitor(); break;
            case 5: loadAdminBookings(); break;
            case 6: loadAdminUsers(); break;
            case 7: loadAdminAnnouncements(); break;
            case 8: loadAdminReports(); break;
        }
    }

    // --- 1. ADMIN DASHBOARD ---
    private void loadAdminDashboard() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_adm_dashboard, contentFrame, false);
        contentFrame.addView(v);

        v.findViewById(R.id.btnQuickAddSlot).setOnClickListener(view -> showAddSlotDialog());

        TextView tvUsersKpi = v.findViewById(R.id.tvAdminKpiUsers);
        if (tvUsersKpi != null) {
            int totalUsers = DatabaseHelper.getInstance(this).getAllUsers().size();
            tvUsersKpi.setText(String.valueOf(totalUsers));
        }

        LinearLayout areasLayout = v.findViewById(R.id.layoutAdminZoneCards);
        for (ParkingArea pa : MockDatabaseStore.getInstance().getParkingAreas().values()) {
            TextView t = new TextView(this);
            t.setBackgroundResource(R.drawable.bg_glass_card);
            t.setPadding(24, 20, 24, 20);
            t.setTextColor(Color.WHITE);
            t.setTextSize(13);
            t.setText("🅿 " + pa.getAreaName() + "  [" + pa.getStatus() + "]\nOccupancy: "
                    + (pa.getTotalCapacity() - pa.getAvailableSpots()) + "/" + pa.getTotalCapacity()
                    + " occupied (" + pa.getAvailableSpots() + " free)");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 14;
            t.setLayoutParams(lp);
            areasLayout.addView(t);
        }

        RecyclerView rv = v.findViewById(R.id.rvAdminRecentBookings);
        rv.setLayoutManager(new LinearLayoutManager(this));
        List<Reservation> resList = new ArrayList<>(MockDatabaseStore.getInstance().getReservations().values());
        rv.setAdapter(new SimpleBookingAdapter(resList));
    }

    // --- 2. ADMIN EVENTS ---
    private void loadAdminEvents() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_adm_events, contentFrame, false);
        contentFrame.addView(v);

        RecyclerView rv = v.findViewById(R.id.rvAdminEvents);
        rv.setLayoutManager(new LinearLayoutManager(this));
        List<Event> eventList = new ArrayList<>(MockDatabaseStore.getInstance().getEvents().values());

        rv.setAdapter(new RecyclerView.Adapter<EventRowVH>() {
            @NonNull
            @Override
            public EventRowVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new EventRowVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adm_event_row, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull EventRowVH holder, int position) {
                Event e = eventList.get(position);
                holder.tvId.setText("#" + e.getEventId());
                holder.tvTitle.setText(e.getEventTitle());
                holder.tvVenueTime.setText("Venue: " + e.getVenue() + " | " + e.getEventDate() + " " + e.getEventTime());
                holder.tvStatus.setText(e.getStatus());
                holder.btnDelete.setOnClickListener(view -> {
                    MockDatabaseStore.getInstance().getEvents().remove(e.getEventId());
                    eventList.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(AdminPortalActivity.this, "Event removed", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public int getItemCount() { return eventList.size(); }
        });
    }

    static class EventRowVH extends RecyclerView.ViewHolder {
        TextView tvId, tvTitle, tvVenueTime, tvStatus, btnDelete;
        EventRowVH(View v) {
            super(v);
            tvId = v.findViewById(R.id.tvAdmEventId);
            tvTitle = v.findViewById(R.id.tvAdmEventTitle);
            tvVenueTime = v.findViewById(R.id.tvAdmEventVenueTime);
            tvStatus = v.findViewById(R.id.tvAdmEventStatus);
            btnDelete = v.findViewById(R.id.btnDeleteEvent);
        }
    }

    // --- 3. ADMIN AREAS ---
    private void loadAdminAreas() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_adm_areas, contentFrame, false);
        contentFrame.addView(v);

        RecyclerView rv = v.findViewById(R.id.rvAdminAreas);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final List<ParkingArea> areaList = new ArrayList<>(MockDatabaseStore.getInstance().getParkingAreas().values());

        rv.setAdapter(new RecyclerView.Adapter<AreaRowVH>() {
            @NonNull
            @Override
            public AreaRowVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new AreaRowVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adm_area_row, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull AreaRowVH holder, int position) {
                ParkingArea a = areaList.get(position);
                holder.tvTitle.setText(a.getAreaName() + " (Code: " + a.getAreaCode() + ")");
                holder.tvDetails.setText("Total Bays: " + a.getTotalCapacity() + " | Available: " + a.getAvailableSpots() + " free | " + a.getProximityLandmark());
                holder.tvStatus.setText(a.getStatus());
            }

            @Override
            public int getItemCount() { return areaList.size(); }
        });
    }

    static class AreaRowVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetails, tvStatus;
        AreaRowVH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvAreaTitle);
            tvDetails = v.findViewById(R.id.tvAreaCapacityDetails);
            tvStatus = v.findViewById(R.id.tvAreaStatus);
        }
    }

    // --- 4. ADMIN SLOTS ---
    private void loadAdminSlots() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_adm_slots, contentFrame, false);
        contentFrame.addView(v);

        v.findViewById(R.id.btnOpenAddSlotDialog).setOnClickListener(view -> showAddSlotDialog());

        RecyclerView rv = v.findViewById(R.id.rvAdminSlots);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final List<ParkingSlot> slotList = new ArrayList<>(MockDatabaseStore.getInstance().getParkingSlots().values());

        rv.setAdapter(new RecyclerView.Adapter<SlotRowVH>() {
            @NonNull
            @Override
            public SlotRowVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new SlotRowVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adm_slot_row, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull SlotRowVH holder, int position) {
                ParkingSlot slot = slotList.get(position);
                holder.tvId.setText(slot.getSlotNumber());
                holder.tvZoneType.setText(slot.getAreaName() + " | " + slot.getVehicleType());
                holder.tvLevel.setText(slot.getFloorLevel());
                holder.tvStatus.setText(slot.getStatus());

                if ("OCCUPIED".equalsIgnoreCase(slot.getStatus())) {
                    holder.tvStatus.setTextColor(Color.parseColor("#EF4444"));
                } else if ("RESERVED".equalsIgnoreCase(slot.getStatus())) {
                    holder.tvStatus.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    holder.tvStatus.setTextColor(Color.parseColor("#E8B86D"));
                }

                holder.btnDelete.setOnClickListener(view -> {
                    MockDatabaseStore.getInstance().getParkingSlots().remove(slot.getSlotId());
                    slotList.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(AdminPortalActivity.this, "Slot " + slot.getSlotNumber() + " deleted", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public int getItemCount() { return slotList.size(); }
        });
    }

    static class SlotRowVH extends RecyclerView.ViewHolder {
        TextView tvId, tvZoneType, tvLevel, tvStatus, btnDelete;
        SlotRowVH(View v) {
            super(v);
            tvId = v.findViewById(R.id.tvSlotIdentifier);
            tvZoneType = v.findViewById(R.id.tvSlotZoneType);
            tvLevel = v.findViewById(R.id.tvSlotLevel);
            tvStatus = v.findViewById(R.id.tvSlotCurrentStatus);
            btnDelete = v.findViewById(R.id.btnDeleteSlot);
        }
    }

    // --- 5. STATUS MONITOR (INTERACTIVE TOGGLE GRID) ---
    private void loadAdminStatusMonitor() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_adm_status_monitor, contentFrame, false);
        contentFrame.addView(v);

        RecyclerView rv = v.findViewById(R.id.rvStatusMonitorGrid);
        rv.setLayoutManager(new GridLayoutManager(this, 4));
        final List<ParkingSlot> monitorSlots = new ArrayList<>(MockDatabaseStore.getInstance().getParkingSlots().values());

        rv.setAdapter(new RecyclerView.Adapter<MonitorVH>() {
            @NonNull
            @Override
            public MonitorVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new MonitorVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adm_monitor_slot, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull MonitorVH holder, int position) {
                ParkingSlot slot = monitorSlots.get(position);
                holder.tvNum.setText(slot.getSlotNumber());
                holder.tvType.setText(slot.getVehicleType());
                holder.tvState.setText(slot.getStatus());

                if ("OCCUPIED".equalsIgnoreCase(slot.getStatus())) {
                    holder.tvState.setTextColor(Color.parseColor("#EF4444"));
                } else if ("BLOCKED".equalsIgnoreCase(slot.getStatus())) {
                    holder.tvState.setTextColor(Color.parseColor("#64748B"));
                } else if ("RESERVED".equalsIgnoreCase(slot.getStatus())) {
                    holder.tvState.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    holder.tvState.setTextColor(Color.parseColor("#E8B86D"));
                }

                holder.itemView.setOnClickListener(view -> {
                    if ("AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
                        slot.setStatus("OCCUPIED");
                    } else if ("OCCUPIED".equalsIgnoreCase(slot.getStatus())) {
                        slot.setStatus("BLOCKED");
                    } else {
                        slot.setStatus("AVAILABLE");
                    }
                    notifyItemChanged(position);
                });
            }

            @Override
            public int getItemCount() { return monitorSlots.size(); }
        });
    }

    static class MonitorVH extends RecyclerView.ViewHolder {
        TextView tvNum, tvType, tvState;
        MonitorVH(View v) {
            super(v);
            tvNum = v.findViewById(R.id.tvMonitorSlotNum);
            tvType = v.findViewById(R.id.tvMonitorSlotType);
            tvState = v.findViewById(R.id.tvMonitorSlotState);
        }
    }

    // --- 6. BOOKINGS ---
    private void loadAdminBookings() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_adm_reservations, contentFrame, false);
        contentFrame.addView(v);

        RecyclerView rv = v.findViewById(R.id.rvAdminReservations);
        rv.setLayoutManager(new LinearLayoutManager(this));
        List<Reservation> list = new ArrayList<>(MockDatabaseStore.getInstance().getReservations().values());
        rv.setAdapter(new SimpleBookingAdapter(list));
    }

    // --- 7. USERS ---
    private void loadAdminUsers() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_adm_users, contentFrame, false);
        contentFrame.addView(v);

        RecyclerView rv = v.findViewById(R.id.rvAdminUsers);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final List<User> userList = DatabaseHelper.getInstance(this).getAllUsers();

        rv.setAdapter(new RecyclerView.Adapter<UserRowVH>() {
            @NonNull
            @Override
            public UserRowVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new UserRowVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adm_user_row, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull UserRowVH holder, int position) {
                User u = userList.get(position);
                holder.tvName.setText(u.getFullName() + " (" + u.getUsername() + ")");
                holder.tvDetails.setText("Email: " + u.getEmail() + " | Phone: " + u.getPhone() + " | Status: " + u.getStatus());
                holder.tvRole.setText(u.getRole());

                holder.btnToggle.setOnClickListener(view -> {
                    u.setStatus("ACTIVE".equalsIgnoreCase(u.getStatus()) ? "DISABLED" : "ACTIVE");
                    DatabaseHelper.getInstance(AdminPortalActivity.this).updateUser(u);
                    notifyItemChanged(position);
                });
            }

            @Override
            public int getItemCount() { return userList.size(); }
        });
    }

    static class UserRowVH extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvDetails, btnToggle;
        UserRowVH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvAdmUserName);
            tvRole = v.findViewById(R.id.tvAdmUserRole);
            tvDetails = v.findViewById(R.id.tvAdmUserDetails);
            btnToggle = v.findViewById(R.id.btnToggleUserStatus);
        }
    }

    // --- 8. ANNOUNCEMENTS ---
    private void loadAdminAnnouncements() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_adm_announcements, contentFrame, false);
        contentFrame.addView(v);

        RecyclerView rv = v.findViewById(R.id.rvAdminNotifs);
        rv.setLayoutManager(new LinearLayoutManager(this));
        final List<Notification> notifList = new ArrayList<>(MockDatabaseStore.getInstance().getNotifications().values());

        rv.setAdapter(new RecyclerView.Adapter<AdminNotifVH>() {
            @NonNull
            @Override
            public AdminNotifVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new AdminNotifVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull AdminNotifVH holder, int position) {
                Notification n = notifList.get(position);
                holder.tvTitle.setText(n.getTitle());
                holder.tvMessage.setText(n.getMessage());
                holder.tvBadge.setText(n.getType());
            }

            @Override
            public int getItemCount() { return notifList.size(); }
        });
    }

    static class AdminNotifVH extends RecyclerView.ViewHolder {
        TextView tvBadge, tvTitle, tvTime, tvMessage;
        AdminNotifVH(View v) {
            super(v);
            tvBadge = v.findViewById(R.id.tvNotifTypeBadge);
            tvTitle = v.findViewById(R.id.tvNotifTitle);
            tvTime = v.findViewById(R.id.tvNotifTime);
            tvMessage = v.findViewById(R.id.tvNotifMessage);
        }
    }

    // --- 9. REPORTS ---
    private void loadAdminReports() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_adm_reports, contentFrame, false);
        contentFrame.addView(v);
    }

    // --- ADD SLOT DIALOG MODAL ---
    private void showAddSlotDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_adm_add_slot);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Spinner spZone = dialog.findViewById(R.id.spDialogZone);
        Spinner spType = dialog.findViewById(R.id.spDialogType);
        EditText etId = dialog.findViewById(R.id.etDialogSlotIdentifier);

        ArrayAdapter<String> zoneAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"SJT PARKING", "CDMM PARKING", "WOODY'S PARKING"});
        spZone.setAdapter(zoneAdapter);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"CAR", "BIKE"});
        spType.setAdapter(typeAdapter);

        dialog.findViewById(R.id.btnCancelAddSlot).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirmCreateSlot).setOnClickListener(v -> {
            String slotNum = etId.getText().toString().trim();
            if (slotNum.isEmpty()) {
                Toast.makeText(this, "Enter slot number (e.g. SJT-C16)", Toast.LENGTH_SHORT).show();
                return;
            }

            int newId = MockDatabaseStore.getInstance().nextSlotId();
            ParkingSlot ps = new ParkingSlot(newId, 1, slotNum, spType.getSelectedItem().toString(), "Ground Level", "AVAILABLE");
            ps.setAreaName(spZone.getSelectedItem().toString());
            MockDatabaseStore.getInstance().getParkingSlots().put(newId, ps);

            dialog.dismiss();
            Toast.makeText(this, "Slot " + slotNum + " created!", Toast.LENGTH_SHORT).show();
            selectTab(3);
        });

        dialog.show();
    }

    // Shared Booking Adapter
    class SimpleBookingAdapter extends RecyclerView.Adapter<SimpleBookingVH> {
        List<Reservation> resList;
        SimpleBookingAdapter(List<Reservation> resList) { this.resList = resList; }

        @NonNull
        @Override
        public SimpleBookingVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SimpleBookingVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adm_res_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SimpleBookingVH holder, int position) {
            Reservation r = resList.get(position);
            holder.tvCode.setText(r.getBookingCode());
            holder.tvStatus.setText(r.getStatus());
            holder.tvDetails.setText("User: " + (r.getUserName() != null ? r.getUserName() : "Customer") + " | " + r.getAreaName() + " (" + r.getSlotNumber() + ")");
            holder.btnCheckIn.setOnClickListener(v -> {
                r.setStatus("COMPLETED");
                notifyItemChanged(position);
                Toast.makeText(AdminPortalActivity.this, "Customer Checked-In!", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() { return resList.size(); }
    }

    static class SimpleBookingVH extends RecyclerView.ViewHolder {
        TextView tvCode, tvStatus, tvDetails, btnCheckIn;
        SimpleBookingVH(View v) {
            super(v);
            tvCode = v.findViewById(R.id.tvAdmResCode);
            tvStatus = v.findViewById(R.id.tvAdmResStatus);
            tvDetails = v.findViewById(R.id.tvAdmResDetails);
            btnCheckIn = v.findViewById(R.id.btnToggleCheckIn);
        }
    }
}