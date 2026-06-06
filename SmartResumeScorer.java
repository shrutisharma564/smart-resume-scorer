import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;

public class SmartResumeScorer extends JFrame {

    static final Color BG_DARK   = new Color(13, 17, 23);
    static final Color BG_CARD   = new Color(22, 27, 34);
    static final Color BG_INPUT  = new Color(33, 38, 45);
    static final Color ACCENT    = new Color(88, 166, 255);
    static final Color CLR_GREEN  = new Color(63, 185, 80);
    static final Color CLR_RED    = new Color(248, 81, 73);
    static final Color CLR_YELLOW = new Color(210, 153, 34);
    static final Color TEXT_MAIN = new Color(230, 237, 243);
    static final Color TEXT_DIM  = new Color(139, 148, 158);
    static final String SAVE_FILE = "resumes.txt";

    List<ResumeData> resumeList = new ArrayList<>();
    JTextField nameField, cgpaField, projField, skillField, jobSkillField;
    JLabel statusBar;
    DefaultTableModel tblModel;
    JTable resultTable;

    static class ResumeData {
        String name;
        float cgpa;
        int projectCount;
        List<String> skillSet;
        int totalScore;
        String decision;

        ResumeData(String name, float cgpa, int projectCount, List<String> skillSet) {
            this.name = name;
            this.cgpa = cgpa;
            this.projectCount = projectCount;
            this.skillSet = skillSet;
            this.totalScore = 0;
            this.decision = "-";
        }
    }

