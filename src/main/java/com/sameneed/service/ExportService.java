package com.sameneed.service;

import com.sameneed.dao.BookingDao;
import com.sameneed.exception.DatabaseException;
import com.sameneed.model.Booking;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExportService {

    private final BookingDao bookingDao = new BookingDao();

    public byte[] exportBookingsCsv(int limit) {
        List<Booking> bookings = bookingDao.findAll(limit, 0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            writer.write("Booking ID,Request ID,Provider,Service,Locality,Scheduled Date,Scheduled Time,Status,Created At\n");
            for (Booking b : bookings) {
                writer.write(escapeCsv(String.valueOf(b.getBookingId())));
                writer.write(",");
                writer.write(escapeCsv(String.valueOf(b.getRequestId())));
                writer.write(",");
                writer.write(escapeCsv(b.getProviderName()));
                writer.write(",");
                writer.write(escapeCsv(b.getServiceName()));
                writer.write(",");
                writer.write(escapeCsv(b.getLocality()));
                writer.write(",");
                writer.write(escapeCsv(b.getScheduledDate() != null ? b.getScheduledDate().toString() : ""));
                writer.write(",");
                writer.write(escapeCsv(b.getScheduledTime() != null ? b.getScheduledTime().toString() : ""));
                writer.write(",");
                writer.write(escapeCsv(b.getStatus() != null ? b.getStatus().name() : ""));
                writer.write(",");
                writer.write(escapeCsv(b.getCreatedAt() != null ? b.getCreatedAt().toString() : ""));
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new DatabaseException("Failed to generate booking export", e);
        }
        return baos.toByteArray();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
