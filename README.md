
간단한 채팅 서버를 만든 후, 성능을 측정하고, 개선해보는 프로젝트입니다.<br><br>
--- 
### 메시지 구조
맨 앞부터의 메시지 구성 요소를 설명합니다.<br><br>

- 2Bytes : 메시지 본문(payload)의 길이<br>
- 2Bytes : 메시지를 송신한 쓰레드의 Number<br>
- ?Bytes : 메시지 본문<br>


---
### SingleThreadVersion 서버 구조
싱글스레드 서버는 다음 요소들로 구성됩니다.

- Listening<br><br>
&nbsp;서버에 들어오는 연결 요청을 기다립니다. 연결이 맺어질 경우, SocketStation과 SocketBuffer에 해당 소켓 채널을 등록합니다.<br><br>
- SocketStation<br><br>
&nbsp;Listening 클래스가 연결 맺은 소켓 채널을 Selector에 등록하며, 연결된 모든 소켓 채널을 리스트의 형태로 관리합니다.<br><br>
- SocketBuffer<br><br>
&nbsp;Listening 클래스가 연결 맺은 소켓 채널을 내부적으로 생성한 ByteBuffer와 하나씩 짝지어 관리합니다. 이때, Map 자료구조를 이용하여 보관합니다.<br><br>

- Sending<br><br>
&nbsp;서버의 핵심 부분입니다.&nbsp;SocketStation으로부터, 메시지가 들어온 소켓 채널을 짝지어진 ByteBuffer로 read합니다. 그 후, 메시지를 SocketStation에서 관리하는 모든 소켓 채널에 송신합니다.<br><br>

---
### 그 외 요소
- DummyClient / DummyProcess / DummyThread<br><br>
&nbsp;DummyClient의 메인 메소드를 실행시키면, DummyProcess를 생성하고, DummyProcess는 DummyThread를 생성합니다. 이를 이용하여, 프로세스의 개수와 각 프로세스와 짝지어진 쓰레드의 개수를 조절할 수 있습니다. 또한, DummyThread는 서버에 과부하를 주는 역할을 수행합니다.<br><br>
- MainClient<br><br>
&nbsp;실질적인 성능 측정을 담당하는 클라이언트입니다. 서버에게 메시지를 보내고 받는 시간 간격차를 Timer를 사용하여 측정하고, 파일로 기록합니다.<br><br>
- Timer<br><br>
&nbsp;측정 시작 값과 끝 값의 차를 토대로 여러 정보로 보관하는 조잡한 타이머입니다.

---
### 싱글쓰레드 서버 성능 측정

###### 서버와 클라의 실행 환경에 따라 결과가 달라질 수 있습니다.<br><br>

#### 측정 방법<br>
&nbsp;실행할 프로세스의 개수 / 각 프로세스마다의 쓰레드 개수 / 각 쓰레드가 1초에 보낼 메시지의 Bytes 를 설정하여 DummyClient를 먼저 실행하고, 그 후 MainClient를 실행하여 시간을 측정합니다. MainClient가 종료하면 측정 결과를 result.txt가 현재 디렉토리에 생성됩니다.<br><br>

- 프로세스 4개 / 프로세스당 쓰레드 50개 / 각 쓰레드가 1초에 104Bytes(100 + 4) 전송<br>

https://github.com/bcy1235/ChatServer-Practice/assets/96825479/4896129f-21b0-433f-a08d-f5befe1e965f

##### (결과)<br>
Sum : 22.603033(sec)<br>
Count : 67<br>
MaxTime : 0.566161(sec)<br>
MinTime : 0.175369(sec)<br>
Average : 0.337359(sec)<br><br>


- 프로세스 5개 / 프로세스당 쓰레드 50개 / 1초당 104Bytes 전송<br>

https://github.com/bcy1235/ChatServer-Practice/assets/96825479/ae664a9c-9bf2-4752-9a60-ce87ebc91593

##### (결과)<br>
Sum : 134.552401(sec)<br>
Count : 49<br>
MaxTime : 7.015459(sec)<br>
MinTime : 0.008106(sec)<br>
Average : 2.745967(sec)<br><br>

