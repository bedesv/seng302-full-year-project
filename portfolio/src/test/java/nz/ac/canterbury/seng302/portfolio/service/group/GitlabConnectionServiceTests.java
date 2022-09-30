package nz.ac.canterbury.seng302.portfolio.service.group;

import nz.ac.canterbury.seng302.portfolio.model.group.GroupRepositorySettings;
import nz.ac.canterbury.seng302.portfolio.repository.group.GroupRepositorySettingsRepository;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Commit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase
@SpringBootTest
class GitlabConnectionServiceTests {
    @Autowired
    GroupRepositorySettingsService repositorySettingsService;

    @Autowired
    GroupRepositorySettingsRepository repositorySettingsRepository;

    @Autowired
    GitlabConnectionService gitlabConnectionService;

    // These details are for a test project I opened on gitlab. It only has read access so should be safe.
    private static final String TEST_ACCESS_TOKEN = "2VCAxY2H2VDVsuor8qeq";
    private static final String TEST_EMPTY_ACCESS_TOKEN = "YPusypuxNNVxRph58wac";
    private static final String TEST_99_COMMITS_ACCESS_TOKEN = "etMHyC6DoFC8KQ9wVRiu";
    private static final String TEST_100_COMMITS_ACCESS_TOKEN = "Y8gewUVgZ-33oLjiVZb2";
    private static final String TEST_101_COMMITS_ACCESS_TOKEN = "uKhoxUSCyF4nVvUqhu-i";
    private static final String TEST_PROJECT_ID = "13642";
    private static final String TEST_EMPTY_PROJECT_ID = "14054";
    private static final String TEST_99_COMMITS_PROJECT_ID = "14158";
    private static final String TEST_100_COMMITS_PROJECT_ID = "14159";
    private static final String TEST_101_COMMITS_PROJECT_ID = "14160";
    private static final String TEST_PROJECT_URL = "https://eng-git.canterbury.ac.nz";

    @BeforeEach
    void reset() {
        repositorySettingsRepository.deleteAll();
    }

    // Test that trying to get a group's settings fails when it doesn't have any settings.
    @Test
    void whenGroupSettingsDontExist_testGetGroupSettings() {
        try {
            gitlabConnectionService.getGroupRepositorySettings(1);
        } catch (Exception e) {
            String expectedMessage = "Given group id doesn't have any repository settings";
            assertEquals(expectedMessage, e.getMessage());
        }

    }

    // Test that trying to get a group's settings succeeds when it has settings.
    @Test
    void whenGroupSettingsExist_testGetGroupSettings() {

        GroupRepositorySettings expectedGroup = repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        GroupRepositorySettings responseGroup = null;
        try {
            responseGroup = gitlabConnectionService.getGroupRepositorySettings(1);
        } catch (Exception e) {
            fail();
        }
        assertEquals(expectedGroup.getGroupId(), responseGroup.getGroupId());
        assertEquals(expectedGroup.getGitlabAccessToken(), responseGroup.getGitlabAccessToken());
        assertEquals(expectedGroup.getGitlabProjectId(), responseGroup.getGitlabProjectId());
        assertEquals(expectedGroup.getGitlabServerUrl(), responseGroup.getGitlabServerUrl());
        assertEquals(expectedGroup.getRepositoryName(), responseGroup.getRepositoryName());
    }

    // Test that trying to connect to a group repository fails when it doesn't have settings.
    @Test
    void whenGroupSettingsDontExist_testGetGitlabConnection() {
        try {
            gitlabConnectionService.getGitLabApiConnection(1);
        } catch (Exception e) {
            String expectedMessage = "Given group id doesn't have any repository settings";
            assertEquals(expectedMessage, e.getMessage());
        }

    }

    // Test that trying to connect to a group repository succeeds when it has settings.
    @Test
    void whenGroupSettingsExist_testGetGitlabConnection() {

        // Ensure the group settings exist
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        GitLabApi connection = null;
        try {
            connection = gitlabConnectionService.getGitLabApiConnection(1);
        } catch (Exception e) {
            fail();
        }
        assertNotNull(connection);
    }

    // Test fetching a repository's commits succeeds when the settings are correct
    @Test
    void whenGroupSettingsCorrect_testGetCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", TEST_ACCESS_TOKEN, TEST_PROJECT_ID, TEST_PROJECT_URL);

