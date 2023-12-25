package com.nowcoder.community.entity;


import java.util.HashMap;
import java.util.Map;

public class Event {
    private String topic;
    private int userId; //触发事件的人，例如张三给李四的帖子点赞，则触发人为张三
    private int entityType; //触发的类型，例如帖子，评论，人
    private int entityId; //实体id
    private int entityUserId; //实体的创建人，例如帖子的创建人
    private Map<String,Object> data = new HashMap<>(); //存其他可能有的数据

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key,value);
        return this;
    }
}