평균 응답시간이 1초가 넘어감으로, 싱글쓰레드 서버의 한계는 이정도라고 볼 수 있을 것 같습니다. 멀티쓰레드 서버 구조로 개선하여, 한번 비교 해보겠습니다.

---

### MultiThreadVersion 서버 구조
SingleThreadVersion과의 차이점을 중심으로 설명하겠습니다.

- Reading<br><br>
&nbsp;기존 싱글 쓰레드 버전의 경우 Sending 클래스가 메시지를 읽고, 읽은 메시지를 연결된 모든 소켓 채널에 뿌렸습니다.<br>
&nbsp;하지만, 이 버전의 서버에서는 메시지를 읽는 쓰레드와 읽은 메시지를 뿌리는 쓰레드를 분리합니다.<br>
&nbsp;그 이유는, 메시지를 뿌리는 과정에 걸리는 시간이 연결된 소켓 채널의 수가 늘어남에 따라 선형적으로 증가하기 때문입니다.<br>
&nbsp;메시지를 읽는 과정도 여러 쓰레드로 만들면 좋겠지만, 일단 메시지를 보내는 과정만 멀티쓰레드로 변경하였습니다.<br><br>
- ProcessingThreadPool<br><br>
&nbsp;메시지를 뿌리는 쓰레드를 담고있는 쓰레드 풀입니다. 쓰레드 풀에 담긴 쓰레드들의 초기 상태는 wait 상태입니다.<br><br>
- TaskQueue<br><br>
&nbsp;읽은 메시지, 즉 뿌려야할 메시지를 담고있는 리스트를 추상화한 클래스입니다.<br><br>
- Detecting<br><br>
&nbsp;Reading 쓰레드가 생성하는 Detecting 쓰레드입니다. TaskQueue에 값이 존재하는지 검사하여, 값이 존재한다면 쓰레드 풀을 가동시킵니다.<br><br>
- Processing<br><br>
&nbsp;현재 TaskQueue에 존재하는 메시지를 뿌리는 임무를 부여받은 쓰레드입니다. 항상 wait 상태로 대기하며, 외부에서 notify 할 경우 작동됩니다.<br><br>

---
### MultiThreadVersion 성능 측정

기존 싱글쓰레드 버전에서는 프로세스 5개 / 각 쓰레드 50개 / 104bytes 조건에서 응답 평균시간이 1초를 넘어가는 모습을 보였었습니다.<br><br>
멀티쓰레드 버전에서는 쓰레드 풀의 크기를 달리하여, 성능을 측정해보겠습니다.<br><br>

&nbsp;쓰레드 풀의 크기가 어떤 영향을 미칠지는 절대적으로 알 수 없지만, 그럼에도 제 생각을 적어보자면.<br>

- 쓰레드 풀의 크기는 서버가 실행되는 pc의 코어의 개수와 동시에 실행되고 있는 여러 프로세스의 쓰레드들에 영향을 받을 것이다.
- 쓰레드 풀의 크기가 현재 점유 가능한 코어의 개수보다 너무 크다면, 컨텍스트 스위칭에 드는 비용이 상대적으로 증가할 것입니다.
- 반대로 쓰레드 풀의 크기가 너무 작다면, 코어를 완전히 사용하지 못해 효율적인 서버라고 보기 힘들것입니다.<br>

따라서, 개수를 적절하게 조절하여 현재 실행 환경에 맞는 최적의 개수를 찾는 것이 중요하다고 생각됩니다.<br>


- 쓰레드 풀 크기 : 5
- 프로세스 10개 / 각 쓰레드 50개 / 1초에 104bytes 전송
  
https://github.com/bcy1235/ChatServer-Practice/assets/96825479/7beff7e3-f6b5-4e4f-9ed8-64c02539bcd0

Sum : 336.955133(sec)<br>
Count : 39<br>
MaxTime : 16.597610(sec)<br>
MinTime : 0.464432(sec)<br>
Average : 8.639875(sec)<br><br>

(영상 생략)<br>
- 쓰레드 풀 크기 : 10
- 프로세스 10개 / 각 쓰레드 50개 / 1초에 104bytes 전송
  
