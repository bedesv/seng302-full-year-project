// Load Google Charts then update charts once it's done
google.charts.load("current", {packages:["corechart"]}).then(updateAllCharts);

// Update charts when the window resizes
window.onresize = updateAllCharts

/**
 * Updates all the charts
 */
async function updateAllCharts() {
    await updateCategoriesChart();
    await updateSkillsChart();
}

/**
 * Sends a get request to the backend for the specified type of group statistic data
 * @param dataType A string specifying what data is required
 */
async function fetchChartData(dataType) {
    let url;
    if (dataType === 'categories') {
        url = new URL (`${CONTEXT}/group-${GROUP_ID}-categoriesData?`);
    }
    if (dataType === 'skills') {
        url = new URL (`${CONTEXT}/group-${GROUP_ID}-skillsData?`);
    }
    return await fetch(url+new URLSearchParams({startDateString: START_DATE, endDateString: END_DATE}), {
        method: "GET",
    }).then(res => {
        return res.json();
    });
}

/**
 * Fetches categories data from the backend then creates a pie chart
 * with the received data
 */
async function updateCategoriesChart() {
    // Fetch updated chart data
    let chartData = await fetchChartData('categories')

    // Convert the json data to a format Google Chart can read
    let data = new google.visualization.DataTable();
    data.addColumn('string', 'word');
    data.addColumn('number', 'count');
    for (let key in chartData) {
        data.addRow([key, chartData[key]]);
    }

    // Specify options for the chart
    let options = {
        title: 'Categories',
        pieHole: 0.2,
        backgroundColor: { fill:'transparent' },
        pieSliceTextStyle: {color: 'black'}
    };

    // Create the categories chart
    let chart = new google.visualization.PieChart(document.getElementById('group-chart__categories-chart'));
    chart.draw(data, options);
}

/**
 * Fetches categories data from the backend then creates a pie chart
 * with the received data
 */
async function updateSkillsChart() {
    // Fetch updated chart data
    let chartData = await fetchChartData('skills')
    console.log(chartData);

    //https://howtodoinjava.com/java/sort/java-sort-map-by-values/
    //https://jsfiddle.net/api/post/library/pure/
    //https://developers.google.com/chart/interactive/docs/gallery/columnchart

    // Convert the json data to a format Google Chart can read
    let data = new google.visualization.DataTable();
    data.addColumn('string', 'Skills');
    data.addColumn('number', 'Count');
    for (let key in chartData) {
        data.addRow([key, chartData[key]]);
    }

    // Specify options for the column chart
    let options = {
        title: 'Skills',
        backgroundColor: { fill:'transparent' }
    };

    // Create the categories chart
    let chart = new google.visualization.ColumnChart(document.getElementById('group-chart__skills-chart'));
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

