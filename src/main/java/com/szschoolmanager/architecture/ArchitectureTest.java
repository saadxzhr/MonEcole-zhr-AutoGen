// package com.szschoolmanager.architecture;

// import com.tngtech.archunit.junit.AnalyzeClasses;
// import com.tngtech.archunit.junit.ArchTest;
// import com.tngtech.archunit.lang.ArchRule;

// import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
// import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

// @AnalyzeClasses(packages = "com.myschool.backend")
// public class ArchitectureTest {

//     // 1️⃣ Les controllers ne doivent pas accéder directement aux repositories
//     @ArchTest
//     static final ArchRule controllers_should_not_depend_on_repositories =
//             noClasses()
//                     .that().resideInAPackage("..controller..")
//                     .should().dependOnClassesThat().resideInAPackage("..repository..");

//     // 2️⃣ Les services ne doivent pas dépendre des controllers
//     @ArchTest
//     static final ArchRule services_should_not_depend_on_controllers =
//             noClasses()
//                     .that().resideInAPackage("..service..")
//                     .should().dependOnClassesThat().resideInAPackage("..controller..");

//     // 3️⃣ Les repositories ne doivent dépendre que des modèles (entity)
//     @ArchTest
//     static final ArchRule repositories_should_only_depend_on_models =
//             classes()
//                     .that().resideInAPackage("..repository..")
//                     .should().onlyDependOnClassesThat()
//                     .resideInAnyPackage("..model..", "java..", "jakarta..",
// "org.springframework.data..");
// }
