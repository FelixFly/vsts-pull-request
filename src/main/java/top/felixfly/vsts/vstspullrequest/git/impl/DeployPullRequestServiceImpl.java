package top.felixfly.vsts.vstspullrequest.git.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.felixfly.vsts.vstspullrequest.constant.BranchNameEnum;
import top.felixfly.vsts.vstspullrequest.git.BasePullRequestService;

/**
 * Deploy分支合并
 *
 * @author FelixFly <chenglinxu@yeah.net>
 * @date 2020/3/19
 */
@Slf4j
@Service
public class DeployPullRequestServiceImpl extends BasePullRequestService {

    @Override
    public boolean isSupport(String targetBranch) {
        return targetBranch.equals(BranchNameEnum.DEPLOY.getValue());
    }

    @Override
    protected boolean checkBranch(String projectName, String sourceBranch, String targetBranch) {
        if (!sourceBranch.startsWith(BranchNameEnum.DEVELOP.getValue())) {
            log.error("PR到deploy分支必须是develop分支，当前分支是{}", sourceBranch);
            return false;
        }
        return true;
    }

    @Override
    protected boolean isDeleteSourceBranch() {
        return false;
    }

    @Override
    protected void autoOtherApprove(String projectName, String pullRequestId) {
    }

    @Override
    protected void createBranchIfNecessary(String projectName, JSONObject defaultRequest, String sourceBranch,
                                           String targetBranch) {
    }
}
