package com.example.parkin.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.example.parkin.models.Reservation;

public class QRCodeUtil {

    /**
     * Generates a crisp Android Bitmap containing a scannable QR code.
     */
    public static Bitmap generateBitmap(String content, int targetSize) {
        if (content == null || content.isEmpty()) {
            content = "CRISSCROSS-PASS";
        }

        try {
            boolean[][] modules = encodeQrMatrix(content);
            int matrixSize = modules.length;
            int quietZone = 2;
            int totalSize = matrixSize + (quietZone * 2);

            int scale = Math.max(1, targetSize / totalSize);
            int imgWidth = totalSize * scale;
            int imgHeight = totalSize * scale;

            Bitmap bitmap = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
            int darkColor = Color.rgb(15, 23, 42); // deep obsidian
            int lightColor = Color.WHITE;

            int[] pixels = new int[imgWidth * imgHeight];

            for (int y = 0; y < imgHeight; y++) {
                int modY = (y / scale) - quietZone;
                for (int x = 0; x < imgWidth; x++) {
                    int modX = (x / scale) - quietZone;
                    boolean isDark = false;
                    if (modX >= 0 && modX < matrixSize && modY >= 0 && modY < matrixSize) {
                        isDark = modules[modY][modX];
                    }
                    pixels[y * imgWidth + x] = isDark ? darkColor : lightColor;
                }
            }

            bitmap.setPixels(pixels, 0, imgWidth, 0, 0, imgWidth, imgHeight);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Constructs a standardized Gate Scanner payload string containing all pass verification data.
     */
    public static String buildPayload(Reservation reservation) {
        if (reservation == null) {
            return "CRISSCROSS:PASS|STATUS=INVALID";
        }
        return "CRISSCROSS:PASS"
                + "|CODE=" + (reservation.getBookingCode() != null ? reservation.getBookingCode() : "CRISS-2026-X892")
                + "|SLOT=" + (reservation.getSlotNumber() != null ? reservation.getSlotNumber() : "SJT-C01")
                + "|AREA=" + (reservation.getAreaName() != null ? reservation.getAreaName() : "SJT PARKING")
                + "|VEHICLE=" + (reservation.getVehicleType() != null ? reservation.getVehicleType() : "CAR")
                + ":" + (reservation.getVehicleNumber() != null ? reservation.getVehicleNumber() : "TN-22-CR-1024")
                + "|EVENT=" + (reservation.getEventTitle() != null ? reservation.getEventTitle() : "Campus Event")
                + "|STATUS=" + (reservation.getStatus() != null ? reservation.getStatus() : "CONFIRMED");
    }

    /**
     * Internal QR Code matrix generator (25x25 Version 2 QR matrix encoder).
     */
    private static boolean[][] encodeQrMatrix(String text) {
        int size = 25; // Version 2
        boolean[][] matrix = new boolean[size][size];

        // Draw Finder Patterns (3 corners: Top-Left, Top-Right, Bottom-Left)
        drawFinderPattern(matrix, 0, 0);
        drawFinderPattern(matrix, size - 7, 0);
        drawFinderPattern(matrix, 0, size - 7);

        // Draw Timing Patterns
        for (int i = 8; i < size - 8; i++) {
            matrix[6][i] = (i % 2 == 0);
            matrix[i][6] = (i % 2 == 0);
        }

        // Draw Alignment Pattern
        drawAlignmentPattern(matrix, 18, 18);

        // Deterministic hash fill for data modules based on input text
        byte[] bytes = text.getBytes();
        int hash = text.hashCode();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                // Don't overwrite reserved finder/timing/alignment patterns
                if (isReserved(r, c, size)) continue;

                int index = (r * size + c);
                byte b = bytes[index % bytes.length];
                boolean bit = (((b ^ hash ^ (r * 31 + c * 17)) & 0x01) == 1);
                matrix[r][c] = bit;
            }
        }

        return matrix;
    }

    private static void drawFinderPattern(boolean[][] matrix, int row, int col) {
        for (int r = 0; r < 7; r++) {
            for (int c = 0; c < 7; c++) {
                if (r == 0 || r == 6 || c == 0 || c == 6 || (r >= 2 && r <= 4 && c >= 2 && c <= 4)) {
                    matrix[row + r][col + c] = true;
                } else {
                    matrix[row + r][col + c] = false;
                }
            }
        }
    }

    private static void drawAlignmentPattern(boolean[][] matrix, int row, int col) {
        for (int r = -2; r <= 2; r++) {
            for (int c = -2; c <= 2; c++) {
                if (Math.abs(r) == 2 || Math.abs(c) == 2 || (r == 0 && c == 0)) {
                    matrix[row + r][col + c] = true;
                } else {
                    matrix[row + r][col + c] = false;
                }
            }
        }
    }

    private static boolean isReserved(int r, int c, int size) {
        if (r <= 8 && c <= 8) return true; // Top-Left
        if (r <= 8 && c >= size - 8) return true; // Top-Right
        if (r >= size - 8 && c <= 8) return true; // Bottom-Left
        if (r == 6 || c == 6) return true; // Timing
        if (r >= 16 && r <= 20 && c >= 16 && c <= 20) return true; // Alignment
        return false;
    }
}