package processing;

import java.sql.*;

public class PrepareData {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3307/job_scraper";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            Statement stmt = conn.createStatement();
            
            // 1. CRÉATION DE LA TABLE
            stmt.executeUpdate("DROP TABLE IF EXISTS prepared_data");
            
            String createTable = """
                CREATE TABLE prepared_data (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    title VARCHAR(500),
                    company VARCHAR(200),
                    location VARCHAR(200),
                    description TEXT,
                    source VARCHAR(100),
                    link VARCHAR(500),
                    title_clean VARCHAR(500),
                    company_clean VARCHAR(200),
                    location_clean VARCHAR(200),
                    description_clean TEXT,
                    category VARCHAR(100),
                    contract_type VARCHAR(50),
                    experience_level VARCHAR(50),
                    education_level VARCHAR(50),
                    salary_min INT,
                    salary_category VARCHAR(50),
                    description_length INT,
                    title_length INT,
                    is_remote BOOLEAN,
                    has_salary_info BOOLEAN,
                    has_experience BOOLEAN,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;
            
            stmt.executeUpdate(createTable);
            System.out.println("✅ Table prepared_data créée");
            
            // 2. TRANSFERT DES DONNÉES
            String insertQuery = """
                INSERT INTO prepared_data (
                    title, company, location, description, source, link,
                    title_clean, company_clean, location_clean, description_clean,
                    description_length, title_length
                )
                SELECT 
                    title,
                    company,
                    location,
                    description,
                    source,
                    link,
                    LOWER(TRIM(REPLACE(REPLACE(REPLACE(COALESCE(title, ''), '\\n', ' '), '\\r', ' '), '  ', ' '))),
                    LOWER(TRIM(COALESCE(company, ''))),
                    CASE 
                        WHEN LOWER(COALESCE(location, '')) LIKE '%casablanca%' THEN 'Casablanca'
                        WHEN LOWER(COALESCE(location, '')) LIKE '%rabat%' THEN 'Rabat'
                        WHEN LOWER(COALESCE(location, '')) LIKE '%marrakech%' THEN 'Marrakech'
                        WHEN LOWER(COALESCE(location, '')) LIKE '%fès%' OR 
                             LOWER(COALESCE(location, '')) LIKE '%fes%' THEN 'Fès'
                        WHEN LOWER(COALESCE(location, '')) LIKE '%tanger%' THEN 'Tanger'
                        WHEN LOWER(COALESCE(location, '')) LIKE '%agadir%' THEN 'Agadir'
                        WHEN LOWER(COALESCE(location, '')) LIKE '%mohammedia%' THEN 'Mohammedia'
                        WHEN LOWER(COALESCE(location, '')) LIKE '%kenitra%' THEN 'Kenitra'
                        WHEN LOWER(COALESCE(location, '')) LIKE '%tetouan%' THEN 'Tétouan'
                        WHEN LOWER(COALESCE(location, '')) LIKE '%sale%' THEN 'Salé'
                        ELSE 'Autre'
                    END,
                    LOWER(TRIM(REPLACE(REPLACE(REPLACE(COALESCE(description, ''), '\\n', ' '), '\\r', ' '), '  ', ' '))),
                    LENGTH(COALESCE(description, '')),
                    LENGTH(COALESCE(title, ''))
                FROM clean_data
                WHERE title IS NOT NULL 
                  AND TRIM(title) != ''
                  AND description IS NOT NULL
                  AND LENGTH(description) > 30;
                """;
            
            int inserted = stmt.executeUpdate(insertQuery);
            System.out.println("📥 " + inserted + " offres préparées");
            
            // 3. EXTRACTIONS
            System.out.println("\n🔧 Extraction des informations :");
            extractCategories(conn);
            extractContractTypes(conn);
            extractExperienceLevels(conn);
            extractEducationLevels(conn);
            
            // 4. EXTRACTION DES SALAIRES
            extractSalariesSimplified(conn);
            
            // 5. EXTRACTION DU TÉLÉTRAVAIL
            extractRemoteWork(conn);
            
            // 6. SUPPRESSION DES DOUBLONS
            removeDuplicates(conn);
            
            // 7. MISE À JOUR DES INDICATEURS D'EXPÉRIENCE
            updateExperienceIndicator(conn);
            
            // 8. STATISTIQUES DÉTAILLÉES
            checkPreparedData(conn);
            
            System.out.println("✅ Préparation terminée");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ========== MÉTHODE D'EXTRACTION DES SALAIRES SIMPLIFIÉE ==========
    private static void extractSalariesSimplified(Connection conn) throws SQLException {
        System.out.println("\n💰 EXTRACTION DES SALAIRES");
        
        // 1. D'abord, identifier les offres qui mentionnent un salaire
        String updateHasSalary = """
            UPDATE prepared_data
            SET has_salary_info = CASE
                WHEN description_clean LIKE '%DH%' 
                     OR description_clean LIKE '%salaire%'
                     OR description_clean LIKE '%mad%'
                     OR description_clean LIKE '%dirham%'
                     OR description_clean LIKE '%rémunération%'
                     OR description_clean LIKE '%salaire proposé%'
                     OR description_clean LIKE '%rémunéré%' THEN TRUE
                ELSE FALSE
            END;
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(updateHasSalary);
            
