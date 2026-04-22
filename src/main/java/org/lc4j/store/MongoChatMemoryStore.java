package org.lc4j.store;


import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.lc4j.bean.ChatMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MongoChatMemoryStore implements ChatMemoryStore {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // 确保查询条件和存入时的类型一致，都用 String
        Criteria criteria = Criteria.where("memoryId").is(memoryId.toString());
        Query query = new Query(criteria);
        ChatMessages chatMessages = mongoTemplate.findOne(query, ChatMessages.class);

        if (chatMessages == null || chatMessages.getContent() == null) {
            return new ArrayList<>();
        }
        return ChatMessageDeserializer.messagesFromJson(chatMessages.getContent());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        // 关键：如果 list 里面没东西，直接 return，不要操作数据库
        if (list == null || list.isEmpty()) {
            return;
        }

        Criteria criteria = Criteria.where("memoryId").is(memoryId.toString());
        Query query = new Query(criteria);

        Update update = new Update();
        update.set("memoryId", memoryId.toString());
        // 确保字段名叫 content，和你实体类 ChatMessages 对应
        update.set("content", ChatMessageSerializer.messagesToJson(list));

        // 使用 upsert，确保没有记录时新增，有记录时更新
        mongoTemplate.upsert(query, update, ChatMessages.class);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        mongoTemplate.remove(query, ChatMessages.class);

    }
}
