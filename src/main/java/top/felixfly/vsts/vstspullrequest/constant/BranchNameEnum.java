package top.felixfly.vsts.vstspullrequest.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 分支名称
 *
 * @author FelixFly <chenglinxu@yeah.net>
 * @date 2020/3/19
 */
@Getter
@RequiredArgsConstructor
public enum BranchNameEnum {
    /**
     * DEPLOY
     */
    DEPLOY("deploy"),
    /**
     * develop
     */
    DEVELOP("develop"),
    /**
     * develop_deploy
     */
    DEVELOP_DEPLOY("develop_deploy"),
    /**
     * DEVELOP_IPT
     */
    DEVELOP_IPT("develop_ipt"),
    /**
     * DEVELOP_HIS
     */
    DEVELOP_HIS("develop_his"),
    /**
     * RC_his
     */
    RC_HIS("RC_his"),
    /**
     * TASK
     */
    TASK("task"),
    /**
     * TASK_DEV
     */
    TASK_DEV("task_dev"),
    /**
     * 1230 迭代
     */
    ITERATION_1230("iteration/1230"),
    /**
     * RC
     */
    RC("RC");

    private final String value;
}
