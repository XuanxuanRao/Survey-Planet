package org.example.surveyplanet.pojo.entity.survey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Survey {
    @Getter
    public enum Status {
        /**
         * allow user to fill in
         */
        OPEN,
        /**
         * not allow user to fill in
         */
        CLOSE,
        /**
         * deleted by creator
         */
        DELETE;

        private final String value = this.name().toLowerCase();

        public static Status fromString(String status) {
            for (Status s : Status.values()) {
                if (s.name().equalsIgnoreCase(status)) {
                    return s;
                }
            }
            throw new IllegalArgumentException(
                    Status.class.getName() + ".fromString() error: unknown status: " + status
            );
        }
    }

    private Integer sid;                // 问卷id
    private Integer uid;                // 创建用户id
    private Status status;              // 问卷状态
    private String title;               // 问卷标题
    private String description;         // 问卷描述(可选项，默认为空)
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间(上一次用户填写时间)
    private LocalDateTime openTime;     // 上一次开放时间
    private Integer fillNum;            // 填写人数
}
