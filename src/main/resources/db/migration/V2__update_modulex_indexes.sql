DROP INDEX IF EXISTS idx_modulex_nommodule;
DROP INDEX IF EXISTS idx_modulex_codemodule;
CREATE INDEX idx_modulex_keyset ON modulex(code_filiere, code_module);
