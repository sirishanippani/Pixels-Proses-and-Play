# Capstone Report — authoring notes

Draft target: Scaler Neovarsity Backend Specialization Applied Software Project Report.

## Files

One markdown per section. Numeric prefix enforces merge order.

- `00-front-matter.md` — title page, certification, declaration, acknowledgment (placeholders student fills).
- `01-abstract.md`
- `02-project-description.md`
- `03-requirement-gathering.md`
- `04-class-diagrams.md`
- `05-database-schema-design.md`
- `06-feature-development-process.md`
- `07-deployment-flow.md`
- `08-technologies-used.md`
- `09-conclusion.md`
- `10-references.md`
- `diagrams/` — draw.io sources + exported PNGs.

Submission flow: `pandoc` merges numeric-ordered markdowns into `.docx` using a reference template that enforces the Scaler formatting.

## Scaler template — what it demands

Front matter (student fills manually):
1. Title page: "Applied Software Project Report" / full name / Scaler email / Thesis Supervisor: Naman Bhalla / submission date
2. Certification (signed by Naman Bhalla)
3. Declaration (signed by candidate, project module start/end dates)
4. Acknowledgment
5. Table of Contents
6. List of Tables
7. List of Figures

Main sections (each a Heading 3 under "Applied Software Project"):

| # | Section | What Scaler asks for |
|---|---------|----------------------|
| 1 | Abstract | < 300 words, summarize title/purpose/methods/results/conclusions, focus on real-life applications |
| 2 | Project Description | Objectives + relevance; pictures + flow diagrams |
| 3 | Requirement Gathering | FR + NFR, users, use cases, use case diagram, feature set as table |
| 4 | Class Diagrams | LLD; class diagrams via draw.io |
| 5 | Database Schema Design | Textual (tables, PKs, FKs, cardinalities) + diagrammatic |
| 6 | Feature Development Process | ONE key feature deep-dive: request flow, API payload, service, MVC flow, perf optimization with benchmarks |
| 7 | Deployment Flow | AWS stack (EC2, VPC, SG, RDS, Cache, Elastic Beanstalk) + diagrams |
| 8 | Technologies Used | Each tech described + real-life applications + examples |
| 9 | Conclusion | Key Takeaways + Practical Applications + Limitations |
| — | References | Websites, authors, titles |

## Formatting

- Minimum 40 pages
- Margins: 1.25" left/right, 1" top/bottom
- Font: Times New Roman; 14pt headings, 12pt body; black
- Line spacing: body 1.5, lists single
- Alignment: title + chapter = centre; subheading = left; body = justify
- Figure captions BELOW; table captions ABOVE
- Numbering `Table 2.02` = second table in chapter 2 (two-digit second part)

## Scope deviations from the template

The template assumes AWS deployment (EC2 + VPC + RDS + Elastic Beanstalk). Our project ships on **Render (web service) + Neon (Postgres)** — a free-tier PaaS stack. Section 7 will:
1. Describe our actual deployment (Render + Neon + UptimeRobot + Dockerfile + env-var secrets).
2. Draw parallels to the AWS equivalents the template references (Render ≈ Elastic Beanstalk; Neon ≈ RDS; UptimeRobot ≈ CloudWatch alarm-triggered warmup; Dockerfile = AMI equivalent; Render-managed TLS ≈ ALB + ACM).
3. Justify the choice on cost + simplicity for a single-maintainer project.

This keeps the section aligned with the template's intent (explain the platform-level thinking) without pretending we deployed on AWS.
