package test.R3;

import domain.*;
import repository.DatabaseManager;

import java.util.Set;

import static test.Common.TestUtil.*;

/**
 * Testes do R3: pesquisas por intervalo nas Red-Black BST e pesquisa por substring.
 */
public class SearchTest {

    /** Pesquisa de robôs por intervalo de bateria. */
    public static void testBatteryRange() {
        section("R3 - Robôs por intervalo de bateria");
        DatabaseManager db = freshDb("r3_battery.txt");
        populate(db);

        check("80-100 devolve os robôs 10 e 11 (mesma chave 90)", ids(db.searchRobotsByBatteryRange(80, 100)).equals(Set.of(10, 11)));
        check("60-80 devolve o robô 12", ids(db.searchRobotsByBatteryRange(60, 80)).equals(Set.of(12)));
        check("Limites são inclusivos (90-90)", ids(db.searchRobotsByBatteryRange(90, 90)).equals(Set.of(10, 11)));
        check("0-100 devolve os 4 robôs", db.searchRobotsByBatteryRange(0, 100).size() == 4);
        check("Intervalo sem robôs devolve lista vazia", db.searchRobotsByBatteryRange(30, 50).isEmpty());
    }

    /** Pesquisa de artefactos por intervalo de valor científico. */
    public static void testScientificValueRange() {
        section("R3 - Artefactos por valor científico");
        DatabaseManager db = freshDb("r3_science.txt");
        populate(db);

        check("80-100 devolve 30, 32 e 33", ids(db.searchArtifactsByScientificValue(80, 100)).equals(Set.of(30, 32, 33)));
        check("70-70 devolve só o 31", ids(db.searchArtifactsByScientificValue(70, 70)).equals(Set.of(31)));
        check("85-85 devolve os dois cristais", ids(db.searchArtifactsByScientificValue(85, 85)).equals(Set.of(32, 33)));
        check("Intervalo sem artefactos devolve lista vazia", db.searchArtifactsByScientificValue(0, 50).isEmpty());
    }

    /** Pesquisa de salas por intervalo de índice de perigo. */
    public static void testHazardRange() {
        section("R3 - Salas por índice de perigo");
        DatabaseManager db = freshDb("r3_hazard.txt");
        populate(db);

        check("0-2 devolve a sala 20", ids(db.searchRoomsByHazardIndex(0, 2)).equals(Set.of(20)));
        check("5-9 devolve 21 e 22 (limites inclusivos)", ids(db.searchRoomsByHazardIndex(5, 9)).equals(Set.of(21, 22)));
        check("0-10 devolve as 3 salas", db.searchRoomsByHazardIndex(0, 10).size() == 3);
        check("Intervalo sem salas devolve lista vazia", db.searchRoomsByHazardIndex(2, 4).isEmpty());
    }

    /** Pesquisa por substring em robôs, artefactos e salas. */
    public static void testSubstring() {
        section("R3 - Pesquisa por substring");
        DatabaseManager db = freshDb("r3_substring.txt");
        populate(db);

        check("\"explorer\" encontra os robôs 10 e 11", ids(db.searchBySubstring("explorer")).equals(Set.of(10, 11)));
        check("Ignora maiúsculas/minúsculas (\"EXPLORER\")", ids(db.searchBySubstring("EXPLORER")).equals(Set.of(10, 11)));
        check("\"cristal\" encontra 32 e 33 (descrição)", ids(db.searchBySubstring("cristal")).equals(Set.of(32, 33)));
        check("\"R-0\" encontra as 3 salas (código)", ids(db.searchBySubstring("R-0")).equals(Set.of(20, 21, 22)));
        check("\"30\" encontra o artefacto 30 (por ID)", ids(db.searchBySubstring("30")).equals(Set.of(30)));
        check("Sem correspondência devolve lista vazia", db.searchBySubstring("xyz").isEmpty());
        check("Texto nulo devolve lista vazia", db.searchBySubstring(null).isEmpty());
        check("Texto em branco devolve lista vazia", db.searchBySubstring("  ").isEmpty());
    }

    /** As pesquisas acompanham edições e remoções. */
    public static void testSearchAfterEditAndRemove() {
        section("R3 - Pesquisas após editar e remover");
        DatabaseManager db = freshDb("r3_after.txt");
        populate(db);

        db.editEntity(10, Robot.class, r -> r.setBatteryLevel(15));
        check("Robô 10 deixou o intervalo 80-100", ids(db.searchRobotsByBatteryRange(80, 100)).equals(Set.of(11)));
        check("Robô 10 aparece no intervalo 10-20 (com o 13)", ids(db.searchRobotsByBatteryRange(10, 20)).equals(Set.of(10, 13)));

        db.editEntity(32, Artifact.class, a -> a.setScientificValue(60));
        check("Artefacto 32 aparece em 60-60", ids(db.searchArtifactsByScientificValue(60, 60)).equals(Set.of(32)));

        db.editEntity(21, Room.class, r -> r.setHazardIndex(0.5));
        check("Sala 21 aparece em 0-1", ids(db.searchRoomsByHazardIndex(0, 1)).equals(Set.of(21)));

        db.removeEntity(11);
        check("Robô removido deixa de aparecer na pesquisa por bateria",
                !ids(db.searchRobotsByBatteryRange(0, 100)).contains(11));
        check("Robô removido deixa de aparecer na pesquisa por substring", db.searchBySubstring("explorer-b").isEmpty());
    }

    /** Pesquisas numa base de dados vazia. */
    public static void testEmptyDatabase() {
        section("R3 - Base de dados vazia");
        DatabaseManager db = freshDb("r3_empty.txt");

        check("Bateria devolve vazio", db.searchRobotsByBatteryRange(0, 100).isEmpty());
        check("Valor científico devolve vazio", db.searchArtifactsByScientificValue(0, 100).isEmpty());
        check("Índice de perigo devolve vazio", db.searchRoomsByHazardIndex(0, 100).isEmpty());
        check("Substring devolve vazio", db.searchBySubstring("a").isEmpty());
    }

    /** Executa os testes do R3. */
    public static void runAll() {
        testBatteryRange();
        testScientificValueRange();
        testHazardRange();
        testSubstring();
        testSearchAfterEditAndRemove();
        testEmptyDatabase();
    }

    /** @param args não utilizado */
    public static void main(String[] args) {
        runAll();
        summary();
    }
}