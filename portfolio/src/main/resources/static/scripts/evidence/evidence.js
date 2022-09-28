/**
 * Toggles a high five adding the user to th
 * @param evidenceId
 * @param userId
 * @returns {Promise<void>}
 */
async function toggleEvidence(evidenceId, userId) {

    let url;
    url = new URL (`${CONTEXT}/evidence-${evidenceId}-highfive?`);

    await fetch(url + new URLSearchParams({userId: userId}), {
        method: "POST"
    });

    await updateEvidenceFragment(evidenceId);
}

async function updateEvidenceFragment(evidenceId) {
    let url;
    url = new URL(`${CONTEXT}/evidence-${evidenceId}`);
    document.getElementById(`evidence-${evidenceId}`).innerHTML = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.text();
    });
}