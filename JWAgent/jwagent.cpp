#include "jvmti.h"
#include "jni.h"
#include "winsock2.h"
#include <stdio.h>
#include <string.h>

static SOCKET sock;
const int PORT = 5555;

#define JW_DISARMCLASS 100
#define JW_CLASSNOCHANGE 101
#define JW_CLASSMODIFIED 102
#define JW_QUIT 103


// Read numBytes from the socket into buf, blocks until finished
void recvNBytes(int numBytes, void* buf)
{
	int complete = 0, result;
	while (complete < numBytes)
	{
		result = recv(sock, (char*)buf + complete, numBytes - complete, 0);
		if (!result || result == SOCKET_ERROR)
		{
			printf("[!] Socket has been closed.\n");
			exit(-1);
		}
		complete += result;
	}
}

// Called by JVM each time a class is about to be loaded
void JNICALL ClassFileLoadHookCB(
			jvmtiEnv *jvmti_env,
            JNIEnv* jni_env,
            jclass class_being_redefined,
            jobject loader,
            const char* name,
            jobject protection_domain,
            jint class_data_len,
            const unsigned char* class_data,
            jint* new_class_data_len,
            unsigned char** new_class_data)
{
	unsigned int cmd, temp;
	printf("[+] Class Load: %s, size:%d\n", name, class_data_len);
	printf("[+] Sending class to JWServer...\n");

	cmd = JW_DISARMCLASS;
		
	temp = htonl(cmd);
	send(sock, (char*)&temp, 4, 0);
		
	temp = htonl(class_data_len);
	send(sock, (char*)&temp, 4, 0);

	send(sock, (char*)class_data, class_data_len, 0);

	recvNBytes(4, (char*)&temp);
	cmd = ntohl(temp);

	if (cmd == JW_CLASSMODIFIED)
	{
		recvNBytes(4, (char*)&temp);
		*new_class_data_len = ntohl(temp);
		jvmti_env->Allocate(*new_class_data_len, new_class_data);

		recvNBytes(*new_class_data_len, (char*)*new_class_data);
		printf("[+] Class has been disarmed, new size=%d\n", *new_class_data_len);
	}
	else if (cmd == JW_CLASSNOCHANGE)
	{
		printf("[+] Class has not been modified\n");
	}
	else if (cmd == JW_QUIT)
	{
		printf("[!] Recieved quit signal. Stopping trace\n");
		closesocket(sock);
		jvmti_env->DisposeEnvironment();
	}
}

// Establish a socket connection to JWServer
BOOL createSocket()
{
	WSADATA wsaData;

	// Initialize Winsock
    int iResult = WSAStartup(MAKEWORD(2,2), &wsaData);

    if (iResult != NO_ERROR)
        perror("Error at WSAStartup()\n");

	// Create a socket
    sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

    if (sock == INVALID_SOCKET) 
	{
        printf( "Error at socket(): %ld\n", WSAGetLastError() );
        WSACleanup();
        return FALSE;
    }

	// Connect to server
    sockaddr_in clientService;

    clientService.sin_family = AF_INET;
    clientService.sin_addr.s_addr = inet_addr("127.0.0.1");
    clientService.sin_port = htons(PORT);

    if (connect(sock, (SOCKADDR*)&clientService, sizeof(clientService)) == SOCKET_ERROR) 
	{
        printf("Failed to connect.\n" );
        WSACleanup();
        return FALSE;
    }

	send(sock, "JW-AGENT", 8, 0);

	char handshake[9] = {0};	
	recvNBytes(9, handshake);

	// Check for correct handshake from server
	return !strncmp(handshake, "JW-SERVER", 9);
}

/* 
Called when agent is loaded for first time
*/
JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved)
{
	jvmtiEnv *jvmti;
	printf("[*] JWAgent Initializing...\n");
	printf("[*] Trying to connect to JWServer at 127.0.0.1:%d\n", PORT);

	if (!createSocket()) 
	{
		printf("[!] Failed to establish connection.\n");
		return -1;
	}
	printf("[*] Connection established\n");

	// Get the jvmti environment pointer
	jint version  = vm->GetEnv((void**)&jvmti, JVMTI_VERSION_1_2);
	
	if (version != JVMTI_ERROR_NONE)
	{		
		printf("[!] GetEnv failed\n");
		return JVMTI_ERROR_INTERNAL;
	}

	jvmtiCapabilities cap = {0};
	jvmtiError err;
	
	// Get capabilities offerred by the VM
	err = jvmti->GetPotentialCapabilities(&cap);
	if (err != JVMTI_ERROR_NONE)
	{
		perror("[!] GetPotentialCapabilities failed");
		return JVMTI_ERROR_INTERNAL;
	}
	
	if (!cap.can_generate_all_class_hook_events)
	{
		perror("[!] JVM does not support class hook events");
		return JVMTI_ERROR_INTERNAL;
	}
	
	if (!cap.can_retransform_classes)
	{
		perror("[!] JVM does not support class retransfomrmation events");
		return JVMTI_ERROR_INTERNAL;
	}
		
	memset(&cap, 0, sizeof(jvmtiCapabilities));
	cap.can_generate_all_class_hook_events = 1;
	//cap.can_retransform_classes = 1;
	//cap.can_retransform_any_class = 1;

	// Add the capability
	if (JVMTI_ERROR_NONE != jvmti->AddCapabilities(&cap))
	{
		perror("[!] AddCapabilities failed");
		return JVMTI_ERROR_INTERNAL;
	}
	
	jvmtiEventCallbacks callback = {0};
	callback.ClassFileLoadHook = &ClassFileLoadHookCB;	

	if (JVMTI_ERROR_NONE != jvmti->SetEventCallbacks(&callback, sizeof(jvmtiEventCallbacks)))
	{
		perror("[!] SetEventCallbacks failed");
		return JVMTI_ERROR_INTERNAL;
	}

	if (JVMTI_ERROR_NONE != jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, NULL))
	{
		perror("[!] SetEventNotificationMode failed");
		return JVMTI_ERROR_INTERNAL;
	}

	return JVMTI_ERROR_NONE;
}