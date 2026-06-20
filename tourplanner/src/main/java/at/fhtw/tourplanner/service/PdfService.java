package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.entity.TourEntity;
import at.fhtw.tourplanner.entity.TourLogEntity;
import at.fhtw.tourplanner.repository.TourLogRepository;
import at.fhtw.tourplanner.repository.TourRepository;
import at.fhtw.tourplanner.util.LoggerUtil;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PdfService {

    private static final Logger log = LoggerUtil.getLogger(PdfService.class);

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourLogRepository tourLogRepository;

    public byte[] generateTourReport(UUID tourId, UUID userId) {
        TourEntity tour = tourRepository.findByIdAndUserId(tourId, userId).orElse(null);
        if (tour == null) {
            log.warn("Tour not found or access denied: tourId={}, userId={}", tourId, userId);
            return null;
        }

        List<TourLogEntity> logs = new ArrayList<>(tourLogRepository.findByTourId(tourId));
        logs.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));

        log.info("Generating PDF report for tour '{}' ({} logs)", tour.getName(), logs.size());

        try {
            return buildPdf(tour, logs);
        } catch (Exception e) {
            log.error("Failed to generate PDF for tour '{}': {}", tour.getName(), e.getMessage());
            return null;
        }
    }

    private byte[] buildPdf(TourEntity tour, List<TourLogEntity> logs) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
        Font subtitleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font smallFont = new Font(Font.HELVETICA, 8, Font.NORMAL);

        document.add(new Paragraph("Tour Summary Report", titleFont));
        document.add(new Paragraph(" "));

        Paragraph namePara = new Paragraph(tour.getName(), subtitleFont);
        namePara.setSpacingAfter(10f);
        document.add(namePara);

        PdfPTable detailsTable = new PdfPTable(2);
        detailsTable.setWidthPercentage(100);
        detailsTable.setSpacingAfter(20f);

        addDetailCell(detailsTable, "Description", tour.getDescription(), bodyFont);
        addDetailCell(detailsTable, "Transport Type", tour.getTransportType(), bodyFont);
        addDetailCell(detailsTable, "From", tour.getFromLocation(), bodyFont);
        addDetailCell(detailsTable, "To", tour.getToLocation(), bodyFont);
        addDetailCell(detailsTable, "Distance", String.format("%.1f km", tour.getDistance()), bodyFont);
        addDetailCell(detailsTable, "Estimated Time",
                tour.getEstimatedTime() != null ? tour.getEstimatedTime() : "N/A", bodyFont);
        addDetailCell(detailsTable, "Popularity Score", tour.getPopularityScore() + "%", bodyFont);
        addDetailCell(detailsTable, "Child-Friendliness", tour.getChildFriendliness() + "%", bodyFont);

        document.add(detailsTable);

        if (logs.isEmpty()) {
            document.add(new Paragraph("No tour logs recorded yet.", bodyFont));
        } else {
            document.add(new Paragraph("Tour Logs", subtitleFont));
            document.add(new Paragraph(" "));

            PdfPTable logTable = new PdfPTable(6);
            logTable.setWidthPercentage(100);
            logTable.setWidths(new float[]{1.5f, 1f, 1f, 1f, 0.8f, 2.5f});
            logTable.setSpacingAfter(15f);

            addHeaderCell(logTable, "Date", headerFont);
            addHeaderCell(logTable, "Difficulty", headerFont);
            addHeaderCell(logTable, "Distance", headerFont);
            addHeaderCell(logTable, "Time", headerFont);
            addHeaderCell(logTable, "Rating", headerFont);
            addHeaderCell(logTable, "Comment", headerFont);

            for (TourLogEntity log : logs) {
                addBodyCell(logTable, log.getDateTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), bodyFont);
                addBodyCell(logTable, log.getDifficulty().name(), bodyFont);
                addBodyCell(logTable, String.format("%.1f km", log.getTotalDistance()), bodyFont);
                addBodyCell(logTable, formatMinutes(log.getTotalTime()), bodyFont);
                addBodyCell(logTable, log.getRating() + "/5", bodyFont);
                addBodyCell(logTable, log.getComment() != null ? log.getComment() : "", smallFont);
            }
        }

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Generated on " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), smallFont));

        document.close();
        return baos.toByteArray();
    }

    private void addDetailCell(PdfPTable table, String label, String value, Font bodyFont) {
        Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(4f);
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setLeading(0f, 1.2f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", bodyFont));
        valueCell.setPadding(4f);
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setLeading(0f, 1.2f);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBackgroundColor(new Color(220, 220, 220));
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5f);
        cell.setLeading(0f, 1.1f);
        table.addCell(cell);
    }

    private String formatMinutes(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        if (h > 0) {
            return h + "h " + m + "min";
        }
        return m + " min";
    }
}
