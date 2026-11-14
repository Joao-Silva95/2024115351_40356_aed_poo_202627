package graph.Enums;

/** Tipo de relação representada por uma aresta do grafo. */
public enum RelationType {
    /** Ligação espacial sala→sala (rota). */
    CORRIDOR(true),
    /** Localização: robô→sala onde está, ou sala→artefacto que contém (rota). */
    LOCATION(true),
    /** Evento: robô entrou numa sala. */
    TRAVERSAL(false),
    /** Evento: robô recolheu um artefacto. */
    COLLECTS(false),
    /** Evento: robô usou uma sala de manutenção. */
    MAINTENANCE(false),
    /** Evento: comunicação entre robôs. */
    COMMUNICATION(false);

    private final boolean routable;

    RelationType(boolean routable) {
        this.routable = routable;
    }

    /** @return true se a aresta pode fazer parte de um caminho (e da conectividade) */
    public boolean isRoutable() {
        return routable;
    }
}