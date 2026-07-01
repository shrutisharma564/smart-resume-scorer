package com.daa.resumescorer.ui;

import com.daa.resumescorer.db.EvaluationDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class HistoryDialog extends JDialog {

    public HistoryDialog(Frame owner, int candidateId, String candidateName) {
        super(owner, "Evaluation History - " + candidateName, true);
        setSize(560, 360);
        setLocationRelativeTo(owner);

        String[] cols = {"Job Skills Used", "Skill Pts", "CGPA Pts", "Proj Pts", "Total", "Decision", "When"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        EvaluationDAO dao = new EvaluationDAO();
        try {
            List<EvaluationDAO.EvalRecord> records = dao.getHistoryForCandidate(candidateId);
            if (records.isEmpty()) {
                model.addRow(new Object[]{"No evaluation history yet.", "", "", "", "", "", ""});
            }
            for (EvaluationDAO.EvalRecord r : records) {
                model.addRow(new Object[]{
                    r.jobSkillsInput, r.skillPoints, r.cgpaPoints, r.projectPoints,
                    r.totalScore, r.decision, r.evaluatedAt
                });
            }
        } catch (SQLException e) {
            model.addRow(new Object[]{"Error loading history: " + e.getMessage(), "", "", "", "", "", ""});
        }

        JTable table = new JTable(model);
        table.setRowHeight(26);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
