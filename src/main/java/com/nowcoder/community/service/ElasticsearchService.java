package com.nowcoder.community.service;

import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.CriteriaQueryBuilder;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public void saveDiscussPost(DiscussPost post){
        discussPostRepository.save(post);
    }

    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }

    public Page<DiscussPost> searchDiscussPost(String keyword,int current,int limit){
        //创建criteria普通查询
        Criteria criteria = new Criteria("title").matches(keyword).or("content").matches(keyword);

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
                .withPageable(PageRequest.of(current,limit));
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

        //创建Page对象
        Page<DiscussPost> page = new PageImpl<>(discussPostList,PageRequest.of(current,limit),result.getTotalHits());
        return page;
    }
}
