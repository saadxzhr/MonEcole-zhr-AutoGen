-- =============================================================
-- V2__create_full_schema.sql
-- Generated from live DB state.
-- Runs only on fresh installations (skipped past V1 baseline).
-- Organized by table in dependency order.
-- =============================================================

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

-- =============================================================
-- EXTENSIONS
-- =============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;

-- =============================================================
-- TYPES
-- =============================================================

DO $$ BEGIN
CREATE TYPE public.planningtypeenum AS ENUM ('Semaine', 'Weekend', 'Mixte');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

-- =============================================================
-- FUNCTIONS
-- =============================================================


CREATE FUNCTION public.createuseronemployeinsert() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO utilisateur(username, password, role, cin)
    VALUES (NEW.cin, NEW.cin, NEW.role, NEW.cin);
    RETURN NEW;
END;
$$;

CREATE FUNCTION public.update_utilisateur_from_employe() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    UPDATE utilisateur
    SET username = NEW.cin, password = NEW.cin, role = NEW.role
    WHERE username = OLD.cin;
    RETURN NEW;
END;
$$;

CREATE FUNCTION public.createetatonemploiinsert() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO etatDavancement(cin, emploiId, statut)
    VALUES (NEW.cin, NEW.id, 'En attente');
    RETURN NEW;
END;
$$;

-- =============================================================
-- TABLE: employe
-- =============================================================

CREATE TABLE public.employe (
    id              BIGSERIAL       NOT NULL,
    cin             VARCHAR(20)     NOT NULL,
    nom             VARCHAR(100)    NOT NULL,
    prenom          VARCHAR(100)    NOT NULL,
    adresse         VARCHAR(255),
    telephone       VARCHAR(20),
    email           VARCHAR(100),
    dateembauche    DATE,
    role            VARCHAR(50),
    specialite      VARCHAR(100),
    niveauetude     VARCHAR(100),
    salaire         NUMERIC(10,2),
    maxheuressemaine INTEGER        DEFAULT 20,
    disponibleweekend BOOLEAN       DEFAULT false,
    seulementweekend  BOOLEAN       DEFAULT false,
    actif           BOOLEAN         DEFAULT true,
    CONSTRAINT employe_pkey PRIMARY KEY (id),
    CONSTRAINT employe_cin_key UNIQUE (cin),
    CONSTRAINT employe_email_key UNIQUE (email)
);

CREATE TRIGGER trgcreateuser
    AFTER INSERT ON public.employe
    FOR EACH ROW EXECUTE FUNCTION public.createuseronemployeinsert();

CREATE TRIGGER trg_update_utilisateur
    AFTER UPDATE OF cin, role ON public.employe
    FOR EACH ROW EXECUTE FUNCTION public.update_utilisateur_from_employe();

-- =============================================================
-- TABLE: utilisateur
-- =============================================================

CREATE TABLE public.utilisateur (
    id                  BIGSERIAL       NOT NULL,
    username            VARCHAR(100)    NOT NULL,
    password            VARCHAR(255)    NOT NULL,
    role                VARCHAR(50)     NOT NULL,
    cin                 VARCHAR(20),
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    forcechangepassword BOOLEAN         NOT NULL DEFAULT true,
    CONSTRAINT utilisateur_pkey PRIMARY KEY (id),
    CONSTRAINT utilisateur_username_key UNIQUE (username)
);

CREATE INDEX idx_utilisateur_role ON public.utilisateur USING btree (role);
CREATE INDEX idx_utilisateur_cin  ON public.utilisateur USING btree (cin);

-- =============================================================
-- TABLE: refreshtoken
-- NOTE: depends on utilisateur
-- =============================================================

CREATE TABLE public.refreshtoken (
    id              BIGSERIAL       NOT NULL,
    utilisateur_id  BIGINT          NOT NULL,
    token           TEXT            NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    expires_at      TIMESTAMP       NOT NULL,
    revoked         BOOLEAN         DEFAULT false,
    useragent       VARCHAR(255),
    ipaddress       VARCHAR(100),
    jti             VARCHAR(100),
    reused          BOOLEAN         DEFAULT false,
    accessjti       VARCHAR(255),
    CONSTRAINT refreshtoken_pkey PRIMARY KEY (id),
    CONSTRAINT refreshtoken_utilisateurid_fkey
        FOREIGN KEY (utilisateur_id) REFERENCES public.utilisateur(id) ON DELETE CASCADE
);

