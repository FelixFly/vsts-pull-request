package top.felixfly.vsts.vstspullrequest.git.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import top.felixfly.vsts.vstspullrequest.configuration.PullRequestProperties;
import top.felixfly.vsts.vstspullrequest.constant.BranchNameEnum;
import top.felixfly.vsts.vstspullrequest.git.BasePullRequestService;

/**
 * test分支合并
 *
 * @author FelixFly <chenglinxu@yeah.net>
 * @date 2020/3/19
 */
@Slf4j
@Service
public class TaskPullRequestServiceImpl extends BasePullRequestService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PullRequestProperties pullRequestProperties;

    @Override
    public boolean isSupport(String targetBranch) {
        return targetBranch.startsWith(BranchNameEnum.TASK.getValue());
    }

    @Override
    protected boolean isDeleteSourceBranch() {
        return true;
    }

    @Override
    protected void autoOtherApprove(String projectName, String pullRequestId) {

    }

    @Override
    protected void createBranchIfNecessary(String projectName, JSONObject defaultRequest, String sourceBranch,
                                           String targetBranch) {
        if (!checkIfNecessary(sourceBranch)) {
            return;
        }
        createBranch(projectName, defaultRequest, sourceBranch, targetBranch);
    }
}
