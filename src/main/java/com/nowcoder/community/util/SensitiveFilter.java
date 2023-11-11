package com.nowcoder.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    //初始化前缀树
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e);
        }

    }

    //将一个关键词添加到trie树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for(int i=0;i<keyword.length();i++){
            char c = keyword.charAt(i);
            //尝试查找下一个节点
            TrieNode subNode=tempNode.getSubNode(c);

            //如果下一个节点不存在，则创建该节点，并将其挂在当前节点下
            if(subNode==null){
                subNode=new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

            //指向下一节点
            tempNode=subNode;

            //标识最后一个节点
            if(i==keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 将输入text进行过滤并输出过滤后的字符串
     * @param text 待过滤文本
     * @return 过滤后文本
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }

        //指向后缀树的指针
        TrieNode tempNode=rootNode;
        //指向检查的开头位置
        int begin=0;
        //指向检查的末尾
        int position=0;
        //过滤结果
        StringBuilder sb = new StringBuilder();

        while(position<text.length()){
            char c = text.charAt(position);

            //跳过符号
            if(isSymbol(c)){
                //如果当前是检查的起始位置（tempNode==rootNode），则将符号记入结果并向下走
                if(tempNode==rootNode){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }

            //检查下一节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                //以begin为开头的子串不是敏感词
                sb.append(text.charAt(begin));
                //检查下一位置的子串
                begin++;
                position=begin;
                //trie树检查指针归位
                tempNode=rootNode;
            }
            else if(tempNode.isKeywordEnd()){
                //以begin为开头的子串是敏感词
                sb.append(REPLACEMENT);
                //继续检查子串的下一个位置
                position++;
                begin=position;
                //trie树检查指针归位
                tempNode=rootNode;
            }
            else{
                //继续检查下一位置
                position++;
            }
        }

        //将最后的字符加入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    //判断一个字符是否为符号
    private boolean isSymbol(Character c){
        return !CharUtils.isAsciiAlphanumeric(c) && (c<0x2E80 || c>0x9FFF);
    }

    private class TrieNode {
        //关键词结尾标识
        private boolean isKeywordEnd = false;

        //子结点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子结点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
