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
#include <dlfcn.h>


#define TAG "DEX_DUMP"

int isArt();

void getProcessName(int pid, char *name, int len);

void dumpFileName(char *name, int len, const char *pname, int dexlen);

static char pname[256];

int isArt() {
    char version[10];
    __system_property_get("ro.build.version.sdk", version);
    __android_log_print(ANDROID_LOG_INFO, TAG, "api level %s", version);
    int sdk = atoi(version);
    if (sdk >= 21) {
        return 1;
    }
    return 0;
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

void hook() {
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

JNIEXPORT void JNICALL Java_com_smartdone_dexdump_Dumpper_dump(JNIEnv *env, jclass clazz) {
    getProcessName(getpid(), pname, sizeof(pname));
    if (isArt()) {
        hook();
    }
}
