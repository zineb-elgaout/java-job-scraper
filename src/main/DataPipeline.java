package main;

import processing.CleanData;
import processing.PrepareData;
import processing.DataQualityFix;
import processing.Vectorization;

public class DataPipeline {
    public static void main(String[] args) {
        System.out.println("🚀 PIPELINE COMPLET DE TRAITEMENT DES DONNÉES");
        System.out.println("═".repeat(60));
        System.out.println("Base de données : job_scraper");
        System.out.println("Table source : jobs");
        System.out.println("═".repeat(60));
        
        try {
            // Étape 1 : Nettoyage
            System.out.println("\n📦 ÉTAPE 1 : NETTOYAGE DES DONNÉES");
            System.out.println("-".repeat(40));
            CleanData.main(args);
            
            Thread.sleep(1000);
            
            // Étape 2 : Préparation
            System.out.println("\n🔧 ÉTAPE 2 : PRÉPARATION DES DONNÉES");
            System.out.println("-".repeat(40));
            PrepareData.main(args);
            
            Thread.sleep(1000);
            
            // Étape 3 : Correction qualité
            System.out.println("\n✨ ÉTAPE 3 : CORRECTION QUALITÉ");
            System.out.println("-".repeat(40));
            DataQualityFix.main(args);
            
            Thread.sleep(1000);
            
            // Étape 4 : Vectorisation
            System.out.println("\n🧠 ÉTAPE 4 : VECTORISATION POUR ML");
            System.out.println("-".repeat(40));
            Vectorization.main(args);
            
            System.out.println("\n✅ PIPELINE TERMINÉ AVEC SUCCÈS !");
            System.out.println("═".repeat(60));
            
        } catch (InterruptedException e) {
            System.err.println("Pipeline interrompu");
        } catch (Exception e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}