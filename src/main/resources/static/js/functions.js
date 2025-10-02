//Changer mot de pass
function changerMotDePass() {
  const submitButton = document.getElementById("submit");
  if (!submitButton) return;

  // Remove existing listeners (in case fragment reloaded)
  const newButton = submitButton.cloneNode(true);
  submitButton.parentNode.replaceChild(newButton, submitButton);

  newButton.addEventListener("click", async (e) => {
    e.preventDefault();

    const oldPassword = document.getElementById("password").value.trim();
    const newPassword = document.getElementById("newpass").value.trim();
    const confirmPassword = document.getElementById("newpassconf").value.trim();

    if (!oldPassword || !newPassword || !confirmPassword) {
      alert("Tous les champs sont obligatoires.");
      return;
    }

    if (newPassword !== confirmPassword) {
      alert("Les nouveaux mots de passe ne correspondent pas.");
      return;
    }

    const data = {
      password: oldPassword,
      newpass: newPassword,
    };

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    // Disable button to prevent multiple clicks
    newButton.disabled = true;

    try {
      const response = await fetch("/req/changepass", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          [csrfHeader]: csrfToken,
        },
        body: JSON.stringify(data),
      });

      if (!response.ok) throw new Error(`HTTP ${response.status}`);

      const result = await response.json(); // expecting JSON from server
      if (result.success) {
        alert("Mot de passe modifié avec succès !");
        // Optionally, clear inputs
        document.getElementById("password").value = "";
        document.getElementById("newpass").value = "";
        document.getElementById("newpassconf").value = "";
      } else {
        alert("Erreur: " + (result.message || "Impossible de changer le mot de passe."));
      }
    } catch (error) {
      alert("Erreur réseau : " + error);
    } finally {
      newButton.disabled = false;
    }
  });
}


//Trouver un emploi
//Filtrer emplois du temps
function trouverEmploidt() {
  const dateInput = document.getElementById("searchDate");
  const formateurSelect = document.getElementById("filterFormateur");
  const filiereSelect = document.getElementById("filterFiliere");
  const matiereSelect = document.getElementById("filterMatiere");
  const rows = document.querySelectorAll("#emploiTable tbody tr");

  function filterRows() {
    const dateValue = dateInput.value.toLowerCase();
    const selectedCIN = formateurSelect.value;
    const selectedFiliereId = filiereSelect.value;
    const selectedMatiereCode = matiereSelect.value;

    rows.forEach((row) => {
      const cells = row.querySelectorAll("td");
      const date = cells[1] ? cells[1].textContent.toLowerCase() : "";

      const rowCIN = row.getAttribute("data-cin");
      const rowFiliereId = row.getAttribute("data-filiere-id");
      const rowMatiereCode = row.getAttribute("data-code-matiere");

      const matchDate = !dateValue || date.includes(dateValue);
      const matchFormateur = !selectedCIN || rowCIN == selectedCIN;
      const matchFiliere = !selectedFiliereId || rowFiliereId == selectedFiliereId;
      const matchMatiere = !selectedMatiereCode || rowMatiereCode == selectedMatiereCode;

      row.style.display =
        matchDate && matchFormateur && matchFiliere && matchMatiere
          ? ""
          : "none";
    });
  }

  function filterMatiereOptionsByFiliere() {
    const selectedFiliereId = filiereSelect.value;
    const matiereOptions = matiereSelect.querySelectorAll("option");

    matiereOptions.forEach((option) => {
      if (!option.value) return;
      const optionFiliereId = option.getAttribute("data-filiere-id");
      option.style.display =
        !selectedFiliereId || optionFiliereId == selectedFiliereId ? "" : "none";
    });

    matiereSelect.value = "";
  }

  dateInput.addEventListener("input", filterRows);
  formateurSelect.addEventListener("change", filterRows);
  filiereSelect.addEventListener("change", () => {
    filterMatiereOptionsByFiliere();
    filterRows();
  });
  matiereSelect.addEventListener("change", filterRows);

  // 🟢 Initial run
  filterRows();
}




//Trouver dans etat d'av par id
function trouverEtatdav() {
  const dateInput = document.getElementById("etatSearchDate");
  const statutSelect = document.getElementById("etatFilterStatut");
  const formateurSelect = document.getElementById("etatFilterFormateur");
  const filiereSelect = document.getElementById("etatFilterFiliere");
  const matiereSelect = document.getElementById("etatFilterMatiere");
  const rows = document.querySelectorAll("#etatTable tbody tr");

  function filterRows() {
    const dateValue = dateInput.value.toLowerCase();
    const statutValue = statutSelect.value;
    const formateurId = formateurSelect.value;
    const filiereId = filiereSelect.value;
    const matiereId = matiereSelect.value;

    rows.forEach((row) => {
      const cells = row.querySelectorAll("td");
      const dateCell = cells[1]?.textContent.toLowerCase();

      const rowStatut = row.dataset.statut;
      const rowFormateurId = row.dataset.formateurId;
      const rowFiliereId = row.dataset.filiereId;
      const rowMatiereId = row.dataset.matiereId;

      const matchDate = !dateValue || dateCell.includes(dateValue);
      const matchStatut = !statutValue || rowStatut === statutValue;
      const matchFormateur = !formateurId || rowFormateurId === formateurId;
      const matchFiliere = !filiereId || rowFiliereId === filiereId;
      const matchMatiere = !matiereId || rowMatiereId === matiereId;

      row.style.display = matchDate && matchStatut && matchFormateur && matchFiliere && matchMatiere ? "" : "none";
    });
  }

  // === Filter Matiere options based on selected Filiere ===
  function filterMatiereOptionsByFiliere() {
    const selectedFiliereId = filiereSelect.value;
    const matiereOptions = matiereSelect.querySelectorAll("option");

    matiereOptions.forEach((option) => {
      if (!option.value) return; // Skip "Toutes les matières"
      const optionFiliereId = option.getAttribute("data-filiere-id");
      option.style.display = !selectedFiliereId || optionFiliereId === selectedFiliereId ? "" : "none";
    });

    // Reset the matiere selection when filiere changes
    matiereSelect.value = "";
  }

  dateInput.addEventListener("input", filterRows);
  statutSelect.addEventListener("change", filterRows);
  formateurSelect.addEventListener("change", filterRows);
  filiereSelect.addEventListener("change", () => {
    filterMatiereOptionsByFiliere();
    filterRows();
  });
  matiereSelect.addEventListener("change", filterRows);
}



