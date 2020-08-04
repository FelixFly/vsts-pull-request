package top.felixfly.vsts.vstspullrequest.event;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 工作项事件
 *
 * @author FelixFly <chenglinxu@yeah.net>
 * @date 2020/4/17
 */
@Slf4j
public class WorkItemEvent {

    private final RestTemplate workItemRestTemplate;

    public WorkItemEvent(RestTemplate workItemRestTemplate) {
        this.workItemRestTemplate = workItemRestTemplate;
    }

    private static final String WORK_ITEM_SUFFIX = "api-version=4.1&$expand=relations";

    @Subscribe
    public void getWorkItem(Collection<String> workItemIds) {
        String nextLine = "\r\n";
        StringBuilder stringBuilder = new StringBuilder(256);
        stringBuilder.append("工作项标识 | 状态 | 接受人 | 工作项迭代 | 工作项类型 | 工作项标题 |  需求标识 | 需求标题 | 需求迭代 ")
                .append(nextLine);
        stringBuilder.append("------ | ------ | ------ | ------ | ------ | ------ | ------ | ------ | ------ ")
                .append(nextLine);

        String workContents = workItemIds.parallelStream().map(workItemId -> {
            try {
                JSONObject jsonObject = workItemRestTemplate
                        .getForObject("/" + workItemId.substring(1) + "?" + WORK_ITEM_SUFFIX, JSONObject.class);
                getWorkItemParent(jsonObject);

                return getData(jsonObject);
            } catch (Exception e) {
                return workItemId + "|------ | ------ | ------ | ------ | ------ | ------ | ------ | ------";
            }
        }).collect(Collectors.joining(nextLine));
        stringBuilder.append(workContents);
        System.out.println(stringBuilder.toString());
    }

    private void getWorkItemParent(JSONObject jsonObject) {
        JSONObject fields = jsonObject.getJSONObject("fields");
        if (!StringUtils.equals("任务", fields.getString("System.WorkItemType"))) {
            return;
        }
        // 获取对应的需求工作项
        getNeedWorkItem(jsonObject);
    }

    private void getNeedWorkItem(JSONObject jsonObject) {
        List<JSONObject> relations = jsonObject.getObject("relations", new TypeReference<List<JSONObject>>() {
        });
        Optional<JSONObject> rel = relations.stream()
                .filter(item -> StringUtils.equals("System.LinkTypes.Hierarchy-Reverse", item.getString("rel")))
                .findAny();
        if (!rel.isPresent()) {
            return;
        }
        String[] urls = rel.get().getString("url").split("/");
        String needWorkItem = urls[urls.length - 1];
        JSONObject needJsonObject = workItemRestTemplate
                .getForObject("/" + needWorkItem + "?" + WORK_ITEM_SUFFIX, JSONObject.class);
        getWorkItemParent(needJsonObject);
        JSONObject fields = needJsonObject.getJSONObject("fields");
        jsonObject.put("need.id", needJsonObject.getString("id"));
        jsonObject.put("need.Title", fields.getString("System.Title"));
        jsonObject.put("need.IterationPath", fields.getString("System.IterationPath"));
    }


    private String getData(JSONObject jsonObject) {
        JSONObject fields = jsonObject.getJSONObject("fields");
        return jsonObject.getString("id") + "|"
                + fields.getString("System.State") + "|"
                + fields.getString("System.AssignedTo") + "|"
                + fields.getString("System.IterationPath") + "|"
                + fields.getString("System.WorkItemType") + "|"
                + fields.getString("System.Title") + "|"
                + StringUtils.defaultIfEmpty(jsonObject.getString("need.id"), " ") + "|"
                + StringUtils.defaultIfEmpty(jsonObject.getString("need.Title"), " ") + "|"
                + StringUtils.defaultIfEmpty(jsonObject.getString("need.IterationPath"), " ");
    }
}
