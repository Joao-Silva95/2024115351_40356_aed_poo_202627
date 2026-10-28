package test.Common;

import domain.*;
import repository.DatabaseManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Funções de apoio aos testes: verificações, contadores e dados de teste.
 */
public final class TestUtil {

    /** Pasta onde os testes escrevem os ficheiros de arquivo. */
    public static final String DIR = "test_output";

    private static int passed = 0;
    private static int failed = 0;

    private TestUtil() {
    }

    /** Imprime o título de uma secção de testes. */
    public static void section(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
    }

    /** Regista e imprime o resultado de uma verificação. */
    public static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + description);
        } else {
            failed++;
            System.out.println("  [FAIL] " + description);
        }
    }

    /** @return true se o código lançar uma exceção do tipo indicado */
    public static boolean throwsA(Class<? extends Throwable> type, Runnable code) {
        try {
            code.run();
            return false;
        } catch (Throwable t) {
            return type.isInstance(t);
        }
    }

    /** @return os IDs das entidades da lista, sem repetições */
    public static Set<Integer> ids(List<? extends Entity> list) {
        return list.stream().map(Entity::getId).collect(Collectors.toSet());
    }

    /** Imprime o total de PASS e FAIL. */
    public static void summary() {
        System.out.println();
        System.out.println("=== RESULTADO: " + passed + " PASS, " + failed + " FAIL ===");
    }

    /** @return base de dados nova, a arquivar num ficheiro de teste que começa vazio */
    public static DatabaseManager freshDb(String archiveName) {
        try {
            Files.createDirectories(Path.of(DIR));
            Path file = Path.of(DIR, archiveName);
            Files.deleteIfExists(file);
            return new DatabaseManager(file.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Popula a base de dados com 4 robôs, 4 artefactos e 3 salas (IDs escolhidos pelo utilizador).
     * Há valores repetidos de propósito (bateria 90, valor científico 85) para testar chaves iguais na BST.
     */
    public static void populate(DatabaseManager db) {
        db.insertEntity(new ExplorerRobot(10, "Explorer-A", 90, LocalDate.of(2026, 1, 15), "ACTIVE", 25));
        db.insertEntity(new ExplorerRobot(11, "Explorer-B", 90, LocalDate.of(2026, 2, 1), "ACTIVE", 30));
        db.insertEntity(new CarrierRobot(12, "Carrier-C", 70, LocalDate.of(2026, 3, 1), "CHARGING", 200));
        db.insertEntity(new CarrierRobot(13, "Carrier-D", 20, LocalDate.of(2026, 3, 5), "IDLE", 150));

        db.insertEntity(new RadioactiveMaterial(30, "Uranio", 12.5, 80, 9, 4.2));
        db.insertEntity(new TechRelic(31, "Disco antigo", 3.2, 70, 2, "Pre-Collapse"));
        db.insertEntity(new EnergyCrystal(32, "Cristal azul", 1.5, 85, 4, 120.0));
        db.insertEntity(new EnergyCrystal(33, "Cristal verde", 2.0, 85, 3, 90.0));

        db.insertEntity(new Room(20, "R-01", "STORAGE", 2, 1.5, LocalDate.of(2026, 2, 10), true));
        db.insertEntity(new Room(21, "R-02", "REACTOR", 5, 9.0, LocalDate.of(2026, 2, 12), false));
        db.insertEntity(new Room(22, "R-03", "CORRIDOR", 1, 5.0, LocalDate.of(2026, 2, 15), false));
    }
}