            // Vérifier combien d'offres mentionnent un salaire
            String countQuery = "SELECT COUNT(*) FROM prepared_data WHERE has_salary_info = TRUE";
            ResultSet rs = stmt.executeQuery(countQuery);
            if (rs.next()) {
                System.out.println("✅ " + rs.getInt(1) + " offres mentionnent un salaire");
            }
        }
        
        // 2. Extraire les valeurs numériques des salaires
        extractSalaryValues(conn);
        
        // 3. Créer les catégories de salaire
        createSalaryCategories(conn);
    }
    
    private static void extractSalaryValues(Connection conn) throws SQLException {
        System.out.println("\n🔍 Extraction des valeurs numériques...");
        
        // Méthode simple : chercher les nombres avant "DH"
        String query = "SELECT id, description_clean FROM prepared_data WHERE has_salary_info = TRUE";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            int extractedCount = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String desc = rs.getString("description_clean");
                
                int salary = extractSalaryNumber(desc);
                if (salary > 0) {
                    // Utiliser un nouveau statement pour l'update
                    try (Statement updateStmt = conn.createStatement()) {
                        String update = "UPDATE prepared_data SET salary_min = " + salary + " WHERE id = " + id;
                        updateStmt.executeUpdate(update);
                    }
                    extractedCount++;
                }
            }
            System.out.println("✅ " + extractedCount + " valeurs salariales extraites");
        }
    }
    
    private static int extractSalaryNumber(String text) {
        if (text == null || text.isEmpty()) return 0;
        
        text = text.toLowerCase();
        
        // Chercher différents motifs de salaire
        String[] patterns = {"dh", "mad", "salaire", "rémunération", "rémunéré"};
        
        for (String pattern : patterns) {
            int patternIndex = text.indexOf(pattern);
            if (patternIndex != -1) {
                // Extraire 50 caractères avant le motif
                int start = Math.max(0, patternIndex - 50);
                String beforePattern = text.substring(start, patternIndex);
                
                // Chercher le dernier nombre dans cette partie
                int salary = findLastValidNumber(beforePattern);
                if (salary > 0) {
                    return salary;
                }
            }
        }
        
        return 0;
    }
    
    private static int findLastValidNumber(String text) {
        // Extraire tous les nombres du texte
        String numbersOnly = text.replaceAll("[^0-9]", " ");
        String[] numberParts = numbersOnly.trim().split("\\s+");
        
        // Parcourir de la fin au début pour trouver le dernier nombre valide
        for (int i = numberParts.length - 1; i >= 0; i--) {
            String part = numberParts[i];
            if (!part.isEmpty()) {
                try {
                    int num = Integer.parseInt(part);
                    // Valider si c'est un salaire plausible (1000-50000 DH)
                    if (num >= 1000 && num <= 50000) {
                        return num;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return 0;
    }
    
    private static void createSalaryCategories(Connection conn) throws SQLException {
        System.out.println("\n🏷️ Création des catégories de salaire");
        
        String updateQuery = """
            UPDATE prepared_data
            SET salary_category = CASE
                -- Quand on a une valeur numérique
                WHEN salary_min IS NOT NULL AND salary_min > 0 THEN 
                    CASE
                        WHEN salary_min < 4000 THEN 'Bas (< 4000 DH)'
                        WHEN salary_min < 8000 THEN 'Moyen (4000-8000 DH)'
                        WHEN salary_min < 12000 THEN 'Élevé (8000-12000 DH)'
                        ELSE 'Très élevé (> 12000 DH)'
                    END
                -- Quand l'offre mentionne un salaire mais pas de valeur extraite
                WHEN has_salary_info = TRUE THEN 'Mentionné mais non spécifié'
                -- Quand l'offre dit "à discuter" ou "négociable"
                WHEN description_clean LIKE '%à discuter%' 
                     OR description_clean LIKE '%a discuter%'
                     OR description_clean LIKE '%négociable%' THEN 'À discuter'
                -- Quand l'offre mentionne qu'il n'y a pas de salaire
                WHEN description_clean LIKE '%non rémunéré%' 
                     OR description_clean LIKE '%gratuit%'
                     OR description_clean LIKE '%sans rémunération%' THEN 'Non rémunéré'
                -- Par défaut
                ELSE 'Non spécifié'
            END;
            """;
        
        try (Statement stmt = conn.createStatement()) {
            int updated = stmt.executeUpdate(updateQuery);
            
            // Statistiques des catégories
            String statsQuery = """
                SELECT 
                    salary_category,
                    COUNT(*) as count,
                    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM prepared_data), 1) as percentage
                FROM prepared_data
                GROUP BY salary_category
                ORDER BY count DESC;
                """;
            
            ResultSet rs = stmt.executeQuery(statsQuery);
            System.out.println("\n📊 DISTRIBUTION DES CATÉGORIES DE SALAIRE :");
            System.out.println("─".repeat(50));
            while (rs.next()) {
                System.out.printf("%-30s : %4d offres (%5.1f%%)\n",
                    rs.getString("salary_category"),
                    rs.getInt("count"),
                    rs.getDouble("percentage"));
            }
        }
    }
    
    // ========== EXTRACTION DU TÉLÉTRAVAIL ==========
    private static void extractRemoteWork(Connection conn) throws SQLException {
        System.out.println("\n🏠 EXTRACTION DU TÉLÉTRAVAIL");
        
        String updateQuery = """
            UPDATE prepared_data 
            SET is_remote = CASE
                WHEN LOWER(description_clean) LIKE '%remote%' THEN TRUE
                WHEN LOWER(description_clean) LIKE '%télétravail%' THEN TRUE
                WHEN LOWER(description_clean) LIKE '%teletravail%' THEN TRUE
                WHEN LOWER(description_clean) LIKE '%home office%' THEN TRUE
                WHEN LOWER(description_clean) LIKE '%travail à distance%' THEN TRUE
                WHEN LOWER(description_clean) LIKE '%travail à domicile%' THEN TRUE
                WHEN LOWER(title_clean) LIKE '%remote%' THEN TRUE
                WHEN LOWER(title_clean) LIKE '%télétravail%' THEN TRUE
                ELSE FALSE
            END;
            """;
        
        try (Statement stmt = conn.createStatement()) {
            int updated = stmt.executeUpdate(updateQuery);
            
            String countQuery = "SELECT SUM(is_remote) as remote_count FROM prepared_data";
            ResultSet rs = stmt.executeQuery(countQuery);
            if (rs.next()) {
                System.out.println("✅ " + rs.getInt("remote_count") + " offres en télétravail");
            }
        }
    }
    
    // ========== MISE À JOUR INDICATEUR D'EXPÉRIENCE ==========
    private static void updateExperienceIndicator(Connection conn) throws SQLException {
        String updateQuery = """
            UPDATE prepared_data
            SET has_experience = CASE
                WHEN experience_level != 'Non spécifié' THEN TRUE
                ELSE FALSE
            END;
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(updateQuery);
        }
    }
    
    // ========== MÉTHODES D'EXTRACTION DES CATÉGORIES - CORRIGÉES ==========
    private static void extractCategories(Connection conn) throws SQLException {
        System.out.println("\n🏷️ EXTRACTION DES CATÉGORIES (version améliorée)");
        
        String updateQuery = """
            UPDATE prepared_data pd
            SET category = CASE
                -- 1. IT & TECHNOLOGIES (très spécifique)
                WHEN LOWER(pd.title_clean) LIKE '%developer%' 
                     OR LOWER(pd.title_clean) LIKE '%développeur%'
                     OR LOWER(pd.title_clean) LIKE '%programmer%'
                     OR LOWER(pd.title_clean) LIKE '%software engineer%'
                     OR (LOWER(pd.description_clean) LIKE '%java%' 
                         AND LOWER(pd.description_clean) NOT LIKE '%café%')
                     OR LOWER(pd.description_clean) LIKE '%python%'
                     OR LOWER(pd.description_clean) LIKE '%javascript%'
                     OR LOWER(pd.description_clean) LIKE '%html%'
                     OR LOWER(pd.description_clean) LIKE '%css%'
                     OR LOWER(pd.description_clean) LIKE '%sql%'
                     OR LOWER(pd.description_clean) LIKE '%database%' 
                     OR LOWER(pd.description_clean) LIKE '%base de données%' THEN 'IT - Développement'
                
                -- 2. SUPPORT INFORMATIQUE (seulement pour IT)
                WHEN (LOWER(pd.title_clean) LIKE '%technicien informatique%'
                     OR LOWER(pd.title_clean) LIKE '%support informatique%'
                     OR LOWER(pd.title_clean) LIKE '%helpdesk%'
                     OR LOWER(pd.title_clean) LIKE '%administrateur réseau%'
                     OR LOWER(pd.title_clean) LIKE '%admin sys%')
                     AND LOWER(pd.description_clean) LIKE '%informatique%' THEN 'IT - Support'
                
                -- 3. DATA & ANALYSE
                WHEN (LOWER(pd.title_clean) LIKE '%data scientist%'
                     OR LOWER(pd.title_clean) LIKE '%data analyst%'
                     OR LOWER(pd.title_clean) LIKE '%analyste de données%'
                     OR LOWER(pd.title_clean) LIKE '%business intelligence%'
                     OR LOWER(pd.title_clean) LIKE '%bi developer%') THEN 'IT - Data'
                
                -- 4. MAINTENANCE AUTO/MÉCANIQUE (à exclure de IT)
                WHEN LOWER(pd.title_clean) LIKE '%mécanicien%'
                     OR LOWER(pd.title_clean) LIKE '%mécanique auto%'
                     OR LOWER(pd.title_clean) LIKE '%maintenance automobile%'
                     OR LOWER(pd.title_clean) LIKE '%garagiste%'
                     OR LOWER(pd.title_clean) LIKE '%réparation auto%' THEN 'Automobile/Mécanique'
                
                -- 5. MAINTENANCE INDUSTRIELLE
                WHEN LOWER(pd.title_clean) LIKE '%maintenance industrielle%'
                     OR LOWER(pd.title_clean) LIKE '%technicien maintenance%'
                     OR (LOWER(pd.description_clean) LIKE '%maintenance%' 
                         AND LOWER(pd.description_clean) LIKE '%industriel%') THEN 'Maintenance Industrielle'
                
                -- 6. COMMERCIAL & VENTE
                WHEN LOWER(pd.title_clean) LIKE '%commercial%'
                     OR LOWER(pd.title_clean) LIKE '%sales%'
                     OR LOWER(pd.title_clean) LIKE '%vendeur%'
                     OR LOWER(pd.title_clean) LIKE '%conseiller commercial%'
                     OR LOWER(pd.title_clean) LIKE '%agent commercial%' THEN 'Commercial/Vente'
                
                -- 7. LOGISTIQUE & TRANSPORT
                WHEN LOWER(pd.title_clean) LIKE '%logisticien%'
                     OR LOWER(pd.title_clean) LIKE '%logistique%'
                     OR LOWER(pd.title_clean) LIKE '%transport%'
                     OR LOWER(pd.title_clean) LIKE '%chauffeur%'
                     OR LOWER(pd.title_clean) LIKE '%livreur%' THEN 'Logistique/Transport'
                
                -- 8. RESSOURCES HUMAINES
                WHEN LOWER(pd.title_clean) LIKE '%rh%'
                     OR LOWER(pd.title_clean) LIKE '%ressources humaines%'
                     OR LOWER(pd.title_clean) LIKE '%recruteur%'
                     OR LOWER(pd.title_clean) LIKE '%recrutement%' THEN 'Ressources Humaines'
                
                -- 9. COMPTABILITÉ & FINANCE
                WHEN LOWER(pd.title_clean) LIKE '%comptable%'
                     OR LOWER(pd.title_clean) LIKE '%comptabilité%'
                     OR LOWER(pd.title_clean) LIKE '%accounting%'
                     OR LOWER(pd.title_clean) LIKE '%finance%'
                     OR LOWER(pd.title_clean) LIKE '%auditeur%' THEN 'Comptabilité/Finance'
                
                -- 10. ADMINISTRATIF & SECRÉTARIAT
                WHEN LOWER(pd.title_clean) LIKE '%secrétaire%'
                     OR LOWER(pd.title_clean) LIKE '%assistant%'
                     OR LOWER(pd.title_clean) LIKE '%administratif%'
                     OR LOWER(pd.title_clean) LIKE '%back office%' THEN 'Administratif'
                
                -- 11. HÔTELLERIE & RESTAURATION
                WHEN LOWER(pd.title_clean) LIKE '%barman%'
                     OR LOWER(pd.title_clean) LIKE '%restaurant%'
                     OR LOWER(pd.title_clean) LIKE '%serveur%'
                     OR LOWER(pd.title_clean) LIKE '%cuisinier%'
                     OR LOWER(pd.title_clean) LIKE '%hôtellerie%' THEN 'Hôtellerie/Restauration'
                
                -- 12. MANAGEMENT & DIRECTION
                WHEN LOWER(pd.title_clean) LIKE '%manager%'
                     OR LOWER(pd.title_clean) LIKE '%directeur%'
                     OR LOWER(pd.title_clean) LIKE '%chef de%'
                     OR LOWER(pd.title_clean) LIKE '%responsable%' THEN 'Management'
                
                -- 13. MARKETING & COMMUNICATION
                WHEN LOWER(pd.title_clean) LIKE '%marketing%'
                     OR LOWER(pd.title_clean) LIKE '%communication%'
                     OR LOWER(pd.title_clean) LIKE '%community manager%'
                     OR LOWER(pd.title_clean) LIKE '%publicité%' THEN 'Marketing/Communication'
                
                -- 14. PRODUCTION & INDUSTRIE
                WHEN LOWER(pd.title_clean) LIKE '%opérateur%'
                     OR LOWER(pd.title_clean) LIKE '%production%'
                     OR LOWER(pd.title_clean) LIKE '%conducteur%'
                     OR LOWER(pd.title_clean) LIKE '%ouvrier%'
                     OR LOWER(pd.title_clean) LIKE '%industrie%' THEN 'Production/Industrie'
                
                -- 15. SANTÉ & SOCIAL
                WHEN LOWER(pd.title_clean) LIKE '%infirmier%'
                     OR LOWER(pd.title_clean) LIKE '%médecin%'
                     OR LOWER(pd.title_clean) LIKE '%santé%'
                     OR LOWER(pd.title_clean) LIKE '%social%' THEN 'Santé/Social'
                
                -- 16. ÉDUCATION & FORMATION
                WHEN LOWER(pd.title_clean) LIKE '%enseignant%'
                     OR LOWER(pd.title_clean) LIKE '%professeur%'
                     OR LOWER(pd.title_clean) LIKE '%formateur%'
                     OR LOWER(pd.title_clean) LIKE '%éducation%' THEN 'Éducation/Formation'
                
                -- 17. BÂTIMENT & CONSTRUCTION
                WHEN LOWER(pd.title_clean) LIKE '%maçon%'
                     OR LOWER(pd.title_clean) LIKE '%plombier%'
                     OR LOWER(pd.title_clean) LIKE '%électricien%'
                     OR LOWER(pd.title_clean) LIKE '%bâtiment%'
                     OR LOWER(pd.title_clean) LIKE '%construction%' THEN 'Bâtiment/Construction'
                
                -- 18. AGRICULTURE & AGROALIMENTAIRE
                WHEN LOWER(pd.title_clean) LIKE '%agriculture%'
                     OR LOWER(pd.title_clean) LIKE '%agroalimentaire%'
                     OR LOWER(pd.description_clean) LIKE '%agricole%' THEN 'Agriculture/Agroalimentaire'
                
                -- 19. TOURISME
                WHEN LOWER(pd.title_clean) LIKE '%tourisme%'
                     OR LOWER(pd.title_clean) LIKE '%guide touristique%'
                     OR LOWER(pd.title_clean) LIKE '%agence de voyage%' THEN 'Tourisme'
                
                -- Par défaut
                ELSE 'Autre'
            END;
            """;
        
        try (Statement stmt = conn.createStatement()) {
            int updated = stmt.executeUpdate(updateQuery);
            System.out.println("📝 " + updated + " catégories extraites");
            
            // Afficher la distribution des catégories
            showCategoryDistribution(conn);
        }
    }
    
    private static void showCategoryDistribution(Connection conn) throws SQLException {
        String query = """
            SELECT 
                category,
                COUNT(*) as count,
                ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM prepared_data), 1) as percentage
            FROM prepared_data
            GROUP BY category
            ORDER BY count DESC;
            """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            System.out.println("\n📊 DISTRIBUTION DES CATÉGORIES :");
            System.out.println("─".repeat(50));
            int total = 0;
            int autreCount = 0;
            
            while (rs.next()) {
                String category = rs.getString("category");
                int count = rs.getInt("count");
                double percentage = rs.getDouble("percentage");
                total += count;
                
                if ("Autre".equals(category)) {
                    autreCount = count;
                } else {
                    System.out.printf("%-25s : %4d offres (%5.1f%%)\n",
                        category, count, percentage);
                }
            }
            
            if (autreCount > 0) {
                double autrePercentage = (autreCount * 100.0) / total;
                System.out.printf("%-25s : %4d offres (%5.1f%%)\n",
                    "Autre", autreCount, autrePercentage);
            }
        }
    }
    
    private static void extractContractTypes(Connection conn) throws SQLException {
        String updateQuery = """
            UPDATE prepared_data pd
            SET contract_type = CASE
                WHEN LOWER(pd.description_clean) LIKE '%cdi%' 
                     OR LOWER(pd.title_clean) LIKE '%cdi%' THEN 'CDI'
                WHEN LOWER(pd.description_clean) LIKE '%cdd%' 
                     OR LOWER(pd.title_clean) LIKE '%cdd%' THEN 'CDD'
                WHEN LOWER(pd.description_clean) LIKE '%stage%' 
                     OR LOWER(pd.title_clean) LIKE '%stage%' THEN 'Stage'
                WHEN LOWER(pd.description_clean) LIKE '%alternance%' 
                     OR LOWER(pd.title_clean) LIKE '%alternance%' THEN 'Alternance'
                WHEN LOWER(pd.description_clean) LIKE '%freelance%' 
                     OR LOWER(pd.description_clean) LIKE '%indépendant%' 
                     OR LOWER(pd.description_clean) LIKE '%consultant%' THEN 'Freelance'
                WHEN LOWER(pd.description_clean) LIKE '%intérim%' 
                     OR LOWER(pd.description_clean) LIKE '%interim%' THEN 'Intérim'
                ELSE 'Non spécifié'
            END;
            """;
        
        try (Statement stmt = conn.createStatement()) {
            int updated = stmt.executeUpdate(updateQuery);
            System.out.println("📝 " + updated + " types de contrat extraits");
        }
    }
    
    private static void extractExperienceLevels(Connection conn) throws SQLException {
        String updateQuery = """
            UPDATE prepared_data pd
            SET experience_level = CASE
                WHEN LOWER(pd.description_clean) LIKE '%débutant%' 
                     OR LOWER(pd.description_clean) LIKE '%junior%' 
                     OR LOWER(pd.description_clean) LIKE '%0 à 2 ans%'
                     OR LOWER(pd.description_clean) LIKE '%0-2 ans%' 
                     OR LOWER(pd.title_clean) LIKE '%junior%' THEN 'Débutant'
                WHEN LOWER(pd.description_clean) LIKE '%intermédiaire%' 
                     OR LOWER(pd.description_clean) LIKE '%3 à 5 ans%'
                     OR LOWER(pd.description_clean) LIKE '%3-5 ans%' THEN 'Intermédiaire'
                WHEN LOWER(pd.description_clean) LIKE '%senior%' 
                     OR LOWER(pd.description_clean) LIKE '%5 à 10 ans%'
                     OR LOWER(pd.description_clean) LIKE '%5-10 ans%' 
                     OR LOWER(pd.title_clean) LIKE '%senior%' THEN 'Senior'
                WHEN LOWER(pd.description_clean) LIKE '%expert%' 
                     OR LOWER(pd.description_clean) LIKE '%+10 ans%'
                     OR LOWER(pd.description_clean) LIKE '%10+ ans%' THEN 'Expert'
                WHEN LOWER(pd.description_clean) LIKE '%manager%' 
                     OR LOWER(pd.description_clean) LIKE '%chef de%' 
                     OR LOWER(pd.title_clean) LIKE '%manager%' THEN 'Manager'
                ELSE 'Non spécifié'
            END;
            """;
        
        try (Statement stmt = conn.createStatement()) {
            int updated = stmt.executeUpdate(updateQuery);
            System.out.println("📝 " + updated + " niveaux d'expérience extraits");
        }
    }
    
    private static void extractEducationLevels(Connection conn) throws SQLException {
        String updateQuery = """
            UPDATE prepared_data pd
            SET education_level = CASE
                WHEN LOWER(pd.description_clean) LIKE '%bac%' 
                     AND NOT LOWER(pd.description_clean) LIKE '%bac+%' THEN 'Bac'
                WHEN LOWER(pd.description_clean) LIKE '%bac+2%' 
                     OR LOWER(pd.description_clean) LIKE '%bts%'
                     OR LOWER(pd.description_clean) LIKE '%dut%'
                     OR LOWER(pd.description_clean) LIKE '%deust%' THEN 'Bac+2'
                WHEN LOWER(pd.description_clean) LIKE '%bac+3%' 
                     OR LOWER(pd.description_clean) LIKE '%licence%'
                     OR LOWER(pd.description_clean) LIKE '%bachelor%' THEN 'Bac+3'
                WHEN LOWER(pd.description_clean) LIKE '%bac+4%' THEN 'Bac+4'
                WHEN LOWER(pd.description_clean) LIKE '%bac+5%' 
                     OR LOWER(pd.description_clean) LIKE '%master%'
                     OR LOWER(pd.description_clean) LIKE '%ingénieur%'
                     OR LOWER(pd.description_clean) LIKE '%engineer%' THEN 'Bac+5'
                WHEN LOWER(pd.description_clean) LIKE '%doctorat%' 
                     OR LOWER(pd.description_clean) LIKE '%phd%' THEN 'Doctorat'
                WHEN LOWER(pd.description_clean) LIKE '%diplôme%' 
                     OR LOWER(pd.description_clean) LIKE '%diplome%' THEN 'Diplôme requis'
                ELSE 'Non spécifié'
            END;
            """;
        
        try (Statement stmt = conn.createStatement()) {
            int updated = stmt.executeUpdate(updateQuery);
            System.out.println("📝 " + updated + " niveaux d'éducation extraits");
        }
    }
    
    private static void removeDuplicates(Connection conn) throws SQLException {
        System.out.println("\n🧹 Suppression des doublons...");
        
        String tempTable = """
            CREATE TABLE prepared_data_temp AS
            SELECT DISTINCT 
                title_clean, 
                company_clean, 
                description_clean,
                category, contract_type, experience_level, education_level,
                location_clean, salary_min, salary_category, description_length, title_length,
                has_salary_info, is_remote, has_experience, source
            FROM prepared_data
            WHERE title_clean IS NOT NULL;
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS prepared_data_temp");
            stmt.executeUpdate(tempTable);
            
            String countQuery = """
                SELECT 
                    (SELECT COUNT(*) FROM prepared_data) as avant,
                    (SELECT COUNT(*) FROM prepared_data_temp) as après;
                """;
            
            ResultSet rs = stmt.executeQuery(countQuery);
            if (rs.next()) {
                int avant = rs.getInt("avant");
                int après = rs.getInt("après");
                System.out.println("✅ Doublons supprimés : " + (avant - après) + " lignes");
            }
            
            stmt.executeUpdate("DROP TABLE prepared_data");
            stmt.executeUpdate("ALTER TABLE prepared_data_temp RENAME TO prepared_data");
        }
    }
    
    private static void checkPreparedData(Connection conn) throws SQLException {
        String query = """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN contract_type != 'Non spécifié' THEN 1 ELSE 0 END) as with_contract,
                SUM(CASE WHEN experience_level != 'Non spécifié' THEN 1 ELSE 0 END) as with_experience,
                SUM(CASE WHEN education_level != 'Non spécifié' THEN 1 ELSE 0 END) as with_education,
                SUM(CASE WHEN category != 'Autre' THEN 1 ELSE 0 END) as with_category,
                SUM(has_salary_info) as with_salary_info,
                SUM(CASE WHEN salary_min IS NOT NULL AND salary_min > 0 THEN 1 ELSE 0 END) as with_salary_value,
                SUM(is_remote) as remote
            FROM prepared_data;
            """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("\n📊 STATISTIQUES FINALES DES DONNÉES PRÉPARÉES :");
                System.out.println("═".repeat(60));
                System.out.println("Total offres : " + total);
                System.out.println("─".repeat(40));
                System.out.println("Avec type de contrat : " + rs.getInt("with_contract") + 
                                 " (" + String.format("%.1f", rs.getInt("with_contract") * 100.0 / total) + "%)");
                System.out.println("Avec niveau d'expérience : " + rs.getInt("with_experience") + 
                                 " (" + String.format("%.1f", rs.getInt("with_experience") * 100.0 / total) + "%)");
                System.out.println("Avec niveau d'éducation : " + rs.getInt("with_education") + 
                                 " (" + String.format("%.1f", rs.getInt("with_education") * 100.0 / total) + "%)");
                System.out.println("Avec catégorie identifiée : " + rs.getInt("with_category") + 
                                 " (" + String.format("%.1f", rs.getInt("with_category") * 100.0 / total) + "%)");
                System.out.println("Avec information salaire : " + rs.getInt("with_salary_info") + 
                                 " (" + String.format("%.1f", rs.getInt("with_salary_info") * 100.0 / total) + "%)");
                System.out.println("Avec valeur salaire : " + rs.getInt("with_salary_value") + 
                                 " (" + String.format("%.1f", rs.getInt("with_salary_value") * 100.0 / total) + "%)");
                System.out.println("En télétravail : " + rs.getInt("remote") + 
                                 " (" + String.format("%.1f", rs.getInt("remote") * 100.0 / total) + "%)");
                System.out.println("═".repeat(60));
                
                // Statistiques supplémentaires sur les salaires
                if (rs.getInt("with_salary_value") > 0) {
                    String salaryStats = """
                        SELECT 
                            AVG(salary_min) as avg_salary,
                            MIN(salary_min) as min_salary,
                            MAX(salary_min) as max_salary,
                            COUNT(CASE WHEN salary_min > 8000 THEN 1 END) as high_salary
                        FROM prepared_data
                        WHERE salary_min IS NOT NULL AND salary_min > 0;
                        """;
                    
                    ResultSet salaryRs = stmt.executeQuery(salaryStats);
                    if (salaryRs.next()) {
                        System.out.println("\n💰 STATISTIQUES SALAIRES :");
                        System.out.println("   Moyenne : " + 
                            String.format("%.0f", salaryRs.getDouble("avg_salary")) + " DH");
                        System.out.println("   Minimum : " + salaryRs.getInt("min_salary") + " DH");
                        System.out.println("   Maximum : " + salaryRs.getInt("max_salary") + " DH");
                        System.out.println("   > 8000 DH : " + salaryRs.getInt("high_salary") + " offres");
                    }
                }
            }
        }
    }
}