//Activer modification etat D'av
// function activerModification() {
//   document.addEventListener("click", function (event) {
//     if (event.target.classList.contains("activer-btn")) {
//       const button = event.target;
//       const row = button.closest("tr");
//       const id = button.getAttribute("data-id");

//       const csrfToken = document.querySelector('meta[name="_csrf"]').content;
//       const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

//       // Send update to Spring Boot
//       fetch("/req/etat/statut", {
//         method: "POST",
//         headers: {
//           "Content-Type": "application/json",
//           [csrfHeader]: csrfToken
//         },
//         body: JSON.stringify({ id: id, statut: "A modifier" }),
//       })
//         .then((response) => {
//           if (response.ok) {
//             // Disable the button immediately
//             button.disabled = true;
//             button.textContent = "Modification activée";
//             button.classList.remove("btn-warning");
//             button.classList.add("btn-secondary");

//             // Update statut column in the UI (column index 5 = statut)
//             const statutCell = row.querySelectorAll("td")[6];
//             statutCell.textContent = "A modifier";
//           } else {
//             alert("Échec de la mise à jour du statut.");
//           }
//         })
//         .catch((err) => {
//           alert("Erreur réseau : " + err);
//         });
//     }
//   });
// }

//Annuler modification etat D'av
// function annulerModification() {
//   document.addEventListener("click", function (event) {
//     if (event.target.classList.contains("activer-btn")) {
//       const button = event.target;
//       const row = button.closest("tr");
//       const id = button.getAttribute("data-id");

//       // Read CSRF token from meta tag
//       const csrfToken = document.querySelector('meta[name="_csrf"]').content;
//       const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;


//       // Send update to Spring Boot
//       fetch("/req/etat/annuler", {
//         method: "POST",
//         headers: {
//           "Content-Type": "application/json",
//           [csrfHeader]: csrfToken
//         },
//         body: JSON.stringify({ id: id }),
//       })
//         .then((response) => {
//           if (response.ok) {
//             // Disable the button immediately
//             button.disabled = true;
//             button.textContent = "Modification Annulée";
//             button.classList.remove("btn-warning");
//             button.classList.add("btn-secondary");

//             // Update statut column in the UI (column index 5 = statut)
//             const statutCell = row.querySelectorAll("td")[6];
//             statutCell.textContent = "Modification Annulée";
//           } else {
//             alert("Échec de la mise à jour du statut.");
//           }
//         })
//         .catch((err) => {
//           alert("Erreur réseau : " + err);
//         });
//     }
//   });
// }


//Activer/Annuler modification etat D'av
function toggleStatut(button) {
  const id = button.getAttribute("data-id");
  const currentStatut = button.getAttribute("data-statut");

  // Decide new statut
  const newStatut = currentStatut === "Rempli" ? "A modifier" : "Rempli";

  // CSRF token
  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  // Prevent spamming
  button.disabled = true;

  fetch("/req/etat/toggle", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      [csrfHeader]: csrfToken
    },
    body: JSON.stringify({ id, newStatut })
  })
    .then((res) => {
      if (!res.ok) throw new Error("Server error");

      // Update button attributes
      button.setAttribute("data-statut", newStatut);
      button.textContent =
        newStatut === "Rempli" ? "Activer modification" : "Annuler modification";

      button.classList.toggle("btn-secondary", newStatut === "Rempli");
      button.classList.toggle("btn-warning", newStatut === "A modifier");

      // Update statut column in same row
      const row = button.closest("tr");
      if (row) {
        const statutCell = row.querySelectorAll("td")[6]; // index of statut column
        if (statutCell) statutCell.textContent = newStatut;
      }
    })
    .catch((err) => {
      console.error(err);
      alert("Erreur lors de la mise à jour");
    })
    .finally(() => {
      button.disabled = false;
    });
}

// Attach globally



