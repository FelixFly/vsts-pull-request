package top.felixfly.vsts.vstspullrequest.git;

/**
 * Git Pull Request 操作
 *
 * @author FelixFly <chenglinxu@yeah.net>
 * @date 2020/3/18
 */
public interface PullRequestService {

    /**
     * 适配的分支
     *
     * @param targetBranch 目标分支
     * @return true 支持 false 不支持
     */
    boolean isSupport(String targetBranch);

    /**
     * 请求Pull Request
     *
     * @param projectName
     * @param sourceBranch
     * @param targetBranch
     */
    void pullRequest(String projectName, String sourceBranch, String targetBranch);
}
