package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Timestamp;
import nz.ac.canterbury.seng302.identityprovider.entity.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserAccountsServiceServiceTests {

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserAccountsServerService userService;

    private final String testUsername = "test user";
    private final String testFirstName = "test fname";
    private final String testMiddleName = "test mname";
    private final String testLastName = "test lname";
    private final String testNickname = "test nname";
    private final String testBio = "test bio";
    private final String testPronouns = "test/tester";
    private final String testEmail = "test@email.com";
    private final String testPassword = "test password";

    private int testId;
    private Timestamp testCreated;

    @BeforeEach
    public void setup() {
        repository.deleteAll();
        User testUser = repository.save(new User(testUsername, testFirstName, testMiddleName, testLastName, testNickname, testBio, testPronouns, testEmail, testPassword));
        testId = testUser.getUserId();
        testCreated = testUser.getTimeCreated();
    }

    //Tests that the password change fails if the new password is too short
    @Test
    void changePasswordEmptyPasswordTest() {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.newBuilder()
                .setUserId(testId)
                .setCurrentPassword(testPassword)
                .build();
        ChangePasswordResponse response = userService.changeUserPasswordHandler(changePasswordRequest);
        assertEquals("Password change failed: Validation failed", response.getMessage());
        assertEquals("password", response.getValidationErrors(0).getFieldName());
        assertEquals("Password must be at least 8 characters", response.getValidationErrors(0).getErrorText());
        assertFalse(response.getIsSuccess());
        assertTrue(repository.findByUserId(testId).checkPassword(testPassword));
    }

    //Test the max length for the new password field
    @Test
    void changePasswordLongFieldTest() {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.newBuilder()
                .setUserId(testId)
                .setCurrentPassword(testPassword)
                .setNewPassword("a".repeat(64))
                .build();
        ChangePasswordResponse response = userService.changeUserPasswordHandler(changePasswordRequest);
        assertTrue(response.getIsSuccess());
        assertTrue(repository.findByUserId(testId).checkPassword("a".repeat(64)));
    }

    //Tests that the password change fails if the new password is too long
    @Test
    void changePasswordExtraLongPasswordTest() {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.newBuilder()
                .setUserId(testId)
                .setCurrentPassword(testPassword)
                .setNewPassword("a".repeat(65))
                .build();
        ChangePasswordResponse response = userService.changeUserPasswordHandler(changePasswordRequest);
        assertEquals("Password change failed: Validation failed", response.getMessage());
        assertEquals("password", response.getValidationErrors(0).getFieldName());
        assertEquals("Password must be less than 65 characters", response.getValidationErrors(0).getErrorText());
        assertFalse(response.getIsSuccess());
        assertTrue(repository.findByUserId(testId).checkPassword(testPassword));
    }

    //Tests that password change fails if the user does not exist
    @Test
    void changePasswordBadUserTest() {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.newBuilder()
                .setUserId(-1)
                .setCurrentPassword("Not a password")
                .setNewPassword("Also not a password")
                .build();

        ChangePasswordResponse response = userService.changeUserPasswordHandler(changePasswordRequest);
        assertEquals("Password change failed: Validation failed", response.getMessage());
        assertFalse(response.getIsSuccess());
        assertTrue(repository.findByUserId(testId).checkPassword(testPassword));
    }

    //Tests that password change fails if the password is incorrect
    @Test
    void changePasswordBadPasswordTest() {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.newBuilder()
                .setUserId(testId)
                .setCurrentPassword("Not a password")
                .setNewPassword("Also not a password")
                .build();

        ChangePasswordResponse response = userService.changeUserPasswordHandler(changePasswordRequest);
        assertEquals("Password change failed: Validation failed", response.getMessage());
        assertFalse(response.getIsSuccess());
        assertTrue(repository.findByUserId(testId).checkPassword(testPassword));
    }

    //Tests that password change succeeds if password is correct
    @Test
    void changePasswordGoodPasswordTest() {
        final String newPassword = "new password";
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.newBuilder()
                .setUserId(testId)
                .setCurrentPassword(testPassword)
                .setNewPassword(newPassword)
                .build();
        ChangePasswordResponse response = userService.changeUserPasswordHandler(changePasswordRequest);
        assertEquals("Successfully changed password", response.getMessage());
        assertTrue(response.getIsSuccess());
        assertTrue(repository.findByUserId(testId).checkPassword(newPassword));
    }

    // Tests that editing a user fails if no fields are entered
    @Test
    void editUserEmptyFieldsTest() {
        EditUserRequest editUserRequest = EditUserRequest.newBuilder()
                .setUserId(testId)
                .setFirstName("")
                .setMiddleName("")
                .setLastName("")
                .setNickname("")
                .setBio("")
                .setPersonalPronouns("")
                .setEmail("")
                .build();
        EditUserResponse response = userService.editUserHandler(editUserRequest);

        assertEquals(3, response.getValidationErrorsCount());
        assertEquals("firstName", response.getValidationErrors(0).getFieldName());
        assertEquals("First name is required", response.getValidationErrors(0).getErrorText());
        assertEquals("lastName", response.getValidationErrors(1).getFieldName());
        assertEquals("Last name is required", response.getValidationErrors(1).getErrorText());
        assertEquals("email", response.getValidationErrors(2).getFieldName());
        assertEquals("Email is required", response.getValidationErrors(2).getErrorText());
        assertEquals("Edit user failed: Validation failed", response.getMessage());

        assertFalse(response.getIsSuccess());

        // Check user hasn't been changed
        User testUser = repository.findByUserId(testId);
        assertEquals(testUsername, testUser.getUsername());
        assertEquals(testFirstName, testUser.getFirstName());
        assertEquals(testMiddleName, testUser.getMiddleName());
        assertEquals(testLastName, testUser.getLastName());
        assertEquals(testNickname, testUser.getNickname());
        assertEquals(testBio, testUser.getBio());
        assertEquals(testPronouns, testUser.getPersonalPronouns());
        assertEquals(testEmail, testUser.getEmail());
    }

    // Tests the max field length
    @Test
    void editUserLongFieldsTest() {
        EditUserRequest editUserRequest = EditUserRequest.newBuilder()
                .setUserId(testId)
                .setFirstName("a".repeat(64))
                .setMiddleName("a".repeat(64))
                .setLastName("a".repeat(64))
                .setNickname("a".repeat(64))
                .setBio("a".repeat(1024))
                .setPersonalPronouns("a".repeat(64))
                .setEmail("@".repeat(255))
                .build();
        EditUserResponse response = userService.editUserHandler(editUserRequest);
        assertEquals("Edit user succeeded", response.getMessage());
        assertTrue(response.getIsSuccess());

        // Check that user info has been changed to correct values
        User testUser = repository.findByUserId(testId);
        assertEquals(testUsername, testUser.getUsername());
        assertEquals("a".repeat(64), testUser.getFirstName());
        assertEquals("a".repeat(64), testUser.getMiddleName());
        assertEquals("a".repeat(64), testUser.getLastName());
        assertEquals("a".repeat(64), testUser.getNickname());
        assertEquals("a".repeat(1024), testUser.getBio());
        assertEquals("a".repeat(64), testUser.getPersonalPronouns());
        assertEquals("@".repeat(255), testUser.getEmail());
    }

    // Tests that if fields are too long, the request is rejected
    @Test
    void editUserExtraLongFieldsTest() {
        EditUserRequest editUserRequest = EditUserRequest.newBuilder()
                .setUserId(testId)
                .setFirstName("a".repeat(65))
                .setMiddleName("a".repeat(65))
                .setLastName("a".repeat(65))
                .setNickname("a".repeat(65))
                .setBio("a".repeat(1025))
                .setPersonalPronouns("a".repeat(65))
                .setEmail("@".repeat(256))
                .build();
        EditUserResponse response = userService.editUserHandler(editUserRequest);
        assertEquals("Edit user failed: Validation failed", response.getMessage());
        assertEquals(7, response.getValidationErrorsCount());
        assertEquals("firstName", response.getValidationErrors(0).getFieldName());
        assertEquals("First name must be less than 65 characters", response.getValidationErrors(0).getErrorText());
        assertEquals("middleName", response.getValidationErrors(1).getFieldName());
        assertEquals("Middle name must be less than 65 characters", response.getValidationErrors(1).getErrorText());
        assertEquals("lastName", response.getValidationErrors(2).getFieldName());
        assertEquals("Last name must be less than 65 characters", response.getValidationErrors(2).getErrorText());
        assertEquals("nickname", response.getValidationErrors(3).getFieldName());
        assertEquals("Nickname must be less than 65 characters", response.getValidationErrors(3).getErrorText());
        assertEquals("bio", response.getValidationErrors(4).getFieldName());
        assertEquals("Bio must be less than 1025 characters", response.getValidationErrors(4).getErrorText());
        assertEquals("personalPronouns", response.getValidationErrors(5).getFieldName());
        assertEquals("Personal pronouns must be less than 65 characters", response.getValidationErrors(5).getErrorText());
        assertEquals("email", response.getValidationErrors(6).getFieldName());
        assertEquals("Email must be less than 256 characters", response.getValidationErrors(6).getErrorText());
        assertFalse(response.getIsSuccess());
    }

    // Tests that editing user fails if user does not exist
    @Test
    void editUserBadUserTest() {
        EditUserRequest editUserRequest = EditUserRequest.newBuilder()
                .setUserId(-1)
                .setFirstName(testFirstName + "new")
                .setMiddleName(testMiddleName + "new")
                .setLastName(testLastName + "new")
                .setNickname(testNickname + "new")
                .setBio(testBio + "new")
                .setPersonalPronouns(testPronouns + "new")
                .setEmail(testEmail + "new")
                .build();
        EditUserResponse response = userService.editUserHandler(editUserRequest);
        assertEquals("Edit user failed: Validation failed", response.getMessage());
        assertFalse(response.getIsSuccess());

        // Check user hasn't been changed
        User testUser = repository.findByUserId(testId);
        assertEquals(testUsername, testUser.getUsername());
        assertEquals(testFirstName, testUser.getFirstName());
        assertEquals(testMiddleName, testUser.getMiddleName());
        assertEquals(testLastName, testUser.getLastName());
        assertEquals(testNickname, testUser.getNickname());
        assertEquals(testBio, testUser.getBio());
        assertEquals(testPronouns, testUser.getPersonalPronouns());
        assertEquals(testEmail, testUser.getEmail());
    }

    // Tests that editing user succeeds if user exists
    @Test
    void editUserGoodUserTest() {
        EditUserRequest editUserRequest = EditUserRequest.newBuilder()
                .setUserId(testId)
                .setFirstName(testFirstName + "new")
                .setMiddleName(testMiddleName + "new")
                .setLastName(testLastName + "new")
                .setNickname(testNickname + "new")
                .setBio(testBio + "new")
                .setPersonalPronouns(testPronouns + "new")
                .setEmail(testEmail + "new")
                .build();
        EditUserResponse response = userService.editUserHandler(editUserRequest);
        assertEquals("Edit user succeeded", response.getMessage());
        assertTrue(response.getIsSuccess());

        // Check that user info has been changed to correct values
        User testUser = repository.findByUserId(testId);
        assertEquals(testUsername, testUser.getUsername());
        assertEquals(testFirstName + "new", testUser.getFirstName());
        assertEquals(testMiddleName + "new", testUser.getMiddleName());
        assertEquals(testLastName + "new", testUser.getLastName());
        assertEquals(testNickname + "new", testUser.getNickname());
        assertEquals(testBio + "new", testUser.getBio());
        assertEquals(testPronouns + "new", testUser.getPersonalPronouns());
        assertEquals(testEmail + "new", testUser.getEmail());
    }

    // Tests that getting user fails if user does not exist
    @Test
    void getUserByIdBadUserTest() {
        GetUserByIdRequest getUserByIdRequest = GetUserByIdRequest.newBuilder()
                .setId(-1)
                .build();
        UserResponse response = userService.getUserAccountByIdHandler(getUserByIdRequest);
        assertEquals("", response.getUsername());
        assertEquals("", response.getFirstName());
        assertEquals("", response.getMiddleName());
        assertEquals("", response.getLastName());
        assertEquals("", response.getNickname());
        assertEquals("", response.getBio());
        assertEquals("", response.getPersonalPronouns());
        assertEquals("", response.getEmail());
        assertEquals(Timestamp.newBuilder().build(), response.getCreated());
    }

    // Tests that getting user succeeds if user exists
    @Test
    void getUserByIdGoodUserTest() {
        GetUserByIdRequest getUserByIdRequest = GetUserByIdRequest.newBuilder()
                .setId(testId)
                .build();
        UserResponse response = userService.getUserAccountByIdHandler(getUserByIdRequest);
        assertEquals(testUsername, response.getUsername());
        assertEquals(testFirstName, response.getFirstName());
        assertEquals(testMiddleName, response.getMiddleName());
        assertEquals(testLastName, response.getLastName());
        assertEquals(testNickname, response.getNickname());
        assertEquals(testBio, response.getBio());
        assertEquals(testPronouns, response.getPersonalPronouns());
        assertEquals(testEmail, response.getEmail());
        assertEquals(testCreated, response.getCreated());
    }

    // Tests that creating user fails if no fields are entered
    @Test
    void userRegisterEmptyFieldsTest() {
        UserRegisterRequest userRegisterRequest = UserRegisterRequest.newBuilder().build();
        UserRegisterResponse response = userService.registerHandler(userRegisterRequest);
        assertEquals("Register attempt failed: Validation failed", response.getMessage());
        assertEquals(5, response.getValidationErrorsCount());
        assertEquals("username", response.getValidationErrors(0).getFieldName());
        assertEquals("Username is required", response.getValidationErrors(0).getErrorText());
        assertEquals("firstName", response.getValidationErrors(1).getFieldName());
        assertEquals("First name is required", response.getValidationErrors(1).getErrorText());
        assertEquals("lastName", response.getValidationErrors(2).getFieldName());
        assertEquals("Last name is required", response.getValidationErrors(2).getErrorText());
        assertEquals("email", response.getValidationErrors(3).getFieldName());
        assertEquals("Email is required", response.getValidationErrors(3).getErrorText());
        assertEquals("password", response.getValidationErrors(4).getFieldName());
        assertEquals("Password must be at least 8 characters", response.getValidationErrors(4).getErrorText());
        assertFalse(response.getIsSuccess());
    }

    // Tests the max length for each field
    @Test
    void userRegisterLongFieldsTest() {
        UserRegisterRequest userRegisterRequest = UserRegisterRequest.newBuilder()
                .setUsername("a".repeat(64))
                .setPassword("a".repeat(64))
                .setFirstName("a".repeat(64))
                .setMiddleName("a".repeat(64))
                .setLastName("a".repeat(64))
                .setNickname("a".repeat(64))
                .setBio("a".repeat(1024))
                .setPersonalPronouns("a".repeat(64))
                .setEmail("@".repeat(255))
                .build();
        UserRegisterResponse response = userService.registerHandler(userRegisterRequest);
        assertEquals("Register attempt succeeded", response.getMessage());
        assertTrue(response.getIsSuccess());
    }

    // Tests that if fields are too long, the request is rejected
    @Test
    void userRegisterExtraLongFieldsTest() {
        UserRegisterRequest userRegisterRequest = UserRegisterRequest.newBuilder()
                .setUsername("a".repeat(65))
                .setPassword("a".repeat(65))
                .setFirstName("a".repeat(65))
                .setMiddleName("a".repeat(65))
                .setLastName("a".repeat(65))
                .setNickname("a".repeat(65))
                .setBio("a".repeat(1025))
                .setPersonalPronouns("a".repeat(65))
                .setEmail("@".repeat(256))
                .build();
        UserRegisterResponse response = userService.registerHandler(userRegisterRequest);
        assertEquals("Register attempt failed: Validation failed", response.getMessage());
        assertEquals(9, response.getValidationErrorsCount());
        assertEquals("username", response.getValidationErrors(0).getFieldName());
        assertEquals("Username must be less than 65 characters", response.getValidationErrors(0).getErrorText());
        assertEquals("firstName", response.getValidationErrors(1).getFieldName());
        assertEquals("First name must be less than 65 characters", response.getValidationErrors(1).getErrorText());
        assertEquals("middleName", response.getValidationErrors(2).getFieldName());
        assertEquals("Middle name must be less than 65 characters", response.getValidationErrors(2).getErrorText());
        assertEquals("lastName", response.getValidationErrors(3).getFieldName());
        assertEquals("Last name must be less than 65 characters", response.getValidationErrors(3).getErrorText());
        assertEquals("nickname", response.getValidationErrors(4).getFieldName());
        assertEquals("Nickname must be less than 65 characters", response.getValidationErrors(4).getErrorText());
        assertEquals("bio", response.getValidationErrors(5).getFieldName());
        assertEquals("Bio must be less than 1025 characters", response.getValidationErrors(5).getErrorText());
        assertEquals("personalPronouns", response.getValidationErrors(6).getFieldName());
        assertEquals("Personal pronouns must be less than 65 characters", response.getValidationErrors(6).getErrorText());
        assertEquals("email", response.getValidationErrors(7).getFieldName());
        assertEquals("Email must be less than 256 characters", response.getValidationErrors(7).getErrorText());
        assertEquals("password", response.getValidationErrors(8).getFieldName());
        assertEquals("Password must be less than 65 characters", response.getValidationErrors(8).getErrorText());
        assertFalse(response.getIsSuccess());
    }

    // Tests that creating user fails if username already exists
    @Test
    void userRegisterRepeatedUsernameTest() {
        UserRegisterRequest userRegisterRequest = UserRegisterRequest.newBuilder()
                .setUsername(testUsername)
                .setPassword(testPassword + "2")
                .setFirstName(testFirstName + "2")
                .setMiddleName(testMiddleName + "2")
                .setLastName(testLastName + "2")
                .setNickname(testNickname + "2")
                .setBio(testBio + "2")
                .setPersonalPronouns(testPronouns + "2")
                .setEmail(testEmail + "2")
                .build();
        UserRegisterResponse response = userService.registerHandler(userRegisterRequest);
        assertEquals("Register attempt failed: Validation failed", response.getMessage());
        assertEquals(1, response.getValidationErrorsCount());
        assertEquals("username", response.getValidationErrors(0).getFieldName());
        assertEquals("Username already taken", response.getValidationErrors(0).getErrorText());
        assertFalse(response.getIsSuccess());
    }

    // Tests that an email that does not contain @ is rejected
    @Test
    void userRegisterBadEmailTest() {
        UserRegisterRequest userRegisterRequest = UserRegisterRequest.newBuilder()
                .setUsername(testUsername + "2")
                .setPassword(testPassword + "2")
                .setFirstName(testFirstName + "2")
                .setMiddleName(testMiddleName + "2")
                .setLastName(testLastName + "2")
                .setNickname(testNickname + "2")
                .setBio(testBio + "2")
                .setPersonalPronouns(testPronouns + "2")
                .setEmail("bad email")
                .build();
        UserRegisterResponse response = userService.registerHandler(userRegisterRequest);
        assertEquals("Register attempt failed: Validation failed", response.getMessage());
        assertEquals(1, response.getValidationErrorsCount());
        assertEquals("email", response.getValidationErrors(0).getFieldName());
        assertEquals("Email must be valid", response.getValidationErrors(0).getErrorText());
        assertFalse(response.getIsSuccess());
    }

    // Tests that a password less than 8 characters is rejected
    @Test
    void userRegisterBadPasswordTest() {
        UserRegisterRequest userRegisterRequest = UserRegisterRequest.newBuilder()
                .setUsername(testUsername + "2")
                .setPassword(":seven:")
                .setFirstName(testFirstName + "2")
                .setMiddleName(testMiddleName + "2")
                .setLastName(testLastName + "2")
                .setNickname(testNickname + "2")
                .setBio(testBio + "2")
                .setPersonalPronouns(testPronouns + "2")
                .setEmail(testEmail + "2")
                .build();
        UserRegisterResponse response = userService.registerHandler(userRegisterRequest);
        assertEquals("Register attempt failed: Validation failed", response.getMessage());
        assertEquals(1, response.getValidationErrorsCount());
        assertEquals("password", response.getValidationErrors(0).getFieldName());
        assertEquals("Password must be at least 8 characters", response.getValidationErrors(0).getErrorText());
        assertFalse(response.getIsSuccess());
    }

    // Tests that creating user succeeds if username does not exist
    @Test
    void userRegisterGoodUsernameTest() {
        UserRegisterRequest userRegisterRequest = UserRegisterRequest.newBuilder()
                .setUsername(testUsername + "2")
                .setPassword(testPassword + "2")
                .setFirstName(testFirstName + "2")
                .setMiddleName(testMiddleName + "2")
                .setLastName(testLastName + "2")
                .setNickname(testNickname + "2")
                .setBio(testBio + "2")
                .setPersonalPronouns(testPronouns + "2")
                .setEmail(testEmail + "2")
                .build();
        UserRegisterResponse response = userService.registerHandler(userRegisterRequest);
        assertEquals("Register attempt succeeded", response.getMessage());
        assertTrue(response.getIsSuccess());
        int newTestId = response.getNewUserId();
        User testUser = repository.findByUserId(newTestId);
        assertEquals(testUsername + "2", testUser.getUsername());
        assertEquals(testFirstName + "2", testUser.getFirstName());
        assertEquals(testMiddleName + "2", testUser.getMiddleName());
        assertEquals(testLastName + "2", testUser.getLastName());
        assertEquals(testNickname + "2", testUser.getNickname());
        assertEquals(testBio + "2", testUser.getBio());
        assertEquals(testPronouns + "2", testUser.getPersonalPronouns());
        assertEquals(testEmail + "2", testUser.getEmail());
        //as the password is stored encrypted, it needs to be checked differently
        assertTrue(testUser.checkPassword(testPassword + "2"));
    }

}