//actions sur table emploidutemps
function actionsEmploi() {
  const modal = document.getElementById("emploiModal");
  const form = document.getElementById("ajouterEmploi");
  const closeBtn = document.querySelector(".close");
  const submitBtn = document.getElementById("submitBtn");

  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;


  // Open Add Modal
  document.getElementById("openModalBtn").addEventListener("click", () => {
    document.getElementById("emploiModal").style.display = "block";
    form.reset();
    form.setAttribute("data-mode", "add");
    form.querySelector('[name="id"]').value = "";
    document.querySelector(".modal-title").textContent =
      "Ajouter un emploi du temps";
    submitBtn.textContent = "Ajouter";
    modal.style.display = "block";
  });

  closeBtn.addEventListener("click", () => (modal.style.display = "none"));

  const getFrenchDayName = (dateString) => {
    const daysFr = [
      "Dimanche",
      "Lundi",
      "Mardi",
      "Mercredi",
      "Jeudi",
      "Vendredi",
      "Samedi",
    ];
    return daysFr[new Date(dateString + "T00:00:00").getDay()];
  };

  const fillHours = () => {
    const debut = form.querySelector('[name="heure_debut"]');
    const fin = form.querySelector('[name="heure_fin"]');
    debut.innerHTML = "";
    fin.innerHTML = "";

    for (let mins = 8 * 60; mins <= 22 * 60; mins += 15) {
      const h = String(Math.floor(mins / 60)).padStart(2, "0");
      const m = String(mins % 60).padStart(2, "0");
      const time = `${h}:${m}`;
      debut.add(new Option(time, time));
      fin.add(new Option(time, time));
    }
  };
  fillHours();

  // Submit Add or Edit
  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const jour_semaine = getFrenchDayName(form.date.value);
    const data = {
      id: form.querySelector('[name="id"]').value || null,
      date: form.date.value,
      jour_semaine: jour_semaine,
      heure_debut: form.heure_debut.value,
      heure_fin: form.heure_fin.value,
      cin: form.cin.value,
      salle: form.salle.value,
      code_matiere: form.code_matiere.value,
      semestre: form.semestre.value,
    };

    const isEdit = form.getAttribute("data-mode") === "edit";
    const url = isEdit
      ? `/req/emploi/modifier/${data.id}`
      : `/req/emploi/ajouter`;
    const method = isEdit ? "PUT" : "POST";
    
    const response = await fetch(url, {
      method: method,
      headers: {
                  "Content-Type": "application/json",
                  [csrfHeader]: csrfToken
                },
      body: JSON.stringify(data),
    });

    if (response.ok) {
      alert(`Emploi ${isEdit ? "modifié" : "ajouté"} avec succès !`);
      loadContent("emplois");
    } else {
      alert("Erreur lors de l'enregistrement.");
    }
  });

  // Delete button logic
  document.querySelectorAll(".delete-btn").forEach((btn) => {
    btn.addEventListener("click", async (e) => {
      const row = e.target.closest("tr");
      const id = row.getAttribute("data-id");

      if (confirm("Êtes-vous sûr de vouloir supprimer cet emploi?")) {
        const res = await fetch(`/req/emploi/supprimer/${id}`, {
          method: "DELETE",
          headers: {
                      [csrfHeader]: csrfToken
                    }
        });
        if (res.ok) {
          alert("Emploi supprimé!");
          row.remove();
        } else {
          alert("Erreur lors de la suppression.");
        }
      }
    });
  });

  // Modify button logic
  document.querySelectorAll(".modify-btn").forEach((btn) => {
    btn.addEventListener("click", async (e) => {
      const row = e.target.closest("tr");
      const id = row.getAttribute("data-id");
      document.querySelector(".modal-title").textContent =
        "Modifier un emploi du temps";
      // You may fetch full emploi if necessary (if all data is not in the row)
      // For now, get values directly from the row
      const cells = row.children;

      form.setAttribute("data-mode", "edit");
      form.querySelector('[name="id"]').value = id;
      form.date.value = cells[1].textContent.trim();
      form.heure_debut.value = cells[3].textContent.trim().split(" - ")[0];
      form.heure_fin.value = cells[3].textContent.trim().split(" - ")[1];
      form.salle.value = cells[7].textContent.trim();
      form.semestre.value = cells[8].textContent.trim();

      form.cin.value = row.getAttribute("data-cin");
      form.code_matiere.value = row.getAttribute("data-code-matiere");
      // You may need to map names to actual values for cin and code_matiere
      // So either fetch by ID or pre-attach data-* attributes

      submitBtn.textContent = "Modifier";
      modal.style.display = "block";
    });
  });
}

//Actions sur table filiere
function actionsFiliere() {
  const form = document.getElementById("filiereForm");
  const submitBtn = document.getElementById("submitBtn");
  const modeField = form;
  const modal = document.getElementById("filiereModal");
  const closeBtn = document.querySelector(".close");

  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
  
  document.getElementById("openModalBtn").addEventListener("click", () => {
    submitBtn.textContent = "Ajouter";
    document.getElementById("filiereModal").style.display = "block";
    document.querySelector(".modal-title").textContent = "Ajouter une filiere";
    form.reset();
    form.setAttribute("data-mode", "add");
    form.querySelector('[name="id"]').value = "";
    modal.style.display = "block";
  });

  closeBtn.addEventListener("click", () => (modal.style.display = "none"));

  // Form submission
  document.getElementById("filiereForm").onsubmit = function (e) {
    e.preventDefault();

    const formData = new FormData(this);
    const filiereObj = Object.fromEntries(formData.entries());

    filiereObj.id = filiereObj.id ? Number(filiereObj.id) : null;

    fetch("/req/filieres/save", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        [csrfHeader]: csrfToken,
      },
      body: JSON.stringify(filiereObj),
    })
      .then((res) => {
        if (!res.ok) throw new Error("Erreur d'enregistrement");
        return res;
      })
      .then(() => {
        modal.style.display = "none";

        // Show confirmation alert based on mode
        const mode = form.getAttribute("data-mode");
        if (mode === "add") {
          alert(`Filière "${filiereObj.nom_filiere}" ajoutée avec succès.`);
        } else if (mode === "edit") {
          alert(`Filière "${filiereObj.nom_filiere}" modifiée avec succès.`);
        }

        loadContent("filieres"); // reload table
      })
      .catch((err) => {
        console.error(err);
        alert("Erreur lors de l'ajout.");
      });
  };

  //Modifier logique
  document.querySelectorAll(".modify-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      const row = e.target.closest("tr");
      const cells = row.querySelectorAll("td");
      const id = row.getAttribute("data-id");
      document.querySelector(".modal-title").textContent = "Modifier filiere";

      const code_filiere = cells[1].innerText.trim();
      const nom_filiere = cells[2].innerText.trim();
      const niveau = cells[3].innerText.trim();
      const duree_heures = cells[4].innerText.trim();
      const description = cells[5].innerText.trim();
      const responsable = cells[6].innerText.trim(); // actual name

      // Set fields

      form.setAttribute("data-mode", "edit");
      form.querySelector('[name="id"]').value = id;
      form.querySelector('[name="code_filiere"]').value = code_filiere;
      form.querySelector('[name="nom_filiere"]').value = nom_filiere;
      form.querySelector('[name="niveau"]').value = niveau;
      form.querySelector('[name="duree_heures"]').value = duree_heures;
      form.querySelector('[name="description"]').value = description;
      form.querySelector('[name="responsable"]').value = responsable;

      submitBtn.textContent = "Modifier";
      modal.style.display = "block";
    });
  });

  //Supprimer logique
  document.querySelectorAll(".delete-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      const row = e.target.closest("tr");
      const id = row.getAttribute("data-id");
      const nom_filiere = row.querySelectorAll("td")[2].innerText.trim();

      if (confirm(`Voulez-vous supprimer la filière ${nom_filiere} ?`)) {
        fetch(`/req/filieres/delete/${id}`, {
          method: "DELETE",
          headers: {
              [csrfHeader]: csrfToken
          }
        })
          .then((res) => {
            if (!res.ok) throw new Error("Erreur de suppression");
            return res;
          })
          .then(() => {
            alert(`Filière "${nom_filiere}" supprimée avec succès.`);
            loadContent("filieres"); // refresh after delete
          })
          .catch((err) => {
            console.error(err);
            alert("Erreur lors de la suppression.");
          });
      }
    });
  });
}

