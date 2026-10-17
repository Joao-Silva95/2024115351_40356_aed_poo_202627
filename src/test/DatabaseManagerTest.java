package test;

import domain.*;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import repository.DatabaseManager;

/**
 * Testes do R2: inserir, remover, editar e listar nas Tabelas de Símbolos.
 */
public class DatabaseManagerTest {

    /** Insere com ID do utilizador e com ID gerado, e testa ID repetido. */
    public static void testInsert(DatabaseManager db) {
        System.out.println("--- Inserir ---");
        db.insertEntity(new ExplorerRobot(10, "Explorer-A", 90, LocalDate.of(2026, 1, 15), "ACTIVE", 25));
        int auto = db.insertEntity(new CarrierRobot(Entity.UNASSIGNED_ID, "Carrier-B", 70,
                LocalDate.of(2026, 3, 1), "CHARGING", 200));
        System.out.println("ID gerado: " + auto);
        db.insertEntity(new Room(20, "R-01", "STORAGE", 2, 1.5, LocalDate.of(2026, 2, 10), true));
        db.insertEntity(new RadioactiveMaterial(30, "Uranio", 12.5, 80, 9, 4.2));
        db.insertEntity(new TechRelic(31, "Disco antigo", 3.2, 70, 2, "Pre-Collapse"));
        db.insertEntity(new EnergyCrystal(32, "Cristal azul", 1.5, 85, 4, 120.0));
        try {
            db.insertEntity(new Room(10, "R-02", "REACTOR", 5, 9.0, LocalDate.now(), false));
        } catch (IllegalArgumentException ex) {
            System.out.println("Esperado (ID repetido): " + ex.getMessage());
        }
    }

    /** Edita atributos e confirma a alteração ao pesquisar de novo. */
    public static void testEdit(DatabaseManager db) {
        System.out.println("--- Editar ---");
        db.editEntity(10, Robot.class, r -> {
            r.setBatteryLevel(55);
            r.setOperationalState("RETURNING");
        });
        System.out.println(db.searchEntityById(10));

        db.editEntity(20, Room.class, r -> r.setMaintenanceRoom(false));
        System.out.println(db.searchEntityById(20));

        try {
            db.editEntity(999, Robot.class, r -> r.setBatteryLevel(1));
        } catch (NoSuchElementException ex) {
            System.out.println("Esperado (ID inexistente): " + ex.getMessage());
        }
        try {
            db.editEntity(20, Robot.class, r -> r.setBatteryLevel(1));
        } catch (IllegalArgumentException ex) {
            System.out.println("Esperado (tipo errado): " + ex.getMessage());
        }
    }

    /** Remove uma entidade existente e uma inexistente. */
    public static void testRemove(DatabaseManager db) {
        System.out.println("--- Remover ---");
        System.out.println("Removido: " + db.removeEntity(30));
        System.out.println("Removido (inexistente): " + db.removeEntity(30));
    }

    /** Lista tudo por tipo. */
    public static void testList(DatabaseManager db) {
        System.out.println("--- Listar ---");
        db.listRobots().forEach(System.out::println);
        db.listArtifacts().forEach(System.out::println);
        db.listRooms().forEach(System.out::println);
        System.out.println("Total: " + db.listAllEntities().size());
    }

    /**
     * Executa todos os testes do R2.
     * @param args não utilizado
     */
    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        testInsert(db);
        testEdit(db);
        testRemove(db);
        testList(db);
    }
}