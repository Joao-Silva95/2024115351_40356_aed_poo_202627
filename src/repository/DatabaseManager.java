package repository;

import domain.*;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.princeton.cs.algs4.SeparateChainingHashST;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Gestor central das entidades: único ponto de acesso às estruturas de dados.
 * Mantém uma Tabela de Símbolos por tipo de entidade, todas com o ID como chave.
 */
public class DatabaseManager implements EntityHashManagement, EntityBSTManagement, EntityConsistencyManagement {
    /**
     * Ficheiro de arquivo por defeito.
     */
    public static final String DEFAULT_ARCHIVE_FILE = "archive.txt";

    /**
     * ST dos robôs.
     */
    private final SeparateChainingHashST<Integer, Robot> robots = new SeparateChainingHashST<>();

    /**
     * ST dos artefactos.
     */
    private final SeparateChainingHashST<Integer, Artifact> artifacts = new SeparateChainingHashST<>();

    /**
     * ST das salas (inclui as salas de manutenção).
     */
    private final SeparateChainingHashST<Integer, Room> rooms = new SeparateChainingHashST<>();

    /**
     * ST auxiliar com as entidades arquivadas (removidas da base de dados ativa).
     */
    private final SeparateChainingHashST<Integer, Entity> archived = new SeparateChainingHashST<>();

    /**
     * BST dos robôs, ordenada pela bateria.
     */
    private RedBlackBST<Double, List<Robot>> robotBatteryIndex = new RedBlackBST<>();

    /**
     * BST dos artefactos, ordenada pelo valor científico.
     */
    private RedBlackBST<Double, List<Artifact>> artifactValueIndex = new RedBlackBST<>();

    /**
     * BST das salas, ordenada pelo índice de perigo ambiental.
     */
    private RedBlackBST<Double, List<Room>> roomHazardIndex = new RedBlackBST<>();

    /**
     * Ficheiro de texto onde as entidades removidas são arquivadas.
     */
    private final Path archiveFile;

    /**
     * Próximo candidato a ID gerado automaticamente.
     */
    private int nextId = 1;
    /**
     * Cria um gestor que arquiva em {@link #DEFAULT_ARCHIVE_FILE}.
     */
    public DatabaseManager() {
        this(DEFAULT_ARCHIVE_FILE);
    }

    /**
     * @param archivePath caminho do ficheiro de arquivo
     */
    public DatabaseManager(String archivePath) {
        this.archiveFile = Paths.get(archivePath);
    }

    //========================================R2: GESTÃO NAS TABELAS DE SÍMBOLOS========================================
    @Override
    public int insertEntity(Entity e) {
        if (e == null) throw new IllegalArgumentException("Entidade nula");
        if (!(e instanceof Robot || e instanceof Artifact || e instanceof Room))
            throw new IllegalArgumentException("Tipo desconhecido: " + e.getClass().getSimpleName());

        if (!e.hasId()) e.assignId(generateId());
        else if (existsId(e.getId()))
            throw new IllegalArgumentException("ID já existe (ativo ou arquivado): " + e.getId());

        if (e instanceof Robot) robots.put(e.getId(), (Robot) e);
        else if (e instanceof Artifact) artifacts.put(e.getId(), (Artifact) e);
        else rooms.put(e.getId(), (Room) e);

        index(e);
        return e.getId();
    }

    /**
     * Remove a entidade das estruturas ativas e arquiva-a (R4).
     * Arquiva primeiro: se a escrita no ficheiro falhar, a remoção é cancelada.
     *
     * @throws UncheckedIOException se não for possível escrever no ficheiro de arquivo
     */
    @Override
    public Entity removeEntity(int id) {
        Entity e = searchEntityById(id);
        if (e == null) return null;

        archive(e);       // 1.º: se falhar, nada foi apagado
        unindex(e);       // 2.º: BST
        if (e instanceof Robot) robots.delete(id);
        else if (e instanceof Artifact) artifacts.delete(id);
        else rooms.delete(id);
        return e;
    }

    @Override
    public <T extends Entity> void editEntity(int id, Class<T> type, Consumer<T> changes) {
        Entity e = searchEntityById(id);
        if (e == null) throw new NoSuchElementException("ID não existe: " + id);
        if (!type.isInstance(e))
            throw new IllegalArgumentException("A entidade " + id + " não é " + type.getSimpleName());

        unindex(e);                       // tira das BST com as chaves antigas
        try {
            changes.accept(type.cast(e)); // altera o objeto partilhado
        } finally {
            index(e);                     // volta a pôr com as chaves novas
        }
    }

    @Override
    public Entity searchEntityById(int id) {
        if (robots.contains(id)) return robots.get(id);
        if (artifacts.contains(id)) return artifacts.get(id);
        return rooms.get(id); // null se não existir
    }

    @Override
    public List<Entity> listAllEntities() {
        List<Entity> all = new ArrayList<>();
        all.addAll(listRobots());
        all.addAll(listArtifacts());
        all.addAll(listRooms());
        return all;
    }

    @Override
    public List<Robot> listRobots() {
        return values(robots);
    }

