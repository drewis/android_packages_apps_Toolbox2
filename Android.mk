LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#LOCAL_JAVA_LIBRARIES := bouncycastle
#LOCAL_STATIC_JAVA_LIBRARIES := guava

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES += android-support-v13 android-support-v4

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := EVToolbox2
LOCAL_CERTIFICATE := platform

include $(BUILD_PACKAGE)
