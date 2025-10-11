-- Crée le type ENUM si non existant
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'planningtypeenum') THEN
        CREATE TYPE planningtypeenum AS ENUM ('Semaine', 'Weekend', 'Mixte');
    END IF;
END
$$;

-- Crée la table filiere
CREATE TABLE IF NOT EXISTS filiere (
    id SERIAL PRIMARY KEY,
    codeFiliere VARCHAR(50) UNIQUE NOT NULL,
    nomFiliere VARCHAR(150) NOT NULL,
    niveau VARCHAR(50),
    dureeHeures INT,
    description TEXT,
    responsableCin VARCHAR(20) REFERENCES employe(cin),
    planninType planningtypeenum DEFAULT 'Semaine',
    actif BOOLEAN DEFAULT TRUE
);


-- Flyway migration: create modulex table (adjust types to your DB if needed)

CREATE TABLE IF NOT EXISTS modulex (
  id SERIAL PRIMARY KEY,
  codemodule VARCHAR(50) UNIQUE NOT NULL,
  nommodule VARCHAR(150) NOT NULL,
  description TEXT,
  nombreheures INT,
  coefficient FLOAT,
  departementdattache VARCHAR(50),
  coordonateur VARCHAR(20),
  semestre VARCHAR(20),
  optionmodule VARCHAR(100),
  codefiliere VARCHAR(50),
  CONSTRAINT fk_modulex_filiere FOREIGN KEY (codefiliere) REFERENCES filiere(codefiliere),
  CONSTRAINT fk_modulex_employe FOREIGN KEY (coordonateur) REFERENCES employe(cin)
);