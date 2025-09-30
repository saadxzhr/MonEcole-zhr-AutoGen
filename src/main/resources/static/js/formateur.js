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

    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP ${response.status} - ${response.statusText}`);
            }
            return response.text();
        })
        .then(html => {
            document.getElementById("formateur-content").innerHTML = html;
            if (url.includes("changepass")) {
                initChangePassScript(); //charger apres chargement de page
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


// Charger cette fonction apres fragment 'changepass' est charger
function initChangePassScript() {
    const submitButton = document.getElementById("submit");
    if (!submitButton) return;

    submitButton.addEventListener("click", () => {
        const oldPassword = document.getElementById("password").value.trim();
        const newPassword = document.getElementById("newpass").value.trim();
        const confirmPassword = document.getElementById("newpassconf").value.trim();


        if (!oldPassword || !newPassword || !confirmPassword) {
            alert('Tous les champs sont obligatoires.');
            return;
        }

        if (newPassword !== confirmPassword) {
            alert('Les nouveaux mots de passe ne correspondent pas.');
            return;
        }

        const data = {
            password: oldPassword,
            newpass: newPassword
        };

        fetch('/req/changepass', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data) //convert to string
        })
        .then(async response => {
            if (!response.ok) {
                const errorMessage = await response.text();
                throw new Error(errorMessage);
            }
            console.log("Password changed successfully");
            return response.json(); // or response.text() if your API returns text
        })
        .catch(error => {
            // Catch and show backend error
            alert("Erreur : " + error.message);
        });
    })
}
