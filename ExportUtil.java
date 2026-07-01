package com.daa.resumescorer.util;

import com.daa.resumescorer.model.Candidate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports the ranked candidate list to CSV (opens fine in Excel) and to a
 * simple one-page-per-batch PDF report, using the pdfbox-app jar that is
 * already part of this project (no extra dependency needed).
 */
public class ExportUtil {

    public static void exportCsv(List<Candidate> candidates, File outFile) throws IOException {
        try (FileWriter w = new FileWriter(outFile)) {
            w.write("Rank,Name,Email,Phone,CGPA,Projects,Skills,Score,Decision\n");
            int rank = 1;
            for (Candidate c : candidates) {
                w.write(String.format("%d,%s,%s,%s,%.2f,%d,\"%s\",%d,%s\n",
                        rank++, escapeCsv(c.getName()), escapeCsv(c.getEmail()), escapeCsv(c.getPhone()),
                        c.getCgpa(), c.getProjectCount(),
                        String.join("; ", c.getSkillSet()), c.getTotalScore(), c.getDecision()));
            }
        }
    }

    private static String escapeCsv(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }

    public static void exportPdfReport(List<Candidate> candidates, File outFile) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontReg  = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            float margin = 40;
            float y = page.getMediaBox().getHeight() - margin;
            float lineHeight = 16;

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            cs.setFont(fontBold, 16);
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText("Smart Resume Scorer - Evaluation Report");
            cs.endText();
            y -= lineHeight * 1.5f;

            cs.setFont(fontReg, 10);
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")));
            cs.endText();
            y -= lineHeight * 1.5f;

            cs.setFont(fontBold, 10);
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText(String.format("%-4s %-18s %-22s %-6s %-6s %-8s %-12s", "Rnk", "Name", "Email", "CGPA", "Proj", "Score", "Decision"));
            cs.endText();
            y -= lineHeight;

            cs.setFont(fontReg, 10);
            int rank = 1;
            for (Candidate c : candidates) {
                if (y < margin + lineHeight) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    cs.setFont(fontReg, 10);
                    y = page.getMediaBox().getHeight() - margin;
                }
                String row = String.format("%-4d %-18s %-22s %-6.2f %-6d %-8d %-12s",
                        rank++, truncate(c.getName(), 18), truncate(c.getEmail(), 22), c.getCgpa(), c.getProjectCount(),
                        c.getTotalScore(), c.getDecision());
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText(row);
                cs.endText();
                y -= lineHeight;
            }
            cs.close();
            doc.save(outFile);
        }
    }

    private static String truncate(String s, int len) {
        if (s == null) return "";
        return s.length() <= len ? s : s.substring(0, len - 1) + "\u2026";
    }
}