    public SmartResumeScorer() {
        setTitle("Smart Resume Scorer - DAA Project");
        setSize(1100, 680);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());
        loadSavedData();
        buildUI();
        setVisible(true);
    }

    void buildUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_CARD);
        topPanel.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BG_INPUT),
            new EmptyBorder(14, 24, 14, 24)));

        JLabel titleLbl = new JLabel("Smart Resume Scorer");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(ACCENT);

        JLabel subLbl = new JLabel("Greedy Scoring  |  Hash Lookup  |  O(n log n) Sort  -  DAA PBL");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(TEXT_DIM);

        topPanel.add(titleLbl, BorderLayout.WEST);
        topPanel.add(subLbl, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, makeLeftPanel(), makeRightPanel());
        splitPane.setDividerLocation(370);
        splitPane.setDividerSize(4);
        splitPane.setBorder(null);
        splitPane.setBackground(BG_DARK);
        add(splitPane, BorderLayout.CENTER);

        add(makeStatusBar(), BorderLayout.SOUTH);
    }

    JPanel makeLeftPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_DARK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 16));

        panel.add(headingLabel("ADD RESUME MANUALLY"));
        panel.add(Box.createVerticalStrut(12));

        nameField  = makeField("Candidate Name");
        cgpaField  = makeField("CGPA (eg. 8.5)");
        projField  = makeField("Number of Projects");
        skillField = makeField("Skills (eg. java python sql)");

        panel.add(inputBlock("Name", nameField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(inputBlock("CGPA", cgpaField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(inputBlock("Projects", projField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(inputBlock("Skills", skillField));
        panel.add(Box.createVerticalStrut(16));

        JButton addBtn = filledBtn("+ Add Resume");
        addBtn.setAlignmentX(LEFT_ALIGNMENT);
        addBtn.setMaximumSize(new Dimension(9999, 42));
        addBtn.addActionListener(e -> addResume());
        panel.add(addBtn);

        panel.add(Box.createVerticalStrut(24));
        JSeparator divider = new JSeparator();
        divider.setMaximumSize(new Dimension(9999, 1));
        divider.setForeground(BG_INPUT);
        panel.add(divider);
        panel.add(Box.createVerticalStrut(20));

        panel.add(headingLabel("UPLOAD RESUME (PDF)"));
        panel.add(Box.createVerticalStrut(12));

        JButton browseBtn = borderBtn("Browse PDF File");
        browseBtn.setAlignmentX(LEFT_ALIGNMENT);
        browseBtn.setMaximumSize(new Dimension(9999, 42));
        browseBtn.addActionListener(e -> loadPdfFile());
        panel.add(browseBtn);

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    JPanel makeRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(20, 16, 20, 20));

        JPanel topRow = new JPanel(new BorderLayout(10, 0));
        topRow.setBackground(BG_DARK);

        jobSkillField = makeField("Enter required job skills (eg. java python sql docker)");
        jobSkillField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton rankBtn = filledBtn("Evaluate and Rank");
        rankBtn.addActionListener(e -> runEvaluation());

        JButton clearBtn = borderBtn("Clear All");
        clearBtn.addActionListener(e -> clearData());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(BG_DARK);
        btnRow.add(rankBtn);
        btnRow.add(clearBtn);

        topRow.add(jobSkillField, BorderLayout.CENTER);
        topRow.add(btnRow, BorderLayout.EAST);
        panel.add(topRow, BorderLayout.NORTH);

        String[] headers = {"#", "Name", "CGPA", "Projects", "Skills Matched", "Score /100", "Result"};
        tblModel = new DefaultTableModel(headers, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        resultTable = new JTable(tblModel);
        resultTable.setBackground(BG_CARD);
        resultTable.setForeground(TEXT_MAIN);
        resultTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultTable.setRowHeight(38);
        resultTable.setShowGrid(false);
        resultTable.setIntercellSpacing(new Dimension(0, 2));
        resultTable.getTableHeader().setBackground(BG_INPUT);
        resultTable.getTableHeader().setForeground(ACCENT);
        resultTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        resultTable.getTableHeader().setPreferredSize(new Dimension(0, 36));

        resultTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBackground(row % 2 == 0 ? BG_CARD : new Color(28, 33, 40));
                setForeground(TEXT_MAIN);
                setBorder(new EmptyBorder(0, 14, 0, 14));
                if (col == 6 && v != null) {
                    String val = v.toString();
                    if (val.contains("Shortlisted")) { setForeground(CLR_GREEN); setFont(getFont().deriveFont(Font.BOLD)); }
                    else if (val.contains("Hold"))   { setForeground(CLR_YELLOW); }
                    else if (val.contains("Rejected")){ setForeground(CLR_RED); }
                }
                if (col == 0) { setHorizontalAlignment(CENTER); setForeground(ACCENT); }
                if (col == 5) { setFont(getFont().deriveFont(Font.BOLD)); }
                if (sel) setBackground(new Color(88, 166, 255, 40));
                return this;
            }
        });

        int[] colWidths = {40, 160, 65, 75, 170, 90, 130};
        for (int i = 0; i < colWidths.length; i++)
            resultTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        JScrollPane scrollArea = new JScrollPane(resultTable);
        scrollArea.setBackground(BG_CARD);
        scrollArea.getViewport().setBackground(BG_CARD);
        scrollArea.setBorder(new LineBorder(BG_INPUT, 1, true));
        panel.add(scrollArea, BorderLayout.CENTER);
        return panel;
    }

    void loadPdfFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = chooser.getSelectedFile();
        updateStatus("Reading PDF: " + selectedFile.getName() + " ...");

        SwingWorker<ResumeData, Void> worker = new SwingWorker<>() {
            @Override
            protected ResumeData doInBackground() throws Exception {
                return parsePdfResume(selectedFile);
            }
            @Override
            protected void done() {
                try {
                    ResumeData rd = get();
                    if (rd == null) {
                        showAlert("Could not read data from PDF. Please fill manually.");
                        updateStatus("PDF read failed: " + selectedFile.getName());
                        return;
                    }
                    nameField.setText(rd.name);
                    cgpaField.setText(rd.cgpa > 0 ? String.valueOf(rd.cgpa) : "");
                    projField.setText(String.valueOf(rd.projectCount));
                    skillField.setText(String.join(" ", rd.skillSet));
                    updateStatus("PDF loaded: " + selectedFile.getName()
                        + " | Name: " + rd.name
                        + " | CGPA: " + (rd.cgpa > 0 ? rd.cgpa : "not found")
                        + " | Projects: " + rd.projectCount
                        + " | Skills: " + rd.skillSet.size());
                    JOptionPane.showMessageDialog(SmartResumeScorer.this,
                        "<html><b>PDF Data Extracted</b><br><br>"
                        + "Name: <b>" + rd.name + "</b><br>"
                        + "CGPA: <b>" + (rd.cgpa > 0 ? rd.cgpa : "Not found - fill manually") + "</b><br>"
                        + "Projects: <b>" + rd.projectCount + "</b><br>"
                        + "Skills: <b>" + String.join(", ", rd.skillSet) + "</b><br><br>"
                        + "Fields filled. Click Add Resume to save.</html>",
                        "PDF Loaded", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    showAlert("Error: " + ex.getMessage());
                    updateStatus("Error while reading PDF.");
                }
            }
        };
        worker.execute();
    }

    ResumeData parsePdfResume(File f) {
        try (PDDocument doc = Loader.loadPDF(f)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String rawText = stripper.getText(doc);
            String nm   = parseName(rawText);
            float  cg   = parseCGPA(rawText);
            int    proj = parseProjects(rawText);
            List<String> sk = parseSkills(rawText);
            return new ResumeData(nm, cg, proj, sk);
        } catch (Exception e) {
            System.err.println("PDF error: " + e.getMessage());
            return null;
        }
    }

    String parseName(String text) {
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

    float parseCGPA(String text) {
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

    int parseProjects(String text) {
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

    List<String> parseSkills(String text) {
        Set<String> found = new LinkedHashSet<>();
        String[] knownTech = {
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

        String ltext = text.toLowerCase();

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

        for (String kw : knownTech) {
            if (ltext.contains(kw)) found.add(kw.replaceAll("\\s+", ""));
        }

        return new ArrayList<>(found);
    }

    void addResume() {
        try {
            String nm = nameField.getText().trim();
            if (nm.isEmpty()) { showAlert("Name field is empty!"); return; }

            String cgStr   = cgpaField.getText().trim();
            String prStr   = projField.getText().trim();
            String skStr   = skillField.getText().trim();

            if (cgStr.isEmpty() || prStr.isEmpty() || skStr.isEmpty()) {
                showAlert("Please fill all fields before adding.");
                return;
            }

            for (ResumeData existing : resumeList) {
                if (existing.name.equalsIgnoreCase(nm)) {
                    int ch = JOptionPane.showConfirmDialog(this,
                        "Resume for \"" + nm + "\" already exists. Add anyway?",
                        "Duplicate Found", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (ch != JOptionPane.YES_OPTION) return;
                    break;
                }
            }

            float cg = Float.parseFloat(cgStr);
            int pr   = Integer.parseInt(prStr);

            if (cg < 0 || cg > 10) { showAlert("CGPA must be between 0 and 10."); return; }

            List<String> sk = Arrays.asList(skStr.toLowerCase().trim().split("\\s+"));
            ResumeData rd = new ResumeData(nm, cg, pr, sk);
            resumeList.add(rd);
            saveData();
            refreshTable(null);
            updateStatus("Resume added: " + nm + "  (Total: " + resumeList.size() + ")");
            nameField.setText(""); cgpaField.setText(""); projField.setText(""); skillField.setText("");

        } catch (NumberFormatException ex) {
            showAlert("CGPA and Projects must be numeric values.");
        }
    }

    void runEvaluation() {
        if (resumeList.isEmpty()) { showAlert("No resumes to evaluate. Please add resumes first."); return; }

        String jobInput = jobSkillField.getText().trim().toLowerCase();
        if (jobInput.isEmpty()) { showAlert("Enter required job skills first."); return; }

        List<String> jobSkills = Arrays.asList(jobInput.split("\\s+"));
        Set<String> jobSet = new HashSet<>(jobSkills);

        for (ResumeData rd : resumeList) {
            int matched = 0;
            for (String s : rd.skillSet) if (jobSet.contains(s)) matched++;

            int skillPts   = jobSkills.size() > 0 ? (matched * 40) / jobSkills.size() : 0;
            int cgpaPts    = (int)((rd.cgpa / 10.0) * 30);
            int projPts    = Math.min(rd.projectCount * 10, 30);
            rd.totalScore  = skillPts + cgpaPts + projPts;

            if (rd.totalScore >= 70)      rd.decision = "Shortlisted";
            else if (rd.totalScore >= 45) rd.decision = "On Hold";
            else                          rd.decision = "Rejected";
        }

        resumeList.sort((a, b) -> b.totalScore - a.totalScore);
        refreshTable(jobSet);

        long sl = resumeList.stream().filter(r -> r.decision.contains("Shortlisted")).count();
        long ho = resumeList.stream().filter(r -> r.decision.contains("Hold")).count();
        long rj = resumeList.stream().filter(r -> r.decision.contains("Rejected")).count();
        updateStatus(String.format("Evaluation done: %d total | Shortlisted: %d | On Hold: %d | Rejected: %d",
            resumeList.size(), sl, ho, rj));
    }

    void refreshTable(Set<String> jobSet) {
        tblModel.setRowCount(0);
        for (int i = 0; i < resumeList.size(); i++) {
            ResumeData rd = resumeList.get(i);
            String matchStr = "-";
            if (jobSet != null) {
                long mc = rd.skillSet.stream().filter(jobSet::contains).count();
                matchStr = mc + "/" + jobSet.size();
            }
            tblModel.addRow(new Object[]{
                i + 1, rd.name, rd.cgpa, rd.projectCount, matchStr, rd.totalScore, rd.decision
            });
        }
    }

    void clearData() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "This will delete all resume data. Continue?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == 0) {
            resumeList.clear();
            tblModel.setRowCount(0);
            new File(SAVE_FILE).delete();
            updateStatus("All data cleared.");
        }
    }

    void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SAVE_FILE))) {
            for (ResumeData rd : resumeList) {
                pw.println(rd.name + "|" + rd.cgpa + "|" + rd.projectCount + "|" + String.join(",", rd.skillSet));
            }
        } catch (Exception e) {
            updateStatus("Save error: " + e.getMessage());
        }
    }

    void loadSavedData() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length < 4) continue;
                String nm = parts[0];
                float cg  = Float.parseFloat(parts[1]);
                int pr    = Integer.parseInt(parts[2]);
                List<String> sk = Arrays.asList(parts[3].split(","));
                resumeList.add(new ResumeData(nm, cg, pr, sk));
            }
        } catch (Exception e) {}
    }

    JPanel makeStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BG_INPUT),
            new EmptyBorder(7, 18, 7, 18)));
        statusBar = new JLabel("Ready  |  " + resumeList.size() + " resume(s) loaded.");
        statusBar.setForeground(TEXT_DIM);
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bar.add(statusBar, BorderLayout.WEST);
        return bar;
    }

    JLabel headingLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_DIM);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    JPanel inputBlock(String label, JTextField field) {
        JPanel block = new JPanel(new BorderLayout(0, 4));
        block.setBackground(BG_DARK);
        block.setAlignmentX(LEFT_ALIGNMENT);
        block.setMaximumSize(new Dimension(9999, 58));
        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_DIM);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        block.add(lbl, BorderLayout.NORTH);
        block.add(field, BorderLayout.CENTER);
        return block;
    }

    JTextField makeField(String hint) {
        JTextField f = new JTextField();
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(ACCENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(
            new LineBorder(new Color(48, 54, 61), 1, true),
            new EmptyBorder(6, 10, 6, 10)));
        f.setMaximumSize(new Dimension(9999, 38));
        f.setAlignmentX(LEFT_ALIGNMENT);
        return f;
    }

    JButton filledBtn(String label) {
        JButton b = new JButton(label);
        b.setBackground(ACCENT);
        b.setForeground(BG_DARK);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 20, 8, 20));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(120, 185, 255)); }
            public void mouseExited(MouseEvent e)  { b.setBackground(ACCENT); }
        });
        return b;
    }

    JButton borderBtn(String label) {
        JButton b = new JButton(label);
        b.setBackground(BG_DARK);
        b.setForeground(ACCENT);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setBorder(new CompoundBorder(new LineBorder(ACCENT, 1, true), new EmptyBorder(6, 16, 6, 16)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(BG_INPUT); }
            public void mouseExited(MouseEvent e)  { b.setBackground(BG_DARK); }
        });
        return b;
    }

    void updateStatus(String msg) {
        statusBar.setForeground(TEXT_MAIN);
        statusBar.setText(msg);
    }

    void showAlert(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SmartResumeScorer::new);
    }
}
