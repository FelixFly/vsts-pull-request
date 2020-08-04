package top.felixfly.vsts.vstspullrequest.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 资源信息
 *
 * @author FelixFly <chenglinxu@yeah.net>
 * @date 2020/3/18
 */
public class RepositoryConstants {

    /**
     * http://tfs2018-web.winning.com.cn:8080/tfs/WINNING-6.0/_apis/git/repositories?api-version=4.1
     */
    public static final Map<String, String> REPOSITORY_MAP = new HashMap<String, String>() {
        {
            put("winning-finance-base", "e7252110-3a74-4482-bf82-42a4373d6f8a");

            put("winning-mds-finance", "fd11337b-8035-4763-b4ea-57d94145c6d0");
            put("winning-mas-finance", "94a712cc-1877-4abd-9dee-aaa614bb34b4");

            put("winning-bmts-finance-fee", "6105d038-de8f-48de-9e29-d145345a56c0");

            put("winning-amts-finance-fee", "31e1653e-0a28-4bc8-a252-ae4096d46c34");
            put("winning-amts-finance-account", "e8648188-b3e1-46e9-86e3-7f91cafb9175");

            put("winning-bas-cis-outpatient-finance", "8d5449ff-06fd-460a-afc8-7650700bf358");
            put("winning-bas-finance-fee-outp", "b8f1b651-3af3-4adb-acb3-1772f241f707");
            put("winning-bas-finance-common", "e2176c64-579b-427b-aa73-c00f2f1ab638");


            put("winning-amts-finance-fee-inp", "30245912-d0c1-464f-ad55-40c1aad5c468");


            put("winning-bmts-finance-invoice", "c6b47688-cc3b-40a5-9afa-2541bda90fa4");
            put("winning-bas-finance-account", "f1a6f162-6eb5-4718-9f6e-35dbd0dc3f32");


            put("winning-bas-finance-fee-inpatient", "5706ea63-276f-4522-8f9e-4d868167e9f5");

            put("winning-bmts-finance-account", "52f8ae15-f03e-420b-9ecb-5941f2fac69c");


        }
    };
}
