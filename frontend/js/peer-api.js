var page = 1;

const apiUrl = "http://localhost:8080/api";

const apiUsername = "user";
const apiPassword = "password";

var tribeData = [];
var waveData = [];

const headers = new Headers();
headers.set(
  "Authorization", "Basic " + btoa(apiUsername + ":" + apiPassword)
);

const sort = {
  column: "login",
  ascending: true
}

const tableB = document.querySelectorAll("#peer-table thead td")
tableB.forEach((e) => {
  e.addEventListener("click", () => {
    if (sort.column == e.id) {
      sort.ascending = !sort.ascending;
    } else {
      sort.column = e.id;
      sort.ascending = true;
    }
    drawOrderIndicator();
    getPeerData(page, sort.column, sort.ascending);
  })
})

function drawOrderIndicator() {
  const existingIndicator = document.querySelector("img");
  if (existingIndicator != null) {
    existingIndicator.remove();
  }
  const indicator = document.createElement("img");
  indicator.setAttribute("src", "images/arrow-" + (sort.ascending ? "up" : "down") + ".png");
  document.getElementById(sort.column).appendChild(indicator);
}

window.onload = () => {
  drawOrderIndicator();
  populateFilters().then();
  getPeerData(1, "login", true);
}

async function populateFilters() {
  try {
    const tribeResponse = await fetch(apiUrl + "/tribes", {
      headers: headers
    });
    tribeData = await tribeResponse.json();
    const waveResponse = await fetch(apiUrl + "/waves", {
      headers: headers
    });
    waveData = await waveResponse.json();
    const tribeFilter = document.getElementById("tribe-filter");
    tribeData.forEach(t => {
      const tribe = document.createElement("option");
      tribe.innerHTML = t.name;
      tribeFilter.appendChild(tribe);
    })
    const waveFilter = document.getElementById("wave-filter");
    waveData.forEach(w => {
      const wave = document.createElement("option");
      wave.innerHTML = w;
      waveFilter.appendChild(wave);
    })
    tribeFilter.addEventListener("change", (event) => {
      console.log(event.target.value);
    })
  } catch (error) {
    console.error(error.message);
  }
}

async function getPeerData(page, orderBy, ascending) {
  const params = new URLSearchParams();
  params.append("orderBy", orderBy);
  params.append("orderAscending", ascending);
  try {
    const peerResponse = await fetch(apiUrl + "/peers?" + params, {
      headers: headers
    });
    const json = await peerResponse.json();
    const tableBody = document.querySelector("#peer-table tbody");
    tableBody.innerHTML = "";
    json.forEach(peer => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${peer.login}</tr>
        <td>${peer.wave}</tr>
        <td>${tribeData.find((t) => t.tribeId == peer.tribeId).name}</tr>
        <td>${peer.expValue}</tr>
        <td>${peer.peerReviewPoints}</tr>
        <td>${peer.codeReviewPoints}</tr>
      `
      tableBody.appendChild(row);
    })
  } catch (error) {
    console.error(error.message);
  }
}
