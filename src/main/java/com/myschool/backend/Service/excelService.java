// package com.myschool.backend.Service;

// import com.myschool.backend.Model.EmploiDuTemps;
// import com.myschool.backend.Repository.EmploiDuTempsRepository;
// import org.apache.poi.ss.usermodel.*;
// import org.apache.poi.xssf.usermodel.XSSFWorkbook;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;

// import java.io.IOException;
// import java.io.InputStream;
// import java.time.LocalDate;
// import java.time.LocalTime;
// import java.time.ZoneId;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.Date;
// import java.util.List;

// @Service
// public class excelService {

//     private final EmploiDuTempsRepository emploiDuTempsRepository;

//     public excelService(EmploiDuTempsRepository emploiDuTempsRepository) {
//         this.emploiDuTempsRepository = emploiDuTempsRepository;
//     }

//     public void importExcelData(MultipartFile file) throws IOException {
//         List<EmploiDuTemps> entities = new ArrayList<>();

//         try (InputStream inputStream = file.getInputStream();
//              Workbook workbook = new XSSFWorkbook(inputStream)) {

//             Sheet sheet = workbook.getSheetAt(0);

//             for (Row row : sheet) {
//                 if (row.getRowNum() == 0) continue; // Skip header

//                 try {
//                     EmploiDuTemps entity = new EmploiDuTemps();

//                     // Column 0: Date
//                     entity.setdate(getCellAsLocalDate(row, 0));

//                     // Column 1: Heure (e.g. "08:00 - 10:00")
//                     String timeRange = getStringCellValue(row, 1);
//                     String[] parts = timeRange.split("-");
//                     if (parts.length != 2) {
//                         throw new RuntimeException("Invalid time format: " + timeRange);
//                     }

//                     LocalTime heureDebut = LocalTime.parse(parts[0].trim());
//                     LocalTime heureFin = LocalTime.parse(parts[1].trim());

//                     entity.setHeure_debut(heureDebut);
//                     entity.setHeure_fin(heureFin);

//                     // Other fields
//                     entity.setCin(getStringCellValue(row, 2));           // Formateur
//                     entity.setCode_matiere(getStringCellValue(row, 3));  // Matière
//                     entity.setSalle(getStringCellValue(row, 4));         // Salle
//                     entity.setSemestre(getStringCellValue(row, 5));      // Semestre

//                     entities.add(entity);
//                 } catch (Exception e) {
//                     System.err.println("⚠️ Error processing row " + row.getRowNum() + ": " + e.getMessage());
//                 }
//             }
//         }

//         if (!entities.isEmpty()) {
//             emploiDuTempsRepository.saveAll(entities);
//             System.out.println("✅ Saved " + entities.size() + " entries to the database.");
//         } else {
//             System.err.println("⚠️ No valid data to save.");
//         }
//     }

//     private LocalDate getCellAsLocalDate(Row row, int cellIndex) {
//         Cell cell = row.getCell(cellIndex);
//         if (cell == null) {
//             throw new RuntimeException("Date cell is empty");
//         }

//         try {
//             if (cell.getCellType() == CellType.NUMERIC) {
//                 // Excel stores actual date as a numeric value
//                 Date date = cell.getDateCellValue();
//                 return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//             } else if (cell.getCellType() == CellType.STRING) {
//                 String dateStr = cell.getStringCellValue().trim();

//                 // Try known formats (dd/MM/yyyy, dd-MM-yyyy, yyyy-MM-dd)
//                 DateTimeFormatter[] formatters = {
//                     DateTimeFormatter.ofPattern("dd/MM/yyyy"),
//                     DateTimeFormatter.ofPattern("dd-MM-yyyy"),
//                     DateTimeFormatter.ISO_LOCAL_DATE
//                 };

//                 for (DateTimeFormatter formatter : formatters) {
//                     try {
//                         return LocalDate.parse(dateStr, formatter);
//                     } catch (Exception ignored) {}
//                 }

//                 throw new RuntimeException("Unrecognized date format: " + dateStr);
//             } else {
//                 throw new RuntimeException("Unsupported date cell type: " + cell.getCellType());
//             }
//         } catch (Exception e) {
//             throw new RuntimeException("Invalid date format at cell " + cellIndex);
//         }
//     }


//     private String getStringCellValue(Row row, int cellIndex) {
//         Cell cell = row.getCell(cellIndex);
//         if (cell == null) return "";

//         switch (cell.getCellType()) {
//             case STRING:
//                 return cell.getStringCellValue().trim();
//             case NUMERIC:
//                 // If numeric but looks like an ID (e.g. Formateur CIN), cast to int then to string
//                 double num = cell.getNumericCellValue();
//                 if (num == (int) num) {
//                     return String.valueOf((int) num);  // No decimal part
//                 } else {
//                     return String.valueOf(num);        // Keep decimal if needed
//                 }
//             case BOOLEAN:
//                 return String.valueOf(cell.getBooleanCellValue());
//             case FORMULA:
//                 return cell.getCellFormula();
//             case BLANK:
//                 return "";
//             default:
//                 throw new RuntimeException("Unsupported cell type at cell " + cellIndex);
//         }
//     }
// }
