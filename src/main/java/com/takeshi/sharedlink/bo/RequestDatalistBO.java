package com.takeshi.sharedlink.bo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * RequestDatalistBO
 *
 * @author 七濑武【Nanase Takeshi】
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestDatalistBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Map<String, Object> shareInfo;

    private List<Map<String, Object>> dataList;

    public RequestDatalistBO(Map<String, Object> shareInfo, List<Map<String, Object>> dataList) {
        this.shareInfo = shareInfo;
        this.dataList = dataList;
    }

    public Map<String, Object> getShareInfo() {
        return shareInfo;
    }

    public void setShareInfo(Map<String, Object> shareInfo) {
        this.shareInfo = shareInfo;
    }

    public List<Map<String, Object>> getDataList() {
        return dataList;
    }

    public void setDataList(List<Map<String, Object>> dataList) {
        this.dataList = dataList;
    }

}
