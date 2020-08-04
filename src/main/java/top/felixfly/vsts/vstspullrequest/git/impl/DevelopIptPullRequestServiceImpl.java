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
public class DevelopIptPullRequestServiceImpl extends BasePullRequestService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestTemplate yjRestTemplate;

    @Autowired
    private PullRequestProperties pullRequestProperties;

    @Override
    public boolean isSupport(String targetBranch) {
        return targetBranch.startsWith(BranchNameEnum.DEVELOP_IPT.getValue());
    }

    @Override
    protected boolean checkBranch(String projectName, String sourceBranch, String targetBranch) {
      /*  if (!sourceBranch.startsWith(BranchNameEnum.DEVELOP.getValue())) {
            log.error("PR到develop_ipt分支必须是develop分支，当前分支是{}", sourceBranch);
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
        // 审批
        autoApprove(projectName, pullRequestId, this.pullRequestProperties.getPullUser().getUserId(),
                this.restTemplate);
        // 需要另外一个人审批
        autoApprove(projectName, pullRequestId, this.pullRequestProperties.getApproveUser().getUserId(),
                this.yjRestTemplate);
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