//Trouver filiere by niveau
function trouverFiliere() {
  const niveauSelect = document.getElementById("filterNiveau");
  const rows = document.querySelectorAll("#filiereTable tbody tr");

  function filterRows() {
    const niveauValue = niveauSelect.value.toLowerCase();

    rows.forEach((row) => {
      const cells = row.querySelectorAll("td");
      const [, , , niveau, , ,] = Array.from(cells).map((td) =>
        td.textContent.toLowerCase()
      );

      const matchNiveau = !niveauValue || niveau.includes(niveauValue);

      row.style.display = matchNiveau ? "" : "none";
    });
  }

  niveauSelect.addEventListener("change", filterRows);
}

//Actions sur table matiere
function actionsMatiere() {
  const form = document.getElementById("matiereForm");
  const submitBtn = document.getElementById("submitBtn");
  const modeField = form;
  const modal = document.getElementById("matiereModal");
  const closeBtn = document.querySelector(".close");

  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
  document.getElementById("openModalBtn").addEventListener("click", () => {
    submitBtn.textContent = "Ajouter";
    document.querySelector(".modal-title").textContent = "Ajouter une matiere";
    document.getElementById("matiereModal").style.display = "block";
    form.reset();
    form.setAttribute("data-mode", "add");
    form.querySelector('[name="id"]').value = "";
    modal.style.display = "block";
  });

  closeBtn.addEventListener("click", () => (modal.style.display = "none"));

  // Form submission
  document.getElementById("matiereForm").onsubmit = function (e) {
    e.preventDefault();

    const formData = new FormData(this);
    const matiereObj = Object.fromEntries(formData.entries());

    matiereObj.id = matiereObj.id ? Number(matiereObj.id) : null;

    fetch("/req/matieres/save", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        [csrfHeader]: csrfToken,
      },
      body: JSON.stringify(matiereObj),
    })
      .then((res) => {
        if (!res.ok) throw new Error("Erreur d'enregistrement");
        return res;
      })
      .then(() => {
        modal.style.display = "none";

        // Show confirmation alert based on mode
        const mode = form.getAttribute("data-mode");
        if (mode === "add") {
          alert(`Matière "${matiereObj.nom_matiere}" ajoutée avec succès.`);
        } else if (mode === "edit") {
          alert(`Matière "${matiereObj.nom_matiere}" modifiée avec succès.`);
        }

        loadContent("matieres"); // reload table
      })
      .catch((err) => {
        console.error(err);
        alert("Erreur lors de l'ajout.");
      });
  };

  //Modifier logique
  document.querySelectorAll(".modify-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      const row = e.target.closest("tr");
      const cells = row.querySelectorAll("td");
      const id = row.getAttribute("data-id");
      document.querySelector(".modal-title").textContent =
        "Modifier une matiere";

      const code_matiere = cells[1].innerText.trim();
      const nom_matiere = cells[2].innerText.trim();
      const description = cells[3].innerText.trim();
      const nombre_heures = cells[4].innerText.trim();
      const coefficient = cells[5].innerText.trim();
      const code_filiere = row.getAttribute("data-code-filiere");

      // Set fields
      form.setAttribute("data-mode", "edit");
      form.querySelector('[name="id"]').value = id;
      form.querySelector('[name="code_matiere"]').value = code_matiere;
      form.querySelector('[name="nom_matiere"]').value = nom_matiere;
      form.querySelector('[name="description"]').value = description;
      form.querySelector('[name="nombre_heures"]').value = nombre_heures;
      form.querySelector('[name="coefficient"]').value = coefficient;
      form.querySelector('[name="code_filiere"]').value = code_filiere;

      submitBtn.textContent = "Modifier";
      modal.style.display = "block";
    });
  });

  //Supprimer logique
  document.querySelectorAll(".delete-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      const row = e.target.closest("tr");
      const id = row.getAttribute("data-id");
      const nom_matiere = row.querySelectorAll("td")[2].innerText.trim();

      if (confirm(`Voulez-vous supprimer le matiere ${nom_matiere} ?`)) {
        fetch(`/req/matieres/delete/${id}`, {
          method: "DELETE",
          headers: {
              [csrfHeader]: csrfToken
          }
        })
          .then((res) => {
            if (!res.ok) throw new Error("Erreur de suppression");
            return res;
          })
          .then(() => {
            alert(`matiere "${nom_matiere}" supprimée avec succès.`);
            loadContent("matieres"); // refresh after delete
          })
          .catch((err) => {
            console.error(err);
            alert("Erreur lors de la suppression.");
          });
      }
    });
  });
}

//Trouver matiere par filiere
function trouverMatiere() {
  const filiereSelect = document.getElementById("filterByFiliere");
  const rows = document.querySelectorAll("#matiereTable tbody tr");

  function filterRows() {
    const filiereValue = filiereSelect.value.toLowerCase();

    rows.forEach((row) => {
      const cells = row.querySelectorAll("td");
      const [, , , , , , filiere] = Array.from(cells).map((td) =>
        td.textContent.toLowerCase()
      );

      const matchFiliere = !filiereValue || filiere.includes(filiereValue);

      row.style.display = matchFiliere ? "" : "none";
    });
  }

  filiereSelect.addEventListener("change", filterRows);
}

