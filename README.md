# ola-be
- <a href="https://taesukang.click">Ola</a>
- Ola는 오르다와 Hola의 합성어입니다.
- 나의 위치와 가까운 장소에 암벽등반을 위한 팀을 꾸릴 수 있는 서비스입니다.
- <a href="https://github.com/taesukang-dev/ola-fe">Front-end Repository</a>

<br />

## 프로젝트 기간
- 2022.11.04 ~ 2022. 12. 02 (총 작업일자 23일)

<br />

## ⚒️ Architecture

![architecture](https://user-images.githubusercontent.com/44432418/205282999-a727705b-66ba-42cf-a3b7-dcaf0cf3802e.jpg)

<br />

## ℹ Information

![information](https://user-images.githubusercontent.com/44432418/205283025-afa33681-06b6-4627-940f-54c02f355fab.jpg)

<br />

## 👥 Entity Relationship Diagram

![ola-erd](https://user-images.githubusercontent.com/44432418/205283044-9075c6a4-f012-4c28-a16a-426aa228e7be.png)

<br />

## 🍽 Tech stack

| 분류        | 기술                                                                                                                                                                                                                                                                                                                       |
|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| IDE       | <img src="https://img.shields.io/badge/intellij idea-0000?style=for-the-badge&logo=IntelliJ IDEA&logoColor=white" />                                                                                                                                                                                                     |
| Language  | <img src="https://img.shields.io/badge/JAVA-0000?style=for-the-badge&logo=JAVA&logoColor=white" /> <img src="https://img.shields.io/badge/javascript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=white" />                                                                                                      |
| Library   | <img src="https://img.shields.io/badge/react-61DAFB?style=for-the-badge&logo=react&logoColor=white"> <img src="https://img.shields.io/badge/redux-764ABC?style=for-the-badge&logo=redux&logoColor=white"> <img src="https://img.shields.io/badge/react query-FF4154?style=for-the-badge&logo=React Query&logoColor=white"> |
| Framework | <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=SPRINGBOOT&logoColor=white">                                                                                                                                                                                                           |
| Build Tool | <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=GRADLE&logoColor=white">                                                                                                                                                                                                                   |
| DB        | <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=MYSQL&logoColor=white">                                                                                                                                                                                                                     |
| Server    | <img src="https://img.shields.io/badge/aws-232F3E?style=for-the-badge&logo=Amazon AWS&logoColor=white">                                                                                                                                                                                                                  |
| CI/CD     | <img src="https://img.shields.io/badge/travis ci-3EAAAF?style=for-the-badge&logo=TRAVIS CI&logoColor=white">                                                                                                                                                                                                             |
| TEST      | <img src="https://img.shields.io/badge/junit 5-25A162?style=for-the-badge&logo=JUNIT5&logoColor=white">                                                                                                                                                                                                                  |

<br />

## ☑ API 명세
- <a href="https://vast-talon-3c4.notion.site/669c550bc3b941ea931f26ccf1c25042?v=a527ccbb309440b0a8d0cd85274d582b">Notion</a>

<br />

## 🪖 Trouble Shooting

` ❓ EC2의 성능적 한계로 Docker Compose로 배포되지 않는 현상`

> ❗️ Docker compose에 같이 올리던 MySql을 AWS Rds로 교체하고, Nginx 를 AWS Application Load Balancer 로 교체 <br />
> EC2가 프리티어라 생기던 현상 <br />
> 덕분에 ec2의 안정과 더불어 Cloud Watch로 모니터링을 간편하게 구축할 수 있었음

<br />
<br />

` ❓ 테스트 중 Static Method Class에 대한 Mocking이 애매하던 상황`

> ❗ try (MockedStatic\<MockClass> Mock = mockStatic(MockClass.class)) 구문을 사용하여 필요할 때에만 mocking을 해주어 해결 <br />
> Static Method Class에 대해 beforeAll과 AfterAll을 통해 매 테스트마다 목킹하여 영향을 받이 않아야 할 테스트에서도 영향을 받았던 상황 <br />
> try 구문을 통해 원하는 때에만 목킹할 수 있었음

<br />
<br />

` ❓ 테스트 중 Entity Class를 목킹하며 실제 값이 없어 Null Pointer Exception이 나던 상황`

> ❗ Mockito 를 통해 when(mock.getId()).thenReturn() 처럼 다른 Mock Bean 처럼 적용하여 해결 <br />
> 엔티티가 실제로 필요한 함수를 호출 하는지 확인이 필요했던 상황 <br />
> 다른 Mock Bean과 마찬가지로 함수가 불리면 값을 리턴해주게 하여 null pointer exception 을 피하고, 원하는 결과를 확인할 수 있었음

<br />
<br />

` ❓ 게시글 조회시 N + 1 문제와 더불어 쿼리가 여러번 나가던 상황`

> ❗ default_batch_fetch_size 와 Join fetch 를 통해 해결 <br />
> 이후 ~~ToOne으로 끝나는 annotation은 모두 LAZY 셋팅과 더불어 쿼리에서 join fetch로 찾아올 수 있게 하였음 <br />
> 수정과 삭제가 잦지 않은 엔티티는 인덱싱 또한 하였음 <br />
> 응답시간을 약 300ms -> 약 50ms 까지 낮출 수 있었음

- 적용 전

<img width="333" alt="join_fetch1" src="https://user-images.githubusercontent.com/44432418/205297613-baf2b6e2-a6cb-437c-aa8c-553c7dac1b99.png">

- 적용 후

<img width="333" alt="join_fetch2" src="https://user-images.githubusercontent.com/44432418/205297623-2a908d01-15a3-4892-8e48-603b43c52d27.png">


<br />
<br />

` ❓ 위치정보를 불러오면서 HTTP 통신이라 확인이 안 되었던 상황`

> ❗ Cloud Front를 통해 HTTPS 통신을 지원하며 해결 <br />
> Geolocation.getCurrentPosition 가 HTTP 에서는 보안의 문제로 지원하지 않았던 상황 <br />
> 배포하면서, 클라이언트와 서버 모두 HTTPS 를 지원하며 해결하였음
