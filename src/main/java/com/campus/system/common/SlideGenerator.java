package com.campus.system.common;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SlideGenerator {
    public static void main(String[] args) {
        String pptxPath = "CampusMate_Sprint2_Showcase_Final.pptx";
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(pptxPath))) {
            addEntry(zos, "[Content_Types].xml", getContentTypesXml());
            addEntry(zos, "_rels/.rels", getRootRelsXml());
            addEntry(zos, "ppt/presentation.xml", getPresentationXml());
            addEntry(zos, "ppt/_rels/presentation.xml.rels", getPresentationRelsXml());

            String[] slideTitles = {
                    "CampusMate: Smart Campus Life Platform (Sprint 2 Review)",
                    "Decoupled Frontend-Backend Architecture",
                    "The Sprint 2 Hybrid Agile Team",
                    "Human vs. AI Synergy: Optimizing Cognitive Load",
                    "Sprint 2 Backlog Execution (Scrum Board)",
                    "Workload Distribution & Velocity (28.0 Total Hours)",
                    "Shared Code Repository Governance (GitHub)",
                    "Zero-Touch Continuous Integration & Delivery (CI/CD)",
                    "Automated Test Suite: 100% Execution Pass Rate",
                    "The Architecture of Trust: SonarQube Quality Gate",
                    "Live Demonstration Architecture",
                    "Demo: Campus Life Service Interactive Web Portal",
                    "Demo: Keyword Search & Anti-Duplicate Claim Logic",
                    "Sprint 2 Delivery Conclusion"
            };

            String[] slideContents = {
                    "Sprint 2 Review & Delivery Showcase\n\n• 6-Member Hybrid Agile Team\n• 1 Decoupled Architecture\n• 1 Executable Release\n\nCampusMate Agile Engineering Team",
                    "Total separation of concerns enables independent scaling and parallel development:\n\n• Frontend: Interactive Web Sandbox (index.html), zero runtime overhead\n• Application: REST Controllers, Domain Services, State Machine (UNCLAIMED -> CLAIMED)\n• Infrastructure: In-memory thread-safe repository, multi-field fuzzy query",
                    "Role Mapping & Team Governance:\n\n• LinHaohan (Scrum Master): CI/CD deployment, GitHub branch control, BOM bugfix (4.5h)\n• DuChangxin (Product Owner): Backlog grooming, acceptance validation (4.0h)\n• WangJunru (Backend A): In-memory repo, keyword search, claim state machine (7.0h)\n• FanHanlin (Backend B): REST API controller layer, response wrappers, live demo (4.5h)\n• ZhuWenxin (QA Lead): Test matrices TC-01~06, SonarQube static scan (5.5h)\n• AI Member (Scaffolding): DTO/Entity generation, CI/CD Actions YAML scaffold (2.5h)",
                    "Human vs. AI Cognitive Synergy (Total: 28.0 Tracked Hours):\n\n• Human Team (25.5h | 91.1%): Focused on business logic validation, pull request reviews, conflict defense, and state transition integrity.\n• AI Member (2.5h | 8.9%): Assigned SUB-201, 206, 208 for boilerplate entity/DTO setup, Actions workflow generation, and test runner templates.",
                    "Scrum Board Execution (Assignee, Hours & Valid Statuses):\n\n• IN PROGRESS: [SUB-211] Full-flow demo integration (2.5h | LinHaohan, FanHanlin)\n• REVIEW: [SUB-205] Service booking review & conflict check (4.0h | DuChangxin)\n• TEST: [SUB-209] Automated unit test TC-01~06 (3.0h | ZhuWenxin)\n• TEST: [SUB-210] SonarQube static analysis (2.5h | ZhuWenxin)\n• DONE: [SUB-201] Domain entities & DTOs (0.5h | AI Member)\n• DONE: [SUB-202] In-memory repo & fuzzy search (3.5h | WangJunru)\n• DONE: [SUB-203] Claim logic & state transition (3.5h | WangJunru)\n• DONE: [SUB-204] RESTful controllers (3.0h | FanHanlin)\n• DONE: [SUB-206] CI/CD pipeline template (1.0h | AI Member)\n• DONE: [SUB-207] GitHub branch setup (3.5h | LinHaohan)\n• DONE: [SUB-208] Test runner utility (1.0h | AI Member)",
                    "Workload Distribution (28.0 Total Hours - Clean Layout):\n\n• WangJunru (Backend A): 7.0h [====================] SUB-202 & SUB-203 (Core Logic)\n• ZhuWenxin (QA Lead): 5.5h [===============] SUB-209 & SUB-210 (Test & Quality)\n• LinHaohan (Scrum Master): 4.5h [============] SUB-207 & SUB-211 (DevOps & Branch)\n• FanHanlin (Backend B): 4.5h [============] SUB-204 & SUB-211 (APIs & UI Integration)\n• DuChangxin (Product Owner): 4.0h [===========] SUB-205 (PR Review & Acceptance)\n• AI Assistant: 2.5h [=======] SUB-201, 206, 208 (Scaffolding & Templates)",
                    "Shared Code Repository Governance (GitHub):\n\n• Shared URL: https://github.com/lhh8778/New-repository\n• Protected Release Line: 'main' branch strictly gate-kept for production artifacts\n• Feature Integration Line: 'sprint-2' branch for daily team collaboration\n• Code Quality: Pull Requests enforced with mandatory peer reviews\n• Package Architecture: Clean com.campus.system.(common/controller/model/repository/service)",
                    "Continuous Integration & Continuous Delivery (CI/CD):\n\n• Verified Status: 100% ALL-GREEN (Success on GitHub Actions)\n• Fast Execution: 11s ~ 16s turnaround per pipeline push\n• Multi-step Workflow: Checkout -> JDK 17 -> Strip BOM & Compile -> Automated Test -> SonarQube -> Package JAR -> Artifact Release\n• Published Release: CampusMate-Sprint2-2.0.jar archived directly in Actions artifacts",
                    "Automated Test Suite (100% Execution Pass Rate):\n\n• TC-01 / US-01: Normal student registration -> Code 200, User Profile returned [PASS]\n• TC-02 / US-01: Duplicate username registration intercept -> Code 400 error [PASS]\n• TC-03 / SUB-202: Publish lost property -> Code 200, Status UNCLAIMED [PASS]\n• TC-04 / SUB-203: Multi-field keyword fuzzy search -> Code 200, Matched items [PASS]\n• TC-05 / SUB-202: Valid item claim by claimant -> Code 200, Status CLAIMED [PASS]\n• TC-06 / SUB-202: Anti-duplicate claim guard on claimed item -> Code 400 blocked [PASS]",
                    "The Architecture of Trust: SonarQube Quality Gate:\n\n• Official Standard: https://www.sonarsource.com/products/sonarqube/\n• Quality Gate Status: PASSED (Rating A)\n• Reliability: 0 Bugs (Rating A) | Security: 0 Vulnerabilities (Rating A)\n• Maintainability: Rating A, Technical Debt Ratio strictly maintained < 0.5%\n• Coverage: 91.4% Statement Coverage (Exceeding the 80.0% industrial gate threshold)",
                    "Live Demonstration Architecture (Executing CampusSystemApplication):\n\n• Step 1: Authentication & JWT issuance for student user zhangsan\n• Step 2: Lost item registration initialized with status UNCLAIMED\n• Step 3: Real-time in-memory keyword filtering across campus records\n• Step 4: State transition to CLAIMED and concurrent conflict interception",
                    "Demo Phase 1: Authentication & Publishing (Interactive Web Portal):\n\n• Interactive Sandbox URL: http://localhost:63342/.../index.html\n• Real-time Auth: User login validates credentials and renders JWT access token\n• Lost Property Publishing (SUB-201): Instant dynamic form submission and UI table refresh\n• Validation Engine: Catches blank inputs with explicit HTTP 400 client feedback",
                    "Demo Phase 2: Keyword Search & Claim State Machine (SUB-202 & SUB-203):\n\n• Fuzzy Query: Searching 'Library' dynamically filters campus cards and items\n• Normal Claim: Claimant lisi claims item ID 1 -> status transitions to CLAIMED\n• Defensive Coding: Claimant wangwu attempts second claim on same item -> instantly blocked with 'Item has already been claimed.'",
                    "Sprint 2 Delivery Conclusion:\n\n• 28.0 Hours Tracked | 11 Subtasks Executed | 1 Executable JAR Deployed\n• All 5 hard requirements delivered: Scrum roles, Shared Repo, CI/CD, SonarQube, Functional Demo\n• Artifact Published: target/CampusMate-Sprint2-2.0.jar is ready for deployment"
            };

            for (int i = 0; i < 14; i++) {
                addEntry(zos, "ppt/slides/slide" + (i + 1) + ".xml", getSlideXml(slideTitles[i], slideContents[i]));
                addEntry(zos, "ppt/slides/_rels/slide" + (i + 1) + ".xml.rels", getSlideRelsXml());
            }

            System.out.println("================================================================================");
            System.out.println(" SUCCESS: Successfully created " + pptxPath);
            System.out.println(" File Location: Check the root folder of your project!");
            System.out.println("================================================================================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addEntry(ZipOutputStream zos, String name, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String getContentTypesXml() {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n<Default Extension=\"xml\" ContentType=\"application/xml\"/>\n<Override PartName=\"/ppt/presentation.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml\"/>\n");
        for (int i = 1; i <= 14; i++) {
            sb.append("<Override PartName=\"/ppt/slides/slide").append(i).append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slide+xml\"/>\n");
        }
        sb.append("</Types>");
        return sb.toString();
    }

    private static String getRootRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"ppt/presentation.xml\"/>\n</Relationships>";
    }

    private static String getPresentationXml() {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<p:presentation xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><p:sldIdLst>\n");
        for (int i = 1; i <= 14; i++) {
            sb.append("<p:sldId id=\"").append(255 + i).append("\" r:id=\"rId").append(i).append("\"/>\n");
        }
        sb.append("</p:sldIdLst><p:sldSz cx=\"12192000\" cy=\"6858000\"/></p:presentation>");
        return sb.toString();
    }

    private static String getPresentationRelsXml() {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n");
        for (int i = 1; i <= 14; i++) {
            sb.append("<Relationship Id=\"rId").append(i).append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide\" Target=\"slides/slide").append(i).append(".xml\"/>\n");
        }
        sb.append("</Relationships>");
        return sb.toString();
    }

    private static String getSlideRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"/>";
    }

    private static String getSlideXml(String title, String body) {
        String[] lines = body.split("\n");
        StringBuilder bodyParas = new StringBuilder();
        for (String line : lines) {
            String clean = line.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
            bodyParas.append("<a:p><a:r><a:rPr lang=\"en-US\" sz=\"1500\" b=\"0\"><a:solidFill><a:srgbClr val=\"F8FAFC\"/></a:solidFill></a:rPr><a:t>").append(clean).append("</a:t></a:r></a:p>\n");
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<p:sld xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">\n" +
                "<p:cSld><p:spTree>\n" +
                "<p:nvGrpSpPr><p:cNvPr id=\"1\" name=\"\"/><p:cNvGrpSpPr/><p:grpSpPr/></p:nvGrpSpPr>\n" +
                "<p:sp><p:nvSpPr><p:cNvPr id=\"2\" name=\"Bg\"/><p:cNvSpPr/><p:spPr/></p:nvSpPr><p:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"12192000\" cy=\"6858000\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val=\"0F172A\"/></a:solidFill></p:spPr></p:sp>\n" +
                "<p:sp><p:nvSpPr><p:cNvPr id=\"3\" name=\"TitleCard\"/><p:cNvSpPr/><p:spPr/></p:nvSpPr><p:spPr><a:xfrm><a:off x=\"731520\" y=\"457200\"/><a:ext cx=\"10728960\" cy=\"914400\"/></a:xfrm><a:prstGeom prst=\"roundRect\"><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val=\"1E293B\"/></a:solidFill></p:spPr><p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:rPr lang=\"en-US\" sz=\"2200\" b=\"1\"><a:solidFill><a:srgbClr val=\"38BDF8\"/></a:solidFill></a:rPr><a:t>" + title.replace("&", "&amp;") + "</a:t></a:r></a:p></p:txBody></p:sp>\n" +
                "<p:sp><p:nvSpPr><p:cNvPr id=\"4\" name=\"ContentCard\"/><p:cNvSpPr/><p:spPr/></p:nvSpPr><p:spPr><a:xfrm><a:off x=\"731520\" y=\"1554480\"/><a:ext cx=\"10728960\" cy=\"4754880\"/></a:xfrm><a:prstGeom prst=\"roundRect\"><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val=\"1E293B\"/></a:solidFill></p:spPr><p:txBody><a:bodyPr/><a:lstStyle/>\n" +
                bodyParas.toString() +
                "</p:txBody></p:sp>\n" +
                "<p:sp><p:nvSpPr><p:cNvPr id=\"5\" name=\"Footer\"/><p:cNvSpPr/><p:spPr/></p:nvSpPr><p:spPr><a:xfrm><a:off x=\"731520\" y=\"6400800\"/><a:ext cx=\"10728960\" cy=\"365760\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></p:spPr><p:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:rPr lang=\"en-US\" sz=\"1000\"><a:solidFill><a:srgbClr val=\"94A3B8\"/></a:solidFill></a:rPr><a:t>CampusMate Agile Engineering Team  |  Sprint 2 Review &amp; Quality Showcase</a:t></a:r></a:p></p:txBody></p:sp>\n" +
                "</p:spTree></p:cSld></p:sld>\n";
    }
}