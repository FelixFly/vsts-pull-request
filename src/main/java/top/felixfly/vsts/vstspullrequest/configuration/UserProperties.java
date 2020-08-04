package top.felixfly.vsts.vstspullrequest.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *  用户配置信息
 *
 * @author FelixFly <chenglinxu@yeah.net>
 * @date 2020/3/18
 */
@Getter
@Setter
@NoArgsConstructor
public class UserProperties {
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * 用户密码
     */
    private String secret;
}
