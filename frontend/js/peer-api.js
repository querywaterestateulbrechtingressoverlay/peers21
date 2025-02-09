var currentPage = 0;
var totalPages = 0;

const apiUrl = "/api";

const apiUsername = "user";
const apiPassword = "password";

var tribeData = [];
var waveData = [];

const headers = new Headers();

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
    getPeerData(0, sort.column, sort.ascending,
      document.getElementById("tribe-filter").options[document.getElementById("tribe-filter").selectedIndex].text,
      document.getElementById("wave-filter").options[document.getElementById("wave-filter").selectedIndex].text);
  })
});

document.querySelectorAll(".first-page").forEach(e => {
  e.addEventListener("click", () => {
    getPeerData(0, sort.column, sort.ascending,
      document.getElementById("tribe-filter").options[document.getElementById("tribe-filter").selectedIndex].text,
      document.getElementById("wave-filter").options[document.getElementById("wave-filter").selectedIndex].text);
  });
});

document.querySelectorAll(".previous-page").forEach(e => {
  e.addEventListener("click", () => {
    getPeerData(currentPage - 1, sort.column, sort.ascending,
      document.getElementById("tribe-filter").options[document.getElementById("tribe-filter").selectedIndex].text,
      document.getElementById("wave-filter").options[document.getElementById("wave-filter").selectedIndex].text);
  });
});

document.querySelectorAll(".next-page").forEach(e => {
  e.addEventListener("click", () => {
    getPeerData(currentPage + 1, sort.column, sort.ascending,
      document.getElementById("tribe-filter").options[document.getElementById("tribe-filter").selectedIndex].text,
      document.getElementById("wave-filter").options[document.getElementById("wave-filter").selectedIndex].text);
  });
});

document.querySelectorAll(".last-page").forEach(e => {
  e.addEventListener("click", () => {
    getPeerData(totalPages - 1, sort.column, sort.ascending,
      document.getElementById("tribe-filter").options[document.getElementById("tribe-filter").selectedIndex].text,
      document.getElementById("wave-filter").options[document.getElementById("wave-filter").selectedIndex].text);
  });
});

function drawOrderIndicator() {
  const existingIndicator = document.querySelector("img");
  if (existingIndicator != null) {
    existingIndicator.remove();
  }
  const indicator = document.createElement("img");
  indicator.setAttribute("src", "images/arrow-" + (sort.ascending ? "up" : "down") + ".png");
  document.getElementById(sort.column).appendChild(indicator);
}

window.onload = async () => {
  headers.set(
    "Authorization", "Basic " + btoa(apiUsername + ":" + apiPassword)
  );
  console.log(btoa(apiUsername + ":" + apiPassword));
  await populateFilters();
  await getPeerData(currentPage, "login", true);
  // currentPage = pagination.currentPage;
  // totalPages = pagination.totalPages;
  drawOrderIndicator();
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
      tribe.setAttribute("id", "filter-" + t.name.toLowerCase());
      tribe.innerHTML = t.name;
      tribeFilter.appendChild(tribe);
    })
    const waveFilter = document.getElementById("wave-filter");
    waveData.forEach(w => {
      const wave = document.createElement("option");
      wave.setAttribute("id", "filter-" + w.toLowerCase());
      wave.innerHTML = w;
      waveFilter.appendChild(wave);
    })
    tribeFilter.addEventListener("change", (event) => {
      getPeerData(0, sort.column, sort.ascending,
        document.getElementById("tribe-filter").options[document.getElementById("tribe-filter").selectedIndex].text,
        document.getElementById("wave-filter").options[document.getElementById("wave-filter").selectedIndex].text);
    })
    waveFilter.addEventListener("change", (event) => {
      getPeerData(0, sort.column, sort.ascending,
        document.getElementById("tribe-filter").options[document.getElementById("tribe-filter").selectedIndex].text,
        document.getElementById("wave-filter").options[document.getElementById("wave-filter").selectedIndex].text);
    })
  } catch (error) {
    console.error(error.message);
  }
}

async function getPeerData(page, orderBy, ascending, tribe, wave) {
  const params = new URLSearchParams();
  params.append("page", page);
  params.append("orderBy", orderBy);
  params.append("orderAscending", ascending);
  if (tribe != null && tribe != "All") {
    params.append("tribeId", tribeData.find((t) => t.name == tribe).tribeId);
  }
  if (wave != null && wave != "All") {
    params.append("wave", wave);
  }
  try {
    const peerResponse = await fetch(apiUrl + "/peers?" + params, {
      headers: headers
    });
    const json = await peerResponse.json();
    const tableBody = document.querySelector("#peer-table tbody");
    if (json.size == 0) {
      tableBody.innerHTML = "no peers found";  
    } else {
      tableBody.innerHTML = "";
      json.peerData.forEach(peer => {
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
    }
    // const pagination = {
    //   currentPage: json.currentPage,
    //   totalPages: json.totalPages
    // }
    // return pagination;
    currentPage = json.currentPage;
    totalPages = json.totalPages;
    if (currentPage == 0) {
      document.querySelectorAll(".first-page").forEach(e => e.disabled = true);
      document.querySelectorAll(".previous-page").forEach(e => e.disabled = true);
    } else {
      document.querySelectorAll(".first-page").forEach(e => e.disabled = false);
      document.querySelectorAll(".previous-page").forEach(e => e.disabled = false);
    }
    if (currentPage == totalPages - 1) {
      document.querySelectorAll(".last-page").forEach(e => e.disabled = true);
      document.querySelectorAll(".next-page").forEach(e => e.disabled = true);
    } else {
      document.querySelectorAll(".last-page").forEach(e => e.disabled = false);
      document.querySelectorAll(".next-page").forEach(e => e.disabled = false);
    }
    document.querySelectorAll(".page-input-field").forEach(e => e.innerHTML = json.currentPage + 1);
  } catch (error) {
    console.error(error.message);
  }
}
