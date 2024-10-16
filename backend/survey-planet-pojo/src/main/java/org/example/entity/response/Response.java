package org.example.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    /**
     * 作答id, 作答的唯一标识, Primary Key in DB
     */
    private Long rid;
    /**
     * 回答的问卷 id
     */
    private Long sid;
    /**
     * 回答用户 id
     */
    private Long uid;
    /**
     * 首次回答时间（提交时间）
     */
    private LocalDateTime createTime;
    /**
     * 最近一次回答时间（提交时间）
     */
    private LocalDateTime updateTime;
    /**
     * 回答内容
     */
    private List<ResponseItem> items;
}
