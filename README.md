# ChatServer-Practice

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




