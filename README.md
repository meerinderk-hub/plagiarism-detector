# Plagiarism Detection Using Word Frequency

Web app that compares two or more texts by word frequency and reports a similarity percentage.
It is an educational similarity indicator, not a plagiarism verdict.

**Architecture:** HTML/CSS frontend -> Java Servlets -> service/business logic -> DAO classes (JDBC) -> MySQL.
No Spring Boot or other framework.

## Features
- Paste text or upload `.txt` files (2 to 10 documents)
- Punctuation removal, lower-casing, tokenization, optional stop-word filter
- Word-frequency counting and list of common words
- Every document pair compared; similarity percentage with a low / moderate / high band
- CRUD with MySQL:
  - **Create** - documents and reports are saved when you compare
  - **Read** - reports list, single report, documents list, single document
  - **Update** - rename a stored document
  - **Delete** - delete a report, or a document (its report pairs are removed too)

## Similarity formula
```
overlap    = sum over every word w of  min(freqA(w), freqB(w))
similarity = 2 * overlap / (wordsA + wordsB) * 100
```
Identical texts -> 100%. No shared words -> 0%.

## Project layout
```
sql/schema.sql                         tables: documents, comparison_runs, comparison_reports
src/main/java/com/plagiarism/
  model/      Doc, PairResult, PairRecord, StoredDocument, RunSummary, RunDetail, ReportRow
  util/       TextProcessor, SimilarityCalculator, InputValidator   (core logic, validation)
  dao/        DBConnection, DocumentDao, RunDao                     (JDBC: PreparedStatement, ResultSet)
  service/    ComparisonService                                     (compare + save in one transaction)
  servlet/    CompareServlet, ReportServlet, HistoryServlet, DocumentServlet
  view/       HtmlPages                                             (HTML output)
  console/    ConsoleApp                                            (Java-only version, no database)
src/main/webapp/                       index.html, style.css, app.js, WEB-INF/web.xml
```

## Run the web app (Tomcat 10.1 or newer)
1. Create the database: `mysql -u root -p < sql/schema.sql`
2. Set the connection before starting Tomcat (defaults: `root`, empty password, `localhost:3306`):
   ```
   export DB_USER=root
   export DB_PASSWORD=your_password
   # optional: export DB_URL="jdbc:mysql://localhost:3306/plagiarism_db?useSSL=false&allowPublicKeyRetrieval=true"
   ```
3. Build: `mvn clean package` (creates `target/plagiarism-detector.war`)
4. Copy the WAR into Tomcat's `webapps/` folder and start Tomcat.
5. Open http://localhost:8080/plagiarism-detector/

Notes
- Tomcat limits form posts to 2 MB by default. For very large texts raise `maxPostSize` on the Connector in `conf/server.xml`.
- Tomcat 9 or older uses `javax.servlet` instead of `jakarta.servlet`. Change the imports, set the dependency to
  `javax.servlet:javax.servlet-api:4.0.1`, and use a `web.xml` for Servlet 4.0.
- In Eclipse/IntelliJ you can instead add Tomcat as a server and run the project directly.

## Run the Java-only console version (no Tomcat, no MySQL)
```
mkdir out
javac -d out src/main/java/com/plagiarism/model/*.java src/main/java/com/plagiarism/util/*.java src/main/java/com/plagiarism/console/*.java
java -cp out com.plagiarism.console.ConsoleApp essay1.txt essay2.txt
```
Run it with no file names to type or paste the texts instead.
