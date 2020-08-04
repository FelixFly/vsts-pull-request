package top.felixfly.vsts.vstspullrequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import top.felixfly.vsts.vstspullrequest.configuration.PullRequestProperties;
import top.felixfly.vsts.vstspullrequest.git.PullRequestService;

import java.util.Arrays;
import java.util.List;

/**
 * 应用启动类
 *
 * @author FelixFly <chenglinxu@yeah.net>
 */
@SpringBootApplication
public class VstsPullRequestApplication implements CommandLineRunner {

    @Autowired
    private List<PullRequestService> pullRequestServices;

    @Autowired
    private PullRequestProperties pullRequestProperties;

    public static void main(String[] args) {
        new SpringApplicationBuilder().sources(VstsPullRequestApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .web(WebApplicationType.NONE)
                .run(args);
    }


    @Override
    public void run(String... args) throws Exception {
        String targetBranch = this.pullRequestProperties.getTargetBranch();
        String sourceBranch = this.pullRequestProperties.getSourceBranch();
        String projectName = this.pullRequestProperties.getProjectName();
        // 若是项目名称是多个考虑并发
        String[] projectNames = StringUtils.split(projectName, ",");
        String[] targetBranches = StringUtils.split(targetBranch, ",");
        String[] sourceBranches = StringUtils.split(sourceBranch, ",");
        if (targetBranches.length > 1 && sourceBranches.length > 1) {
            throw new RuntimeException("源分支和目标分支不能同时为多个");
        }
        if (projectNames.length > 1 && (targetBranches.length > 1 || sourceBranches.length > 1)) {
            throw new RuntimeException("项目名称和源分支(目标分支)不能同时为多个");
        }
        Arrays.stream(projectNames)
                .filter(StringUtils::isNotBlank)
                .forEach(itemName -> {
                    if (targetBranches.length > 1) {
                        processMoreTargetBranch(sourceBranch, targetBranches, itemName);
                    }
                    if (sourceBranches.length > 1) {
                        processMoreSourceBranch(sourceBranches, targetBranch, itemName);
                    }
                    PullRequestService pullRequestService = this.pullRequestServices.stream()
                            .filter(item -> item.isSupport(targetBranch))
                            .findAny()
                            .orElseThrow(() -> new RuntimeException("没有对应的分支" + targetBranch + "处理"));
                    pullRequestService.pullRequest(itemName, sourceBranch, targetBranch);
                });
    }

    private void processMoreSourceBranch(String[] sourceBranches, String targetBranch, String itemName) {
        Arrays.stream(sourceBranches).filter(StringUtils::isNotBlank)
                .forEach(source -> {
                    PullRequestService pullRequestService = this.pullRequestServices.stream()
                            .filter(item -> item.isSupport(targetBranch))
                            .findAny()
                            .orElseThrow(() -> new RuntimeException("没有对应的分支" + targetBranch + "处理"));
                    pullRequestService.pullRequest(itemName, source, targetBranch);
                });
    }

    private void processMoreTargetBranch(String sourceBranch, String[] targetBranches, String itemName) {
        Arrays.stream(targetBranches).filter(StringUtils::isNotBlank)
                .parallel()
                .forEach(target -> {
                    PullRequestService pullRequestService = this.pullRequestServices.stream()
                            .filter(item -> item.isSupport(target))
                            .findAny()
                            .orElseThrow(() -> new RuntimeException("没有对应的分支" + target + "处理"));
                    pullRequestService.pullRequest(itemName, sourceBranch, target);
                });
    }
}
