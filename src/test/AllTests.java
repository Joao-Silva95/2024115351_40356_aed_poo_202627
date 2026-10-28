package test;

import test.R2.DatabaseManagerTest;
import test.R3.SearchTest;
import test.R4.ConsistencyTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static test.Common.TestUtil.summary;

/**
 * Executa todos os testes (R2, R3 e R4) e guarda a saída em test_results/AllTests_output.txt.
 */
public class AllTests {

    /**
     * @param args não utilizado
     * @throws IOException se não for possível criar o ficheiro de resultados
     */
    public static void main(String[] args) throws IOException {
        Files.createDirectories(Path.of("test_results"));
        PrintStream console = System.out;
        PrintStream file = new PrintStream(new FileOutputStream("test_results/AllTests_output.txt"), true,
                StandardCharsets.UTF_8);
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                console.write(b);
                file.write(b);
            }
        }, true, StandardCharsets.UTF_8));

        DatabaseManagerTest.runAll();
        SearchTest.runAll();
        ConsistencyTest.runAll();
        summary();

        file.close();
    }
}