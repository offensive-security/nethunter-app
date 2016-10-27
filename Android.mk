LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := Nethunter-Terminal-Emulator
LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := Nethunter-Terminal-Emulator

nhterm_root  := $(LOCAL_PATH)
nhterm_dir   := app
nhterm_out   := $(PWD)/$(OUT_DIR)/target/common/obj/APPS/$(LOCAL_MODULE)_intermediates
nhterm_build := $(nhterm_root)/$(nhterm_dir)/build
nhterm_apk   := build/outputs/apk/$(nhterm_dir)-release-unsigned.apk

$(nhterm_root)/$(nhterm_dir)/$(nhterm_apk):
	rm -Rf $(nhterm_build)
	mkdir -p $(nhterm_out)
	ln -sf $(nhterm_out) $(nhterm_build)
	cd $(nhterm_root)/$(nhterm_dir) && gradle assembleRelease

LOCAL_CERTIFICATE := platform
LOCAL_SRC_FILES := $(nhterm_dir)/$(nhterm_apk)
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)

include $(BUILD_PREBUILT)