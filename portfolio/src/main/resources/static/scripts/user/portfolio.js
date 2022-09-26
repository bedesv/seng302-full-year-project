/**
 * If the user is in the portfolio and edits, adds or deletes evidence the user is returned to the portfolio tab
 * index 0 is profile
 * index 1 is portfolio
 */
function makePortfolioActive() {
    console.log(IN_PORTFOLIO)
    if(IN_PORTFOLIO === 'true') {
        const carouselItems = document.getElementsByClassName("carousel-item")
        carouselItems[0].className = "carousel-item"
        carouselItems[1].classList.add("active")
    }
}


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
}