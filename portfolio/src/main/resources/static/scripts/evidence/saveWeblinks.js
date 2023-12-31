let index;

/**
 * Gets web links from backend and updates DOM
 * @param id id of piece of evidence
 * @returns {Promise<void>}
 */
async function getWebLinks(id) {
    let url
    url = new URL (`${CONTEXT}/getWebLinks-${id}`);
    const updatedEvidence = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.text()
    })
    // Update the page with the new HTML content
    const evidenceWrapper = document.getElementById(`web-link__wrapper_${id}`)
    evidenceWrapper.innerHTML = updatedEvidence
}

/**
 * Goes through all pieces of evidence on the page and
 * fetches the weblinks for that piece of evidence
 */
async function updateAllWeblinks() {
    for (let evidenceId of EVIDENCE_IDS) {
        await getWebLinks(evidenceId);
    }
}

/**
 * Uses the Javascript fetch API to send the updated web link from the
 * edit modal. Then updates the web link on the page with the updated information.
 */
async function saveWebLink(id) {

    // Build the url with the repository information as parameters
    let url
    url = new URL (`${CONTEXT}/addWebLink-${id}`);
    url.searchParams.append("webLinkName", document.getElementById(`weblink-modal__name-field_${id}`).value)
    url.searchParams.append("webLink", document.getElementById(`weblink-modal__link-field_${id}`).value)
    url.searchParams.append("webLinkIndex", index);

    // Send a post request to update the web link
    // Receives the updated element HTML content as a response
    let updatedEvidence = await fetch(url, {
        method: "POST"
    }).then(res => {
        if (res.status === 400) {
            return ""
        } else {
            return res.text();
        }
    })
    // Update the page with the new HTML content
    if (updatedEvidence !== "") {
        const evidenceWrapper = document.getElementById(`web-link__wrapper_${id}`);
        evidenceWrapper.innerHTML = updatedEvidence;
        bootstrap.Modal.getInstance(document.getElementById(`addingWeblink_${id}`)).hide();
        updateWeblinks(id);
        return false;

    } else {
        document.getElementById("weblink-incorrect").hidden = false;
    }
}

// Clears the edit modal
function clearModel(id) {
    document.getElementById(`weblink-modal__name-field_${id}`).value = "";
    document.getElementById(`weblink-modal__link-field_${id}`).value = "";
    document.getElementById("weblink-incorrect").hidden = true;
    document.getElementById(`weblink-modal__title-${id}`).textContent = "Add Weblink";
}

//update display based on number of weblinks
function updateWeblinks(id) {
    const divs = document.querySelectorAll(`.web-links_${id}`);
    document.getElementById(`evidence-${id}-title__number-weblinks`).textContent = divs.length.toString();
    if (divs.length >= MAX_WEBLINKS) {
        document.getElementById("add-weblink-button__div").hidden = true;
    }
}

// Sets index that of the web link in the modal. The index is that from the evidence web links array.
function setIndex(i) {
    index = i;
}

// Sets the modal to have details from the web link to edit.
function editWebLink(name, link, safe, id) {
    let isTrueSet = (safe === 'true');
    document.getElementById("weblink-incorrect").hidden = true;
    if (name) {
        document.getElementById(`weblink-modal__name-field_${id}`).value = name;
        document.getElementById(`weblink-modal__title-${id}`).textContent = "Edit Weblink"
    }
    if (isTrueSet) {
        document.getElementById(`weblink-modal__link-field_${id}`).value = "https://" + link;
    } else {
        document.getElementById(`weblink-modal__link-field_${id}`).value = "http://" + link;
    }

    if (link === null) {
        document.getElementById(`weblink-modal__link-field_${id}`).value = "";
    }
}