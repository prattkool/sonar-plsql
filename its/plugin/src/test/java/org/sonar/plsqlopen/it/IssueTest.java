/*
 * Z PL/SQL Analyzer
 * Copyright (C) 2015-2019 Felipe Zorzo
 * mailto:felipebzorzo AT gmail DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plsqlopen.it;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.Issues.Issue;
import org.sonarqube.ws.client.issue.SearchWsRequest;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;

public class IssueTest {

    @ClassRule
    public static Orchestrator orchestrator = Tests.ORCHESTRATOR;

    private static final String PROJECT_KEY = "issue";


    @BeforeClass
    public static void init() {
        orchestrator.getServer().provisionProject(PROJECT_KEY, PROJECT_KEY);
        orchestrator.getServer().associateProjectToQualityProfile(PROJECT_KEY, "plsqlopen", "it-profile");

        SonarScanner build = Tests.createSonarScanner()
            .setProjectDir(new File("projects/metrics/"))
            .setProjectKey(PROJECT_KEY)
            .setProjectName(PROJECT_KEY)
            .setProjectVersion("1.0")
            .setSourceDirs("src")
            .setProperty("sonar.sourceEncoding", "UTF-8");
        orchestrator.executeBuild(build);
    }

    @Test
    public void issues() {
        List<Issue> issues = getIssues(PROJECT_KEY);

        assertThat(issues).extracting("rule", "component")
            .containsExactlyInAnyOrder(
                tuple("plsql:EmptyBlock", PROJECT_KEY + ":src/source1.sql"),
                tuple("my-rules:ForbiddenDmlCheck", PROJECT_KEY + ":src/custom_rule.sql"));
    }

    /* Helper methods */
    private List<Issue> getIssues(String componentKey) {
        return Tests.newWsClient(orchestrator)
            .issues()
            .search(new SearchWsRequest().setComponentKeys(Collections.singletonList(componentKey)))
            .getIssuesList();
    }
}
