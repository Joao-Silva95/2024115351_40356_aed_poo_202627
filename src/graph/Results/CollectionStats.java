package graph.Results;

/**
 * Estatísticas de recolha de artefactos.
 *
 * @param count                  número de artefactos recolhidos
 * @param totalWeight            peso total (kg)
 * @param totalScientificValue   soma dos valores científicos
 * @param averageScientificValue valor científico médio (0 se não houver recolhas)
 */
public record CollectionStats(int count, double totalWeight, int totalScientificValue, double averageScientificValue) {
}