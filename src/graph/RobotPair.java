package graph;

import domain.Robot;

/**
 * Par de robôs que operaram no mesmo setor.
 *
 * @param first  robô com o ID menor
 * @param second robô com o ID maior
 * @param sector ID do setor (sala)
 */
public record RobotPair(Robot first, Robot second, String sector) {
}