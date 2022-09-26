let ctrlPressed;
let shiftPressed;
let lastRow;
let lastTable;
let currentTable;
let clipboard;
let copiedRow;

/**
 * Disables text selection
 */
document.onselectstart = function() {
    return false;
}

/**
 * Key released event listener
 * Marks control/command or shift pressed variables as false if they've been released
 */
document.addEventListener("keyup", function (event) {
    if (event.key === "Control" || event.key === "Meta") {
        ctrlPressed = false;
    } else if (event.key === "Shift") {
        shiftPressed = false;
    }
}, false)

/**
 * Key pressed down event listener
 * Marks the control/command or shift pressed variables as true if they've been pressed
 * If control/command is down and A is pressed, it selects all in the current table
 */
document.addEventListener("keydown", function (event) {
    if (event.key === "Control" || event.key === "Meta") {
        ctrlPressed = true;
    } else if (event.key === "Shift") {
        shiftPressed = true;
    } else if (event.key === "Escape") {
        clearTableSelection(currentTable)
    } else if (event.key === "a" && ctrlPressed) {
        selectAllInTable()
    }
}, false)

/**
 * Triggered when the user drags a row
 * Only does something when an unselected row is dragged
 * Unselects all rows then selects the copied rows
 * See copyMembers for more information on how copying works
 */
document.addEventListener("dragstart", function () {
    clearTableSelection(lastTable)
    lastRow = null
    clearTableSelection(currentTable)
    selectRows(clipboard)
})

/**
 * Copies members to group specified by groupId. Function is called by copy buttons for mobile.
 * @param groupId Group to copy to
 */
function copyHere(groupId) {
    console.log("mate");
    clearTableSelection(lastTable);
    lastRow = null;
    clearTableSelection(currentTable);
    selectRows(clipboard);
    let group = document.getElementById(groupId).parentElement.parentElement;
    pasteMembers(group);
    hideCopyButton();
}

/**
 * Overrides the allowDrop event so that users aren't allowed to be dropped
 * in groups that don't allow it (e.g. groupless group)
 * @param ev allowDrop event
 */
function allowDrop(ev) {
    ev.preventDefault();
}

/**
 * Triggered when the user clicks on a row.
 * If the row is selected, copies all selected users
 * If not, only copies the dragged user
 * @param currRow the clicked on row
 */
function copyMembers(currRow) {
    lastTable = currentTable
    currentTable = currRow.parentNode.getElementsByTagName("tr")
    copiedRow = currRow
    clipboard = []
    // If not selected, only copy the dragged user
    if (currRow.className !== "selected") {
        clipboard.push(currRow)
    // If selected, copy all selected users
    } else {
        for (let row of currentTable) {
            if (row.className === "selected") {
                clipboard.push(row)
            }
        }
    }
    removeButtonVisible(currentTable, true);
}

/**
 * Fetches the group table and updates its content to the new table.
 * Then iterates through each user and checks if they should be selected or not.
 * (Newly added users should be selected)
 * @param groupId The id of the group which will be updated
 * @param content HTML content containing the new table
 * @param selectedUserIds The user ids of the new users, used to select them in the new table
 */
function updateTable(groupId, content, selectedUserIds) {

    // Update the table
    const newGroupTable = document.getElementById(`group_${groupId}_members`)
    newGroupTable.innerHTML = content
    let tableIdRows = newGroupTable.getElementsByClassName("user_id")
    let tableRows = []
    // Select the new users
    let userId;
    let numUsers = 0;
    for (let row of tableIdRows) {
        tableRows.push(row.parentElement)

        numUsers += 1
        userId = parseInt(row.innerText, 10)
        if (selectedUserIds.includes(userId) && row.id !== "no-hover") {
            row.parentElement.className = "selected"
        } else {
            row.parentElement.className = "unselected"
        }
    }
    currentTable = tableRows
    if (groupId !== GROUPLESS_GROUP_ID ) {
        if (selectedUserIds.length > 0) {
            removeButtonVisible(tableRows, true);
        } else if (tableRows.length > 0){
            removeButtonVisible(tableRows, false);
        }
    }

    if (groupId >= 0) {
        // Update the delete group button alert to have the correct number of members
        const groupDeleteButton = document.getElementById(`group_${groupId}_delete_button`)
        groupDeleteButton.onsubmit = () => {return confirm(`Are you sure you want to delete this group?\n` +
                                                           `Doing so will remove ${numUsers} member(s) from the group.\n` +
                                                           `This action cannot be undone.`)}
    }


}