        List<Commit> commits = null;
        try {
            commits = gitlabConnectionService.getAllCommits(1);
        } catch (Exception e) {
            fail();
        }
        assertNotNull(commits);
        assertEquals(2, commits.size());
    }

    // Test fetching a repository's commits fails when the url doesn't have a protocol
    @Test
    void whenGroupRepoUrlNoProtocol_testGetCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", TEST_ACCESS_TOKEN, TEST_PROJECT_ID, "url.without.protocol.com");

        List<Commit> commits = null;
        try {
            commits = gitlabConnectionService.getAllCommits(1);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("no protocol"));
        }
        assertNull(commits);
    }

    // Test fetching a repository's commits fails when the url has the wrong protocol
    @Test
    void whenGroupRepoUrlHasWrongProtocol_testGetCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", TEST_ACCESS_TOKEN, TEST_PROJECT_ID, "http://eng-git.canterbury.ac.nz");

        List<Commit> commits = null;
        try {
            commits = gitlabConnectionService.getAllCommits(1);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Moved Temporarily"));
        }
        assertNull(commits);
    }

    // Test fetching a repository's commits fails when the url is invalid
    @Test
    void whenGroupRepoUrlUnknownHost_testGetCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", TEST_ACCESS_TOKEN, TEST_PROJECT_ID, "http://notaurl");

        List<Commit> commits = null;
        try {
            commits = gitlabConnectionService.getAllCommits(1);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("UnknownHostException"));
        }
        assertNull(commits);
    }

    // Test fetching a repository's commits fails when the url is not an url
    @Test
    void whenGroupRepoUrlInvalid_testGetCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", TEST_ACCESS_TOKEN, TEST_PROJECT_ID, "this isn't a url");

        List<Commit> commits = null;
        try {
            commits = gitlabConnectionService.getAllCommits(1);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("no protocol"));
        }
        assertNull(commits);
    }

    // Test fetching a repository's commits fails when the api key is invalid
    @Test
    void whenGroupRepoApiKeyInvalid_testGetCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", "not an api key", TEST_PROJECT_ID, TEST_PROJECT_URL);

        List<Commit> commits = null;
        try {
            commits = gitlabConnectionService.getAllCommits(1);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("401 Unauthorized"));
        }
        assertNull(commits);
    }

    // Test fetching a repository's commits fails when the project id doesn't exist
    @Test
    void whenGroupRepoProjectIdDoesntExist_testGetCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", TEST_ACCESS_TOKEN, "-1232", TEST_PROJECT_URL);

        List<Commit> commits = null;
        try {
            commits = gitlabConnectionService.getAllCommits(1);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("404 Project Not Found"));
        }
        assertNull(commits);
    }

    // Test fetching a repository's commits fails when the project id is for a different project
    @Test
    void whenGroupRepoProjectIdForAnotherProject_testGetCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", TEST_ACCESS_TOKEN, "12296", TEST_PROJECT_URL);

        List<Commit> commits = null;
        try {
            commits = gitlabConnectionService.getAllCommits(1);
        } catch (Exception e) {
            // Returns 404 because the project isn't found in the projects the api key has access to.
            assertTrue(e.getMessage().contains("404 Project Not Found"));
        }
        System.out.println(commits + "This is the line I need");
        assertNull(commits);
    }

    @Test
    void whenGroupRepoHasCommits_testHasCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", TEST_ACCESS_TOKEN, TEST_PROJECT_ID, TEST_PROJECT_URL);
        assertEquals(1, gitlabConnectionService.repositoryHasCommits(1));
    }

    @Test
    void whenGroupRepoHasNoCommits_testHasCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", TEST_EMPTY_ACCESS_TOKEN, TEST_EMPTY_PROJECT_ID, TEST_PROJECT_URL);
        assertEquals(0, gitlabConnectionService.repositoryHasCommits(1));
    }

    @Test
    void whenGroupRepoHas99Commits_testHas100OrMoreCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", TEST_99_COMMITS_ACCESS_TOKEN, TEST_99_COMMITS_PROJECT_ID, TEST_PROJECT_URL);
        assertEquals(0, gitlabConnectionService.repositoryHas100OrMoreCommits(1));
    }

    @Test
    void whenGroupRepoHas100Commits_testHas100OrMoreCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", TEST_100_COMMITS_ACCESS_TOKEN, TEST_100_COMMITS_PROJECT_ID, TEST_PROJECT_URL);
        assertEquals(1, gitlabConnectionService.repositoryHas100OrMoreCommits(1));
    }

    @Test
    void whenGroupRepoHas101Commits_testHas100OrMoreCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", TEST_101_COMMITS_ACCESS_TOKEN, TEST_101_COMMITS_PROJECT_ID, TEST_PROJECT_URL);
        assertEquals(1, gitlabConnectionService.repositoryHas100OrMoreCommits(1));
    }

    @Test
    void whenGroupRepoIsInvalid_testHas100OrMoreCommits() {
        repositorySettingsService.getGroupRepositorySettingsByGroupId(1);
        repositorySettingsService.updateRepositoryInformation(1, "REPO NAME", TEST_ACCESS_TOKEN, TEST_EMPTY_PROJECT_ID, TEST_PROJECT_URL);
        assertEquals(-1, gitlabConnectionService.repositoryHas100OrMoreCommits(1));
    }






}
