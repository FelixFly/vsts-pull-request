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
public class RCPullRequestServiceImpl extends BasePullRequestService {


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestTemplate yjRestTemplate;

    @Autowired
    private PullRequestProperties pullRequestProperties;

    @Override
    public boolean isSupport(String targetBranch) {
        return targetBranch.equals(BranchNameEnum.RC.getValue());
    }

    @Override
    protected boolean checkBranch(String projectName, String sourceBranch, String targetBranch) {
        /*if (!sourceBranch.startsWith(BranchNameEnum.TEST.getValue())) {
            log.error("PR到RC分支必须是test分支，当前分支是{}", sourceBranch);
            return false;
        }*/
        return true;
    }

    @Override
    protected boolean isDeleteSourceBranch() {
        return true;
    }

    @Override
    protected void autoOtherApprove(String projectName, String pullRequestId) {
        // 开发人审批
        autoApprove(projectName, pullRequestId, this.pullRequestProperties.getApproveUser().getUserId(),
                this.yjRestTemplate);
        // 代码审核审批
        autoApprove(projectName, pullRequestId, this.pullRequestProperties.getPullUser().getUserId(),
                this.restTemplate);
    }

    @Override
    protected void createBranchIfNecessary(String projectName, JSONObject defaultRequest, String sourceBranch,
                                           String targetBranch) {
        //createBranch(projectName, defaultRequest, sourceBranch, targetBranch);
    }
}