/**
 * Triggered when a user drops members onto a group
 * Fetches the old and new group ids.
 * Then goes through the clipboard elements and fetches the user ids from them.
 * Then sends a request to the server to add those users to the new group.
 * Then updates the new group so that it contains the new users.
 * If the old group is the "users without a group" group, then it fetches an updated version of that group
 * @param currGroup the group that the selection was dropped on
 */
async function pasteMembers(currGroup) {
    let newTable = currGroup.getElementsByTagName("tbody")[0]
    let oldGroupId = currentTable[0].parentNode.parentNode.id;
    let newGroupId = newTable.parentNode.id;

    // Only paste the users if it's not the same group
    if (oldGroupId !== newGroupId) {
        const teacherIds = getTeacherIds()

        // Fetch the user ids of the members to be added from the table
        let userIds = [];
        let userId;
        let movingTeacherToGroupless = false
        for (let element of clipboard) {
            userId = element.getElementsByClassName("user_id")[0].innerText;
            userIds.push(parseInt(userId, 10));

            // Check if the user is a teacher and trying to move a teacher to the groupless group
            if (parseInt(newGroupId, 10) === GROUPLESS_GROUP_ID && (userIsTeacher && !userIsAdmin) && teacherIds.includes(parseInt(userId, 10))) {
                movingTeacherToGroupless = true
            }
        }

        // If the user is a teacher and trying to move a teacher to the groupless group
        if (movingTeacherToGroupless) {
            // Confirm that the user wants to remove a teacher from all other groups
            const confirmation = confirm("Note: You cannot remove teaching roles. \nAt least one selected user is a teacher, this will remove them from all other groups, but not remove their teaching role\nDo you want to continue?")
            if (!confirmation) {
                return;
            }
        }

        clearTableSelection(currentTable)
        lastRow = null

        // Build the url with parameters for the members to be added
        let url
        url = new URL (`${CONTEXT}/group-${newGroupId}-members`)
        for (let user of userIds) {
            url.searchParams.append("members", user)
        }

        // Send a post request to update the groups members
        // Receives the updated table HTML content as a response
        const response = await fetch(url, {
            method: "POST"
        }).then(res => {
            return res.text()
        })

        // If adding to the groupless group, update all the tables and don't select anything in them
        if (Number.parseInt(newGroupId, 10) === GROUPLESS_GROUP_ID) {
            for (let id of allGroupIds) {
                if (id !== GROUPLESS_GROUP_ID) {
                    await updateTableById(id, [])
                }
            }
        }

        // If the old group was the groupless group, update that group
        // Only needs to be done for groupless as moving people from other groups
        // doesn't remove them from the old group
        if (Number.parseInt(oldGroupId, 10) === GROUPLESS_GROUP_ID) {
            await updateTableById(GROUPLESS_GROUP_ID, userIds)
        }

        updateTable(newGroupId, response, userIds)

        expandGroup(currGroup);


    }
}

/**
 * Triggered when a row is clicked on
 * Takes the clicked on row, checks if Control or Shift are being held and performs the relevant selection action
 * @param currRow the row that was clicked on
 */
function rowClick(currRow) {
    // Set the current table to the one that the clicked on row belongs to

    currentTable = currRow.parentNode.getElementsByTagName("tr")
    if (lastTable !== currentTable) {
        clearTableSelection(lastTable)
        lastRow = null
    }

    if (shiftPressed && ctrlPressed) {  // Select all rows between (inclusive) the current and last clicked on rows
        // Just select the current row if no other rows have been clicked on
        if (!lastRow) {
            toggleRow(currRow)
        } else {
            selectRowsBetween([lastRow.rowIndex, currRow.rowIndex])
        }
        removeButtonVisible(currentTable, true);
    } else if (ctrlPressed) { // Toggle the clicked on row if Control is pressed
        toggleRow(currRow)
        removeButtonVisible(currentTable, countSelectedRows(currentTable) !== 0);
    } else if (shiftPressed) { // Select all rows (inclusive) between current and last pressed, update when last pressed changes
        if (!lastRow) {
            toggleRow(currRow)
        } else {
            clearTableSelection(currentTable)
            selectRowsBetween([lastRow.rowIndex, currRow.rowIndex])
        }
        removeButtonVisible(currentTable, true);
    } else { // Otherwise, clear any other selected rows and mark the clicked on row
        clearTableSelection(currentTable)
        toggleRow(currRow)
        removeButtonVisible(currentTable, true);
    }

}