//Actions sur table employe
function actionsEmploye() {
  const form = document.getElementById("employeForm");
  const submitBtn = document.getElementById("submitBtn");
  const modeField = form;
  const modal = document.getElementById("employeModal");
  const closeBtn = document.querySelector(".close");

  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  document.getElementById("openModalBtn").addEventListener("click", () => {
    submitBtn.textContent = "Ajouter";
    document.querySelector(".modal-title").textContent = "Ajouter un employe";
    document.getElementById("employeModal").style.display = "block";
    form.reset();
    form.setAttribute("data-mode", "add");
    form.querySelector('[name="id"]').value = "";
    modal.style.display = "block";
  });

  closeBtn.addEventListener("click", () => (modal.style.display = "none"));

  // Form submission
  document.getElementById("employeForm").onsubmit = function (e) {
    e.preventDefault();

    const formData = new FormData(this);
    const employeObj = Object.fromEntries(formData.entries());

    employeObj.id = employeObj.id ? Number(employeObj.id) : null;

    fetch("/req/employes/save", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        [csrfHeader]: csrfToken,
      },
      body: JSON.stringify(employeObj),
    })
      .then((res) => {
        if (!res.ok) throw new Error("Erreur d'enregistrement");
        return res;
      })
      .then(() => {
        modal.style.display = "none";

        // Show confirmation alert based on mode
        const mode = form.getAttribute("data-mode");
        if (mode === "add") {
          alert(
            `Employé "${employeObj.nom}" "${employeObj.prenom}" ajoutée avec succès.`
          );
        } else if (mode === "edit") {
          alert(
            `Employé "${employeObj.nom}" "${employeObj.prenom}" modifiée avec succès.`
          );
        }

        loadContent("employes"); // reload table
      })
      .catch((err) => {
        console.error(err);
        alert("Erreur lors de l'ajout.");
      });
  };

  //Modifier logique
  document.querySelectorAll(".modify-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      const row = e.target.closest("tr");
      const cells = row.querySelectorAll("td");
      const id = row.getAttribute("data-id");
      document.querySelector(".modal-title").textContent = "Modifier employe";

      const cin = cells[1].innerText.trim();
      const nom = cells[2].innerText.trim();
      const prenom = cells[3].innerText.trim();
      const adresse = cells[4].innerText.trim();
      const telephone = cells[5].innerText.trim();
      const email = cells[6].innerText.trim();
      const date_embauche = cells[7].innerText.trim();
      const role = cells[8].innerText.trim();
      const specialite = cells[9].innerText.trim();
      const niveau_etude = cells[10].innerText.trim();
      const salaire = cells[11].innerText.trim();

      // Set fields
      form.setAttribute("data-mode", "edit");
      form.querySelector('[name="id"]').value = id;
      form.querySelector('[name="cin"]').value = cin;
      form.querySelector('[name="nom"]').value = nom;
      form.querySelector('[name="prenom"]').value = prenom;
      form.querySelector('[name="adresse"]').value = adresse;
      form.querySelector('[name="telephone"]').value = telephone;
      form.querySelector('[name="email"]').value = email;
      form.querySelector('[name="date_embauche"]').value = date_embauche;
      form.querySelector('[name="role"]').value = role;
      form.querySelector('[name="specialite"]').value = specialite;
      form.querySelector('[name="niveau_etude"]').value = niveau_etude;
      form.querySelector('[name="salaire"]').value = salaire;

      submitBtn.textContent = "Modifier";
      modal.style.display = "block";
    });
  });

  //Supprimer logique
  document.querySelectorAll(".delete-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      const row = e.target.closest("tr");
      const id = row.getAttribute("data-id");
      const cin = row.querySelectorAll("td")[2].innerText.trim();

      if (confirm(`Voulez-vous supprimer l'employe "${cin}" ?`)) {
        fetch(`/req/employes/delete/${id}`, {
          method: "DELETE",
          headers: {
                    [csrfHeader]: csrfToken
                }
        })
          .then((res) => {
            if (!res.ok) throw new Error("Erreur de suppression");
            return res;
          })
          .then(() => {
            alert(`Employe "${cin}" supprimée avec succès.`);
            loadContent("employes"); // refresh after delete
          })
          .catch((err) => {
            console.error(err);
            alert("Erreur lors de la suppression.");
          });
      }
    });
  });
}

//Filtrer Employee par role
function trouverEmploye() {
  const roleSelect = document.getElementById("filterRole");
  const rows = document.querySelectorAll("#employesTable tbody tr");

  function filterRows() {
    const roleValue = roleSelect.value.toLowerCase();

    rows.forEach((row) => {
      const cells = row.querySelectorAll("td");
      const [, , , , , , , , role, , ,] = Array.from(cells).map((td) =>
        td.textContent.toLowerCase()
      );

      const matchRole = !roleValue || role.includes(roleValue);

      row.style.display = matchRole ? "" : "none";
    });
  }

  roleSelect.addEventListener("change", filterRows);
}

