package top.felixfly.vsts.vstspullrequest.autoconfigure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import top.felixfly.vsts.vstspullrequest.configuration.PullRequestProperties;
import top.felixfly.vsts.vstspullrequest.configuration.UserProperties;

/**
 * Pull Request 自动装配
 *
 * @author FelixFly <chenglinxu@yeah.net>
 * @date 2020/3/19
 */
@Configuration
@EnableConfigurationProperties(PullRequestProperties.class)
public class PullRequestAutoConfiguration {

    /**
     * 根URL
     */
    private final static String ROOT_URI = "http://tfs2018-web.winning.com.cn:8080/tfs/WINNING-6.0/";
    /**
     * GIT 参数配置
     */
    private final static String GIT = "/_apis/git/repositories";

    /**
     * 工作项查询
     */
    private final static String WORK_ITEM = "_apis/wit/workitems";

    @Bean
    public RestTemplate yjRestTemplate(PullRequestProperties pullRequestProperties) {
        UserProperties approveUser = pullRequestProperties.getApproveUser();
        String organization = pullRequestProperties.getOrganization();
        RestTemplate build = new RestTemplateBuilder()
                .basicAuthentication(approveUser.getUserName(), approveUser.getSecret())
                .rootUri(ROOT_URI + organization + GIT)
                .build();
        build.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return build;
    }

    @Bean
    public RestTemplate restTemplate(PullRequestProperties pullRequestProperties) {
        UserProperties pullUser = pullRequestProperties.getPullUser();
        String organization = pullRequestProperties.getOrganization();
        RestTemplate build = new RestTemplateBuilder()
                .basicAuthentication(pullUser.getUserName(), pullUser.getSecret())
                .rootUri(ROOT_URI + organization + GIT)
                .build();
        build.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return build;
    }


    @Bean
    public RestTemplate workItemRestTemplate(PullRequestProperties pullRequestProperties) {
        UserProperties pullUser = pullRequestProperties.getPullUser();
        RestTemplate build = new RestTemplateBuilder()
                .basicAuthentication(pullUser.getUserName(), pullUser.getSecret())
                .rootUri(ROOT_URI + WORK_ITEM)
                .build();
        build.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return build;
    }
}