    @Override
    public List<Artifact> listArtifacts() {
        return values(artifacts);
    }

    @Override
    public List<Room> listRooms() {
        return values(rooms);
    }

    /**
     * @return true se o ID está em uso numa ST ativa
     */
    private boolean existsActive(int id) {
        return robots.contains(id) || artifacts.contains(id) || rooms.contains(id);
    }

    /**
     * @return true se o ID já está em uso em qualquer tipo
     */
    private boolean existsId(int id) {
        return existsActive(id) || archived.contains(id);
    }

    /**
     * @return um ID ainda não usado (salta os que o utilizador já escolheu)
     */
    private int generateId() {
        while (existsId(nextId)) nextId++;
        return nextId++;
    }

    /**
     * Copia os valores de uma ST para uma lista.
     */
    private static <T> List<T> values(SeparateChainingHashST<Integer, T> st) {
        List<T> list = new ArrayList<>();
        for (int key : st.keys()) list.add(st.get(key));
        return list;
    }

    //==================================================ÍNDICES (BST)==================================================

    /**
     * Regista a entidade nas estruturas ordenadas RedBlackBST.
     */
    private void index(Entity e) {
        if (e instanceof Robot r) {
            double b = r.getBatteryLevel();
            if (!robotBatteryIndex.contains(b)) robotBatteryIndex.put(b, new ArrayList<>());
            robotBatteryIndex.get(b).add(r);
        } else if (e instanceof Artifact a) {
            double val = a.getScientificValue();
            if (!artifactValueIndex.contains(val)) artifactValueIndex.put(val, new ArrayList<>());
            artifactValueIndex.get(val).add(a);
        } else if (e instanceof Room rm) {
            double hazard = rm.getEnvironmentalHazardIndex();
            if (!roomHazardIndex.contains(hazard)) roomHazardIndex.put(hazard, new ArrayList<>());
            roomHazardIndex.get(hazard).add(rm);
        }
    }

    /**
     * Remove a entidade das BST do seu tipo, usando os valores atuais dos atributos.
     */
    private void unindex(Entity e) {
        if (e instanceof Robot r) {
            double b = r.getBatteryLevel();
            if (robotBatteryIndex.contains(b)) {
                List<Robot> list = robotBatteryIndex.get(b);
                list.remove(r);
                if (list.isEmpty()) robotBatteryIndex.delete(b);
            }
        } else if (e instanceof Artifact a) {
            double val = a.getScientificValue();
            if (artifactValueIndex.contains(val)) {
                List<Artifact> list = artifactValueIndex.get(val);
                list.remove(a);
                if (list.isEmpty()) artifactValueIndex.delete(val);
            }
        } else if (e instanceof Room rm) {
            double hazard = rm.getEnvironmentalHazardIndex();
            if (roomHazardIndex.contains(hazard)) {
                List<Room> list = roomHazardIndex.get(hazard);
                list.remove(rm);
                if (list.isEmpty()) roomHazardIndex.delete(hazard);
            }
        }
    }

