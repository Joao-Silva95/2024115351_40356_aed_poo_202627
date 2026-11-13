package graph;

/** Critério de peso usado nos caminhos mais curtos. */
public enum WeightType {
    DISTANCE, ENERGY, TIME, RADIATION;

    /**
     * @param e aresta
     * @return o peso da aresta segundo este critério
     */
    public double of(MultiEdge e) {
        return switch (this) {
            case DISTANCE -> e.getDistance();
            case ENERGY -> e.getEnergyCost();
            case TIME -> e.getTransitTime();
            case RADIATION -> e.getRadiationDangerLevel();
        };
    }
}