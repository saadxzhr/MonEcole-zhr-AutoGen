function loadContent(pageOrUrl) {
    let urlMap = {
        accueil: "/req/accueil",
        emploi: "/req/emploi",
        sidebar: "/req/sidebar",
        historique: "/req/etatsdavancement",
        changepass: "/req/changepass"

    };


    let url = urlMap[pageOrUrl] ?? pageOrUrl; //qlqs page ont des alias

    if (!url) {
        console.error("Unknown page:", page);
        return;
    }

    fetch(url, {
            method: 'GET',
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP ${response.status} - ${response.statusText}`);
            }
            return response.text();
        })
        .then(html => {
            document.getElementById("formateur-content").innerHTML = html;
            if (url.includes("changepass")) {
                changerMotDePasse(); //charger apres chargement de page
            }
        })
        .catch(err => {
            console.error("Failed to load content:", err);
            document.getElementById("formateur-content").innerHTML = `<p style="color:red;">Erreur de chargement</p>`;
        });
}

document.addEventListener("DOMContentLoaded", function () {
        loadContent('accueil');
});


// Charger mot de passe
function changerMotDePasse() {
  const submitBtn = document.getElementById("submit");
  if (!submitBtn) return;

  submitBtn.addEventListener("click", async (e) => {
    e.preventDefault();

    const oldPass = document.getElementById("password").value.trim();
    const newPass = document.getElementById("newpass").value.trim();
    const confirmPass = document.getElementById("newpassconf").value.trim();

    if (!oldPass || !newPass || !confirmPass) {
      alert("Tous les champs sont obligatoires.");
      return;
    }
    if (newPass !== confirmPass) {
      alert("Les mots de passe ne correspondent pas.");
      return;
    }

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    submitBtn.disabled = true;

    try {
      const res = await fetch("/req/changepass", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          [csrfHeader]: csrfToken
        },
        body: JSON.stringify({ password: oldPass, newpass: newPass })
      });

      const data = await res.json();

      if (data.success) {
        alert(data.message);
        document.getElementById("password").value = "";
        document.getElementById("newpass").value = "";
        document.getElementById("newpassconf").value = "";
      } else {
        alert("Erreur : " + data.message);
      }
    } catch (err) {
      alert("Erreur réseau : " + err);
    } finally {
      submitBtn.disabled = false;
    }
  });
}


//Se deconnecter
document.getElementById("logoutBtn").addEventListener("click", async (e) => {
    e.preventDefault(); // prevent default if it's inside a form
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;

    const response = await fetch('/logout', {
        method: 'POST',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }
    });

    if (response.ok) {
        window.location.href = '/login?logout'; // redirect after logout
    } else {
        alert("Erreur lors de la déconnexion.");
    }
});
