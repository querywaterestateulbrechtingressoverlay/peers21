var currentPage = 0;
var totalPages = 0;

const mockApiUrl = "/mockapi"
const apiUrl = "/api/frontend";

var successfulLogin = false;
var tokenExpiry;

var tribeData = [];
var waveData = [];

var headers = new Headers();

const sort = {
  column: "login",
  ascending: true
}

document.querySelector("#login-form").addEventListener("submit", async (event) =>  {
  event.preventDefault();
  const username = new FormData(event.target).get("username");
  const response = await fetch("/api/auth/login", {
    method: 'POST',
    headers: {
      Authorization: "Basic " + btoa(username + ":password")
    }
  });
  if (response.ok) {
    const responseBody = await response.json();
    headers = new Headers(
      {
        Authorization: "Bearer " + responseBody.token
      }
    );
    successfulLogin = true;
    tokenExpiry = Date.now() + responseBody.expiry * 1000;
    const loginElement = document.getElementById("login-div");
    loginElement.innerHTML = "";
    const userInfo = document.createElement("p");
    userInfo.innerHTML = "Username: " + username;
    loginElement.appendChild(userInfo);    
    await populateFilters();
    await getPeerData(currentPage, "login", true);
  } else {
    headers = new Headers();
  }
})

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
  await populateFilters();
  await getPeerData(currentPage, "login", true);
  // currentPage = pagination.currentPage;
  // totalPages = pagination.totalPages;
  drawOrderIndicator();
}

async function populateFilters() {
  try {
    const tribeResponse = await fetch((successfulLogin ? apiUrl : mockApiUrl) + "/tribes", {
      headers: headers
    });
    tribeData = (await tribeResponse.json()).tribes;
    const waveResponse = await fetch((successfulLogin ? apiUrl : mockApiUrl) + "/waves", {
      headers: headers
    });
    waveData = await waveResponse.json();
    const tribeFilter = document.getElementById("tribe-filter");
    tribeFilter.innerHTML = "<option>All</option>";
    tribeData.forEach(t => {
      const tribe = document.createElement("option");
      tribe.setAttribute("id", "filter-" + t.name.toLowerCase());
      tribe.innerHTML = t.name;
      tribeFilter.appendChild(tribe);
    })
    const waveFilter = document.getElementById("wave-filter");
    waveFilter.innerHTML = "<option>All</option>";
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
    params.append("tribeId", tribeData.find((t) => t.name == tribe).id);
  }
  if (wave != null && wave != "All") {
    params.append("wave", wave);
  }
  try {
    const peerResponse = await fetch((successfulLogin ? apiUrl : mockApiUrl) + "/peers?" + params, {
      headers: headers
    });
    if (peerResponse.ok) {
      const json = await peerResponse.json();
      const tableBody = document.querySelector("#peer-table tbody");
      if (json.peerData.size == 0) {
        tableBody.innerHTML = "no peers found";  
      } else {
        tableBody.innerHTML = "";
        json.peerData.forEach(peer => {
          const row = document.createElement("tr");
          row.innerHTML = `
            <td>${peer.login}</tr>
            <td>${peer.wave}</tr>
            <td>${tribeData.find((t) => t.id == peer.tribeId).name}</tr>
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
    }
  } catch (error) {
    console.error(error.message);
  }
}