//Actions sur table users
function actionsUsers() {
  const form = document.getElementById("usersForm");
  const submitBtn = document.getElementById("submitBtn");
  const modal = document.getElementById("usersModal");
  const closeBtn = document.querySelector(".close");

  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  document.getElementById("openModalBtn").addEventListener("click", () => {
    submitBtn.textContent = "Ajouter";
    document.querySelector(".modal-title").textContent =
      "Ajouter un utilisateur";
    document.getElementById("usersModal").style.display = "block";
    form.reset();
    form.setAttribute("data-mode", "add");
    form.querySelector('[name="id"]').value = "";
    modal.style.display = "block";
  });

  closeBtn.addEventListener("click", () => (modal.style.display = "none"));

  // Form submission
  document.getElementById("usersForm").onsubmit = function (e) {
    e.preventDefault();

    const formData = new FormData(this);
    const usersObj = Object.fromEntries(formData.entries());

    usersObj.id = usersObj.id ? Number(usersObj.id) : null;

    fetch("/req/users/save", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        [csrfHeader]: csrfToken,
      },
      body: JSON.stringify(usersObj),
    })
      .then((res) => {
        if (!res.ok) throw new Error("Erreur d'enregistrement");
        return res;
      })
      .then(() => {
        modal.style.display = "none";

        // Show confirmation alert based on mode
        const mode = form.getAttribute("data-mode");
        if (mode === "add") {
          alert(`Utilisateur ${usersObj.username} ajoutée avec succès.`);
        } else if (mode === "edit") {
          alert(`Utilisateur ${usersObj.username} modifiée avec succès.`);
        }

        loadContent("users"); // reload table
      })
      .catch((err) => {
        console.error(err);
        alert("Erreur lors de l'ajout.");
      });
  };

  //Modifier logique
  document.querySelectorAll(".modify-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      const row = e.target.closest("tr");
      const cells = row.querySelectorAll("td");
      const id = row.getAttribute("data-id");
      document.querySelector(".modal-title").textContent =
        "Modifier Utilisateur";

      const username = cells[1].innerText.trim();
      const password = cells[2].innerText.trim();
      const role = cells[3].innerText.trim();

      // Set fields
      form.setAttribute("data-mode", "edit");
      form.querySelector('[name="id"]').value = id;
      form.querySelector('[name="username"]').value = username;
      form.querySelector('[name="password"]').value = password;
      form.querySelector('[name="role"]').value = role;

      submitBtn.textContent = "Modifier";
      modal.style.display = "block";
    });
  });

  //Supprimer logique
  document.querySelectorAll(".delete-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      const row = e.target.closest("tr");
      const id = row.getAttribute("data-id");
      const username = row.querySelectorAll("td")[1].innerText.trim();

      if (confirm(`Voulez-vous supprimer cette utilisateur "${username}" ?`)) {
        fetch(`/req/users/delete/${id}`, {
          method: "DELETE",
          headers: {
              [csrfHeader]: csrfToken
          }
        })
          .then((res) => {
            if (!res.ok) throw new Error("Erreur de suppression");
            return res;
          })
          .then(() => {
            alert(`Utilisateur "${username}" supprimée avec succès.`);
            loadContent("users"); // refresh after delete
          })
          .catch((err) => {
            console.error(err);
            alert("Erreur lors de la suppression.");
          });
      }
    });
  });
}

//Filtrer utilisateurs par role
function trouverUser() {
  const roleSelect = document.getElementById("filterRole");
  const rows = document.querySelectorAll("#usersTable tbody tr");

  function filterRows() {
    const roleValue = roleSelect.value.toLowerCase();

    rows.forEach((row) => {
      const cells = row.querySelectorAll("td");
      const [, , , role] = Array.from(cells).map((td) =>
        td.textContent.toLowerCase()
      );

      const matchRole = !roleValue || role.includes(roleValue);

      row.style.display = matchRole ? "" : "none";
    });
  }

  roleSelect.addEventListener("change", filterRows);
}


//Afficher la liste des employees (cin) sur button page d'accueil direction
function afficherEmployes(toggleBtnId, listContainerId) {
  const toggleBtn = document.getElementById(toggleBtnId);
  const list = document.getElementById(listContainerId);

  if (!toggleBtn || !list) return;

  toggleBtn.addEventListener("click", (e) => {
    e.stopPropagation();
    const isVisible = list.style.display === "inline-block";
    list.style.display = isVisible ? "none" : "inline-block";
    //toggleBtn.textContent = isVisible ? "Show Employees" : "Hide Employees";
  });
  // Close the list if clicking outside
  document.addEventListener("click", (e) => {
    if (!list.contains(e.target)) {
      list.style.display = "none";
    }
  });
  // Optional: prevent closing when clicking inside the list
  list.addEventListener("click", (e) => {
    e.stopPropagation();
  });
}

//Afficher la liste des Matieres (filiere) sur button page d'accueil direction
function afficherMatieres(toggleBtnId, listContainerId) {
  const toggleBtn = document.getElementById(toggleBtnId);
  const list = document.getElementById(listContainerId);

  if (!toggleBtn || !list) return;

  toggleBtn.addEventListener("click", (e) => {
    e.stopPropagation();
    const isVisible = list.style.display === "inline-block";
    list.style.display = isVisible ? "none" : "inline-block";
  });
  // Close the list if clicking outside
  document.addEventListener("click", (e) => {
    if (!list.contains(e.target)) {
      list.style.display = "none";
    }
  });
  // Optional: prevent closing when clicking inside the list
  list.addEventListener("click", (e) => {
    e.stopPropagation();
  });
}

//Afficher la liste des Filieres (cin resp) sur button page d'accueil direction
function afficherFilieres(toggleBtnId, listContainerId) {
  const toggleBtn = document.getElementById(toggleBtnId);
  const list = document.getElementById(listContainerId);

  if (!toggleBtn || !list) return;

  toggleBtn.addEventListener("click", (e) => {
    e.stopPropagation();
    const isVisible = list.style.display === "inline-block";
    list.style.display = isVisible ? "none" : "inline-block";
  });
  // Close the list if clicking outside
  document.addEventListener("click", (e) => {
    if (!list.contains(e.target)) {
      list.style.display = "none";
    }
  });
  // Optional: prevent closing when clicking inside the list
  list.addEventListener("click", (e) => {
    e.stopPropagation();
  });
}

// Export the table to Excel
function exportTableToExcel(tableId, fileName) {
  console.log("Export function triggered");

  // Get the table element by its ID
  var table = document.getElementById(tableId);
  if (!table) {
    console.log("Table not found!");
    return;
  }

  // Convert the table to a workbook
  var wb = XLSX.utils.table_to_book(table, { sheet: "Etat" });
  console.log("Workbook created");

  // Trigger the file download
  XLSX.writeFile(wb, fileName);
  console.log("Excel file should have been downloaded.");
}



//Dupliquer les emplois du temps selon nombre d'heurs (condition ajouter les emplois d'une seul semaine avant appliquer)
function submitGenerateRecurring(form) {
    // ensure meta tags exist
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    if (!csrfToken || !csrfHeader) {
        console.error("CSRF token or header missing!");
        return;
    }

    const formData = new FormData(form);

    fetch('/generate-recurring', {
        method: 'POST',
        headers: {
          [csrfHeader]: csrfToken
        },
        body: formData
    })
    .then(resp => resp.text().then(msg => ({ status: resp.ok, message: msg })))
    .then(result => {
        showToast(result.message, result.status ? 'success' : 'danger');
    })
    .catch(err => {
        showToast("❌ Une erreur est survenue: " + err, 'danger');
    });
}





