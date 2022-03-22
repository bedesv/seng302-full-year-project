package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.MessageOrBuilder;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.entity.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc.UserAccountServiceImplBase;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@GrpcService
public class UserAccountsServerService extends UserAccountServiceImplBase {

    @Autowired
    private UserRepository repository;

    @Override
    public void changeUserPassword(ChangePasswordRequest request, StreamObserver<ChangePasswordResponse> responseObserver) {
        ChangePasswordResponse reply = changeUserPasswordHandler(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    /**
     * Handler for password change requests.
     * If the user exists, and their password is correct, change it to the new password.
     * @param request A password change request according to user_accounts.proto
     * @return A password change response according to user_accounts.proto
     */
    @VisibleForTesting
    ChangePasswordResponse changeUserPasswordHandler(ChangePasswordRequest request) {
        ChangePasswordResponse.Builder reply = ChangePasswordResponse.newBuilder();

        int userId = request.getUserId();
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();

        reply.addAllValidationErrors(checkPassword(newPassword));

        List<ValidationError> userValidationErrors = checkUserExists(userId);
        reply.addAllValidationErrors(userValidationErrors);

        if (userValidationErrors.size() == 0) {
            reply.addAllValidationErrors(checkCurrentPassword(currentPassword, userId));
        }

        if (reply.getValidationErrorsCount() == 0) {
            User user = repository.findByUserId(request.getUserId());
            user.setPassword(newPassword);
            repository.save(user);
            reply.setIsSuccess(true).setMessage("Successfully changed password");

        } else {
            reply.setIsSuccess(false).setMessage("Password change failed: Validation failed");
        }

        return reply.build();
    }

    @Override
    public void editUser(EditUserRequest request, StreamObserver<EditUserResponse> responseObserver) {
        EditUserResponse reply = editUserHandler(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    /**
     * Handler for edit user requests.
     * If the user exists, change their details to the new details in the request.
     * @param request A user edit request according to user_accounts.proto
     * @return A user edit response according to user_accounts.proto
     */
    @VisibleForTesting
    EditUserResponse editUserHandler(EditUserRequest request) {
        EditUserResponse.Builder reply = EditUserResponse.newBuilder();

        int userId = request.getUserId();
        String firstName = request.getFirstName();
        String middleName = request.getMiddleName();
        String lastName = request.getLastName();
        String nickname = request.getNickname();
        String bio = request.getBio();
        String personalPronouns = request.getPersonalPronouns();
        String email = request.getEmail();

        reply.addAllValidationErrors(checkFirstName(firstName));
        reply.addAllValidationErrors(checkMiddleName(middleName));
        reply.addAllValidationErrors(checkLastName(lastName));
        reply.addAllValidationErrors(checkNickname(nickname));
        reply.addAllValidationErrors(checkBio(bio));
        reply.addAllValidationErrors(checkPersonalPronouns(personalPronouns));
        reply.addAllValidationErrors(checkEmail(email));
        reply.addAllValidationErrors(checkUserExists(userId));

        if (reply.getValidationErrorsCount() == 0) {

            User user = repository.findByUserId(userId);
            user.setFirstName(firstName);
            user.setMiddleName(middleName);
            user.setLastName(lastName);
            user.setNickname(nickname);
            user.setBio(bio);
            user.setPersonalPronouns(personalPronouns);
            user.setEmail(email);
            repository.save(user);
            reply.setIsSuccess(true).setMessage("Edit user succeeded");
        } else {
            reply.setIsSuccess(false).setMessage("Edit user failed: Validation failed");
        }
        return reply.build();
    }

    @Override
    public void getUserAccountById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        UserResponse reply = getUserAccountByIdHandler(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    /**
     * Handler for user information retrieval requests.
     * If the user id exists, return their information.
     * Else, return blank information.
     * @param request A get user by id request according to user_accounts.proto
     * @return A user response according to user_accounts.proto
     */
    @VisibleForTesting
    UserResponse getUserAccountByIdHandler(GetUserByIdRequest request) {
        UserResponse.Builder reply = UserResponse.newBuilder();

        if (repository.existsById(request.getId())) {
            User user = repository.findByUserId(request.getId());
            reply.setUsername(user.getUsername())
                    .setFirstName(user.getFirstName())
                    .setMiddleName(user.getMiddleName())
                    .setLastName(user.getLastName())
                    .setNickname(user.getNickname())
                    .setBio(user.getBio())
                    .setPersonalPronouns(user.getPersonalPronouns())
                    .setEmail(user.getEmail())
                    .setCreated(user.getTimeCreated());
        }
        return reply.build();
    }

    @Override
    public void register(UserRegisterRequest request, StreamObserver<UserRegisterResponse> responseObserver) {

        UserRegisterResponse reply = registerHandler(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();

    }

    /**
     * Handler for user register requests.
     * Checks if the fields for user creation are valid, and creates the user.
     * If some fields are not valid, instead fails and returns a list of non-valid fields.
     * @param request A user register request according to user_accounts.proto
     * @return A user register response according to user_accounts.proto
     */
    @VisibleForTesting
    UserRegisterResponse registerHandler(UserRegisterRequest request) {
        UserRegisterResponse.Builder reply = UserRegisterResponse.newBuilder();

        String username = request.getUsername();
        String firstName = request.getFirstName();
        String middleName = request.getMiddleName();
        String lastName = request.getLastName();
        String nickname = request.getNickname();
        String bio = request.getBio();
        String personalPronouns = request.getPersonalPronouns();
        String email = request.getEmail();
        String password = request.getPassword();

        if (repository.findByUsername(username) != null) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Username already taken").setFieldName("username").build();
            reply.addValidationErrors(validationError);
        }

        reply.addAllValidationErrors(checkUsername(username));
        reply.addAllValidationErrors(checkFirstName(firstName));
        reply.addAllValidationErrors(checkMiddleName(middleName));
        reply.addAllValidationErrors(checkLastName(lastName));
        reply.addAllValidationErrors(checkNickname(nickname));
        reply.addAllValidationErrors(checkBio(bio));
        reply.addAllValidationErrors(checkPersonalPronouns(personalPronouns));
        reply.addAllValidationErrors(checkEmail(email));
        reply.addAllValidationErrors(checkPassword(password));

        if (reply.getValidationErrorsCount() == 0) {
            repository.save(new User(
                    username,
                    firstName,
                    middleName,
                    lastName,
                    nickname,
                    bio,
                    personalPronouns,
                    email,
                    password));
            reply
                    .setIsSuccess(true)
                    .setNewUserId(repository.findByUsername(request.getUsername()).getUserId())
                    .setMessage("Register attempt succeeded");
        } else {
            reply.setIsSuccess(false).setMessage("Register attempt failed: Validation failed");
        }
        return reply.build();
    }

    private List<ValidationError> checkUsername(String username) {
        List<ValidationError> validationErrors = new ArrayList<>();

        if (username.equals("")) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Username is required").setFieldName("username").build();
            validationErrors.add(validationError);
        }

        if (username.length() > 64) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Username must be less than 65 characters").setFieldName("username").build();
            validationErrors.add(validationError);
        }
        return validationErrors;
    }

    private List<ValidationError> checkFirstName(String firstName) {
        List<ValidationError> validationErrors = new ArrayList<>();

        if (firstName.equals("")) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("First name is required").setFieldName("firstName").build();
            validationErrors.add(validationError);
        }
        if (firstName.length() > 64) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("First name must be less than 65 characters").setFieldName("firstName").build();
            validationErrors.add(validationError);
        }
        return validationErrors;
    }

    private List<ValidationError> checkMiddleName(String middleName) {
        List<ValidationError> validationErrors = new ArrayList<>();
        if (middleName.length() > 64) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Middle name must be less than 65 characters").setFieldName("middleName").build();
            validationErrors.add(validationError);
        }
        return validationErrors;
    }

