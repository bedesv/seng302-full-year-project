/**
 * Toggles a high five adding the user to th
 * @param evidenceId the evidence fragment that the high-five icon belongs too.
 */
async function toggleEvidence(evidenceId) {

    let url;
    url = new URL (`${CONTEXT}/evidence-${evidenceId}-highfive`);

    await fetch(url, {
        method: "POST"
    });

    await updateHighFiveFragment(evidenceId);
}

/**
 * Updates the highFive fragment when a user clicks on the high five icon
 * @param evidenceId the evidence fragment that the high-five icon belongs too.
 */
async function updateHighFiveFragment(evidenceId) {
    let url;
    url = new URL(`${CONTEXT}/evidence-${evidenceId}-highfive`);
    document.getElementById(`evidence-${evidenceId}-highfive`).innerHTML = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.text();
    });
}