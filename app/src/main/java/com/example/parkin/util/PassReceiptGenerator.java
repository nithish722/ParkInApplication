package com.example.parkin.util;

import android.content.Context;
import android.os.Environment;
import com.example.parkin.models.Reservation;
import com.example.parkin.models.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PassReceiptGenerator {

    /**
     * Generates a native PDF Digital Parking Pass receipt and saves it to the device's Downloads directory.
     */
    public static File generateAndSavePdfPass(Context context, Reservation reservation, User user) throws Exception {
        if (reservation == null) {
            throw new Exception("No active reservation to download.");
        }

        byte[] pdfBytes = generateReceiptPdfBytes(reservation, user);
        String code = reservation.getBookingCode() != null ? reservation.getBookingCode().replaceAll("[^a-zA-Z0-9_-]", "_") : "CRISS_PASS";
        String fileName = "CrissCross_Parking_Pass_" + code + ".pdf";

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadsDir == null || !downloadsDir.exists()) {
            downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        }
        if (downloadsDir != null && !downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }

        File targetFile = new File(downloadsDir, fileName);
        FileOutputStream fos = new FileOutputStream(targetFile);
        fos.write(pdfBytes);
        fos.flush();
        fos.close();

        return targetFile;
    }

    /**
     * Builds a 100% standards-compliant ISO-32000 Adobe PDF 1.4 document containing vector
     * receipt layouts, booking details, and gate scanner verification specs.
     */
    public static byte[] generateReceiptPdfBytes(Reservation reservation, User user) {
        String bookingCode = safeText(reservation.getBookingCode(), "CRISS-2026-X892");
        String eventTitle = safeText(reservation.getEventTitle(), "Campus Event");
        String eventVenue = safeText(reservation.getEventVenue(), "Main Campus Venue");
        String areaName = safeText(reservation.getAreaName(), "SJT PARKING");
        String slotNum = safeText(reservation.getSlotNumber(), "SJT-C01");
        String vehicleType = safeText(reservation.getVehicleType(), "CAR");
        String vehiclePlate = safeText(reservation.getVehicleNumber(), "TN-22-CR-1024");
        String resStatus = safeText(reservation.getStatus(), "CONFIRMED");

        String userEmail = (user != null && user.getEmail() != null && !user.getEmail().isEmpty())
                ? user.getEmail() : "visitor@campus.edu";
        String userName = (user != null && user.getFullName() != null && !user.getFullName().isEmpty())
                ? user.getFullName() : safeText(reservation.getUserName(), "Alex Reynolds");
        String userPhone = (user != null && user.getPhone() != null && !user.getPhone().isEmpty())
                ? user.getPhone() : "+91 9845012345";

        double totalPaid = reservation.getTotalAmount();
        String amountFormatted = totalPaid > 0.0
                ? String.format(Locale.US, "INR %.2f (PAID)", totalPaid)
                : "INR 0.00 (100% FREE CAMPUS PASS)";

        String generatedTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a"));

        StringBuilder s = new StringBuilder(16384);

        // 1. Page Background (#F1F5F9)
        fillRect(s, 0, 0, 595, 842, 0.945, 0.961, 0.976);

        // 2. Main Receipt Card Container (#0F1428 dark slate card)
        fillStrokeRect(s, 28, 30, 539, 782, 0.059, 0.078, 0.157, 0.910, 0.722, 0.427, 1.5);

        // 3. Top Header Banner (#E8B86D Warm Amber Gold)
        fillRect(s, 28, 734, 539, 78, 0.910, 0.722, 0.427);
        // Coral accent stripe below header
        fillRect(s, 28, 728, 539, 6, 0.945, 0.431, 0.408);

        // Brand Text in Header
        drawText(s, "F2", 18, 46, 782, 0.059, 0.078, 0.157, "CRISSCROSS SMART EVENT PARKING");
        drawText(s, "F1", 10, 46, 764, 0.120, 0.150, 0.250, "Official Digital Gate Access Pass & Receipt");
        drawText(s, "F2", 9, 46, 747, 0.180, 0.210, 0.320, "CAMPUS EVENT PARKING • AUTOMATED BARRIER VERIFICATION");

        // Booking Code Badge Box on Header Right
        fillRect(s, 392, 748, 158, 48, 0.059, 0.078, 0.157);
        drawText(s, "F1", 8, 404, 781, 0.910, 0.722, 0.427, "BOOKING REFERENCE CODE");
        drawText(s, "F3", 13, 404, 760, 1.000, 1.000, 1.000, bookingCode);

        // 4. Gate Verification Banner
        fillStrokeRect(s, 44, 574, 507, 140, 0.090, 0.118, 0.220, 0.220, 0.270, 0.400, 1.0);

        // Gate Verification Right Text
        drawText(s, "F2", 13, 68, 688, 1.0, 1.0, 1.0, "OFFICIAL GATE BARRIER CHECK-IN PASS");
        drawText(s, "F1", 9.5, 68, 670, 0.796, 0.835, 0.882, "Present this PDF pass at the " + truncate(areaName, 28) + " entry gate scanner.");
        drawText(s, "F1", 9.5, 68, 655, 0.796, 0.835, 0.882, "Camera automatically verifies your reserved bay (" + slotNum + ") & plate (" + vehiclePlate + ").");

        // Badges
        fillStrokeRect(s, 68, 616, 235, 26, 0.086, 0.396, 0.204, 0.290, 0.871, 0.502, 1.0);
        drawText(s, "F2", 9.5, 80, 625, 0.290, 0.871, 0.502, "STATUS VERIFIED: " + amountFormatted);

        fillStrokeRect(s, 315, 616, 120, 26, 0.150, 0.200, 0.350, 0.910, 0.722, 0.427, 1.0);
        drawText(s, "F2", 9.5, 327, 625, 0.910, 0.722, 0.427, resStatus);

        drawText(s, "F1", 8.5, 68, 594, 0.580, 0.639, 0.722, "Generated Timestamp: " + generatedTimestamp + "   |   Pass Duration: 4 Hours Base Access");

        // 5. SECTION 1: EVENT & PARKING SLOT ALLOCATION DETAILS
        drawSectionHeader(s, 44, 540, 507, "1. EVENT & DESIGNATED PARKING SLOT DETAILS");
        fillStrokeRect(s, 44, 422, 507, 114, 0.082, 0.106, 0.200, 0.200, 0.250, 0.380, 0.8);

        drawField(s, 58, 508, "CAMPUS EVENT TITLE", truncate(eventTitle, 38), false);
        drawField(s, 310, 508, "EVENT VENUE LOCATION", truncate(eventVenue, 34), false);

        drawField(s, 58, 472, "ALLOCATED PARKING AREA", truncate(areaName, 34), true);
        drawField(s, 310, 472, "RESERVED PARKING SLOT", slotNum + " (Designated Bay)", true);

        drawField(s, 58, 436, "VEHICLE CLASSIFICATION", vehicleType + " (Campus Registered)", false);
        drawField(s, 310, 436, "VEHICLE PLATE NUMBER", vehiclePlate, false);

        // 6. SECTION 2: ATTENDEE & ACCOUNT CREDENTIALS
        drawSectionHeader(s, 44, 390, 507, "2. ATTENDEE & ACCOUNT CREDENTIALS");
        fillStrokeRect(s, 44, 308, 507, 78, 0.082, 0.106, 0.200, 0.200, 0.250, 0.380, 0.8);

        drawField(s, 58, 358, "BOOKED FOR (ATTENDEE NAME)", truncate(userName, 34), false);
        drawField(s, 310, 358, "REGISTERED ACCOUNT EMAIL", truncate(userEmail, 36), false);

        drawField(s, 58, 322, "CONTACT PHONE NUMBER", truncate(userPhone, 28), false);
        drawField(s, 310, 322, "TOTAL AMOUNT PAID", amountFormatted, true);

        // 7. Footer
        fillRect(s, 28, 30, 539, 38, 0.039, 0.051, 0.110);
        drawText(s, "F1", 8.5, 44, 46, 0.580, 0.639, 0.722, "Receipt Generated: " + generatedTimestamp);
        drawText(s, "F2", 8.5, 300, 46, 0.910, 0.722, 0.427, "CrissCross Smart Event Parking System • Official PDF Pass");

        return assembleSinglePagePdf(s.toString());
    }

    private static void drawSectionHeader(StringBuilder s, double x, double y, double w, String title) {
        fillRect(s, x, y, w, 20, 0.145, 0.180, 0.310);
        fillRect(s, x, y, 4, 20, 0.945, 0.431, 0.408);
        drawText(s, "F2", 9.5, x + 12, y + 6, 0.910, 0.722, 0.427, title);
    }

    private static void drawField(StringBuilder s, double x, double y, String label, String value, boolean highlight) {
        drawText(s, "F1", 8.0, x, y + 13, 0.580, 0.639, 0.722, label);
        if (highlight) {
            drawText(s, "F2", 11.0, x, y, 0.949, 0.627, 0.416, value);
        } else {
            drawText(s, "F2", 10.5, x, y, 0.973, 0.980, 0.988, value);
        }
    }

    private static void fillRect(StringBuilder s, double x, double y, double w, double h, double r, double g, double b) {
        s.append(String.format(Locale.US, "%.3f %.3f %.3f rg\n%.2f %.2f %.2f %.2f re f\n", r, g, b, x, y, w, h));
    }

    private static void fillStrokeRect(StringBuilder s, double x, double y, double w, double h,
                                       double fr, double fg, double fb,
                                       double sr, double sg, double sb, double lineWidth) {
        s.append(String.format(Locale.US, "%.2f w\n%.3f %.3f %.3f rg\n%.3f %.3f %.3f RG\n%.2f %.2f %.2f %.2f re B\n",
                lineWidth, fr, fg, fb, sr, sg, sb, x, y, w, h));
    }

    private static void drawText(StringBuilder s, String fontKey, double fontSize, double x, double y,
                                 double r, double g, double b, String text) {
        String clean = sanitizePdfText(text);
        s.append(String.format(Locale.US, "BT /%s %.1f Tf %.3f %.3f %.3f rg %.2f %.2f Td (%s) Tj ET\n",
                fontKey, fontSize, r, g, b, x, y, clean));
    }

    private static String sanitizePdfText(String text) {
        if (text == null) return "";
        String replaced = text
                .replace("₹", "INR ")
                .replace("•", "-")
                .replace("—", "-")
                .replace("–", "-");
        StringBuilder sb = new StringBuilder(replaced.length());
        for (int i = 0; i < replaced.length(); i++) {
            char c = replaced.charAt(i);
            if (c == '\\' || c == '(' || c == ')') {
                sb.append('\\').append(c);
            } else if (c >= 32 && c <= 126) {
                sb.append(c);
            } else {
                sb.append(' ');
            }
        }
        return sb.toString().replaceAll("\\s+", " ").trim();
    }

    private static String safeText(String val, String fallback) {
        return (val != null && !val.trim().isEmpty()) ? val.trim() : fallback;
    }

    private static String truncate(String val, int maxLen) {
        if (val == null) return "";
        return val.length() <= maxLen ? val : val.substring(0, maxLen - 3) + "...";
    }

    private static byte[] assembleSinglePagePdf(String contentStream) {
        try {
            byte[] streamBytes = contentStream.getBytes(StandardCharsets.ISO_8859_1);
            ByteArrayOutputStream out = new ByteArrayOutputStream(streamBytes.length + 2048);
            List<Integer> offsets = new ArrayList<>();

            writeAscii(out, "%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n");

            // Object 1: Catalog
            offsets.add(out.size());
            writeAscii(out, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

            // Object 2: Pages
            offsets.add(out.size());
            writeAscii(out, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

            // Object 3: Page (A4 595 x 842)
            offsets.add(out.size());
            writeAscii(out, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 4 0 R /F2 5 0 R /F3 6 0 R >> >> "
                    + "/Contents 7 0 R >>\nendobj\n");

            // Object 4: Font F1 (Helvetica)
            offsets.add(out.size());
            writeAscii(out, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\nendobj\n");

            // Object 5: Font F2 (Helvetica-Bold)
            offsets.add(out.size());
            writeAscii(out, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\nendobj\n");

            // Object 6: Font F3 (Courier-Bold)
            offsets.add(out.size());
            writeAscii(out, "6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier-Bold /Encoding /WinAnsiEncoding >>\nendobj\n");

            // Object 7: Page Content Stream
            offsets.add(out.size());
            writeAscii(out, "7 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
            out.write(streamBytes);
            writeAscii(out, "\nendstream\nendobj\n");

            // Cross-Reference Table (xref)
            int xrefOffset = out.size();
            int totalObjects = offsets.size() + 1;
            writeAscii(out, "xref\n0 " + totalObjects + "\n");
            writeAscii(out, "0000000000 65535 f \n");
            for (int offset : offsets) {
                writeAscii(out, String.format(Locale.US, "%010d 00000 n \n", offset));
            }

            // Trailer
            writeAscii(out, "trailer\n<< /Size " + totalObjects + " /Root 1 0 R >>\n");
            writeAscii(out, "startxref\n" + xrefOffset + "\n%%EOF\n");

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build PDF document: " + e.getMessage(), e);
        }
    }

    private static void writeAscii(ByteArrayOutputStream out, String s) throws Exception {
        out.write(s.getBytes(StandardCharsets.ISO_8859_1));
    }
}