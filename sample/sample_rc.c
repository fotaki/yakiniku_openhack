#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>

void err_func(char *msg)
{
perror(msg);
exit(EXIT_FAILURE);
}

//not use
pthread_mutex_t mutex;
pthread_t thread_id;
pthread_cond_t cond_end;

void *dataupdate(void*);

//アクセル値・ハンドル値
//pwm[0] アクセル値
//pwm[1] ハンドル値
unsigned char pwm[] = {0,0,0,0,0,0};



int end = 0;

int main(int argc, char **argv)
{
    int sockfd, len;
    char buf[BUFSIZ];
    struct sockaddr_in serv;
    unsigned short port;
    int i,j; 
    unsigned char tmp;
    int n;
    unsigned char senddata[] = {
                 0x55,0x00,0x0b,0x00,   // header
                 0x50,                  // accel data
                 0x50,                  // handle data
                 0x00,0x00,0x00,0x00,   // not use
                 0x00};                 // parity
                 

    // not use
    pthread_mutex_init(&mutex, NULL);
    pthread_cond_init(&cond_end, NULL);
    
    if(pthread_create(&thread_id, NULL,
       dataupdate, (void *)&thread_id) < 0){
        perror("pthread_create error");
        exit(1);
    }
    
    // make socket
    if((sockfd = socket(PF_INET, SOCK_STREAM, 0)) < 0)
    err_func("socket");
    serv.sin_family = PF_INET;
    
    // port
    port = 8899;
    serv.sin_port = htons(port);
    
    // ip
    inet_aton("10.10.100.254", &(serv.sin_addr));

    if(connect(sockfd, (struct sockaddr *)&serv, sizeof(struct sockaddr_in)) < 0)
    err_func("connect");

    // 常にパケットを送り続ける．
    // 今は，256回送ったら停止
    // データのアップデートは下記のdataupdateスレッドで実行
    // 
    for(i=0;i<256;i++){
        printf("times %d\n",i);
        tmp = 0;
        
//        pthread_mutex_lock(&mutex);
        senddata[4]=pwm[0];
        senddata[5]=pwm[1];
        senddata[6]=pwm[2];
        senddata[7]=pwm[3];
//        pthread_mutex_unlock(&mutex);
        
        //　パリティ計算
        for(j=0;j<10;j++){
        tmp = tmp + senddata[j];
        printf("%d tmp %d\n",j,tmp);
        senddata[10] = tmp;
        }
        
        //データ送信
        len = sizeof(senddata);
        printf("data len = %d\n",len);
        send(sockfd, senddata, len, 0);
        usleep(50*1000);
    }
    
    end = 1;
    
    //終了操作
    senddata[4]=50;
    senddata[5]=50;
    senddata[6]=50;
    senddata[7]=50;
    send(sockfd, senddata, len, 0);
    usleep(50*1000);
        
//    pthread_cond_signal(&cond_end);

    if(pthread_join(thread_id, NULL) < 0){
        perror("pthread_join error");
        exit(1);
    } 

    return 0;
}


void *dataupdate(void *arg){
    int i = 0;
   
    
    char a[20];
    
    // 停止
    pwm[0] = (char)(50);
    scanf("%s",&a);

    // 前進
    pwm[0] = (char)(60);
    scanf("%s",&a);
      
   // 停止
    pwm[0] = (char)(50);
    
    usleep(2000*1000);   
   
    while(1){
        i++;
        printf("##############\n");
        pwm[0] = (char)(50);
        pwm[1] = (char)(50);
        if(end == 1){
          printf("end th\n");
          pthread_exit(0);
        }
        usleep(50*1000);
    }
    

}