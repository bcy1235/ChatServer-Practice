# ChatServer-Practice

간단한 채팅 서버를 만든 후, 성능을 측정하고, 개선해보는 프로젝트입니다.

---

&nbsp;채팅 서버의 구조는 [바로 전 프로젝트](https://github.com/bcy1235/EchoServer-Client-Practice)의 MultiNioVersion에서 멀티 쓰레드가 추가된 서버 구조로 이루어집니다.

대략적인 객체 설계는 다음과 같습니다.

1. Logger : 로그를 저장하는 객체입니다.
2. Timer : 시간을 측정하는 객체입니다.
3. Server : 채팅 서버 역할을하는 객체입니다.
4. Client : 채팅 클라 역할을하는 객체입니다.

### Logger

&nbsp;로그를 저장할 객체이며, 로그로 무엇을 남길지, 어떻게 저장할지에대한 방식은 아직 정해지지는 않았으며, 후보는 다음과 같습니다.

1. 로그를 가지고 있다가, 한가할 때 파일로 남기기 (한가할 때가 언제인지? 또 어떻게 인지할 것인지? 만약 한가한 구간이 계속 존재하지 않는다면 발생하는 문제점은?)
2. 로그가 발생할 때마다, db에 바로바로 쏘기 또는 1번처럼 한가할 때 쏘기
3. 그냥 프로세스에서 계속 로그를 가지고있다가, 특정 키가 눌리거나 또는 특정 시간마다 로그를 파일/db에 남기기

지금 당장 떠오르는 방법은 이정도입니다.

### Timer

&nbsp;시간을 측정할 객체이며, 특정 코드 부분에 진입하기전에 시간 측정을 시작하고, 코드 부분을 빠져나오면, 측정을 종료하여 그 차이를 통해 성능을 비교할 예정입니다.

그 과정에서 여러가지 문제점이 발생할 수 있습니다.

1. 시간 측정 결과는 어떻게 기록하고 저장할 것인지?
2. 만약 시간 측정을 시작하고, 종료를 깜빡했다면(반대로 시간 측정을 시작하지도 않았는데, 종료를 시도한다면) 그걸 컴파일 타임 또는 IDE 코드 작성 중에 캐치할 수 있는 방법이 있는지?
3. 여러개의 쓰레드가 진입하는 특정 코드에 시간 측정을 시도했을 때 발생하는 문제점은 무엇이 있는지?

### Server

&nbsp; 채팅 서버입니다. 성능 측정 및 업그레이드의 주체가 되는 프로젝트의 메인 객체이며, 구체적인 구현 시, 여러가지 구성 요소로 구성될 수 있습니다.

다음은 채팅 서버에게 요구되는 역할입니다.

1. n명의 사용자가 채팅 서버에 접속하여 채팅을 시도한다.
2. 어떤 사용자가 채팅을 쳤을 때, 해당 채팅의 내용이 서버에 접속된 모든 사용자에게 보여야한다.
3. 채팅의 최대 길이는 1024바이트로 제한한다.

이때, 사용자 명수인 n과 채팅의 인풋 정도가 서버의 성능에 영향을 미칠 것으로 예상됩니다.

### Client

&nbsp; 채팅 클라입니다. 인위적으로 많은 프로세스 및 쓰레드를 생성해서, 서버에 과부하를 주는 역할을 합니다.

---

위 내용은 프로젝트 본격적인 시작 전 구상 내용이며, 아래부터 본격적인 프로젝트 진행 과정을 설명합니다.


### 서버 및 클라 동작 예시

![image](https://github.com/bcy1235/ChatServer-Practice/assets/96825479/005476f9-ec59-45c8-a4f3-fcd4ea3d2a9f)


