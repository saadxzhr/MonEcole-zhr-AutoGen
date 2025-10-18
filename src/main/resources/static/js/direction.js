//load direction content
function loadContent(pageOrUrl) {
  let urlMap = {
    etatsdavancement: "/req/alletat",
    emplois: "/req/emplois",
    changepass: "/req/changepass",
    filieres: "/req/filieres",
    matieres: "/req/matieres",
    employes: "/req/employes",
    modulex: "/api/v1/modulex",
    users: "/req/users",
    accueildirection: "/req/accueildirection",
  };

  let url = urlMap[pageOrUrl] ?? pageOrUrl; //qlqs page ont des alias

  if (!url) {
    console.error("Unknown page:", page);
    return;
  }

  // Read CSRF token from meta tag
  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

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
      document.getElementById("direction-content").innerHTML = html;
      if (url.includes("accueildirection")) {
        //activer/'desactiver' modification
        document.addEventListener("click", function (event) {
          if (event.target.classList.contains("activer-btn")) {
            toggleStatut(event.target);
          }
        });
        //executer fonction
        afficherEmployes("toggleEmployesBtn", "employesList");
        afficherMatieres("toggleMatieresBtn", "matieresList");
        afficherFilieres("toggleFilieresBtn", "filieresList");
        setTimeout(() => createProgressChart(), 150);
        setTimeout(() => createProgressCharts(), 150);
      }
      if (url.includes("users")) {
        actionsUsers();
        trouverUser();
      }
      if (url.includes("employes")) {
        actionsEmploye();
        trouverEmploye();
      }
      if (url.includes("matieres")) {
        actionsMatiere();
        trouverMatiere();
      }
      if (url.includes("filieres")) {
        actionFiliere();
        trouverFiliere();
      }
      if (url.includes("modulex")) {
        actionModulex();
      }
      if (url.includes("changepass")) {
        changerMotDePasse();
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
            exportTableToExcel("emploiTable", "EmploisDutemps.xlsx");
          });
      }
      if (url.includes("alletat")) {
        trouverEtatdav();

        //Activer/desactiver modification
        document.addEventListener("click", function (event) {
          if (event.target.classList.contains("activer-btn")) {
            toggleStatut(event.target);
          }
        });

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
      document.getElementById(
        "direction-content"
      ).innerHTML = `<p style="color:red;">Erreur de chargement</p>`;
    });
}
document.addEventListener("DOMContentLoaded", function () {
  loadContent("accueildirection");

  // Attach logout function
  const logoutBtn = document.getElementById("logoutBtn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", (e) => {
      e.preventDefault();
      logoutUser();
    });
  }
});
