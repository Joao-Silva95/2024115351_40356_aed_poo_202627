package test.R4;

import domain.*;
import repository.DatabaseManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static test.Common.TestUtil.*;

/**
 * Testes do R4: consistência entre ST e BST, e arquivo das entidades removidas.
 */
public class ConsistencyTest {

    /** Imprime os problemas encontrados pela validação. */
    private static void show(List<String> problems) {
        problems.forEach(p -> System.out.println("         . " + p));
    }

    /** Uma base de dados bem populada e nunca sabotada está consistente. */
    public static void testValidAfterInsertAndEdit() {
        section("R4 - Validar após inserir e editar");
        DatabaseManager db = freshDb("r4_valid.txt");
        populate(db);
        check("Após inserir: 0 problemas", db.validateConsistency().isEmpty());

        db.editEntity(10, Robot.class, r -> r.setBatteryLevel(55));
        db.editEntity(32, Artifact.class, a -> a.setScientificValue(60));
        db.editEntity(20, Room.class, r -> r.setHazardIndex(7.5));
        check("Após editEntity: 0 problemas", db.validateConsistency().isEmpty());
    }

    /** Remover tira a entidade das ST e BST e arquiva-a na ST auxiliar e no ficheiro. */
    public static void testRemoveAndArchive() throws IOException {
        section("R4 - Remover e arquivar");
        DatabaseManager db = freshDb("r4_archive.txt");
        populate(db);

        Entity removed = db.removeEntity(30);
        check("Removido da ST", db.searchEntityById(30) == null);
        check("Removido da BST", !ids(db.searchArtifactsByScientificValue(0, 100)).contains(30));
        check("Está na ST auxiliar de arquivo", db.searchArchivedById(30) == removed);
        check("listArchived tem 1 entidade", db.listArchived().size() == 1);

        List<String> lines = Files.readAllLines(Path.of(DIR, "r4_archive.txt"));
        check("Ficheiro tem 1 linha", lines.size() == 1);
        check("Linha começa por ARCHIVED e contém tipo e ID",
                lines.get(0).startsWith("ARCHIVED;") && lines.get(0).contains(";RadioactiveMaterial;30;Uranio;"));
        check("Validação continua a 0 problemas", db.validateConsistency().isEmpty());

        db.removeEntity(10);
        db.removeEntity(20);
        check("Três remoções => 3 linhas no ficheiro", Files.readAllLines(Path.of(DIR, "r4_archive.txt")).size() == 3);
        check("Robô e sala removidos também saem das BST",
                !ids(db.searchRobotsByBatteryRange(0, 100)).contains(10)
                        && !ids(db.searchRoomsByHazardIndex(0, 100)).contains(20));
        check("Validação continua a 0 problemas", db.validateConsistency().isEmpty());
    }

    /** Remover um ID inexistente não arquiva nada. */
    public static void testRemoveMissing() {
        section("R4 - Remover inexistente");
        DatabaseManager db = freshDb("r4_missing.txt");
        populate(db);
        check("Devolve null", db.removeEntity(999) == null);
        check("Nada foi arquivado", db.listArchived().isEmpty());
    }

    /** IDs arquivados não são reutilizados, nem manualmente nem pelo gerador automático. */
    public static void testArchivedIdNotReused() {
        section("R4 - ID arquivado não é reutilizado");
        DatabaseManager db = freshDb("r4_ids.txt");
        int first = db.insertEntity(new Room(Entity.UNASSIGNED_ID, "R-A", "STORAGE", 1, 1, LocalDate.now(), false));
        check("Primeiro ID automático é 1", first == 1);
        db.removeEntity(first);

        check("Reinserir manualmente o ID arquivado é recusado", throwsA(IllegalArgumentException.class,
                () -> db.insertEntity(new Room(1, "R-B", "STORAGE", 1, 1, LocalDate.now(), false))));
        int next = db.insertEntity(new Room(Entity.UNASSIGNED_ID, "R-C", "STORAGE", 1, 1, LocalDate.now(), false));
        check("O gerador automático salta o ID arquivado (2)", next == 2);
        check("Validação a 0 problemas", db.validateConsistency().isEmpty());
    }

    /** Se a escrita do arquivo falhar, a remoção é cancelada e nada se perde. */
    public static void testArchiveFailureCancelsRemoval() throws IOException {
        section("R4 - Falha no arquivo cancela a remoção");
        Path dirAsFile = Path.of(DIR, "pasta_em_vez_de_ficheiro");
        Files.createDirectories(dirAsFile);
        DatabaseManager db = new DatabaseManager(dirAsFile.toString());
        db.insertEntity(new Room(5, "R-F", "STORAGE", 1, 2.0, LocalDate.now(), false));

        check("removeEntity lança UncheckedIOException", throwsA(UncheckedIOException.class, () -> db.removeEntity(5)));
        check("A entidade continua ativa na ST", db.searchEntityById(5) != null);
        check("A entidade continua na BST", ids(db.searchRoomsByHazardIndex(0, 10)).contains(5));
        check("Nada ficou arquivado", db.listArchived().isEmpty());
        check("Validação a 0 problemas", db.validateConsistency().isEmpty());
    }

    /** Setters diretos estragam as BST; a validação deteta e o rebuild repara. */
    public static void testSabotageAndRebuild() {
        section("R4 - Estragar de propósito e reparar");
        DatabaseManager db = freshDb("r4_sabotage.txt");
        populate(db);

        ((Robot) db.searchEntityById(10)).setBatteryLevel(5);
        ((Artifact) db.searchEntityById(31)).setScientificValue(10);
        ((Room) db.searchEntityById(21)).setHazardIndex(0.1);
        List<String> problems = db.validateConsistency();
        show(problems);
        check("Setters diretos nos 3 tipos: 6 problemas detetados", problems.size() == 6);

        db.rebuildIndexes();
        check("Após rebuildIndexes: 0 problemas", db.validateConsistency().isEmpty());
        check("As pesquisas já refletem os valores novos", ids(db.searchRobotsByBatteryRange(0, 10)).equals(java.util.Set.of(10)));
    }

    /** Remover uma entidade com a BST desatualizada deixa um fantasma, que a validação deteta. */
    public static void testGhostAfterSabotagedRemove() {
        section("R4 - Fantasma após remover com BST desatualizada");
        DatabaseManager db = freshDb("r4_ghost.txt");
        populate(db);

        ((Robot) db.searchEntityById(10)).setBatteryLevel(5);
        db.removeEntity(10);
        List<String> problems = db.validateConsistency();
        show(problems);
        check("A validação deteta o fantasma na BST", !problems.isEmpty());

        db.rebuildIndexes();
        check("Após rebuildIndexes: 0 problemas", db.validateConsistency().isEmpty());
    }

    /** Executa os testes do R4. */
    public static void runAll() throws IOException {
        testValidAfterInsertAndEdit();
        testRemoveAndArchive();
        testRemoveMissing();
        testArchivedIdNotReused();
        testArchiveFailureCancelsRemoval();
        testSabotageAndRebuild();
        testGhostAfterSabotagedRemove();
    }

    /** @param args não utilizado */
    public static void main(String[] args) throws IOException {
        runAll();
        summary();
    }
}