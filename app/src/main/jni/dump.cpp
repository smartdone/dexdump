//
// Created by 袁东明 on 2017/7/1.
//

extern "C" {
#include "include/inlineHook.h"
}

#include "dump.h"
#include <unistd.h>
#include <android/log.h>
#include <sys/system_properties.h>
#include <stdlib.h>
#include <fcntl.h>
#include <time.h>
#include <string>
#include <dlfcn.h>


#define TAG "DEX_DUMP"

int apiLevel();

void getProcessName(int pid, char *name, int len);

void dumpFileName(char *name, int len, const char *pname, int dexlen);

static char pname[256];

int apiLevel() {
    char version[10];
    __system_property_get("ro.build.version.sdk", version);
    __android_log_print(ANDROID_LOG_INFO, TAG, "api level %s", version);
    int sdk = atoi(version);
    return sdk;
}

void getProcessName(int pid, char *name, int len) {
    int fp = open("/proc/self/cmdline", O_RDONLY);
    memset(name, 0, len);
    read(fp, name, len);
    close(fp);
}

void dumpFileName(char *name, int len, const char *pname, int dexlen) {
    time_t now;
    struct tm *timenow;
    time(&now);
    timenow = localtime(&now);
    memset(name, 0, len);
    sprintf(name, "/data/data/%s/dump_size_%u_time_%d_%d_%d_%d_%d_%d.dex", pname, dexlen,
            timenow->tm_year + 1900,
            timenow->tm_mon + 1,
            timenow->tm_mday,
            timenow->tm_hour,
            timenow->tm_min,
            timenow->tm_sec);
}

void writeToFile(const char *pname, u_int8_t *data, size_t length) {
    char dname[1024];
    dumpFileName(dname, sizeof(dname), pname, length);
    __android_log_print(ANDROID_LOG_ERROR, TAG, "dump dex file name is : %s", dname);
    __android_log_print(ANDROID_LOG_ERROR, TAG, "start dump");
    int dex = open(dname, O_CREAT | O_WRONLY, 0644);
    if (dex < 0) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "open or create file error");
        return;
    }
    int ret = write(dex, data, length);
    if (ret < 0) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "write file error");
    } else {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "dump dex file success `%s`", dname);
    }
    close(dex);
}

art::DexFile *(*old_openmemory)(const byte *base, size_t size, const std::string &location,
                                uint32_t location_checksum, art::MemMap *mem_map,
                                const art::OatDexFile *oat_dex_file, std::string *error_msg) = NULL;

art::DexFile *new_openmemory(const byte *base, size_t size, const std::string &location,
                             uint32_t location_checksum, art::MemMap *mem_map,
                             const art::OatDexFile *oat_dex_file, std::string *error_msg) {
    __android_log_print(ANDROID_LOG_ERROR, TAG, "art::DexFile::OpenMemory is called");
    writeToFile(pname, (uint8_t *) base, size);
    return (*old_openmemory)(base, size, location, location_checksum, mem_map, oat_dex_file,
                             error_msg);
}


art::DexFile *(*old_openmemory_23)(void* DexFile_thiz,char* base,int size,void* location,
                                   void* location_checksum,void* mem_map,void* oat_dex_file,void* error_meessage) = NULL;

art::DexFile *new_openmemory_23(void* DexFile_thiz,char* base,int size,void* location,
                                void* location_checksum,void* mem_map,void* oat_dex_file,void* error_meessage) {
    writeToFile(pname, (u_int8_t *)base, size);
    return (*old_openmemory_23)(DexFile_thiz, base, size, location, location_checksum, mem_map,
                                oat_dex_file, error_meessage);
}

DexFile* (*old_dexFileParse)(const u1 *data, size_t length, int flags) = NULL;

DexFile* new_dexFileParse(const u1 *data, size_t length, int flags) {
    writeToFile(pname, (u_int8_t *)data, length);
    return (*old_dexFileParse)(data, length, flags);
}

DvmDex* (*old_dvmDexFileOpenPartial)(const void* addr, int len, DvmDex** ppDvmDex) = NULL;

DvmDex* new_dvmDexFileOpenPartial(const void* addr, int len, DvmDex** ppDvmDex) {
    writeToFile(pname, (u_int8_t *)addr, len);
    return (*old_dvmDexFileOpenPartial)(addr, len, ppDvmDex);
}

void hook_dvm() {
    void *handle = dlopen("libdvm.so", RTLD_GLOBAL | RTLD_LAZY);
    if(handle == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Error: unable to find the SO : libdvm.so");
        return;
    }
    void *addr = dlsym(handle, "_Z12dexFileParsePKhji");
    if(addr == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Unable find symbol _Z12dexFileParsePKhji");
        return;
    }
    if (registerInlineHook((uint32_t) addr, (uint32_t) new_dexFileParse,
                           (uint32_t **) &old_dexFileParse) != ELE7EN_OK) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "register hook failed");
        return;
    }

    if (inlineHook((uint32_t) addr) != ELE7EN_OK) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "register hook failed");
        return;
    }

    addr = dlsym(handle, "_Z21dvmDexFileOpenPartialPKviPP6DvmDex");
    if(addr == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Unable find symbol _Z21dvmDexFileOpenPartialPKviPP6DvmDex");
        return;
    }
    if (registerInlineHook((uint32_t) addr, (uint32_t) new_dvmDexFileOpenPartial,
                           (uint32_t **) &old_dvmDexFileOpenPartial) != ELE7EN_OK) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "register hook failed");
        return;
    }

    if (inlineHook((uint32_t) addr) != ELE7EN_OK) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "register hook failed");
        return;
    }

}

void hook_21_22() {
    void *handle = dlopen("libart.so", RTLD_GLOBAL | RTLD_LAZY);
    if (handle == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Error: unable to find the SO : libart.so");
        return;
    }
    void *addr = dlsym(handle,
                       "_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_");
    if (addr == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, TAG,
                            "Error: unable to find the Symbol : _ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_");
        return;
    }

    if (registerInlineHook((uint32_t) addr, (uint32_t) new_openmemory,
                           (uint32_t **) &old_openmemory) != ELE7EN_OK) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "register hook failed");
        return;
    }

    if (inlineHook((uint32_t) addr) != ELE7EN_OK) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "register hook failed");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, TAG, "register hook success");
}

void hook_23() {
    void *handle = dlopen("libart.so", RTLD_GLOBAL | RTLD_LAZY);
    if (handle == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Error: unable to find the SO : libart.so");
        return;
    }
    void *addr = dlsym(handle,
                       "_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_");
    if (addr == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, TAG,
                            "Error: unable to find the Symbol : _ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_10OatDexFileEPS9_");
        return;
    }

    if (registerInlineHook((uint32_t) addr, (uint32_t) new_openmemory_23,
                           (uint32_t **) &old_openmemory_23) != ELE7EN_OK) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "register hook failed");
        return;
    }

    if (inlineHook((uint32_t) addr) != ELE7EN_OK) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "register hook failed");
        return;
    }

    __android_log_print(ANDROID_LOG_INFO, TAG, "register hook success");
}

JNIEXPORT void JNICALL Java_com_smartdone_dexdump_Dumpper_dump(JNIEnv *env, jclass clazz) {
    getProcessName(getpid(), pname, sizeof(pname));
    int api = apiLevel();
    if(api < 21){
        hook_dvm();
    } else if(api < 23) {
        hook_21_22();
    } else {
        hook_23();
    }
}