    //===================================================R4: ARQUIVO===================================================
    /**
     * Arquiva a entidade: acrescenta uma linha ao ficheiro e guarda-a na ST auxiliar.
     * Formato da linha: ARCHIVED;data-hora;tipo;id;nome;atributos...
     *
     * @throws UncheckedIOException se a escrita falhar (a ST auxiliar só é atualizada se a escrita correr bem)
     */
    private void archive(Entity e) {
        String line = "ARCHIVED;" + LocalDateTime.now() + ";" + e.toRecord() + System.lineSeparator();
        try {
            Path parent = archiveFile.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.writeString(archiveFile, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            throw new UncheckedIOException("Falha ao arquivar a entidade " + e.getId(), ex);
        }
        archived.put(e.getId(), e);
    }

    @Override
    public List<Entity> listArchived() {
        return values(archived);
    }

    @Override
    public Entity searchArchivedById(int id) {
        return archived.get(id);
    }

    //==========================================R4: VALIDAÇÃO DE CONSISTÊNCIA==========================================

    @Override
    public List<String> validateConsistency() {
        List<String> problems = new ArrayList<>();
        checkIds(problems);
        checkIndex("Robô", robots, robotBatteryIndex, Robot::getBatteryLevel, problems);
        checkIndex("Artefacto", artifacts, artifactValueIndex,
                a -> (double) a.getScientificValue(), problems);
        checkIndex("Sala", rooms, roomHazardIndex, Room::getEnvironmentalHazardIndex, problems);
        return problems;
    }

    /**
     * Verifica que a chave de cada ST coincide com o ID da entidade, que nenhum ID está em
     * duas ST e que nenhuma entidade arquivada continua ativa.
     */
    private void checkIds(List<String> problems) {
        for (int id : robots.keys()) {
            if (robots.get(id).getId() != id)
                problems.add("Robô guardado com a chave " + id + " tem o ID " + robots.get(id).getId());
            if (artifacts.contains(id) || rooms.contains(id))
                problems.add("ID " + id + " existe em mais do que uma ST");
        }
        for (int id : artifacts.keys()) {
            if (artifacts.get(id).getId() != id)
                problems.add("Artefacto guardado com a chave " + id + " tem o ID " + artifacts.get(id).getId());
            if (rooms.contains(id))
                problems.add("ID " + id + " existe em mais do que uma ST");
        }
        for (int id : rooms.keys()) {
            if (rooms.get(id).getId() != id)
                problems.add("Sala guardada com a chave " + id + " tem o ID " + rooms.get(id).getId());
        }
        for (int id : archived.keys()) {
            if (existsActive(id))
                problems.add("O ID arquivado " + id + " também está ativo");
        }
    }

    /**
     * Compara uma ST com a BST correspondente, nos dois sentidos.
     *
     * @param label  nome do tipo, para as mensagens
     * @param st     ST do tipo
     * @param bst    BST do tipo
     * @param keyOf  função que dá a chave atual da entidade na BST
     * @param problems lista onde são acrescentados os problemas
     */
    private <T extends Entity> void checkIndex(String label, SeparateChainingHashST<Integer, T> st,
                                               RedBlackBST<Double, List<T>> bst,
                                               Function<T, Double> keyOf, List<String> problems) {
        // ST -> BST: cada entidade está uma vez, na chave atual, como a mesma referência
        for (T e : values(st)) {
            double key = keyOf.apply(e);
            List<T> list = bst.contains(key) ? bst.get(key) : null;
            int count = 0;
            if (list != null) for (T x : list) if (x == e) count++;
            if (count == 0)
                problems.add(label + " " + e.getId() + " não está na BST com a chave atual " + key);
            else if (count > 1)
                problems.add(label + " " + e.getId() + " aparece " + count + " vezes na BST");
        }

        // BST -> ST: sem fantasmas, sem cópias, sem chaves desatualizadas, sem listas vazias
        int inBst = 0;
        for (double key : bst.keys()) {
            List<T> list = bst.get(key);
            if (list.isEmpty()) problems.add(label + ": a BST tem uma lista vazia na chave " + key);
            for (T x : list) {
                inBst++;
                if (Double.compare(keyOf.apply(x), key) != 0)
                    problems.add(label + " " + x.getId() + " está na BST com a chave " + key
                            + " mas o valor atual é " + keyOf.apply(x));
                if (st.get(x.getId()) != x)
                    problems.add(label + " " + x.getId() + " está na BST mas não é o mesmo objeto da ST"
                            + " (fantasma ou cópia)");
            }
        }
        if (inBst != st.size())
            problems.add(label + ": a ST tem " + st.size() + " entidades e a BST tem " + inBst);
    }

    @Override
    public void rebuildIndexes() {
        robotBatteryIndex = new RedBlackBST<>();
        artifactValueIndex = new RedBlackBST<>();
        roomHazardIndex = new RedBlackBST<>();
        for (Entity e : listAllEntities()) index(e);
    }

    // =========================================================
    // IMPLEMENTAÇÃO DOS MÉTODOS DE EntityBSTManagement (R3)
    // =========================================================

    @Override
    public List<Entity> searchRobotsByBatteryRange(double minBattery, double maxBattery) {
        List<Entity> result = new ArrayList<>();
        // Range query nativa da RedBlackBST da algs4: O(log N + M)
        Iterable<Double> keys = robotBatteryIndex.keys(minBattery, maxBattery);
        for (Double key : keys) {
            result.addAll(robotBatteryIndex.get(key));
        }
        return result;
    }

    @Override
    public List<Entity> searchArtifactsByScientificValue(double minVal, double maxVal) {
        List<Entity> result = new ArrayList<>();
        Iterable<Double> keys = artifactValueIndex.keys(minVal, maxVal);
        for (Double key : keys) {
            result.addAll(artifactValueIndex.get(key));
        }
        return result;
    }

    @Override
    public List<Entity> searchRoomsByHazardIndex(double minHazard, double maxHazard) {
        List<Entity> result = new ArrayList<>();
        Iterable<Double> keys = roomHazardIndex.keys(minHazard, maxHazard);
        for (Double key : keys) {
            result.addAll(roomHazardIndex.get(key));
        }
        return result;
    }

    @Override
    public List<Entity> searchBySubstring(String query) {
        List<Entity> matches = new ArrayList<>();
        if (query == null || query.isBlank()) return matches;
        String q = query.toLowerCase();

        for (Entity e : listAllEntities()) {
            if (e instanceof Robot r) {
                if (r.getName().toLowerCase().contains(q) || String.valueOf(r.getId()).contains(q)) {
                    matches.add(r);
                }
            } else if (e instanceof Artifact a) {
                if (a.getDescription().toLowerCase().contains(q) || String.valueOf(a.getId()).contains(q)) {
                    matches.add(a);
                }
            } else if (e instanceof Room rm) {
                if (rm.getRoomCode().toLowerCase().contains(q) || String.valueOf(rm.getId()).contains(q)) {
                    matches.add(rm);
                }
            }
        }
        return matches;
    }
}