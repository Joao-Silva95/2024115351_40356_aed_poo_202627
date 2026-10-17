package repository;

import domain.*;
import edu.princeton.cs.algs4.SeparateChainingHashST;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Gestor central das entidades: único ponto de acesso às estruturas de dados.
 * Mantém uma Tabela de Símbolos por tipo de entidade, todas com o ID como chave.
 */
public class DatabaseManager implements EntityHashManagement {

  /** ST dos robôs. */
  private final SeparateChainingHashST<Integer, Robot> robots = new SeparateChainingHashST<>();

  /** ST dos artefactos. */
  private final SeparateChainingHashST<Integer, Artifact> artifacts = new SeparateChainingHashST<>();

  /** ST das salas (inclui as salas de manutenção). */
  private final SeparateChainingHashST<Integer, Room> rooms = new SeparateChainingHashST<>();

  /** Próximo candidato a ID gerado automaticamente. */
  private int nextId = 1;

  @Override
  public int insertEntity(Entity e) {
    if (e == null) throw new IllegalArgumentException("Entidade nula");
    if (!(e instanceof Robot || e instanceof Artifact || e instanceof Room))
      throw new IllegalArgumentException("Tipo desconhecido: " + e.getClass().getSimpleName());

    if (!e.hasId()) e.assignId(generateId());
    else if (existsId(e.getId()))
      throw new IllegalArgumentException("ID já existe: " + e.getId());

    if (e instanceof Robot) robots.put(e.getId(), (Robot) e);
    else if (e instanceof Artifact) artifacts.put(e.getId(), (Artifact) e);
    else rooms.put(e.getId(), (Room) e);

    index(e);
    return e.getId();
  }

  @Override
  public Entity removeEntity(int id) {
    Entity e = searchEntityById(id);
    if (e == null) return null;

    unindex(e);
    if (e instanceof Robot) robots.delete(id);
    else if (e instanceof Artifact) artifacts.delete(id);
    else rooms.delete(id);

    archive(e);
    return e;
  }

  @Override
  public <T extends Entity> void editEntity(int id, Class<T> type, Consumer<T> changes) {
    Entity e = searchEntityById(id);
    if (e == null) throw new NoSuchElementException("ID não existe: " + id);
    if (!type.isInstance(e))
      throw new IllegalArgumentException("A entidade " + id + " não é " + type.getSimpleName());

    unindex(e);                     // tira das estruturas ordenadas (R3)
    try {
      changes.accept(type.cast(e)); // altera o objeto partilhado
    } finally {
      index(e);                     // volta a pôr, já com a chave nova
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
  public List<Robot> listRobots() { return values(robots); }

  @Override
  public List<Artifact> listArtifacts() { return values(artifacts); }

  @Override
  public List<Room> listRooms() { return values(rooms); }

  /** @return true se o ID já está em uso em qualquer tipo */
  private boolean existsId(int id) {
    return robots.contains(id) || artifacts.contains(id) || rooms.contains(id);
  }

  /** @return um ID ainda não usado (salta os que o utilizador já escolheu) */
  private int generateId() {
    while (existsId(nextId)) nextId++;
    return nextId++;
  }

  /** Copia os valores de uma ST para uma lista. */
  private static <T> List<T> values(SeparateChainingHashST<Integer, T> st) {
    List<T> list = new ArrayList<>();
    for (int key : st.keys()) list.add(st.get(key));
    return list;
  }

  /** Regista a entidade nas estruturas ordenadas. Por preencher no R3 (Red-Black BST). */
  private void index(Entity e) { }

  /** Remove a entidade das estruturas ordenadas. Por preencher no R3. */
  private void unindex(Entity e) { }

  /** Arquiva a entidade removida. Por preencher no R4 (ficheiro ou estrutura auxiliar). */
  private void archive(Entity e) { }
}