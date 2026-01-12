package ml;

import java.util.*;

public class RecommendationService {
    
    /**
     * Génère des recommandations basées sur une catégorie
     */
    public Map<String, Object> generateRecommendations(String category) {
        Map<String, Object> recommendations = new HashMap<>();

        // Recommandations par catégorie
        Map<String, List<String>> categoryAdvice = new HashMap<>();

        categoryAdvice.put("IT_DEVELOPPEMENT", Arrays.asList(
                "Mettez à jour vos compétences en frameworks modernes",
                "Participez à des projets open source",
                "Obtenez des certifications cloud (AWS, Azure)",
                "Développez votre portfolio GitHub"));

        categoryAdvice.put("COMMERCIAL_VENTE", Arrays.asList(
                "Améliorez vos techniques de négociation",
                "Développez votre réseau professionnel",
                "Suivez une formation en CRM",
                "Participez à des salons professionnels"));

        categoryAdvice.put("RESSOURCES_HUMAINES", Arrays.asList(
                "Formez-vous aux nouvelles lois du travail",
                "Développez vos compétences en psychologie",
                "Maîtrisez les outils de recrutement digitaux",
                "Participez à des conférences RH"));

        categoryAdvice.put("DATA_IA", Arrays.asList(
                "Développez vos compétences en statistiques",
                "Apprenez Python et les librairies de data science",
                "Maîtrisez les outils de visualisation de données",
                "Obtenez des certifications en machine learning"));

        categoryAdvice.put("MARKETING", Arrays.asList(
                "Maîtrisez les outils d'analyse web (Google Analytics)",
                "Développez vos compétences en marketing digital",
                "Apprenez à créer des campagnes publicitaires efficaces",
                "Suivez les tendances des réseaux sociaux"));

        if (categoryAdvice.containsKey(category)) {
            recommendations.put("conseils", categoryAdvice.get(category));
            recommendations.put("formations_suggerees", suggestTrainings(category));
            recommendations.put("competences_cles", suggestSkills(category));
        } else {
            recommendations.put("conseils", Arrays.asList(
                    "Consultez les offres similaires sur les plateformes d'emploi",
                    "Adaptez votre CV aux exigences du marché",
                    "Développez des compétences transversales"));
            recommendations.put("formations_suggerees", suggestTrainings("general"));
            recommendations.put("competences_cles", suggestSkills("general"));
        }

        return recommendations;
    }

    private List<String> suggestTrainings(String category) {
        // Suggestions de formations par catégorie
        Map<String, List<String>> trainings = new HashMap<>();

        trainings.put("IT_DEVELOPPEMENT", Arrays.asList(
                "Formation Full Stack Development",
                "Certification AWS Solutions Architect",
                "Cours de Machine Learning",
                "Formation DevOps"));

        trainings.put("DATA_IA", Arrays.asList(
                "Formation Data Science",
                "Certification TensorFlow",
                "Cours de Big Data",
                "Formation en statistiques avancées"));

        trainings.put("COMMERCIAL_VENTE", Arrays.asList(
                "Formation en techniques de vente avancées",
                "Certification en négociation",
                "Cours de gestion de clientèle",
                "Formation en CRM"));

        trainings.put("general", Arrays.asList(
                "Formation continue dans votre domaine",
                "Développement des compétences transversales",
                "Formation en communication",
                "Cours de gestion de projet"));

        return trainings.getOrDefault(category, trainings.get("general"));
    }

    private List<String> suggestSkills(String category) {
        // Compétences clés par catégorie
        Map<String, List<String>> skills = new HashMap<>();

        skills.put("IT_DEVELOPPEMENT", Arrays.asList(
                "Programmation (Java, Python, JavaScript)",
                "Frameworks (Spring, React, Angular)",
                "Bases de données (SQL, NoSQL)",
                "DevOps et outils cloud"));

        skills.put("DATA_IA", Arrays.asList(
                "Analyse de données",
                "Machine Learning",
                "Statistiques",
                "Visualisation de données"));

        skills.put("COMMERCIAL_VENTE", Arrays.asList(
                "Techniques de vente",
                "Négociation",
                "Gestion de clientèle",
                "Analyse du marché"));

        skills.put("RESSOURCES_HUMAINES", Arrays.asList(
                "Recrutement et sélection",
                "Gestion des conflits",
                "Droit du travail",
                "Communication interne"));

        skills.put("MARKETING", Arrays.asList(
                "Marketing digital",
                "Analyse de marché",
                "Stratégie de marque",
                "Communication"));

        skills.put("general", Arrays.asList(
                "Compétences analytiques",
                "Communication efficace",
                "Résolution de problèmes",
                "Adaptabilité",
                "Travail en équipe"));

        return skills.getOrDefault(category, skills.get("general"));
    }
}