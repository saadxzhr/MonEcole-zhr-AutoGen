-- =====================================
-- Flyway migration V1 - Create table modulex
-- =====================================

CREATE TABLE modulex (
    id SERIAL PRIMARY KEY,
    codemodule VARCHAR(50) UNIQUE NOT NULL,
    nommodule VARCHAR(150) NOT NULL,
    description TEXT,
    nombreheures FLOAT,
    coefficient FLOAT,
    departementdattache VARCHAR(20),
    coordinateur VARCHAR(20) NOT NULL REFERENCES employe(cin),
    semestre INT,
    optionmodule VARCHAR(100),
    codefiliere VARCHAR(50) NOT NULL REFERENCES filiere(codefiliere),
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes pour recherches rapides
CREATE INDEX idx_modulex_codefiliere ON modulex(codefiliere);
CREATE INDEX idx_modulex_departement ON modulex(departementdattache);
CREATE INDEX idx_modulex_nommodule ON modulex(nommodule);

-- Contraintes sur matiere
ALTER TABLE matiere
ADD CONSTRAINT matiere_modulex_fk
FOREIGN KEY (codemodule)
REFERENCES modulex(codemodule)
ON UPDATE CASCADE
ON DELETE CASCADE;
