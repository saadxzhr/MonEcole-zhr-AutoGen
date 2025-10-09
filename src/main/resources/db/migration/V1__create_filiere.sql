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
