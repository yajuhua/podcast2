package com.podcast.pojo;

import java.util.List;

/**
 * 获取系统信息
 */
public class SystemInfo {
        private String systemRuntime;
        private String systemVersion;
        private String systemUpdate;
        private String systemCode;
        private List<Plugin> pluginList;

        public SystemInfo() {
        }

        public SystemInfo(String systemRuntime, String systemVersion, String systemUpdate, String systemCode, List<Plugin> pluginList) {
                this.systemRuntime = systemRuntime;
                this.systemVersion = systemVersion;
                this.systemUpdate = systemUpdate;
                this.systemCode = systemCode;
                this.pluginList = pluginList;
        }

        public String getSystemRuntime() {
                return systemRuntime;
        }

        public void setSystemRuntime(String systemRuntime) {
                this.systemRuntime = systemRuntime;
        }

        public String getSystemVersion() {
                return systemVersion;
        }

        public void setSystemVersion(String systemVersion) {
                this.systemVersion = systemVersion;
        }

        public String getSystemUpdate() {
                return systemUpdate;
        }

        public void setSystemUpdate(String systemUpdate) {
                this.systemUpdate = systemUpdate;
        }

        public String getSystemCode() {
                return systemCode;
        }

        public void setSystemCode(String systemCode) {
                this.systemCode = systemCode;
        }

        public List<Plugin> getPluginList() {
                return pluginList;
        }

        public void setPluginList(List<Plugin> pluginList) {
                this.pluginList = pluginList;
        }

        @Override
        public String toString() {
                return "SystemInfo{" +
                        "systemRuntime='" + systemRuntime + '\'' +
                        ", systemVersion='" + systemVersion + '\'' +
                        ", systemUpdate='" + systemUpdate + '\'' +
                        ", systemCode='" + systemCode + '\'' +
                        ", pluginList=" + pluginList +
                        '}';
        }
}
