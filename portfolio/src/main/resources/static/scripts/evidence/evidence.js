/**
 * Toggles a high five adding the user to th
 * @param evidenceId the evidence fragment that the like icon belongs too.
 */
async function toggleEvidence(evidenceId) {
    let url;
    url = new URL (`${CONTEXT}/evidence-${evidenceId}-like`);

    await fetch(url, {
        method: "POST"
    });

    await updateLikeFragment(evidenceId);
}

/**
 * Updates the like fragment when a user clicks on the high five icon
 * @param evidenceId the evidence fragment that the like icon belongs too.
 */
async function updateLikeFragment(evidenceId) {
    let url;
    url = new URL(`${CONTEXT}/evidence-${evidenceId}-like`);
    document.getElementById(`evidence-${evidenceId}-like`).innerHTML = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.text();
    });
}

