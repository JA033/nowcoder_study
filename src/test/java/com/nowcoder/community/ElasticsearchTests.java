package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100));
    }

    @Test
    public void testUpdate(){
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("guanshui");
        discussPostRepository.save(post);
    }

    @Test
    public void testDelete(){
//        discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }

    @Test
    public void testSearch(){
        //创建criteria普通查询
        Criteria criteria = new Criteria("title").matches("互联网寒冬").or("content").matches("互联网寒冬");

        //创建list，分别添加高亮的field和内容
        List<HighlightField> highlightFields = new ArrayList<>();
        HighlightField highlightField = new HighlightField("title", HighlightFieldParameters.builder().withPreTags("<em>").withPostTags("</em>").build());
        highlightFields.add(highlightField);
        highlightField = new HighlightField("content",HighlightFieldParameters.builder().withPreTags("<em>").withPostTags("</em>").build());
        highlightFields.add(highlightField);

        //传入高亮配置，创建高亮查询
        Highlight highlight = new Highlight(highlightFields);
        HighlightQuery highlightQuery = new HighlightQuery(highlight,DiscussPost.class);

        //创建criteria查询的builder，并设置排序，高亮，支持分页
        CriteriaQueryBuilder builder = new CriteriaQueryBuilder(criteria)
                .withSort(Sort.by(Sort.Direction.DESC,"type"))
                .withSort(Sort.by(Sort.Direction.DESC,"score"))
                .withSort(Sort.by(Sort.Direction.DESC,"createTime"))
                .withHighlightQuery(highlightQuery)
                .withPageable(PageRequest.of(0,10));
        //用builder创建query
        CriteriaQuery query = new CriteriaQuery(builder);

        //查询结果，返回hits
        SearchHits<DiscussPost> result = elasticsearchTemplate.search(query, DiscussPost.class);

        //从hits中取出每个hit到列表中
        List<SearchHit<DiscussPost>> searchHitList = result.getSearchHits();

        List<DiscussPost> discussPostList =new ArrayList<>();

        for(SearchHit<DiscussPost> hit:searchHitList){
            //取出未高亮的结果
            DiscussPost post = hit.getContent();

            //将高亮结果加入到结果中
            var titleHighlight = hit.getHighlightField("title");
            if(titleHighlight.size()!=0){
                post.setTitle(titleHighlight.get(0));
            }

            var contentHighlight = hit.getHighlightField("content");
            if(contentHighlight.size()!=0){
                post.setTitle(contentHighlight.get(0));
            }

            discussPostList.add(post);
        }

        //创建Page对象，并从中取出所有数据打印
        Page<DiscussPost> page = new PageImpl<>(discussPostList,PageRequest.of(0,10),result.getTotalHits());
        for(DiscussPost post:page) {
            System.out.println(post);
        }

    }
}
