#
# Copyright (C) 2015 fattire <f4ttire@gmail.com>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)
LOCAL_SRC_PATH := $(LOCAL_PATH)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_CERTIFICATE := platform

#
# Since NH uses the BuildConfig.java that is built via gradle, and since AOSP's
# build system doesn't create Buildconfig.java, we have to do it manually here
# in the Makefile
#
# IOW, this is a hacky cheat to emulate Android Studio's behavior.  It could be
# useful behind this particular app (NH).

BC_VERSION_CODE := 1
BC_VERSION_NAME := "1.0"
BC_APPLICATION_ID := "com.offsec.nethunter"
LOCAL_BUILDCONFIG_CLASS := src/com/offsec/nethunter/BuildConfig.java

$(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS): FORCE
	case $(TARGET_BUILD_VARIANT) in \
	  userdebug) $(eval BC_DEBUG_STATUS="true") ;; \
	          *) $(eval BC_DEBUG_STATUS="false") ;; \
	esac
	echo "/**" > $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo "* Automatically generated file. DO NOT MODIFY" >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo "*/" >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo "package com.offsec.nethunter;" >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo "public final class BuildConfig {" >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo "  public static final boolean DEBUG = "$(BC_DEBUG_STATUS)";" >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo "  public static final String APPLICATION_ID = \""$(BC_APPLICATION_ID)"\";" >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo "  public static final String BUILD_TYPE = \"release\";" >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo "  public static final String FLAVOR = \"\";" >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo "  public static final int VERSION_CODE = "$(BC_VERSION_CODE)";" >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo "  public static final String VERSION_NAME = \""$(BC_VERSION_NAME)"\";" >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo "  public static final String BUILD_NAME = \""$(USER)"\";" >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo -n "  public static final java.util.Date BUILD_TIME = new java.util.Date((long) " >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	printf '%(%s)T' -1 >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo " * 1000);" >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
	echo "}" >> $(LOCAL_SRC_PATH)/$(LOCAL_BUILDCONFIG_CLASS)
FORCE:

LOCAL_SRC_FILES := \
  $(call all-java-files-under, src) \
  $(LOCAL_BUILDCONFIG_CLASS)

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4

LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PACKAGE_NAME := Nethunter

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
