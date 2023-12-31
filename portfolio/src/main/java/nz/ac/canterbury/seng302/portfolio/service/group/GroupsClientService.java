package nz.ac.canterbury.seng302.portfolio.service.group;

import com.google.common.annotations.VisibleForTesting;
import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.portfolio.model.group.Group;
import nz.ac.canterbury.seng302.portfolio.model.group.GroupListResponse;
import nz.ac.canterbury.seng302.portfolio.model.group.PortfolioGroup;
import nz.ac.canterbury.seng302.portfolio.model.user.User;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class GroupsClientService {

    @GrpcClient("identity-provider-grpc-server")
    private GroupsServiceGrpc.GroupsServiceBlockingStub groupsStub;

    @GrpcClient("identity-provider-grpc-server")
    private GroupsServiceGrpc.GroupsServiceStub groupsNonBlockingStub;

    @Autowired
    private GroupRepositorySettingsService groupRepositorySettingsService;
    @Autowired
    private PortfolioGroupService portfolioGroupService;

    public CreateGroupResponse createGroup(final String shortName, final String longName, final int parentProjectId) {
        CreateGroupRequest createGroupRequest = CreateGroupRequest.newBuilder()
                .setShortName(shortName)
                .setLongName(longName)
                .build();
        CreateGroupResponse response = groupsStub.createGroup(createGroupRequest);
        if (response.getIsSuccess()) {
            portfolioGroupService.createPortfolioGroup(response.getNewGroupId(), parentProjectId);
        }
        return response;
    }

    public ModifyGroupDetailsResponse modifyGroupDetails(final int groupId, final String shortName, final String longName) {
        ModifyGroupDetailsRequest modifyGroupDetailsRequest = ModifyGroupDetailsRequest.newBuilder()
                .setGroupId(groupId)
                .setShortName(shortName)
                .setLongName(longName)
                .build();
        return groupsStub.modifyGroupDetails(modifyGroupDetailsRequest);
    }

    /**
     * Service for getting the group details
     * @param groupId the id of the group to get the details for
     * @return the response from the server
     */
    public GroupDetailsResponse getGroupDetailsById(final int groupId) {
        GetGroupDetailsRequest getGroupDetailsRequest = GetGroupDetailsRequest.newBuilder()
                .setGroupId(groupId)
                .build();
        return groupsStub.getGroupDetails(getGroupDetailsRequest);
    }

    /**
     * Service for deleting a group
     * @param groupId the id of the group to be deleted
     * @return the response from the server
     */
    public DeleteGroupResponse deleteGroupById(final int groupId) {
        DeleteGroupRequest deleteGroupRequest = DeleteGroupRequest.newBuilder()
                .setGroupId(groupId)
                .build();
        DeleteGroupResponse response = groupsStub.deleteGroup(deleteGroupRequest);

        // If the group was deleted in the identity provider then delete it and its repository settings in the portfolio
        if (response.getIsSuccess()) {
            groupRepositorySettingsService.deleteGroupRepositoryByGroupId(groupId);
            portfolioGroupService.deletePortfolioGroupByGroupId(groupId);
        }
        return response;
    }

    /**
     * Client service method for adding Users to groups
     * @param groupId The group ID which users will be added to
     * @param userIds The list of user ID which will be added to the group
     * @return The response from the IDP
     */
    public AddGroupMembersResponse addGroupMembers(final int groupId, final List<Integer> userIds) {
        AddGroupMembersRequest addGroupMembersRequest = AddGroupMembersRequest.newBuilder()
                .setGroupId(groupId)
                .addAllUserIds(userIds)
                .build();
        return groupsStub.addGroupMembers(addGroupMembersRequest);
    }

    public RemoveGroupMembersResponse removeGroupMembers(final int groupId, final List<Integer> userIds) {
        RemoveGroupMembersRequest removeGroupMembersRequest = RemoveGroupMembersRequest.newBuilder()
                .setGroupId(groupId)
                .addAllUserIds(userIds)
                .build();
        return groupsStub.removeGroupMembers(removeGroupMembersRequest);
    }


    /**
     * Creates a request to be sent to the IDP for requesting a paginated list of group responses
     * @param offset The number of groups to be sliced from the original list of groups from the DB
     * @param limit The max number of groups to be returned
     * @param orderBy How the list of groups will be sorted: "short" = sort by short names, "long" = sort by long name, "members" = sort by number of members
     * @param isAscending Whether the list is ascending or descending
     * @return A list of paginated, sorted and ordered group responses
     */
    public GroupListResponse getPaginatedGroups(int offset, int limit, String orderBy, boolean isAscending) {
        GetPaginatedGroupsRequest getPaginatedGroupsRequest = GetPaginatedGroupsRequest.newBuilder()
                .setOffset(offset)
                .setLimit(limit)
                .setOrderBy(orderBy)
                .setIsAscendingOrder(isAscending)
                .build();
        PaginatedGroupsResponse response = groupsStub.getPaginatedGroups(getPaginatedGroupsRequest);
        return new GroupListResponse(response);
    }

    /**
     * Gets all groups in a list of group responses.
     * @return A list of group responses, ordered by short name, in ascending order
     */
    public GroupListResponse getAllGroups() {
        return getPaginatedGroups(0, Integer.MAX_VALUE, "short", true);
    }

    /**
     * Gets all groups in a list of group responses.
     * @return A list of group responses, ordered by short name, in ascending order
     */
    public List<Group> getAllGroupsInProject(int projectId) {
        GroupListResponse response = getAllGroups();
        List<Group> groups = new ArrayList<>();
        List<PortfolioGroup> portfolioGroups = portfolioGroupService.findPortfolioGroupsByParentProjectId(projectId);
        for (Group group : response.getGroups()) {
            for (PortfolioGroup portfolioGroup : portfolioGroups) {
                if (portfolioGroup.getGroupId() == group.getGroupId()) {
                    groups.add(group);
                }
            }
        }
        return groups;
    }

    /**
     * Builds a request to update the groups information in the database
     * @param group the group object to be saved
     * @return a ModifyGroupDetailsResponse that contains the status of the request and any errors found
     */
    public ModifyGroupDetailsResponse updateGroupDetails(Group group) {
        ModifyGroupDetailsRequest modifyGroupDetailsRequest = ModifyGroupDetailsRequest.newBuilder()
                .setGroupId(group.getGroupId())
                .setLongName(group.getLongName())
                .setShortName(group.getShortName())
                .build();
        return groupsStub.modifyGroupDetails(modifyGroupDetailsRequest);
    }

    /**
     * Checks if the given user is in the given group
     * @param groupId Group id of the group to check
     * @param userId User id of the user to check
     * @return A boolean, true if the user is in the group, false otherwise
     */
    public boolean userInGroup(int groupId, int userId) {
        Group group = new Group(getGroupDetailsById(groupId));
        for (User user : group.getMembers()) {
            if (user.getId() == userId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of all groups a user is a member of in the given project
     * @param projectId the parent project id
     * @param userId the id of the user
     * @return a list of groups that the user is in
     */
    public List<Group> getAllGroupsUserIn(int projectId, int userId) {
        List<Group> groups = new ArrayList<>();
        for (Group group : getAllGroupsInProject(projectId)) {
            if (userInGroup(group.getGroupId(), userId)) {
                groups.add(group);
            }
        }
        return groups;
    }

    /**
     * Only for mocking purposes
     * Updates the current stub with a new one
     * @param newStub the new (mocked) GroupsServiceBlockingStub
     */
    @VisibleForTesting
    protected void setGroupsStub(GroupsServiceGrpc.GroupsServiceBlockingStub newStub) {
        groupsStub = newStub;
    }

    /**
     * Only for mocking purposes
     * @return the current groups stub
     */
    @VisibleForTesting
    protected GroupsServiceGrpc.GroupsServiceBlockingStub getGroupsStub() {
        return groupsStub;
    }
}