Sum : 87.897608(sec)<br>
Count : 38<br>
MaxTime : 4.231894(sec)<br>
MinTime : 0.229595(sec)<br>
Average : 2.313095(sec)<br><br>

- 쓰레드 풀 크기 : 20
- 프로세스 10개 / 각 쓰레드 50개 / 1초에 104bytes 전송

Sum : 0.530368(sec)<br>
Count : 55<br>
MaxTime : 0.011398(sec)<br>
MinTime : 0.007019(sec)<br>
Average : 0.009643(sec)<br><br>

- 쓰레드 풀 크기 : 20
- 프로세스 14개 / 각 쓰레드 50개 / 1초에 104bytes 전송

Sum : 114.865532(sec)<br>
Count : 39<br>
MaxTime : 5.097860(sec)<br>
MinTime : 0.382470(sec)<br>
Average : 2.945270(sec)<br><br>


- 쓰레드 풀 크기 : 30
- 프로세스 14개 / 각 쓰레드 50개 / 1초에 104bytes 전송

Sum : 1.785475(sec)<br>
Count : 42<br>
MaxTime : 0.319820(sec)<br>
MinTime : 0.008979(sec)<br>
Average : 0.042511(sec)<br><br>

---
### 문제 발생

멀티쓰레드 버전 서버 성능 측정 도중, 갑자기 MainClient가 멈추는 현상 발생.<br>

작업관리자로 확인해보니, 서버의 메모리가 계속해서 증가(대략 1초에 MB 단위)하는 현상 관찰. 인텔리제이로 디버깅 모드를 실행한 다음, 메모리가 어느정도 증가했을때의 상태를 확인해보겠음.<br>

![image](https://github.com/bcy1235/ChatServer-Practice/assets/96825479/16671a92-2b66-4414-ac53-005c91d55f08)


104bytes * 94051 => 9.7MB<br>
적은 숫자는 아니지만, 초당 2MB 증가했던 점을 생각하면 그렇게 큰 용량은 아님.<br><br>

찾아보던중, 충격적인 사실 발견; SocketStaion의 list의 크기가 내가 연결 목표로 했던 소켓 채널 개수보다 더 적다는 사실을 알아챔.<br><br>

![image](https://github.com/bcy1235/ChatServer-Practice/assets/96825479/bfb28da2-dd4d-486c-b984-74f78070c7e5)

(목표로 했던 리스트의 크기는 500)

현재 발생한 문제는 2가지.

- 서버가 잘 작동하다가 갑자기 멈춰버리는 현상, 가끔가다 다시 재작동되기는하나 바로 멈춘다.
- 내가 목표했던 DummyThread의 수보다 더 적은 쓰레드가 서버와 연결됨.<br>

&nbsp;먼저, 2번째 오류를 해결했다. 원인은 서버쪽에 있었다. DummyClient가 1000여개의 쓰레드를 생성함과 동시에 그 쓰레드들은 서버에 연결 요청을 보내게된다. 이때, 서버의 listening 포트가 유지하고 있는 backlog 큐의 길이를 벗어나게 되면서 서버의 운영체제가 들어온 요청을 거부해버린 것이다. 여기서 backlog 큐란, 클라와 서버가 첫 tcp 연결을 맺기 위해 3-way 핸드쉐이킹을 하는동안 연결 정보를 listening port에서 잠시 담고있는 공간이라고한다.<br><br>&nbsp;정리하면, 클라쪽에서 너무 많은 쓰레드가 연결과 동시에 데이터를 보내게 되고, 서버의 리스닝 쓰레드가 들어온 요청을 accept하고, accept으로 생성된 소켓 채널을 SocketStation과 SocketBuffer에 등록하면서 딜레이가 발생(synchronized로 인한)하고, 결국 listeing 포트가 꽉 차서 에러가 발생한 것으로 결론을 내렸다. 이를 해결하기 위해, 클라의 모든 쓰레드가 연결을 맺고나서 약 1초동안 기다렸다가 메시지를 보내는 것으로 수정하였다.