/**
 * Takes a row and toggles it from selected to unselected, or from unselected to selected
 * @param row the row to have selection toggled
 */
function toggleRow(row) {
    row.className = row.className === 'selected' ? 'unselected' : 'selected';
    lastRow = row;
}

/**
 * Takes a row and toggles it from selected to unselected, or from unselected to selected
 * For mobile called by touch.
 * @param row the row to have selection toggled
 */
function toggleRowMobile(row) {
    currentTable = row.parentNode.getElementsByTagName("tr");
    if (lastTable !== currentTable) {
        clearTableSelection(lastTable);
        lastRow = null;
        hideCopyButton();
    }
    row.className = row.className === 'selected' ? 'unselected' : 'selected';
    lastRow = row;
    viewCopyButton(row);
}

/**
 * Takes a list of two indexes, sorts them, then marks all rows of the table between (inclusive) those indices as selected
 * @param indexes two table indexes in random order
 */
function selectRowsBetween(indexes) {

    // Sort the index to find the top and bottom index
    indexes.sort(function(a, b) {
        return a - b;
    });

    for (let i = indexes[0]; i <= indexes[1]; i++) {
        if (currentTable[i - 1].id !== "no-hover") {
            currentTable[i - 1].className = 'selected';
        }
    }
}


/**
 * Selects the given rows
 * @param rows A list of tr row objects
 */
function selectRows(rows) {
    for (let row of rows) {
        if (row.id !== "no-hover") {
            row.className = "selected"
            removeButtonVisible(currentTable, true);
        }
    }
}

/**
 * Unselects all items in the given table
 */
function clearTableSelection(table) {
    if (table && table.length > 0) {
        for (let i = 0; i < table.length; i++) {
            table[i].className = 'unselected';
        }
        removeButtonVisible(table, false);
    }
}

/**
 * Selects all items in the current table
 */
function selectAllInTable() {
    for (let i = 0; i < currentTable.length; i++) {
        if (currentTable[i].id !== "no-hover") {
            currentTable[i].className = 'selected';
        }
    }
    removeButtonVisible(currentTable, true);
}

/**
 * Checks that the user is doing a valid action, then prompts them for confirmation on whether they're sure
 * they want to remove users from the group
 * Then removes the selecte users from the group and updates both the current group and the groupless group
 * @param button the button that was clicked on
 */
async function removeSelectedUsers(button) {
    const groupId = button.parentNode.parentNode.parentNode.getElementsByTagName("table")[0].id
    const selectedRows = button.parentNode.parentNode.parentNode.getElementsByClassName("selected")

    // Fetch the user ids of the members to be removed from the group
    let userIds = [];
    let userId;
    for (let element of selectedRows) {
        userId = element.getElementsByClassName("user_id")[0].innerText;
        userIds.push(parseInt(userId, 10));
    }

    if (userIds.length === 0) {
        alert("Error: Please select users to remove")
        return
    }
    // Check that the user is allowed to remove group members
    if (!userIsTeacher && !userIsAdmin) {
        alert("Error: You do not have permission to remove group members.")
        return

    // Check that the user is not trying to remove members from the groupless group
    } else if (parseInt(groupId, 10) === GROUPLESS_GROUP_ID) {
        alert("Error: You cannot remove members from the groupless group.")
        return

    // Check that the user isn't removing their own teaching role when they're not an admin
    } else if (userIds.includes(CURRENT_USER_ID) && parseInt(groupId, 10) === TEACHER_GROUP_ID && (userIsTeacher && !userIsAdmin)) {
        alert("Error: You cannot remove your own teaching role.")
        return
    }

    const removeConfirmation = confirm(`Are you sure you want to remove ${userIds.length} user(s) from the group?`)

    if (removeConfirmation) {
        // Build the url with parameters for the members to be removed
        let url
        url = new URL (`${CONTEXT}/group-${groupId}-members`)
        for (let user of userIds) {
            url.searchParams.append("members", user)
        }
        removeButtonVisible(selectedRows, false);
        // Send a post request to update the groups members
        // Receives the updated table HTML content as a response
        const response = await fetch(url, {
            method: "DELETE"
        }).then(res => {
            return res.text()
        })

        // Update the group table. Don't select any members
        updateTable(groupId, response, [])

        // Update the groupless group. Don't select any members
        await updateTableById(GROUPLESS_GROUP_ID, [])

    }
}