// Bootstrap toast message generator
function showToast(message, type = 'success') {
    const toastId = 'toast-' + Date.now();
    const toastHTML = `
        <div id="${toastId}" class="toast align-items-center text-white bg-${type} border-0 mb-2" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Fermer"></button>
            </div>
        </div>
    `;

    const toastContainer = document.getElementById('toastContainer');
    toastContainer.insertAdjacentHTML('beforeend', toastHTML);

    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, { delay: 5000 });
    toast.show();

    // Optionally remove toast from DOM after hiding
    toastElement.addEventListener('hidden.bs.toast', () => {
        toastElement.remove();
    });
}


// Function to create progress chart
// Function to create progress chart with detailed tooltips
function createProgressChart() {

  const canvas = document.getElementById("progressBarChart");
  if (!canvas) {
    console.warn("Canvas element progressBarChart not found");
    return;
  }

  // Get progress data from hidden container
  const dataContainer = document.getElementById("progressDataContainer");
  if (!dataContainer) {
    console.warn("Progress data container not found");
    return;
  }

  let detailedData = {};
  const dataAttr = dataContainer.getAttribute("data-progress-data");

  if (dataAttr) {
    try {
      detailedData = JSON.parse(dataAttr);
    } catch (e) {
        console.error("Error parsing progress data:", e);
      return;
    }
  } else {
    console.error("No data attribute found");
    return;
  }

  // Extract data for chart
  const labels = Object.keys(detailedData);
  const progressData = [];
  const totalHours = [];
  const completedHours = [];
  const filieres = [];

  labels.forEach((label) => {
    const details = detailedData[label];
    progressData.push(details.progress || 0);
    totalHours.push(details.totalHours || 0);
    completedHours.push(details.completedHours || 0);
    filieres.push(details.filiere || "Non assignée");
  });


  if (labels.length === 0) {
    console.error("No labels found - chart will be empty");
    return;
  }

  // Destroy existing chart if it exists
  if (
    window.progressChart &&
    typeof window.progressChart.destroy === "function"
  ) {
    console.log("Destroying existing chart");
    window.progressChart.destroy();
    window.progressChart = null;
  }

  // Reset canvas size
  canvas.style.width = "";
  canvas.style.height = "";
  canvas.width = "";
  canvas.height = "";

  const ctx = canvas.getContext("2d");

  try {
    window.progressChart = new Chart(ctx, {
      type: "bar",
      data: {
        labels: labels,
        datasets: [
          {
            label: "Progression des Matières (%)",
            data: progressData,
            backgroundColor: "rgba(54, 162, 235, 0.2)",
            borderColor: "rgba(54, 162, 235, 1)",
            borderWidth: 1,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        indexAxis: "y", // ✅ Make it horizontal
        plugins: {
          legend: {
            display: true,
            position: "top",
          },
          tooltip: {
            callbacks: {
              title: function (context) {
                return context[0].label; // Matiere name
              },
              afterTitle: function (context) {
                const index = context[0].dataIndex;
                return "Filière: " + filieres[index];
              },
              label: function (context) {
                const index = context.dataIndex;
                const progress = progressData[index];
                const completed = completedHours[index];
                const total = totalHours[index];

                return [
                  `Progression: ${progress}%`,
                  `Heures terminées: ${completed}h`,
                  `Heures totales: ${total}h`,
                ];
              },
            },
          },
        },
        scales: {
          x: {
            beginAtZero: true,
            max: 100,
            title: {
              display: true,
              text: "Progression (%)",
            },
            ticks: {
              callback: function (value) {
                return value + "%";
              },
            },
            grid: {
              display: true,
            },
          },
          y: {
            title: {
              display: true,
              text: "Matières",
            },
            ticks: {
              font: {
                size: 12,
              },
            },
            grid: {
              display: false,
            },
          },
        },
      },
    });
  } catch (error) {
    console.error("Error creating chart:", error);
  }
}



//Graph progression bar par filiere (1 par 1)
// function createProgressCharts() {
//   console.log("=== Starting createProgressCharts ===");

//   const container = document.getElementById("progressChartsContainer");
//   if (!container) {
//     console.warn("Container for progress charts not found");
//     return;
//   }

//   container.innerHTML = ""; // Clear any existing content

//   const dataContainer = document.getElementById("progressDataContainer");
//   if (!dataContainer) {
//     console.warn("Progress data container not found");
//     return;
//   }

//   let detailedData = {};
//   const dataAttr = dataContainer.getAttribute("data-progress-data");

//   if (dataAttr) {
//     try {
//       detailedData = JSON.parse(dataAttr);
//       console.log("Parsed detailed data:", detailedData);
//     } catch (e) {
//       console.error("Error parsing progress data:", e);
//       return;
//     }
//   } else {
//     console.error("No data attribute found");
//     return;
//   }

//   // Group data by filière
//   const filiereGroups = {};
//   for (const matiereName in detailedData) {
//     const matiere = detailedData[matiereName];
//     const filiere = matiere.filiere || "Non assignée";

//     if (!filiereGroups[filiere]) {
//       filiereGroups[filiere] = [];
//     }

//     filiereGroups[filiere].push({
//       name: matiereName,
//       progress: matiere.progress || 0,
//       totalHours: matiere.totalHours || 0,
//       completedHours: matiere.completedHours || 0,
//     });
//   }

//   // Create a chart per filière
//   for (const filiere in filiereGroups) {
//     const matieres = filiereGroups[filiere];

//     const chartId = `chart-${filiere.replace(/\s+/g, "_")}`;
    
//     // Create container and canvas
//     const section = document.createElement("div");
//     section.className = "chart-section";
//     section.style.marginBottom = "30px";

//     const title = document.createElement("h4");
//     title.textContent = `Filière: ${filiere}`;
//     section.appendChild(title);

//     const canvas = document.createElement("canvas");
//     canvas.id = chartId;
//     canvas.style.maxHeight = "400px";
//     section.appendChild(canvas);
//     container.appendChild(section);

//     // Create chart
//     const ctx = canvas.getContext("2d");

//     const labels = matieres.map((m) => m.name);
//     const progressData = matieres.map((m) => m.progress);
//     const totalHours = matieres.map((m) => m.totalHours);
//     const completedHours = matieres.map((m) => m.completedHours);

//     new Chart(ctx, {
//       type: "bar",
//       data: {
//         labels: labels,
//         datasets: [
//           {
//             label: "Progression des Matières (%)",
//             data: progressData,
//             backgroundColor: "rgba(75, 192, 192, 0.2)",
//             borderColor: "rgba(75, 192, 192, 1)",
//             borderWidth: 1,
//           },
//         ],
//       },
//       options: {
//         responsive: true,
//         maintainAspectRatio: false,
//         indexAxis: "y",
//         plugins: {
//           legend: {
//             display: true,
//             position: "top",
//           },
//           tooltip: {
//             callbacks: {
//               title: function (context) {
//                 return context[0].label;
//               },
//               label: function (context) {
//                 const index = context.dataIndex;
//                 return [
//                   `Progression: ${progressData[index]}%`,
//                   `Heures terminées: ${completedHours[index]}h`,
//                   `Heures totales: ${totalHours[index]}h`,
//                 ];
//               },
//             },
//           },
//         },
//         scales: {
//           x: {
//             beginAtZero: true,
//             max: 100,
//             title: {
//               display: true,
//               text: "Progression (%)",
//             },
//             ticks: {
//               callback: function (value) {
//                 return value + "%";
//               },
//             },
//           },
//           y: {
//             title: {
//               display: true,
//               text: "Matières",
//             },
//             ticks: {
//               font: {
//                 size: 12,
//               },
//             },
//             grid: {
//               display: false,
//             },
//           },
//         },
//       },
//     });
//   }

//   console.log("All charts created successfully");
// }



//Graph progression bar par filiere (2 par 2)
function createProgressCharts() {
  console.log("=== Starting createProgressCharts ===");

  const container = document.getElementById("progressChartsContainer");
  if (!container) {
    console.warn("Container for progress charts not found");
    return;
  }

  container.innerHTML = ""; // Clear previous charts

  const dataContainer = document.getElementById("progressDataContainer");
  if (!dataContainer) {
    console.warn("Progress data container not found");
    return;
  }

  let detailedData = {};
  const dataAttr = dataContainer.getAttribute("data-progress-data");

  if (dataAttr) {
    try {
      detailedData = JSON.parse(dataAttr);
      console.log("Parsed detailed data:", detailedData);
    } catch (e) {
      console.error("Error parsing progress data:", e);
      return;
    }
  } else {
    console.error("No data attribute found");
    return;
  }

  // Group by filière
  const filiereGroups = {};
  for (const matiereName in detailedData) {
    const matiere = detailedData[matiereName];
    const filiere = matiere.filiere || "Non assignée";

    if (!filiereGroups[filiere]) {
      filiereGroups[filiere] = [];
    }

    filiereGroups[filiere].push({
      name: matiereName,
      progress: matiere.progress || 0,
      totalHours: matiere.totalHours || 0,
      completedHours: matiere.completedHours || 0,
    });
  }

  const filiereEntries = Object.entries(filiereGroups);

  for (let i = 0; i < filiereEntries.length; i += 2) {
    // Create a row for every 2 charts
    const row = document.createElement("div");
    row.className = "chart-row";

    // Add up to two columns in the row
    for (let j = 0; j < 2 && i + j < filiereEntries.length; j++) {
      const [filiere, matieres] = filiereEntries[i + j];
      const chartId = `chart-${filiere.replace(/\s+/g, "_")}`;

      const col = document.createElement("div");
      col.className = "chart-column";

      const title = document.createElement("h4");
      title.textContent = `Filière: ${filiere}`;
      col.appendChild(title);

      const canvas = document.createElement("canvas");
      canvas.id = chartId;
      col.appendChild(canvas);
      row.appendChild(col);

      const ctx = canvas.getContext("2d");

      const labels = matieres.map((m) => m.name);
      const progressData = matieres.map((m) => m.progress);
      const totalHours = matieres.map((m) => m.totalHours);
      const completedHours = matieres.map((m) => m.completedHours);

      new Chart(ctx, {
        type: "bar",
        data: {
          labels: labels,
          datasets: [
            {
              label: "Progression des Matières (%)",
              data: progressData,
              backgroundColor: "rgba(75, 192, 192, 0.2)",
              borderColor: "rgba(75, 192, 192, 1)",
              borderWidth: 1,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          indexAxis: "y",
          plugins: {
            legend: {
              display: true,
              position: "top",
            },
            tooltip: {
              callbacks: {
                title: function (context) {
                  return context[0].label;
                },
                label: function (context) {
                  const index = context.dataIndex;
                  return [
                    `Progression: ${progressData[index]}%`,
                    `Heures terminées: ${completedHours[index]}h`,
                    `Heures totales: ${totalHours[index]}h`,
                  ];
                },
              },
            },
          },
          scales: {
            x: {
              beginAtZero: true,
              max: 100,
              title: {
                display: true,
                text: "Progression (%)",
              },
              ticks: {
                callback: function (value) {
                  return value + "%";
                },
              },
            },
            y: {
              title: {
                display: true,
                text: "Matières",
              },
              grid: {
                display: false,
              },
            },
          },
        },
      });
    }

    container.appendChild(row);
  }

  console.log("All charts created successfully");
}



function logoutUser() {
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;

    fetch('/logout', {
        method: 'POST',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    })
    .then(response => {
        if (response.ok) {
            window.location.href = '/login?logout'; // redirect after logout
        } else {
            alert("Erreur lors de la déconnexion.");
        }
    })
    .catch(error => {
        console.error("Logout error:", error);
        alert("Une erreur est survenue lors de la déconnexion.");
    });
}

// Usage: attach to a button
// document.getElementById("logoutBtn").addEventListener("click", (e) => {
//     e.preventDefault();
//     logoutUser();
// });
