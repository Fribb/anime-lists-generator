package net.fribbtastic.coding.animelistsgenerator.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

/**
 * data.download and the local source files
 */
class PropertiesTest {

    private boolean download;
    private String projectPath;

    @BeforeEach
    void saveProperties() {
        this.download = Properties.download;
        this.projectPath = Properties.projectPath;
        Properties.projectPath = "anime-lists";
    }

    @AfterEach
    void restoreProperties() {
        Properties.download = this.download;
        Properties.projectPath = this.projectPath;
    }

    @Test
    @DisplayName("Test: downloading ignores the local files")
    void testDownload() {
        Properties.download = true;
        Assertions.assertThat(Properties.localFile("C:/data/anime-list-full.xml", "anime-list-full.xml")).isNull();
    }

    @Test
    @DisplayName("Test: without downloading the local file is used")
    void testLocalFile() {
        Properties.download = false;
        Assertions.assertThat(Properties.localFile("C:/data/anime-list-full.xml", "anime-list-full.xml"))
                .isEqualTo("C:/data/anime-list-full.xml");
    }

    @Test
    @DisplayName("Test: without downloading and without a file the saved download is used")
    void testSavedDownload() {
        Properties.download = false;
        Assertions.assertThat(Properties.localFile(" ", "anime-list-full.xml"))
                .isEqualTo(Path.of("anime-lists", "source", "anime-list-full.xml").toString());
    }

    @Test
    @DisplayName("Test: a missing local file is an error that names the setting")
    void testMissingLocalFile() {
        Assertions.assertThatThrownBy(() -> SourceFiles.read("does/not/exist.xml", "anime-lists.file"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("anime-lists.file");
    }
}
