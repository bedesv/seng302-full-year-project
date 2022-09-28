/**
 * Upon entering user page uses the Javascript fetch API to fetch user portfolio and display it in the portfolio tab
 */
async function updateUserPortfolio() {
    // Build the url
    let url
    url = new URL (`${CONTEXT}/portfolio-${USER_ID}`)

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
    await updateAllWeblinks();
}

async function updateUserPortfolioWithSkill(selectedSkill) {
    // Build the url
    let url
    url = new URL (`${CONTEXT}/portfolio-${USER_ID}-skill?`)

    // Send a get request to fetch the user portfolio
    // Receives the updated element HTML content as a response
    const portfolio = await fetch(url+new URLSearchParams({skill: selectedSkill}), {
        method: "GET"
    }).then(res => {
        return res.text()
    })

    // Update the page with the new HTML content
    const portfolioWrapper = document.getElementById("portfolio_container")
    portfolioWrapper.innerHTML = portfolio
    updateEvidenceIds();
    await updateAllWeblinks();
}

async function updateUserPortfolioWithCategory(selectedCategory) {
    // Build the url
    let url
    url = new URL (`${CONTEXT}/portfolio-${USER_ID}-categories?`)

    // Send a get request to fetch the user portfolio
    // Receives the updated element HTML content as a response
    const portfolio = await fetch(url+new URLSearchParams({category: selectedCategory}), {
        method: "GET"
    }).then(res => {
        return res.text()
    })
    // Update the page with the new HTML content
    const portfolioWrapper = document.getElementById("portfolio_container")
    portfolioWrapper.innerHTML = portfolio
    updateEvidenceIds();
    await updateAllWeblinks();
}

/**
 * Saves the id of all the pieces of evidence on the page
 */
function updateEvidenceIds() {
    const evidenceObjects = document.getElementsByClassName("evidence__details");
    let EVIDENCE_IDS = []
    for (let evidence of evidenceObjects) {
        EVIDENCE_IDS.push(evidence.id.split("_")[1])
    }
}