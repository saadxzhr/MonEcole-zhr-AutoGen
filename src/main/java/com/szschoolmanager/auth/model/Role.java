// package com.szschoolmanager.auth.model;

// public enum Role {
//   DIRECTION,
//   ADMIN,
//   SECRETARIAT,
//   FORMATEUR_VACATAIRE,
//   FORMATEUR_PERMANENT,
//   FORMATEUR; // utilisé comme type générique

//   public static Role normalize(String role) {
//     if (role == null) return null;
//     String r = role.trim().toUpperCase();
//     if (r.contains("VAC")) return FORMATEUR_VACATAIRE;
//     if (r.contains("PERM")) return FORMATEUR_PERMANENT;
//     if (r.contains("FORMATEUR")) return FORMATEUR;
//     if (r.equals("ADMIN")) return ADMIN;
//     if (r.equals("DIRECTION")) return DIRECTION;
//     if (r.equals("SECRETARIAT")) return SECRETARIAT;
//     // fallback
//     return valueOf(r);
//   }

//   public boolean isFormateurGroup() {
//     return this == FORMATEUR || this == FORMATEUR_PERMANENT || this == FORMATEUR_VACATAIRE;
//   }
// }

// // public enum Role {
// //     Direction,
// //     Admin,
// //     Secretariat,
// //     Formateur_Vacataire,
// //     Formateur_Permanent,
// //     Formateur; // utilisé comme type générique

// //     public static Role normalize(String role) {
// //         if (role == null) return null;
// //         String r = role.trim();
// //         if (r.contains("Vac") ) return Formateur_Vacataire;
// //         if (r.contains("Perm") ) return Formateur_Permanent;
// //         if (r.contains("Formateur")) return Formateur;
// //         if (r.equals("Admin")) return Admin;
// //         if (r.equals("Direction")) return Direction;
// //         if (r.equals("Secretariat")) return Secretariat;
// //         // fallback
// //         return valueOf(r);
// //     }

// //     public boolean isFormateurGroup() {
// //         return this == Formateur || this == Formateur_Vacataire || this == Formateur_Permanent;
// //     }
