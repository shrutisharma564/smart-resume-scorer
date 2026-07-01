package com.daa.resumescorer.model;

import java.util.List;

/**
 * POJO representing one candidate / resume.
 * id is -1 until the row has actually been persisted in MySQL.
 */
public class Candidate {

    private int id;
    private String name;
    private String email;
    private String phone;
    private float cgpa;
    private int projectCount;
    private List<String> skillSet;

    // runtime-only fields, computed during "Evaluate & Rank" — not stored on the object permanently
    private int totalScore;
    private String decision;

    public Candidate(String name, float cgpa, int projectCount, List<String> skillSet) {
        this(-1, name, "", "", cgpa, projectCount, skillSet);
    }

    public Candidate(String name, String email, String phone, float cgpa, int projectCount, List<String> skillSet) {
        this(-1, name, email, phone, cgpa, projectCount, skillSet);
    }

    public Candidate(int id, String name, String email, String phone, float cgpa, int projectCount, List<String> skillSet) {
        this.id = id;
        this.name = name;
        this.email = email == null ? "" : email;
        this.phone = phone == null ? "" : phone;
        this.cgpa = cgpa;
        this.projectCount = projectCount;
        this.skillSet = skillSet;
        this.totalScore = 0;
        this.decision = "-";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public float getCgpa() { return cgpa; }
    public void setCgpa(float cgpa) { this.cgpa = cgpa; }

    public int getProjectCount() { return projectCount; }
    public void setProjectCount(int projectCount) { this.projectCount = projectCount; }

    public List<String> getSkillSet() { return skillSet; }
    public void setSkillSet(List<String> skillSet) { this.skillSet = skillSet; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
}
