google.charts.load("current", {packages:["corechart"]});

async function updateGroupGraphElement() {
    let url
    url = new URL (`${CONTEXT}/group-${GROUP_ID}-categoriesData`)
    const chartData = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.json();
    })
    drawChart(chartData)
}

function drawChart(chartData) {

    var data = new google.visualization.DataTable();
    data.addColumn('string', 'word');
    data.addColumn('number', 'count');
    console.log(chartData)
    for (let key in chartData) {
        data.addRow([key, chartData[key]]);
    }

    var options = {
        title: 'My Daily Activities',
        pieHole: 0.4,
    };

    var chart = new google.visualization.PieChart(document.getElementById('group-chart-div'));
    chart.draw(data, options);
}

/**
 * Uses the Javascript fetch API to fetch updated repository information
 * then updates the page to reflect the new information.
 */
async function updateGroupRepositoryElement(firstLoad) {
    // Build the url
    let url
    url = new URL (`${CONTEXT}/group-${GROUP_ID}-repository`)
    url.searchParams.append("firstLoad", firstLoad)

    // Send a get request to fetch the updated group repository
    // Receives the updated element HTML content as a response
    const updatedRepositoryInformation = await fetch(url, {
        method: "GET"
    }).then(res => {
        return res.text()
    })


    // Update the page with the new HTML content
    const groupRepositoryWrapper = document.getElementById("repository_container")
    groupRepositoryWrapper.innerHTML = updatedRepositoryInformation

    if(!userInGroup && !authUserIsTeacher) {
        document.getElementById("open-modal__button").hidden = true;
    }

}

/**
 * Uses the Javascript fetch API to send the updated repository information from the
 * edit modal. Then updates the repository information on the page with the updated information.
 */
async function saveGroupRepositorySettings() {
    bootstrap.Modal.getInstance(document.getElementById("group-repository-settings__modal")).hide()
    // Build the url with the repository information as parameters
    let url
    url = new URL (`${CONTEXT}/group-${GROUP_ID}-repository`)
    url.searchParams.append("repositoryName", document.getElementById("group-repository__name").value)
    url.searchParams.append("gitlabAccessToken", document.getElementById("group-repository__api-key").value)
    url.searchParams.append("gitlabProjectId", document.getElementById("group-repository__id").value)
    url.searchParams.append("gitlabServerUrl", document.getElementById("group-repository__server-url").value)


    // Send a post request to update the group repository
    // Receives the updated element HTML content as a response
    const updatedRepositoryInformation = await fetch(url, {
        method: "POST"
    }).then(res => {
        return res.text()
    })

    // Update the page with the new HTML content
    const groupRepositoryWrapper = document.getElementById("repository_container")
    groupRepositoryWrapper.innerHTML = updatedRepositoryInformation
    return false;
}

