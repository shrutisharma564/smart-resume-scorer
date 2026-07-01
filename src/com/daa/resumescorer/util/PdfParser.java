package com.daa.resumescorer.util;

import com.daa.resumescorer.model.Candidate;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pulls name / CGPA / project-count / skills out of a resume PDF using
 * heuristics (regex + keyword scanning). Same logic as the original app,
 * just moved into its own class.
 */
public class PdfParser {

    private static final String[] KNOWN_TECH = {
        "java", "python", "c++", "c", "javascript", "js", "html", "css",
        "sql", "mysql", "sqlite", "mongodb", "postgresql",
        "flask", "django", "spring", "react", "node", "nodejs",
        "git", "github", "docker", "kubernetes", "linux",
        "machine learning", "ml", "nlp", "nltk", "tensorflow", "pytorch",
        "data structures", "dsa", "algorithms", "oop",
        "aws", "azure", "gcp", "cloud",
        "android", "kotlin", "swift", "r", "matlab",
        "vscode", "eclipse", "intellij"
    };

    public Candidate parse(File f) {
        try (PDDocument doc = Loader.loadPDF(f)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(doc);
            String name      = parseName(rawText);
            String email     = extractEmail(rawText);
            String phone     = extractPhone(rawText);
            float cgpa       = parseCGPA(rawText);
            int projectCount = parseProjects(rawText);
            List<String> skills = parseSkills(rawText);
            return new Candidate(name, email, phone, cgpa, projectCount, skills);
        } catch (Exception e) {
            System.err.println("PDF error: " + e.getMessage());
            return null;
        }
    }

    /** Finds the first email-looking string in the resume text. */
    public String extractEmail(String text) {
        Matcher m = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matcher(text);
        return m.find() ? m.group() : "";
    }

    /** Finds the first phone-number-looking string (handles +91, spaces, dashes, 10-digit numbers). */
    public String extractPhone(String text) {
        Matcher m = Pattern.compile("(\\+?\\d{1,3}[-\\s]?)?\\d{10}|(\\+?\\d{1,3}[-\\s]?)?\\d{3}[-\\s]\\d{3}[-\\s]\\d{4}").matcher(text);
        if (m.find()) return m.group().trim();
        return "";
    }

    private String parseName(String text) {
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.matches(".*[+\\d]{7,}.*")) continue;
            if (line.toLowerCase().contains("@")) continue;
            if (line.toLowerCase().contains("linkedin")) continue;
            if (line.toLowerCase().contains("github")) continue;
            if (line.length() > 2 && line.length() < 50) {
                return line.replaceAll("[^\\p{L}\\s]", "").trim();
            }
        }
        return "Unknown";
    }

    private float parseCGPA(String text) {
        Matcher m1 = Pattern.compile("(\\d\\.\\d)\\s*/\\s*10").matcher(text);
        if (m1.find()) return Float.parseFloat(m1.group(1));

        Matcher m2 = Pattern.compile("(?i)cgpa[:\\s]+([0-9]+(\\.[0-9]+)?)").matcher(text);
        if (m2.find()) return Float.parseFloat(m2.group(1));

        Matcher m3 = Pattern.compile("(?i)gpa[:\\s]+([0-9]+(\\.[0-9]+)?)").matcher(text);
        if (m3.find()) return Float.parseFloat(m3.group(1));

        Matcher m4 = Pattern.compile("([0-9]+(\\.[0-9]+)?)\\s*(?i)cgpa").matcher(text);
        if (m4.find()) return Float.parseFloat(m4.group(1));

        Matcher m5 = Pattern.compile("(\\d{2,3})\\s*%").matcher(text);
        if (m5.find()) {
            float pct = Float.parseFloat(m5.group(1));
            return pct / 10.0f;
        }
        return 0.0f;
    }

    private int parseProjects(String text) {
        String lower = text.toLowerCase();
        int startIdx = lower.indexOf("projects");
        if (startIdx == -1) startIdx = lower.indexOf("project");
        if (startIdx == -1) return 0;

        String section = text.substring(startIdx);
        String[] stops = {"technical skills", "certifications", "experience", "education", "achievements"};
        int endIdx = section.length();
        for (String sw : stops) {
            int si = section.toLowerCase().indexOf(sw);
            if (si > 10 && si < endIdx) endIdx = si;
        }
        section = section.substring(0, endIdx);

        String[] lines = section.split("\\r?\\n");
        int cnt = 0;
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.toLowerCase().startsWith("project")) continue;
            if ((line.contains("\u2013") || line.contains("-")) && line.length() > 15 && !line.startsWith("\u2022")) {
                cnt++;
            }
        }
        if (cnt == 0) {
            long bullets = Arrays.stream(lines)
                .filter(l -> l.trim().startsWith("\u2022") || l.trim().startsWith("-"))
                .count();
            cnt = (int) Math.max(1, bullets / 4);
        }
        return Math.max(cnt, 0);
    }

    private List<String> parseSkills(String text) {
        Set<String> found = new LinkedHashSet<>();

        Matcher lm = Pattern.compile("(?i)languages\\s*[:\\-]\\s*(.+)").matcher(text);
        if (lm.find()) {
            for (String p : lm.group(1).split("[,;/]")) {
                String s = p.trim().toLowerCase().replaceAll("[^a-z0-9+#]", "");
                if (!s.isEmpty() && s.length() > 1) found.add(s);
            }
        }

        Matcher tm = Pattern.compile("(?i)(libraries|tools|frameworks)[\\s&A-Za-z]*[:\\-]\\s*(.+)").matcher(text);
        if (tm.find()) {
            for (String p : tm.group(2).split("[,;/]")) {
                String s = p.trim().toLowerCase().replaceAll("[^a-z0-9+#.]", "");
                if (!s.isEmpty() && s.length() > 1) found.add(s);
            }
        }

        String ltext = text.toLowerCase();
        for (String kw : KNOWN_TECH) {
            if (ltext.contains(kw)) found.add(kw.replaceAll("\\s+", ""));
        }

        return new ArrayList<>(found);
    }
}
