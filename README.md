# Smart Resume Scorer — MySQL / JDBC Edition

A Java Swing desktop app that scores and ranks resumes against a job's
required skills, with login, MySQL persistence, evaluation history, and
CSV/PDF export.


## ✨ Key Features

- 🔐 User authentication with secure password hashing
- 📄 Resume parsing using Apache PDFBox
- 🎯 Skill-based resume matching and candidate scoring
- 📊 Candidate ranking using efficient sorting algorithms
- 🗄️ MySQL database integration through JDBC
- 📝 Evaluation history tracking
- 📑 CSV and PDF report generation
- 🖥️ Java Swing desktop interface

## 🏗️ Project Structure

```text
src/com/daa/resumescorer/
├── model/      Data models
├── db/         JDBC & database access
├── util/       PDF parsing, authentication & exports
├── ui/         Swing user interface
└── Main.java   Application entry point


```md
sql/            Database schema
lib/            External libraries


## ⚙️ Setup & Installation

This project requires Java JDK 17+, MySQL 8+, Apache PDFBox, and MySQL Connector/J. Create the database using `mysql -u root -p < sql/schema.sql`. Then configure your MySQL credentials in `db.properties` using the provided database URL, username, and password. Place the MySQL Connector/J `.jar` file inside the project's `lib/` directory along with `pdfbox-app-3.0.7.jar`. On Windows, compile and run the application using `compile.bat` followed by `run.bat`; on macOS/Linux, use `./compile.sh` followed by `./run.sh`. On first launch, create a new account and log in to start using the application.

## 🧑‍💻 How It Works

1. Create an account and log in.
2. Add a candidate manually or upload a resume PDF.
3. Enter the required skills for the target job.
4. Click **Evaluate and Rank** to score and rank candidates based on skill matching.
5. View evaluation history for previously evaluated candidates.
6. Export ranked results as CSV or PDF reports.



- ## 📸 Application Screenshots

### Login Screen

![Login](screenshots/login.png)

### Dashboard

![Dashboard](screenshots/dashboard.png)

### Resume Evaluation

![Resume Evaluation](screenshots/result.png)

---

## 🚀 Future Enhancements

- AI-powered resume analysis using NLP
- ATS compatibility checking
- Resume keyword recommendations
- Skill gap analysis
- AI-generated interview questions
- Recruiter dashboard
- LLM-based resume feedback
- REST API integration
- Cloud deployment
- Email notification system


## 🛠️ Troubleshooting

- **MySQL connection error:** Make sure MySQL is running and the credentials in `db.properties` are correct.
- **JDBC driver not found:** Make sure the MySQL Connector/J `.jar` is present in the `lib/` directory and included in the classpath.
- **Application exits on startup:** Check the terminal output for JDBC or configuration errors.
