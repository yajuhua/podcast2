package io.github.yajuhua.podcast2.pojo.entity;

import lombok.*;

import java.util.List;

/**
 * 地址过滤
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class AddressFilter {
    private List<String> whitelist;
    private List<String> blacklist;
}
