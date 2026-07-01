package com.daa.resumescorer.db;

import com.daa.resumescorer.model.Candidate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CandidateDAO {

    private final SkillDAO skillDAO = new SkillDAO();

    /** Inserts a candidate plus its skills (candidate_skills bridge rows) in a single transaction. */
    public int insertCandidate(Candidate c) throws SQLException {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            String sql = "INSERT INTO candidates (name, email, phone, cgpa, project_count) VALUES (?, ?, ?, ?, ?)";
            int candidateId;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getName());
                ps.setString(2, c.getEmail());
                ps.setString(3, c.getPhone());
                ps.setFloat(4, c.getCgpa());
                ps.setInt(5, c.getProjectCount());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No candidate id generated");
                    candidateId = keys.getInt(1);
                }
            }

            String linkSql = "INSERT INTO candidate_skills (candidate_id, skill_id) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(linkSql)) {
                for (String skill : c.getSkillSet()) {
                    if (skill == null || skill.isBlank()) continue;
                    int skillId = skillDAO.getOrCreateSkillId(con, skill.trim().toLowerCase());
                    ps.setInt(1, candidateId);
                    ps.setInt(2, skillId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
            c.setId(candidateId);
            return candidateId;

        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException ignored) {}
        }
    }

    /** Loads every candidate with their skills joined back together. */
    public List<Candidate> getAllCandidates() throws SQLException {
        Map<Integer, Candidate> byId = new LinkedHashMap<>();

        String sql =
            "SELECT c.id, c.name, c.email, c.phone, c.cgpa, c.project_count, s.skill_name " +
            "FROM candidates c " +
            "LEFT JOIN candidate_skills cs ON cs.candidate_id = c.id " +
            "LEFT JOIN skills s ON s.id = cs.skill_id " +
            "ORDER BY c.id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                Candidate c = byId.get(id);
                if (c == null) {
                    c = new Candidate(id, rs.getString("name"), rs.getString("email"), rs.getString("phone"),
                            rs.getFloat("cgpa"), rs.getInt("project_count"), new ArrayList<>());
                    byId.put(id, c);
                }
                String skill = rs.getString("skill_name");
                if (skill != null) c.getSkillSet().add(skill);
            }
        }
        return new ArrayList<>(byId.values());
    }

    public boolean nameExists(String name) throws SQLException {
        String sql = "SELECT 1 FROM candidates WHERE LOWER(name) = LOWER(?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Deletes everything — candidate_skills and evaluations cascade automatically via FK. */
    public void deleteAllCandidates() throws SQLException {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM candidates");
        }
    }
}
