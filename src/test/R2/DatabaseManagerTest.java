package test.R2;

import domain.*;
import repository.DatabaseManager;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static test.Common.TestUtil.*;

/**
 * Testes do R2: inserir, remover, editar e listar nas Tabelas de Símbolos.
 */
public class DatabaseManagerTest {

    /** Inserção com ID do utilizador, ID automático, duplicados e dados inválidos. */
    public static void testInsert() {
        section("R2 - Inserir");
        DatabaseManager db = freshDb("r2_insert.txt");

        int id = db.insertEntity(new ExplorerRobot(10, "Explorer-A", 90, LocalDate.of(2026, 1, 15), "ACTIVE", 25));
        check("ID escolhido pelo utilizador é mantido", id == 10);

        int auto = db.insertEntity(new CarrierRobot(Entity.UNASSIGNED_ID, "Carrier-B", 70,
                LocalDate.of(2026, 3, 1), "CHARGING", 200));
        check("ID automático gerado (1)", auto == 1);

        db.insertEntity(new Room(2, "R-01", "STORAGE", 2, 1.5, LocalDate.of(2026, 2, 10), true));
        int auto2 = db.insertEntity(new TechRelic(Entity.UNASSIGNED_ID, "Disco", 3.2, 70, 2, "Pre-Collapse"));
        check("ID automático salta os IDs já escolhidos (3)", auto2 == 3);

        check("ID repetido no mesmo tipo é recusado", throwsA(IllegalArgumentException.class,
                () -> db.insertEntity(new ExplorerRobot(10, "Outro", 50, LocalDate.now(), "ACTIVE", 1))));
        check("ID repetido noutro tipo é recusado", throwsA(IllegalArgumentException.class,
                () -> db.insertEntity(new Room(10, "R-X", "STORAGE", 1, 1, LocalDate.now(), false))));
        check("Entidade nula é recusada", throwsA(IllegalArgumentException.class, () -> db.insertEntity(null)));
        check("Nome vazio é recusado", throwsA(IllegalArgumentException.class,
                () -> new ExplorerRobot(50, "", 50, LocalDate.now(), "ACTIVE", 1)));
        check("Bateria fora de 0-100 é recusada", throwsA(IllegalArgumentException.class,
                () -> new ExplorerRobot(51, "X", 150, LocalDate.now(), "ACTIVE", 1)));
        check("Total de entidades = 4", db.listAllEntities().size() == 4);
    }

    /** Pesquisa por ID e listagens por tipo. */
    public static void testSearchAndList() {
        section("R2 - Pesquisar por ID e listar");
        DatabaseManager db = freshDb("r2_list.txt");
        populate(db);

        check("Pesquisa por ID devolve a entidade certa", db.searchEntityById(12) instanceof CarrierRobot);
        check("Pesquisa de ID inexistente devolve null", db.searchEntityById(999) == null);
        check("4 robôs", db.listRobots().size() == 4);
        check("4 artefactos", db.listArtifacts().size() == 4);
        check("3 salas", db.listRooms().size() == 3);
        check("11 entidades no total", db.listAllEntities().size() == 11);
    }

    /** Edição de atributos, tipo errado, ID inexistente e valor inválido. */
    public static void testEdit() {
        section("R2 - Editar");
        DatabaseManager db = freshDb("r2_edit.txt");
        populate(db);

        db.editEntity(10, Robot.class, r -> {
            r.setBatteryLevel(55);
            r.setOperationalState("RETURNING");
        });
        Robot r10 = (Robot) db.searchEntityById(10);
        check("Bateria alterada para 55", r10.getBatteryLevel() == 55);
        check("Estado alterado para RETURNING", r10.getOperationalState().equals("RETURNING"));

        db.editEntity(20, Room.class, r -> r.setMaintenanceRoom(false));
        check("Sala deixa de ser sala de manutenção", !((Room) db.searchEntityById(20)).isMaintenanceRoom());

        db.editEntity(31, TechRelic.class, t -> t.setHistoricalEra("Post-Collapse"));
        check("Atributo de subclasse alterado", ((TechRelic) db.searchEntityById(31)).getHistoricalEra()
                .equals("Post-Collapse"));

        check("Editar ID inexistente lança NoSuchElementException", throwsA(NoSuchElementException.class,
                () -> db.editEntity(999, Robot.class, r -> r.setBatteryLevel(1))));
        check("Editar com tipo errado lança IllegalArgumentException", throwsA(IllegalArgumentException.class,
                () -> db.editEntity(20, Robot.class, r -> r.setBatteryLevel(1))));

        check("Valor inválido na edição lança exceção", throwsA(IllegalArgumentException.class,
                () -> db.editEntity(11, Robot.class, r -> r.setBatteryLevel(200))));
        check("Após a exceção, o valor antigo mantém-se", ((Robot) db.searchEntityById(11)).getBatteryLevel() == 90);
        check("Após a exceção, as BST continuam consistentes", db.validateConsistency().isEmpty());
    }

    /** Remoção de entidade existente e inexistente. */
    public static void testRemove() {
        section("R2 - Remover");
        DatabaseManager db = freshDb("r2_remove.txt");
        populate(db);

        Entity removed = db.removeEntity(30);
        check("Remover devolve a entidade removida", removed != null && removed.getId() == 30);
        check("A entidade já não é encontrada", db.searchEntityById(30) == null);
        check("Remover de novo devolve null", db.removeEntity(30) == null);
        check("Total desce para 10", db.listAllEntities().size() == 10);
    }

    /** Executa os testes do R2. */
    public static void runAll() {
        testInsert();
        testSearchAndList();
        testEdit();
        testRemove();
    }

    /** @param args não utilizado */
    public static void main(String[] args) {
        runAll();
        summary();
    }
}