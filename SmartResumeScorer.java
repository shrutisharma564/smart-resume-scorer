package com.daa.resumescorer.ui;

import com.daa.resumescorer.db.CandidateDAO;
import com.daa.resumescorer.db.EvaluationDAO;
import com.daa.resumescorer.model.Candidate;
import com.daa.resumescorer.model.User;
import com.daa.resumescorer.util.ExportUtil;
import com.daa.resumescorer.util.PdfParser;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

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

    private final CandidateDAO candidateDAO = new CandidateDAO();
    private final EvaluationDAO evaluationDAO = new EvaluationDAO();
    private final PdfParser pdfParser = new PdfParser();

    private final User currentUser;
    List<Candidate> candidateList = new ArrayList<>();
    JTextField nameField, emailField, phoneField, cgpaField, projField, skillField, jobSkillField;
    JLabel statusBar;
    DefaultTableModel tblModel;
    JTable resultTable;

    public SmartResumeScorer(User user) {
        this.currentUser = user;
        setTitle("Smart Resume Scorer - DAA Project (MySQL Edition)");
        setSize(1150, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());
        loadFromDatabase();
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

        JLabel subLbl = new JLabel("Greedy Scoring | Hash Lookup | O(n log n) Sort | MySQL/JDBC  -  Signed in as "
                + currentUser.getUsername());
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
        emailField = makeField("Email (eg. name@gmail.com)");
        phoneField = makeField("Phone (eg. 9876543210)");
        cgpaField  = makeField("CGPA (eg. 8.5)");
        projField  = makeField("Number of Projects");
        skillField = makeField("Skills (eg. java python sql)");

        panel.add(inputBlock("Name", nameField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(inputBlock("Email", emailField));
        panel.add(Box.createVerticalStrut(10));
        panel.add(inputBlock("Phone", phoneField));
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

        panel.add(Box.createVerticalStrut(24));
        JSeparator divider2 = new JSeparator();
        divider2.setMaximumSize(new Dimension(9999, 1));
        divider2.setForeground(BG_INPUT);
        panel.add(divider2);
        panel.add(Box.createVerticalStrut(20));

        panel.add(headingLabel("VIEW HISTORY"));
        panel.add(Box.createVerticalStrut(12));
        JButton historyBtn = borderBtn("Selected Candidate's History");
        historyBtn.setAlignmentX(LEFT_ALIGNMENT);
        historyBtn.setMaximumSize(new Dimension(9999, 42));
        historyBtn.addActionListener(e -> showHistory());
        panel.add(historyBtn);

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

        JButton exportBtn = borderBtn("Export");
        exportBtn.addActionListener(e -> showExportMenu(exportBtn));

        JButton clearBtn = borderBtn("Clear All");
        clearBtn.addActionListener(e -> clearData());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(BG_DARK);
        btnRow.add(rankBtn);
        btnRow.add(exportBtn);
        btnRow.add(clearBtn);

        topRow.add(jobSkillField, BorderLayout.CENTER);
        topRow.add(btnRow, BorderLayout.EAST);
        panel.add(topRow, BorderLayout.NORTH);

        String[] headers = {"#", "Name", "Email", "Phone", "CGPA", "Projects", "Skills Matched", "Score /100", "Result"};
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
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
                if (col == 8 && v != null) {
                    String val = v.toString();
                    if (val.contains("Shortlisted")) { setForeground(CLR_GREEN); setFont(getFont().deriveFont(Font.BOLD)); }
                    else if (val.contains("Hold"))   { setForeground(CLR_YELLOW); }
                    else if (val.contains("Rejected")){ setForeground(CLR_RED); }
                }
                if (col == 0) { setHorizontalAlignment(CENTER); setForeground(ACCENT); }
                if (col == 7) { setFont(getFont().deriveFont(Font.BOLD)); }
                if (sel) setBackground(new Color(88, 166, 255, 40));
                return this;
            }
        });

        int[] colWidths = {35, 130, 150, 100, 55, 65, 150, 80, 120};
        for (int i = 0; i < colWidths.length; i++)
            resultTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        JScrollPane scrollArea = new JScrollPane(resultTable);
        scrollArea.setBackground(BG_CARD);
        scrollArea.getViewport().setBackground(BG_CARD);
        scrollArea.setBorder(new LineBorder(BG_INPUT, 1, true));
        panel.add(scrollArea, BorderLayout.CENTER);
        return panel;
    }

    // ---------------------------------------------------------------- PDF

    void loadPdfFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = chooser.getSelectedFile();
        updateStatus("Reading PDF: " + selectedFile.getName() + " ...");

        SwingWorker<Candidate, Void> worker = new SwingWorker<>() {
            @Override
            protected Candidate doInBackground() {
                return pdfParser.parse(selectedFile);
            }
            @Override
            protected void done() {
                try {
                    Candidate rd = get();
                    if (rd == null) {
                        showAlert("Could not read data from PDF. Please fill manually.");
                        updateStatus("PDF read failed: " + selectedFile.getName());
                        return;
                    }
                    nameField.setText(rd.getName());
                    emailField.setText(rd.getEmail());
                    phoneField.setText(rd.getPhone());
                    cgpaField.setText(rd.getCgpa() > 0 ? String.valueOf(rd.getCgpa()) : "");
                    projField.setText(String.valueOf(rd.getProjectCount()));
                    skillField.setText(String.join(" ", rd.getSkillSet()));
                    updateStatus("PDF loaded: " + selectedFile.getName()
                        + " | Name: " + rd.getName()
                        + " | Email: " + (rd.getEmail().isEmpty() ? "not found" : rd.getEmail())
                        + " | Phone: " + (rd.getPhone().isEmpty() ? "not found" : rd.getPhone())
                        + " | CGPA: " + (rd.getCgpa() > 0 ? rd.getCgpa() : "not found")
                        + " | Projects: " + rd.getProjectCount()
                        + " | Skills: " + rd.getSkillSet().size());
                    JOptionPane.showMessageDialog(SmartResumeScorer.this,
                        "<html><b>PDF Data Extracted</b><br><br>"
                        + "Name: <b>" + rd.getName() + "</b><br>"
                        + "Email: <b>" + (rd.getEmail().isEmpty() ? "Not found" : rd.getEmail()) + "</b><br>"
                        + "Phone: <b>" + (rd.getPhone().isEmpty() ? "Not found" : rd.getPhone()) + "</b><br>"
                        + "CGPA: <b>" + (rd.getCgpa() > 0 ? rd.getCgpa() : "Not found - fill manually") + "</b><br>"
                        + "Projects: <b>" + rd.getProjectCount() + "</b><br>"
                        + "Skills: <b>" + String.join(", ", rd.getSkillSet()) + "</b><br><br>"
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

    // ---------------------------------------------------------------- CRUD

    void addResume() {
        try {
            String nm = nameField.getText().trim();
            if (nm.isEmpty()) { showAlert("Name field is empty!"); return; }

            String cgStr = cgpaField.getText().trim();
            String prStr = projField.getText().trim();
            String skStr = skillField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (cgStr.isEmpty() || prStr.isEmpty() || skStr.isEmpty()) {
                showAlert("Please fill all fields before adding.");
                return;
            }

            if (candidateDAO.nameExists(nm)) {
                int ch = JOptionPane.showConfirmDialog(this,
                    "Resume for \"" + nm + "\" already exists. Add anyway?",
                    "Duplicate Found", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (ch != JOptionPane.YES_OPTION) return;
            }

            float cg = Float.parseFloat(cgStr);
            int pr   = Integer.parseInt(prStr);

            if (cg < 0 || cg > 10) { showAlert("CGPA must be between 0 and 10."); return; }

            List<String> sk = Arrays.asList(skStr.toLowerCase().trim().split("\\s+"));
            Candidate c = new Candidate(nm, email, phone, cg, pr, sk);

            candidateDAO.insertCandidate(c);
            candidateList.add(c);
            refreshTable(null);
            updateStatus("Resume saved to MySQL: " + nm + "  (Total: " + candidateList.size() + ")");
            nameField.setText(""); emailField.setText(""); phoneField.setText("");
            cgpaField.setText(""); projField.setText(""); skillField.setText("");

        } catch (NumberFormatException ex) {
            showAlert("CGPA and Projects must be numeric values.");
        } catch (SQLException ex) {
            showAlert("Database error while saving: " + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------- Evaluate

    void runEvaluation() {
        if (candidateList.isEmpty()) { showAlert("No resumes to evaluate. Please add resumes first."); return; }

        String jobInput = jobSkillField.getText().trim().toLowerCase();
        if (jobInput.isEmpty()) { showAlert("Enter required job skills first."); return; }

        List<String> jobSkills = Arrays.asList(jobInput.split("\\s+"));
        Set<String> jobSet = new HashSet<>(jobSkills); // O(1) hash lookup per skill check

        for (Candidate c : candidateList) {
            int matched = 0;
            for (String s : c.getSkillSet()) if (jobSet.contains(s)) matched++;

            int skillPts = jobSkills.size() > 0 ? (matched * 40) / jobSkills.size() : 0;
            int cgpaPts  = (int) ((c.getCgpa() / 10.0) * 30);
            int projPts  = Math.min(c.getProjectCount() * 10, 30);
            int total    = skillPts + cgpaPts + projPts;
            c.setTotalScore(total);

            String decision;
            if (total >= 70)      decision = "Shortlisted";
            else if (total >= 45) decision = "On Hold";
            else                   decision = "Rejected";
            c.setDecision(decision);

            try {
                evaluationDAO.saveEvaluation(c.getId(), jobInput, skillPts, cgpaPts, projPts, total, decision);
            } catch (SQLException ex) {
                System.err.println("Could not save evaluation history: " + ex.getMessage());
            }
        }

        candidateList.sort((a, b) -> b.getTotalScore() - a.getTotalScore()); // O(n log n) sort
        refreshTable(jobSet);

        long sl = candidateList.stream().filter(r -> r.getDecision().contains("Shortlisted")).count();
        long ho = candidateList.stream().filter(r -> r.getDecision().contains("Hold")).count();
        long rj = candidateList.stream().filter(r -> r.getDecision().contains("Rejected")).count();
        updateStatus(String.format("Evaluation done & saved to history: %d total | Shortlisted: %d | On Hold: %d | Rejected: %d",
            candidateList.size(), sl, ho, rj));
    }

    void refreshTable(Set<String> jobSet) {
        tblModel.setRowCount(0);
        for (int i = 0; i < candidateList.size(); i++) {
            Candidate c = candidateList.get(i);
            String matchStr = "-";
            if (jobSet != null) {
                long mc = c.getSkillSet().stream().filter(jobSet::contains).count();
                matchStr = mc + "/" + jobSet.size();
            }
            tblModel.addRow(new Object[]{
                i + 1, c.getName(), c.getEmail(), c.getPhone(), c.getCgpa(), c.getProjectCount(),
                matchStr, c.getTotalScore(), c.getDecision()
            });
        }
    }

    void showHistory() {
        int row = resultTable.getSelectedRow();
        if (row == -1) { showAlert("Select a candidate row first."); return; }
        Candidate c = candidateList.get(row);
        if (c.getId() <= 0) { showAlert("This candidate hasn't been saved yet."); return; }
        new HistoryDialog(this, c.getId(), c.getName()).setVisible(true);
    }

    void clearData() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "This will permanently delete all candidates from the MySQL database. Continue?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == 0) {
            try {
                candidateDAO.deleteAllCandidates();
                candidateList.clear();
                tblModel.setRowCount(0);
                updateStatus("All data cleared from database.");
            } catch (SQLException ex) {
                showAlert("Could not clear database: " + ex.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------- Export

    void showExportMenu(Component anchor) {
        if (candidateList.isEmpty()) { showAlert("Nothing to export yet."); return; }
        JPopupMenu menu = new JPopupMenu();
        JMenuItem csvItem = new JMenuItem("Export as CSV (Excel)");
        JMenuItem pdfItem = new JMenuItem("Export as PDF report");
        csvItem.addActionListener(e -> doExport(true));
        pdfItem.addActionListener(e -> doExport(false));
        menu.add(csvItem);
        menu.add(pdfItem);
        menu.show(anchor, 0, anchor.getHeight());
    }

    void doExport(boolean csv) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(csv ? "resume_results.csv" : "resume_report.pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File out = chooser.getSelectedFile();
        try {
            if (csv) ExportUtil.exportCsv(candidateList, out);
            else ExportUtil.exportPdfReport(candidateList, out);
            updateStatus("Exported to " + out.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Export failed: " + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------- Data load

    void loadFromDatabase() {
        try {
            candidateList = candidateDAO.getAllCandidates();
        } catch (SQLException e) {
            candidateList = new ArrayList<>();
            JOptionPane.showMessageDialog(this,
                "Could not load data from MySQL:\n" + e.getMessage()
                + "\n\nCheck db.properties and make sure MySQL is running.",
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------------------------------------------------------- UI helpers (unchanged styling)

    JPanel makeStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BG_INPUT),
            new EmptyBorder(7, 18, 7, 18)));
        statusBar = new JLabel("Ready  |  " + candidateList.size() + " candidate(s) loaded from MySQL.");
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
}
