package io.github.yajuhua.podcast2.pojo.entity;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DataExport {
    private Sub sub;
    private List<Extend> extendList;
}