/**
 * Changes the visibility of the remove button for the given table, depending
 * on the "visible" parameter
 * @param table The table to change the visibility of the remove button for
 * @param visible Boolean value, whether the button should be visible
 */
function removeButtonVisible(table, visible) {
    const groupId = table[0].parentNode.parentNode.id

    // Don't do anything if it's the groupless group or if
    // it's the teacher group and the user is a teacher
    if (parseInt(groupId, 10) === GROUPLESS_GROUP_ID || (parseInt(groupId, 10) === TEACHER_GROUP_ID && (userIsTeacher && !userIsAdmin))) {
        return;
    }

    // Get the button object
    const buttonObject = table[0].parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.getElementsByClassName(`group_${groupId}_remove_users_button`)[0]
    // Change the visibility of the button
    if (!visible) {
        buttonObject.setAttribute("hidden", "hidden");
    } else if (buttonObject.getAttribute("hidden")) {
        buttonObject.removeAttribute("hidden");
    }
}


/**
 * Counts the number of selected rows in the given table
 * @param table Table rows to search in
 * @returns An integer number of rows with class 'selected'
 */
function countSelectedRows(table) {
    return table[0].parentNode.getElementsByClassName("selected").length
}


/**
 * Sends a get request to update the given table
 * Selects the given user ids
 * @param groupId Group to be updated
 * @param userIds User ids to be selected
 * @returns An updated group table in HTML form
 */
async function updateTableById(groupId, userIds) {
    let url = new URL(`${CONTEXT}/group-${groupId}-members`)
    const response = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.text()
    })
    updateTable(groupId, response, userIds)
}

/**
 * Takes the group html element and makes it expanded
 * @param group html element
 */
function expandGroup(group) {
    group.getElementsByClassName("group__details collapse")[0].className = "group__details collapse show"
    group.getElementsByClassName("group__name")[0].setAttribute("aria-expanded", "true")
}

/**
 * Returns a list of user ids of all users in the teachers group
 */
function getTeacherIds() {
    const teacherRows = document.getElementById("group_" + TEACHER_GROUP_ID).getElementsByClassName("user_id");

    let teacherIds = []
    for (let teacher of teacherRows) {
        teacherIds.push(parseInt(teacher.innerText, 10))
    }
    return teacherIds
}

/**
 * Makes copy button visible for mobile users to copy members.
 * @param row
 */
function viewCopyButton(row) {
    let tableId = row.parentElement.parentElement.id;
    let copyButtons = document.getElementsByClassName("groups__copy-member-mobile");
    for (let i = 0; i < copyButtons.length; i++) {
        let groupId = copyButtons[i].id;
        if ((parseInt(groupId, 10) === GROUPLESS_GROUP_ID || (parseInt(groupId, 10) === TEACHER_GROUP_ID && (userIsTeacher && !userIsAdmin))) || parseInt(groupId, 10) === parseInt(tableId, 10)) {
            copyButtons[i].hidden = true;
        } else {
            copyButtons[i].hidden = false;
        }

    }
}

/**
 * hides all copy buttons
 */
function hideCopyButton() {
    let copyButtons = document.getElementsByClassName("groups__copy-member-mobile");
    for (let i = 0; i < copyButtons.length; i++) {
        copyButtons[i].hidden = true;
    }
}