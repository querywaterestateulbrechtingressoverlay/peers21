async function getPeerData() {
  const apiUrl = "http://127.0.0.1:8080/api/peers";
  const apiUsername = "abbottme";
  const apiPassword = "password";
  const headers = new Headers();
  headers.set(
    "Authorization", "Basic " + btoa(apiUsername + ":" + apiPassword)
  );
  try {
    console.log("*" + btoa(apiUsername + ":" + apiPassword) + "*");
    const peerResponse = await fetch(apiUrl, {
      headers: headers
    });
    // if (!peerResponse.ok) {
    //   console
    //   console.log(peerResponse.headers);
    //   console.log(peerResponse.body);
    //   throw new Error(`Response status: ${peerResponse.status}`);
    // }
    const json = await peerResponse.json();
    console.log(json);

    const tableBody = document.querySelector("#peer-table tbody");
    tableBody.innerHTML = "";
    json.forEach(peer => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${peer.login}</tr>
        <td>${peer.wave}</tr>
        <td>${peer.tribeId}</tr>
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
window.onload = getPeerData;