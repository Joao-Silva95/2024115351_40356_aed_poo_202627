package repository;

import domain.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * API de gestão das entidades em Tabelas de Símbolos (hash), com o ID como chave
 */
public interface EntityHashManagement {

  /**
   * Insere uma entidade. Se não tiver ID, o sistema gera um
   * @param e entidade a inserir
   * @return o ID da entidade inserida
   * @throws IllegalArgumentException se for nula, de tipo desconhecido ou o ID já existir
   */
  int insertEntity(Entity e);

  /**
   * Remove a entidade de todas as estruturas
   * @param id ID da entidade
   * @return a entidade removida, ou null se não existia
   */
  Entity removeEntity(int id);

  /**
   * Edita os atributos da entidade guardada; a alteração vale em todas as estruturas
   * @param <T> tipo esperado da entidade
   * @param id ID da entidade
   * @param type classe esperada (ex.: Robot.class)
   * @param changes alterações a aplicar (normalmente chamadas a setters)
   * @throws java.util.NoSuchElementException se o ID não existir
   * @throws IllegalArgumentException se a entidade não for do tipo indicado
   */
  <T extends Entity> void editEntity(int id, Class<T> type, Consumer<T> changes);

  /**
   * @param id ID a procurar
   * @return a entidade, ou null se não existir
   */
  Entity searchEntityById(int id);

  /** @return todas as entidades, de todos os tipos */
  List<Entity> listAllEntities();

  /** @return todos os robôs */
  List<Robot> listRobots();

  /** @return todos os artefactos */
  List<Artifact> listArtifacts();

  /** @return todas as salas */
  List<Room> listRooms();
}