    private List<ValidationError> checkLastName(String lastName) {
        List<ValidationError> validationErrors = new ArrayList<>();

        if (lastName.equals("")) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Last name is required").setFieldName("lastName").build();
            validationErrors.add(validationError);
        }
        if (lastName.length() > 64) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Last name must be less than 65 characters").setFieldName("lastName").build();
            validationErrors.add(validationError);
        }
        return validationErrors;
    }

    private List<ValidationError> checkNickname(String nickname) {
        List<ValidationError> validationErrors = new ArrayList<>();

        if (nickname.length() > 64) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Nickname must be less than 65 characters").setFieldName("nickname").build();
            validationErrors.add(validationError);
        }
        return validationErrors;
    }

    private List<ValidationError> checkBio(String bio) {
        List<ValidationError> validationErrors = new ArrayList<>();

        if (bio.length() > 1024) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Bio must be less than 1025 characters").setFieldName("bio").build();
            validationErrors.add(validationError);
        }
        return validationErrors;
    }

    private List<ValidationError> checkPersonalPronouns(String personalPronouns) {
        List<ValidationError> validationErrors = new ArrayList<>();

        if (personalPronouns.length() > 64) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Personal pronouns must be less than 65 characters").setFieldName("personalPronouns").build();
            validationErrors.add(validationError);
        }
        return validationErrors;
    }

    private List<ValidationError> checkEmail(String email) {
        List<ValidationError> validationErrors = new ArrayList<>();

        if (email.equals("")) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Email is required").setFieldName("email").build();
            validationErrors.add(validationError);
        } else if (!email.contains("@")) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Email must be valid").setFieldName("email").build();
            validationErrors.add(validationError);
        }

        if (email.length() > 255) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Email must be less than 256 characters").setFieldName("email").build();
            validationErrors.add(validationError);
        }
        return validationErrors;
    }

    private List<ValidationError> checkPassword(String password) {
        List<ValidationError> validationErrors = new ArrayList<>();

        if (password.length() <= 8) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Password must be at least 8 characters").setFieldName("password").build();
            validationErrors.add(validationError);
        }

        if (password.length() > 64) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Password must be less than 65 characters").setFieldName("password").build();
            validationErrors.add(validationError);
        }
        return validationErrors;
    }

    private List<ValidationError> checkUserExists(int userId) {
        List<ValidationError> validationErrors = new ArrayList<>();
        if (!repository.existsById(userId)) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("User does not exist").setFieldName("userId").build();
            validationErrors.add(validationError);
        }
        return validationErrors;
    }

    private List<ValidationError> checkCurrentPassword(String currentPassword, int userId) {
        List<ValidationError> validationErrors = new ArrayList<>();
        User tempUser = repository.findByUserId(userId);
        if (Boolean.FALSE.equals(tempUser.checkPassword(currentPassword))) {
            ValidationError validationError = ValidationError.newBuilder().setErrorText("Current password is incorrect").setFieldName("currentPassword").build();
            validationErrors.add(validationError);
        }
        return validationErrors;
    }









}
