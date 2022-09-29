/**
 * Upon entering user page uses the Javascript fetch API to fetch user portfolio and display it in the portfolio tab
 */
async function updateUserPortfolio() {
    // Build the url
    let url
    url = new URL (`${CONTEXT}/portfolio-${USER_ID}`)
    url.searchParams.append("portfolioLinks", 'T')

    // Send a get request to fetch the user portfolio
    // Receives the updated element HTML content as a response
    let portfolio = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.text()
    })

    // Update the page with the new HTML content
    const portfolioWrapper = document.getElementById("portfolio_container")
    portfolioWrapper.innerHTML = portfolio

    updateEvidenceIds();
    updateLikes();
    await updateAllWeblinks();
}

async function updateUserPortfolioWithSkill(selectedSkill) {
    // Build the url
    let url
    url = new URL (`${CONTEXT}/portfolio-${USER_ID}-skill`)
    url.searchParams.append("skill", selectedSkill)
    url.searchParams.append("portfolioLinks", 'T')

    // Send a get request to fetch the user portfolio
    // Receives the updated element HTML content as a response
    const portfolio = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.text()
    })

    // Update the page with the new HTML content
    const portfolioWrapper = document.getElementById("portfolio_container")
    portfolioWrapper.innerHTML = portfolio
    updateEvidenceIds();
    updateLikes();
    await updateAllWeblinks();
}

async function updateUserPortfolioWithCategory(selectedCategory) {
    // Build the url
    let url

    url = new URL (`${CONTEXT}/portfolio-${USER_ID}-categories`)
    url.searchParams.append("category", selectedCategory)
    url.searchParams.append("portfolioLinks", 'T')
    console.log(url)

    // Send a get request to fetch the user portfolio
    // Receives the updated element HTML content as a response
    const portfolio = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.text()
    })
    // Update the page with the new HTML content
    const portfolioWrapper = document.getElementById("portfolio_container")
    portfolioWrapper.innerHTML = portfolio
    updateEvidenceIds();
    updateLikes();
    await updateAllWeblinks();
}

/**
 * Saves the id of all the pieces of evidence on the page
 */
function updateEvidenceIds() {
    const evidenceObjects = document.getElementsByClassName("evidence__details");
    EVIDENCE_IDS = []
    for (let evidence of evidenceObjects) {
        EVIDENCE_IDS.push(evidence.id.split("_")[1])
    }
}

/**
 * Updates all the likes for each piece of evidence
 */
function updateLikes() {
    for (let evidenceId of EVIDENCE_IDS) {
        updateLikeFragment(evidenceId)
    }
}