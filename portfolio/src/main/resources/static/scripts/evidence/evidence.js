/**
 * Toggles a high five adding the user to th
 * @param evidenceId
 * @param userId
 * @returns {Promise<void>}
 */
async function toggleEvidence(evidenceId) {

    let url;
    url = new URL (`${CONTEXT}/evidence-${evidenceId}-highfive`);

    await fetch(url, {
        method: "POST"
    });

    await updateEvidenceFragment(evidenceId);
}

async function updateEvidenceFragment(evidenceId) {
    let url;
    url = new URL(`${CONTEXT}/evidence-${evidenceId}-highfive`);
    document.getElementById(`evidence-${evidenceId}-highfive`).innerHTML = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.text();
    });
}