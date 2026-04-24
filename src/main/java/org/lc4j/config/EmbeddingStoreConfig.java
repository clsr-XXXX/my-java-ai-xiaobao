package org.lc4j.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingStoreConfig {
    @Autowired
    private EmbeddingModel embeddingModel;

    // 从 application.yml 中读取 Pinecone 配置
    @Value("${langchain4j.pinecone.api-key}")
    private String pineconeApiKey;

    @Value("${langchain4j.pinecone.index-name}")
    private String indexName;

    @Value("${langchain4j.pinecone.environment}")
    private String environment;

    @Value("${langchain4j.pinecone.namespace}")
    private String namespace;

    /**
     * 创建 Pinecone 向量存储 Bean
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        // 验证 API Key 配置
        if (pineconeApiKey == null || pineconeApiKey.isEmpty()) {
            throw new IllegalStateException(
                    "❌ Pinecone API Key 未配置！\n" +
                            "请在 application.yml 中配置：\n" +
                            "pinecone:\n" +
                            "  api-key: pcsk_your_api_key\n" +
                            "  index-name: xiaobao-index\n" +
                            "  environment: us-east-1\n" +
                            "  namespace: default"
            );
        }

        System.out.println("🔗 正在初始化 Pinecone 向量存储...");
        System.out.println("   Index: " + indexName);
        System.out.println("   Environment: " + environment);
        System.out.println("   Namespace: " + namespace);

        // 创建 Pinecone 向量存储
        PineconeEmbeddingStore embeddingStore = PineconeEmbeddingStore.builder()
                .apiKey(pineconeApiKey)
                .index(indexName)
                .nameSpace(namespace)
                .build();  // ← 只有一个 build()

        System.out.println("✅ Pinecone 向量存储初始化成功");

        return embeddingStore;
    }
}
