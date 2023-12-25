package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    //查询当前用户的会话列表（按不同的人来算），每个会话只返回最新的一条消息
    List<Message> selectConversations(int userId,int offset,int limit);

    //查询当前用户的所有会话数量
    int selectConversationCount(int userId);

    //查询某个会话包含的所有消息列表
    List<Message> selectLetters(String conversationId,int offset,int limit);

    //查询某个会话包含的所有消息的数量
    int selectLetterCount(String conversationId);

    //查询所有会话的未读消息的总和(conversationId作为动态条件，如果条件为空就是查询所有会话的，否则就是查询某一个会话的)
    int selectLetterUnreadCount(int userId, String conversationId);

    //新增消息
    int insertMessage(Message message);

    //修改一系列消息的状态
    int updateStatus(List<Integer> ids,int status);

    //查询某个主题下最新的一条通知
    Message selectLatestNotice(int userId,String topic);

    //查询某个主题包含的通知数量
    int selectNoticeCount(int userId,String topic);

    //查询未读的通知的数量
    int selectNoticeUnreadCount(int userId,String topic);

    //查询某个主题包含的通知列表
    List<Message> selectNotices(int userId,String topic,int offset,int limit);
}
