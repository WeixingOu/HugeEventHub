package com.itbh.hevent.services.impls;

import com.itbh.hevent.Repositories.ArticleRepository;
import com.itbh.hevent.Repositories.CategoryRepository;
import com.itbh.hevent.Repositories.UserRepository;
import com.itbh.hevent.dtos.ArticleDTO;
import com.itbh.hevent.dtos.PageBean;
import com.itbh.hevent.dtos.records.AddArticleDTO;
import com.itbh.hevent.enums.ArticleState;
import com.itbh.hevent.mappers.ArticleMapper;
import com.itbh.hevent.models.Article;
import com.itbh.hevent.models.Category;
import com.itbh.hevent.models.User;
import com.itbh.hevent.services.ArticleService;
import com.itbh.hevent.utils.AuthenticationFacadeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AuthenticationFacadeImpl authenticationFacade;
    private final ArticleMapper articleMapper;
    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository, UserRepository userRepository, CategoryRepository categoryRepository, AuthenticationFacadeImpl authenticationFacade, ArticleMapper articleMapper) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.authenticationFacade = authenticationFacade;
        this.articleMapper = articleMapper;
    }

    @Override
    public ArticleDTO addArticle(AddArticleDTO addArticleDTO) {
        User user = AuthenticationFacadeUtil.getAuthenticatedUser(authenticationFacade,userRepository);

        Category category = categoryRepository.findById(addArticleDTO.categoryId())
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + addArticleDTO.categoryId()));

        Article article = new Article(addArticleDTO.title(), addArticleDTO.content(), addArticleDTO.coverImg(), addArticleDTO.state(), category, user);

        return articleMapper.toArticleDTO(articleRepository.save(article));
    }

    @Override
    public PageBean<ArticleDTO> getArticles(int page, int size, Long categoryId, ArticleState state) {
        User user = AuthenticationFacadeUtil.getAuthenticatedUser(authenticationFacade, userRepository);

        Pageable pageable = PageRequest.of(page, size);
        Page<Article> articles;

        if (categoryId != null && state != null) {
            articles = articleRepository.findByCreateUserAndCategoryIdAndState(user, categoryId, state, pageable);
        }
        else if (state != null) {
            articles = articleRepository.findByCreateUserAndState(user, state, pageable);
        }
        
        else if (categoryId != null) {
            articles = articleRepository.findByCreateUserAndCategoryId(user, categoryId, pageable);
        }
        else {
            articles = articleRepository.findByCreateUser(user, pageable);
        }
        return new PageBean<>(articles.getContent().stream().map(articleMapper::toArticleDTO).toList(), articles.getTotalElements());
    }
}
