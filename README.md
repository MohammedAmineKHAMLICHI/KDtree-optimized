# KDTree optimized
[![CI](https://github.com/MohammedAmineKHAMLICHI/KDtree-optimized/actions/workflows/ci.yml/badge.svg)](https://github.com/MohammedAmineKHAMLICHI/KDtree-optimized/actions/workflows/ci.yml)

Auteur : Mohammed Amine KHAMLICHI
LinkedIn : https://www.linkedin.com/in/mohammedaminekhamlichi/

## 🎯 Résumé du projet
Implémentation Java d’un KD-tree 2D avec attributs textuels, construction par médiane, requêtes min/max et range search, mini parseur SQL et interface console interactive.

## 🧭 Contexte et objectif
Projet orienté algorithmique et structures de données. Objectif principal : fournir un KD-tree efficace pour requêtes de proximité et filtrage, avec une CLI permettant de charger des échantillons, d’exécuter des requêtes simples et de visualiser l’arbre en ASCII.

## 🔑 Fonctionnalités principales
- Construction d’un KD-tree équilibré à partir d’échantillons texte.
- Recherches min/max par axe et range search axis-aligned.
- Mini langage de requêtes SQL-like pour filtrer les points.
- Export/import d’échantillons et affichage ASCII de l’arbre.
- Suite de tests Gradle/JUnit.

## 🛠️ Stack technique
- Java 21 (compatible 17)
- Gradle 8.x (wrapper fourni)
- JUnit pour les tests

## ⚙️ Installation
1. Installer un JDK 21 (ou 17).
2. S’assurer que `JAVA_HOME` et `PATH` pointent vers le JDK.
3. Rendre le wrapper exécutable si besoin : `chmod +x gradlew`.

## 🚀 Utilisation
- Construire et tester :  
  `./gradlew test` (Linux/macOS) ou `./gradlew.bat test` (Windows)
- Construire le JAR :  
  `./gradlew build` puis lancer avec  
  `java -cp build/libs/KDTree_Last-1.0-SNAPSHOT.jar org.application.Main`
- Fichiers d’exemple : `src/main/resources/1.txt` et `src/main/resources/2.txt`.

## 🗂️ Structure du dépôt
- `src/main/java/org/application/` : entrée console (`Main`, `Menu`)
- `src/main/java/org/application/kdtree/` : structure KD-tree et nœuds
- `src/main/java/org/application/menu/` : CLI et exécution de requêtes
- `src/main/java/org/application/utils/` : outils de tri (HeapSort)
- `src/main/resources/` : échantillons texte
- `src/test/java/` : tests
- `.github/workflows/ci.yml` : CI GitHub Actions (Gradle)

## ✅ Tests
- Commande : `./gradlew test`
- CI : workflow GitHub Actions `ci.yml` (JDK Temurin 21)

## 🌟 Compétences mises en avant
- Conception de structures de données (KD-tree) et requêtes spatiales
- Parsing et exécution de requêtes simples
- Tests automatisés avec Gradle/JUnit
- Maintenance d’une CLI Java et gestion du build Gradle
