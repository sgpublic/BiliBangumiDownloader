package io.github.sgpublic.bilidownload.core.forest.data;

import io.github.sgpublic.bilidownload.base.forest.DataResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserInfoResp extends DataResp<UserInfoResp.UserInfo> {
    @Data
    public static class UserInfo {
        private int mid;
        private String name;
        private String sign;
        private String face;
        private int sex;
        private int level;
        private Vip vip = new Vip();

        @Data
        public static class Vip {
            private int type;
            private int status;
            private Label label = new Label();

            @Data
            public static class Label {
                private String text;
            }
        }
    }
}
