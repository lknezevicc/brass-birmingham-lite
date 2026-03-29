package hr.lknezevic.brassbirmingham.serialization;

import hr.lknezevic.brassbirmingham.reflection.HtmlReportGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlReportGeneratorTest {

    @Test
    void generatesFileWithContent(@TempDir Path tempDir) throws Exception {
        File output = tempDir.resolve("report.html").toFile();
        new HtmlReportGenerator().generate(output);

        assertThat(output).exists();
        String html = Files.readString(output.toPath());
        assertThat(html).contains("<!DOCTYPE html>");
        assertThat(html).contains("Industry Report");
    }

    @Test
    void containsAllFourIndustryTypes(@TempDir Path tempDir) throws Exception {
        File output = tempDir.resolve("report.html").toFile();
        new HtmlReportGenerator().generate(output);
        String html = Files.readString(output.toPath());

        assertThat(html).contains("Coal mine");
        assertThat(html).contains("Iron works");
        assertThat(html).contains("Brewery");
        assertThat(html).contains("Cotton mill");
    }

    @Test
    void containsAttributeValues(@TempDir Path tempDir) throws Exception {
        File output = tempDir.resolve("report.html").toFile();
        new HtmlReportGenerator().generate(output);
        String html = Files.readString(output.toPath());

        // CoalMine L1 build cost is 5, L2 is 8
        assertThat(html).contains(">5<");
        assertThat(html).contains(">8<");
        // VPs exist somewhere
        assertThat(html).contains("Victory Points");
        assertThat(html).contains("Build Cost");
    }

    @Test
    void usesReflectionMetadata(@TempDir Path tempDir) throws Exception {
        File output = tempDir.resolve("report.html").toFile();
        new HtmlReportGenerator().generate(output);
        String html = Files.readString(output.toPath());

        assertThat(html).contains("Declared Methods");
        assertThat(html).contains("Class.getDeclaredMethods()");
    }

    @Test
    void buildHtmlReturnsString() {
        String html = new HtmlReportGenerator().buildHtml();
        assertThat(html).isNotEmpty();
        assertThat(html).startsWith("<!DOCTYPE html>");
    }
}
