package com.daa.resumescorer.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EvaluationDAO {

    public static class EvalRecord {
        public String jobSkillsInput;
        public int skillPoints, cgpaPoints, projectPoints, totalScore;
        public String decision;
        public Timestamp evaluatedAt;
    }

    public void saveEvaluation(int candidateId, String jobSkillsInput,
                                int skillPts, int cgpaPts, int projPts,
                                int totalScore, String decision) throws SQLException {
        String sql = "INSERT INTO evaluations " +
                "(candidate_id, job_skills_input, skill_points, cgpa_points, project_points, total_score, decision) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setString(2, jobSkillsInput);
            ps.setInt(3, skillPts);
            ps.setInt(4, cgpaPts);
            ps.setInt(5, projPts);
            ps.setInt(6, totalScore);
            ps.setString(7, decision);
            ps.executeUpdate();
        }
    }

    public List<EvalRecord> getHistoryForCandidate(int candidateId) throws SQLException {
        List<EvalRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM evaluations WHERE candidate_id = ? ORDER BY evaluated_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EvalRecord r = new EvalRecord();
                    r.jobSkillsInput = rs.getString("job_skills_input");
                    r.skillPoints    = rs.getInt("skill_points");
                    r.cgpaPoints     = rs.getInt("cgpa_points");
                    r.projectPoints  = rs.getInt("project_points");
                    r.totalScore     = rs.getInt("total_score");
                    r.decision       = rs.getString("decision");
                    r.evaluatedAt    = rs.getTimestamp("evaluated_at");
                    list.add(r);
                }
            }
        }
        return list;
    }
}
