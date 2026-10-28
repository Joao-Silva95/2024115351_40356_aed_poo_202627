package test;

import domain.*;
import repository.DatabaseManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Testes do R4: consistência entre ST e BST, e arquivo das entidades removidas.
 */
public class ConsistencyTest {

    /**
     * Ficheiro de arquivo usado só nos testes.
     */
    private static final String FILE = "test_output/archive_test.txt";

    /**
     * Cria uma base de dados nova e popula-a (o arquivo de teste começa vazio).
     */
    public static DatabaseManager populate() throws IOException {
        Files.deleteIfExists(Path.of(FILE));
        DatabaseManager db = new DatabaseManager(FILE);
        db.insertEntity(new ExplorerRobot(10, "Explorer-A", 90, LocalDate.of(2026, 1, 15), "ACTIVE", 25));
        db.insertEntity(new ExplorerRobot(11, "Explorer-B", 90, LocalDate.of(2026, 2, 1), "ACTIVE", 30));
        db.insertEntity(new CarrierRobot(Entity.UNASSIGNED_ID, "Carrier-B", 70,
                LocalDate.of(2026, 3, 1), "CHARGING", 200));
        db.insertEntity(new Room(20, "R-01", "STORAGE", 2, 1.5, LocalDate.of(2026, 2, 10), true));
        db.insertEntity(new RadioactiveMaterial(30, "Uranio", 12.5, 80, 9, 4.2));
        db.insertEntity(new TechRelic(31, "Disco antigo", 3.2, 70, 2, "Pre-Collapse"));
        db.insertEntity(new EnergyCrystal(32, "Cristal azul", 1.5, 85, 4, 120.0));
        return db;
    }

    /**
     * Imprime o resultado de uma validação.
     */
    private static void report(String title, List<String> problems) {
        System.out.println(title + ": "
                + (problems.isEmpty() ? "OK (0 problemas)" : problems.size() + " problema(s)"));
        problems.forEach(p -> System.out.println("   - " + p));
    }

    /**
     * Depois de inserir várias entidades (incluindo duas com a mesma bateria), tem de dar 0 problemas.
     */
    public static void testValidAfterInsert(DatabaseManager db) {
        System.out.println("--- Validar após inserir ---");
        report("Validação", db.validateConsistency());
    }

    /**
     * Edições feitas via editEntity mantêm as BST consistentes.
     */
    public static void testValidAfterEdit(DatabaseManager db) {
        System.out.println("--- Validar após editar (editEntity) ---");
        db.editEntity(10, Robot.class, r -> r.setBatteryLevel(55));
        db.editEntity(32, Artifact.class, a -> a.setScientificValue(60));
        db.editEntity(20, Room.class, r -> r.setHazardIndex(7.5));
        report("Validação", db.validateConsistency());
        System.out.println("Robôs com bateria 50-60: " + db.searchRobotsByBatteryRange(50, 60));
    }

    /**
     * Remover tira a entidade da ST e da BST e arquiva-a (estrutura e ficheiro).
     */
    public static void testRemoveAndArchive(DatabaseManager db) throws IOException {
        System.out.println("--- Remover e arquivar ---");
        System.out.println("Removido: " + db.removeEntity(30));
        System.out.println("Ainda na ST? " + (db.searchEntityById(30) != null));
        boolean naBst = db.searchArtifactsByScientificValue(0, 100).stream().anyMatch(e -> e.getId() == 30);
        System.out.println("Ainda na BST? " + naBst);
        System.out.println("No arquivo (ST auxiliar)? " + (db.searchArchivedById(30) != null));
        System.out.println("Conteúdo do ficheiro:");
        Files.readAllLines(Path.of(FILE)).forEach(l -> System.out.println("   " + l));
        report("Validação", db.validateConsistency());
    }

    /**
     * Remover um ID inexistente devolve null e não arquiva nada.
     */
    public static void testRemoveMissing(DatabaseManager db) {
        System.out.println("--- Remover inexistente ---");
        System.out.println("Resultado: " + db.removeEntity(999));
        System.out.println("Arquivados: " + db.listArchived().size());
    }

    /**
     * Um ID arquivado não pode ser reutilizado.
     */
    public static void testArchivedIdNotReused(DatabaseManager db) {
        System.out.println("--- ID arquivado não é reutilizado ---");
        try {
            db.insertEntity(new Room(30, "R-X", "STORAGE", 1, 1.0, LocalDate.now(), false));
            System.out.println("ERRO: devia ter recusado");
        } catch (IllegalArgumentException ex) {
            System.out.println("Esperado: " + ex.getMessage());
        }
    }

    /**
     * Estraga de propósito (setter direto), mostra que a validação deteta e que o rebuild repara.
     */
    public static void testSabotage(DatabaseManager db) {
        System.out.println("--- Estragar de propósito (setter direto) ---");
        Robot r = (Robot) db.searchEntityById(10);
        r.setBatteryLevel(5); // não passa por editEntity: a BST fica com a chave antiga
        report("Validação após estragar", db.validateConsistency());
        db.rebuildIndexes();
        report("Validação após rebuildIndexes", db.validateConsistency());
    }

    /**
     * Executa todos os testes do R4.
     *
     * @param args não utilizado
     */
    public static void main(String[] args) throws IOException {
        DatabaseManager db = populate();
        testValidAfterInsert(db);
        testValidAfterEdit(db);
        testRemoveAndArchive(db);
        testRemoveMissing(db);
        testArchivedIdNotReused(db);
        testSabotage(db);
    }
}