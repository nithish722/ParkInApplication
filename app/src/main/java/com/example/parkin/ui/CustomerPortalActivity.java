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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.net.Uri;
import com.example.parkin.R;
import com.example.parkin.database.MockDatabaseStore;
import com.example.parkin.models.*;
import com.example.parkin.services.ParkingPricingService;
import com.example.parkin.util.QRCodeUtil;
import com.example.parkin.util.PassReceiptGenerator;

import java.io.File;
import java.util.Locale;

import java.util.ArrayList;
import java.util.List;

public class CustomerPortalActivity extends AppCompatActivity {

    private FrameLayout contentFrame;
    private TextView[] navPills;
    private int currentUserId = 2; // Default to Alex Reynolds
    private String currentUserName = "Alex Reynolds";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_portal);

        currentUserId = getIntent().getIntExtra("USER_ID", 2);
        currentUserName = getIntent().getStringExtra("USER_NAME");
        if (currentUserName == null) currentUserName = "Alex Reynolds";

        TextView tvHeaderUser = findViewById(R.id.tvHeaderUserName);
        tvHeaderUser.setText(currentUserName);

        contentFrame = findViewById(R.id.contentFrame);

        initNavPills();
        findViewById(R.id.btnHeaderLogout).setOnClickListener(v -> showLogoutDialog());

        // Default to Screen 1: Dashboard
        selectTab(0);
    }

    private void initNavPills() {
        navPills = new TextView[]{
                findViewById(R.id.navDashboard),
                findViewById(R.id.navCampusEvents),
                findViewById(R.id.navSmartRecommender),
                findViewById(R.id.navParkingMap),
                findViewById(R.id.navReservations),
                findViewById(R.id.navAnnouncements),
                findViewById(R.id.navProfile),
                findViewById(R.id.navHelp)
        };

        for (int i = 0; i < navPills.length; i++) {
            final int index = i;
            navPills[i].setOnClickListener(v -> selectTab(index));
        }
    }

    private void selectTab(int index) {
        for (int i = 0; i < navPills.length; i++) {
            if (i == index) {
                navPills[i].setBackgroundResource(R.drawable.bg_nav_pill_active);
                navPills[i].setTextColor(Color.WHITE);
            } else {
                navPills[i].setBackgroundResource(R.drawable.bg_nav_pill_inactive);
                navPills[i].setTextColor(Color.parseColor("#CBD5E1"));
            }
        }
        contentFrame.removeAllViews();

        switch (index) {
            case 0: loadDashboardView(); break;
            case 1: loadCampusEventsView(); break;
            case 2: loadSmartRecommenderView(); break;
            case 3: loadParkingMapView(); break;
            case 4: loadReservationsView(); break;
            case 5: loadAnnouncementsView(); break;
            case 6: loadProfileView(); break;
            case 7: loadHelpView(); break;
        }
    }

    // --- SCREEN 1: DASHBOARD ---
    private void loadDashboardView() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_cus_dashboard, contentFrame, false);
        contentFrame.addView(v);

        TextView tvHero = v.findViewById(R.id.tvHeroTitle);
        tvHero.setText("Welcome back, " + currentUserName.split(" ")[0] + " 👋");

        v.findViewById(R.id.btnHeroBookParking).setOnClickListener(view -> selectTab(1));
        v.findViewById(R.id.btnHeroInteractiveMap).setOnClickListener(view -> selectTab(3));

        LinearLayout areasContainer = v.findViewById(R.id.layoutDashboardAreas);
        areasContainer.removeAllViews();

        for (ParkingArea area : MockDatabaseStore.getInstance().getParkingAreas().values()) {
            TextView areaCard = new TextView(this);
            areaCard.setBackgroundResource(R.drawable.bg_glass_card);
            areaCard.setPadding(30, 24, 30, 24);
            areaCard.setTextColor(Color.WHITE);
            areaCard.setTextSize(14);
            areaCard.setText(area.getAreaName() + "  [OPEN]\n" + area.getProximityLandmark() + " • " + area.getAvailableSpots() + " free slots available");

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = 20;
            areaCard.setLayoutParams(lp);
            areaCard.setOnClickListener(view -> selectTab(3));
            areasContainer.addView(areaCard);
        }
    }

    // --- SCREEN 2: EVENTS CATALOG ---
    private void loadCampusEventsView() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_cus_events, contentFrame, false);
        contentFrame.addView(v);

        RecyclerView rv = v.findViewById(R.id.rvEventsList);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<Event> list = new ArrayList<>(MockDatabaseStore.getInstance().getEvents().values());
        rv.setAdapter(new RecyclerView.Adapter<EventVH>() {
            @NonNull
            @Override
            public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new EventVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_card, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull EventVH holder, int position) {
                Event e = list.get(position);
                holder.tvTitle.setText(e.getEventTitle());
                holder.tvDesc.setText(e.getDescription());
                holder.tvVenue.setText("📍 " + e.getVenue());
                holder.tvDateTime.setText(e.getEventDate() + "  " + e.getEventTime());
                holder.tvCategory.setText(e.getCategory());
                holder.btnSelectParking.setOnClickListener(view -> selectTab(2));
            }

            @Override
            public int getItemCount() { return list.size(); }
        });
    }

    static class EventVH extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDateTime, tvTitle, tvDesc, tvVenue;
        Button btnSelectParking;
        EventVH(View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tvEventCategoryBadge);
            tvDateTime = v.findViewById(R.id.tvEventDateTime);
            tvTitle = v.findViewById(R.id.tvEventTitle);
            tvDesc = v.findViewById(R.id.tvEventDesc);
            tvVenue = v.findViewById(R.id.tvEventVenue);
            btnSelectParking = v.findViewById(R.id.btnSelectParkingForEvent);
        }
    }

    // --- SCREEN 3: SMART RECOMMENDER ---
    private void loadSmartRecommenderView() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_cus_recommender, contentFrame, false);
        contentFrame.addView(v);

        Spinner sp = v.findViewById(R.id.spRecommenderEvent);
        List<String> eventTitles = new ArrayList<>();
        for (Event e : MockDatabaseStore.getInstance().getEvents().values()) {
            eventTitles.add(e.getEventTitle() + " @ " + e.getVenue());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, eventTitles);
        sp.setAdapter(adapter);

        v.findViewById(R.id.btnChooseRecSlot).setOnClickListener(view -> selectTab(3));
    }

    // --- SCREEN 4: 2D PARKING BLUEPRINT ---
    // --- SCREEN 4: 2D PARKING BLUEPRINT (4-COLUMN CIRCULAR NODE GRID) ---
    private ParkingSlot selectedSlot = null;
    private void loadParkingMapView() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_cus_parking_map, contentFrame, false);
        contentFrame.addView(v);

        RecyclerView rv = v.findViewById(R.id.rvParkingBays);
        rv.setLayoutManager(new GridLayoutManager(this, 4));

        TextView tvInfo = v.findViewById(R.id.tvSelectedBayInfo);
        Button btnProceed = v.findViewById(R.id.btnProceedToBookSlot);

        TextView tabAll = v.findViewById(R.id.tabSectorAll);
        TextView tabSjt = v.findViewById(R.id.tabSectorSJT);
        TextView tabCdmm = v.findViewById(R.id.tabSectorCDMM);
        TextView tabWoodys = v.findViewById(R.id.tabSectorWoodys);

        List<ParkingSlot> allSlots = new ArrayList<>(MockDatabaseStore.getInstance().getParkingSlots().values());
        List<ParkingSlot> filteredSlots = new ArrayList<>(allSlots);

        RecyclerView.Adapter<BayVH> adapter = new RecyclerView.Adapter<BayVH>() {
            @NonNull
            @Override
            public BayVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new BayVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking_bay, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull BayVH holder, int position) {
                ParkingSlot slot = filteredSlots.get(position);
                holder.tvBayNumber.setText(slot.getSlotNumber());
                holder.tvVehicleType.setText(slot.getVehicleType());

                boolean isSelected = (selectedSlot != null && selectedSlot.getSlotId() == slot.getSlotId());

                if (isSelected) {
                    holder.tvStatus.setText("SELECTED");
                    holder.tvStatus.setTextColor(Color.WHITE);
                    holder.layoutNode.setBackgroundResource(R.drawable.bg_glass_circle_slot_selected);
                    holder.itemView.setEnabled(true);
                } else if ("OCCUPIED".equalsIgnoreCase(slot.getStatus())) {
                    holder.tvStatus.setText("OCCUPIED");
                    holder.tvStatus.setTextColor(Color.parseColor("#EF4444"));
                    holder.layoutNode.setBackgroundResource(R.drawable.bg_glass_circle_slot);
                    holder.itemView.setEnabled(false);
                } else if ("RESERVED".equalsIgnoreCase(slot.getStatus())) {
                    holder.tvStatus.setText("RESERVED");
                    holder.tvStatus.setTextColor(Color.parseColor("#F59E0B"));
                    holder.layoutNode.setBackgroundResource(R.drawable.bg_glass_circle_slot);
                    holder.itemView.setEnabled(false);
                } else if ("BLOCKED".equalsIgnoreCase(slot.getStatus())) {
                    holder.tvStatus.setText("BLOCKED");
                    holder.tvStatus.setTextColor(Color.parseColor("#64748B"));
                    holder.layoutNode.setBackgroundResource(R.drawable.bg_glass_circle_slot);
                    holder.itemView.setEnabled(false);
                } else {
                    holder.tvStatus.setText("AVAILABLE");
                    holder.tvStatus.setTextColor(Color.parseColor("#E8B86D"));
                    holder.layoutNode.setBackgroundResource(R.drawable.bg_glass_circle_slot);
                    holder.itemView.setEnabled(true);
                }

                holder.itemView.setOnClickListener(view -> {
                    selectedSlot = slot;
                    tvInfo.setText("Selected Bay: " + slot.getSlotNumber() + " (" + slot.getAreaName() + ")");
                    btnProceed.setEnabled(true);
                    notifyDataSetChanged();
                });
            }

            @Override
            public int getItemCount() { return filteredSlots.size(); }
        };

        rv.setAdapter(adapter);

        // Filter tab actions
        View.OnClickListener filterListener = view -> {
            tabAll.setBackgroundResource(R.drawable.bg_nav_pill_inactive); tabAll.setTextColor(Color.parseColor("#94A3B8"));
            tabSjt.setBackgroundResource(R.drawable.bg_nav_pill_inactive); tabSjt.setTextColor(Color.parseColor("#94A3B8"));
            tabCdmm.setBackgroundResource(R.drawable.bg_nav_pill_inactive); tabCdmm.setTextColor(Color.parseColor("#94A3B8"));
            tabWoodys.setBackgroundResource(R.drawable.bg_nav_pill_inactive); tabWoodys.setTextColor(Color.parseColor("#94A3B8"));

            TextView clicked = (TextView) view;
            clicked.setBackgroundResource(R.drawable.bg_nav_pill_active);
            clicked.setTextColor(Color.WHITE);

            filteredSlots.clear();
            if (view.getId() == R.id.tabSectorSJT) {
                for (ParkingSlot s : allSlots) { if (s.getAreaName().contains("SJT")) filteredSlots.add(s); }
            } else if (view.getId() == R.id.tabSectorCDMM) {
                for (ParkingSlot s : allSlots) { if (s.getAreaName().contains("CDMM")) filteredSlots.add(s); }
            } else if (view.getId() == R.id.tabSectorWoodys) {
                for (ParkingSlot s : allSlots) { if (s.getAreaName().contains("Woody")) filteredSlots.add(s); }
            } else {
                filteredSlots.addAll(allSlots);
            }
            adapter.notifyDataSetChanged();
        };

        tabAll.setOnClickListener(filterListener);
        tabSjt.setOnClickListener(filterListener);
        tabCdmm.setOnClickListener(filterListener);
        tabWoodys.setOnClickListener(filterListener);

        btnProceed.setOnClickListener(view -> {
            if (selectedSlot != null) {
                showBookingPaymentDialog(selectedSlot);
            }
        });
    }

    static class BayVH extends RecyclerView.ViewHolder {
        View layoutNode;
        TextView tvBayNumber, tvVehicleType, tvStatus;
        BayVH(View v) {
            super(v);
            layoutNode = v.findViewById(R.id.layoutBayNode);
            tvBayNumber = v.findViewById(R.id.tvBayNumber);
            tvVehicleType = v.findViewById(R.id.tvBayVehicleType);
            tvStatus = v.findViewById(R.id.tvBayStatus);
        }
    }

    // --- SCREEN 5: RESERVATIONS ---
    private void loadReservationsView() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_cus_reservations, contentFrame, false);
        contentFrame.addView(v);

        v.findViewById(R.id.btnBookNewReservation).setOnClickListener(view -> selectTab(1));

        RecyclerView rv = v.findViewById(R.id.rvReservations);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<Reservation> list = new ArrayList<>(MockDatabaseStore.getInstance().getReservations().values());
        rv.setAdapter(new RecyclerView.Adapter<ResVH>() {
            @NonNull
            @Override
            public ResVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ResVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation_pass, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull ResVH holder, int position) {
                Reservation r = list.get(position);
                holder.tvCode.setText(r.getBookingCode());
                holder.tvStatus.setText(r.getStatus());
                holder.tvEventTitle.setText(r.getEventTitle() != null ? r.getEventTitle() : "Campus Event");
                holder.tvDetails.setText("Zone: " + r.getAreaName() + " | Slot: " + r.getSlotNumber() + " | " + r.getVehiclePlate());

                holder.btnCancel.setOnClickListener(view -> {
                    MockDatabaseStore.getInstance().getReservations().remove(r.getReservationId());
                    Toast.makeText(CustomerPortalActivity.this, "Reservation cancelled", Toast.LENGTH_SHORT).show();
                    loadReservationsView();
                });

                holder.btnViewPass.setOnClickListener(view -> showQrPassDialog(r));
            }

            @Override
            public int getItemCount() { return list.size(); }
        });
    }

    static class ResVH extends RecyclerView.ViewHolder {
        TextView tvCode, tvStatus, tvEventTitle, tvDetails, btnCancel, btnViewPass;
        ResVH(View v) {
            super(v);
            tvCode = v.findViewById(R.id.tvPassBookingCode);
            tvStatus = v.findViewById(R.id.tvPassStatus);
            tvEventTitle = v.findViewById(R.id.tvPassEventTitle);
            tvDetails = v.findViewById(R.id.tvPassDetails);
            btnCancel = v.findViewById(R.id.btnCancelPass);
            btnViewPass = v.findViewById(R.id.btnViewQrPass);
        }
    }

    // --- SCREEN 6: ANNOUNCEMENTS ---
    private void loadAnnouncementsView() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_cus_announcements, contentFrame, false);
        contentFrame.addView(v);

        RecyclerView rv = v.findViewById(R.id.rvAnnouncements);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<Notification> list = new ArrayList<>(MockDatabaseStore.getInstance().getNotifications().values());
        rv.setAdapter(new RecyclerView.Adapter<NotifVH>() {
            @NonNull
            @Override
            public NotifVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new NotifVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull NotifVH holder, int position) {
                Notification n = list.get(position);
                holder.tvTitle.setText(n.getTitle());
                holder.tvMessage.setText(n.getMessage());
                holder.tvBadge.setText(n.getType());
                if ("WARNING".equalsIgnoreCase(n.getType())) {
                    holder.tvBadge.setTextColor(Color.parseColor("#F59E0B"));
                } else if ("URGENT".equalsIgnoreCase(n.getType())) {
                    holder.tvBadge.setTextColor(Color.parseColor("#EF4444"));
                } else {
                    holder.tvBadge.setTextColor(Color.parseColor("#E8B86D"));
                }
            }

            @Override
            public int getItemCount() { return list.size(); }
        });
    }

    static class NotifVH extends RecyclerView.ViewHolder {
        TextView tvBadge, tvTitle, tvTime, tvMessage;
        NotifVH(View v) {
            super(v);
            tvBadge = v.findViewById(R.id.tvNotifTypeBadge);
            tvTitle = v.findViewById(R.id.tvNotifTitle);
            tvTime = v.findViewById(R.id.tvNotifTime);
            tvMessage = v.findViewById(R.id.tvNotifMessage);
        }
    }

    // --- SCREEN 7: PROFILE & SETTINGS ---
    private void loadProfileView() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_cus_profile, contentFrame, false);
        contentFrame.addView(v);

        User currentUser = MockDatabaseStore.getInstance().getUsers().get(currentUserId);
        EditText etName = v.findViewById(R.id.etProfileName);
        EditText etEmail = v.findViewById(R.id.etProfileEmail);
        EditText etPhone = v.findViewById(R.id.etProfilePhone);
        EditText etPlate = v.findViewById(R.id.etProfilePlate);

        if (currentUser != null) {
            etName.setText(currentUser.getFullName());
            etEmail.setText(currentUser.getEmail());
            etPhone.setText(currentUser.getPhone());
            etPlate.setText(currentUser.getVehiclePlate());
        }

        v.findViewById(R.id.btnSaveProfile).setOnClickListener(view -> {
            if (currentUser != null) {
                currentUser.setFullName(etName.getText().toString());
                currentUser.setEmail(etEmail.getText().toString());
                currentUser.setPhone(etPhone.getText().toString());
                currentUser.setVehiclePlate(etPlate.getText().toString());
                currentUserName = currentUser.getFullName();
                ((TextView) findViewById(R.id.tvHeaderUserName)).setText(currentUserName);
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- SCREEN 8: HELP & ABOUT ---
    private void loadHelpView() {
        View v = LayoutInflater.from(this).inflate(R.layout.view_cus_help, contentFrame, false);
        contentFrame.addView(v);
    }

    // --- SCREEN 9: DIGITAL QR PASS & PDF DOWNLOAD DIALOG ---
    private void showQrPassDialog(Reservation r) {
        if (r == null) return;
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pass_qr);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvBadge = dialog.findViewById(R.id.tvDialogPassBadge);
        TextView tvTitle = dialog.findViewById(R.id.tvDialogEventTitle);
        TextView tvSub = dialog.findViewById(R.id.tvDialogPassSubDetails);
        ImageView ivQr = dialog.findViewById(R.id.ivPassQrCode);
        Button btnDownload = dialog.findViewById(R.id.btnDownloadPdfPass);
        TextView btnClose = dialog.findViewById(R.id.btnCloseQrDialog);

        tvBadge.setText(r.getBookingCode() != null ? r.getBookingCode() : "CRISS-2026-X892");
        tvTitle.setText(r.getEventTitle() != null ? r.getEventTitle() : "Campus Event");
        tvSub.setText("Zone: " + r.getAreaName() + "  |  Slot: " + r.getSlotNumber() + "  |  Plate: " + r.getVehiclePlate());

        String payload = QRCodeUtil.buildPayload(r);
        Bitmap qrBitmap = QRCodeUtil.generateBitmap(payload, 500);
        if (qrBitmap != null) {
            ivQr.setImageBitmap(qrBitmap);
        }

        btnDownload.setOnClickListener(v -> {
            try {
                User user = MockDatabaseStore.getInstance().getUsers().get(currentUserId);
                File pdfFile = PassReceiptGenerator.generateAndSavePdfPass(this, r, user);
                Toast.makeText(this, "Pass PDF downloaded: " + pdfFile.getName(), Toast.LENGTH_LONG).show();

                try {
                    Uri fileUri = FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".provider",
                            pdfFile
                    );
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(fileUri, "application/pdf");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                } catch (Exception ignored) {}
            } catch (Exception ex) {
                Toast.makeText(this, "Failed to download PDF: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // --- SCREEN 10: BOOKING & PAYMENT TARIFF SUMMARY DIALOG ---
    private void showBookingPaymentDialog(ParkingSlot slot) {
        if (slot == null) return;

        User user = MockDatabaseStore.getInstance().getUsers().get(currentUserId);
        String userEmail = user != null ? user.getEmail() : "alex@vit.ac.in";
        boolean isVit = ParkingPricingService.isVitEmail(userEmail);
        double amount = ParkingPricingService.calculateParkingFee(userEmail, 4);

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_booking_payment);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvAreaSlot = dialog.findViewById(R.id.tvSummaryAreaSlot);
        TextView tvEmail = dialog.findViewById(R.id.tvSummaryUserEmail);
        TextView tvTariffTitle = dialog.findViewById(R.id.tvSummaryTariffTitle);
        TextView tvTariffDesc = dialog.findViewById(R.id.tvSummaryTariffDesc);
        TextView tvTotal = dialog.findViewById(R.id.tvSummaryTotalPayable);
        View layoutMethods = dialog.findViewById(R.id.layoutPaymentMethods);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmBookingPayment);
        TextView btnCancel = dialog.findViewById(R.id.btnCancelBookingPayment);

        tvAreaSlot.setText("Zone: " + slot.getAreaName() + "  |  Slot: " + slot.getSlotNumber());
        tvEmail.setText("Account: " + userEmail);

        if (isVit) {
            tvTariffTitle.setText("🎉 VIT Campus Member Waiver Applied");
            tvTariffDesc.setText("100% Free Campus Parking Pass for @vit.ac.in users");
            tvTotal.setText("Total Amount Payable: INR 0.00 (FREE)");
            layoutMethods.setVisibility(View.GONE);
            btnConfirm.setText("Confirm Free Booking →");
        } else {
            tvTariffTitle.setText("💳 Standard Visitor Parking Tariff");
            tvTariffDesc.setText("Base Charge: INR 50.00 (4 Hours Pass)\nOvertime Policy: 12% Extra (INR 6.00 / hr after 4 hrs)");
            tvTotal.setText(String.format(Locale.US, "Total Amount Payable: INR %.2f", amount));
            layoutMethods.setVisibility(View.VISIBLE);
            btnConfirm.setText("Pay INR 50.00 & Book →");
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();

            slot.setStatus("RESERVED");
            String plate = (user != null && user.getVehiclePlate() != null && !user.getVehiclePlate().isEmpty())
                    ? user.getVehiclePlate() : "TN-22-CR-1024";

            Reservation res = new Reservation(
                    MockDatabaseStore.getInstance().nextReservationId(),
                    "CRISS-2026-" + (int)(Math.random() * 9000 + 1000),
                    currentUserId, 1, slot.getAreaId(), slot.getSlotId(),
                    slot.getVehicleType(), plate,
                    java.time.LocalDateTime.now(), 4, amount, "CONFIRMED", "Online booking (" + (isVit ? "VIT Free Pass" : "Paid INR 50.00") + ")"
            );
            res.setUserName(user != null ? user.getFullName() : "Alex Reynolds");
            res.setAreaName(slot.getAreaName());
            res.setSlotNumber(slot.getSlotNumber());
            res.setEventTitle("Campus Visit");

            MockDatabaseStore.getInstance().getReservations().put(res.getReservationId(), res);

            Toast.makeText(this, "Slot " + slot.getSlotNumber() + " reserved successfully!", Toast.LENGTH_SHORT).show();
            selectTab(4); // Switch to My Passes
            showQrPassDialog(res); // Show QR Code & PDF Pass Dialog
        });

        dialog.show();
    }

    // --- SCREEN 11: LOGOUT MODAL DIALOG ---
    private void showLogoutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_glass_logout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnDialogConfirmLogout).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(CustomerPortalActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}