CREATE INDEX idx_refreshtoken_covering  ON public.refreshtoken USING btree (utilisateur_id, revoked, created_at);
CREATE INDEX idx_refreshtoken_expiresat ON public.refreshtoken USING btree (expires_at);
CREATE INDEX idx_refreshtoken_token     ON public.refreshtoken USING btree (token);
CREATE INDEX idx_refreshtoken_jti       ON public.refreshtoken USING btree (jti);

-- =============================================================
-- TABLE: salle
-- =============================================================

CREATE TABLE public.salle (
    id          SERIAL          NOT NULL,
    codesalle   VARCHAR(50)     NOT NULL,
    nomsalle    VARCHAR(100),
    capacite    INTEGER,
    typesalle   VARCHAR(50),
    equipement  TEXT,
    CONSTRAINT salle_pkey PRIMARY KEY (id),
    CONSTRAINT salle_codesalle_key UNIQUE (codesalle)
);

-- =============================================================
-- TABLE: reglecontrainte
-- =============================================================

CREATE TABLE public.reglecontrainte (
    id          SERIAL          NOT NULL,
    nomregle    VARCHAR(150)    NOT NULL,
    description TEXT,
    active      BOOLEAN         DEFAULT true,
    CONSTRAINT reglecontrainte_pkey PRIMARY KEY (id)
);

-- =============================================================
-- TABLE: filiere
-- NOTE: depends on employe; self-referencing (parentfiliereid)
-- =============================================================

CREATE TABLE public.filiere (
    id              BIGSERIAL       NOT NULL,
    codefiliere     VARCHAR(50)     NOT NULL,
    nomfiliere      VARCHAR(150)    NOT NULL,
    niveau          VARCHAR(50),
    dureeheures     INTEGER,
    description     TEXT,
    responsablecin  VARCHAR(20),
    plannintype     VARCHAR(50)     DEFAULT 'Semaine'::public.planningtypeenum,
    actif           BOOLEAN         DEFAULT true,
    parentfiliereid BIGINT,
    CONSTRAINT filiere_pkey PRIMARY KEY (id),
    CONSTRAINT filiere_codefiliere_key UNIQUE (codefiliere),
    CONSTRAINT filiere_parentfiliereid_fkey
        FOREIGN KEY (parentfiliereid) REFERENCES public.filiere(id),
    CONSTRAINT filiere_responsablecin_fkey
        FOREIGN KEY (responsablecin) REFERENCES public.employe(cin)
);

-- =============================================================
-- TABLE: modulex
-- NOTE: depends on employe, filiere
-- =============================================================

