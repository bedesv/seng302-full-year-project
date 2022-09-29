
/**
 * Fetches the pieces of evidence in the group, up to the pre-defined limit, and updates the evidence on the page
 */
async function resetEvidenceFilter() {
    // Fetch and update the new evidence
    let url;
    url = new URL (`${CONTEXT}/group-${GROUP_ID}-evidence`);
    document.getElementById("group-home__evidence-container").innerHTML = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.text();
    });

    // Update the header
    document.getElementById("group-home__evidence-title").textContent = "Recent Evidence";
    await updateEvidenceWeblinks();
}

/**
 * Fetches the pieces of evidence in the group with the given skill and updates the evidence on the page
 * @param skill The skill tag to filter by. Is '#no_skill' if wanting to find evidence with no skill
 */
async function fetchEvidenceWithSkill(skill) {
    // Fetch and update the new evidence
    let url;
    url = new URL (`${CONTEXT}/group-${GROUP_ID}-evidence-skill`);
    url.searchParams.append("skill", skill)
    document.getElementById("group-home__evidence-container").innerHTML = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.text();
    });

    // Update the header
    let title
    if (skill !== "#no_skill") {
        title = "Evidence with skill: " + skill.replace("_", " ");
    } else {
        title = "Evidence with no skill";
    }
    document.getElementById("group-home__evidence-title").textContent = title;
    await updateEvidenceWeblinks();
}

async function fetchEvidenceWithCategories(category) {
    // Fetch and update the new evidence
    let url;
    url = new URL (`${CONTEXT}/group-${GROUP_ID}-evidence-categories`);
    url.searchParams.append("category", category)
    document.getElementById("group-home__evidence-container").innerHTML = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.text();
    });

    // Update the header
    let title;
    if (category !== "#no_categories") {
        title = "Evidence with category: " + category;
    } else {
        title = "Evidence with no category";
    }
    document.getElementById("group-home__evidence-title").textContent = title;
    await updateEvidenceWeblinks();
}

/**
 * Finds all pieces of evidence on the page and fetches their weblinks
 */
async function updateEvidenceWeblinks() {
    const evidenceObjects = document.getElementsByClassName("evidence__details");
    for (let evidence of evidenceObjects) {
        await getWebLinks(evidence.id.split("_")[1]);
    }
}