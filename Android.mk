LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := Nethunter
LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := Nethunter

nethunter_root  := $(LOCAL_PATH)
nethunter_out   := $(PWD)/$(OUT_DIR)/target/common/obj/APPS/$(LOCAL_MODULE)_intermediates
nethunter_build := $(nethunter_root)/build
nethunter_apk   := build/outputs/apk/Nethunter-release-unsigned.apk

$(nethunter_root)/$(nethunter_apk):
	rm -rf $(nethunter_build)
	mkdir -p $(nethunter_build)/outputs/apk
	mkdir -p $(nethunter_out)
	ln -sf $(nethunter_out) $(nethunter_build)
	cd $(nethunter_root) && gradle assembleRelease

LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_SRC_FILES := $(nethunter_apk)
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

include $(BUILD_PREBUILT)