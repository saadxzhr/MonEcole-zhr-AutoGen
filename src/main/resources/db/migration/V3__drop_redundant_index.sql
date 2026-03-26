//Redundant
DROP INDEX IF EXISTS idx_modulex_codefiliere;

//duplicate
ALTER TABLE modulex
DROP CONSTRAINT IF EXISTS modulex_codefiliere_fkey;
