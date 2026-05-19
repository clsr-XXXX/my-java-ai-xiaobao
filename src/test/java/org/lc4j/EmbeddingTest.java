package org.lc4j;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class EmbeddingTest {
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Test
    public void testPinecone() {
        TextSegment segment = TextSegment.from("Hello World");
        Embedding content = embeddingModel.embed(segment).content();
        embeddingStore.add(content, segment);
    }
}
