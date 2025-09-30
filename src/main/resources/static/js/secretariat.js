//load secretariat content
function loadContent(pageOrUrl) {
  let urlMap = {
    etatsdavancement: "/req/alletat",
    emplois: "/req/emplois",
    changepass: "/req/changepass",
    accueilsecretariat: "/req/accueilsecretariat",
  };

  let url = urlMap[pageOrUrl] ?? pageOrUrl; //qlqs pqge ont des alias

  if (!url) {
    console.error("Unknown page:", page);
    return;
  }

  fetch(url)
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} - ${response.statusText}`);
      }
      return response.text();
    })
    .then((html) => {
      document.getElementById("secretariat-content").innerHTML = html;
      if (url.includes("accueilsecretariat")) {
        annulerModification(); //charger apres chargement de page
        setTimeout(() => createProgressChart(), 150);
        setTimeout(() => createProgressCharts(), 150);
      }

      if (url.includes("changepass")) {
        changerMotDePass(); //charger apres chargement de page
      }
      if (url.includes("emplois")) {
        trouverEmploidt();
        actionsEmploi();
      }
      if (url.includes("alletat")) {
        activerModification();
        annulerModification();
        trouverEtatdav();
      }
    })
    .catch((err) => {
      console.error("Failed to load content:", err);
      document.getElementById(
        "secretariat-content"
      ).innerHTML = `<p style="color:red;">Erreur de chargement</p>`;
    });
}

document.addEventListener("DOMContentLoaded", function () {
  loadContent("accueilsecretariat");
});
