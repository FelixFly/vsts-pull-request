package top.felixfly.vsts.vstspullrequest.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;


/**
 * Pull Request 配置
 *
 * @author FelixFly <chenglinxu@yeah.net>
 * @date 2020/3/18
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "pull.request")
public class PullRequestProperties {
    /**
     * 发起用户
     */
    @NotNull
    private UserProperties pullUser;
    /**
     * 审批用户
     */
    @NotNull
    private UserProperties approveUser;
    /**
     * 组织名称
     */
    @NotNull
    private String organization;
    /**
     * 项目名称，多个以，分隔
     */
    @NotNull
    private String projectName;
    /**
     * 源分支，多个以，分隔，不能与目标分支同时存在多个
     */
    @NotNull
    private String sourceBranch;
    /**
     * 目标分支，多个以，分隔，不能与源分支同时存在多个
     */
    @NotNull
    private String targetBranch;
}
