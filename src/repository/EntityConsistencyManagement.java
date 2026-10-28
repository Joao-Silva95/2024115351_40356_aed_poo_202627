package repository;

import domain.Entity;

import java.util.List;

/**
 * API de consistência e arquivo (R4).
 */
public interface EntityConsistencyManagement {

    /**
     * Verifica as invariantes entre as ST e as BST:
     * cada entidade está uma só vez na BST do seu tipo, com a chave atual e como a mesma
     * referência da ST; não há entradas fantasma nem listas vazias; os totais coincidem;
     * nenhum ID está em duas ST; nenhuma entidade arquivada está ativa.
     *
     * @return lista de problemas encontrados (vazia se tudo estiver consistente)
     */
    List<String> validateConsistency();

    /**
     * Reconstrói todas as BST a partir das ST. Serve para reparar as BST depois de
     * alterações feitas diretamente nos setters, sem passar por editEntity.
     */
    void rebuildIndexes();

    /**
     * @return todas as entidades arquivadas
     */
    List<Entity> listArchived();

    /**
     * @param id ID a procurar no arquivo
     * @return a entidade arquivada, ou null se não existir
     */
    Entity searchArchivedById(int id);
}