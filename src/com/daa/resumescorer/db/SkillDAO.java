package com.daa.resumescorer.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles the master "skills" lookup table.
 * getOrCreateSkillId acts like a hash-map lookup backed by a UNIQUE column,
 * so the same skill string never gets duplicated in the table.
 */
public class SkillDAO {

    public int getOrCreateSkillId(Connection con, String skillName) throws SQLException {
        String selectSql = "SELECT id FROM skills WHERE skill_name = ?";
        try (PreparedStatement ps = con.prepareStatement(selectSql)) {
            ps.setString(1, skillName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        String insertSql = "INSERT INTO skills (skill_name) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, skillName);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Could not get or create skill: " + skillName);
    }
}
