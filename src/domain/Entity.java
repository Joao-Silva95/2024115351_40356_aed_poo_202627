package domain;

import graph.LabyrinthGraph;

/**
 * Entidade base do sistema, identificada por um ID inteiro único
 */
public abstract class Entity {

    /**
     * Valor que indica que o ID ainda não foi atribuído (será gerado pelo sistema).
     */
    public static final int UNASSIGNED_ID = -1;

    /**
     * Identificador único da entidade
     */
    private int id;

    /**
     * Nome ou código da entidade
     */
    private String name;

    /**
     * @param id   ID escolhido pelo utilizador (>= 0) ou {@link #UNASSIGNED_ID}
     * @param name nome ou código da entidade
     * @throws IllegalArgumentException se o ID for inválido ou o nome vazio
     */
    protected Entity(int id, String name) {
        if (id < 0 && id != UNASSIGNED_ID)
            throw new IllegalArgumentException("ID inválido: " + id);
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nome inválido");
        this.id = id;
        this.name = name;
    }

    /**
     * @return o ID da entidade, ou {@link #UNASSIGNED_ID} se ainda não tem
     */
    public int getId() {
        return id;
    }

    /**
     * @return true se a entidade já tem ID atribuído
     */
    public boolean hasId() {
        return id != UNASSIGNED_ID;
    }

    /**
     * Atribui o ID uma única vez (chamado pelo sistema ao inserir)
     *
     * @param id novo ID (>= 0)
     * @throws IllegalStateException se já tinha ID
     */
    public void assignId(int id) {
        if (hasId()) throw new IllegalStateException("ID já atribuído: " + this.id);
        if (id < 0) throw new IllegalArgumentException("ID inválido: " + id);
        this.id = id;
    }

    /**
     * @return o nome da entidade
     */
    public String getName() {
        return name;
    }

    /**
     * @param name novo nome (não vazio)
     */
    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nome inválido");
        this.name = name;
    }

    public LabyrinthGraph hasVertices;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id + ", name=" + name + "]";
    }
}