// Load Google Charts then update charts once it's done
google.charts.load("current", {packages:["corechart"]}).then(updateAllCharts);

// Update charts when the window resizes
window.onresize = updateAllCharts

/**
 * Updates all the charts
 */
async function updateAllCharts() {
    await updateSkillsChart();
}


/**
 * Sends a get request to the backend for the specified type of user statistic data
 * @param dataType A string specifying what data is required
 */
async function fetchChartData(dataType) {
    let url;
    if (dataType === 'skills') {
        url = new URL (`${CONTEXT}/user-${USER_ID}-skillsData?`);
    }
    return await fetch(url+new URLSearchParams({parentProjectId: PROJECT_ID, startDateString: START_DATE, endDateString: END_DATE}), {
        method: "GET",
    }).then(res => {
        return res.json();
    });
}

/**
 * Fetches skills data from the backend then creates a column chart
 * with the received data
 */
async function updateSkillsChart() {
    // Fetch updated chart data
    let chartData = await fetchChartData('skills')

    // Convert the json data to a format Google Chart can read
    let data = new google.visualization.DataTable();
    data.addColumn('string', 'Skills');
    data.addColumn('number', 'Number of Skills');
    for (let key in chartData) {
        data.addRow([key, chartData[key]]);
    }

    // Specify options for the column chart
    let options = {
        title: 'Top 10 Skills',
        titleTextStyle: {fontSize: 20},
        backgroundColor: { fill:'transparent' },
    };

    // Create the categories chart
    let chart = new google.visualization.ColumnChart(document.getElementById('user-chart__skills-chart'));
    chart.draw(data, options);
}