function loadContent(pageOrUrl) {
  const urlMap = {
    etatsdavancement: "/req/alletat",
    emplois: "/req/emplois",
    changepass: "/req/changepass",
    accueilsecretariat: "/req/accueilsecretariat",
  };

  const url = urlMap[pageOrUrl] ?? pageOrUrl;
  if (!url) {
    console.error("Unknown page:", pageOrUrl);
    return;
  }

  const contentContainer = document.getElementById("secretariat-content");
  const loader = document.getElementById("loading-overlay");

  // Show loader if loading takes more than 100ms
  const loaderTimeout = setTimeout(() => {
    loader.style.display = "flex";
  }, 200);

  // Read CSRF token from meta tag
  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector(
    'meta[name="_csrf_header"]'
  ).content;

  fetch(url, {
    method: "GET",
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} - ${response.statusText}`);
      }
      return response.text();
    })
    .then((html) => {
      contentContainer.innerHTML = html;

      // Run any page-specific logic after loading
      if (url.includes("accueilsecretariat")) {
        //activer/'desactiver' modification
        document.addEventListener("click", function (event) {
          if (event.target.classList.contains("activer-btn")) {
            toggleStatut(event.target);
          }
        });
        setTimeout(() => createProgressChart(), 150);
        setTimeout(() => createProgressCharts(), 150);
      }
      if (url.includes("changepass")) {
        changerMotDePass();
      }
      if (url.includes("emplois")) {
        trouverEmploidt();
        actionsEmploi();
        document
          .querySelectorAll(".generate-recurring-form")
          .forEach((form) => {
            form.addEventListener("submit", function (e) {
              e.preventDefault();
              submitGenerateRecurring(form);
            });
          });
        document
          .getElementById("exportToExcel")
          .addEventListener("click", function () {
            exportTableToExcel("emploiTable", "Etat_rapport.xlsx");
          });
      }
      if (url.includes("alletat")) {
        //activer/'desactiver' modification
        document.addEventListener("click", function (event) {
          if (event.target.classList.contains("activer-btn")) {
            toggleStatut(event.target);
          }
        });
        //filtrer
        trouverEtatdav();
        // Event listener for the export button to trigger export when clicked
        document
          .getElementById("exportToExcel")
          .addEventListener("click", function () {
            exportTableToExcel("etatTable", "Etat_rapport.xlsx");
          });
      }
    })
    .catch((err) => {
      console.error("Failed to load content:", err);
      contentContainer.innerHTML = `<p style="color:red;">Erreur de chargement</p>`;
    })
    .finally(() => {
      clearTimeout(loaderTimeout);
      loader.style.display = "none";
    });
    


}

// ✅ First-time content load when the app loads
document.addEventListener("DOMContentLoaded", function () {
  loadContent("accueilsecretariat");

  // Attach logout function
  const logoutBtn = document.getElementById("logoutBtn");
  if (logoutBtn) {
      logoutBtn.addEventListener("click", (e) => {
          e.preventDefault();
          logoutUser();
      });
  }
});
