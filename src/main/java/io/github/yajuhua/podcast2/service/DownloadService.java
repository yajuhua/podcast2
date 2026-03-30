package io.github.yajuhua.podcast2.service;

import io.github.yajuhua.podcast2.common.result.Result;
import io.github.yajuhua.podcast2.pojo.vo.DownloadConfVO;

public interface DownloadService {
    Result<DownloadConfVO> getDownloadConf(String uuid) throws Exception;
}