CREATE TABLE public.modulex (
    id                      BIGSERIAL       NOT NULL,
    code_module             VARCHAR(50)     NOT NULL,
    nom_module              VARCHAR(150)    NOT NULL,
    description             TEXT,
    nombre_heures           DOUBLE PRECISION,
    coefficient             DOUBLE PRECISION,
    code_filiere            VARCHAR(50)     NOT NULL,
    departement_dattache    VARCHAR(100),
    coordinateur            VARCHAR(20)     NOT NULL,
    option_module           VARCHAR(50),
    semestre                INTEGER,
    version                 INTEGER         DEFAULT 0,
    created_at              TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT modulex_pkey PRIMARY KEY (id),
    CONSTRAINT modulex_codemodule_key UNIQUE (code_module),
    CONSTRAINT fk_coordinateur
        FOREIGN KEY (coordinateur) REFERENCES public.employe(cin),
    CONSTRAINT modulex_filiere_fk
        FOREIGN KEY (code_filiere) REFERENCES public.filiere(codefiliere) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX idx_modulex_codefiliere     ON public.modulex USING btree (code_filiere);
CREATE INDEX idx_modulex_coordinateur    ON public.modulex USING btree (coordinateur);
CREATE INDEX idx_modulex_departement     ON public.modulex USING btree (departement_dattache);
CREATE INDEX idx_modulex_nommodule       ON public.modulex USING btree (nom_module);

-- =============================================================
-- TABLE: matiere
-- NOTE: depends on modulex
-- =============================================================

CREATE TABLE public.matiere (
    id              SERIAL          NOT NULL,
    codematiere     VARCHAR(50)     NOT NULL,
    nommatiere      VARCHAR(150)    NOT NULL,
    typecours       VARCHAR(20)     DEFAULT 'Theorique',
    volumetotal     INTEGER,
    maxheuressemaine INTEGER,
    coefficient     DOUBLE PRECISION,
    codemodule      VARCHAR(50),
    CONSTRAINT matiere_pkey PRIMARY KEY (id),
    CONSTRAINT matiere_codematiere_key UNIQUE (codematiere),
    CONSTRAINT matiere_modulex_fk
        FOREIGN KEY (codemodule) REFERENCES public.modulex(code_module) ON UPDATE CASCADE ON DELETE CASCADE
);

-- =============================================================
-- TABLE: matiereannee
-- NOTE: depends on matiere
-- =============================================================

CREATE TABLE public.matiereannee (
    id              SERIAL          NOT NULL,
    matiereid       INTEGER,
    anneescolaire   VARCHAR(20)     NOT NULL,
    semestre        INTEGER,
    active          BOOLEAN         DEFAULT true,
    CONSTRAINT matiereannee_pkey PRIMARY KEY (id),
    CONSTRAINT matiereannee_matiereid_fkey
        FOREIGN KEY (matiereid) REFERENCES public.matiere(id) ON DELETE CASCADE
);

-- =============================================================
-- TABLE: groupefiliere
-- NOTE: depends on filiere
-- =============================================================

CREATE TABLE public.groupefiliere (
    id          SERIAL          NOT NULL,
    nom         VARCHAR(50)     NOT NULL,
    anneeetude  INTEGER         NOT NULL,
    codefiliere VARCHAR(50)     NOT NULL,
    session     INTEGER,
    actif       BOOLEAN         DEFAULT true,
    CONSTRAINT groupefiliere_pkey PRIMARY KEY (id),
    CONSTRAINT groupefiliere_codefiliere_fkey
        FOREIGN KEY (codefiliere) REFERENCES public.filiere(codefiliere) ON DELETE CASCADE
);

-- =============================================================
-- TABLE: optionfiliere
-- NOTE: depends on filiere
-- =============================================================

CREATE TABLE public.optionfiliere (
    codeoption  VARCHAR(50)     NOT NULL,
    nomoption   VARCHAR(150),
    codefiliere VARCHAR(50),
    CONSTRAINT optionfiliere_pkey PRIMARY KEY (codeoption),
    CONSTRAINT optionfiliere_codefiliere_fkey
        FOREIGN KEY (codefiliere) REFERENCES public.filiere(codefiliere)
);

-- =============================================================
-- TABLE: peutenseigner
-- NOTE: depends on employe, modulex
-- =============================================================

CREATE TABLE public.peutenseigner (
    id          SERIAL          NOT NULL,
    cin         VARCHAR(20),
    codemodule  VARCHAR(20),
    CONSTRAINT peutenseigner_pkey PRIMARY KEY (id),
    CONSTRAINT peutenseigner_cin_fkey
        FOREIGN KEY (cin) REFERENCES public.employe(cin) ON DELETE CASCADE,
    CONSTRAINT peutenseigner_codemodule_fkey
        FOREIGN KEY (codemodule) REFERENCES public.modulex(code_module) ON DELETE CASCADE
);

-- =============================================================
-- TABLE: emploidutemps
-- NOTE: depends on employe, matiere, salle
-- =============================================================

CREATE TABLE public.emploidutemps (
    id              BIGSERIAL       NOT NULL,
    joursemaine     INTEGER         CHECK (joursemaine >= 1 AND joursemaine <= 7),
    heuredebut      TIME            NOT NULL,
    heurefin        TIME            NOT NULL,
    salleid         INTEGER,
    matiereid       INTEGER,
    cin             VARCHAR(255),
    semestre        VARCHAR(20),
    anneescolaire   VARCHAR(20),
    datecours       DATE,
    groupeid        INTEGER,
    code_matiere    VARCHAR(255),
    date            DATE,
    heure_debut     TIME(6),
    heure_fin       TIME(6),
    jour_semaine    VARCHAR(255),
    salle           VARCHAR(255),
    CONSTRAINT emploidutemps_pkey PRIMARY KEY (id),
    CONSTRAINT emploidutemps_joursemaine_heuredebut_salleid_datecours_grou_key
        UNIQUE (joursemaine, heuredebut, salleid, datecours, groupeid),
    CONSTRAINT emploidutemps_cin_fkey
        FOREIGN KEY (cin) REFERENCES public.employe(cin),
    CONSTRAINT emploidutemps_matiereid_fkey
        FOREIGN KEY (matiereid) REFERENCES public.matiere(id),
    CONSTRAINT emploidutemps_salleid_fkey
        FOREIGN KEY (salleid) REFERENCES public.salle(id)
);

CREATE TRIGGER trgcreateetat
    AFTER INSERT ON public.emploidutemps
    FOR EACH ROW EXECUTE FUNCTION public.createetatonemploiinsert();

-- =============================================================
-- TABLE: etatdavancement
-- NOTE: depends on employe, emploidutemps
-- =============================================================

CREATE TABLE public.etatdavancement (
    id                  SERIAL          NOT NULL,
    typeactivite        VARCHAR(100),
    objectif            TEXT,
    descriptif          TEXT,
    statut              VARCHAR(50),
    cin                 VARCHAR(20),
    observations        TEXT,
    prochaineseance     VARCHAR(100),
    emploiid            INTEGER,
    CONSTRAINT etatdavancement_pkey PRIMARY KEY (id),
    CONSTRAINT etatdavancement_cin_fkey
        FOREIGN KEY (cin) REFERENCES public.employe(cin),
    CONSTRAINT etatdavancement_emploiid_fkey
        FOREIGN KEY (emploiid) REFERENCES public.emploidutemps(id)
);

-- =============================================================
-- TABLE: emploiprevisionnel
-- NOTE: depends on employe, matiere, salle
-- =============================================================

CREATE TABLE public.emploiprevisionnel (
    id              SERIAL          NOT NULL,
    joursemaine     INTEGER         CHECK (joursemaine >= 1 AND joursemaine <= 7),
    heuredebut      TIME            NOT NULL,
    heurefin        TIME            NOT NULL,
    salleid         INTEGER,
    matiereid       INTEGER,
    cin             VARCHAR(20),
    semestre        INTEGER,
    anneescolaire   VARCHAR(20),
    datecours       DATE,
    groupeid        INTEGER,
    CONSTRAINT emploiprevisionnel_pkey PRIMARY KEY (id),
    CONSTRAINT emploiprevisionnel_cin_fkey
        FOREIGN KEY (cin) REFERENCES public.employe(cin),
    CONSTRAINT emploiprevisionnel_matiereid_fkey
        FOREIGN KEY (matiereid) REFERENCES public.matiere(id),
    CONSTRAINT emploiprevisionnel_salleid_fkey
        FOREIGN KEY (salleid) REFERENCES public.salle(id)
);

-- =============================================================
-- TABLE: indisponibiliteemploye
-- NOTE: depends on employe
-- =============================================================

CREATE TABLE public.indisponibiliteemploye (
    id          SERIAL          NOT NULL,
    cin         VARCHAR(20),
    joursemaine INTEGER         CHECK (joursemaine >= 1 AND joursemaine <= 7),
    heuredebut  TIME            NOT NULL,
    heurefin    TIME            NOT NULL,
    raison      TEXT,
    CONSTRAINT indisponibiliteemploye_pkey PRIMARY KEY (id),
    CONSTRAINT indisponibiliteemploye_cin_fkey
        FOREIGN KEY (cin) REFERENCES public.employe(cin) ON DELETE CASCADE
);