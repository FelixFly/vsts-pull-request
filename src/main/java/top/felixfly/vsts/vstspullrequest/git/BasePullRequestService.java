package top.felixfly.vsts.vstspullrequest.git;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 抽象实现
 *
 * @author FelixFly <chenglinxu@yeah.net>
 * @date 2020/3/18
 */
@Slf4j
@Component
public abstract class BasePullRequestService implements PullRequestService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EventBus eventBus;

    private final static Pattern PATTERN = Pattern.compile("#(\\d+)");

    protected JSONObject getDefaultRequest(String sourceBranch, String targetBranch) {
        JSONObject request = new JSONObject();
        request.put("sourceRefName", "refs/heads/" + sourceBranch);
        request.put("targetRefName", "refs/heads/" + targetBranch);
        request.put("title", "merge " + sourceBranch + " into " + targetBranch);
        return request;
    }


    @Override
    public void pullRequest(String projectName, String sourceBranch, String targetBranch) {
        // 进行必要的检查
        boolean checkFlag = checkBranch(projectName, sourceBranch, targetBranch);
        if (!checkFlag) {
            return;
        }
        // 创建PR请求
        JSONObject defaultRequest = getDefaultRequest(sourceBranch, targetBranch);
        // 获取工作项
        boolean workFlag = setWorkItem(projectName, defaultRequest, sourceBranch, targetBranch);
        // 无工作项信息不进行执行
        if (!workFlag) {
            return;
        }
        // 创建中间合并分支
        createBranchIfNecessary(projectName, defaultRequest, sourceBranch, targetBranch);
        // PR请求
        JSONObject pullRequest = autoPullRequest(projectName, defaultRequest);
        // 请求ID
        String pullRequestId = pullRequest.getString("pullRequestId");
        // 设置自动完成
        boolean deleteFlag = isDeleteSourceBranch();
        setAutoComplete(projectName, pullRequestId, deleteFlag);
        // 自动审批
        autoOtherApprove(projectName, pullRequestId);
        // 需要验证这个状态(多次循环，成功直接退出)
        getPullRequestStatus(projectName, pullRequestId);
    }

    protected abstract boolean isDeleteSourceBranch();

    protected abstract void autoOtherApprove(String projectName, String pullRequestId);

    protected abstract void createBranchIfNecessary(String projectName, JSONObject defaultRequest, String sourceBranch,
                                                    String targetBranch);

    protected boolean checkIfNecessary(String sourceBranch){
        return !sourceBranch.startsWith("feature") && !sourceBranch.startsWith("issue");
    }


    protected boolean checkBranch(String projectName, String sourceBranch, String targetBranch) {
        return true;
    }

    protected JSONObject autoPullRequest(String projectName, JSONObject request) {
        JSONObject jsonObject = this.restTemplate
                .postForObject(
                        "/" + projectName + "/pullrequests?supportsIterations=true&api-version=4.1",
                        request,
                        JSONObject.class);
        log.info("PR返回：{}", JSON.toJSONString(jsonObject));
        return jsonObject;
    }


    protected boolean setWorkItem(String projectName, JSONObject request, String sourceBranch, String targetBranch) {
        // 获取信息
        JSONObject contentObject = this.restTemplate
                .getForObject(
                        "/" + projectName + "/commits?searchCriteria.itemVersion.version=" +
                                targetBranch + "&searchCriteria.compareVersion.version=" + sourceBranch +
                                "&api-version=4.1",
                        JSONObject.class);

        List<JSONObject> values = contentObject.getObject("value", new TypeReference<List<JSONObject>>() {
        }.getType());
        Set<String> workItemIds = values.stream().flatMap(item -> {
            String comment = item.getString("comment");
            // PR信息给去掉
            if (comment.contains("PR")) {
                return Stream.empty();
            }
            Matcher matcher = PATTERN.matcher(comment);
            Set<String> workId = new HashSet<>();
            while (matcher.find()) {
                workId.add(matcher.group().trim());
            }
            return workId.stream();
        }).collect(Collectors.toSet());
        List<JSONObject> workItemRefs = workItemIds.stream()
                .filter(StringUtils::isNotBlank)
                .map(workItemId -> {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", workItemId.substring(1));
                    return jsonObject;
                }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(workItemRefs)) {
            log.info("项目名称{}：{} -> {}无合并信息", projectName, sourceBranch, targetBranch);
            return false;
        }
        request.put("workItemRefs", workItemRefs);
        request.put("description", " " + String.join(" ", workItemIds));
        // 发布事件
        eventBus.post(workItemIds);
        return true;
    }

    protected void abandon(String projectName, String oriPullRequestId) {
        JSONObject abandonedRequest = new JSONObject();
        abandonedRequest.put("status", "abandoned");
        String patchForObject = this.restTemplate
                .patchForObject(
                        "/" + projectName + "/pullrequests/" + oriPullRequestId + "?api-version=4.1",
                        abandonedRequest, String.class);
        log.info("放弃PR返回{}", patchForObject);
    }

    protected void autoApprove(String projectName, String pullRequestId, String userId, RestTemplate restTemplate) {
        JSONObject checkRequest = new JSONObject();
        checkRequest.put("vote", 10);
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(checkRequest);

        // 自动审批
        ResponseEntity<String> exchange = restTemplate.exchange(
                "/" + projectName + "/pullrequests/" + pullRequestId + "/reviewers/" + userId + "?api-version=4.1",
                HttpMethod.PUT, httpEntity, String.class);
        log.info("自动审批返回{}", exchange.getBody());
    }

    protected void setAutoComplete(String projectName, String pullRequestId, boolean deleteFlag) {
        JSONObject patchRequest = new JSONObject();
        JSONObject autoCompleteSetBy = new JSONObject();
        autoCompleteSetBy.put("id", "a883ce99-736d-4068-a13c-57cefa2c0f89");
        patchRequest.put("autoCompleteSetBy", autoCompleteSetBy);
        JSONObject completionOptions = new JSONObject();
        completionOptions.put("triggeredByAutoComplete", true);
        completionOptions.put("deleteSourceBranch", deleteFlag);
        completionOptions.put("squashMerge", false);
        patchRequest.put("completionOptions", completionOptions);
        String patchForObject = this.restTemplate
                .patchForObject(
                        "/" + projectName + "/pullrequests/" + pullRequestId + "?api-version=4.1",
                        patchRequest, String.class);
        log.info("自动完成返回{}", patchForObject);
    }


    private void getPullRequestStatus(String projectName, String pullRequestId) {
        for (int i = 0; i < 3; i++) {
            JSONObject pullRequestStatus = this.restTemplate
                    .getForObject("/" + projectName + "/pullrequests/" + pullRequestId + "?api-version=4.1",
                            JSONObject.class);
            String status = pullRequestStatus.getString("status");
            String mergeStatus = pullRequestStatus.getString("mergeStatus");
            log.info("项目{}合并状态:{},PR状态:{}", projectName, status, mergeStatus);
            if ("conflicts".equals(mergeStatus) || "failure".equals(mergeStatus)) {
                // 冲突和失败进行提示
                log.error("项目{}合并状态:{},PR状态:{}", projectName, status, mergeStatus);
                break;
            }
            if ("succeeded".equals(mergeStatus)) {
                // 成功状态直接退出
                break;
            }
            // 每隔1分支
            try {
                TimeUnit.SECONDS.sleep(30);
            } catch (InterruptedException e) {
            }
        }
    }

    protected void createBranch(String projectName, JSONObject defaultRequest, String sourceBranch,
                                String targetBranch) {
        String headsBranch = "heads/" + sourceBranch;
        // 首先要获取分支的信息
        JSONObject branchObject = this.restTemplate
                .getForObject("/" + projectName + "/refs?filter=" + headsBranch + "&api-version=4.1",
                        JSONObject.class);
        List<JSONObject> branches = branchObject.getObject("value", new TypeReference<List<JSONObject>>() {
        }.getType());
        JSONObject source = branches.stream().filter(item -> item.getString("name").endsWith(headsBranch))
                .findAny()
                .orElseThrow(() -> new RuntimeException("项目" + projectName + "没有找到对应的分支" + sourceBranch + "信息"));
        log.info("项目{}分支{}信息:{}", projectName, sourceBranch, JSON.toJSONString(source));
        String commitId = source.getString("objectId");
        JSONArray array = new JSONArray();

        JSONObject jsonObject = new JSONObject();
        String otherBranchName = sourceBranch + "_to_" + targetBranch;
        String otherBranch = "refs/heads/middle/" + otherBranchName;
        jsonObject.put("name", otherBranch);
        jsonObject.put("oldObjectId", "0000000000000000000000000000000000000000");
        jsonObject.put("newObjectId", commitId);
        array.add(jsonObject);

        JSONObject otherBranchResponse = this.restTemplate
                .postForObject("/" + projectName + "/refs?filter=" + headsBranch + "&api-version=4.1", array,
                        JSONObject.class);
        log.info("项目{}创建分支返回{}", projectName, JSON.toJSONString(otherBranchResponse));
        // 修改原分支名称
        defaultRequest.put("sourceRefName", otherBranch);
        defaultRequest.put("title", "merge " + sourceBranch + " into " + targetBranch);
    }

}
